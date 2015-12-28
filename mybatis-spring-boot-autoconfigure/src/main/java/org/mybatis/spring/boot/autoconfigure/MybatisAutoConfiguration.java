/*
 *    Copyright 2010-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.mybatis.spring.boot.autoconfigure;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-Configuration} for Mybatis. Contributes a
 * {@link SqlSessionFactory} and a {@link SqlSessionTemplate}.
 *
 * If {@link org.mybatis.spring.annotation.MapperScan} is used, or a configuration file is
 * specified as a property, those will be considered, otherwise this auto-configuration
 * will attempt to register mappers based on the interface definitions in or under the
 * root auto-configuration package.
 *
 * @author Eddú Meléndez
 * @author Josh Long
 */
@Configuration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisAutoConfiguration {

	private static Log log = LogFactory.getLog(MybatisAutoConfiguration.class);

	@Autowired
	private MybatisProperties properties;

	@Autowired(required = false)
	private Interceptor[] interceptors;

	@Autowired
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	@PostConstruct
	public void checkConfigFileExists() {
		if (this.properties.isCheckConfigLocation()) {
			Resource resource = this.resourceLoader
					.getResource(this.properties.getConfig());
			Assert.state(resource.exists(),
					"Cannot find config location: " + resource
							+ " (please add config file or check your Mybatis "
							+ "configuration)");
		}
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		if (StringUtils.hasText(this.properties.getConfig())) {
			factory.setConfigLocation(
					this.resourceLoader.getResource(this.properties.getConfig()));
		} else {
			if (this.interceptors != null && this.interceptors.length > 0) {
				factory.setPlugins(this.interceptors);
			}
			factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
			factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
			factory.setMapperLocations(this.properties.getMapperLocations());
		}
		return factory.getObject();
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory,
				this.properties.getExecutorType());
	}

	/**
	 * This will just scan the same base package as Spring Boot does. If you want more
	 * power, you can explicitly use {@link org.mybatis.spring.annotation.MapperScan} but
	 * this will get typed mappers working correctly, out-of-the-box, similar to using
	 * Spring Data JPA repositories.
	 */
	public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware,
			ImportBeanDefinitionRegistrar, ResourceLoaderAware {

		private BeanFactory beanFactory;

		private ResourceLoader resourceLoader;

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {

			ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

			List<String> pkgs;
			try {
				pkgs = AutoConfigurationPackages.get(this.beanFactory);
				for (String pkg : pkgs) {
					log.debug("Found MyBatis auto-configuration package '" + pkg + "'");
				}

				if (this.resourceLoader != null) {
					scanner.setResourceLoader(this.resourceLoader);
				}

				scanner.registerFilters();
				scanner.doScan(pkgs.toArray(new String[pkgs.size()]));
			}
			catch (IllegalStateException ex) {
				log.debug("Could not determine auto-configuration "
						+ "package, automatic mapper scanning disabled.");
			}
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}
	}

	/**
	 * {@link org.mybatis.spring.annotation.MapperScan} ultimately ends up creating
	 * instances of {@link MapperFactoryBean}. If
	 * {@link org.mybatis.spring.annotation.MapperScan} is used then this
	 * auto-configuration is not needed. If it is _not_ used, however, then this will
	 * bring in a bean registrar and automatically register components based on the same
	 * component-scanning path as Spring Boot itself.
	 */
	@Configuration
	@Import({ AutoConfiguredMapperScannerRegistrar.class })
	@ConditionalOnMissingBean(MapperFactoryBean.class)
	public static class MapperScannerRegistrarNotFoundConfiguration {

		@PostConstruct
		public void afterPropertiesSet() {
			log.debug(String.format("No %s found.", MapperFactoryBean.class.getName()));
		}
	}

}

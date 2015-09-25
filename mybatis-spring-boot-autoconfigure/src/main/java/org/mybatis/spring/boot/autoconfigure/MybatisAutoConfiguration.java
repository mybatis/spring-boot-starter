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

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-Configuration} for Mybatis.
 *
 * @author Eddú Meléndez
 */
@Configuration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisAutoConfiguration {

	@Autowired
	private MybatisProperties properties;

	@Autowired
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	@PostConstruct
	public void checkConfigFileExists() {
		if (this.properties.isCheckConfigLocation()) {
			Resource resource = this.resourceLoader.getResource(this.properties
					.getConfig());
			Assert.state(resource.exists(), "Cannot find config location: " + resource
					+ " (please add config file or check your Mybatis " + "configuration)");
		}
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		if(StringUtils.hasText(this.properties.getConfig())) {
			factory.setConfigLocation(this.resourceLoader.getResource(this.properties
					.getConfig()));
		}
		return factory.getObject();
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory,
				this.properties.getExecutorType());
	}

}

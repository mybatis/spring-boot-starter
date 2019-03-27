/**
 *    Copyright 2015-2019 the original author or authors.
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

import java.math.BigInteger;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.handler.DummyTypeHandler;
import org.mybatis.spring.boot.autoconfigure.mapper.CityMapper;
import org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link MybatisAutoConfiguration}
 *
 * @author Eddú Meléndez
 * @author Josh Long
 * @author Kazuki Shimizu
 */
public class MybatisAutoConfigurationTest {

	private AnnotationConfigApplicationContext context;

	@Before
	public void init() {
		this.context = new AnnotationConfigApplicationContext();
	}

	@After
	public void closeContext() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void testNoDataSource() throws Exception {
		this.context.register(MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).isEmpty();
	}

	@Test
	public void testDefaultConfiguration() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisScanMapperConfiguration.class, MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase()).isFalse();
	}

	@Test
	public void testWithConfigLocation() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MybatisMapperConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapperImpl.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.BATCH);
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase()).isTrue();
	}

	@Test
	public void testWithConfig() {
		// test for compatibility with 1.0.x
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config:mybatis-config.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MybatisMapperConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapperImpl.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.BATCH);
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase()).isTrue();
	}

	@Test
	public void testWithCheckConfigLocationFileExists() {
		EnvironmentTestUtils
				.addEnvironment(this.context, "mybatis.config-location:mybatis-config.xml",
						"mybatis.check-config-location=true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
	}

	@Test
	public void testWithCheckConfigLocationFileNotSpecify() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.check-config-location=true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
	}

	@Test
	public void testWithCheckConfigLocationFileDoesNotExists() {

		EnvironmentTestUtils.addEnvironment(this.context, "mybatis.config-location:foo.xml",
				"mybatis.check-config-location=true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);

		try {
			this.context.refresh();
			fail("Should be occurred a BeanCreationException.");
		} catch (BeanCreationException e) {
			assertThat(e.getMessage()).isEqualTo("Error creating bean with name 'mybatisAutoConfiguration': Invocation of init method failed; nested exception is java.lang.IllegalStateException: Cannot find config location: class path resource [foo.xml] (please add config file or check your Mybatis configuration)");
		}
	}

	@Test
	public void testWithTypeHandlersPackage() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.type-handlers-package:org.mybatis.spring.boot.autoconfigure.handler");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();

		TypeHandlerRegistry typeHandlerRegistry = this.context.getBean(SqlSessionFactory.class).getConfiguration().getTypeHandlerRegistry();
		assertThat(typeHandlerRegistry.hasTypeHandler(BigInteger.class)).isTrue();
	}

	@Test
	public void testWithMapperLocation() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
				"mybatis.mapper-locations:classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().getMappedStatementNames()).hasSize(2);
	}

	@Test
	public void testWithExecutorType() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config.xml", "mybatis.executor-type:REUSE");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MybatisMapperConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.REUSE);
	}

	@Test
	public void testDefaultBootConfiguration() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
	}

	@Test
	public void testWithInterceptors() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisInterceptorConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors()).hasSize(1);
		this.context.close();
	}

	@Test
	public void testWithDatabaseIdProvider() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				DatabaseProvidersConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().getDatabaseId()).isEqualTo("h2");
	}

	@Test
	public void testMixedWithConfigurationFileAndInterceptor() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config-settings-only.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisInterceptorConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
				SqlSessionFactory.class).getConfiguration();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
		assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
		assertThat(configuration.getInterceptors()).hasSize(1);
		assertThat(configuration.getInterceptors().get(0)).isInstanceOf(MyInterceptor.class);
	}

	@Test
	public void testMixedWithConfigurationFileAndDatabaseIdProvider() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config-settings-only.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class,
				DatabaseProvidersConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
				SqlSessionFactory.class).getConfiguration();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
		assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
		assertThat(configuration.getDatabaseId()).isEqualTo("h2");
	}

	@Test
	public void testMixedWithConfigurationFileAndTypeHandlersPackage() {
		EnvironmentTestUtils
				.addEnvironment(this.context,
						"mybatis.config-location:mybatis-config-settings-only.xml",
						"mybatis.type-handlers-package:org.mybatis.spring.boot.autoconfigure.handler.");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
				SqlSessionFactory.class).getConfiguration();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
		assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
		assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(BigInteger.class)).isInstanceOf(DummyTypeHandler.class);
	}

	@Test
	public void testMixedWithConfigurationFileAndTypeAliasesPackageAndMapperLocations() {
		EnvironmentTestUtils
				.addEnvironment(
						this.context,
						"mybatis.config-location:mybatis-config-settings-only.xml",
						"mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
						"mybatis.mapper-locations:classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
				SqlSessionFactory.class).getConfiguration();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
		assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
		assertThat(configuration.getMappedStatementNames()).contains("selectCityById");
		assertThat(configuration.getMappedStatementNames()).contains("org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl.selectCityById");
		assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("city");
		assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("name");
	}

	@Test
	public void testMixedWithFullConfigurations() {
		EnvironmentTestUtils
				.addEnvironment(
						this.context,
						"mybatis.config-location:mybatis-config-settings-only.xml",
						"mybatis.type-handlers-package:org.mybatis.spring.boot.autoconfigure.handler",
						"mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
						"mybatis.mapper-locations:classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml",
						"mybatis.executor-type=REUSE");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class,
				MybatisInterceptorConfiguration.class,
				DatabaseProvidersConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
				SqlSessionFactory.class).getConfiguration();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBeanNamesForType(CityMapper.class)).hasSize(1);
		assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
		assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(BigInteger.class)).isInstanceOf(DummyTypeHandler.class);
		assertThat(configuration.getMappedStatementNames()).hasSize(4);
		assertThat(configuration.getMappedStatementNames()).contains("selectCityById");
		assertThat(configuration.getMappedStatementNames()).contains("org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl.selectCityById");
		assertThat(configuration.getMappedStatementNames()).contains("findById");
		assertThat(configuration.getMappedStatementNames()).contains("org.mybatis.spring.boot.autoconfigure.mapper.CityMapper.findById");
		assertThat(this.context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.REUSE);
		assertThat(configuration.getInterceptors()).hasSize(1);
		assertThat(configuration.getInterceptors().get(0)).isInstanceOf(MyInterceptor.class);
		assertThat(configuration.getDatabaseId()).isEqualTo("h2");
	}

	@Test
	public void testWithMyBatisConfiguration() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration.map-underscore-to-camel-case:true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase()).isTrue();
	}

	@Test
	public void testWithMyBatisConfigurationCustomizeByJavaConfig() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration.default-fetch-size:100");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				MybatisPropertiesConfigurationCustomizer.class);
		this.context.refresh();
		SqlSessionFactory sqlSessionFactory = this.context
				.getBean(SqlSessionFactory.class);
		assertThat(sqlSessionFactory.getConfiguration().getDefaultFetchSize()).isEqualTo(100);
		assertThat(sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().getTypeHandler(BigInteger.class)).isInstanceOf(DummyTypeHandler.class);
	}

	@Test
	public void testWithMyBatisConfigurationCustomizer() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
			MybatisAutoConfiguration.class,
			MyBatisConfigurationCustomizerConfiguration.class);
		this.context.refresh();
		SqlSessionFactory sqlSessionFactory = this.context
			.getBean(SqlSessionFactory.class);
		assertThat(sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().getTypeHandler(BigInteger.class)).isInstanceOf(DummyTypeHandler.class);
		assertThat(sqlSessionFactory.getConfiguration().getCache("test")).isNotNull();
	}

	@Test
	public void testConfigFileAndConfigurationWithTogether() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config.xml",
				"mybatis.configuration.default-statement-timeout:30");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);

		try {
			this.context.refresh();
			fail("Should be occurred a BeanCreationException.");
		} catch (BeanCreationException e) {
			assertThat(e.getMessage()).contains("Property 'configuration' and 'configLocation' can not specified with together");
		}
	}

	@Test
	public void testWithoutConfigurationVariablesAndProperties() {
		EnvironmentTestUtils.addEnvironment(this.context);
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();

		Properties variables = this.context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
		assertThat(variables).isEmpty();
	}

	@Test
	public void testWithConfigurationVariablesOnly() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration.variables.key1:value1");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();

		Properties variables = this.context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
		assertThat(variables).hasSize(1);
		assertThat(variables.getProperty("key1")).isEqualTo("value1");
	}

	@Test
	public void testWithConfigurationPropertiesOnly() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration-properties.key2:value2");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();

		Properties variables = this.context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
		assertThat(variables).hasSize(1);
		assertThat(variables.getProperty("key2")).isEqualTo("value2");
	}

	@Test
	public void testWithConfigurationVariablesAndPropertiesOtherKey() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration.variables.key1:value1",
				"mybatis.configuration-properties.key2:value2");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();

		Properties variables = this.context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
		assertThat(variables).hasSize(2);
		assertThat(variables.getProperty("key1")).isEqualTo("value1");
		assertThat(variables.getProperty("key2")).isEqualTo("value2");
	}

	@Test
	public void testWithConfigurationVariablesAndPropertiesSameKey() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration.variables.key:value1",
				"mybatis.configuration-properties.key:value2");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();

		Properties variables = this.context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
		assertThat(variables).hasSize(1);
		assertThat(variables.getProperty("key")).isEqualTo("value2");
	}

	@Test
	public void testCustomSqlSessionFactory() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, CustomSqlSessionFactoryConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionFactory.class).getConfiguration().getVariables().getProperty("key")).isEqualTo("value");
	}

	@Test
	public void testMySqlSessionFactory() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MySqlSessionFactoryConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionFactory.class)).isInstanceOf(MySqlSessionFactory.class);
	}

	@Test
	public void testCustomSqlSessionTemplate() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, CustomSqlSessionTemplateConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.BATCH);
	}

	@Test
	public void testMySqlSessionTemplate() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MySqlSessionTemplateConfiguration.class);
		this.context.refresh();
		assertThat(this.context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
		assertThat(this.context.getBean(SqlSessionTemplate.class)).isInstanceOf(MySqlSessionTemplate.class);
	}

	@Test
	public void testTypeAliasesSuperTypeIsSpecify() {
		EnvironmentTestUtils
			.addEnvironment(
				this.context,
				"mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
				"mybatis.type-aliases-super-type:org.mybatis.spring.boot.autoconfigure.domain.Domain");
		this.context.register(EmbeddedDataSourceConfiguration.class,
			MybatisBootMapperScanAutoConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
			SqlSessionFactory.class).getConfiguration();
		assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("city");
		assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).doesNotContainKey("name");
	}

	@Configuration
	@EnableAutoConfiguration
	@MapperScan("org.mybatis.spring.boot.autoconfigure.mapper")
	static class MybatisScanMapperConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	static class MybatisBootMapperScanAutoConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	static class MybatisMapperConfiguration {

		@Bean
		public CityMapperImpl cityMapper() {
			return new CityMapperImpl();
		}

	}

	@Configuration
	@EnableAutoConfiguration
	static class MybatisInterceptorConfiguration {

		@Bean
		public MyInterceptor myInterceptor() {
			return new MyInterceptor();
		}

	}

	@Configuration
	@EnableAutoConfiguration
	static class MybatisPropertiesConfigurationCustomizer {
		@Autowired
		void customize(MybatisProperties properties) {
			properties.getConfiguration().getTypeHandlerRegistry()
					.register(new DummyTypeHandler());
		}
	}

	@Configuration
	@EnableAutoConfiguration
	static class MyBatisConfigurationCustomizerConfiguration {
		@Bean
		ConfigurationCustomizer typeHandlerConfigurationCustomizer() {
			return new ConfigurationCustomizer() {
				@Override
				public void customize(org.apache.ibatis.session.Configuration configuration) {
					configuration.getTypeHandlerRegistry()
						.register(new DummyTypeHandler());
				}
			};
		}
		@Bean
		ConfigurationCustomizer cacheConfigurationCustomizer() {
			return new ConfigurationCustomizer() {
				@Override
				public void customize(org.apache.ibatis.session.Configuration configuration) {
					configuration.addCache(new PerpetualCache("test"));
				}
			};
		}
	}

	@Intercepts(
			@Signature(type = Map.class, method = "get", args = { Object.class })
	)
	static class MyInterceptor implements Interceptor {

		@Override
		public Object intercept(Invocation invocation) throws Throwable {
			return "Test";
		}

		@Override
		public Object plugin(Object target) {
			return Plugin.wrap(target, this);
		}

		@Override
		public void setProperties(Properties properties) {

		}
	}

	@Configuration
	static class DatabaseProvidersConfiguration {

		@Bean
		public PropertiesFactoryBean vendorProperties() {
			Properties properties = new Properties();
			properties.put("SQL Server", "sqlserver");
			properties.put("DB2", "db2");
			properties.put("H2", "h2");

			PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
			propertiesFactoryBean.setProperties(properties);
			return propertiesFactoryBean;
		}

		@Bean
		public VendorDatabaseIdProvider vendorDatabaseIdProvider(Properties vendorProperties) {
			VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
			databaseIdProvider.setProperties(vendorProperties);
			return databaseIdProvider;
		}

	}

	@Configuration
	static class CustomSqlSessionFactoryConfiguration {
		@Bean
		public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
			SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
			sqlSessionFactoryBean.setDataSource(dataSource);
			Properties props = new Properties();
			props.setProperty("key", "value");
			sqlSessionFactoryBean.setConfigurationProperties(props);
			return sqlSessionFactoryBean.getObject();
		}
	}

	@Configuration
	static class MySqlSessionFactoryConfiguration {
		@Bean
		public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
			MySqlSessionFactory sqlSessionFactory = new MySqlSessionFactory(new org.apache.ibatis.session.Configuration());
			sqlSessionFactory.getConfiguration().setEnvironment(new Environment("",new SpringManagedTransactionFactory(),dataSource));
			return sqlSessionFactory;
		}
	}

	@Configuration
	static class CustomSqlSessionTemplateConfiguration {
		@Bean
		public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory){
			return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
		}
	}

	@Configuration
	static class MySqlSessionTemplateConfiguration {
		@Bean
		public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory){
			return new MySqlSessionTemplate(sqlSessionFactory);
		}
	}

	static class MySqlSessionFactory extends DefaultSqlSessionFactory {
		public MySqlSessionFactory(org.apache.ibatis.session.Configuration configuration) {
			super(configuration);
		}
	}

	static class MySqlSessionTemplate extends SqlSessionTemplate {
		public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
			super(sqlSessionFactory);
		}
	}

}

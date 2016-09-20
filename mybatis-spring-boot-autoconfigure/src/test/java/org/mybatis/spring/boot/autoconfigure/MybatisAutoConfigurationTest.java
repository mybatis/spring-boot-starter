/**
 *    Copyright 2015-2016 the original author or authors.
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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.handler.DummyTypeHandler;
import org.mybatis.spring.boot.autoconfigure.mapper.CityMapper;
import org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tests for {@link MybatisAutoConfiguration}
 *
 * @author Eddú Meléndez
 * @author Josh Long
 * @author Kazuki Shimizu
 */
public class MybatisAutoConfigurationTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

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
		assertEquals(0, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
	}

	@Test
	public void testDefaultConfiguration() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisScanMapperConfiguration.class, MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1,
				this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
		assertEquals(ExecutorType.SIMPLE, this.context.getBean(SqlSessionTemplate.class).getExecutorType());
		assertFalse(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase());
	}

	@Test
	public void testWithConfigLocation() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config.xml");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MybatisMapperConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapperImpl.class).length);
		assertEquals(ExecutorType.BATCH, this.context.getBean(SqlSessionTemplate.class).getExecutorType());
		assertTrue(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase());
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
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapperImpl.class).length);
		assertEquals(ExecutorType.BATCH, this.context.getBean(SqlSessionTemplate.class).getExecutorType());
		assertTrue(this.context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase());
	}

	@Test
	public void testWithCheckConfigLocationFileExists() {
		EnvironmentTestUtils
				.addEnvironment(this.context, "mybatis.config-location:mybatis-config.xml",
						"mybatis.check-config-location=true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
	}

	@Test
	public void testWithCheckConfigLocationFileNotSpecify() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.check-config-location=true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
	}

	@Test
	public void testWithCheckConfigLocationFileDoesNotExists() {

		EnvironmentTestUtils.addEnvironment(this.context, "mybatis.config-location:foo.xml",
				"mybatis.check-config-location=true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);

		expectedException.expect(isA(BeanCreationException.class));
		expectedException
				.expectMessage(is("Error creating bean with name 'mybatisAutoConfiguration': Invocation of init method failed; nested exception is java.lang.IllegalStateException: Cannot find config location: class path resource [foo.xml] (please add config file or check your Mybatis configuration)"));

		this.context.refresh();
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
		assertTrue(typeHandlerRegistry.hasTypeHandler(BigInteger.class));
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
		assertEquals(2, this.context.getBean(SqlSessionFactory.class).getConfiguration().getMappedStatementNames().size());
	}

	@Test
	public void testWithExecutorType() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config.xml", "mybatis.executor-type:REUSE");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class, MybatisMapperConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertEquals(ExecutorType.REUSE, this.context.getBean(SqlSessionTemplate.class)
				.getExecutorType());
	}

	@Test
	public void testDefaultBootConfiguration() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1,
				this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
	}

	@Test
	public void testWithInterceptors() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisInterceptorConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1,
				this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors().size());
		this.context.close();
	}

	@Test
	public void testWithDatabaseIdProvider() {
		this.context.register(EmbeddedDataSourceConfiguration.class,
				DatabaseProvidersConfiguration.class,
				MybatisAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
		assertEquals("h2", this.context.getBean(SqlSessionFactory.class).getConfiguration().getDatabaseId());
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
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
		assertEquals(Integer.valueOf(1000), configuration.getDefaultFetchSize());
		assertEquals(1, configuration.getInterceptors().size());
		assertEquals(MyInterceptor.class, configuration.getInterceptors().get(0)
				.getClass());
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
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
		assertEquals(Integer.valueOf(1000), configuration.getDefaultFetchSize());
		assertEquals("h2", configuration.getDatabaseId());
	}

	@Test
	public void testMixedWithConfigurationFileAndTypeHandlersPackage() {
		EnvironmentTestUtils
				.addEnvironment(this.context,
						"mybatis.config-location:mybatis-config-settings-only.xml",
						"mybatis.type-handlers-package:org.mybatis.spring.boot.autoconfigure.handler");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisBootMapperScanAutoConfiguration.class);
		this.context.refresh();

		org.apache.ibatis.session.Configuration configuration = this.context.getBean(
				SqlSessionFactory.class).getConfiguration();
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
		assertEquals(Integer.valueOf(1000), configuration.getDefaultFetchSize());
		assertEquals(DummyTypeHandler.class, configuration.getTypeHandlerRegistry()
				.getTypeHandler(BigInteger.class).getClass());
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
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
		assertEquals(Integer.valueOf(1000), configuration.getDefaultFetchSize());
		assertTrue(configuration.getMappedStatementNames().contains("selectCityById"));
		assertTrue(configuration
				.getMappedStatementNames()
				.contains(
						"org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl.selectCityById"));
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
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionFactory.class).length);
		assertEquals(1, this.context.getBeanNamesForType(SqlSessionTemplate.class).length);
		assertEquals(1, this.context.getBeanNamesForType(CityMapper.class).length);
		assertEquals(Integer.valueOf(1000), configuration.getDefaultFetchSize());
		assertEquals(DummyTypeHandler.class, configuration.getTypeHandlerRegistry()
				.getTypeHandler(BigInteger.class).getClass());
		assertEquals(4, configuration.getMappedStatementNames().size());
		assertTrue(configuration.getMappedStatementNames().contains("selectCityById"));
		assertTrue(configuration
				.getMappedStatementNames()
				.contains(
						"org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl.selectCityById"));
		assertTrue(configuration.getMappedStatementNames().contains("findById"));
		assertTrue(configuration.getMappedStatementNames().contains(
				"org.mybatis.spring.boot.autoconfigure.mapper.CityMapper.findById"));
		assertEquals(ExecutorType.REUSE, this.context.getBean(SqlSessionTemplate.class)
				.getExecutorType());
		assertEquals(1, configuration.getInterceptors().size());
		assertEquals(MyInterceptor.class, configuration.getInterceptors().get(0)
				.getClass());
		assertEquals("h2", configuration.getDatabaseId());
	}

	@Test
	public void testWithMyBatisConfiguration() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.configuration.map-underscore-to-camel-case:true");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);
		this.context.refresh();
		assertTrue(this.context.getBean(SqlSessionFactory.class).getConfiguration()
				.isMapUnderscoreToCamelCase());
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
		assertEquals(100, sqlSessionFactory.getConfiguration().getDefaultFetchSize()
				.intValue());
		assertEquals(DummyTypeHandler.class, sqlSessionFactory.getConfiguration()
				.getTypeHandlerRegistry().getTypeHandler(BigInteger.class).getClass());
	}

	@Test
	public void testConfigFileAndConfigurationWithTogether() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"mybatis.config-location:mybatis-config.xml",
				"mybatis.configuration.default-statement-timeout:30");
		this.context.register(EmbeddedDataSourceConfiguration.class,
				MybatisAutoConfiguration.class);

		expectedException.expect(isA(BeanCreationException.class));
		expectedException
				.expectMessage("Property 'configuration' and 'configLocation' can not specified with together");

		this.context.refresh();
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

}

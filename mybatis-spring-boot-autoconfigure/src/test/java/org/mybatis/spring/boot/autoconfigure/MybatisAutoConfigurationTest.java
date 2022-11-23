/*
 *    Copyright 2015-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mapper.DateTimeMapper;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver;
import org.mybatis.scripting.velocity.VelocityLanguageDriver;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.handler.AtomicNumberTypeHandler;
import org.mybatis.spring.boot.autoconfigure.handler.DummyTypeHandler;
import org.mybatis.spring.boot.autoconfigure.mapper.CityMapper;
import org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import liquibase.integration.spring.SpringLiquibase;

/**
 * Tests for {@link MybatisAutoConfiguration}
 *
 * @author Eddú Meléndez
 * @author Josh Long
 * @author Kazuki Shimizu
 */
class MybatisAutoConfigurationTest {

  private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(MybatisAutoConfiguration.class));

  @Test
  void testNoDataSource() {
    this.contextRunner.withUserConfiguration(PropertyPlaceholderAutoConfiguration.class).run(context -> {
      assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).isEmpty();
      assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).isEmpty();
      assertThat(context.getBeanNamesForType(MybatisProperties.class)).isEmpty();
    });
  }

  @Test
  void testMultipleDataSource() {
    this.contextRunner
        .withUserConfiguration(MultipleDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).isEmpty();
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).isEmpty();
          assertThat(context.getBeanNamesForType(MybatisProperties.class)).isEmpty();
        });
  }

  @Test
  void testSingleCandidateDataSource() {
    this.contextRunner
        .withUserConfiguration(SingleCandidateDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(MybatisProperties.class)).hasSize(1);
        });
  }

  @Test
  void testDefaultConfiguration() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisLanguageDriverAutoConfiguration.class,
            MybatisScanMapperConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(DateTimeMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
          Map<String, LanguageDriver> languageDriverBeans = context.getBeansOfType(LanguageDriver.class);
          assertThat(languageDriverBeans).hasSize(3).containsKeys("freeMarkerLanguageDriver", "velocityLanguageDriver",
              "thymeleafLanguageDriver");
          assertThat(languageDriverBeans.get("freeMarkerLanguageDriver")).isInstanceOf(FreeMarkerLanguageDriver.class);
          assertThat(languageDriverBeans.get("velocityLanguageDriver")).isInstanceOf(VelocityLanguageDriver.class);
          assertThat(languageDriverBeans.get("thymeleafLanguageDriver")).isInstanceOf(ThymeleafLanguageDriver.class);
          LanguageDriverRegistry languageDriverRegistry = sqlSessionFactory.getConfiguration().getLanguageRegistry();
          assertThat(languageDriverRegistry.getDefaultDriverClass()).isEqualTo(XMLLanguageDriver.class);
          assertThat(languageDriverRegistry.getDefaultDriver()).isInstanceOf(XMLLanguageDriver.class);
          assertThat(languageDriverRegistry.getDriver(XMLLanguageDriver.class)).isNotNull();
          assertThat(languageDriverRegistry.getDriver(RawLanguageDriver.class)).isNotNull();
          assertThat(languageDriverRegistry.getDriver(FreeMarkerLanguageDriver.class)).isNotNull();
          assertThat(languageDriverRegistry.getDriver(VelocityLanguageDriver.class)).isNotNull();
          assertThat(languageDriverRegistry.getDriver(ThymeleafLanguageDriver.class)).isNotNull();
        });
  }

  @Test
  void testScanWithLazy() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisScanMapperConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.lazy-initialization:true").run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(0);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(DateTimeMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
          context.getBean(DateTimeMapper.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
        });
  }

  @Test
  void testAutoScanWithDefault() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class, CityMapperRepositoryConfiguration.class)
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
          context.getBean(CityMapper.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(((RuntimeBeanReference) context.getBeanFactory().getBeanDefinition("cityMapper")
              .getPropertyValues().getPropertyValue("sqlSessionTemplate").getValue()).getBeanName())
                  .isEqualTo("sqlSessionTemplate");
          assertThat(context.getBeanFactory()
              .getBeanDefinition(context.getBeanNamesForType(MapperScannerConfigurer.class)[0]).getRole())
                  .isEqualTo(BeanDefinition.ROLE_INFRASTRUCTURE);
        });
  }

  @Test
  void testAutoScanWithInjectSqlSessionOnMapperScanIsFalse() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class, CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.inject-sql-session-on-mapper-scan:false").run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
          context.getBean(CityMapper.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(context.getBeanFactory().getBeanDefinition("cityMapper").getPropertyValues()
              .getPropertyValue("sqlSessionTemplate")).isNull();
          assertThat(context.getBeanFactory().getBeanDefinition("cityMapper").getPropertyValues()
              .getPropertyValue("sqlSessionFactory")).isNull();
        });
  }

  @Test
  void testAutoScanWithLazy() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class, CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.lazy-initialization:true").run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(0);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
          context.getBean(CityMapper.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
        });
  }

  @Test
  void testAutoScanWithDefaultScope() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class, CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.mapper-default-scope:thread").run(context -> {
          context.getBean(CityMapper.class);
          BeanDefinition bd = context.getBeanFactory().getBeanDefinition("cityMapper");
          assertThat(bd.getBeanClassName()).isEqualTo(ScopedProxyFactoryBean.class.getName());
          BeanDefinition spbd = context.getBeanFactory().getBeanDefinition("scopedTarget.cityMapper");
          assertThat(spbd.getBeanClassName()).isEqualTo(MapperFactoryBean.class.getName());
          assertThat(spbd.getScope()).isEqualTo("thread");
        });
  }

  @Test
  void testAutoScanWithoutDefaultScope() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class, CityMapperRepositoryConfiguration.class)
        .run(context -> {
          context.getBean(CityMapper.class);
          BeanDefinition df = context.getBeanFactory().getBeanDefinition("cityMapper");
          assertThat(df.getBeanClassName()).isEqualTo(MapperFactoryBean.class.getName());
          assertThat(df.getScope()).isEqualTo("singleton");
        });
  }

  @Test
  void testWithConfigLocation() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisMapperConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config.xml").run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapperImpl.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.BATCH);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase()).isTrue();
        });
  }

  @Test
  void testWithCheckConfigLocationFileExists() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisAutoConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config.xml", "mybatis.check-config-location=true")
        .run(context -> assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1));
  }

  @Test
  void testWithCheckConfigLocationFileNotSpecify() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
        .withPropertyValues("mybatis.check-config-location=true")
        .run(context -> assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1));
  }

  @Test
  void testWithCheckConfigLocationFileDoesNotExists() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
        .withPropertyValues("mybatis.config-location:foo.xml", "mybatis.check-config-location=true")
        .run(context -> assertThat(context).getFailure().isInstanceOf(BeanCreationException.class)
            .hasMessageContainingAll("Error creating bean with name 'mybatisAutoConfiguration':",
                "Cannot find config location: class path resource [foo.xml] (please add config file or check your Mybatis configuration)"));
  }

  @Test
  void testWithTypeHandlersPackage() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.type-handlers-package:org.mybatis.spring.boot.autoconfigure.handler")
        .run(context -> {
          TypeHandlerRegistry typeHandlerRegistry = context.getBean(SqlSessionFactory.class).getConfiguration()
              .getTypeHandlerRegistry();
          assertThat(typeHandlerRegistry.hasTypeHandler(BigInteger.class)).isTrue();
          assertThat(typeHandlerRegistry.hasTypeHandler(AtomicInteger.class)).isTrue();
          assertThat(typeHandlerRegistry.hasTypeHandler(AtomicLong.class)).isTrue();
        });
  }

  @Test
  void testWithMapperLocation() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
            "mybatis.mapper-locations:classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml")
        .run(
            context -> assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getMappedStatementNames())
                .hasSize(2));
  }

  @Test
  void testWithExecutorType() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisMapperConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config.xml", "mybatis.executor-type:REUSE")
        .run(context -> assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType())
            .isEqualTo(ExecutorType.REUSE));
  }

  @Test
  void testDefaultBootConfiguration() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class, CityMapperRepositoryConfiguration.class)
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
        });
  }

  @Test
  void testWithInterceptorsOrder1() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        MybatisInterceptorConfiguration.class, PropertyPlaceholderAutoConfiguration.class).run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors()).hasSize(2);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors().get(0))
              .isInstanceOf(MyInterceptor2.class);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors().get(1))
              .isInstanceOf(MyInterceptor.class);
        });
  }

  @Test
  void testWithInterceptorsOrder2() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        MybatisInterceptorConfiguration2.class, PropertyPlaceholderAutoConfiguration.class).run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors()).hasSize(2);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors().get(0))
              .isInstanceOf(MyInterceptor.class);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getInterceptors().get(1))
              .isInstanceOf(MyInterceptor2.class);
        });
  }

  @Test
  void testWithTypeHandlers() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        MybatisTypeHandlerConfiguration.class, PropertyPlaceholderAutoConfiguration.class).run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getTypeHandlerRegistry()
              .getTypeHandler(UUID.class)).isInstanceOf(MyTypeHandler.class);
        });
  }

  @Test
  void testWithDatabaseIdProvider() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, DatabaseProvidersConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class)
        .run(context -> assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDatabaseId())
            .isEqualTo("h2"));
  }

  @Test
  void testMixedWithConfigurationFileAndInterceptor() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisInterceptorConfiguration.class,
            CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config-settings-only.xml").run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
          assertThat(configuration.getInterceptors()).hasSize(2);
          assertThat(configuration.getInterceptors().get(0)).isInstanceOf(MyInterceptor2.class);
          assertThat(configuration.getInterceptors().get(1)).isInstanceOf(MyInterceptor.class);
        });
  }

  @Test
  void testMixedWithConfigurationFileAndDatabaseIdProvider() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            DatabaseProvidersConfiguration.class, CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config-settings-only.xml").run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
          assertThat(configuration.getDatabaseId()).isEqualTo("h2");
        });
  }

  @Test
  void testMixedWithConfigurationFileAndTypeHandlersPackage() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config-settings-only.xml",
            "mybatis.type-handlers-package:org.mybatis.spring.boot.autoconfigure.handler.")
        .run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(BigInteger.class))
              .isInstanceOf(DummyTypeHandler.class);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(AtomicInteger.class))
              .isInstanceOf(AtomicNumberTypeHandler.class);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(AtomicLong.class))
              .isInstanceOf(AtomicNumberTypeHandler.class);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(AtomicInteger.class))
              .hasToString("type=" + AtomicInteger.class);
        });
  }

  @Test
  void testMixedWithConfigurationFileAndTypeAliasesPackageAndMapperLocations() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config-settings-only.xml",
            "mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
            "mybatis.mapper-locations:classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml")
        .run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
          assertThat(configuration.getMappedStatementNames()).contains("selectCityById");
          assertThat(configuration.getMappedStatementNames())
              .contains("org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl.selectCityById");
          assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("city");
          assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("name");
        });
  }

  @Test
  void testMixedWithFullConfigurations() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            MybatisInterceptorConfiguration.class, DatabaseProvidersConfiguration.class,
            CityMapperRepositoryConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config-settings-only.xml",
            "mybatis.type-handlers-package:org.mybatis.spring.**.handler",
            "mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
            "mybatis.mapper-locations:classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml",
            "mybatis.executor-type=REUSE")
        .run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(configuration.getDefaultFetchSize()).isEqualTo(1000);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(BigInteger.class))
              .isInstanceOf(DummyTypeHandler.class);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(AtomicInteger.class))
              .isInstanceOf(AtomicNumberTypeHandler.class);
          assertThat(configuration.getTypeHandlerRegistry().getTypeHandler(AtomicLong.class))
              .isInstanceOf(AtomicNumberTypeHandler.class);
          assertThat(configuration.getMappedStatementNames()).hasSize(4);
          assertThat(configuration.getMappedStatementNames()).contains("selectCityById");
          assertThat(configuration.getMappedStatementNames())
              .contains("org.mybatis.spring.boot.autoconfigure.repository.CityMapperImpl.selectCityById");
          assertThat(configuration.getMappedStatementNames()).contains("findById");
          assertThat(configuration.getMappedStatementNames())
              .contains("org.mybatis.spring.boot.autoconfigure.mapper.CityMapper.findById");
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.REUSE);
          assertThat(configuration.getInterceptors()).hasSize(2);
          assertThat(configuration.getInterceptors().get(0)).isInstanceOf(MyInterceptor2.class);
          assertThat(configuration.getInterceptors().get(1)).isInstanceOf(MyInterceptor.class);
          assertThat(configuration.getDatabaseId()).isEqualTo("h2");
        });
  }

  @Test
  void testWithMyBatisConfiguration() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
        .withPropertyValues("mybatis.configuration.map-underscore-to-camel-case:true").run(context -> assertThat(
            context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase()).isTrue());
  }

  @Test
  void testWithMyBatisConfigurationCustomizer() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MyBatisConfigurationCustomizerConfiguration.class)
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().getTypeHandler(BigInteger.class))
              .isInstanceOf(DummyTypeHandler.class);
          assertThat(sqlSessionFactory.getConfiguration().getCache("test")).isNotNull();
        });
  }

  @Test
  void testWithSqlSessionFactoryBeanCustomizer() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        SqlSessionFactoryBeanCustomizerConfiguration.class).run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().getTypeHandler(BigInteger.class))
              .isInstanceOf(DummyTypeHandler.class);
          assertThat(sqlSessionFactory.getConfiguration().getCache("test")).isNotNull();
        });
  }

  @Test
  void testConfigFileAndConfigurationWithTogether() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config.xml",
            "mybatis.configuration.default-statement-timeout:30")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context).getFailure().isInstanceOf(BeanCreationException.class)
              .hasMessageContaining("Property 'configuration' and 'configLocation' can not specified with together");
        });
  }

  @Test
  void testWithoutConfigurationVariablesAndProperties() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .run(context -> {
          Properties variables = context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
          assertThat(variables).isEmpty();
        });
  }

  @Test
  void testWithConfigurationVariablesOnly() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.configuration.variables.key1:value1").run(context -> {
          Properties variables = context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
          assertThat(variables).hasSize(1);
          assertThat(variables.getProperty("key1")).isEqualTo("value1");
        });
  }

  @Test
  void testWithConfigurationPropertiesOnly() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.configuration-properties.key2:value2").run(context -> {
          Properties variables = context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
          assertThat(variables).hasSize(1);
          assertThat(variables.getProperty("key2")).isEqualTo("value2");
        });
  }

  @Test
  void testWithConfigurationVariablesAndPropertiesOtherKey() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.configuration.variables.key1:value1",
            "mybatis.configuration-properties.key2:value2")
        .run(context -> {
          Properties variables = context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
          assertThat(variables).hasSize(2);
          assertThat(variables.getProperty("key1")).isEqualTo("value1");
          assertThat(variables.getProperty("key2")).isEqualTo("value2");
        });
  }

  @Test
  void testWithConfigurationVariablesAndPropertiesSameKey() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues("mybatis.configuration.variables.key:value1", "mybatis.configuration-properties.key:value2")
        .run(context -> {
          Properties variables = context.getBean(SqlSessionFactory.class).getConfiguration().getVariables();
          assertThat(variables).hasSize(1);
          assertThat(variables.getProperty("key")).isEqualTo("value2");
        });
  }

  @Test
  void testCustomSqlSessionFactory() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            CustomSqlSessionFactoryConfiguration.class, CityMapperRepositoryConfiguration.class)
        .run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getVariables().getProperty("key"))
              .isEqualTo("value");
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(((RuntimeBeanReference) context.getBeanFactory().getBeanDefinition("cityMapper")
              .getPropertyValues().getPropertyValue("sqlSessionFactory").getValue()).getBeanName())
                  .isEqualTo("customSqlSessionFactory");
        });
  }

  @Test
  void testMySqlSessionFactory() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MySqlSessionFactoryConfiguration.class)
        .run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionFactory.class)).isInstanceOf(MySqlSessionFactory.class);
        });
  }

  @Test
  void testCustomSqlSessionTemplate() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class,
            CustomSqlSessionTemplateConfiguration.class, CityMapperRepositoryConfiguration.class)
        .run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.BATCH);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(((RuntimeBeanReference) context.getBeanFactory().getBeanDefinition("cityMapper")
              .getPropertyValues().getPropertyValue("sqlSessionTemplate").getValue()).getBeanName())
                  .isEqualTo("customSqlSessionTemplate");
        });
  }

  @Test
  void testMySqlSessionTemplate() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MySqlSessionTemplateConfiguration.class)
        .run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class)).isInstanceOf(MySqlSessionTemplate.class);
        });
  }

  @Test
  void testCustomSqlSessionTemplateAndSqlSessionFactory() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        MybatisBootMapperScanAutoConfiguration.class, CustomSqlSessionFactoryConfiguration.class,
        CustomSqlSessionTemplateConfiguration.class, CityMapperRepositoryConfiguration.class).run(context -> {
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.BATCH);
          assertThat(context.getBeanNamesForType(CityMapper.class)).hasSize(1);
          assertThat(((RuntimeBeanReference) context.getBeanFactory().getBeanDefinition("cityMapper")
              .getPropertyValues().getPropertyValue("sqlSessionTemplate").getValue()).getBeanName())
                  .isEqualTo("customSqlSessionTemplate");
        });
  }

  @Test
  void testTypeAliasesSuperTypeIsSpecify() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class)
        .withPropertyValues("mybatis.type-aliases-package:org.mybatis.spring.boot.autoconfigure.domain",
            "mybatis.type-aliases-super-type:org.mybatis.spring.boot.autoconfigure.domain.Domain")
        .run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("city");
          assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).doesNotContainKey("name");
        });
  }

  @Test
  void testMapperFactoryBean() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        MapperFactoryBeanConfiguration.class, PropertyPlaceholderAutoConfiguration.class).run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(DateTimeMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
        });
  }

  @Test
  void testMapperScannerConfigurer() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class,
        MapperScannerConfigurerConfiguration.class, PropertyPlaceholderAutoConfiguration.class).run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(DateTimeMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
        });
  }

  @Test
  void testDefaultScriptingLanguageIsSpecify() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisScanMapperConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues(
            "mybatis.default-scripting-language-driver:org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver")
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          LanguageDriverRegistry languageDriverRegistry = sqlSessionFactory.getConfiguration().getLanguageRegistry();
          assertThat(languageDriverRegistry.getDefaultDriverClass()).isEqualTo(ThymeleafLanguageDriver.class);
          assertThat(languageDriverRegistry.getDefaultDriver()).isInstanceOf(ThymeleafLanguageDriver.class);
          assertThat(languageDriverRegistry.getDriver(ThymeleafLanguageDriver.class)).isNotNull();
        });
  }

  @Test
  void testExcludeMybatisLanguageDriverAutoConfiguration() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisScanMapperConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues(
            "spring.autoconfigure.exclude:org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration")
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          assertThat(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(SqlSessionTemplate.class)).hasSize(1);
          assertThat(context.getBeanNamesForType(DateTimeMapper.class)).hasSize(1);
          assertThat(context.getBean(SqlSessionTemplate.class).getExecutorType()).isEqualTo(ExecutorType.SIMPLE);
          assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMapUnderscoreToCamelCase())
              .isFalse();
          assertThat(context.getBeanNamesForType(LanguageDriver.class)).hasSize(0);
        });
  }

  @Test
  void testMybatisLanguageDriverAutoConfigurationWithSingleCandidate() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisScanMapperConfiguration.class,
            SingleLanguageDriverConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues(
            "spring.autoconfigure.exclude:org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration")
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          LanguageDriverRegistry languageDriverRegistry = sqlSessionFactory.getConfiguration().getLanguageRegistry();
          assertThat(context.getBeanNamesForType(LanguageDriver.class)).hasSize(1);
          assertThat(languageDriverRegistry.getDefaultDriverClass()).isEqualTo(ThymeleafLanguageDriver.class);
          assertThat(languageDriverRegistry.getDefaultDriver()).isInstanceOf(ThymeleafLanguageDriver.class);
          assertThat(languageDriverRegistry.getDriver(ThymeleafLanguageDriver.class)).isNotNull();
        });
  }

  @Test
  void testMybatisLanguageDriverAutoConfigurationWithSingleCandidateWhenDefaultLanguageDriverIsSpecify() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisScanMapperConfiguration.class,
            SingleLanguageDriverConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
        .withPropertyValues(
            "mybatis.default-scripting-language-driver:org.apache.ibatis.scripting.xmltags.XMLLanguageDriver",
            "spring.autoconfigure.exclude:org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration")
        .run(context -> {
          SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);
          LanguageDriverRegistry languageDriverRegistry = sqlSessionFactory.getConfiguration().getLanguageRegistry();
          assertThat(context.getBeanNamesForType(LanguageDriver.class)).hasSize(1);
          assertThat(languageDriverRegistry.getDefaultDriverClass()).isEqualTo(XMLLanguageDriver.class);
          assertThat(languageDriverRegistry.getDefaultDriver()).isInstanceOf(XMLLanguageDriver.class);
          assertThat(languageDriverRegistry.getDriver(ThymeleafLanguageDriver.class)).isNotNull();
        });
  }

  @Test
  void whenFlywayIsAutoConfiguredThenMybatisSqlSessionTemplateDependsOnFlywayBeans() {
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FlywayAutoConfiguration.class, MybatisAutoConfiguration.class));
    contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class).run((context) -> {
      BeanDefinition beanDefinition = context.getBeanFactory().getBeanDefinition("sqlSessionTemplate");
      assertThat(beanDefinition.getDependsOn()).containsExactlyInAnyOrder("flywayInitializer", "flyway");
    });
  }

  @Test
  void whenCustomMigrationInitializerIsDefinedThenMybatisSqlSessionTemplateDependsOnIt() {
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FlywayAutoConfiguration.class, MybatisAutoConfiguration.class));
    contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class, CustomFlywayMigrationInitializer.class)
        .run((context) -> {
          BeanDefinition beanDefinition = context.getBeanFactory().getBeanDefinition("sqlSessionTemplate");
          assertThat(beanDefinition.getDependsOn()).containsExactlyInAnyOrder("flywayMigrationInitializer", "flyway");
        });
  }

  @Test
  void whenCustomFlywayIsDefinedThenMybatisSqlSessionTemplateDependsOnIt() {
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FlywayAutoConfiguration.class, MybatisAutoConfiguration.class));
    contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class, CustomFlyway.class).run((context) -> {
      BeanDefinition beanDefinition = context.getBeanFactory().getBeanDefinition("sqlSessionTemplate");
      assertThat(beanDefinition.getDependsOn()).containsExactlyInAnyOrder("customFlyway");
    });
  }

  @Test
  void whenLiquibaseIsAutoConfiguredThenMybatisSqlSessionTemplateDependsOnSpringLiquibaseBeans() {
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LiquibaseAutoConfiguration.class, MybatisAutoConfiguration.class));
    contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class).run((context) -> {
      BeanDefinition beanDefinition = context.getBeanFactory().getBeanDefinition("sqlSessionTemplate");
      assertThat(beanDefinition.getDependsOn()).containsExactly("liquibase");
    });
  }

  @Test
  void whenCustomSpringLiquibaseIsDefinedThenMybatisSqlSessionTemplateDependsOnSpringLiquibaseBeans() {
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LiquibaseAutoConfiguration.class, MybatisAutoConfiguration.class));
    contextRunner.withUserConfiguration(LiquibaseUserConfiguration.class, EmbeddedDataSourceConfiguration.class)
        .run((context) -> {
          BeanDefinition beanDefinition = context.getBeanFactory().getBeanDefinition("sqlSessionTemplate");
          assertThat(beanDefinition.getDependsOn()).containsExactly("springLiquibase");
        });
  }

  @Test
  void testTypeAliasesWithMultiByteCharacterInPackageName() {
    this.contextRunner
        .withUserConfiguration(EmbeddedDataSourceConfiguration.class, MybatisBootMapperScanAutoConfiguration.class)
        .withPropertyValues("mybatis.config-location:mybatis-config2.xml").run(context -> {
          org.apache.ibatis.session.Configuration configuration = context.getBean(SqlSessionFactory.class)
              .getConfiguration();
          assertThat(configuration.getTypeAliasRegistry().getTypeAliases()).containsKey("シティー");
        });
  }

  @Configuration
  static class MultipleDataSourceConfiguration {
    @Bean
    DataSource dataSourcePrimary() {
      return Mockito.mock(DataSource.class);
    }

    @Bean
    DataSource dataSourceReplica() {
      return Mockito.mock(DataSource.class);
    }
  }

  @Configuration
  static class SingleCandidateDataSourceConfiguration {
    @Bean
    @Primary
    DataSource dataSourcePrimary() {
      return Mockito.mock(DataSource.class);
    }

    @Bean
    DataSource dataSourceReplica() {
      return Mockito.mock(DataSource.class);
    }
  }

  @Configuration
  @MapperScan(basePackages = "com.example.mapper", lazyInitialization = "${mybatis.lazy-initialization:false}")
  static class MybatisScanMapperConfiguration {
  }

  @Configuration
  static class MapperFactoryBeanConfiguration {
    @Bean
    MapperFactoryBean<DateTimeMapper> dateTimeMapper(SqlSessionFactory sqlSessionFactory) {
      MapperFactoryBean<DateTimeMapper> factoryBean = new MapperFactoryBean<>(DateTimeMapper.class);
      factoryBean.setSqlSessionFactory(sqlSessionFactory);
      return factoryBean;
    }
  }

  @Configuration
  static class MapperScannerConfigurerConfiguration {
    @Bean
    static MapperScannerConfigurer mapperScannerConfigurer() {
      MapperScannerConfigurer configurer = new MapperScannerConfigurer();
      configurer.setBasePackage("com.example.mapper");
      return configurer;
    }
  }

  @Configuration
  static class MybatisBootMapperScanAutoConfiguration implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      beanFactory.registerScope("thread", new SimpleThreadScope());
    }
  }

  @Configuration
  static class MybatisMapperConfiguration {

    @Bean
    public CityMapperImpl cityMapper() {
      return new CityMapperImpl();
    }

  }

  @Configuration
  static class MybatisInterceptorConfiguration {

    @Bean
    @Order(2)
    public MyInterceptor myInterceptor() {
      return new MyInterceptor();
    }

    @Bean
    @Order(1)
    public MyInterceptor2 myInterceptor2() {
      return new MyInterceptor2();
    }

  }

  @Configuration
  static class MybatisInterceptorConfiguration2 {

    @Bean
    @Order(1)
    public MyInterceptor myInterceptor() {
      return new MyInterceptor();
    }

    @Bean
    @Order(2)
    public MyInterceptor2 myInterceptor2() {
      return new MyInterceptor2();
    }

  }

  @Configuration
  static class MybatisTypeHandlerConfiguration {

    @Bean
    public MyTypeHandler myTypeHandler() {
      return new MyTypeHandler();
    }

  }

  @Configuration
  static class MyBatisConfigurationCustomizerConfiguration {
    @Bean
    ConfigurationCustomizer typeHandlerConfigurationCustomizer() {
      return configuration -> configuration.getTypeHandlerRegistry().register(new DummyTypeHandler());
    }

    @Bean
    ConfigurationCustomizer cacheConfigurationCustomizer() {
      return configuration -> configuration.addCache(new PerpetualCache("test"));
    }
  }

  @Configuration
  static class SqlSessionFactoryBeanCustomizerConfiguration {
    @Bean
    SqlSessionFactoryBeanCustomizer typeHandlerSqlSessionFactoryBeanCustomizer() {
      return factoryBean -> factoryBean.setTypeHandlers(new DummyTypeHandler());
    }

    @Bean
    SqlSessionFactoryBeanCustomizer cacheSqlSessionFactoryBeanCustomizer() {
      return factoryBean -> factoryBean.setCache(new PerpetualCache("test"));
    }
  }

  @Configuration
  static class SingleLanguageDriverConfiguration {
    @Bean
    ThymeleafLanguageDriver myThymeleafLanguageDriver() {
      return new ThymeleafLanguageDriver();
    }
  }

  @Intercepts(@Signature(type = Map.class, method = "get", args = { Object.class }))
  static class MyInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) {
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

  @Intercepts(@Signature(type = Map.class, method = "get", args = { Object.class }))
  static class MyInterceptor2 implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) {
      return "Test2";
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
    public VendorDatabaseIdProvider vendorDatabaseIdProvider(
        @Qualifier("vendorProperties") Properties vendorProperties) {
      VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
      databaseIdProvider.setProperties(vendorProperties);
      return databaseIdProvider;
    }

  }

  @Configuration
  static class CustomSqlSessionFactoryConfiguration {
    @Bean
    public SqlSessionFactory customSqlSessionFactory(DataSource dataSource) throws Exception {
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
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
      MySqlSessionFactory sqlSessionFactory = new MySqlSessionFactory(new org.apache.ibatis.session.Configuration());
      sqlSessionFactory.getConfiguration()
          .setEnvironment(new Environment("", new SpringManagedTransactionFactory(), dataSource));
      return sqlSessionFactory;
    }
  }

  @Configuration
  static class CustomSqlSessionTemplateConfiguration {
    @Bean
    public SqlSessionTemplate customSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
      return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }
  }

  @Configuration
  static class MySqlSessionTemplateConfiguration {
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
      return new MySqlSessionTemplate(sqlSessionFactory);
    }
  }

  static class MySqlSessionFactory extends DefaultSqlSessionFactory {
    MySqlSessionFactory(org.apache.ibatis.session.Configuration configuration) {
      super(configuration);
    }
  }

  static class MySqlSessionTemplate extends SqlSessionTemplate {
    MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
      super(sqlSessionFactory);
    }
  }

  static class MyTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) {

    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) {
      return null;
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) {
      return null;
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) {
      return null;
    }

  }

  @Configuration
  @TestAutoConfigurationPackage(CityMapper.class)
  static class CityMapperRepositoryConfiguration {

  }

  @Configuration(proxyBeanMethods = false)
  static class CustomFlywayMigrationInitializer {

    @Bean
    FlywayMigrationInitializer flywayMigrationInitializer(Flyway flyway) {
      FlywayMigrationInitializer initializer = new FlywayMigrationInitializer(flyway);
      initializer.setOrder(Ordered.HIGHEST_PRECEDENCE);
      return initializer;
    }

  }

  @Configuration(proxyBeanMethods = false)
  static class CustomFlyway {

    @Bean
    Flyway customFlyway() {
      return Flyway.configure().load();
    }

  }

  @Configuration(proxyBeanMethods = false)
  static class LiquibaseUserConfiguration {

    @Bean
    SpringLiquibase springLiquibase(DataSource dataSource) {
      SpringLiquibase liquibase = new SpringLiquibase();
      liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.yaml");
      liquibase.setShouldRun(true);
      liquibase.setDataSource(dataSource);
      return liquibase;
    }

  }

}

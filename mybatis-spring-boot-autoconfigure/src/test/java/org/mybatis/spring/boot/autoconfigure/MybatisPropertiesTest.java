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
import static org.junit.jupiter.api.Assertions.assertAll;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * @author Eddú Meléndez
 */
class MybatisPropertiesTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(MybatisAutoConfiguration.class));

  @Test
  void emptyMapperLocations() {
    MybatisProperties properties = new MybatisProperties();
    assertThat(properties.resolveMapperLocations()).isEmpty();
  }

  @Test
  void twoLocations() {
    MybatisProperties properties = new MybatisProperties();
    properties
        .setMapperLocations(new String[] { "classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml",
            "classpath:org/mybatis/spring/boot/autoconfigure/repository/*Mapper.xml" });
    assertThat(properties.resolveMapperLocations()).hasSize(2);
  }

  @Test
  void twoLocationsWithOneIncorrectLocation() {
    MybatisProperties properties = new MybatisProperties();
    properties
        .setMapperLocations(new String[] { "classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml",
            "classpath:org/mybatis/spring/boot/autoconfigure/repositoy/*Mapper.xml" });
    assertThat(properties.resolveMapperLocations()).hasSize(1);
  }

  @Test
  void testWithDefaultCoreConfiguration() {
    this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class).run(context -> {
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isSafeRowBoundsEnabled()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isSafeResultHandlerEnabled()).isTrue();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isSafeResultHandlerEnabled()).isTrue();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isAggressiveLazyLoading()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isMultipleResultSetsEnabled()).isTrue();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isUseGeneratedKeys()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isUseColumnLabel()).isTrue();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isCacheEnabled()).isTrue();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isCallSettersOnNulls()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isUseActualParamName()).isTrue();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isReturnInstanceForEmptyRow()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isShrinkWhitespacesInSql()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isNullableOnForEach()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isArgNameBasedConstructorAutoMapping())
          .isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isLazyLoadingEnabled()).isFalse();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultStatementTimeout()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultFetchSize()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLocalCacheScope())
          .isEqualTo(LocalCacheScope.SESSION);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getJdbcTypeForNull())
          .isEqualTo(JdbcType.OTHER);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultResultSetType()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultExecutorType())
          .isEqualTo(ExecutorType.SIMPLE);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getAutoMappingBehavior())
          .isEqualTo(AutoMappingBehavior.PARTIAL);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getAutoMappingUnknownColumnBehavior())
          .isEqualTo(AutoMappingUnknownColumnBehavior.NONE);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLogPrefix()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLazyLoadTriggerMethods())
          .isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLogImpl()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getVfsImpl())
          .isEqualTo(SpringBootVFS.class);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultSqlProviderType()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getTypeHandlerRegistry()
          .getTypeHandler(JdbcType.class).getClass()).isEqualTo(EnumTypeHandler.class);
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getConfigurationFactory()).isNull();
      assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getVariables()).hasToString("{}");
    });
  }

  @Test
  void testWithCustomizeCoreConfiguration() {
    assertAll(
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.safe-row-bounds-enabled:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isSafeRowBoundsEnabled()).isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.safe-result-handler-enabled:false")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isSafeResultHandlerEnabled()).isFalse()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.safe-result-handler-enabled:false")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isSafeResultHandlerEnabled()).isFalse()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.aggressive-lazy-loading:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isAggressiveLazyLoading()).isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.multiple-result-sets-enabled:false")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isMultipleResultSetsEnabled()).isFalse()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.use-generated-keys:true")
            .run(context -> assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isUseGeneratedKeys())
                .isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.use-column-label:false")
            .run(context -> assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isUseColumnLabel())
                .isFalse()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.cache-enabled:false")
            .run(context -> assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isCacheEnabled())
                .isFalse()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.call-setters-on-nulls:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isCallSettersOnNulls()).isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.use-actual-paramName:false")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isUseActualParamName()).isFalse()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.return-instance-for-empty-row:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isReturnInstanceForEmptyRow()).isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.shrink-whitespaces-in-sql:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isShrinkWhitespacesInSql()).isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.nullable-on-for-each:true").run(
                context -> assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().isNullableOnForEach())
                    .isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.arg-name-based-constructor-auto-mapping:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isArgNameBasedConstructorAutoMapping())
                    .isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class)
            .withPropertyValues("mybatis.configuration.lazy-loading-enabled:true")
            .run(context -> assertThat(
                context.getBean(SqlSessionFactory.class).getConfiguration().isLazyLoadingEnabled()).isTrue()),
        () -> this.contextRunner.withUserConfiguration(EmbeddedDataSourceConfiguration.class).withPropertyValues(
            "mybatis.configuration.default-statement-timeout:2000", "mybatis.configuration.default-fetch-size:1000",
            "mybatis.configuration.local-cache-scope:STATEMENT", "mybatis.configuration.jdbc-type-for-null:NULL",
            "mybatis.configuration.default-result-set-type:FORWARD_ONLY",
            "mybatis.configuration.default-executor-type:BATCH", "mybatis.configuration.auto-mapping-behavior:FULL",
            "mybatis.configuration.auto-mapping-unknown-column-behavior:WARNING",
            "mybatis.configuration.log-prefix:[SQL]",
            "mybatis.configuration.lazy-Load-trigger-methods:equals,clone,hashCode,toString,toDumpString",
            "mybatis.configuration.logImpl:org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl",
            "mybatis.configuration.vfsImpl:org.mybatis.spring.boot.autoconfigure.MybatisPropertiesTest$MyVFS",
            "mybatis.configuration.default-sql-provider-type:org.mybatis.spring.boot.autoconfigure.MybatisPropertiesTest$MySqlProvider",
            "mybatis.configuration.defaultEnumTypeHandler:org.apache.ibatis.type.EnumOrdinalTypeHandler",
            "mybatis.configuration.configuration-factory:org.mybatis.spring.boot.autoconfigure.MybatisPropertiesTest$MyConfigurationFactory",
            "mybatis.configuration.variables.key1:value1", "mybatis.configuration.variables.key2:value2")
            .run(context -> {
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultStatementTimeout())
                  .isEqualTo(2000);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultFetchSize())
                  .isEqualTo(1000);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLocalCacheScope())
                  .isEqualTo(LocalCacheScope.STATEMENT);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getJdbcTypeForNull())
                  .isEqualTo(JdbcType.NULL);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultResultSetType())
                  .isEqualTo(ResultSetType.FORWARD_ONLY);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultExecutorType())
                  .isEqualTo(ExecutorType.BATCH);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getAutoMappingBehavior())
                  .isEqualTo(AutoMappingBehavior.FULL);
              assertThat(
                  context.getBean(SqlSessionFactory.class).getConfiguration().getAutoMappingUnknownColumnBehavior())
                      .isEqualTo(AutoMappingUnknownColumnBehavior.WARNING);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLogPrefix()).isEqualTo("[SQL]");
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLazyLoadTriggerMethods())
                  .isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString", "toDumpString")));
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getLogImpl())
                  .isEqualTo(JakartaCommonsLoggingImpl.class);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getVfsImpl())
                  .isEqualTo(MyVFS.class);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getDefaultSqlProviderType())
                  .isEqualTo(MySqlProvider.class);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getTypeHandlerRegistry()
                  .getTypeHandler(JdbcType.class).getClass()).isEqualTo(EnumOrdinalTypeHandler.class);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getConfigurationFactory())
                  .isEqualTo(MyConfigurationFactory.class);
              assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getVariables())
                  .hasToString("{key1=value1, key2=value2}");
            }));
  }

  @Test
  void checkProperties() {
    Set<String> mybatisCoreConfigurationProperties = Arrays
        .stream(PropertyAccessorFactory.forBeanPropertyAccess(new Configuration()).getPropertyDescriptors())
        .map(PropertyDescriptor::getName).collect(Collectors.toSet());
    Set<String> mybatisSpringBootConfigurationProperties = Arrays.stream(PropertyAccessorFactory
        .forBeanPropertyAccess(new MybatisProperties.CoreConfiguration()).getPropertyDescriptors())
        .map(PropertyDescriptor::getName).collect(Collectors.toSet());
    mybatisCoreConfigurationProperties.removeAll(mybatisSpringBootConfigurationProperties);
    mybatisCoreConfigurationProperties.removeAll(Arrays.asList("reflectorFactory", "defaultScriptingLanguage",
        "sqlFragments", "typeHandlerRegistry", "mapperRegistry", "interceptors", "cacheNames", "incompleteResultMaps",
        "typeAliasRegistry", "incompleteMethods", "proxyFactory", "resultMaps", "defaultScriptingLanguageInstance",
        "parameterMaps", "keyGenerators", "parameterMapNames", "caches", "mappedStatementNames", "objectWrapperFactory",
        "objectFactory", "databaseId", "incompleteStatements", "resultMapNames", "defaultScriptingLanuageInstance",
        "keyGeneratorNames", "environment", "mappedStatements", "languageRegistry", "incompleteCacheRefs"));
    assertThat(mybatisCoreConfigurationProperties).isEmpty();
  }

  static class MyVFS extends SpringBootVFS {
  }

  static class MySqlProvider {
  }

  static class MyConfigurationFactory {
  }

}

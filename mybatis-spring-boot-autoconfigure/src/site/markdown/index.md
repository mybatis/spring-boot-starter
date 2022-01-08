# Introduction

## What is MyBatis-Spring-Boot-Starter?

The MyBatis-Spring-Boot-Starter help you build quickly MyBatis applications on top of the [Spring Boot](https://spring.io/projects/spring-boot).

By using this module you will achieve:

* Build standalone applications
* Reduce the boilerplate to almost zero
* Less XML configuration

## Requirements

The MyBatis-Spring-Boot-Starter requires following versions:

| MyBatis-Spring-Boot-Starter | MyBatis-Spring | Spring Boot | Java |
| --- | --- | --- | --- |
| **2.2** | 2.0 (need 2.0.6+ for enable all features) | 2.5 or higher | 8 or higher |
| **2.1** | 2.0 (need 2.0.6+ for enable all features) | 2.1 - 2.4 | 8 or higher |
| **~~2.0 (EOL)~~** | ~~2.0~~ | ~~2.0 or 2.1~~ | ~~8 or higher~~ |
| **~~1.3 (EOL)~~** | ~~1.3~~ | ~~1.5~~ | ~~6 or higher~~ |
| **~~1.2 (EOL)~~** | ~~1.3~~ | ~~1.4~~ | ~~6 or higher~~ |
| **~~1.1 (EOL)~~** | ~~1.3~~ | ~~1.3~~ | ~~6 or higher~~ |
| **~~1.0 (EOL)~~** | ~~1.2~~ | ~~1.3~~ | ~~6 or higher~~ |

## Installation

To use the MyBatis-Spring-Boot-Starter module, you just need to include the `mybatis-spring-boot-autoconfigure.jar` file and its dependencies(`mybatis.jar`, `mybatis-spring.jar` and etc ...) in the classpath.

### Maven

If you are using Maven just add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Gradle

If using gradle add this to your `build.gradle`:

```groovy
dependencies {
  compile("org.mybatis.spring.boot:mybatis-spring-boot-starter:${project.version}")
}
```

## Quick Setup

As you may already know, to use MyBatis with Spring you need at least an `SqlSessionFactory` and at least one mapper interface.

MyBatis-Spring-Boot-Starter will:

* Autodetect an existing `DataSource`
* Will create and register an instance of a `SqlSessionFactory` passing that `DataSource` as an input using the `SqlSessionFactoryBean`
* Will create and register an instance of a `SqlSessionTemplate` got out of the `SqlSessionFactory`
* Auto-scan your mappers, link them to the `SqlSessionTemplate` and register them to Spring context so they can be injected into your beans

Suppose we have the following mapper:

```java
@Mapper
public interface CityMapper {

  @Select("SELECT * FROM CITY WHERE state = #{state}")
  City findByState(@Param("state") String state);

}
```

You just need to create a normal Spring boot application and let the mapper be injected like follows(available on Spring 4.3+):

```java
@SpringBootApplication
public class SampleMybatisApplication implements CommandLineRunner {

  private final CityMapper cityMapper;

  public SampleMybatisApplication(CityMapper cityMapper) {
    this.cityMapper = cityMapper;
  }

  public static void main(String[] args) {
    SpringApplication.run(SampleMybatisApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    System.out.println(this.cityMapper.findByState("CA"));
  }

}
```

This is all you have to do. You application can now be run as a normal Spring Boot application.

## Advanced scanning

The MyBatis-Spring-Boot-Starter will search, by default, for mappers marked with the `@Mapper` annotation.

You may want to specify a custom annotation or a marker interface for scanning. If so, you must use the `@MapperScan` annotation. See more about it in the [MyBatis-Spring reference page](http://www.mybatis.org/spring/mappers.html#scan).

The MyBatis-Spring-Boot-Starter will not start the scanning process if it finds at least one `MapperFactoryBean` in the Spring's context so if you want to stop the scanning at all you should register your mappers explicitly with `@Bean` methods.


## Using an SqlSession

An instance of a `SqlSessionTemplate` is created and added to the Spring context, so you can use the MyBatis API letting it be injected into your beans like follows(available on Spring 4.3+):

```java
@Component
public class CityDao {

  private final SqlSession sqlSession;

  public CityDao(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public City selectCityById(long id) {
    return this.sqlSession.selectOne("selectCityById", id);
  }

}
```

## Configuration

As any other Spring Boot application a MyBatis-Spring-Boot-Application configuration parameters are stored inside the `application.properties`(or `application.yml`).

MyBatis uses the prefix `mybatis` for its properties.

Available properties are:

| Property | Description |
| :--- | :--- |
| `config-location` | Location of MyBatis xml config file. |
| `check-config-location` | Indicates whether perform presence check of the MyBatis xml config file. |
| `mapper-locations` | Locations of Mapper xml config file. |
| `type-aliases-package` | Packages to search for type aliases. (Package delimiters are "`,; \t\n`") |
| `type-aliases-super-type` | The super class for filtering type alias. If this not specifies, the MyBatis deal as type alias all classes that searched from `type-aliases-package`. |
| `type-handlers-package` | Packages to search for type handlers. (Package delimiters are "`,; \t\n`") |
| `executor-type` | Executor type: `SIMPLE`, `REUSE`, `BATCH` |
| `default-scripting-language-driver` | The default scripting language driver class. This feature requires to use together with mybatis-spring 2.0.2+. |
| `configuration-properties` | Externalized properties for MyBatis configuration. Specified properties can be used as placeholder on MyBatis config file and Mapper file. For detail see the [MyBatis reference page](http://www.mybatis.org/mybatis-3/configuration.html#properties). |
| `lazy-initialization` | Whether enable lazy initialization of mapper bean. Set `true` to enable lazy initialization. This feature requires to use together with mybatis-spring 2.0.2+. |
| `mapper-default-scope` | Default scope for mapper bean that scanned by auto-configure. This feature requires to use together with mybatis-spring 2.0.6+. |
| `mybatis.inject-sql-session-on-mapper-scan` | Set whether inject a `SqlSessionTemplate` or `SqlSessionFactory` bean (If you want to back to the behavior of 2.2.1 or before, specify `false`). If you use together with spring-native, should be set `true`(default). |
| `configuration.*` | Property keys for `Configuration` bean provided by MyBatis Core. About available nested properties see the [MyBatis reference page](http://www.mybatis.org/mybatis-3/configuration.html#settings). <span class="label important">NOTE</span>: This property cannot be used at the same time with the `config-location`. |
| `scripting-language-driver.thymeleaf.*` | Property keys for `ThymeleafLanguageDriverConfig` bean provided by MyBatis Thymeleaf. About available nested properties see the [MyBatis Thymeleaf reference page](http://www.mybatis.org/thymeleaf-scripting/user-guide.html#_configuration_properties). |
| `scripting-language-driver.freemarker.*` | Properties keys for `FreeMarkerLanguageDriverConfig` bean provided by MyBatis FreeMarker. About available nested properties see the [MyBatis FreeMarker reference page](http://www.mybatis.org/freemarker-scripting/#Configuration). This feature requires to use together with mybatis-freemarker 1.2.0+. |
| `scripting-language-driver.velocity.*` | Properties keys for `VelocityLanguageDriverConfig` bean provided by MyBatis Velocity. About available nested properties see the [MyBatis Velocity reference page](http://www.mybatis.org/velocity-scripting/#Configuration). This feature requires to use together with mybatis-velocity 2.1.0+. |

For example:

```properties
# application.properties
mybatis.type-aliases-package=com.example.domain.model
mybatis.type-handlers-package=com.example.typehandler
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.default-fetch-size=100
mybatis.configuration.default-statement-timeout=30
...
```

```yaml
# application.yml
mybatis:
    type-aliases-package: com.example.domain.model
    type-handlers-package: com.example.typehandler
    configuration:
        map-underscore-to-camel-case: true
        default-fetch-size: 100
        default-statement-timeout: 30
...
```

## Using a ConfigurationCustomizer

The MyBatis-Spring-Boot-Starter provide opportunity to customize a MyBatis configuration generated by auto-configuration using Java Config.
The MyBatis-Spring-Boot-Starter will search beans that implement the `ConfigurationCustomizer` interface by automatically,
and call a method that customize a MyBatis configuration. (Available since 1.2.1 or above)

For example:

```java
@Configuration
public class MyBatisConfig {
  @Bean
  ConfigurationCustomizer mybatisConfigurationCustomizer() {
    return new ConfigurationCustomizer() {
      @Override
      public void customize(Configuration configuration) {
        // customize ...
      }
    };
  }
}
```

## Using a SqlSessionFactoryBeanCustomizer

The MyBatis-Spring-Boot-Starter provide opportunity to customize a `SqlSessionFactoryBean` generated by auto-configuration using Java Config.
The MyBatis-Spring-Boot-Starter will search beans that implement the `SqlSessionFactoryBeanCustomizer` interface by automatically,
and call a method that customize a `SqlSessionFactoryBean`. (Available since 2.2.2 or above)

For example:

```java
@Configuration
public class MyBatisConfig {
  @Bean
  SqlSessionFactoryBeanCustomizer sqlSessionFactoryBeanCustomizer() {
    return new SqlSessionFactoryBeanCustomizer() {
      @Override
      public void customize(SqlSessionFactoryBean factoryBean) {
        // customize ...
      }
    };
  }
}
```

## Using the SpringBootVFS

The MyBatis-Spring-Boot-Starter provides the `SpringBootVFS` as an implementation class of `VFS`.
The `VFS` is used for searching classes (e.g. target class of type alias, type handler class) from an application (or application server).
If you run a Spring Boot application using the executable jar, you need to use the `SpringBootVFS`.
The auto-configuration feature provided by the MyBatis-Spring-Boot-Starter used it automatically,
but it does not use automatically by a manual configuration (e.g. when uses multiple `DataSource`).

How to use the `SpringBootVFS` on manual configuration:

```java
@Configuration
public class MyBatisConfig {
  @Bean
  public SqlSessionFactory masterSqlSessionFactory() throws Exception {
    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDataSource(masterDataSource());
    factoryBean.setVfs(SpringBootVFS.class); // Sets the SpringBootVFS class into SqlSessionFactoryBean
    // ...
    return factoryBean.getObject();
  }
}
```

## Detecting MyBatis components

The MyBatis-Spring-Boot-Starter will detects beans that implements following interface provided by MyBatis.

* [`Interceptor`](http://www.mybatis.org/mybatis-3/configuration.html#plugins)
* [`TypeHandler`](http://www.mybatis.org/mybatis-3/configuration.html#typeHandlers)
* [`LanguageDriver`](http://www.mybatis.org/mybatis-3/dynamic-sql.html#Pluggable_Scripting_Languages_For_Dynamic_SQL) (Requires to use together with mybatis-spring 2.0.2+)
* [`DatabaseIdProvider`](http://www.mybatis.org/mybatis-3/configuration.html#databaseIdProvider)

```java
@Configuration
public class MyBatisConfig {
  @Bean
  MyInterceptor myInterceptor() {
    return MyInterceptor();
  }
  @Bean
  MyTypeHandler myTypeHandler() {
    return MyTypeHandler();
  }
  @Bean
  MyLanguageDriver myLanguageDriver() {
    return MyLanguageDriver();
  }
  @Bean
  VendorDatabaseIdProvider databaseIdProvider() {
    VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    properties.put("SQL Server", "sqlserver");
    properties.put("DB2", "db2");
    properties.put("H2", "h2");
    databaseIdProvider.setProperties(properties);
    return databaseIdProvider;
  }  
}
```

<span class="label important">NOTE</span>: If detected `LangaugeDriver`'s count is one, it set to default scripting language automatically.

## Customization for LanguageDriver

If you want to customize the `LanguageDriver` that creating by auto-configure,
please register user defined bean.

### ThymeleafLanguageDriver

```java
@Configuration
public class MyBatisConfig {
  @Bean
  ThymeleafLanguageDriverConfig thymeleafLanguageDriverConfig() {
    return ThymeleafLanguageDriverConfig.newInstance(c -> {
      // ... customization code
    });
  }
}
```

### FreeMarkerLanguageDriverConfig

```java
@Configuration
public class MyBatisConfig {
  @Bean
  FreeMarkerLanguageDriverConfig freeMarkerLanguageDriverConfig() {
    return FreeMarkerLanguageDriverConfig.newInstance(c -> {
      // ... customization code
    });
  }
}
```

### VelocityLanguageDriver

```java
@Configuration
public class MyBatisConfig {
  @Bean
  VelocityLanguageDriverConfig velocityLanguageDriverConfig() {
    return VelocityLanguageDriverConfig.newInstance(c -> {
      // ... customization code
    });
  }
}
```

### Running Samples

The project provides two samples so you play and experiment with them:

| Category | Sample | Description |
| :--- | :--- | :--- |
| Core | [1st Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-annotation) | Show the simplest scenario with just a mapper and a bean where the mapper is injected into. This is the sample we saw in the Quick Setup section. |
|      | [2nd Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-xml) | Shows how to use a Mapper that has its statements in an xml file and Dao that uses an `SqlSessionTemplate`. |
| LanguageDriver | [3rd Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-thymeleaf) | Shows how to use the language driver for Thymeleaf with mybatis-thymeleaf. |
|                | [4th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-freemarker) | Shows how to use the language driver for FreeMarker with mybatis-freemarker. |
|                | [5th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-velocity) | Shows how to use the language driver for Velocity with mybatis-velocity. |
| JVM Language | [6th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-kotlin) | Shows how to use with kotlin. |
|              | [7th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-groovy) | Shows how to use with groovy. |
| Web | [8th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-web) | Show how to use the web environment. |
|     | [9th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-war) | Show how to use the web environment with war(=deploy to the application server). |

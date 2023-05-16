# 简介

## 文档的翻译版本

可以阅读 MyBatis-Spring-Boot-Starter 文档的以下翻译版本：

<ul class="i18n">
  <li class="en"><a href="./../index.html">English</a></li>
  <li class="zh"><a href="./../zh/index.html">简体中文</a></li>
</ul>

## 什么是 MyBatis-Spring-Boot-Starter?

MyBatis-Spring-Boot-Starter 可以帮助你更快地在 [Spring Boot](https://spring.io/projects/spring-boot) 之上构建 MyBatis 应用。

你将通过使用这个模块实现以下目的：

* 构建单体应用程序
* 将几乎不需要样板配置
* 使用更少的 XML 配置

## 要求

MyBatis-Spring-Boot-Starter 要求以下版本:

| MyBatis-Spring-Boot-Starter | MyBatis-Spring       | Spring Boot   | Java      |
|-----------------------------|----------------------|---------------|-----------|
| **3.0**                     | 3.0                  | 3.0 - 3.1     | 17 或更高    |
| **2.3**                     | 2.1                  | 2.5 - 2.7     | 8 或更高     |
| **~~2.2 (EOL)~~**           | ~~2.0（2.0.6 以上可开启所有特性~~） | ~~2.5 - 2.7~~     | ~~8 或更高~~     |
| **~~2.1 (EOL)~~**           | ~~2.0（2.0.6 以上可开启所有特性）~~ | ~~2.1 - 2.4~~     | ~~8 或更高~~     |
| **~~2.0 (EOL)~~**           | ~~2.0~~              | ~~2.0 或 2.1~~ | ~~8 或更高~~ |
| **~~1.3 (EOL)~~**           | ~~1.3~~              | ~~1.5~~       | ~~6 或更高~~ |
| **~~1.2 (EOL)~~**           | ~~1.3~~              | ~~1.4~~       | ~~6 或更高~~ |
| **~~1.1 (EOL)~~**           | ~~1.3~~              | ~~1.3~~       | ~~6 或更高~~ |
| **~~1.0 (EOL)~~**           | ~~1.2~~              | ~~1.3~~       | ~~6 或更高~~ |

## 安装

要使用 MyBatis-Spring-Boot-Starter 模块，你只需要将 `mybatis-spring-boot-autoconfigure.jar` 文件以及它的依赖（ `mybatis.jar`, `mybatis-spring.jar` 等） 放在类路径下。

### Maven

如果你使用 Maven，只需要在你的 `pom.xml` 添加以下依赖：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Gradle

如果使用 gradle，请在你的 `build.gradle` 中加入以下内容：

```groovy
dependencies {
  implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:${project.version}")
}
```

## 快速开始

正如你已经知道的， 要与 Spring 一起使用 MyBatis，你至少需要一个 `SqlSessionFactory`         和一个 mapper 接口。

MyBatis-Spring-Boot-Starter 将会：

* 自动探测存在的 `DataSource`
* 将使用 `SqlSessionFactoryBean` 创建并注册一个 `SqlSessionFactory` 的实例，并将探测到的 `DataSource` 作为数据源
* 将创建并注册一个从 `SqlSessionFactory` 中得到的 `SqlSessionTemplate` 的实例
* 自动扫描你的 mapper，将它们与 `SqlSessionTemplate` 相关联，并将它们注册到Spring 的环境（context）中去，这样它们就可以被注入到你的 bean 中

假设我们有下面的 mapper ：

```java
@Mapper
public interface CityMapper {

  @Select("SELECT * FROM CITY WHERE state = #{state}")
  City findByState(@Param("state") String state);

}
```

你只需要创建一个 Spring boot 应用，像下面这样，将 mapper 注入进去（ Spring 4.3 以上可用）。

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

这就是你需要做的所有事情了。 你的 Spring boot 应用可以正常运行了。

## “扫描”的进阶用法

 MyBatis-Spring-Boot-Starter 将默认搜寻带有 `@Mapper` 注解的 mapper 接口。

你可能想指定一个自定义的注解或接口来扫描，如果那样的话，你就必须使用 `@MapperScan` 注解了。在 [MyBatis-Spring 参考页面](https://mybatis.org/spring/zh/mappers.html#scan) 中查看更多信息。

如果 MyBatis-Spring-Boot-Starter 发现至少有一个 `SqlSessionFactoryBean` ，它将不会开始扫描。 所以如果你想停止扫描，你应该用 `@Bean` 方法明确注册你的 mapper。

## 使用 SqlSession

一个 `SqlSessionTemplate` 的实例被创建并添加到 Spring 的环境中，因此你可以使用 MyBatis API，让它像下面一样被注入到你的 bean 中（Spring 4.3 以上可用）。

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

## 配置

像其他的 Spring Boot 应用一样，配置参数在 `application.properties` （或 `application.yml` )。

MyBatis 在它的配置项中，使用 `mybatis` 作为前缀。

可用的配置项如下：

| 配置项（properties）                          | 描述                                                                                                                                                                                                           |
|:---------------------------------------- |:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `config-location`                        | MyBatis XML 配置文件的路径。                                                                                                                                                                                         |
| `check-config-location`                  | 指定是否对 MyBatis XML 配置文件的存在进行检查。                                                                                                                                                                               |
| `mapper-locations`                       | XML 映射文件的路径。                                                                                                                                                                                                 |
| `type-aliases-package`                   | 搜索类型别名的包名。（包使用的分隔符是 "`,; \t\n`"）                                                                                                                                                                             |
| `type-aliases-super-type`                | 用于过滤类型别名的父类。如果没有指定，MyBatis会将所有从 `type-aliases-package` 搜索到的类作为类型别名处理。                                                                                                                                        |
| `type-handlers-package`                  | 搜索类型处理器的包名。（包使用的分隔符是 "`,; \t\n`"）                                                                                                                                                                            |
| `executor-type`                          | SQL 执行器类型： `SIMPLE`, `REUSE`, `BATCH`                                                                                                                                                                        |
| `default-scripting-language-driver`      | 默认的脚本语言驱动（模板引擎），此功能需要与 mybatis-spring 2.0.2 以上版本一起使用。                                                                                                                                                        |
| `configuration-properties`               | 可在外部配置的 MyBatis 配置项。指定的配置项可以被用作 MyBatis 配置文件和 Mapper 文件的占位符。更多细节 见 [MyBatis 参考页面](https://mybatis.org/mybatis-3/zh/configuration.html#properties)。                                                           |
| `lazy-initialization`                    | 是否启用 mapper bean 的延迟初始化。设置 `true` 以启用延迟初始化。此功能需要与 mybatis-spring 2.0.2 以上版本一起使用。                                                                                                                             |
| `mapper-default-scope`                   | 通过自动配置扫描的 mapper 组件的默认作用域。该功能需要与 mybatis-spring 2.0.6 以上版本一起使用。                                                                                                                                              |
| `inject-sql-session-on-mapper-scan`      | 设置是否注入 `SqlSessionTemplate` 或  `SqlSessionFactory` 组件 （如果你想回到 2.2.1 或之前的行为，请指定 `false` ）。如果你和 spring-native 一起使用，应该设置为 `true` （默认）。                                                                          |
| `configuration.*`                        | MyBatis Core 提供的`Configuration` 组件的配置项。有关可用的内部配置项，请参阅[MyBatis 参考页面](http://www.mybatis.org/mybatis-3/zh/configuration.html#settings)。注：此属性不能与 `config-location` 同时使用。                                        |
| `scripting-language-driver.thymeleaf.*`  | MyBatis `ThymeleafLanguageDriverConfig` 组件的 properties keys。有关可用的内部配置项，请参阅 [MyBatis Thymeleaf 参考页面](http://www.mybatis.org/thymeleaf-scripting/user-guide.html#_configuration_properties)。                   |
| `scripting-language-driver.freemarker.*` | MyBatis `FreemarkerLanguageDriverConfig` 组件的 properties keys。有关可用的内部配置项，请参阅 [MyBatis FreeMarker 参考页面](http://www.mybatis.org/freemarker-scripting/#Configuration)。这个特性需要与 mybatis-freemarker 1.2.0 以上版本一起使用。 |
| `scripting-language-driver.velocity.*`   | MyBatis `VelocityLanguageDriverConfig` 组件的  properties keys。有关可用的内部属性，请参阅 [MyBatis Velocity 参考页面](http://www.mybatis.org/velocity-scripting/#Configuration)。这个特性需要与 mybatis-velocity 2.1.0 以上版本一起使用。         |

例如：

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

## 使用 ConfigurationCustomizer

MyBatis-Spring-Boot-Starter 提供了使用 Java Config 来自定义 MyBatis 配置的可能。

MyBatis-Spring-Boot-Starter 将自动寻找实现了 `ConfigurationCustomizer` 接口的组件，调用自定义 MyBatis 配置的方法。( 1.2.1 及以上的版本可用）

例如：

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

## 使用 SqlSessionFactoryBeanCustomizer

MyBatis-Spring-Boot-Starter 提供了使用 Java Config 来自定义自动配置生成的 `SqlSessionFactoryBean` 。

MyBatis-Spring-Boot-Starter 将自动寻找实现了 `SqlSessionFactoryBeanCustomizer` 接口的组件，调用自定义 `SqlSessionFactoryBean` 的方法。( 2.2.2 及以上的版本可用）

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

## 使用 SpringBootVFS

MyBatis-Spring-Boot-Starter 提供了 `SpringBootVFS` 作为 `VFS` 的实现类。
 `VFS` 用于从应用或应用服务器中寻找类 （例如： 类型别名的目标类，类型处理器类） 。
如果你使用可执行的 jar 文件来运行 Spring boot 应用，你需要使用 `SpringBootVFS` 。
由于拥有自动配置的特性，MyBatis-Spring-Boot-Starter 会自动启用它。
但在你手动配置（MyBatis-Spring-Boot-Starter）的时候 (例如： 当你使用多个 `DataSource` 的时候）。

在手动配置（MyBatis-Spring-Boot-Starter）的时候，这样使用 `SpringBootVFS` ：

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

## 探测 MyBatis 组件

The MyBatis-Spring-Boot-Starter 将检测实现以下由 MyBatis 提供的接口的组件。

* [`Interceptor`](http://www.mybatis.org/mybatis-3/zh/configuration.html#plugins) (拦截器)
* [`TypeHandler`](http://www.mybatis.org/mybatis-3/zh/configuration.html#typeHandlers) (类型处理器)
* [`LanguageDriver`](http://www.mybatis.org/mybatis-3/zh/dynamic-sql.html#Pluggable_Scripting_Languages_For_Dynamic_SQL) （插入脚本语言）(需要 mybatis-spring 2.0.2 以上配合使用)
* [`DatabaseIdProvider`](http://www.mybatis.org/mybatis-3/zh/configuration.html#databaseIdProvider)

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

<span class="label important">注意</span>: 如果只有一个 `LangaugeDriver` ，它将自动地将其作为默认的脚本语言。

如果你想自定义 `LangaugeDriver` 的配置，请注册用户定义的组件。

### ThymeleafLanguageDriver

```java
@Configuration
public class MyBatisConfig {
  @Bean
  ThymeleafLanguageDriverConfig thymeleafLanguageDriverConfig() {
    return ThymeleafLanguageDriverConfig.newInstance(c -> {
      // ... 自定义代码
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
      // ... 自定义代码
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

### 可以运行的样例

项目（为每个分类）提供了至少两个样例，可以为你所用。

| 分类             | 样例                                                                                                                                  | 描述                                                                    |
|:-------------- |:----------------------------------------------------------------------------------------------------------------------------------- |:--------------------------------------------------------------------- |
| 核心组件           | [样例1](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-annotation) | 展示了最简单的场景，只有一个 mapper 和一个注入 mapper 的组件。这就是我们在“快速入门”部分看到的例子。           |
|                | [样例2](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-xml)        | 展示了如何在 XML 文件中使用一个带有语句的 Mapper，并且也有使用 `SqlSessionTemplate` 的 DAO 的示例。 |
| LangaugeDriver | [样例3](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-thymeleaf)  | 展示了如何在 mybatis-thymeleaf 的帮助下，使用 Thymeleaf。                           |
|                | [样例4](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-freemarker) | 展示了如何在 mybatis-freemarker 的帮助下，使用 Freemarker。                         |
|                | [样例5](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-velocity)   | 展示了如何在 mybatis-velocity 的帮助下，使用 Velocity。                             |
| JVM 语言         | [样例6](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-kotlin)     | 展示了如何和 kotlin 一同使用。                                                   |
|                | [样例7](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-groovy)     | 展示了如何和 groovy 一同使用。                                                   |
| Web            | [样例8](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-web)        | 展示了如何在 web 环境中使用。                                                     |
|                | [样例9](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-war)        | 展示了如何在 web 环境中使用并且让 war 文件部署在应用程序服务器上。                                |

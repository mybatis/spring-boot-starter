# 简介

## 文档的翻译版本

可以阅读 MyBatis-Spring-Boot-Starter-Test 文档的以下翻译版本：

<ul class="i18n">
  <li class="en"><a href="./../index.html">English</a></li>
  <li class="zh"><a href="./../zh/index.html">简体中文</a></li>
</ul>

## 什么是 MyBatis-Spring-Boot-Starter-Test?

MyBatis-Spring-Boot-Starter-Test 为 [MyBatis-Spring-Boot-Starter](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/) 中的 MyBatis 组件提供测试用例。

使用它你将可以做到：

* 可以使用 `@MybatisTest` 单独为 MyBatis 组件进行测试
* 在测试 MyBatis 组件时，可以导入依赖

TheMyBatis-Spring-Boot-Starter-Test 要求以下版本：

| MyBatis-Spring-Boot-Starter-Test | Spring Boot   | Java      |
|----------------------------------|---------------|-----------|
| **3.0**                          | 3.0 - 3.1     | 17 或更高    |
| **2.3**                          | 2.5 - 2.7     | 8 或更高     |
| **~~2.2 (EOL)~~**                | ~~2.5 - 2.7~~ | ~~8 或更高~~  |
| **~~2.1 (EOL)~~**                | ~~2.1 - 2.4~~ | ~~8 或更高~~  |
| **~~2.0 (EOL)~~**                | ~~2.0 或 2.1~~ | ~~8 或更高~~ |
| **~~1.3 (EOL)~~**                | ~~1.5~~       | ~~6 或更高~~ |
| **~~1.2 (EOL)~~**                | ~~1.4~~       | ~~6 或更高~~ |

## 安装

### Maven

如果你使用 Maven，只需要将下面的依赖放入你的 `pom.xml`：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

### Gradle

如果使用 Gradle，在 `build.gradle` 中加入以下内容：

```groovy
dependencies {
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:${project.version}")
}
```

## 使用 @MybatisTest

当你想对 MyBatis 组件进行测试时，可以使用 `@MybatisTest` （Mapper 接口与 `SqlSession`）。
默认情况下， 它将会配置 MyBatis（MyBatis-Spring）组件（`SqlSessionFactory` 与 `SqlSessionTemplate`），配置 MyBatis mapper 接口和内存中的内嵌的数据库。
MyBatis 测试默认情况下基于事务，且在测试的结尾进行回滚。
更多相关的信息可参见 [Spring 参考文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-tx-enabling-transactions) 。
再者，普遍情况下 `@Component` beans 将不会被载入 `ApplicationContext` 。

### 对 Mapper 接口进行测试

如果你想对下面的 Mapper 接口进行测试，你只需要将 `@MybatisTest` 添加在测试类上。

Mapper 接口：

```java
package sample.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sample.mybatis.domain.City;

@Mapper
public interface CityMapper {

    @Select("SELECT * FROM CITY WHERE state = #{state}")
    City findByState(@Param("state") String state);

}
```

测试类：

```java
package sample.mybatis.mapper;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import sample.mybatis.domain.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@MybatisTest
public class CityMapperTest {

    @Autowired
    private CityMapper cityMapper;

    @Test
    public void findByStateTest() {
        City city = cityMapper.findByState("CA");
        assertThat(city.getName()).isEqualTo("San Francisco");
        assertThat(city.getState()).isEqualTo("CA");
        assertThat(city.getCountry()).isEqualTo("US");
    }

}
```

### DAO 模式下的测试

如果你为下面的 DAO 类创建测试，你只需要将 `@MybatisTest` 和 `@Import` 添加在你的测试类上。

DAO 类：

```java
package sample.mybatis.dao;

import org.apache.ibatis.session.SqlSession;
import sample.mybatis.domain.City;

import org.springframework.stereotype.Component;

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

测试类：

```java
package sample.mybatis.dao;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import sample.mybatis.domain.City;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@MybatisTest
@Import(CityDao.class)
public class CityDaoTest {

    @Autowired
    private CityDao cityDao;

    @Test
    public void selectCityByIdTest() {
        City city = cityDao.selectCityById(1);
        assertThat(city.getName()).isEqualTo("San Francisco");
        assertThat(city.getState()).isEqualTo("CA");
        assertThat(city.getCountry()).isEqualTo("US");
    }

}
```

## 使用真实的数据库

内嵌的数据库通常在测试上表现良好，因为们很快，且不需要安装一些开发工具。
然而如果你希望使用真实的数据库，你可以像下面这样使用 `@AutoConfigureTestDatabase` ：

```java
package sample.mybatis.mapper;
// ...
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@RunWith(SpringRunner.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CityMapperTest {
    // ...
}
```

## 避免探测到真实的 @SpringBootApplication

 `@MybatisTest` 在默认情况下将会探测到带有 `@SpringBootApplication` 的类。
因此，由于 bean 定义的一些方法，可能会发生一些意想不到的错误，或者一些不必要的组件被装入 `ApplicationContext` 。
为了避免这种情况，我们可以在与测试类相同的包中创建带有 `@SpringBootApplication` 的类。

```java
package sample.mybatis.mapper;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class MapperTestApplication {

}
```

## 与其他 @***Test 一起使用

如果 `@MybatisTest` 与其他的 `@***Test` 一起使用（例如： `@WebMvcTest`），
请考虑使用 `@AutoConfigureMybatis` ，因为不能在同一测试中指定两个或多个`@***Test` 注解。

测试的目标类：

```java
@RestController
public class PingController {
  PingMapper mapper;

  PingController(PingMapper mapper) {
    this.mapper = mapper;
  }

  @GetMapping("ping")
  String ping() {
    return mapper.ping();
  }
}
```

```java
@Mapper
public interface PingMapper {
@Select("SELECT 'OK'")
  String ping();
}
```

测试类：

```java
@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMybatis // 替代 @MybatisTest
public class PingTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void ping() throws Exception {
    this.mvc.perform(get("/ping"))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
  }

}
```

## 在 JUnit 5 上使用 @MybatisTest

`@MybatisTest` 可以在 JUnit 5 上使用：

```java
@ExtendWith(SpringExtension.class)
@MybatisTest
public class CityMapperTest {
    // ...
}
```

自 2.0.1 起， `@ExtendWith(SpringExtension.class)` 可以像下面这样被忽略：

```java
@MybatisTest
public class CityMapperTest {
    // ...
}
```

## 附录

### 导入的自动配置

 `@MybatisTest` 将会导入以下自动配置的类：

* `org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration`
* `org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration`
* `org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration`
* `org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration`
* `org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration`
* `org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration`
* `org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration`
* `org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration`
* `org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration`
* `org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration`

### 可运行的样例

项目（为每个类型）提供了两个样例供使用：

| 分类     | 样例                                                                                                                                  | 描述                                                                    |
|:------ |:----------------------------------------------------------------------------------------------------------------------------------- |:--------------------------------------------------------------------- |
| 核心     | [样例1](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-annotation) | 展示了最简单的场景，只有一个 mapper 和一个注入 mapper 的组件。这就是我们在“快速入门”部分看到的例子。           |
|        | [样例2](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-xml)        | 展示了如何在 XML 文件中使用一个带有语句的 Mapper，并且也有使用 `SqlSessionTemplate` 的 DAO 的示例。 |
| JVM 语言 | [样例3](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-kotlin)     | 展示了如何和 kotlin 一同使用。                                                   |
|        | [样例4](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-groovy)     | 展示了如何和 groovy 一同使用。                                                   |

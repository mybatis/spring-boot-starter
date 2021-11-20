# Introduction

## What is MyBatis-Spring-Boot-Starter-Test?

The MyBatis-Spring-Boot-Starter-Test help creating a test cases for MyBatis component using the [MyBatis-Spring-Boot-Starter](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/).

By using this module you will can be:

* Can use the `@MybatisTest` that setup test components for testing pure MyBatis component
* Can import dependency artifacts for performing tests for pure MyBatis component

## Requirements

The MyBatis-Spring-Boot-Starter-Test requires following versions:

| MyBatis-Spring-Boot-Starter-Test | Spring Boot | Java |
| --- | --- | --- |
| **2.2** | 2.5 or higher | 8 or higher |
| **2.1** | 2.1 - 2.4 | 8 or higher |
| **~~2.0 (EOL)~~** | ~~2.0 or 2.1~~ | ~~8 or higher~~ |
| **~~1.3 (EOL)~~** | ~~1.5~~ | ~~6 or higher~~ |
| **~~1.2 (EOL)~~** | ~~1.4~~ | ~~6 or higher~~ |

## Installation

### Maven

If you are using Maven just add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

### Gradle

If using gradle add this to your `build.gradle`:

```groovy
dependencies {
    testCompile("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:${project.version}")
}
```

## Using @MybatisTest

The `@MybatisTest` can be used if you want to test MyBatis components(Mapper interface and `SqlSession`).
By default it will configure MyBatis(MyBatis-Spring) components(`SqlSessionFactory` and `SqlSessionTemplate`),
configure MyBatis mapper interfaces and configure an in-memory embedded database.
MyBatis tests are transactional and rollback at the end of each test by default,
for more details refer to the relevant section in the [Spring Reference Documentation](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-tx-enabling-transactions).
Also regular `@Component` beans will not be loaded into the `ApplicationContext`.

### Test for Mapper interface

if you create a test case for following mapper interface, you add just `@MybatisTest` on your test case class.

Mapper interface :

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

TestCase class :

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

### Test for DAO pattern

if you create a test case for following DAO class, you add just `@MybatisTest` and `@Import` on your test case class.

DAO class :

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

TestCase class :

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

## Using a real database

The In-memory embedded databases generally work well for tests since they are fast and don’t require any developer installation.
However if you prefer to run tests against a real database, you can use the `@AutoConfigureTestDatabase` as follow:

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

## Prevent detecting a real @SpringBootApplication

The `@MybatisTest` will detect a class that annotated `@SpringBootApplication` by default.
Therefore by depend on bean definition methods, there is case that an unexpected error is occurred or unnecessary components are loaded into `ApplicationContext`.
This behavior can prevent by creating a `@SpringBootApplication` class into same package as test class as follow:

```java
package sample.mybatis.mapper;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class MapperTestApplication {

}
```

## Use together with other @***Test

If you use the `@MybatisTest` together with other `@***Test`(e.g. `@WebMvcTest`),
Please consider to use the `@AutoConfigureMybatis` because `@***Test` annotation cannot specify two or more on same test.

Target classes for testing:

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

Test class:

```java
@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMybatis // Specify instead of @MybatisTest
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

## Using @MybatisTest on JUnit 5

The `@MybatisTest` can be used on JUnit 5.

```java
@ExtendWith(SpringExtension.class)
@MybatisTest
public class CityMapperTest {
    // ...
}
```

Since 2.0.1, the `@ExtendWith(SpringExtension.class)` can omit as follow:

```java
@MybatisTest
public class CityMapperTest {
    // ...
}
```

## Appendix

### Imported auto-configuration

The `@MybatisTest` will import following auto-configuration classes.

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


### Running Samples

The project provides two samples so you play and experiment with them.

| Category | Sample | Description |
| :--- | :--- | :--- |
| Core | [1st Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-annotation) | Show the simplest scenario with just a mapper and a bean where the mapper is injected into. This is the sample we saw in the Quick Setup section. |
|      | [2nd Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-xml) | Shows how to use a Mapper that has its statements in an xml file and Dao that uses an `SqlSessionTemplate`. |
| JVM Language | [3rd Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-kotlin) | Shows how to use with kotlin. |
|              | [4th Sample](https://github.com/mybatis/spring-boot-starter/tree/master/mybatis-spring-boot-samples/mybatis-spring-boot-sample-groovy) | Shows how to use with groovy. |

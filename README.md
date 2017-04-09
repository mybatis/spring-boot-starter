# MyBatis integration with Spring Boot

[![Build Status](https://travis-ci.org/mybatis/spring-boot-starter.svg)](https://travis-ci.org/mybatis/spring-boot-starter)
[![Coverage Status](https://coveralls.io/repos/github/mybatis/spring-boot-starter/badge.svg?branch=master)](https://coveralls.io/github/mybatis/spring-boot-starter?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56ef48ed35630e0029dafdb0/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56ef48ed35630e0029dafdb0)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis.spring.boot/mybatis-spring-boot/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis.spring.boot/mybatis-spring-boot)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-spring](http://mybatis.github.io/images/mybatis-logo.png)

MyBatis Spring-Boot-Starter will help you use MyBatis with Spring Boot

Essentials
----------

* [See the docs](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure)


Quick Start
----------

Let's create a MyBatis Spring Boot Application quickly using the [SPRING INITIALIZR](https://start.spring.io/).

### Create a project

Create a Spring Boot standalone application for MyBatis + H2 Database using following command (or the SPRING INITIALIZR UI).

```
$ curl -s https://start.spring.io/starter.tgz\
       -d name=mybatis-sample\
       -d artifactId=mybatis-sample\
       -d dependencies=mybatis,h2\
       -d baseDir=mybatis-sample\
       | tar -xzvf -
```

### Create sql files

Create a sql file(`src/main/resources/schema.sql`) to generate the city table.

```sql
CREATE TABLE city (
    id INT PRIMARY KEY auto_increment,
    name VARCHAR,
    state VARCHAR,
    country VARCHAR
);
```

Create a sql file(`src/main/resources/data.sql`) to import demo data into the city table.

```sql
INSERT INTO city (name, state, country) VALUES ('San Francisco', 'CA', 'US');
```

### Create a domain class

Create the `City` class.

```java
package com.example;

import java.io.Serializable;

public class City implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String state;
    private String country;

    // omit setter and getter methods (please generate those)

    @Override
    public String toString() {
        return getId() + "," + getName() + "," + getState() + "," + getCountry();
    }    
}
```

### Create a mapper interface

Create the `CityMapper` interface for annotation driven.

```java
package com.example;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CityMapper {

    @Select("SELECT id, name, state, country FROM city WHERE state = #{state}")
    City findByState(String state);

}
```

### Modify a spring boot application class

Modify to implements the `CommandLineRunner` interface at the `MybatisSampleApplication` class and call a mapper method.

```java
package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MybatisSampleApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MybatisSampleApplication.class, args);
    }
    
    private final CityMapper cityMapper;
    
    public MybatisSampleApplication(CityMapper cityMapper) {
        this.cityMapper = cityMapper;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println(this.cityMapper.findByState("CA"));
    }
}
```

### Run a spring boot application

Run a created application using the Spring Boot Maven Plugin.

```
$ ./mvnw spring-boot:run
...

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (vx.x.x.RELEASE)

...
1,San Francisco,CA,US
...
```

Also, you can package to a jar file and run using java command as follow:

```
$ ./mvnw package
$ java -jar target/mybatis-sample-0.0.1-SNAPSHOT.jar
```
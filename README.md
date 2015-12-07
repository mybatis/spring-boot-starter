# mybatis-spring-boot

[![Build Status](https://travis-ci.org/mybatis/mybatis-spring-boot.svg)](https://travis-ci.org/mybatis/mybatis-spring-boot)
[![Coverage Status](https://coveralls.io/repos/mybatis/mybatis-spring-boot/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/mybatis-spring-boot?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis.spring.boot/mybatis-spring-boot/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis.spring.boot/mybatis-spring-boot)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)

MyBatis integration with Spring Boot

## Properties file configuration

```
mybatis.config= # mybatis config file
mybatis.mapperLocations= # mappers file
mybatis.typeAliasesPackage= # domain object's package 
mybatis.typeHandlersPackage= # handler's package
mybatis.check-config-location= # check the mybatis configuration exists
mybatis.executorType= # mode of execution. Default is SIMPLE
```

## Issue Tracking

[GitHub Issues](https://github.com/mybatis/mybatis-spring-boot/issues)

## Maven dependency

```xml
<dependency>
	<groupId>org.mybatis.spring.boot</groupId>
	<artifactId>mybatis-spring-boot-starter</artifactId>
	<version>1.0.0</version>
</dependency>
```

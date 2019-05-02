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

import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriverConfig;
import org.mybatis.scripting.velocity.Driver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MybatisLanguageDriverAutoConfiguration}.
 *
 * @author Kazuki Shimizu
 */
class MybatisLanguageDriverAutoConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @BeforeEach
  void init() {
    this.context = new AnnotationConfigApplicationContext();
  }

  @AfterEach
  void closeContext() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  void testDefaultConfiguration() {
    this.context.register(MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    Map<String, LanguageDriver> languageDriverBeans = this.context.getBeansOfType(LanguageDriver.class);
    assertThat(languageDriverBeans).hasSize(3).containsKeys("freeMarkerLanguageDriver", "velocityLanguageDriver",
        "thymeleafLanguageDriver");
    assertThat(languageDriverBeans.get("freeMarkerLanguageDriver")).isInstanceOf(FreeMarkerLanguageDriver.class);
    assertThat(languageDriverBeans.get("velocityLanguageDriver")).isInstanceOf(Driver.class);
    assertThat(languageDriverBeans.get("thymeleafLanguageDriver")).isInstanceOf(ThymeleafLanguageDriver.class);
  }

  @Test
  void testCustomConfiguration() {
    this.context.register(MyLanguageDriverConfig.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    Map<String, LanguageDriver> languageDriverBeans = this.context.getBeansOfType(LanguageDriver.class);
    assertThat(languageDriverBeans).hasSize(3).containsKeys("myFreeMarkerLanguageDriver", "myVelocityLanguageDriver",
        "myThymeleafLanguageDriver");
  }

  @Test
  void testCustomThymeleafConfig() {
    this.context.register(ThymeleafCustomLanguageDriverConfig.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    ThymeleafLanguageDriver driver = this.context.getBean(ThymeleafLanguageDriver.class);
    SqlSource sqlSource = driver.createSqlSource(new Configuration(),
        "SELECT * FROM users WHERE id = /*[# m:p='id']*/ 1 /*[/]*/", Integer.class);
    BoundSql boundSql = sqlSource.getBoundSql(10);
    assertThat(boundSql.getSql()).isEqualTo("SELECT * FROM users WHERE id = ?");
    assertThat(boundSql.getParameterObject()).isEqualTo(10);
    assertThat(boundSql.getParameterMappings().get(0).getProperty()).isEqualTo("id");
    assertThat(boundSql.getParameterMappings().get(0).getJavaType()).isEqualTo(Integer.class);
  }

  @Test
  void testExcludeMybatisLanguageDriverAutoConfiguration() {
    TestPropertyValues
        .of("spring.autoconfigure.exclude:org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration")
        .applyTo(this.context);
    this.context.register(MyAutoConfiguration.class);
    this.context.refresh();
    assertThat(this.context.getBeanNamesForType(LanguageDriver.class)).hasSize(0);
  }

  @EnableAutoConfiguration
  private static class MyAutoConfiguration {
  }

  private static class MyLanguageDriverConfig {
    @Bean
    FreeMarkerLanguageDriver myFreeMarkerLanguageDriver() {
      return new FreeMarkerLanguageDriver();
    }

    @Bean
    Driver myVelocityLanguageDriver() {
      return new Driver();
    }

    @Bean
    ThymeleafLanguageDriver myThymeleafLanguageDriver() {
      return new ThymeleafLanguageDriver();
    }
  }

  private static class ThymeleafCustomLanguageDriverConfig {
    @Bean
    ThymeleafLanguageDriverConfig thymeleafLanguageDriverConfig() {
      return ThymeleafLanguageDriverConfig.newInstance(c -> c.getDialect().setPrefix("m"));
    }
  }

}

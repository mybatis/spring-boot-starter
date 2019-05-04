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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriverConfig;
import org.mybatis.scripting.thymeleaf.TemplateEngineCustomizer;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriverConfig;
import org.mybatis.scripting.velocity.Driver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;

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
    {
      ThymeleafLanguageDriverConfig config = this.context.getBean(ThymeleafLanguageDriverConfig.class);
      assertThat(config.isUse2way()).isEqualTo(true);
      assertThat(config.getDialect().getPrefix()).isEqualTo("mb");
      assertThat(config.getDialect().getLikeAdditionalEscapeTargetChars()).isNull();
      assertThat(config.getDialect().getLikeEscapeChar()).isEqualTo('\\');
      assertThat(config.getDialect().getLikeEscapeClauseFormat()).isEqualTo("ESCAPE '%s'");
      assertThat(config.getTemplateFile().getBaseDir()).isEqualTo("");
      assertThat(config.getTemplateFile().getCacheTtl()).isNull();
      assertThat(config.getTemplateFile().getEncoding()).isEqualTo(StandardCharsets.UTF_8);
      assertThat(config.getTemplateFile().getPatterns()).hasSize(1).contains("*.sql");
      assertThat(config.getCustomizer()).isNull();
    }
    {
      FreeMarkerLanguageDriverConfig config = this.context.getBean(FreeMarkerLanguageDriverConfig.class);
      assertThat(config.getBasePackage()).isEqualTo("");
      assertThat(config.getDefaultEncoding()).isEqualTo(StandardCharsets.UTF_8);
      assertThat(config.getIncompatibleImprovementsVersion())
          .isEqualTo(freemarker.template.Configuration.VERSION_2_3_22);
      assertThat(config.getFreemarkerSettings()).isEmpty();
    }
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
    ThymeleafLanguageDriverConfig config = this.context.getBean(ThymeleafLanguageDriverConfig.class);
    assertThat(config.isUse2way()).isEqualTo(true);
    assertThat(config.getDialect().getPrefix()).isEqualTo("m");
    assertThat(config.getDialect().getLikeAdditionalEscapeTargetChars()).isNull();
    assertThat(config.getDialect().getLikeEscapeChar()).isEqualTo('\\');
    assertThat(config.getDialect().getLikeEscapeClauseFormat()).isEqualTo("ESCAPE '%s'");
    assertThat(config.getTemplateFile().getBaseDir()).isEqualTo("");
    assertThat(config.getTemplateFile().getCacheTtl()).isNull();
    assertThat(config.getTemplateFile().getEncoding()).isEqualTo(StandardCharsets.UTF_8);
    assertThat(config.getTemplateFile().getPatterns()).hasSize(1).contains("*.sql");
    assertThat(config.getCustomizer()).isNull();
  }

  @Test
  void testCustomFreeMarkerConfig() {
    this.context.register(FreeMarkerCustomLanguageDriverConfig.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    FreeMarkerLanguageDriver driver = this.context.getBean(FreeMarkerLanguageDriver.class);
    @SuppressWarnings("unused")
    class Param {
      private Integer id;
      private Integer version;
    }
    Param params = new Param();
    params.id = 10;
    params.version = 20;
    SqlSource sqlSource = driver.createSqlSource(new Configuration(),
        "SELECT * FROM users WHERE id = #{id} and version = <@p name='version'/>", Param.class);
    BoundSql boundSql = sqlSource.getBoundSql(params);
    assertThat(boundSql.getSql()).isEqualTo("SELECT * FROM users WHERE id = ? and version = ?");
    assertThat(boundSql.getParameterMappings().get(0).getProperty()).isEqualTo("id");
    assertThat(boundSql.getParameterMappings().get(0).getJavaType()).isEqualTo(Integer.class);
    assertThat(boundSql.getParameterMappings().get(1).getProperty()).isEqualTo("version");
    assertThat(boundSql.getParameterMappings().get(1).getJavaType()).isEqualTo(Integer.class);
    FreeMarkerLanguageDriverConfig config = this.context.getBean(FreeMarkerLanguageDriverConfig.class);
    assertThat(config.getBasePackage()).isEqualTo("");
    assertThat(config.getDefaultEncoding()).isEqualTo(StandardCharsets.UTF_8);
    assertThat(config.getIncompatibleImprovementsVersion()).isEqualTo(freemarker.template.Configuration.VERSION_2_3_22);
    assertThat(config.getFreemarkerSettings()).hasSize(1);
    assertThat(config.getFreemarkerSettings().get("interpolation_syntax")).isEqualTo("dollar");
  }

  @Test
  void testCustomThymeleafConfigUsingConfigurationProperty() {
    TestPropertyValues.of("mybatis.scripting-language-driver.thymeleaf.use2way=false",
        "mybatis.scripting-language-driver.thymeleaf.dialect.like-additional-escape-target-chars=*,?",
        "mybatis.scripting-language-driver.thymeleaf.dialect.like-escape-char=~",
        "mybatis.scripting-language-driver.thymeleaf.dialect.like-escape-clause-format=escape '%s'",
        "mybatis.scripting-language-driver.thymeleaf.dialect.prefix=mybatis",
        "mybatis.scripting-language-driver.thymeleaf.template-file.base-dir=sqls",
        "mybatis.scripting-language-driver.thymeleaf.template-file.cache-enabled=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.cache-ttl=1234",
        "mybatis.scripting-language-driver.thymeleaf.template-file.encoding=Windows-31J",
        "mybatis.scripting-language-driver.thymeleaf.template-file.patterns=*.sql,*.sqlf",
        "mybatis.scripting-language-driver.thymeleaf.customizer=org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfigurationTest$MyTemplateEngineCustomizer")
        .applyTo(this.context);
    this.context.register(MyAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    ThymeleafLanguageDriver driver = this.context.getBean(ThymeleafLanguageDriver.class);
    SqlSource sqlSource = driver.createSqlSource(new Configuration(),
        "SELECT * FROM users WHERE id = [# mybatis:p='id' /]", Integer.class);
    BoundSql boundSql = sqlSource.getBoundSql(10);
    assertThat(boundSql.getSql()).isEqualTo("SELECT * FROM users WHERE id = ?");
    assertThat(boundSql.getParameterObject()).isEqualTo(10);
    assertThat(boundSql.getParameterMappings().get(0).getProperty()).isEqualTo("id");
    assertThat(boundSql.getParameterMappings().get(0).getJavaType()).isEqualTo(Integer.class);
    ThymeleafLanguageDriverConfig config = this.context.getBean(ThymeleafLanguageDriverConfig.class);
    assertThat(config.isUse2way()).isEqualTo(false);
    assertThat(config.getDialect().getPrefix()).isEqualTo("mybatis");
    assertThat(config.getDialect().getLikeAdditionalEscapeTargetChars()).hasSize(2).contains('*', '?');
    assertThat(config.getDialect().getLikeEscapeChar()).isEqualTo('~');
    assertThat(config.getDialect().getLikeEscapeClauseFormat()).isEqualTo("escape '%s'");
    assertThat(config.getTemplateFile().getBaseDir()).isEqualTo("sqls");
    assertThat(config.getTemplateFile().getCacheTtl()).isEqualTo(1234);
    assertThat(config.getTemplateFile().getEncoding()).isEqualTo(Charset.forName("Windows-31J"));
    assertThat(config.getTemplateFile().getPatterns()).hasSize(2).contains("*.sql", "*.sqlf");
    assertThat(config.getCustomizer()).isEqualTo(MyTemplateEngineCustomizer.class);
  }

  @Test
  void testCustomFreeMarkerConfigUsingConfigurationProperty() {
    TestPropertyValues
        .of("mybatis.scripting-language-driver.freemarker.base-package=sqls",
            "mybatis.scripting-language-driver.freemarker.default-encoding=" + StandardCharsets.ISO_8859_1.name(),
            "mybatis.scripting-language-driver.freemarker.incompatible-improvements-version=2.3.28",
            "mybatis.scripting-language-driver.freemarker.freemarker-settings.interpolation_syntax=dollar",
            "mybatis.scripting-language-driver.freemarker.freemarker-settings.whitespace_stripping=yes")
        .applyTo(this.context);
    this.context.register(MyAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    FreeMarkerLanguageDriver driver = this.context.getBean(FreeMarkerLanguageDriver.class);
    @SuppressWarnings("unused")
    class Param {
      private Integer id;
      private Integer version;
    }
    Param params = new Param();
    params.id = 10;
    params.version = 20;
    SqlSource sqlSource = driver.createSqlSource(new Configuration(),
        "SELECT * FROM users WHERE id = #{id} and version = <@p name='version'/>", Param.class);
    BoundSql boundSql = sqlSource.getBoundSql(params);
    assertThat(boundSql.getSql()).isEqualTo("SELECT * FROM users WHERE id = ? and version = ?");
    assertThat(boundSql.getParameterMappings().get(0).getProperty()).isEqualTo("id");
    assertThat(boundSql.getParameterMappings().get(0).getJavaType()).isEqualTo(Integer.class);
    assertThat(boundSql.getParameterMappings().get(1).getProperty()).isEqualTo("version");
    assertThat(boundSql.getParameterMappings().get(1).getJavaType()).isEqualTo(Integer.class);
    FreeMarkerLanguageDriverConfig config = this.context.getBean(FreeMarkerLanguageDriverConfig.class);
    assertThat(config.getBasePackage()).isEqualTo("sqls");
    assertThat(config.getDefaultEncoding()).isEqualTo(StandardCharsets.ISO_8859_1);
    assertThat(config.getIncompatibleImprovementsVersion()).isEqualTo(freemarker.template.Configuration.VERSION_2_3_28);
    assertThat(config.getFreemarkerSettings()).hasSize(2);
    assertThat(config.getFreemarkerSettings().get("interpolation_syntax")).isEqualTo("dollar");
    assertThat(config.getFreemarkerSettings().get("whitespace_stripping")).isEqualTo("yes");
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

  private static class FreeMarkerCustomLanguageDriverConfig {
    @Bean
    FreeMarkerLanguageDriverConfig freeMarkerLanguageDriverConfig() {
      return FreeMarkerLanguageDriverConfig
          .newInstance(c -> c.getFreemarkerSettings().put("interpolation_syntax", "dollar"));
    }
  }

  public static class MyTemplateEngineCustomizer implements TemplateEngineCustomizer {
    @Override
    public void customize(TemplateEngine defaultTemplateEngine) {
    }
  }

}

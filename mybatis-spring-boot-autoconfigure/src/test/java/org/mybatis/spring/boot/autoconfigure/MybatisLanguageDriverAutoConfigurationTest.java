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

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriverConfig;
import org.mybatis.scripting.thymeleaf.TemplateEngineCustomizer;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriverConfig;
import org.mybatis.scripting.velocity.VelocityFacade;
import org.mybatis.scripting.velocity.VelocityLanguageDriver;
import org.mybatis.scripting.velocity.VelocityLanguageDriverConfig;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;

/**
 * Tests for {@link MybatisLanguageDriverAutoConfiguration}.
 *
 * @author Kazuki Shimizu
 * @author Eddú Meléndez
 */
class MybatisLanguageDriverAutoConfigurationTest {

  private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(MybatisLanguageDriverAutoConfiguration.class));

  @BeforeEach
  @AfterEach
  void initializeVelocity() {
    VelocityFacade.destroy();
  }

  @Test
  void testDefaultConfiguration() {
    this.contextRunner.run(context -> {
      Map<String, LanguageDriver> languageDriverBeans = context.getBeansOfType(LanguageDriver.class);
      assertThat(languageDriverBeans).hasSize(3).containsKeys("freeMarkerLanguageDriver", "velocityLanguageDriver",
          "thymeleafLanguageDriver");
      assertThat(languageDriverBeans.get("freeMarkerLanguageDriver")).isInstanceOf(FreeMarkerLanguageDriver.class);
      assertThat(languageDriverBeans.get("velocityLanguageDriver")).isInstanceOf(VelocityLanguageDriver.class);
      assertThat(languageDriverBeans.get("thymeleafLanguageDriver")).isInstanceOf(ThymeleafLanguageDriver.class);

      ThymeleafLanguageDriverConfig thymeleafLanguageDriverConfig = context
          .getBean(ThymeleafLanguageDriverConfig.class);
      assertThat(thymeleafLanguageDriverConfig.isUse2way()).isTrue();
      assertThat(thymeleafLanguageDriverConfig.getDialect().getPrefix()).isEqualTo("mb");
      assertThat(thymeleafLanguageDriverConfig.getDialect().getLikeAdditionalEscapeTargetChars()).isNull();
      assertThat(thymeleafLanguageDriverConfig.getDialect().getLikeEscapeChar()).isEqualTo('\\');
      assertThat(thymeleafLanguageDriverConfig.getDialect().getLikeEscapeClauseFormat()).isEqualTo("ESCAPE '%s'");
      assertThat(thymeleafLanguageDriverConfig.getTemplateFile().getBaseDir()).isEmpty();
      assertThat(thymeleafLanguageDriverConfig.getTemplateFile().getCacheTtl()).isNull();
      assertThat(thymeleafLanguageDriverConfig.getTemplateFile().getEncoding()).isEqualTo(StandardCharsets.UTF_8);
      assertThat(thymeleafLanguageDriverConfig.getTemplateFile().getPatterns()).hasSize(1).contains("*.sql");
      assertThat(thymeleafLanguageDriverConfig.getCustomizer()).isNull();

      FreeMarkerLanguageDriverConfig freeMarkerLanguageDriverConfig = context
          .getBean(FreeMarkerLanguageDriverConfig.class);
      assertThat(freeMarkerLanguageDriverConfig.getBasePackage()).isEmpty();
      assertThat(freeMarkerLanguageDriverConfig.getFreemarkerSettings()).isEmpty();

      VelocityLanguageDriverConfig velocityLanguageDriverConfig = context.getBean(VelocityLanguageDriverConfig.class);
      @SuppressWarnings("deprecation")
      String[] userDirective = velocityLanguageDriverConfig.getUserdirective();
      assertThat(userDirective).isEmpty();
      assertThat(velocityLanguageDriverConfig.getAdditionalContextAttributes()).isEmpty();
      assertThat(velocityLanguageDriverConfig.getVelocitySettings()).hasSize(2);
      assertThat(velocityLanguageDriverConfig.getVelocitySettings()).containsEntry(RuntimeConstants.RESOURCE_LOADERS,
          "class");
      assertThat(velocityLanguageDriverConfig.getVelocitySettings()).containsEntry(
          RuntimeConstants.RESOURCE_LOADER + ".class.class",
          "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
      assertThat(velocityLanguageDriverConfig.generateCustomDirectivesString()).isEqualTo(
          "org.mybatis.scripting.velocity.TrimDirective,org.mybatis.scripting.velocity.WhereDirective,org.mybatis.scripting.velocity.SetDirective,org.mybatis.scripting.velocity.InDirective,org.mybatis.scripting.velocity.RepeatDirective");
    });
  }

  @Test
  void testCustomConfiguration() {
    this.contextRunner.withUserConfiguration(MyLanguageDriverConfig.class).run(context -> {
      Map<String, LanguageDriver> languageDriverBeans = context.getBeansOfType(LanguageDriver.class);
      assertThat(languageDriverBeans).hasSize(3).containsKeys("myFreeMarkerLanguageDriver", "myVelocityLanguageDriver",
          "myThymeleafLanguageDriver");
    });
  }

  @Test
  @SuppressWarnings("deprecation")
  void testLegacyConfiguration() {
    new ApplicationContextRunner()
        .withUserConfiguration(TestingLegacyFreeMarkerConfiguration.class, TestingLegacyVelocityConfiguration.class)
        .run(context -> {
          Map<String, LanguageDriver> languageDriverBeans = context.getBeansOfType(LanguageDriver.class);
          assertThat(languageDriverBeans).hasSize(2).containsKeys("freeMarkerLanguageDriver", "velocityLanguageDriver");
          assertThat(context.getBean(org.mybatis.scripting.velocity.Driver.class)).isNotNull();
          assertThat(context.getBean(FreeMarkerLanguageDriver.class)).isNotNull();
          assertThat(context.getBeanNamesForType(VelocityLanguageDriverConfig.class)).isEmpty();
          assertThat(context.getBeanNamesForType(FreeMarkerLanguageDriverConfig.class)).isEmpty();
        });
  }

  @Test
  void testCustomThymeleafConfig() {
    this.contextRunner.withUserConfiguration(ThymeleafCustomLanguageDriverConfig.class).run(context -> {
      ThymeleafLanguageDriver driver = context.getBean(ThymeleafLanguageDriver.class);
      SqlSource sqlSource = driver.createSqlSource(new Configuration(),
          "SELECT * FROM users WHERE id = /*[# m:p='id']*/ 1 /*[/]*/", Integer.class);
      BoundSql boundSql = sqlSource.getBoundSql(10);
      assertThat(boundSql.getSql()).isEqualTo("SELECT * FROM users WHERE id = ?");
      assertThat(boundSql.getParameterObject()).isEqualTo(10);
      assertThat(boundSql.getParameterMappings().get(0).getProperty()).isEqualTo("id");
      assertThat(boundSql.getParameterMappings().get(0).getJavaType()).isEqualTo(Integer.class);
      ThymeleafLanguageDriverConfig config = context.getBean(ThymeleafLanguageDriverConfig.class);
      assertThat(config.isUse2way()).isTrue();
      assertThat(config.getDialect().getPrefix()).isEqualTo("m");
      assertThat(config.getDialect().getLikeAdditionalEscapeTargetChars()).isNull();
      assertThat(config.getDialect().getLikeEscapeChar()).isEqualTo('\\');
      assertThat(config.getDialect().getLikeEscapeClauseFormat()).isEqualTo("ESCAPE '%s'");
      assertThat(config.getTemplateFile().getBaseDir()).isEmpty();
      assertThat(config.getTemplateFile().getCacheTtl()).isNull();
      assertThat(config.getTemplateFile().getEncoding()).isEqualTo(StandardCharsets.UTF_8);
      assertThat(config.getTemplateFile().getPatterns()).hasSize(1).contains("*.sql");
      assertThat(config.getCustomizer()).isNull();
    });
  }

  @Test
  void testCustomFreeMarkerConfig() {
    this.contextRunner.withUserConfiguration(FreeMarkerCustomLanguageDriverConfig.class).run(context -> {
      FreeMarkerLanguageDriver driver = context.getBean(FreeMarkerLanguageDriver.class);
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
      FreeMarkerLanguageDriverConfig config = context.getBean(FreeMarkerLanguageDriverConfig.class);
      assertThat(config.getBasePackage()).isEmpty();
      assertThat(config.getFreemarkerSettings()).hasSize(1);
      assertThat(config.getFreemarkerSettings()).containsEntry("interpolation_syntax", "dollar");
    });
  }

  @Test
  void testCustomVelocityConfig() {
    this.contextRunner.withUserConfiguration(VelocityCustomLanguageDriverConfig.class).run(context -> {
      VelocityLanguageDriver driver = context.getBean(VelocityLanguageDriver.class);
      @SuppressWarnings("unused")
      class Param {
        private Integer id;
        private Integer version;
      }
      Param params = new Param();
      params.id = 10;
      params.version = 20;
      SqlSource sqlSource = driver.createSqlSource(new Configuration(), "#now()", Param.class);
      BoundSql boundSql = sqlSource.getBoundSql(params);
      assertThat(boundSql.getSql()).isEqualTo("SELECT CURRENT_TIMESTAMP");
      VelocityLanguageDriverConfig config = context.getBean(VelocityLanguageDriverConfig.class);
      @SuppressWarnings("deprecation")
      String[] userDirective = config.getUserdirective();
      assertThat(userDirective).isEmpty();
      assertThat(config.getAdditionalContextAttributes()).isEmpty();
      assertThat(config.getVelocitySettings()).hasSize(3);
      assertThat(config.getVelocitySettings()).containsEntry(RuntimeConstants.RESOURCE_LOADERS, "class");
      assertThat(config.getVelocitySettings()).containsEntry(RuntimeConstants.RESOURCE_LOADER + ".class.class",
          "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
      assertThat(config.generateCustomDirectivesString()).isEqualTo(NowDirective.class.getName()
          + ",org.mybatis.scripting.velocity.TrimDirective,org.mybatis.scripting.velocity.WhereDirective,org.mybatis.scripting.velocity.SetDirective,org.mybatis.scripting.velocity.InDirective,org.mybatis.scripting.velocity.RepeatDirective");
    });
  }

  @Test
  void testCustomThymeleafConfigUsingConfigurationProperty() {
    this.contextRunner.withUserConfiguration(MyAutoConfiguration.class).withPropertyValues(
        "mybatis.scripting-language-driver.thymeleaf.use2way=false",
        "mybatis.scripting-language-driver.thymeleaf.dialect.like-additional-escape-target-chars=*,?",
        "mybatis.scripting-language-driver.thymeleaf.dialect.like-escape-char=~",
        "mybatis.scripting-language-driver.thymeleaf.dialect.like-escape-clause-format=escape '%s'",
        "mybatis.scripting-language-driver.thymeleaf.dialect.prefix=mybatis",
        "mybatis.scripting-language-driver.thymeleaf.template-file.base-dir=sqls",
        "mybatis.scripting-language-driver.thymeleaf.template-file.cache-enabled=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.cache-ttl=1234",
        "mybatis.scripting-language-driver.thymeleaf.template-file.encoding=Windows-31J",
        "mybatis.scripting-language-driver.thymeleaf.template-file.patterns=*.sql,*.sqlf",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.prefix=sql/",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.includes-package-path=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.separate-directory-per-mapper=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.includes-mapper-name-when-separate-directory=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.cache-enabled=false",
        "mybatis.scripting-language-driver.thymeleaf.customizer=org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfigurationTest$MyTemplateEngineCustomizer")
        .run(context -> {
          ThymeleafLanguageDriver driver = context.getBean(ThymeleafLanguageDriver.class);
          SqlSource sqlSource = driver.createSqlSource(new Configuration(),
              "SELECT * FROM users WHERE id = [# mybatis:p='id' /]", Integer.class);
          BoundSql boundSql = sqlSource.getBoundSql(10);
          assertThat(boundSql.getSql()).isEqualTo("SELECT * FROM users WHERE id = ?");
          assertThat(boundSql.getParameterObject()).isEqualTo(10);
          assertThat(boundSql.getParameterMappings().get(0).getProperty()).isEqualTo("id");
          assertThat(boundSql.getParameterMappings().get(0).getJavaType()).isEqualTo(Integer.class);
          ThymeleafLanguageDriverConfig config = context.getBean(ThymeleafLanguageDriverConfig.class);
          assertThat(config.isUse2way()).isFalse();
          assertThat(config.getDialect().getPrefix()).isEqualTo("mybatis");
          assertThat(config.getDialect().getLikeAdditionalEscapeTargetChars()).hasSize(2).contains('*', '?');
          assertThat(config.getDialect().getLikeEscapeChar()).isEqualTo('~');
          assertThat(config.getDialect().getLikeEscapeClauseFormat()).isEqualTo("escape '%s'");
          assertThat(config.getTemplateFile().getBaseDir()).isEqualTo("sqls");
          assertThat(config.getTemplateFile().getCacheTtl()).isEqualTo(1234);
          assertThat(config.getTemplateFile().getEncoding()).isEqualTo(Charset.forName("Windows-31J"));
          assertThat(config.getTemplateFile().getPatterns()).hasSize(2).contains("*.sql", "*.sqlf");
          assertThat(config.getTemplateFile().getPathProvider().getPrefix()).isEqualTo("sql/");
          assertThat(config.getTemplateFile().getPathProvider().isIncludesPackagePath()).isFalse();
          assertThat(config.getTemplateFile().getPathProvider().isSeparateDirectoryPerMapper()).isFalse();
          assertThat(config.getTemplateFile().getPathProvider().isIncludesMapperNameWhenSeparateDirectory()).isFalse();
          assertThat(config.getTemplateFile().getPathProvider().isCacheEnabled()).isFalse();
          assertThat(config.getCustomizer()).isEqualTo(MyTemplateEngineCustomizer.class);
        });
  }

  @Test
  void testCustomFreeMarkerConfigUsingConfigurationProperty() {
    this.contextRunner.withUserConfiguration(MyAutoConfiguration.class)
        .withPropertyValues("mybatis.scripting-language-driver.freemarker.base-package=sqls",
            "mybatis.scripting-language-driver.freemarker.freemarker-settings.interpolation_syntax=dollar",
            "mybatis.scripting-language-driver.freemarker.freemarker-settings.whitespace_stripping=yes")
        .run(context -> {
          FreeMarkerLanguageDriver driver = context.getBean(FreeMarkerLanguageDriver.class);
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
          FreeMarkerLanguageDriverConfig config = context.getBean(FreeMarkerLanguageDriverConfig.class);
          assertThat(config.getBasePackage()).isEqualTo("sqls");
          assertThat(config.getFreemarkerSettings()).hasSize(2);
          assertThat(config.getFreemarkerSettings()).containsEntry("interpolation_syntax", "dollar");
          assertThat(config.getFreemarkerSettings()).containsEntry("whitespace_stripping", "yes");
        });
  }

  @Test
  void testCustomVelocityConfigUsingConfigurationProperty() {
    this.contextRunner.withUserConfiguration(MyAutoConfiguration.class)
        .withPropertyValues("mybatis.scripting-language-driver.velocity.userdirective=" + NowDirective.class.getName(),
            "mybatis.scripting-language-driver.velocity.velocity-settings." + RuntimeConstants.INPUT_ENCODING + "="
                + RuntimeConstants.ENCODING_DEFAULT,
            "mybatis.scripting-language-driver.velocity.additional-context-attributes.attribute1=java.lang.String",
            "mybatis.scripting-language-driver.velocity.additional-context-attributes.attribute2=java.util.HashMap")
        .run(context -> {
          VelocityLanguageDriver driver = context.getBean(VelocityLanguageDriver.class);
          @SuppressWarnings("unused")
          class Param {
            private Integer id;
            private Integer version;
          }
          Param params = new Param();
          params.id = 10;
          params.version = 20;
          SqlSource sqlSource = driver.createSqlSource(new Configuration(), "#now()", Param.class);
          BoundSql boundSql = sqlSource.getBoundSql(params);
          assertThat(boundSql.getSql()).isEqualTo("SELECT CURRENT_TIMESTAMP");
          VelocityLanguageDriverConfig config = context.getBean(VelocityLanguageDriverConfig.class);
          @SuppressWarnings("deprecation")
          String[] userDirective = config.getUserdirective();
          assertThat(userDirective).hasSize(1).contains(NowDirective.class.getName());
          assertThat(config.getAdditionalContextAttributes()).hasSize(2);
          assertThat(config.getAdditionalContextAttributes()).containsEntry("attribute1", "java.lang.String");
          assertThat(config.getAdditionalContextAttributes()).containsEntry("attribute2", "java.util.HashMap");
          assertThat(config.getVelocitySettings()).hasSize(3);
          assertThat(config.getVelocitySettings()).containsEntry(RuntimeConstants.RESOURCE_LOADERS, "class");
          assertThat(config.getVelocitySettings()).containsEntry(RuntimeConstants.RESOURCE_LOADER + ".class.class",
              "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
          assertThat(config.generateCustomDirectivesString()).isEqualTo(NowDirective.class.getName()
              + ",org.mybatis.scripting.velocity.TrimDirective,org.mybatis.scripting.velocity.WhereDirective,org.mybatis.scripting.velocity.SetDirective,org.mybatis.scripting.velocity.InDirective,org.mybatis.scripting.velocity.RepeatDirective");
          assertThat(config.getVelocitySettings()).containsEntry(RuntimeConstants.INPUT_ENCODING,
              RuntimeConstants.ENCODING_DEFAULT);
        });
  }

  @Test
  void testExcludeMybatisLanguageDriverAutoConfiguration() {
    new ApplicationContextRunner().withUserConfiguration(MyAutoConfiguration.class)
        .run(context -> assertThat(context.getBeanNamesForType(LanguageDriver.class)).isEmpty());
  }

  @EnableConfigurationProperties
  static class MyAutoConfiguration {
  }

  @org.springframework.context.annotation.Configuration
  static class TestingLegacyFreeMarkerConfiguration
      extends MybatisLanguageDriverAutoConfiguration.LegacyFreeMarkerConfiguration {
  }

  @org.springframework.context.annotation.Configuration
  static class TestingLegacyVelocityConfiguration
      extends MybatisLanguageDriverAutoConfiguration.LegacyVelocityConfiguration {
  }

  @org.springframework.context.annotation.Configuration
  static class MyLanguageDriverConfig {
    @Bean
    FreeMarkerLanguageDriver myFreeMarkerLanguageDriver() {
      return new FreeMarkerLanguageDriver();
    }

    @Bean
    VelocityLanguageDriver myVelocityLanguageDriver() {
      return new VelocityLanguageDriver();
    }

    @Bean
    ThymeleafLanguageDriver myThymeleafLanguageDriver() {
      return new ThymeleafLanguageDriver();
    }
  }

  @org.springframework.context.annotation.Configuration
  static class ThymeleafCustomLanguageDriverConfig {
    @Bean
    ThymeleafLanguageDriverConfig thymeleafLanguageDriverConfig() {
      return ThymeleafLanguageDriverConfig.newInstance(c -> c.getDialect().setPrefix("m"));
    }
  }

  @org.springframework.context.annotation.Configuration
  static class FreeMarkerCustomLanguageDriverConfig {
    @Bean
    FreeMarkerLanguageDriverConfig freeMarkerLanguageDriverConfig() {
      return FreeMarkerLanguageDriverConfig
          .newInstance(c -> c.getFreemarkerSettings().put("interpolation_syntax", "dollar"));
    }
  }

  @org.springframework.context.annotation.Configuration
  static class VelocityCustomLanguageDriverConfig {
    @Bean
    VelocityLanguageDriverConfig velocityLanguageDriverConfig() {
      return VelocityLanguageDriverConfig.newInstance(
          c -> c.getVelocitySettings().put(RuntimeConstants.CUSTOM_DIRECTIVES, NowDirective.class.getName()));
    }
  }

  public static class MyTemplateEngineCustomizer implements TemplateEngineCustomizer {
    @Override
    public void customize(TemplateEngine defaultTemplateEngine) {
    }
  }

  public static class NowDirective extends Directive {

    @Override
    public String getName() {
      return "now";
    }

    @Override
    public int getType() {
      return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
      writer.append("SELECT CURRENT_TIMESTAMP");
      return true;
    }

  }
}

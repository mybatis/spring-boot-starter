/**
 *    Copyright 2015-2020 the original author or authors.
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;

/**
 * Tests for {@link MybatisLanguageDriverAutoConfiguration}.
 *
 * @author Kazuki Shimizu
 */
class MybatisLanguageDriverAutoConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @BeforeEach
  void init() {
    VelocityFacade.destroy();
    this.context = new AnnotationConfigApplicationContext();
  }

  @AfterEach
  void closeContext() {
    if (this.context != null) {
      this.context.close();
    }
    VelocityFacade.destroy();
  }

  @Test
  void testDefaultConfiguration() {
    this.context.register(MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    Map<String, LanguageDriver> languageDriverBeans = this.context.getBeansOfType(LanguageDriver.class);
    assertThat(languageDriverBeans).hasSize(3).containsKeys("freeMarkerLanguageDriver", "velocityLanguageDriver",
        "thymeleafLanguageDriver");
    assertThat(languageDriverBeans.get("freeMarkerLanguageDriver")).isInstanceOf(FreeMarkerLanguageDriver.class);
    assertThat(languageDriverBeans.get("velocityLanguageDriver")).isInstanceOf(VelocityLanguageDriver.class);
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
      assertThat(config.getFreemarkerSettings()).isEmpty();
    }
    {
      VelocityLanguageDriverConfig config = this.context.getBean(VelocityLanguageDriverConfig.class);
      @SuppressWarnings("deprecation")
      String[] userDirective = config.getUserdirective();
      assertThat(userDirective).hasSize(0);
      assertThat(config.getAdditionalContextAttributes()).hasSize(0);
      assertThat(config.getVelocitySettings()).hasSize(2);
      assertThat(config.getVelocitySettings().get(RuntimeConstants.RESOURCE_LOADERS)).isEqualTo("class");
      assertThat(config.getVelocitySettings().get(RuntimeConstants.RESOURCE_LOADER + ".class.class"))
          .isEqualTo("org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
      assertThat(config.generateCustomDirectivesString()).isEqualTo(
          "org.mybatis.scripting.velocity.TrimDirective,org.mybatis.scripting.velocity.WhereDirective,org.mybatis.scripting.velocity.SetDirective,org.mybatis.scripting.velocity.InDirective,org.mybatis.scripting.velocity.RepeatDirective");
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
  @SuppressWarnings("deprecation")
  void testLegacyConfiguration() {
    this.context.register(TestingLegacyFreeMarkerConfiguration.class, TestingLegacyVelocityConfiguration.class);
    this.context.refresh();
    Map<String, LanguageDriver> languageDriverBeans = this.context.getBeansOfType(LanguageDriver.class);
    assertThat(languageDriverBeans).hasSize(2).containsKeys("freeMarkerLanguageDriver", "velocityLanguageDriver");
    assertThat(this.context.getBean(org.mybatis.scripting.velocity.Driver.class)).isNotNull();
    assertThat(this.context.getBean(FreeMarkerLanguageDriver.class)).isNotNull();
    assertThat(this.context.getBeanNamesForType(VelocityLanguageDriverConfig.class)).hasSize(0);
    assertThat(this.context.getBeanNamesForType(FreeMarkerLanguageDriverConfig.class)).hasSize(0);
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
  void test() {
    this.context.register(FreeMarkerCustomLanguageDriverConfig.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
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
    assertThat(config.getFreemarkerSettings()).hasSize(1);
    assertThat(config.getFreemarkerSettings().get("interpolation_syntax")).isEqualTo("dollar");
  }

  @Test
  void testCustomVelocityConfig() {
    this.context.register(VelocityCustomLanguageDriverConfig.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    VelocityLanguageDriver driver = this.context.getBean(VelocityLanguageDriver.class);
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
    VelocityLanguageDriverConfig config = this.context.getBean(VelocityLanguageDriverConfig.class);
    @SuppressWarnings("deprecation")
    String[] userDirective = config.getUserdirective();
    assertThat(userDirective).hasSize(0);
    assertThat(config.getAdditionalContextAttributes()).hasSize(0);
    assertThat(config.getVelocitySettings()).hasSize(3);
    assertThat(config.getVelocitySettings().get(RuntimeConstants.RESOURCE_LOADERS)).isEqualTo("class");
    assertThat(config.getVelocitySettings().get(RuntimeConstants.RESOURCE_LOADER + ".class.class"))
        .isEqualTo("org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    assertThat(config.generateCustomDirectivesString()).isEqualTo(NowDirective.class.getName()
        + ",org.mybatis.scripting.velocity.TrimDirective,org.mybatis.scripting.velocity.WhereDirective,org.mybatis.scripting.velocity.SetDirective,org.mybatis.scripting.velocity.InDirective,org.mybatis.scripting.velocity.RepeatDirective");
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
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.prefix=sql/",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.includes-package-path=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.separate-directory-per-mapper=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.includes-mapper-name-when-separate-directory=false",
        "mybatis.scripting-language-driver.thymeleaf.template-file.path-provider.cache-enabled=false",
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
    assertThat(config.getTemplateFile().getPathProvider().getPrefix()).isEqualTo("sql/");
    assertThat(config.getTemplateFile().getPathProvider().isIncludesPackagePath()).isFalse();
    assertThat(config.getTemplateFile().getPathProvider().isSeparateDirectoryPerMapper()).isFalse();
    assertThat(config.getTemplateFile().getPathProvider().isIncludesMapperNameWhenSeparateDirectory()).isFalse();
    assertThat(config.getTemplateFile().getPathProvider().isCacheEnabled()).isFalse();
    assertThat(config.getCustomizer()).isEqualTo(MyTemplateEngineCustomizer.class);
  }

  @Test
  void testCustomFreeMarkerConfigUsingConfigurationProperty() {
    TestPropertyValues
        .of("mybatis.scripting-language-driver.freemarker.base-package=sqls",
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
    assertThat(config.getFreemarkerSettings()).hasSize(2);
    assertThat(config.getFreemarkerSettings().get("interpolation_syntax")).isEqualTo("dollar");
    assertThat(config.getFreemarkerSettings().get("whitespace_stripping")).isEqualTo("yes");
  }

  @Test
  void testCustomVelocityConfigUsingConfigurationProperty() {
    TestPropertyValues
        .of("mybatis.scripting-language-driver.velocity.userdirective=" + NowDirective.class.getName(),
            "mybatis.scripting-language-driver.velocity.velocity-settings." + RuntimeConstants.INPUT_ENCODING + "="
                + RuntimeConstants.ENCODING_DEFAULT,
            "mybatis.scripting-language-driver.velocity.additional-context-attributes.attribute1=java.lang.String",
            "mybatis.scripting-language-driver.velocity.additional-context-attributes.attribute2=java.util.HashMap")
        .applyTo(this.context);
    this.context.register(MyAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class);
    this.context.refresh();
    VelocityLanguageDriver driver = this.context.getBean(VelocityLanguageDriver.class);
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
    VelocityLanguageDriverConfig config = this.context.getBean(VelocityLanguageDriverConfig.class);
    @SuppressWarnings("deprecation")
    String[] userDirective = config.getUserdirective();
    assertThat(userDirective).hasSize(1).contains(NowDirective.class.getName());
    assertThat(config.getAdditionalContextAttributes()).hasSize(2);
    assertThat(config.getAdditionalContextAttributes().get("attribute1")).isEqualTo("java.lang.String");
    assertThat(config.getAdditionalContextAttributes().get("attribute2")).isEqualTo("java.util.HashMap");
    assertThat(config.getVelocitySettings()).hasSize(3);
    assertThat(config.getVelocitySettings().get(RuntimeConstants.RESOURCE_LOADERS)).isEqualTo("class");
    assertThat(config.getVelocitySettings().get(RuntimeConstants.RESOURCE_LOADER + ".class.class"))
        .isEqualTo("org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    assertThat(config.generateCustomDirectivesString()).isEqualTo(NowDirective.class.getName()
        + ",org.mybatis.scripting.velocity.TrimDirective,org.mybatis.scripting.velocity.WhereDirective,org.mybatis.scripting.velocity.SetDirective,org.mybatis.scripting.velocity.InDirective,org.mybatis.scripting.velocity.RepeatDirective");
    assertThat(config.getVelocitySettings().get(RuntimeConstants.INPUT_ENCODING))
        .isEqualTo(RuntimeConstants.ENCODING_DEFAULT);

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

  @org.springframework.context.annotation.Configuration
  static class TestingLegacyFreeMarkerConfiguration
      extends MybatisLanguageDriverAutoConfiguration.LegacyFreeMarkerConfiguration {
  }

  @org.springframework.context.annotation.Configuration
  static class TestingLegacyVelocityConfiguration
      extends MybatisLanguageDriverAutoConfiguration.LegacyVelocityConfiguration {
  }

  private static class MyLanguageDriverConfig {
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

  private static class VelocityCustomLanguageDriverConfig {
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

/*
 *    Copyright 2015-2022 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertAll;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

/**
 * Tests for definition of additional-spring-configuration-metadata.json.
 *
 * @author Kazuki Shimizu
 * @since 1.3.1
 */
class AdditionalConfigurationMetadataTest {

  @Test
  void testProperties() throws IOException {

    DocumentContext documentContext = JsonPath
        .parse(new FileSystemResource("src/main/resources/META-INF/additional-spring-configuration-metadata.json")
            .getInputStream());

    List<Map<String, Object>> properties = documentContext.read("$.properties");

    assertAll(() -> assertThat(properties.size()).isEqualTo(6), () -> {
      // assert for mybatis.configuration.default-scripting-language
      Map<String, Object> element = properties.get(0);
      assertThat(element.get("sourceType")).isEqualTo("org.apache.ibatis.session.Configuration");
      assertThat(element.get("defaultValue")).isEqualTo("org.apache.ibatis.scripting.xmltags.XMLLanguageDriver");
      assertThat(element.get("name")).isEqualTo("mybatis.configuration.default-scripting-language");
      assertThat(element.get("type"))
          .isEqualTo("java.lang.Class<? extends org.apache.ibatis.scripting.LanguageDriver>");
      @SuppressWarnings("unchecked")
      Map<String, Object> deprecation = (Map<String, Object>) element.get("deprecation");
      assertThat(deprecation.get("reason")).isEqualTo(
          "Because when this configuration property is used, there is case that custom language driver cannot be registered correctly.");
      assertThat(deprecation.get("replacement")).isEqualTo("mybatis.default-scripting-language-driver");
    }, () -> {
      // assert for mybatis.configuration.default-enum-type-handler
      Map<String, Object> element = properties.get(1);
      assertThat(element.get("sourceType")).isEqualTo("org.apache.ibatis.session.Configuration");
      assertThat(element.get("defaultValue")).isEqualTo("org.apache.ibatis.type.EnumTypeHandler");
      assertThat(element.get("name")).isEqualTo("mybatis.configuration.default-enum-type-handler");
      assertThat(element.get("type")).isEqualTo("java.lang.Class<? extends org.apache.ibatis.type.TypeHandler>");
    }, () -> {
      // assert for mybatis.lazy-initialization
      Map<String, Object> element = properties.get(2);
      assertThat(element.get("defaultValue")).isEqualTo(false);
      assertThat(element.get("name")).isEqualTo("mybatis.lazy-initialization");
      assertThat(element.get("type")).isEqualTo("java.lang.Boolean");
    }, () -> {
      // assert for mybatis.mapper-default-scope
      Map<String, Object> element = properties.get(3);
      assertThat(element.get("defaultValue")).isEqualTo("");
      assertThat(element.get("name")).isEqualTo("mybatis.mapper-default-scope");
      assertThat(element.get("type")).isEqualTo("java.lang.String");
    }, () -> {
      // assert for mybatis.inject-sql-session-on-mapper-scan
      Map<String, Object> element = properties.get(4);
      assertThat(element.get("defaultValue")).isEqualTo(true);
      assertThat(element.get("name")).isEqualTo("mybatis.inject-sql-session-on-mapper-scan");
      assertThat(element.get("type")).isEqualTo("java.lang.Boolean");
    }, () -> {
      // assert for mybatis.scripting-language-driver.velocity.userdirective
      Map<String, Object> element = properties.get(5);
      assertThat(element.get("name")).isEqualTo("mybatis.scripting-language-driver.velocity.userdirective");
      @SuppressWarnings("unchecked")
      Map<String, Object> deprecation = (Map<String, Object>) element.get("deprecation");
      assertThat(deprecation.get("level")).isEqualTo("error");
      assertThat(deprecation.get("reason")).isEqualTo(
          "The 'userdirective' is deprecated since Velocity 2.x. This property defined for keeping backward compatibility with older velocity version.");
      assertThat(deprecation.get("replacement"))
          .isEqualTo("mybatis.scripting-language-driver.velocity.velocity-settings.runtime.custom_directives");
    });
  }
}

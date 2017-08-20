/**
 *    Copyright 2015-2017 the original author or authors.
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for definition of additional-spring-configuration-metadata.json.
 *
 * @author Kazuki Shimizu
 * @since 1.3.1
 */
public class AdditionalConfigurationMetadataTest {

	@Test
	public void testProperties() throws IOException {

		DocumentContext documentContext = JsonPath
				.parse(new FileSystemResource("src/main/resources/META-INF/additional-spring-configuration-metadata.json")
						.getInputStream());

		List<Map<String, String>> properties = documentContext.read("$.properties");

		assertThat(properties.size(), is(2));

		// assert for default-scripting-language
		{
			Map<String, String> element = properties.get(0);
			assertThat(element.get("sourceType"), is("org.apache.ibatis.session.Configuration"));
			assertThat(element.get("defaultValue"), is("org.apache.ibatis.scripting.xmltags.XMLLanguageDriver"));
			assertThat(element.get("name"), is("mybatis.configuration.default-scripting-language"));
			assertThat(element.get("type"), is("java.lang.Class<? extends org.apache.ibatis.scripting.LanguageDriver>"));
		}

		// assert for default-enum-type-handler
		{
		  Map<String, String> element = properties.get(1);
		  assertThat(element.get("sourceType"), is("org.apache.ibatis.session.Configuration"));
		  assertThat(element.get("defaultValue"), is("org.apache.ibatis.type.EnumTypeHandler"));
		  assertThat(element.get("name"), is("mybatis.configuration.default-enum-type-handler"));
		  assertThat(element.get("type"), is("java.lang.Class<? extends org.apache.ibatis.type.TypeHandler>"));
		}

	}

}

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
package org.mybatis.spring.boot.test.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test with custom filter for {@link MybatisTest}.
 *
 * @author Kazuki Shimizu
 * @since 1.2.1
 */
@RunWith(SpringRunner.class)
@MybatisTest(includeFilters = @ComponentScan.Filter(Component.class), excludeFilters = @ComponentScan.Filter(Service.class))
@TestPropertySource(properties = {
  "mybatis.type-aliases-package=org.mybatis.spring.boot.test.autoconfigure"
})
public class MybatisTestCustomFilterIntegrationTest {

  @Autowired
  private ExampleComponent component;

  @Autowired(required = false)
  private ExampleService service;

  @Test
  public void testIncludeFilter() {
    assertThat(component.getMessage()).isEqualTo("Hello!");
  }

  @Test
  public void testExcludeFilter() {
    assertThat(service).isNull();
  }

}

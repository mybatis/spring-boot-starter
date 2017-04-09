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

import org.springframework.stereotype.Component;

/**
 * Example component that annotated {@link Component @Component} used with {@link MybatisTest} tests.
 *
 * @author wonwoo
 * @since 1.2.1
 */
@Component
public class ExampleComponent {

  public String getMessage() {
    return "Hello!";
  }

}

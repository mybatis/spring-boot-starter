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
package sample.mybatis.war;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * @author Kazuki Shimizu
 */
class SampleMybatisApplicationIT {

  @Test
  void test() {
    RestTemplate restTemplate = new RestTemplate();
    @SuppressWarnings("unchecked")
    Map<String, Object> body = restTemplate
        .getForObject("http://localhost:18080/mybatis-spring-boot-sample-war/cities/{state}", Map.class, "CA");
    assertThat(body).hasSize(4).containsEntry("id", 1).containsEntry("name", "San Francisco")
        .containsEntry("state", "CA").containsEntry("country", "US");
  }

}

/*
 *    Copyright 2015-2023 the original author or authors.
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
package sample.mybatis.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import sample.mybatis.web.domain.City;
import sample.mybatis.web.mapper.CityMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kazuki Shimizu
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SampleMybatisWithMockBeanApplicationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  CityMapper cityMapper;
  
  @BeforeEach
  void setup() {
    City city = new City();
    city.setId(10L);
    city.setCountry("US");
    city.setState("NV");
    city.setName("Las Vegas");
    Mockito.when(cityMapper.findByState("NV")).thenReturn(city);
  }

  @Test
  void test() {
    @SuppressWarnings("unchecked")
    Map<String, Object> body = this.restTemplate.getForObject("/cities/{state}", Map.class, "NV");
    assertThat(body).hasSize(4).containsEntry("id", 10).containsEntry("name", "Las Vegas").containsEntry("state", "NV")
        .containsEntry("country", "US");
  }
}

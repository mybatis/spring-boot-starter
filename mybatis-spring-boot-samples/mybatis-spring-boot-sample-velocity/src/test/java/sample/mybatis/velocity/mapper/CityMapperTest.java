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
package sample.mybatis.velocity.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import sample.mybatis.velocity.domain.City;

/**
 * Tests for {@link CityMapper}.
 *
 * @author Kazuki Shimizu
 */
@MybatisTest
class CityMapperTest {

  @Autowired
  private CityMapper cityMapper;

  @Test
  @Disabled("Does not support template file yet")
  void findByState() {
    City city = cityMapper.findByState("CA");
    assertThat(city.getId()).isEqualTo(1);
    assertThat(city.getName()).isEqualTo("San Francisco");
    assertThat(city.getState()).isEqualTo("CA");
    assertThat(city.getCountry()).isEqualTo("US");
  }

  @Test
  void findById() {
    City city = cityMapper.findById(1L);
    assertThat(city.getId()).isEqualTo(1);
    assertThat(city.getName()).isEqualTo("San Francisco");
    assertThat(city.getState()).isEqualTo("CA");
    assertThat(city.getCountry()).isEqualTo("US");
  }

  @Test
  void findByName() {
    City city = cityMapper.findByName("San Francisco");
    assertThat(city.getId()).isEqualTo(1);
    assertThat(city.getName()).isEqualTo("San Francisco");
    assertThat(city.getState()).isEqualTo("CA");
    assertThat(city.getCountry()).isEqualTo("US");
  }

}

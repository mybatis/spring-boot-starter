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
package sample.mybatis.kotlin.mapper

import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest

import org.springframework.beans.factory.annotation.Autowired

import org.assertj.core.api.Assertions.assertThat

@MybatisTest
class CityMapperTest {

  @Autowired
  lateinit var cityMapper: CityMapper

  @Test
  fun findByState() {
    val city = cityMapper.findByState("CA")
    assertThat(city.id).isEqualTo(1)
    assertThat(city.name).isEqualTo("San Francisco")
    assertThat(city.state).isEqualTo("CA")
    assertThat(city.country).isEqualTo("US")
  }

}

/**
 * Copyright 2015-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.mybatis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import sample.mybatis.domain.City;
import sample.mybatis.mapper.CityMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author wonwoo
 * @since 1.2.1
 */
@RunWith(SpringRunner.class)
@MybatisTest
public class MapperTests {

  @Autowired
  private CityMapper cityMapper;

  @Test
  public void mapperIsNotNullTest() {
    assertThat(cityMapper).isNotNull();
  }

  @Test
  public void findBynameTest() {
    City city = cityMapper.findByname("Seoul");
    assertThat(city.getName()).isEqualTo("Seoul");
    assertThat(city.getState()).isEqualTo("Seoul KR");
    assertThat(city.getCountry()).isEqualTo("KOREA");
  }
}

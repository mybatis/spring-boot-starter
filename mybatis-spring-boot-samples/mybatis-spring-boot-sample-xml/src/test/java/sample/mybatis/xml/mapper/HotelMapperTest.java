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
package sample.mybatis.xml.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import sample.mybatis.xml.domain.Hotel;

/**
 * Tests for {@link HotelMapper}.
 *
 * @author wonwoo
 *
 * @since 1.2.1
 */
@MybatisTest
class HotelMapperTest {

  @Autowired
  private HotelMapper hotelMapper;

  @Test
  void selectByCityIdTest() {
    Hotel hotel = hotelMapper.selectByCityId(1);
    assertThat(hotel.getCity()).isEqualTo(1);
    assertThat(hotel.getName()).isEqualTo("Conrad Treasury Place");
    assertThat(hotel.getAddress()).isEqualTo("William & George Streets");
    assertThat(hotel.getZip()).isEqualTo("4001");
  }

}

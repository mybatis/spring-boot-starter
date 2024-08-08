/*
 *    Copyright 2015-2024 the original author or authors.
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
package sample.mybatis.graalvm.annotation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import sample.mybatis.graalvm.annotation.domain.City;

/**
 * Tests for {@link CityMapper}.
 *
 * @author wonwoo
 *
 * @since 1.2.1
 */
@MybatisTest
class CityMapperTest {

  @Autowired
  private CityMapper cityMapper;

  @Test
  void findByStateTest() {
    City city = cityMapper.findByState("CA");
    assertThat(city.getId()).isEqualTo(1);
    assertThat(city.getName()).isEqualTo("San Francisco");
    assertThat(city.getState()).isEqualTo("CA");
    assertThat(city.getCountry()).isEqualTo("US");
  }

  @Test
  void listByStateTest() {
    List<City> cityList = cityMapper.listByState("ShanDong");
    assertThat(cityList).isNotNull();
    assertThat(cityList.size()).isEqualTo(2);

    assertThat(cityList).satisfies(cities -> {
      assertThat(cities).filteredOn(city -> city.getId() == 2 && "Jinan".equals(city.getName())
          && "ShanDong".equals(city.getState()) && "CN".equals(city.getCountry())).hasSize(1);
      assertThat(cities).filteredOn(city -> city.getId() == 3 && "Tsingtao".equals(city.getName())
          && "ShanDong".equals(city.getState()) && "CN".equals(city.getCountry())).hasSize(1);
    });
  }

  @Test
  void mapByStateTest() {
    Map<String, Object> map = cityMapper.mapByState("CA");
    assertThat(map).isNotNull();
    assertThat(map.size()).isEqualTo(4);
    assertThat(map).satisfies(cities -> {
      assertThat(cities)
          .contains(entry("ID", 1), entry("NAME", "San Francisco"), entry("STATE", "CA"), entry("COUNTRY", "US"))
          .isNotNull();
    });
  }

  @Test
  void listMapByStateTest() {
    List<Map<String, Object>> cityList = cityMapper.listMapByState("ShanDong");
    assertThat(cityList).isNotNull();
    assertThat(cityList).satisfies(map -> {
      assertThat(map).isNotNull();
      assertThat(map.size()).isEqualTo(2);
      assertThat(map).satisfies(cities -> {
        assertThat(cities).filteredOn(city -> city.size() == 4 && city.get("ID") instanceof Integer
            && (Integer) city.get("ID") == 2 && "Jinan".equals(city.get("NAME")) && "ShanDong".equals(city.get("STATE"))
            && "CN".equals(city.get("COUNTRY"))).hasSize(1);
        assertThat(cities).filteredOn(city -> city.size() == 4 && city.get("ID") instanceof Integer
            && (Integer) city.get("ID") == 3 && "Tsingtao".equals(city.get("NAME"))
            && "ShanDong".equals(city.get("STATE")) && "CN".equals(city.get("COUNTRY"))).hasSize(1);
      });
    });
  }

  @Test
  void treeSetStateByStateTest() {
    TreeSet<String> stateSet = cityMapper.treeSetStateByState("CN");
    assertThat(stateSet).isNotNull();
    assertThat(stateSet.size()).isEqualTo(1);
    assertThat(stateSet.first()).isEqualTo("ShanDong");
  }

  @Test
  void hashSetStateByStateTest() {
    HashSet<String> stateSet = cityMapper.hashSetStateByState("CN");
    assertThat(stateSet).isNotNull();
    assertThat(stateSet.size()).isEqualTo(1);
    assertThat(stateSet).contains("ShanDong");
  }

}

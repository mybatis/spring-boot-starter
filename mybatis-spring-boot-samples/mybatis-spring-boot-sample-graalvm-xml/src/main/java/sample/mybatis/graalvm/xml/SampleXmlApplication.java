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
package sample.mybatis.graalvm.xml;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import sample.mybatis.graalvm.xml.dao.CityDao;
import sample.mybatis.graalvm.xml.mapper.CityMapper;
import sample.mybatis.graalvm.xml.mapper.HotelMapper;

@SpringBootApplication
public class SampleXmlApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(SampleXmlApplication.class, args);
  }

  private final CityDao cityDao;

  private final CityMapper cityMapper;

  private final HotelMapper hotelMapper;

  public SampleXmlApplication(CityDao cityDao, CityMapper cityMapper, HotelMapper hotelMapper) {
    this.cityDao = cityDao;
    this.cityMapper = cityMapper;
    this.hotelMapper = hotelMapper;
  }

  @Override
  @SuppressWarnings("squid:S106")
  public void run(String... args) {
    System.out.println(this.cityDao.selectCityById(1));
    System.out.println(this.cityMapper.findByState("CA"));
    System.out.println(this.cityMapper.listByState("ShanDong"));
    System.out.println(this.cityMapper.mapByState("CA"));
    System.out.println(this.cityMapper.listMapByState("ShanDong"));
    System.out.println(this.cityMapper.treeSetStateByState("CN"));
    System.out.println(this.cityMapper.hashSetStateByState("CN"));
    System.out.println(this.hotelMapper.selectByCityId(1));
  }

}

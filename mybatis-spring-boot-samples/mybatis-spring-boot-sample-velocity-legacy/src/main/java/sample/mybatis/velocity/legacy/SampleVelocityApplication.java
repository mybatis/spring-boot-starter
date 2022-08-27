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
package sample.mybatis.velocity.legacy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import sample.mybatis.velocity.legacy.mapper.CityMapper;

/**
 * @author Kazuki Shimizu
 */
@SpringBootApplication
public class SampleVelocityApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(SampleVelocityApplication.class, args);
  }

  private final CityMapper cityMapper;

  public SampleVelocityApplication(CityMapper cityMapper) {
    this.cityMapper = cityMapper;
  }

  @Override
  @SuppressWarnings("squid:S106")
  public void run(String... args) {
    System.out.println(this.cityMapper.findById(1L));
  }

}

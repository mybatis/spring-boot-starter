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
package sample.mybatis.groovy

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.transaction.annotation.Transactional

import sample.mybatis.groovy.mapper.CityMapper

@SpringBootApplication
class SampleGroovyApplication implements CommandLineRunner {

	static void main(String[] args) {
		SpringApplication.run(SampleGroovyApplication, args)
	}

	final CityMapper cityMapper

	SampleGroovyApplication(CityMapper cityMapper) {
		this.cityMapper = cityMapper;
	}

	@Transactional
	@Override
	void run(String... args) throws Exception {
		println cityMapper.findByState("CA")
	}

}

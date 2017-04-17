/**
 *    Copyright 2015-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.boot.autoconfigure;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eddú Meléndez
 */
public class MybatisPropertiesTest {

    @Test
    public void emptyMapperLocations() {
        MybatisProperties properties = new MybatisProperties();
        assertThat(properties.resolveMapperLocations()).isEmpty();
    }

    @Test
    public void twoLocations() {
        MybatisProperties properties = new MybatisProperties();
        properties
                .setMapperLocations(new String[] {
                        "classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml",
                        "classpath:org/mybatis/spring/boot/autoconfigure/repository/*Mapper.xml" });
        assertThat(properties.resolveMapperLocations()).hasSize(2);
    }

	@Test
	public void twoLocationsWithOneIncorrectLocation() {
		MybatisProperties properties = new MybatisProperties();
		properties
				.setMapperLocations(new String[] {
						"classpath:org/mybatis/spring/boot/autoconfigure/repository/CityMapper.xml",
						"classpath:org/mybatis/spring/boot/autoconfigure/repositoy/*Mapper.xml" });
		assertThat(properties.resolveMapperLocations()).hasSize(1);
	}

}

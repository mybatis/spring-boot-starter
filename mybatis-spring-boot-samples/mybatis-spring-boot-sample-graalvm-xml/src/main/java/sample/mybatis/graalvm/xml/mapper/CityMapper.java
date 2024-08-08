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
package sample.mybatis.graalvm.xml.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import sample.mybatis.graalvm.xml.domain.City;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Eddú Meléndez
 */
@Mapper
public interface CityMapper {

    City findByState(@Param("state") String state);

    List<City> listByState(@Param("state") String state);

    Map<String, Object> mapByState(@Param("state") String state);

    List<Map<String, Object>> listMapByState(@Param("state") String state);

    TreeSet<String> treeSetStateByState(@Param("country") String country);

    HashSet<String> hashSetStateByState(@Param("country") String country);

}

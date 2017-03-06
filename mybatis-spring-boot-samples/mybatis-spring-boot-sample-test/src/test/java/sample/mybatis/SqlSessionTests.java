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

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import sample.mybatis.domain.City;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author wonwoo
 * @since 1.2.1
 */
@RunWith(SpringRunner.class)
@MybatisTest
public class SqlSessionTests {

  @Autowired
  private SqlSession sqlSession;

  @Test
  public void sqlSessionIsNotNullTest() {
    assertThat(sqlSession).isNotNull();
  }


  @Test
  public void findByIdTest() {
    City city = sqlSession.selectOne("findById", 1);
    assertThat(city.getName()).isEqualTo("Seoul");
    assertThat(city.getState()).isEqualTo("Seoul KR");
    assertThat(city.getCountry()).isEqualTo("KOREA");
  }

  @Test
  public void createCityTest() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("name", "San Francisco");
    parameters.put("state", "CA");
    parameters.put("country", "US");
    sqlSession.insert("createCity", parameters);

    City city = sqlSession.selectOne("findByname", "San Francisco");
    assertThat(city.getName()).isEqualTo("San Francisco");
    assertThat(city.getState()).isEqualTo("CA");
    assertThat(city.getCountry()).isEqualTo("US");
  }
}

package org.mybatis.spring.boot.autoconfigure.repository;

import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.domain.City;
import org.springframework.beans.factory.annotation.Autowired;

public class CityMapperImpl {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	public City findById(long id) {
		return this.sqlSessionTemplate.selectOne("selectCityById", id);
	}

}

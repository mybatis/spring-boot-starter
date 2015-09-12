package sample.mybatis.mapper;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sample.mybatis.domain.City;

@Component
public class CityMapper {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	public City selectCityById(long id) {
		return this.sqlSessionTemplate.selectOne("selectCityById", id);
	}

}

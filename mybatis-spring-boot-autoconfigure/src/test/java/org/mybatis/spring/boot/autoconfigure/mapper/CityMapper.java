package org.mybatis.spring.boot.autoconfigure.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mybatis.spring.boot.autoconfigure.domain.City;

public interface CityMapper {

	@Select("SELECT * FROM city WHERE id = #{cityId}")
	City findById(@Param("cityId") Long cityId);

}

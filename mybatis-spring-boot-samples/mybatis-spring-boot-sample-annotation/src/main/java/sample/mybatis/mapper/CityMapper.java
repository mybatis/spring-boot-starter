package sample.mybatis.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import sample.mybatis.domain.City;

/**
 * @author Eddú Meléndez
 */
public interface CityMapper {

	@Select("SELECT * FROM CITY WHERE state = #{state}")
	City findByState(@Param("state") String state);

}

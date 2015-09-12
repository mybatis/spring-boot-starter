package sample.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import sample.mybatis.mapper.CityMapper;

@SpringBootApplication
public class SampleMybatisApplication implements CommandLineRunner {

	@Autowired
	private CityMapper cityMapper;

	public static void main(String[] args) {
		SpringApplication.run(SampleMybatisApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println(this.cityMapper.selectCityById(1));
	}

}

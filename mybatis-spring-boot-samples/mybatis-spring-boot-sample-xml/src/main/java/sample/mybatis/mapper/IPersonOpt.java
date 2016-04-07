package sample.mybatis.mapper;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import sample.mybatis.domain.Person;

import javax.annotation.Resource;

/**
 * Created by lky on 2016/3/18.
 */
@Repository
public interface IPersonOpt {

    Person selectPersonById(long id);
}

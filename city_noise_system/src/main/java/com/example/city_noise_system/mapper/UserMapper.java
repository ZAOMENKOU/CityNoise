package com.example.city_noise_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.city_noise_system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询所有用户，包括已删除的
     * @return 所有用户列表
     */
    @Select("SELECT * FROM users")
    List<User> selectAllIncludingDeleted();

}
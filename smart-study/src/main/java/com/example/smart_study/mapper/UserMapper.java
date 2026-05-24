package com.example.smart_study.mapper;

import com.example.smart_study.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    // 登录查询
    @Select("SELECT * FROM users WHERE username = #{username} AND password = #{password}")
    User login(String username, String password);

    // --- 新增：用户注册 ---
    @Insert("INSERT INTO users(username, password, role) VALUES(#{username}, #{password}, #{role})")
    void register(User user);

    // 根据ID找人
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);
}

package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.User;
import com.itheim.program_platform_backend.domain.po.UserToken;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {
    @Insert("insert into user(username, password, email, nickname, role, phone,create_time,update_time) " +
            "values(#{username}, #{password}, #{email}, #{nickname}, #{role}, #{phone},#{createTime},#{updateTime})")
    void addUser(User user);

    @Select("SELECT id, username, password, email, nickname, avatar, role, phone, create_time, update_time FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Delete("delete from user_token where user_id = #{id}")
    void deleteUserTokenByUserId(Long id);
    
    @Insert("INSERT INTO user_token(user_id, token, expire_time, create_time, update_time) " +
            "VALUES(#{userId}, #{token}, #{expireTime}, #{createTime}, #{updateTime})")
    void saveUserToken(UserToken userToken);

    @Select("SELECT COUNT(*) FROM user_token WHERE token = #{token}")
    int countByToken(String token);
}

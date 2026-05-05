package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.User;
import com.itheim.program_platform_backend.domain.po.UserToken;
import org.apache.ibatis.annotations.*;

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

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long userId);

    @Update("UPDATE user SET nickname = #{nickname}, phone = #{phone}, update_time = NOW() WHERE id = #{id}")
    void updateUserById(@Param("id") Long id, @Param("nickname") String nickname, @Param("phone") String phone);

    @Update("UPDATE user SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    void updateAvatarById(@Param("id") Long id, @Param("avatar") String avatar);

    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    void updatePasswordById(@Param("id") Long id, @Param("password") String password);
}

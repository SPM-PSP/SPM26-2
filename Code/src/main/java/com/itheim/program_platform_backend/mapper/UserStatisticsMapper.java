package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.UserStatistics;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserStatisticsMapper {
    @Select("SELECT * FROM `user_statistics` WHERE user_id = #{userId}")
    UserStatistics selectByUserId(Long userId);

    @Insert("INSERT INTO `user_statistics`(user_id, total_submit, total_accept, create_time, update_time) " +
            "VALUES(#{userId}, #{totalSubmit}, #{totalAccept}, #{createTime}, #{updateTime})")
    void insert(UserStatistics userStatistics);

    @Update("UPDATE `user_statistics` SET total_submit = #{totalSubmit}, total_accept = #{totalAccept}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    void updateByUserId(@Param("userId") Long userId, @Param("totalSubmit") Integer totalSubmit, @Param("totalAccept") Integer totalAccept);
}
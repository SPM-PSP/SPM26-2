package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.Evaluation;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EvaluationMapper {
    @Insert("INSERT INTO `evaluation`(submission_id, time_complexity, space_complexity, code_style, readability, optimization, ai_model, cost, create_time) " +
            "VALUES(#{submissionId}, #{timeComplexity}, #{spaceComplexity}, #{codeStyle}, #{readability}, #{optimization}, #{aiModel}, #{cost}, #{createTime})")
    void insert(Evaluation evaluation);

    @Select("SELECT * FROM `evaluation` WHERE submission_id = #{submissionId}")
    Evaluation selectBySubmissionId(Long submissionId);
}
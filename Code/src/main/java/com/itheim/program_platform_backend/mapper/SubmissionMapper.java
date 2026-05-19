package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.dto.SubmissionQueryDTO;
import com.itheim.program_platform_backend.domain.po.Submission;
import com.itheim.program_platform_backend.domain.vo.SubmissionDetailVO;
import com.itheim.program_platform_backend.domain.vo.SubmissionListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubmissionMapper {

    /**
     * 查询用户提交记录列表
     */
    List<SubmissionListItemVO> selectSubmissionList(@Param("userId") Long userId,
                                                    @Param("query") SubmissionQueryDTO queryDTO);

    SubmissionDetailVO selectSubmissionDetail(@Param("submissionId") Long submissionId,
                                              @Param("userId") Long userId);

    /**
     * 插入提交记录
     */
    int insertSubmission(Submission submission);

    /**
     * 统计题目的总提交用户数（去重）
     */
    Integer countDistinctUsersByProblemId(@Param("problemId") Long problemId);

    /**
     * 统计题目通过的用户数（去重，status=0表示通过）
     */
    Integer countDistinctAcceptedUsersByProblemId(@Param("problemId") Long problemId);
}

package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.dto.SubmissionQueryDTO;
import com.itheim.program_platform_backend.domain.vo.PageResultVO;
import com.itheim.program_platform_backend.domain.vo.SubmissionDetailVO;
import com.itheim.program_platform_backend.domain.vo.SubmissionListItemVO;

public interface SubmissionService {

    /**
     * 获取用户提交记录列表
     */
    PageResultVO<SubmissionListItemVO> getSubmissionList(Long userId, SubmissionQueryDTO queryDTO);

    SubmissionDetailVO getSubmissionDetail(Long userId, Long submissionId);
}

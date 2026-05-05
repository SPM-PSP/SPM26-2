package com.itheim.program_platform_backend.service.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheim.program_platform_backend.domain.dto.SubmissionQueryDTO;
import com.itheim.program_platform_backend.domain.vo.PageResultVO;
import com.itheim.program_platform_backend.domain.vo.SubmissionDetailVO;
import com.itheim.program_platform_backend.domain.vo.SubmissionListItemVO;
import com.itheim.program_platform_backend.enums.CommonResultCode;
import com.itheim.program_platform_backend.exception.BusinessException;
import com.itheim.program_platform_backend.mapper.SubmissionMapper;
import com.itheim.program_platform_backend.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private SubmissionMapper submissionMapper;

    @Override
    public PageResultVO<SubmissionListItemVO> getSubmissionList(Long userId, SubmissionQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());

        List<SubmissionListItemVO> list = submissionMapper.selectSubmissionList(userId, queryDTO);

        PageInfo<SubmissionListItemVO> pageInfo = new PageInfo<>(list);

        PageResultVO<SubmissionListItemVO> result = new PageResultVO<>();
        result.setTotal(pageInfo.getTotal());
        result.setPages((long) pageInfo.getPages());
        result.setCurrentPage((long) pageInfo.getPageNum());
        result.setList(pageInfo.getList());

        return result;
    }

    @Override
    public SubmissionDetailVO getSubmissionDetail(Long userId, Long submissionId) {
        SubmissionDetailVO detail = submissionMapper.selectSubmissionDetail(submissionId, userId);

        if (detail == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR,"提交记录不存在或无权查看");
        }

        return detail;
    }


}

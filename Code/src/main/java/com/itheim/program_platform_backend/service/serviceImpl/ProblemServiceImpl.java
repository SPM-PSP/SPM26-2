package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.domain.po.Category;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.Solution;
import com.itheim.program_platform_backend.domain.vo.CategoryVO;
import com.itheim.program_platform_backend.domain.vo.PageResult;
import com.itheim.program_platform_backend.domain.vo.ProblemDetailVO;
import com.itheim.program_platform_backend.domain.vo.ProblemListItemVO;
import com.itheim.program_platform_backend.domain.vo.ProblemSolutionVO;
import com.itheim.program_platform_backend.domain.vo.SolutionVO;
import com.itheim.program_platform_backend.exception.BusinessException;
import com.itheim.program_platform_backend.mapper.ProblemMapper;
import com.itheim.program_platform_backend.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题库管理Service实现
 */
@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemMapper problemMapper;

    @Override
    public List<CategoryVO> getCategoryList() {
        List<Category> categories = problemMapper.selectAllCategories();
        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setCategoryId(c.getId());
            vo.setCategoryName(c.getName());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<ProblemListItemVO> getProblemList(int page, int size, List<String> problemCategory,
                                                         String difficulty, String keyword, Integer status, Long userId) {
        // 未登录用户忽略状态筛选
        if (userId == null) {
            status = null;
        }
        int offset = (page - 1) * size;
        long total = problemMapper.countProblemList(difficulty, keyword, problemCategory, status, userId);
        List<Problem> problems = problemMapper.selectProblemList(offset, size, difficulty, keyword, problemCategory, status, userId);

        // 查询分类名称
        Map<Long, List<String>> categoryMap = new HashMap<>();
        for (Problem p : problems) {
            List<String> names = problemMapper.selectCategoryNamesByProblemId(p.getId());
            categoryMap.put(p.getId(), names);
        }

        // 查询用户状态
        Map<Long, Integer> statusMap = new HashMap<>();
        if (userId != null) {
            for (Problem p : problems) {
                Integer s = problemMapper.selectUserProblemStatus(userId, p.getId());
                if (s != null) {
                    statusMap.put(p.getId(), s);
                }
            }
        }

        List<ProblemListItemVO> list = problems.stream().map(p -> {
            ProblemListItemVO vo = new ProblemListItemVO();
            vo.setProblemId(p.getId());
            vo.setTitle(p.getTitle());
            vo.setDifficulty(p.getDifficulty());
            vo.setAcceptRate(p.getPassRate());
            vo.setCategoryNames(categoryMap.getOrDefault(p.getId(), Collections.emptyList()));
            if (userId != null) {
                vo.setStatus(statusMap.get(p.getId()));
            }
            return vo;
        }).collect(Collectors.toList());

        long pages = total == 0 ? 0 : (total + size - 1) / size;

        PageResult<ProblemListItemVO> result = new PageResult<>();
        result.setTotal(total);
        result.setPages(pages);
        result.setCurrentPage(page);
        result.setList(list);
        return result;
    }

    @Override
    public ProblemDetailVO getProblemDetail(Long problemId, Long userId) {
        Problem problem = problemMapper.selectProblemById(problemId);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在");
        }

        ProblemDetailVO vo = new ProblemDetailVO();
        vo.setProblemId(problem.getId());
        vo.setTitle(problem.getTitle());
        vo.setDifficulty(problem.getDifficulty());
        vo.setDescription(problem.getDescription());
        vo.setInputFormat(problem.getInputFormat());
        vo.setOutputFormat(problem.getOutputFormat());
        vo.setSampleInput(problem.getSampleInput());
        vo.setSampleOutput(problem.getSampleOutput());
        vo.setTimeLimit(problem.getTimeLimit());
        vo.setMemoryLimit(problem.getMemoryLimit());
        vo.setAcceptRate(problem.getPassRate());
        vo.setCategoryNames(problemMapper.selectCategoryNamesByProblemId(problemId));

        if (userId != null) {
            Integer status = problemMapper.selectUserProblemStatus(userId, problemId);
            vo.setUserStatus(status);
        }

        return vo;
    }

    @Override
    public ProblemSolutionVO getProblemSolution(Long problemId) {
        Problem problem = problemMapper.selectProblemById(problemId);
        if (problem == null) {
            throw new BusinessException(404, "该题不存在");
        }

        List<Solution> solutions = problemMapper.selectSolutionsByProblemId(problemId);

        ProblemSolutionVO vo = new ProblemSolutionVO();
        vo.setProblemId(problemId);

        if (solutions == null || solutions.isEmpty()) {
            vo.setSolution(Collections.emptyList());
            return vo;
        }

        List<SolutionVO> solutionVOList = solutions.stream().map(s -> {
            SolutionVO sv = new SolutionVO();
            sv.setSolutionId(s.getId());
            sv.setTitle(s.getTitle());
            sv.setContent(s.getContent());
            sv.setLanguage(s.getLanguage());
            sv.setCode(s.getCode());
            sv.setCreateTime(s.getCreateTime());

            Long createUserId = s.getCreateUserId();
            if (createUserId != null && createUserId == -1L) {
                sv.setCreateUserName("AI");
            } else if (createUserId != null) {
                String username = problemMapper.selectUsernameById(createUserId);
                sv.setCreateUserName(username != null ? username : "该用户已被注销");
            } else {
                sv.setCreateUserName("未知用户");
            }

            return sv;
        }).collect(Collectors.toList());

        vo.setSolution(solutionVOList);
        return vo;
    }
}

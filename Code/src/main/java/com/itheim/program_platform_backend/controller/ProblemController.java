package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.domain.vo.CategoryVO;
import com.itheim.program_platform_backend.domain.vo.PageResult;
import com.itheim.program_platform_backend.domain.vo.ProblemDetailVO;
import com.itheim.program_platform_backend.domain.vo.ProblemListItemVO;
import com.itheim.program_platform_backend.domain.vo.ProblemSolutionVO;
import com.itheim.program_platform_backend.service.ProblemService;
import com.itheim.program_platform_backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库管理Controller
 */
@RestController
@RequestMapping("/api/v1/problem")
@RequiredArgsConstructor
@Api(tags = "题库管理接口")
public class ProblemController {

    private final ProblemService problemService;
    private final JwtUtil jwtUtil;

    /**
     * 4.2 获取题目分类列表
     */
    @ApiOperation("获取题目分类列表")
    @GetMapping("/category/list")
    public Result<List<CategoryVO>> getCategoryList() {
        return Result.success(problemService.getCategoryList());
    }

    /**
     * 4.3 获取题库题目列表
     */
    @ApiOperation("获取题库题目列表")
    @GetMapping("/list")
    public Result<PageResult<ProblemListItemVO>> getProblemList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> problemCategory,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = getOptionalUserId(request);
        PageResult<ProblemListItemVO> result = problemService.getProblemList(page, size, problemCategory, difficulty, keyword, status, userId);
        return Result.success(result);
    }

    /**
     * 4.4 获取题目详情
     */
    @ApiOperation("获取题目详情")
    @GetMapping("/detail/{problemId}")
    public Result<ProblemDetailVO> getProblemDetail(@PathVariable Long problemId, HttpServletRequest request) {
        Long userId = getOptionalUserId(request);
        ProblemDetailVO detail = problemService.getProblemDetail(problemId, userId);
        return Result.success(detail);
    }

    /**
     * 4.5 获取题目题解
     */
    @ApiOperation("获取题目题解")
    @GetMapping("/solution/{problemId}")
    public Result<ProblemSolutionVO> getProblemSolution(@PathVariable Long problemId) {
        ProblemSolutionVO solution = problemService.getProblemSolution(problemId);
        return Result.success(solution);
    }

    /**
     * 从请求头中尝试获取用户ID（token可选）
     */
    private Long getOptionalUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            return ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
            return null;
        }
    }
}

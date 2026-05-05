package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.Category;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.Solution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题库管理Mapper
 */
@Mapper
public interface ProblemMapper {

    /**
     * 获取所有题目分类
     */
    List<Category> selectAllCategories();

    /**
     * 统计符合条件的题目数量
     */
    long countProblemList(@Param("difficulty") String difficulty,
                          @Param("keyword") String keyword,
                          @Param("categoryNames") List<String> categoryNames,
                          @Param("status") Integer status,
                          @Param("userId") Long userId);

    /**
     * 分页查询题目列表
     */
    List<Problem> selectProblemList(@Param("offset") int offset,
                                    @Param("size") int size,
                                    @Param("difficulty") String difficulty,
                                    @Param("keyword") String keyword,
                                    @Param("categoryNames") List<String> categoryNames,
                                    @Param("status") Integer status,
                                    @Param("userId") Long userId);

    /**
     * 根据ID查询题目详情
     */
    Problem selectProblemById(Long problemId);

    /**
     * 查询题目的分类名称列表
     */
    List<String> selectCategoryNamesByProblemId(Long problemId);

    /**
     * 查询用户对某题的状态
     */
    Integer selectUserProblemStatus(@Param("userId") Long userId, @Param("problemId") Long problemId);

    /**
     * 查询题目的题解列表
     */
    List<Solution> selectSolutionsByProblemId(Long problemId);

    /**
     * 根据用户ID查询用户名
     */
    String selectUsernameById(Long userId);
}

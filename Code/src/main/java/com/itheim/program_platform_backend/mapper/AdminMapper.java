package com.itheim.program_platform_backend.mapper;

import com.itheim.program_platform_backend.domain.po.Category;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.ProblemTestCase;
import com.itheim.program_platform_backend.domain.po.Solution;
import com.itheim.program_platform_backend.domain.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminMapper {

    Integer selectUserRoleById(Long userId);

    User selectUserByUsername(String username);

    User selectUserByEmail(String email);

    long countAdminUsers(@Param("keyword") String keyword);

    List<User> selectAdminUsers(@Param("offset") int offset,
                                @Param("size") int size,
                                @Param("keyword") String keyword,
                                @Param("sortOrder") String sortOrder);

    int deleteUserTokenByUserId(Long userId);

    int deleteAdminUserById(Long userId);

    int insertUser(User user);

    Category selectCategoryByName(String name);

    long countCategoryUsage(Long categoryId);

    int insertCategory(@Param("name") String name,
                       @Param("createTime") LocalDateTime createTime,
                       @Param("updateTime") LocalDateTime updateTime);

    int deleteCategoryById(Long categoryId);

    List<Category> selectCategoriesByNames(@Param("names") List<String> names);

    long countProblemList(@Param("difficulty") String difficulty,
                          @Param("keyword") String keyword,
                          @Param("categoryNames") List<String> categoryNames);

    List<Problem> selectProblemList(@Param("offset") int offset,
                                    @Param("size") int size,
                                    @Param("difficulty") String difficulty,
                                    @Param("keyword") String keyword,
                                    @Param("categoryNames") List<String> categoryNames);

    Problem selectProblemById(Long problemId);

    List<String> selectCategoryNamesByProblemId(Long problemId);

    List<ProblemTestCase> selectProblemTestCasesByProblemId(Long problemId);

    ProblemTestCase selectProblemTestCaseById(Long caseId);

    int insertProblem(Problem problem);

    int updateProblem(Problem problem);

    int deleteProblemById(Long problemId);

    int deleteProblemCategoriesByProblemId(Long problemId);

    int insertProblemCategory(@Param("problemId") Long problemId,
                              @Param("categoryId") Long categoryId,
                              @Param("createTime") LocalDateTime createTime);

    int deleteProblemTestCasesByProblemId(Long problemId);

    int deleteSolutionsByProblemId(Long problemId);

    int deleteUserProblemsByProblemId(Long problemId);

    int deleteEvaluationsByProblemId(Long problemId);

    int deleteSubmissionsByProblemId(Long problemId);

    int insertProblemTestCase(ProblemTestCase testCase);

    int updateProblemTestCase(@Param("caseId") Long caseId,
                              @Param("inputUrl") String inputUrl,
                              @Param("outputUrl") String outputUrl,
                              @Param("updateTime") LocalDateTime updateTime);

    int deleteProblemTestCaseById(Long caseId);

    Solution selectSolutionById(Long solutionId);

    int insertSolution(Solution solution);

    int updateSolution(Solution solution);

    int deleteSolutionById(Long solutionId);
}



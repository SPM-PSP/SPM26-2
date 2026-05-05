package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.dto.AdminAddUserDTO;
import com.itheim.program_platform_backend.domain.dto.AdminProblemUpsertDTO;
import com.itheim.program_platform_backend.domain.dto.AdminSolutionCreateDTO;
import com.itheim.program_platform_backend.domain.dto.AdminSolutionUpdateDTO;
import com.itheim.program_platform_backend.domain.vo.AdminProblemDetailVO;
import com.itheim.program_platform_backend.domain.vo.AdminProblemListItemVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseAddVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseFileVO;
import com.itheim.program_platform_backend.domain.vo.AdminUserListItemVO;
import com.itheim.program_platform_backend.domain.vo.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {

    PageResult<AdminUserListItemVO> getAdminUserList(int page, int size, String keyword, String sortOrder);

    void deleteAdminUser(Long userId);

    void addAdminUser(AdminAddUserDTO dto);

    void addCategory(String name);

    void deleteCategory(Long categoryId);

    PageResult<AdminProblemListItemVO> getProblemList(int page, int size, List<String> problemCategory,
                                                     String difficulty, String keyword);

    AdminProblemDetailVO getProblemDetail(Long problemId);

    void updateProblem(AdminProblemUpsertDTO dto);

    void addProblem(AdminProblemUpsertDTO dto);

    AdminTestCaseFileVO updateProblemTestCase(Long caseId, MultipartFile input, MultipartFile output);

    AdminTestCaseAddVO addProblemTestCase(Long problemId, MultipartFile input, MultipartFile output);

    void deleteProblemTestCase(Long caseId);

    ResponseEntity<MultiValueMap<String, Object>> downloadTestCase(String inputUrl, String outputUrl);

    void deleteProblem(Long problemId);

    void updateSolution(AdminSolutionUpdateDTO dto);

    void addSolution(AdminSolutionCreateDTO dto);

    void deleteSolution(Long solutionId);
}


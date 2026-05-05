package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.dto.AdminAddUserDTO;
import com.itheim.program_platform_backend.domain.dto.AdminCategoryAddDTO;
import com.itheim.program_platform_backend.domain.dto.AdminProblemUpsertDTO;
import com.itheim.program_platform_backend.domain.dto.AdminSolutionCreateDTO;
import com.itheim.program_platform_backend.domain.dto.AdminSolutionUpdateDTO;
import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.domain.vo.AdminProblemDetailVO;
import com.itheim.program_platform_backend.domain.vo.AdminProblemListItemVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseAddVO;
import com.itheim.program_platform_backend.domain.vo.AdminTestCaseFileVO;
import com.itheim.program_platform_backend.domain.vo.AdminUserListItemVO;
import com.itheim.program_platform_backend.domain.vo.PageResult;
import com.itheim.program_platform_backend.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Api(tags = "管理员后台管理接口")
public class AdminController {

    private final AdminService adminService;

    @ApiOperation("获取管理员用户列表")
    @GetMapping("/list")
    public Result<PageResult<AdminUserListItemVO>> getAdminUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(adminService.getAdminUserList(page, size, keyword, sortOrder));
    }

    @ApiOperation("删除管理员用户")
    @DeleteMapping("/user/delete")
    public Result<Void> deleteAdminUser(@RequestParam Long userId) {
        adminService.deleteAdminUser(userId);
        return Result.success("删除用户成功");
    }

    @ApiOperation("新增管理员用户")
    @PostMapping("/user/add")
    public Result<Void> addAdminUser(@RequestBody AdminAddUserDTO dto) {
        adminService.addAdminUser(dto);
        return Result.success("添加管理员成功");
    }

    @ApiOperation("新增题目类别")
    @PostMapping("/category/add")
    public Result<Void> addCategory(@RequestBody AdminCategoryAddDTO dto) {
        adminService.addCategory(dto.getName());
        return Result.success("题目类别添加成功");
    }

    @ApiOperation("删除题目类别")
    @DeleteMapping("/category/delete")
    public Result<Void> deleteCategory(@RequestParam Long categoryId) {
        adminService.deleteCategory(categoryId);
        return Result.success("删除题目类别成功");
    }

    @ApiOperation("获取题库题目列表")
    @GetMapping("/problem/list")
    public Result<PageResult<AdminProblemListItemVO>> getProblemList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> problemCategory,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getProblemList(page, size, problemCategory, difficulty, keyword));
    }

    @ApiOperation("获取题目详情")
    @GetMapping("/problem/detail/{problemId}")
    public Result<AdminProblemDetailVO> getProblemDetail(@PathVariable Long problemId) {
        return Result.success(adminService.getProblemDetail(problemId));
    }

    @ApiOperation("更改题目")
    @PutMapping("/problem")
    public Result<Void> updateProblem(@RequestBody AdminProblemUpsertDTO dto) {
        adminService.updateProblem(dto);
        return Result.success("更新题目成功");
    }

    @ApiOperation("新增题目")
    @PostMapping("/problem")
    public Result<Void> addProblem(@RequestBody AdminProblemUpsertDTO dto) {
        adminService.addProblem(dto);
        return Result.success("新增题目成功");
    }

    @ApiOperation("更改题目测试用例")
    @PostMapping(value = "/problem/case", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AdminTestCaseFileVO> updateProblemCase(@RequestParam Long caseId,
                                                         @RequestParam MultipartFile input,
                                                         @RequestParam MultipartFile output) {
        return Result.success(adminService.updateProblemTestCase(caseId, input, output));
    }

    @ApiOperation("新增题目测试用例")
    @PostMapping(value = "/problem/case/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AdminTestCaseAddVO> addProblemCase(@RequestParam Long problemId,
                                                     @RequestParam MultipartFile input,
                                                     @RequestParam MultipartFile output) {
        return Result.success(adminService.addProblemTestCase(problemId, input, output));
    }

    @ApiOperation("删除题目测试用例")
    @DeleteMapping("/problem/case")
    public Result<Void> deleteProblemCase(@RequestParam Long caseId) {
        adminService.deleteProblemTestCase(caseId);
        return Result.success("删除测试用例成功");
    }

    @ApiOperation("获取题目测试用例")
    @GetMapping("/problem/case")
    public ResponseEntity<MultiValueMap<String, Object>> downloadProblemCase(
            @RequestParam String inputUrl,
            @RequestParam String outputUrl) {
        return adminService.downloadTestCase(inputUrl, outputUrl);
    }

    @ApiOperation("删除题目")
    @DeleteMapping("/problem")
    public Result<Void> deleteProblem(@RequestParam Long problemId) {
        adminService.deleteProblem(problemId);
        return Result.success("删除题目成功");
    }

    @ApiOperation("更改题目题解")
    @PutMapping("/solution")
    public Result<Void> updateSolution(@RequestBody AdminSolutionUpdateDTO dto) {
        adminService.updateSolution(dto);
        return Result.success("题解修改成功");
    }

    @ApiOperation("新增题目题解")
    @PostMapping("/solution")
    public Result<Void> addSolution(@RequestBody AdminSolutionCreateDTO dto) {
        adminService.addSolution(dto);
        return Result.success("新增题解成功");
    }

    @ApiOperation("删除题目题解")
    @DeleteMapping("/solution")
    public Result<Void> deleteSolution(@RequestParam Long solutionId) {
        adminService.deleteSolution(solutionId);
        return Result.success("删除题目题解成功");
    }
}


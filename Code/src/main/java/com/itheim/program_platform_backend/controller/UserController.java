package com.itheim.program_platform_backend.controller;


import com.itheim.program_platform_backend.domain.dto.ChangePasswordDTO;
import com.itheim.program_platform_backend.domain.dto.SubmissionQueryDTO;
import com.itheim.program_platform_backend.domain.dto.UpdateUserDTO;
import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.domain.vo.*;
import com.itheim.program_platform_backend.service.AuthService;
import com.itheim.program_platform_backend.service.SubmissionService;
import com.itheim.program_platform_backend.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@Api(tags = "用户信息接口")
public class UserController {

    @Autowired
    private AuthService authService;
    @Autowired
    private SubmissionService submissionService;
    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        UserInfoVO userInfo = authService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @ApiOperation("修改用户信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UpdateUserDTO updateUserDTO) {
        Long userId = UserContext.getCurrentUserId();
        authService.updateUserInfo(userId, updateUserDTO);
        return Result.success();
    }

    @ApiOperation("上传头像")
    @PostMapping("/avatar/upload")
    public Result<AvatarUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getCurrentUserId();
        AvatarUploadVO result = authService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", result);
    }

    @ApiOperation("修改用户密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        Long userId = UserContext.getCurrentUserId();
        authService.changePassword(userId, changePasswordDTO);
        return Result.success();
    }



    @ApiOperation("获取用户历史提交记录列表")
    @GetMapping("/submission/list")
    public Result getSubmissionList(SubmissionQueryDTO queryDTO) {
        Long userId = UserContext.getCurrentUserId();
        PageResultVO<SubmissionListItemVO> result = submissionService.getSubmissionList(userId, queryDTO);
        return Result.success(result);
    }

    @ApiOperation("获取用户历史提交记录详情")
    @GetMapping("/detail/{submissionId}")
    public Result getSubmissionDetail(@PathVariable Long submissionId) {
        Long userId = UserContext.getCurrentUserId();
        SubmissionDetailVO detail = submissionService.getSubmissionDetail(userId, submissionId);
        return Result.success(detail);
    }

    @ApiOperation("获取用户提交记录及进度")
    @GetMapping("/study/statistics")
    public Result<StudyStatisticsVO> getStudyStatistics() {
        Long userId = UserContext.getCurrentUserId();
        StudyStatisticsVO statistics = authService.getStudyStatistics(userId);
        return Result.success(statistics);
    }



}

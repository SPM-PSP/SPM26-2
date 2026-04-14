package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.dto.RegisterUserDTO;
import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.domain.vo.LoginVO;
import com.itheim.program_platform_backend.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Api(tags = "用户认证接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody RegisterUserDTO userDTO) {
        LoginVO login = authService.login(userDTO);
        return Result.success("登录成功",login);

    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterUserDTO userDTO) {
        authService.addUser(userDTO);
        return Result.success();
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result logout() {
        //销毁token
        return Result.success("退出登录成功");
    }
}

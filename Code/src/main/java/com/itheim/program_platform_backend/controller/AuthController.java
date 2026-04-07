package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.po.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Api(tags = "用户认证接口")
public class AuthController {

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result login() {
        // ...
        return Result.success();
    }
}

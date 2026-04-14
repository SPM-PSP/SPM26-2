package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.dto.RegisterUserDTO;
import com.itheim.program_platform_backend.domain.vo.LoginVO;

public interface AuthService {
    /*
    *  用户注册
    * */
    void addUser(RegisterUserDTO userDTO);

    /*
    *  用户登录
    * */
    LoginVO login(RegisterUserDTO userDTO);

}

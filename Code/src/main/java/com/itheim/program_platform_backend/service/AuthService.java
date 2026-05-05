package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.dto.ChangePasswordDTO;
import com.itheim.program_platform_backend.domain.dto.RegisterUserDTO;
import com.itheim.program_platform_backend.domain.dto.UpdateUserDTO;
import com.itheim.program_platform_backend.domain.vo.AvatarUploadVO;
import com.itheim.program_platform_backend.domain.vo.LoginVO;
import com.itheim.program_platform_backend.domain.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    /*
    *  用户注册
    * */
    void addUser(RegisterUserDTO userDTO);

    /*
    *  用户登录
    * */
    LoginVO login(RegisterUserDTO userDTO);

    UserInfoVO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserDTO updateUserDTO);

    AvatarUploadVO uploadAvatar(Long userId, MultipartFile file);

    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}

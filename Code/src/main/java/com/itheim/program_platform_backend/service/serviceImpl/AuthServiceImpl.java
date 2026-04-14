package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.domain.dto.RegisterUserDTO;
import com.itheim.program_platform_backend.domain.po.User;
import com.itheim.program_platform_backend.domain.po.UserToken;
import com.itheim.program_platform_backend.domain.vo.LoginVO;
import com.itheim.program_platform_backend.enums.CommonResultCode;
import com.itheim.program_platform_backend.exception.BusinessException;
import com.itheim.program_platform_backend.mapper.AuthMapper;
import com.itheim.program_platform_backend.service.AuthService;
import com.itheim.program_platform_backend.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthMapper authMapper;
    @Autowired
    private JwtUtil jwtUtil;

    // BCrypt密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /*
    *  用户注册
    * */
    @Override
    public void addUser(RegisterUserDTO userDTO) {
        if (userDTO == null){
            throw new BusinessException(CommonResultCode.PARAM_ERROR,"用户信息不能为空");
        }
        // 检查用户名是否已存在
        User existingUser = authMapper.findByUsername(userDTO.getUsername());
        if (existingUser != null) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "该用户名已被注册，请更换");
        }
        log.info("用户注册：{}", userDTO);
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        
        // BCrypt加密密码
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        user.setPassword(encodedPassword);
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        authMapper.addUser(user);
    }


    /*
    *  用户登录
    * */
    @Override
    public LoginVO login(RegisterUserDTO userDTO) {
        if (userDTO == null){
            throw new BusinessException(CommonResultCode.PARAM_ERROR,"用户信息不能为空");
        }
        log.info("用户登录：{}", userDTO);

        // 1. 查询用户是否存在
        User user = authMapper.findByUsername(userDTO.getUsername());
        if (user == null) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "用户名或密码错误");
        }

        // 3. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole() == 1 ? "admin" : "user");

        String token = jwtUtil.generateToken(claims);

        // 4. 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(jwtUtil.getExpiration() / 1000);

        // 5. 删除该用户的旧 Token，保存新 Token（实现覆盖效果）
        authMapper.deleteUserTokenByUserId(user.getId());

        UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setToken(token);
        userToken.setExpireTime(expireTime);
        userToken.setCreateTime(LocalDateTime.now());
        userToken.setUpdateTime(LocalDateTime.now());
        authMapper.saveUserToken(userToken);
        // 5. 构建返回结果
        return LoginVO.builder()
                .userId(user.getId())
                .role(user.getRole() == 1 ? "admin" : "user")
                .nickname(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .token(token)
                .expireTime(expireTime.format(FORMATTER))
                .build();

    }

}

package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.domain.dto.ChangePasswordDTO;
import com.itheim.program_platform_backend.domain.dto.RegisterUserDTO;
import com.itheim.program_platform_backend.domain.dto.UpdateUserDTO;
import com.itheim.program_platform_backend.domain.po.User;
import com.itheim.program_platform_backend.domain.po.UserStatistics;
import com.itheim.program_platform_backend.domain.po.UserToken;
import com.itheim.program_platform_backend.domain.vo.AvatarUploadVO;
import com.itheim.program_platform_backend.domain.vo.LoginVO;
import com.itheim.program_platform_backend.domain.vo.StudyStatisticsVO;
import com.itheim.program_platform_backend.domain.vo.UserInfoVO;
import com.itheim.program_platform_backend.enums.CommonResultCode;
import com.itheim.program_platform_backend.exception.BusinessException;
import com.itheim.program_platform_backend.mapper.AuthMapper;
import com.itheim.program_platform_backend.mapper.UserStatisticsMapper;
import com.itheim.program_platform_backend.service.AuthService;
import com.itheim.program_platform_backend.utils.AliyunOSSOperator;
import com.itheim.program_platform_backend.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private UserStatisticsMapper userStatisticsMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;


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
                .avatar(user.getAvatar())
                .expireTime(expireTime.format(FORMATTER))
                .build();

    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        User user = authMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "用户不存在");
        }

        return UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserDTO updateUserDTO) {
        if (userId == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        if (updateUserDTO == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "更新信息不能为空");
        }

        // 检查用户是否存在
        User user = authMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "用户不存在");
        }

        // 只更新非空字段
        String nickname = updateUserDTO.getNickname() != null ? updateUserDTO.getNickname() : user.getNickname();
        String phone = updateUserDTO.getPhone() != null ? updateUserDTO.getPhone() : user.getPhone();

        authMapper.updateUserById(userId, nickname, phone);
        log.info("用户信息更新成功，用户ID: {}", userId);
    }

    @Override
    public AvatarUploadVO uploadAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "上传文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "仅支持jpg/jpeg/png/webp格式的头像文件");
        }

        // 验证文件大小(限制10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "头像文件大小不能超过10MB");
        }

        try {
            // 上传到阿里云OSS
            String avatarUrl = aliyunOSSOperator.upload(file.getBytes(), originalFilename);

            // 更新数据库中的头像URL
            authMapper.updateAvatarById(userId, avatarUrl);

            // 获取文件类型
            String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            log.info("用户头像上传成功，用户ID: {}, URL: {}", userId, avatarUrl);

            return AvatarUploadVO.builder()
                    .avatarUrl(avatarUrl)
                    .avatarSize(file.getSize())
                    .avatarType(fileType)
                    .build();
        } catch (Exception e) {
            log.error("头像上传失败，用户ID: {}", userId, e);
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "头像上传失败: " + e.getMessage());
        }
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        if (userId == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        if (changePasswordDTO == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "请求参数不能为空");
        }

        // 验证新密码和确认密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "新密码和确认密码不一致");
        }

        // 查询用户信息
        User user = authMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "用户不存在");
        }

        // 验证原密码是否正确
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "原密码错误");
        }

        // 验证新密码不能与原密码相同
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException(CommonResultCode.BUSINESS_ERROR, "新密码不能与原密码相同");
        }

        // 加密新密码并更新
        String encodedNewPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        authMapper.updatePasswordById(userId, encodedNewPassword);

        log.info("用户密码修改成功，用户ID: {}", userId);
    }

    @Override
    public StudyStatisticsVO getStudyStatistics(Long userId) {
        if (userId == null) {
            throw new BusinessException(CommonResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 查询用户统计信息
        UserStatistics userStatistics = userStatisticsMapper.selectByUserId(userId);
        
        // 如果用户统计记录不存在，创建默认记录
        if (userStatistics == null) {
            userStatistics = new UserStatistics();
            userStatistics.setUserId(userId);
            userStatistics.setTotalSubmit(0);
            userStatistics.setTotalAccept(0);
            userStatistics.setCreateTime(LocalDateTime.now());
            userStatistics.setUpdateTime(LocalDateTime.now());
            
            userStatisticsMapper.insert(userStatistics);
        }

        return StudyStatisticsVO.builder()
                .totalSubmit(userStatistics.getTotalSubmit())
                .totalAccept(userStatistics.getTotalAccept())
                .build();
    }


    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".jpg") ||
                lowerCase.endsWith(".jpeg") ||
                lowerCase.endsWith(".png") ||
                lowerCase.endsWith(".webp");
    }



}

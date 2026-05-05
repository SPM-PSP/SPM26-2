package com.itheim.program_platform_backend.domain.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class UpdateUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

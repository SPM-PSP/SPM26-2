package com.itheim.program_platform_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAddUserDTO implements Serializable {
    private String username;
    private String password;
    private String email;
    private String nickname;
}


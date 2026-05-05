package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvatarUploadVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String avatarUrl;
    private Long avatarSize;
    private String avatarType;
}

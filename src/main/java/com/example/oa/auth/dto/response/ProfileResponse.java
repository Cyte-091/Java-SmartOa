package com.example.oa.auth.dto.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private Long id;

    private String username;

    private String displayName;

    private Integer status;
}

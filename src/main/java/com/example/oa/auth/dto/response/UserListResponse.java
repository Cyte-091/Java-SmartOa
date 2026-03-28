package com.example.oa.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserListResponse {

    private Long id;

    private String username;

    private Integer status;

    private String displayName;

    private LocalDateTime createdAt;
}

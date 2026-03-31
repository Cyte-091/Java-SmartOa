package com.example.oa.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDetailsResponse {

    private Long id;

    private String username;

    private Integer status;

    private String displayName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<String> roleCodes;
}

package com.xrdj.iris.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String password; // Optional
    private String role;
}

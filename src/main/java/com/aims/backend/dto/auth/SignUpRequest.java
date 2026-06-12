package com.aims.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String name;
    private String email;
    private String password;
    private int workExperience;
}

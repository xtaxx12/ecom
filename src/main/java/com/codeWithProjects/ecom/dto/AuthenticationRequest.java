package com.codeWithProjects.ecom.dto;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class AuthenticationRequest {
    private String username;
    private String password;



}

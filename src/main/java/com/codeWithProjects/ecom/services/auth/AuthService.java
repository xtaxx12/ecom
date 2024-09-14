package com.codeWithProjects.ecom.services.auth;

import com.codeWithProjects.ecom.dto.SignupRequest;
import com.codeWithProjects.ecom.dto.UserDto;
import com.codeWithProjects.ecom.entity.PasswordResetToken;
import com.codeWithProjects.ecom.entity.User;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.Optional;

public interface AuthService {
    UserDto createUser(SignupRequest signupRequest);

    Boolean hasUserWithEmail (String email);

    void sendRecoveryEmail(String email) throws MessagingException, IOException;

    void resetPassword(String token, String newPassword);

    boolean validatePasswordResetToken(String token);

}


package com.codeWithProjects.ecom.services.jwt;

import com.codeWithProjects.ecom.entity.PasswordResetToken;
import com.codeWithProjects.ecom.entity.User;
import com.codeWithProjects.ecom.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public PasswordResetToken createToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(30)); // Token válido por 30 minutos
        return tokenRepository.save(token);
    }

    public PasswordResetToken findByToken(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }

    public boolean isTokenExpired(PasswordResetToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }
}
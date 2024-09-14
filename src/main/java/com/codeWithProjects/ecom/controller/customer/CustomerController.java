package com.codeWithProjects.ecom.controller.customer;

import com.codeWithProjects.ecom.dto.UserDto;
import com.codeWithProjects.ecom.services.auth.AuthService;
import com.codeWithProjects.ecom.services.user.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final AuthService authService;

    private final UserService userService; // Suponiendo que tienes un servicio de usuario

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserDto> getProfile(@PathVariable Long userId) {
        UserDto userDto = userService.getUserById(userId);
        if (userDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserDto> updateProfile(@PathVariable Long userId, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(userId, userDto);
        if (updatedUser == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(updatedUser);
    }



}

package com.codeWithProjects.ecom.services.user;

import com.codeWithProjects.ecom.dto.UserDto;
import com.codeWithProjects.ecom.entity.User;
import com.codeWithProjects.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::convertToDto)
                .orElse(null);
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setName(userDto.getName());
                    user.setEmail(userDto.getEmail());
                    userRepository.save(user);
                    return convertToDto(user);
                }).orElse(null);
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        userDto.setUserRole(user.getRole());
        return userDto;
    }
}
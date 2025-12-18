package com.example.couriermanagement.dto;

import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String login;
    private String name;
    private UserRole role;
    private LocalDateTime createdAt;

    //todo в мапперы
    public static UserDto from(User user) {
        return UserDto.builder()
            .id(user.getId())
            .login(user.getLogin())
            .name(user.getName())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
    }
}

package com.fitness.userservice.dto;

import com.fitness.userservice.models.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Data
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


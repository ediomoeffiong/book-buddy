package com.bookbuddy.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Email or username is required")
    @JsonAlias({"email", "usernameOrEmail", "username"})
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    // Keep legacy getter for existing code that calls getEmail()
    public String getEmail() {
        return this.usernameOrEmail;
    }
}


package com.bookbuddy.service;

import com.bookbuddy.config.MaintenanceConfig;
import com.bookbuddy.dto.AuthRequest;
import com.bookbuddy.dto.AuthResponse;
import com.bookbuddy.dto.RegisterRequest;
import com.bookbuddy.dto.UserDTO;
import com.bookbuddy.exception.DuplicateResourceException;
import com.bookbuddy.exception.MaintenanceModeException;
import com.bookbuddy.exception.ResourceNotFoundException;
import com.bookbuddy.model.User;
import com.bookbuddy.repository.UserRepository;
import com.bookbuddy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final MaintenanceConfig maintenanceConfig;
    
    public AuthResponse register(RegisterRequest request) {
        // Validate password and confirmPassword match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        // Generate username from email (before @ symbol)
        String username = request.getEmail().split("@")[0];

        // If username exists, append a number to make it unique
        String finalUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + counter;
            counter++;
        }

        // Parse fullName into firstName and lastName
        String firstName = request.getFullName();
        String lastName = "";

        int lastSpaceIndex = request.getFullName().lastIndexOf(' ');
        if (lastSpaceIndex > 0) {
            firstName = request.getFullName().substring(0, lastSpaceIndex).trim();
            lastName = request.getFullName().substring(lastSpaceIndex + 1).trim();
        }

        // Create new user
        User user = User.builder()
                .username(finalUsername)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(firstName)
                .lastName(lastName)
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(firstName)
                .build();
    }
    
    public AuthResponse login(AuthRequest request) {
        // Check if system is under maintenance
        if (maintenanceConfig.isEnabled()) {
            throw new MaintenanceModeException(maintenanceConfig.getMessage());
        }

        // Get the usernameOrEmail from the request
        String usernameOrEmail = request.getUsernameOrEmail();

        try {
            // Authenticate user (supports both username and email)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, request.getPassword())
            );
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", usernameOrEmail);
            throw new IllegalArgumentException("Invalid email/username or password");
        }

        // Load user details (supports both username and email)
        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
        User user = userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .build();
    }

    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}


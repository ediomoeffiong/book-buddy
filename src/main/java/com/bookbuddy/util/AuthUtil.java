package com.bookbuddy.util;

import com.bookbuddy.exception.UnauthorizedException;
import com.bookbuddy.model.User;
import com.bookbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    
    private final UserRepository userRepository;
    
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        return user.getId();
    }
    
    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}


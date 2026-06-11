package com.aims.backend.service;

import com.aims.backend.config.jwt.TokenProvider;
import com.aims.backend.domain.user.User;
import com.aims.backend.domain.user.UserRole;
import com.aims.backend.dto.auth.LoginRequest;
import com.aims.backend.dto.auth.SignUpRequest;
import com.aims.backend.dto.auth.TokenResponse;
import com.aims.backend.exception.GeneralException;
import com.aims.backend.repository.UserRepository;
import com.aims.backend.common.status.ErrorStatus; // Import ErrorStatus

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import BCryptPasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // Added for empNo generation logic

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository; // Injected UserRepository
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // Injected BCryptPasswordEncoder

    public TokenResponse.TokenDTO login(LoginRequest.LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        return tokenProvider.generateTokens(authentication);
    }

    public User signUp(SignUpRequest request) {
        // 1. Check for existing email
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new GeneralException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        });

        // 2. Generate empNo
        Long lastEmpNo = userRepository.findTopByOrderByEmpNoDesc()
                .map(User::getEmpNo)
                .orElse(20260000L); // Default if no users exist, as per requirement "현재 20260005까지 저장되어 있는 상태야"

        Long newEmpNo = lastEmpNo + 1;

        // 3. Encode password
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        // 4. Create new User object
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(UserRole.valueOf(request.getRole().toUpperCase())) // Convert String role to UserRole enum
                .empNo(newEmpNo)
                .build();

        // 5. Save the new user
        return userRepository.save(newUser);
    }
}

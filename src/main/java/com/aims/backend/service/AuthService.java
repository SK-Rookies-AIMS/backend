package com.aims.backend.service;

import com.aims.backend.config.jwt.TokenProvider;
import com.aims.backend.domain.user.User;
import com.aims.backend.domain.user.UserRole;
import com.aims.backend.dto.auth.LoginRequest;
import com.aims.backend.dto.auth.RefreshRequest;
import com.aims.backend.dto.auth.SignUpRequest;
import com.aims.backend.dto.auth.TokenResponse;
import com.aims.backend.exception.GeneralException;
import com.aims.backend.repository.UserRepository;
import com.aims.backend.common.status.ErrorStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public TokenResponse.TokenDTO login(LoginRequest.LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        Optional<User> userOptional = userRepository.findByEmail(loginDTO.getEmail());
        User user = userOptional.get();

        if (user.getRole() == UserRole.JUNIOR) {
            LocalDateTime createdAt = user.getCreatedAt();
            if (createdAt != null) {
                int yearsSinceRegistration = Period.between(createdAt.toLocalDate(), LocalDate.now()).getYears();
                int currentTotalWorkExperience = user.getWorkExperience() + yearsSinceRegistration;

                if (currentTotalWorkExperience >= 6) {
                    user.setRole(UserRole.SENIOR);
                    userRepository.save(user);
                }
            }
        }
        
        return tokenProvider.generateTokens(authentication);
    }

    public TokenResponse.TokenDTO refresh(RefreshRequest.RefreshDTO refreshDTO) {
        TokenResponse.TokenDTO tokenDTO = tokenProvider.refreshAccessToken(refreshDTO.getRefreshToken());
        if (tokenDTO == null) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }
        return tokenDTO;
    }

    public User signUp(SignUpRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new GeneralException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        });

        Long lastEmpNo = userRepository.findTopByOrderByEmpNoDesc()
                .map(User::getEmpNo)
                .orElse(20260000L);

        Long newEmpNo = lastEmpNo + 1;

        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        UserRole determinedRole;
        if (request.getWorkExperience() >= 6) {
            determinedRole = UserRole.SENIOR;
        } else {
            determinedRole = UserRole.JUNIOR;
        }

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .workExperience(request.getWorkExperience())
                .role(determinedRole)
                .empNo(newEmpNo)
                .build();

        return userRepository.save(newUser);
    }
}

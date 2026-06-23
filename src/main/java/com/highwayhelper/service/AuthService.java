package com.highwayhelper.service;

import com.highwayhelper.dto.request.LoginRequest;
import com.highwayhelper.dto.request.RegisterRequest;
import com.highwayhelper.dto.response.AuthResponse;
import com.highwayhelper.dto.response.UserResponse;
import com.highwayhelper.entity.User;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.exception.BadRequestException;
import com.highwayhelper.exception.ResourceNotFoundException;
import com.highwayhelper.mapper.UserMapper;
import com.highwayhelper.repository.UserRepository;
import com.highwayhelper.security.JwtTokenProvider;
import com.highwayhelper.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        if (role == Role.ADMIN) {
            throw new BadRequestException("Admin registration is not allowed via public API");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User saved = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(
                saved.getId(), saved.getEmail(), saved.getPassword(), saved.getRole().name());

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .tokenType("Bearer")
                .user(userMapper.toResponse(saved))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }
}

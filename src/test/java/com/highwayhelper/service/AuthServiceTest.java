package com.highwayhelper.service;

import com.highwayhelper.dto.request.LoginRequest;
import com.highwayhelper.dto.request.RegisterRequest;
import com.highwayhelper.dto.response.AuthResponse;
import com.highwayhelper.entity.User;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.exception.BadRequestException;
import com.highwayhelper.mapper.UserMapper;
import com.highwayhelper.repository.UserRepository;
import com.highwayhelper.security.JwtTokenProvider;
import com.highwayhelper.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("9876543210")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("9876543210")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(new User());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(
                com.highwayhelper.dto.response.UserResponse.builder().id(1L).email("john@example.com").build());
        when(jwtTokenProvider.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .phoneNumber("9876543210")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is already registered");
    }

    @Test
    void login_shouldReturnTokenOnSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        UserPrincipal principal = new UserPrincipal(1L, "john@example.com", "encoded", "USER");
        User user = User.builder().id(1L).email("john@example.com").role(Role.USER).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(
                com.highwayhelper.dto.response.UserResponse.builder().id(1L).build());
        when(jwtTokenProvider.generateToken(principal)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
    }
}

package com.banking.service;

import com.banking.dto.AuthDto;
import com.banking.exception.CustomException;
import com.banking.model.User;
import com.banking.repository.UserRepository;
import com.banking.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new CustomException.DuplicateAccountException(
                    "Username already taken: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new CustomException.DuplicateAccountException(
                    "Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("Registered user: {}", user.getUsername());

        String token = jwtService.generateToken(toPrincipal(user));
        return new AuthDto.AuthResponse(token, "Bearer", user.getUsername());
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
        String token = jwtService.generateToken(toPrincipal(user));
        log.info("User logged in: {}", user.getUsername());
        return new AuthDto.AuthResponse(token, "Bearer", user.getUsername());
    }

    private org.springframework.security.core.userdetails.User toPrincipal(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name())));
    }
}
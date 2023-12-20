package com.gmonitor.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmonitor.auth.models.AuthenticationResponse;
import com.gmonitor.auth.models.ChangePasswordView;
import com.gmonitor.model.configuration.User;
import com.gmonitor.service.JwtService;
import com.gmonitor.storage.entity.configuration.TokenType;
import com.gmonitor.storage.entity.configuration.UserEntity;
import com.gmonitor.storage.entity.configuration.UserTokenEntity;
import com.gmonitor.storage.repository.TokenRepository;
import com.gmonitor.storage.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConversionService conversionService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    public static final String BEARER = "Bearer";

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 ConversionService conversionService, JwtService jwtService,
                                 AuthenticationManager authenticationManager, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.conversionService = conversionService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void changePassword(ChangePasswordView request) {

        Optional<UserEntity> user = userRepository.findByLogin(request.getLogin());
        if (user.isPresent() && passwordEncoder.matches(request.getOldPassword(), user.get().getPassword())) {
            user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.get().setActivated(true);
            userRepository.save(user.get());
            revokeAllUserTokens(user.get());
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        UserEntity userEntity = userRepository.findByLogin(request.getEmail()).orElseThrow();

        if (!userEntity.isActivated()) {
            return AuthenticationResponse.builder()
                    .requiredToChangePassword(true)
                    .build();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = conversionService.convert(userEntity, User.class);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(userEntity);
        saveUserToken(userEntity, jwtToken, TokenType.BEARER);
        return AuthenticationResponse.builder()
                .requiredToChangePassword(false)
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeAllUserTokens(UserEntity user) {
        List<UserTokenEntity> validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(UserEntity user, String jwtToken, TokenType tokenType) {
        var userToken = UserTokenEntity.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(userToken);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String login;
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return;
        }
        refreshToken = authHeader.split(BEARER)[1].trim();
        login = jwtService.extractUserLogin(refreshToken);
        if (login != null) {
            UserEntity userEntity = this.userRepository.findByLogin(login).orElseThrow();
            User userDetails = conversionService.convert(userEntity, User.class);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                var accessToken = jwtService.generateToken(userDetails);
                revokeAllUserTokens(userEntity);
                saveUserToken(userEntity, accessToken, TokenType.BEARER);
                var authResponse = AuthenticationResponse.builder()
                        .refreshToken(refreshToken)
                        .accessToken(accessToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}

package com.gmonitor.auth;

import com.gmonitor.service.JwtService;
import com.gmonitor.storage.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    public WebSocketAuthInterceptor(JwtService jwtService, UserDetailsService userDetailsService,
                                    TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        final var cmd = accessor.getCommand();
        String jwt = null;
        try {
            if (StompCommand.CONNECT == cmd) {
                final var requestTokenHeader = accessor.getFirstNativeHeader("Authorization");
                if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
                    jwt = requestTokenHeader.substring(7);
                }
                String login = jwtService.extractUserLogin(jwt);
                if (login != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(login);
                    boolean isTokenValid = tokenRepository.findByToken(jwt).map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
                    if (isTokenValid && jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } else {
                    throw new RuntimeException("Auth data isn't resolved");
                }
            }
            return message;
        } catch (Exception e) {
            String errorMessage = format("Security exception: %s", e.getMessage());
            log.error(errorMessage, e);
            throw new AuthenticationCredentialsNotFoundException(errorMessage);
        }
    }

}

package com.likelion.medicast.auth.security;

import com.likelion.medicast.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RequestUserIdResolver {

    static final String DEMO_USER_EMAIL = "public-demo@medicast.invalid";

    private final UserRepository userRepository;
    private final boolean authEnabled;

    public RequestUserIdResolver(
            UserRepository userRepository,
            @Value("${app.auth.enabled:true}") boolean authEnabled
    ) {
        this.userRepository = userRepository;
        this.authEnabled = authEnabled;
    }

    public Long resolve(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser != null) {
            return authenticatedUser.id();
        }
        if (authEnabled) {
            throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
        }

        return userRepository.findByEmail(DEMO_USER_EMAIL)
                .orElseThrow(() -> new IllegalStateException("공용 데모 사용자가 초기화되지 않았습니다."))
                .getId();
    }
}

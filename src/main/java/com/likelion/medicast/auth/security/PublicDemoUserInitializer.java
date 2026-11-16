package com.likelion.medicast.auth.security;

import com.likelion.medicast.user.entity.User;
import com.likelion.medicast.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "false")
public class PublicDemoUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(RequestUserIdResolver.DEMO_USER_EMAIL)) {
            return;
        }
        userRepository.save(new User(
                RequestUserIdResolver.DEMO_USER_EMAIL,
                "AUTHENTICATION_DISABLED",
                "공용 데모 사용자"
        ));
    }
}

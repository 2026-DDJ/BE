package com.likelion.medicast.auth.security;

import com.likelion.medicast.user.entity.User;

public record AuthenticatedUser(
        Long id,
        String email,
        String name
) {

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getName());
    }
}

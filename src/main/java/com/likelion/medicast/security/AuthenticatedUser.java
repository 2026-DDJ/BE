package com.likelion.medicast.security;

import com.likelion.medicast.entity.User;

public record AuthenticatedUser(
        Long id,
        String email,
        String name
) {

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getName());
    }
}

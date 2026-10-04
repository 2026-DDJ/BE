package com.likelion.medicast.repository;

import com.likelion.medicast.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

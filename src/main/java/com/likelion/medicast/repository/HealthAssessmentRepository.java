package com.likelion.medicast.repository;

import com.likelion.medicast.entity.HealthAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, Long> {
}

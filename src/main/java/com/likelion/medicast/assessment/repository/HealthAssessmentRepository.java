package com.likelion.medicast.assessment.repository;

import com.likelion.medicast.assessment.entity.HealthAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, Long> {

    Optional<HealthAssessment> findByIdAndUser_Id(Long id, Long userId);
}

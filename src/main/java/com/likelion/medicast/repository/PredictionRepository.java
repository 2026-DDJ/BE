package com.likelion.medicast.repository;

import com.likelion.medicast.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    Optional<Prediction> findByIdAndAssessment_User_Id(Long id, Long userId);
}

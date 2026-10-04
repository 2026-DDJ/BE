package com.likelion.medicast.repository;

import com.likelion.medicast.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

}

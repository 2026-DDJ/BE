package com.likelion.medicast.assessment.service;

import com.likelion.medicast.assessment.dto.HealthAssessmentInput;
import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.repository.HealthAssessmentRepository;
import com.likelion.medicast.global.exception.ResourceNotFoundException;
import com.likelion.medicast.user.entity.User;
import com.likelion.medicast.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class HealthAssessmentService {

    private final UserRepository userRepository;
    private final HealthAssessmentRepository healthAssessmentRepository;
    private final BodyMassIndexCalculator bodyMassIndexCalculator;

    public HealthAssessment create(Long userId, HealthAssessmentInput input) {
        User user = userRepository.findById(userId)
                .orElseThrow(ResourceNotFoundException::new);
        BigDecimal bmi = bodyMassIndexCalculator.calculate(input.heightCm(), input.weightKg());

        return healthAssessmentRepository.save(new HealthAssessment(
                user,
                input.householdSize(),
                input.incomeRange(),
                input.healthInsuranceType(),
                input.region(),
                input.selfRatedHealth(),
                input.heightCm(),
                input.weightKg(),
                bmi,
                input.exercises(),
                input.outpatientVisitsLastYear(),
                input.dentalVisitsLastYear(),
                input.hasPrivateHealthInsurance(),
                input.hospitalizedLastYear(),
                input.activityLimitation(),
                input.anxietyDepression(),
                input.occupation()
        ));
    }
}

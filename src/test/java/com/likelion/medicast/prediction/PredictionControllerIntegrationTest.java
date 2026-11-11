package com.likelion.medicast.prediction;

import com.likelion.medicast.entity.DrinkingFrequency;
import com.likelion.medicast.entity.ExerciseFrequency;
import com.likelion.medicast.entity.HealthAssessment;
import com.likelion.medicast.entity.IncomeRange;
import com.likelion.medicast.entity.SelfRatedHealth;
import com.likelion.medicast.entity.Sex;
import com.likelion.medicast.entity.SmokingStatus;
import com.likelion.medicast.entity.User;
import com.likelion.medicast.repository.HealthAssessmentRepository;
import com.likelion.medicast.repository.PredictionRepository;
import com.likelion.medicast.repository.UserRepository;
import com.likelion.medicast.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PredictionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HealthAssessmentRepository healthAssessmentRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User owner;
    private User otherUser;
    private HealthAssessment assessment;

    @BeforeEach
    void setUp() {
        cleanUp();

        owner = userRepository.saveAndFlush(new User("owner@example.com", "hash", "소유자"));
        otherUser = userRepository.saveAndFlush(new User("other@example.com", "hash", "다른 사용자"));
        assessment = healthAssessmentRepository.saveAndFlush(assessmentOf(owner));
    }

    @AfterEach
    void cleanUp() {
        predictionRepository.deleteAll();
        healthAssessmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsAndGetsPredictionForOwnedAssessment() throws Exception {
        String token = jwtTokenProvider.createAccessToken(owner.getId());

        String response = mockMvc.perform(post("/assessments/{assessmentId}/predictions", assessment.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assessmentId").value(assessment.getId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.predictedAnnualMedicalCost").isNumber())
                .andExpect(jsonPath("$.currency").value("KRW"))
                .andExpect(jsonPath("$.modelVersion").value("deterministic-fake-v1"))
                .andExpect(jsonPath("$.completedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long predictionId = ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/predictions/{predictionId}", predictionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(predictionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void hidesOtherUsersAssessmentAndPrediction() throws Exception {
        String ownerToken = jwtTokenProvider.createAccessToken(owner.getId());
        String otherToken = jwtTokenProvider.createAccessToken(otherUser.getId());

        mockMvc.perform(post("/assessments/{assessmentId}/predictions", assessment.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        String response = mockMvc.perform(post("/assessments/{assessmentId}/predictions", assessment.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long predictionId = ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();

        mockMvc.perform(get("/predictions/{predictionId}", predictionId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void rejectsPredictionRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/assessments/{assessmentId}/predictions", assessment.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private HealthAssessment assessmentOf(User user) {
        return new HealthAssessment(
                user,
                45,
                Sex.FEMALE,
                "서울",
                IncomeRange.FROM_40_TO_60_MILLION,
                new BigDecimal("22.50"),
                true,
                SelfRatedHealth.FAIR,
                SmokingStatus.NEVER,
                DrinkingFrequency.MONTHLY_OR_LESS,
                ExerciseFrequency.THREE_TO_FOUR_PER_WEEK
        );
    }
}

package com.likelion.medicast.prediction.controller;

import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.entity.IncomeRange;
import com.likelion.medicast.assessment.entity.Region;
import com.likelion.medicast.assessment.repository.HealthAssessmentRepository;
import com.likelion.medicast.auth.security.JwtTokenProvider;
import com.likelion.medicast.prediction.repository.PredictionRepository;
import com.likelion.medicast.user.entity.User;
import com.likelion.medicast.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PredictionControllerIntegrationTest {

    private static final String[] REQUIRED_FIELDS = {
            "householdSize",
            "incomeRange",
            "healthInsuranceType",
            "region",
            "selfRatedHealth",
            "heightCm",
            "weightKg",
            "exercises",
            "outpatientVisitsLastYear",
            "dentalVisitsLastYear",
            "hasPrivateHealthInsurance",
            "hospitalizedLastYear",
            "activityLimitation",
            "anxietyDepression",
            "occupation"
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    void setUp() {
        cleanUp();
        owner = userRepository.saveAndFlush(new User("owner@example.com", "hash", "소유자"));
        otherUser = userRepository.saveAndFlush(new User("other@example.com", "hash", "다른 사용자"));
    }

    @AfterEach
    void cleanUp() {
        predictionRepository.deleteAll();
        healthAssessmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsAssessmentAndPredictionFromFifteenInputs() throws Exception {
        String token = jwtTokenProvider.createAccessToken(owner.getId());

        String response = mockMvc.perform(post("/predictions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.predictedAnnualMedicalCost").isNumber())
                .andExpect(jsonPath("$.highCostProbability").value(0.15))
                .andExpect(jsonPath("$.currency").value("KRW"))
                .andExpect(jsonPath("$.modelVersion").value("deterministic-fake-v1"))
                .andExpect(jsonPath("$.completedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long predictionId = ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();
        long assessmentId = ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.assessmentId")).longValue();

        HealthAssessment saved = healthAssessmentRepository.findById(assessmentId).orElseThrow();
        assertThat(saved.getUser().getId()).isEqualTo(owner.getId());
        assertThat(saved.getHouseholdSize()).isEqualTo(3);
        assertThat(saved.getIncomeRange()).isEqualTo(IncomeRange.FROM_2400_TO_4500);
        assertThat(saved.getRegion()).isEqualTo(Region.SEOUL);
        assertThat(saved.getHeightCm()).isEqualByComparingTo("170.0000");
        assertThat(saved.getWeightKg()).isEqualByComparingTo("65.0000");
        assertThat(saved.getBmi()).isEqualByComparingTo("22.4913");

        mockMvc.perform(get("/predictions/{predictionId}", predictionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(predictionId))
                .andExpect(jsonPath("$.assessmentId").value(assessmentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void rejectsEveryMissingInput() throws Exception {
        String token = jwtTokenProvider.createAccessToken(owner.getId());

        for (String field : REQUIRED_FIELDS) {
            Map<String, Object> payload = validPayload();
            payload.remove(field);

            mockMvc.perform(post("/predictions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void rejectsOutOfRangeOrUnknownInput(String field, Object invalidValue) throws Exception {
        String token = jwtTokenProvider.createAccessToken(owner.getId());
        Map<String, Object> payload = validPayload();
        payload.put(field, invalidValue);

        mockMvc.perform(post("/predictions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void acceptsDocumentedNumericBoundaries() throws Exception {
        String token = jwtTokenProvider.createAccessToken(owner.getId());
        Map<String, Object> payload = validPayload();
        payload.put("householdSize", 1);
        payload.put("outpatientVisitsLastYear", 0);
        payload.put("dentalVisitsLastYear", 0);

        mockMvc.perform(post("/predictions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        payload.put("householdSize", 10);
        mockMvc.perform(post("/predictions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }

    @Test
    void hidesOtherUsersPrediction() throws Exception {
        String ownerToken = jwtTokenProvider.createAccessToken(owner.getId());
        String otherToken = jwtTokenProvider.createAccessToken(otherUser.getId());

        String response = mockMvc.perform(post("/predictions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
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
        mockMvc.perform(post("/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of("householdSize", 0),
                Arguments.of("householdSize", 11),
                Arguments.of("heightCm", 0),
                Arguments.of("weightKg", 0),
                Arguments.of("outpatientVisitsLastYear", -1),
                Arguments.of("dentalVisitsLastYear", -1),
                Arguments.of("incomeRange", "UNKNOWN"),
                Arguments.of("healthInsuranceType", "UNKNOWN"),
                Arguments.of("region", "UNKNOWN"),
                Arguments.of("selfRatedHealth", "UNKNOWN"),
                Arguments.of("activityLimitation", "UNKNOWN"),
                Arguments.of("anxietyDepression", "UNKNOWN"),
                Arguments.of("occupation", "UNKNOWN")
        );
    }

    private static Map<String, Object> validPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("householdSize", 3);
        payload.put("incomeRange", "2400-4500");
        payload.put("healthInsuranceType", "1");
        payload.put("region", "서울특별시");
        payload.put("selfRatedHealth", "FAIR");
        payload.put("heightCm", new BigDecimal("170"));
        payload.put("weightKg", new BigDecimal("65"));
        payload.put("exercises", true);
        payload.put("outpatientVisitsLastYear", 4);
        payload.put("dentalVisitsLastYear", 1);
        payload.put("hasPrivateHealthInsurance", true);
        payload.put("hospitalizedLastYear", false);
        payload.put("activityLimitation", "NONE");
        payload.put("anxietyDepression", "SOME");
        payload.put("occupation", "31");
        return payload;
    }
}

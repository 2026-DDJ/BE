package com.likelion.medicast.auth.security;

import com.likelion.medicast.assessment.repository.HealthAssessmentRepository;
import com.likelion.medicast.prediction.repository.PredictionRepository;
import com.likelion.medicast.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.auth.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:medicast-auth-disabled;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
class AuthenticationDisabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private HealthAssessmentRepository healthAssessmentRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        predictionRepository.deleteAll();
        healthAssessmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void servesPredictionApiWithoutAuthenticationUsingPublicDemoUser() throws Exception {
        String response = mockMvc.perform(post("/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPredictionRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long predictionId = ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id"))
                .longValue();

        mockMvc.perform(get("/predictions/{predictionId}", predictionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(predictionId));

        assertThat(userRepository.findByEmail(RequestUserIdResolver.DEMO_USER_EMAIL)).isPresent();
    }

    @Test
    void removesAuthenticationEndpointsAndBearerRequirementFromOpenApi() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode document = objectMapper.readTree(response);

        assertThat(document.at("/paths/~1auth~1signup").isMissingNode()).isTrue();
        assertThat(document.at("/paths/~1auth~1login").isMissingNode()).isTrue();
        assertThat(document.at("/paths/~1predictions/post/security").isMissingNode()).isTrue();
        assertThat(document.at("/components/securitySchemes/bearerAuth").isMissingNode()).isTrue();
    }

    private String validPredictionRequest() {
        return """
                {
                  "householdSize": 3,
                  "incomeRange": "2400-4500",
                  "healthInsuranceType": "1",
                  "region": "서울특별시",
                  "selfRatedHealth": "FAIR",
                  "heightCm": 170,
                  "weightKg": 65,
                  "exercises": true,
                  "outpatientVisitsLastYear": 4,
                  "dentalVisitsLastYear": 1,
                  "hasPrivateHealthInsurance": true,
                  "hospitalizedLastYear": false,
                  "activityLimitation": "NONE",
                  "anxietyDepression": "SOME",
                  "occupation": "31"
                }
                """;
    }
}

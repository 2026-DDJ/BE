package com.likelion.medicast.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exposesDocumentedApiContractWithoutAuthentication() throws Exception {
        String responseBody = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode document = objectMapper.readTree(responseBody);

        assertThat(document.at("/info/title").asString()).isEqualTo("MEDICAST API");
        assertThat(document.at("/components/securitySchemes/bearerAuth/scheme").asString())
                .isEqualTo("bearer");

        assertThat(document.at("/paths/~1auth~1signup/post/responses/201").isMissingNode()).isFalse();
        assertThat(document.at("/paths/~1auth~1login/post/responses/200").isMissingNode()).isFalse();
        assertThat(document.at("/paths/~1auth~1my/get/security/0/bearerAuth").isArray()).isTrue();
        assertThat(document.at("/paths/~1predictions/post/responses/201").isMissingNode()).isFalse();
        assertThat(document.at("/paths/~1predictions~1{predictionId}/get/responses/404").isMissingNode())
                .isFalse();

        JsonNode assessmentSchema = document.at("/components/schemas/HealthAssessmentInput/properties");
        assertThat(assessmentSchema.get("householdSize").get("minimum").asInt()).isEqualTo(1);
        assertThat(assessmentSchema.get("incomeRange").get("example").asString())
                .isEqualTo("2400-4500");
    }
}

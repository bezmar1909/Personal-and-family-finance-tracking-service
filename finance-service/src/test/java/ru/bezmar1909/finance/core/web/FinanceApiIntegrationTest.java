package ru.bezmar1909.finance.core.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FinanceApiIntegrationTest {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Test
    void createsIncomeOperationWithIncomeSourceThroughRestApi() throws Exception {
        String authorization = bearer(101L, "USER");

        long groupId = idFrom(mockMvc.perform(post("/api/groups")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Family\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long categoryId = idFrom(mockMvc.perform(post("/api/categories")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Salary","type":"INCOME","groupId":%d}
                                """.formatted(groupId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long incomeSourceId = idFrom(mockMvc.perform(post("/api/income-sources")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Main job","groupId":%d}
                                """.formatted(groupId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/operations")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 150000,
                                  "operationDate": "2026-06-15",
                                  "type": "INCOME",
                                  "description": "June salary",
                                  "categoryId": %d,
                                  "incomeSourceId": %d,
                                  "groupId": %d
                                }
                                """.formatted(categoryId, incomeSourceId, groupId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incomeSourceName").value("Main job"));

        mockMvc.perform(get("/api/operations")
                        .header("Authorization", authorization)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].incomeSourceName").value("Main job"));
    }

    @Test
    void adminStatsRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/finance/stats")
                        .header("Authorization", bearer(201L, "USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/finance/stats")
                        .header("Authorization", bearer(202L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups").exists())
                .andExpect(jsonPath("$.incomeSources").exists());
    }

    private long idFrom(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    private String bearer(Long userId, String role) throws Exception {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("""
                {"sub":"test@example.com","userId":%d,"role":"%s","exp":%d}
                """.formatted(userId, role, Instant.now().plusSeconds(3600).getEpochSecond()));
        String signature = sign(header + "." + payload);
        return "Bearer " + header + "." + payload + "." + signature;
    }

    private String encode(String json) {
        return ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}

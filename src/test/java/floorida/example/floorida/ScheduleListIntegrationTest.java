package floorida.example.floorida;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import floorida.example.floorida.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleListIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserRepository userRepository;

    private record LoginResult(String token, long userId) {}

    private LoginResult registerVerifyLogin(String email, String username, String password) throws Exception {
        String registerBody = "{" +
                "\"email\":\"" + email + "\"," +
                "\"username\":\"" + username + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        long userId = registerJson.get("userId").asLong();

        // 테스트에서는 이메일 인증 링크를 따라갈 수 없으니 DB에서 인증 처리
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setEmailVerified(Boolean.TRUE);
            userRepository.save(u);
        });

        String loginBody = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.get("accessToken").asText();

        return new LoginResult(token, userId);
    }

    @Test
    void listSchedules_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/schedules"))
                                .andExpect(status().isForbidden());
    }

    @Test
    void listSchedules_withAuth_returns200() throws Exception {
        LoginResult login = registerVerifyLogin("schedule-list@floorida.local", "schedulelist", "test1234!");

        mockMvc.perform(get("/api/schedules")
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isOk());
    }
}

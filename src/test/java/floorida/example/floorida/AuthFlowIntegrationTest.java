package floorida.example.floorida;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import floorida.example.floorida.repository.UserProfileRepository;
import floorida.example.floorida.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserProfileRepository userProfileRepository;

        @Autowired
        UserRepository userRepository;

    @Test
    void register_then_login_createsUserProfileOnce() throws Exception {
        String email = "newuser@floorida.local";
        String username = "newuser";
        String password = "test1234!";

        // register
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
                assertThat(registerJson.get("email").asText()).isEqualTo(email);

        long initialProfileCount = userProfileRepository.count();

                // 테스트에서는 이메일 인증 링크를 실제로 따라갈 수 없으니 DB에서 인증 처리
                userRepository.findByEmail(email).ifPresent(u -> {
                        u.setEmailVerified(Boolean.TRUE);
                        userRepository.save(u);
                });

        // login (first login should create profile)
        String loginBody = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());

        assertThat(userProfileRepository.findById(userId)).isPresent();

        // login again should not create duplicates / fail
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());

        assertThat(userProfileRepository.count()).isEqualTo(initialProfileCount + 1);
    }
}

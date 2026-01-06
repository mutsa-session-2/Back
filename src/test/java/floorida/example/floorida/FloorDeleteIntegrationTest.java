package floorida.example.floorida;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import floorida.example.floorida.repository.FloorPlanRepository;
import floorida.example.floorida.repository.FloorStatusRepository;
import floorida.example.floorida.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FloorDeleteIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserRepository userRepository;
    @Autowired FloorPlanRepository floorPlanRepository;
    @Autowired FloorStatusRepository floorStatusRepository;

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

    private JsonNode createSchedule(LoginResult login, String title, LocalDate start, LocalDate end) throws Exception {
        String body = "{" +
                "\"title\":\"" + title + "\"," +
                "\"startDate\":\"" + start + "\"," +
                "\"endDate\":\"" + end + "\"," +
                "\"teamId\":null" +
                "}";

        String resp = mockMvc.perform(post("/api/schedules")
                        .header("Authorization", "Bearer " + login.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(resp);
    }

    @Test
    void deleteSingleFloor_withStatus_doesNot500_andCleansUp() throws Exception {
        LoginResult login = registerVerifyLogin("floor-del1@floorida.local", "floordel1", "test1234!");

        LocalDate day = LocalDate.of(2025, 1, 1);
        JsonNode schedule = createSchedule(login, "삭제 테스트", day, day);

        long floorId = schedule.get("floors").get(0).get("floorId").asLong();

        // 완료 처리로 floor_statuses 생성
        mockMvc.perform(post("/api/floors/" + floorId + "/complete")
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isOk());

        // 삭제: 204
        mockMvc.perform(delete("/api/floors/" + floorId)
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isNoContent());

        assertThat(floorPlanRepository.findById(floorId)).isEmpty();
        assertThat(floorStatusRepository.findByFloor_FloorIdAndUser_UserId(floorId, login.userId)).isEmpty();
    }

    @Test
        void deleteFloorsBySchedule_withStatuses_deletesScheduleToo() throws Exception {
        LoginResult login = registerVerifyLogin("floor-del2@floorida.local", "floordel2", "test1234!");

        LocalDate start = LocalDate.of(2025, 2, 1);
        LocalDate end = LocalDate.of(2025, 2, 3);
        JsonNode schedule = createSchedule(login, "일괄 삭제 테스트", start, end);

        long scheduleId = schedule.get("scheduleId").asLong();
        long floorId0 = schedule.get("floors").get(0).get("floorId").asLong();

        // 일부만 완료 처리하여 status 생성
        mockMvc.perform(post("/api/floors/" + floorId0 + "/complete")
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/floors/schedule/" + scheduleId)
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isNoContent());

        assertThat(floorPlanRepository.findBySchedule_ScheduleId(scheduleId)).isEmpty();
        // floorId0의 status도 함께 삭제되어야 함
        assertThat(floorStatusRepository.findByFloor_FloorIdAndUser_UserId(floorId0, login.userId)).isEmpty();

        // 일정도 삭제되어야 함
        mockMvc.perform(get("/api/schedules/" + scheduleId)
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSchedule_withStatuses_doesNot500() throws Exception {
        LoginResult login = registerVerifyLogin("floor-del3@floorida.local", "floordel3", "test1234!");

        LocalDate day = LocalDate.of(2025, 3, 1);
        JsonNode schedule = createSchedule(login, "일정 삭제 테스트", day, day);

        long scheduleId = schedule.get("scheduleId").asLong();
        long floorId = schedule.get("floors").get(0).get("floorId").asLong();

        mockMvc.perform(post("/api/floors/" + floorId + "/complete")
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/schedules/" + scheduleId)
                        .header("Authorization", "Bearer " + login.token))
                .andExpect(status().isNoContent());

        assertThat(floorPlanRepository.findById(floorId)).isEmpty();
        assertThat(floorStatusRepository.findByFloor_FloorIdAndUser_UserId(floorId, login.userId)).isEmpty();
    }
}

package com.b2bgym.controller;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.config.security.JwtProvider;
import com.b2bgym.config.security.UserType;
import com.b2bgym.domain.admin.Admin;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.AdminRepository;
import com.b2bgym.repository.GymRepository;
import com.b2bgym.repository.MemberRepository;
import com.b2bgym.repository.TrainerRepository;
import com.b2bgym.support.TestFixtures;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가입·로그인·권한을 실제 필터 체인으로 확인하는 통합 테스트
 *  - @SpringBootTest: 애플리케이션 전체를 띄움 (Security 필터, 컨트롤러, 서비스, JPA, Testcontainers MySQL)
 *  - MockMvc: 서버를 띄우지 않고 요청을 흉내 냄. apply(springSecurity())가 Security 필터 체인을 MockMvc에 끼움
 *    -> 이게 없으면 JwtFilter·인가 검사를 건너뛰어서 테스트가 의미 없어짐
 *  - @Transactional: 테스트마다 끝나면 롤백 -> 가입한 회원이 다음 테스트에 남지 않음
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthApiTest {

    private static final String PASSWORD = "password123!";

    @Autowired WebApplicationContext context;
    @Autowired MemberRepository memberRepository;
    @Autowired TrainerRepository trainerRepository;
    @Autowired AdminRepository adminRepository;
    @Autowired GymRepository gymRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    @Value("${jwt.secret}")
    String secret;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ---------- 회원 가입 ----------

    @Test
    @DisplayName("회원 가입에 성공하면 201이고, 비밀번호는 해시로 저장된다")
    void memberSignup() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("new@example.com", "01012345678")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").isNumber())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Member saved = memberRepository.findByEmail("new@example.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(saved.getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches(PASSWORD, saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이미 가입된 이메일로 가입하면 409 EMAIL_ALREADY_EXISTS")
    void memberSignupDuplicateEmail() throws Exception {
        saveMember("dup@example.com");

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("dup@example.com", "01099998888")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("이메일 형식이 틀리면 400 INVALID_INPUT")
    void memberSignupInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("not-an-email", "01012345678")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ---------- 로그인 ----------

    @Test
    @DisplayName("회원 로그인에 성공하면 토큰을 받고, 그 토큰으로 내 정보를 조회할 수 있다")
    void memberLoginAndMe() throws Exception {
        saveMember("login@example.com");

        String body = mockMvc.perform(post("/api/v1/auth/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(body, "$.accessToken");

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("login@example.com"));
    }

    @Test
    @DisplayName("비밀번호가 틀려도, 없는 이메일이어도 똑같이 401 LOGIN_FAILED")
    void memberLoginFail() throws Exception {
        saveMember("login@example.com");

        mockMvc.perform(post("/api/v1/auth/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));

        mockMvc.perform(post("/api/v1/auth/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("nobody@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    @DisplayName("회원 계정으로 트레이너 로그인 주소에 로그인할 수 없다 (테이블이 다름)")
    void memberCannotLoginAsTrainer() throws Exception {
        saveMember("login@example.com");

        mockMvc.perform(post("/api/v1/auth/trainers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    @DisplayName("트레이너·관리자 로그인 토큰에는 각자의 사용자 종류가 담긴다")
    void trainerAndAdminLogin() throws Exception {
        Gym gym = gymRepository.save(TestFixtures.gym());
        Trainer trainer = trainerRepository.save(Trainer.createTrainer(
                gym, "trainer@example.com", passwordEncoder.encode(PASSWORD), "트레이너", "01011112222"));
        Admin admin = adminRepository.save(Admin.createAdmin(
                gym, "admin@example.com", passwordEncoder.encode(PASSWORD), "관리자"));

        String trainerBody = mockMvc.perform(post("/api/v1/auth/trainers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("trainer@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminBody = mockMvc.perform(post("/api/v1/auth/admins/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var trainerUser = jwtProvider.parse(JsonPath.read(trainerBody, "$.accessToken"));
        var adminUser = jwtProvider.parse(JsonPath.read(adminBody, "$.accessToken"));
        assertThat(trainerUser.id()).isEqualTo(trainer.getId());
        assertThat(trainerUser.type()).isEqualTo(UserType.TRAINER);
        assertThat(adminUser.id()).isEqualTo(admin.getId());
        assertThat(adminUser.type()).isEqualTo(UserType.ADMIN);
    }

    // ---------- 트레이너 가입 ----------

    @Test
    @DisplayName("트레이너 가입에 성공하면 201이고 소속 헬스장 id가 응답에 담긴다")
    void trainerSignup() throws Exception {
        Gym gym = gymRepository.save(TestFixtures.gym());

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainerSignupJson(gym.getId(), "newtrainer@example.com", "01033334444")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainerId").isNumber())
                .andExpect(jsonPath("$.gymId").value(gym.getId().intValue()));
    }

    @Test
    @DisplayName("없는 헬스장으로 트레이너 가입하면 404 GYM_NOT_FOUND")
    void trainerSignupGymNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainerSignupJson(999_999L, "newtrainer@example.com", "01033334444")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GYM_NOT_FOUND"));
    }

    // ---------- 401: 인증 실패 ----------

    @Test
    @DisplayName("토큰 없이 보호된 주소를 호출하면 401 UNAUTHORIZED이고 응답 모양은 { code, message }")
    void noToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("Bearer로 시작하지 않는 Authorization 헤더는 토큰이 없는 것으로 본다 -> 401")
    void notBearer() throws Exception {
        String token = jwtProvider.createAccessToken(1L, UserType.MEMBER);

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("서명을 바꾼 토큰은 401")
    void tamperedToken() throws Exception {
        String[] parts = jwtProvider.createAccessToken(1L, UserType.MEMBER).split("\\.");
        String signature = parts[2];
        char changed = signature.charAt(0) == 'A' ? 'B' : 'A'; // 서명 첫 글자만 다른 글자로
        String tampered = parts[0] + "." + parts[1] + "." + changed + signature.substring(1);

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("만료된 토큰은 401")
    void expiredToken() throws Exception {
        String expired = new JwtProvider(secret, -60).createAccessToken(1L, UserType.MEMBER);

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("만료된 토큰이 붙어 있어도 로그인 주소(공개)는 통과한다")
    void expiredTokenOnPublicUrl() throws Exception {
        saveMember("login@example.com");
        String expired = new JwtProvider(secret, -60).createAccessToken(1L, UserType.MEMBER);

        mockMvc.perform(post("/api/v1/auth/members/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com", PASSWORD)))
                .andExpect(status().isOk());
    }

    // ---------- 403: 역할이 맞지 않음 ----------

    @Test
    @DisplayName("트레이너 토큰으로 회원 전용 주소를 호출하면 403 ACCESS_DENIED (id가 같은 회원의 정보가 나가지 않음)")
    void trainerOnMemberUrl() throws Exception {
        Member member = saveMember("member@example.com");
        String trainerToken = jwtProvider.createAccessToken(member.getId(), UserType.TRAINER); // id는 회원과 같게

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + trainerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("회원 토큰으로 관리자 주소를 호출하면 403, 관리자 토큰은 인가를 통과한다")
    void adminUrl() throws Exception {
        String memberToken = jwtProvider.createAccessToken(1L, UserType.MEMBER);
        String adminToken = jwtProvider.createAccessToken(1L, UserType.ADMIN);

        mockMvc.perform(get("/api/v1/admin/no-such-api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        // 관리자는 Security를 통과해 컨트롤러 찾기까지 감 -> 없는 주소라 404 (403이 아니라는 게 핵심)
        mockMvc.perform(get("/api/v1/admin/no-such-api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ---------- 도우미 ----------

    private Member saveMember(String email) {
        return memberRepository.save(Member.createMember(
                email, passwordEncoder.encode(PASSWORD), "회원", "01055556666"));
    }

    private String signupJson(String email, String phone) {
        return """
                {"email": "%s", "password": "%s", "name": "홍길동", "phone": "%s"}
                """.formatted(email, PASSWORD, phone);
    }

    private String trainerSignupJson(Long gymId, String email, String phone) {
        return """
                {"gymId": %d, "email": "%s", "password": "%s", "name": "김트레이너", "phone": "%s"}
                """.formatted(gymId, email, PASSWORD, phone);
    }

    private String loginJson(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }
}

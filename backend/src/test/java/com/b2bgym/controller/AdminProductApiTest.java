package com.b2bgym.controller;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.config.security.JwtProvider;
import com.b2bgym.config.security.UserType;
import com.b2bgym.domain.admin.Admin;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.repository.AdminRepository;
import com.b2bgym.repository.GymRepository;
import com.b2bgym.repository.MemberRepository;
import com.b2bgym.repository.MembershipProductRepository;
import com.b2bgym.repository.TrainerRepository;
import com.b2bgym.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 관리자 상품 API (2-1b) 통합 테스트
 *  - 헬스장 A·B 두 곳을 만들고, A 관리자가 B의 트레이너·상품에 손댈 수 없는지가 핵심
 *  - 토큰은 로그인 API를 거치지 않고 JwtProvider로 바로 만듦 (로그인은 AuthApiTest에서 확인함)
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AdminProductApiTest {

    private static final String URL = "/api/v1/admin/products";

    @Autowired WebApplicationContext context;
    @Autowired GymRepository gymRepository;
    @Autowired AdminRepository adminRepository;
    @Autowired TrainerRepository trainerRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired MembershipProductRepository productRepository;
    @Autowired JwtProvider jwtProvider;

    MockMvc mockMvc;

    Gym gymA;
    Trainer trainerA;
    Trainer trainerB;
    MembershipProduct productB;
    String adminAToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        gymA = gymRepository.save(TestFixtures.gym());
        Gym gymB = gymRepository.save(TestFixtures.gym());
        Admin adminA = adminRepository.save(Admin.createAdmin(gymA, "adminA@example.com", "pw", "A 관리자"));
        trainerA = trainerRepository.save(TestFixtures.trainer(gymA));
        trainerB = trainerRepository.save(TestFixtures.trainer(gymB));
        productB = productRepository.save(TestFixtures.periodProduct(gymB));

        adminAToken = bearer(adminA.getId(), UserType.ADMIN);
    }

    // ---------- 등록 ----------

    @Test
    @DisplayName("기간권을 등록하면 201이고, 헬스장은 요청 값이 아니라 관리자의 헬스장으로 정해진다")
    void createPeriod() throws Exception {
        String body = """
                {"type":"PERIOD","name":"헬스 1개월","durationDays":30,"price":70000}
                """;

        Long productId = createAndGetId(body);

        assertThat(productRepository.findById(productId).orElseThrow().getGym().getId()).isEqualTo(gymA.getId());
    }

    @Test
    @DisplayName("자기 헬스장 트레이너의 PT 상품을 등록할 수 있다")
    void createPtWithOwnTrainer() throws Exception {
        String body = """
                {"type":"PT_COUNT","name":"PT 10회","durationDays":60,"price":600000,"trainerId":%d,"totalCount":10}
                """.formatted(trainerA.getId());

        mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainerId").value(trainerA.getId()))
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.onSale").value(true));
    }

    @Test
    @DisplayName("다른 헬스장 트레이너로 PT 상품을 등록하면 403 FORBIDDEN_GYM이고, 상품은 저장되지 않는다")
    void createPtWithOtherGymTrainer() throws Exception {
        long before = productRepository.count();
        String body = """
                {"type":"PT_COUNT","name":"PT 10회","durationDays":60,"price":600000,"trainerId":%d,"totalCount":10}
                """.formatted(trainerB.getId());

        mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_GYM"));

        assertThat(productRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("체험 PT는 trial=true만 보내면 1회권으로 등록된다")
    void createTrial() throws Exception {
        String body = """
                {"type":"PT_COUNT","name":"체험 PT","durationDays":14,"price":20000,"trainerId":%d,"trial":true}
                """.formatted(trainerA.getId());

        mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trial").value(true))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @DisplayName("PT 상품에 트레이너가 없으면 400 PT_TRAINER_REQUIRED")
    void createPtWithoutTrainer() throws Exception {
        String body = """
                {"type":"PT_COUNT","name":"PT 10회","durationDays":60,"price":600000,"totalCount":10}
                """;

        mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PT_TRAINER_REQUIRED"));
    }

    @Test
    @DisplayName("기간권에 트레이너를 넣으면 400 INVALID_INPUT")
    void createPeriodWithTrainer() throws Exception {
        String body = """
                {"type":"PERIOD","name":"헬스 1개월","durationDays":30,"price":70000,"trainerId":%d}
                """.formatted(trainerA.getId());

        mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("가격이 0이면 400 INVALID_INPUT (@Valid)")
    void createWithZeroPrice() throws Exception {
        String body = """
                {"type":"PERIOD","name":"헬스 1개월","durationDays":30,"price":0}
                """;

        mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ---------- 목록 ----------

    @Test
    @DisplayName("목록에는 자기 헬스장 상품만 나온다")
    void listOnlyOwnGym() throws Exception {
        MembershipProduct own = productRepository.save(TestFixtures.periodProduct(gymA));

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, adminAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(own.getId()));
    }

    // ---------- 수정 ----------

    @Test
    @DisplayName("수정에서 판매 종료일을 null로 보내면 상시 판매로 바뀐다")
    void updateClearsSaleEndDate() throws Exception {
        MembershipProduct own = productRepository.save(MembershipProduct.createPeriodProduct(
                gymA, "헬스 1개월", 30, 70000, LocalDate.now().plusDays(10)));
        String body = """
                {"name":"헬스 1개월 (할인)","durationDays":30,"price":60000,"saleEndDate":null}
                """;

        mockMvc.perform(patch(URL + "/" + own.getId()).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("헬스 1개월 (할인)"))
                .andExpect(jsonPath("$.price").value(60000))
                .andExpect(jsonPath("$.saleEndDate").doesNotExist());
    }

    @Test
    @DisplayName("다른 헬스장 상품을 수정하면 403 FORBIDDEN_GYM이고, 값은 그대로다")
    void updateOtherGymProduct() throws Exception {
        String body = """
                {"name":"가로채기","durationDays":30,"price":1}
                """;

        mockMvc.perform(patch(URL + "/" + productB.getId()).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_GYM"));

        assertThat(productRepository.findById(productB.getId()).orElseThrow().getPrice()).isEqualTo(100000);
    }

    @Test
    @DisplayName("없는 상품을 수정하면 404 PRODUCT_NOT_FOUND")
    void updateNotFound() throws Exception {
        String body = """
                {"name":"헬스","durationDays":30,"price":1000}
                """;

        mockMvc.perform(patch(URL + "/999999").header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    // ---------- 판매 중지 ----------

    @Test
    @DisplayName("판매 중지하면 active·onSale이 false가 되고, 두 번 해도 200이다")
    void deactivate() throws Exception {
        MembershipProduct own = productRepository.save(TestFixtures.periodProduct(gymA));

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post(URL + "/" + own.getId() + "/deactivate").header(HttpHeaders.AUTHORIZATION, adminAToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false))
                    .andExpect(jsonPath("$.onSale").value(false));
        }
    }

    @Test
    @DisplayName("다른 헬스장 상품은 판매 중지할 수 없다")
    void deactivateOtherGymProduct() throws Exception {
        mockMvc.perform(post(URL + "/" + productB.getId() + "/deactivate").header(HttpHeaders.AUTHORIZATION, adminAToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_GYM"));

        assertThat(productRepository.findById(productB.getId()).orElseThrow().isActive()).isTrue();
    }

    // ---------- 역할 ----------

    @Test
    @DisplayName("회원·트레이너 토큰으로는 관리자 상품 API에 접근할 수 없다 (403)")
    void notAdmin() throws Exception {
        Member member = memberRepository.save(TestFixtures.member());

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(member.getId(), UserType.MEMBER)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(trainerA.getId(), UserType.TRAINER)))
                .andExpect(status().isForbidden());
    }

    // ---------- 도우미 ----------

    private String bearer(Long id, UserType type) {
        return "Bearer " + jwtProvider.createAccessToken(id, type);
    }

    private Long createAndGetId(String body) throws Exception {
        String response = mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, adminAToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.productId")).longValue();
    }
}

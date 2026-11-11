package com.b2bgym.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 스프링 컨텍스트·DB 없이 도는 테스트
 *  - standaloneSetup: 테스트용 컨트롤러 하나 + GlobalExceptionHandler만 올린 가짜 MVC
 *  - 각 엔드포인트가 일부러 예외를 던지고, 응답이 { code, message }로 바뀌는지 확인
 */
class GlobalExceptionHandlerTest {

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    @DisplayName("BusinessException은 ErrorCode의 상태 코드와 { code, message }로 응답한다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRIAL_ALREADY_USED"))
                .andExpect(jsonPath("$.message").value(ErrorCode.TRIAL_ALREADY_USED.getMessage()));
    }

    @Test
    @DisplayName("BusinessException에 문구를 따로 주면 그 문구로 응답한다")
    void businessExceptionWithMessage() throws Exception {
        mockMvc.perform(get("/test/business-message"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("경력 연차는 0 이상이어야 합니다."));
    }

    @Test
    @DisplayName("@Valid 검증에 실패하면 400 INVALID_INPUT, 메시지는 '필드: 이유'")
    void validationFail() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(startsWith("name: ")));
    }

    @Test
    @DisplayName("JSON 문법이 깨지면 400 INVALID_INPUT")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("필수 헤더(Idempotency-Key)가 없으면 400 INVALID_INPUT, 메시지에 헤더 이름")
    void missingHeader() throws Exception {
        mockMvc.perform(post("/test/header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("필수 헤더가 없습니다: Idempotency-Key"));
    }

    @Test
    @DisplayName("DB 제약 위반은 500이 아니라 409 DATA_CONFLICT")
    void dataIntegrityViolation() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_CONFLICT"));
    }

    @Test
    @DisplayName("예상 못 한 예외는 500, 응답에 원인 메시지를 노출하지 않는다")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @GetMapping("/business")
        public void business() {
            throw new BusinessException(ErrorCode.TRIAL_ALREADY_USED);
        }

        @GetMapping("/business-message")
        public void businessMessage() {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "경력 연차는 0 이상이어야 합니다.");
        }

        @PostMapping("/valid")
        public void valid(@Valid @RequestBody TestRequest request) {
        }

        @PostMapping("/header")
        public void header(@RequestHeader("Idempotency-Key") String idempotencyKey) {
        }

        @GetMapping("/conflict")
        public void conflict() {
            throw new DataIntegrityViolationException("Duplicate entry 'same-key' for key 'payment.idempotency_key'");
        }

        @GetMapping("/unexpected")
        public void unexpected() {
            throw new IllegalStateException("내부 정보가 담긴 메시지");
        }
    }

    public record TestRequest(@NotBlank String name) {
    }
}

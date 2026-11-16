package com.b2bgym.controller;

import com.b2bgym.dto.trainer.TrainerResponse;
import com.b2bgym.dto.trainer.TrainerSignupRequest;
import com.b2bgym.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    // 트레이너 가입 (공개, 가입할 때 소속 헬스장 선택)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerResponse signup(@Valid @RequestBody TrainerSignupRequest request) {
        return trainerService.signup(request);
    }
}

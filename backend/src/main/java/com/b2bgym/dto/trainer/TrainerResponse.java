package com.b2bgym.dto.trainer;

import com.b2bgym.domain.trainer.Trainer;

/**
 * 트레이너 가입 응답
 *  - trainer.getGym().getId(): gym은 LAZY 프록시지만 id는 프록시가 이미 갖고 있어서 추가 쿼리가 나가지 않음
 */
public record TrainerResponse(Long trainerId, Long gymId, String email, String name) {

    public static TrainerResponse from(Trainer trainer) {
        return new TrainerResponse(trainer.getId(), trainer.getGym().getId(), trainer.getEmail(), trainer.getName());
    }
}

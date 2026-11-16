package com.b2bgym.service;

import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.dto.trainer.TrainerResponse;
import com.b2bgym.dto.trainer.TrainerSignupRequest;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.GymRepository;
import com.b2bgym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;

    // 트레이너 가입: 회원 가입과 같은 흐름 + 소속 헬스장이 실제로 있는지 확인
    @Transactional
    public TrainerResponse signup(TrainerSignupRequest request) {
        Gym gym = gymRepository.findById(request.gymId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GYM_NOT_FOUND));

        if (trainerRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (trainerRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Trainer trainer = Trainer.createTrainer(
                gym,
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phone());
        trainerRepository.save(trainer);

        return TrainerResponse.from(trainer);
    }
}

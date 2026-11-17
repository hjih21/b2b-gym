package com.b2bgym.service;

import com.b2bgym.domain.admin.Admin;
import com.b2bgym.domain.gx.GxClass;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.pt.PtSlot;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소속 단위 인가 (1-17): "역할은 맞는데, 이 데이터가 네 것이냐"
 *  - 역할 검사(ADMIN인가, TRAINER인가)는 SecurityConfig의 URL 규칙이 이미 끝냄
 *  - 여기서는 대상 데이터와 로그인 사용자를 비교 -> URL만 보는 Security는 할 수 없는 검사
 *    (권한 목록 GrantedAuthority는 "ADMIN" 같은 앱 전체 권한용이고, 상품 7번에 대한 권한 같은 건 담지 않음)
 *  - 쓰는 곳: 관리자 상품 API(2-1b), GX 수업 수정(3-2), PT 슬롯 닫기(4-1)의 서비스 메서드 첫 줄
 *  - 실패는 BusinessException -> 컨트롤러 안에서 나므로 GlobalExceptionHandler가 그대로 403으로 응답
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnershipChecker {

    private final AdminRepository adminRepository;

    // 관리자의 소속 헬스장 id (상품 등록·목록 조회에서 "자기 헬스장"을 정할 때도 사용)
    public Long getGymIdOfAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED)); // 토큰은 유효한데 계정이 없어진 경우
        return admin.getGym().getId();
    }

    // 관리자는 자기 헬스장 상품만
    public void checkAdminManagesProduct(Long adminId, MembershipProduct product) {
        Long adminGymId = getGymIdOfAdmin(adminId);
        if (!adminGymId.equals(product.getGym().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_GYM);
        }
    }

    /**
     * 관리자는 자기 헬스장 트레이너의 PT 상품만 등록 (2-1b)
     *  - 상품 팩토리는 "상품 헬스장 = 트레이너 헬스장"만 보장 -> 다른 헬스장 트레이너를 넘기면 그 헬스장 상품이 만들어져 버림
     *  - 그래서 "요청한 관리자가 그 트레이너의 헬스장 사람인가"는 여기서 따로 검사
     */
    public void checkAdminManagesTrainer(Long adminId, Trainer trainer) {
        Long adminGymId = getGymIdOfAdmin(adminId);
        if (!adminGymId.equals(trainer.getGym().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_GYM);
        }
    }

    // 트레이너는 자기 GX 수업만
    public void checkTrainerOwnsClass(Long trainerId, GxClass gxClass) {
        if (!gxClass.getTrainer().getId().equals(trainerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 수업이 아닙니다.");
        }
    }

    // 트레이너는 자기 PT 슬롯만
    public void checkTrainerOwnsSlot(Long trainerId, PtSlot slot) {
        if (!slot.getTrainer().getId().equals(trainerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 슬롯이 아닙니다.");
        }
    }
}

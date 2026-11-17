package com.b2bgym.service;

import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.membership.MembershipType;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.dto.product.AdminProductCreateRequest;
import com.b2bgym.dto.product.AdminProductResponse;
import com.b2bgym.dto.product.AdminProductUpdateRequest;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.GymRepository;
import com.b2bgym.repository.MembershipProductRepository;
import com.b2bgym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 상품 관리 (2-1b): 등록·목록·수정·판매 중지
 *  - 역할 검사(ADMIN인가)는 SecurityConfig의 /api/v1/admin/** 규칙이 끝냄
 *  - 여기서는 "자기 헬스장 것인가"만 OwnershipChecker로 검사 (1-17)
 *
 * 검사 위치 정리
 *  - 엔티티 팩토리: 데이터끼리의 일관성 (상품 헬스장 = 트레이너 헬스장, PT 횟수 >= 1)
 *  - 이 서비스 + OwnershipChecker: 요청한 사람의 권한 (관리자 헬스장 = 대상의 헬스장)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final MembershipProductRepository productRepository;
    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final OwnershipChecker ownershipChecker;

    @Transactional
    public AdminProductResponse create(Long adminId, AdminProductCreateRequest request) {
        MembershipProduct product = switch (request.type()) {
            case PERIOD -> createPeriod(adminId, request);
            case PT_COUNT -> createPt(adminId, request);
        };
        productRepository.save(product);
        return AdminProductResponse.of(product, LocalDate.now());
    }

    public List<AdminProductResponse> list(Long adminId) {
        Long gymId = ownershipChecker.getGymIdOfAdmin(adminId);
        LocalDate today = LocalDate.now();
        return productRepository.findAllByGymIdOrderByIdDesc(gymId).stream()
                .map(product -> AdminProductResponse.of(product, today))
                .toList();
    }

    @Transactional
    public AdminProductResponse update(Long adminId, Long productId, AdminProductUpdateRequest request) {
        MembershipProduct product = findManagedProduct(adminId, productId);
        product.update(request.name(), request.durationDays(), request.price(), request.saleEndDate());
        return AdminProductResponse.of(product, LocalDate.now()); // 변경 감지로 커밋 시 update 쿼리
    }

    @Transactional
    public AdminProductResponse deactivate(Long adminId, Long productId) {
        MembershipProduct product = findManagedProduct(adminId, productId);
        product.deactivate();
        return AdminProductResponse.of(product, LocalDate.now());
    }

    // ---------- 내부 ----------

    private MembershipProduct createPeriod(Long adminId, AdminProductCreateRequest request) {
        if (request.trainerId() != null || request.totalCount() != null || request.isTrial()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "기간권에는 트레이너·횟수·체험권을 지정할 수 없습니다.");
        }
        Long gymId = ownershipChecker.getGymIdOfAdmin(adminId);
        // getReferenceById: select 없이 id만 가진 프록시 -> FK(gym_id)만 채우면 되므로 조회가 필요 없음
        Gym gym = gymRepository.getReferenceById(gymId);
        return MembershipProduct.createPeriodProduct(
                gym, request.name(), request.durationDays(), request.price(), request.saleEndDate());
    }

    private MembershipProduct createPt(Long adminId, AdminProductCreateRequest request) {
        if (request.trainerId() == null) {
            throw new BusinessException(ErrorCode.PT_TRAINER_REQUIRED);
        }
        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAINER_NOT_FOUND));
        ownershipChecker.checkAdminManagesTrainer(adminId, trainer);

        if (request.isTrial()) {
            if (request.totalCount() != null && request.totalCount() != 1) {
                throw new BusinessException(ErrorCode.INVALID_PRODUCT_COUNT, "체험 PT는 1회권만 만들 수 있습니다.");
            }
            return MembershipProduct.createTrialPtProduct(
                    trainer, request.name(), request.durationDays(), request.price(), request.saleEndDate());
        }
        if (request.totalCount() == null) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_COUNT, "PT 횟수를 입력해 주세요.");
        }
        return MembershipProduct.createPtProduct(
                trainer, request.name(), request.durationDays(), request.totalCount(), request.price(), request.saleEndDate());
    }

    // 상품이 있는지 -> 자기 헬스장 것인지 순서로 확인
    private MembershipProduct findManagedProduct(Long adminId, Long productId) {
        MembershipProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        ownershipChecker.checkAdminManagesProduct(adminId, product);
        return product;
    }
}

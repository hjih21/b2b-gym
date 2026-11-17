package com.b2bgym.controller;

import com.b2bgym.config.security.LoginUser;
import com.b2bgym.dto.product.AdminProductCreateRequest;
import com.b2bgym.dto.product.AdminProductResponse;
import com.b2bgym.dto.product.AdminProductUpdateRequest;
import com.b2bgym.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 관리자 상품 관리 API (2-1b, API v1.2)
 *  - /api/v1/admin/** 는 SecurityConfig에서 hasRole("ADMIN") -> 회원·트레이너 토큰은 여기 오기 전에 403
 *  - 삭제 API는 없음: 판매 중지(deactivate)로 대신 (이미 팔린 회원권이 상품을 참조)
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductResponse create(@AuthenticationPrincipal LoginUser loginUser,
                                       @Valid @RequestBody AdminProductCreateRequest request) {
        return adminProductService.create(loginUser.id(), request);
    }

    @GetMapping
    public List<AdminProductResponse> list(@AuthenticationPrincipal LoginUser loginUser) {
        return adminProductService.list(loginUser.id());
    }

    @PatchMapping("/{productId}")
    public AdminProductResponse update(@AuthenticationPrincipal LoginUser loginUser,
                                       @PathVariable Long productId,
                                       @Valid @RequestBody AdminProductUpdateRequest request) {
        return adminProductService.update(loginUser.id(), productId, request);
    }

    @PostMapping("/{productId}/deactivate")
    public AdminProductResponse deactivate(@AuthenticationPrincipal LoginUser loginUser,
                                           @PathVariable Long productId) {
        return adminProductService.deactivate(loginUser.id(), productId);
    }
}

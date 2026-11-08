package com.b2bgym.repository;

import com.b2bgym.domain.membership.MembershipProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipProductRepository extends JpaRepository<MembershipProduct, Long> {

}

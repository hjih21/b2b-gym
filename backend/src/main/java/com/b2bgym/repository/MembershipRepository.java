package com.b2bgym.repository;

import com.b2bgym.domain.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}

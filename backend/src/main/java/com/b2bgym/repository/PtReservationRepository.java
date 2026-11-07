package com.b2bgym.repository;

import com.b2bgym.domain.pt.PtReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PtReservationRepository extends JpaRepository<PtReservation, Long> {
}

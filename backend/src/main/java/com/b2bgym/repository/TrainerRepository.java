package com.b2bgym.repository;

import com.b2bgym.domain.trainer.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

}

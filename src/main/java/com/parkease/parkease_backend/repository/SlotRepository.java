package com.parkease.parkease_backend.repository;

import com.parkease.parkease_backend.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {
	List<Slot> findByAvailableTrue();
	Optional<Slot> findByCode(String code);
}




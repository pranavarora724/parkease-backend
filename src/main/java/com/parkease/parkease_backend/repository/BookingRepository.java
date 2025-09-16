package com.parkease.parkease_backend.repository;

import com.parkease.parkease_backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	@Query("SELECT b FROM Booking b JOIN FETCH b.slot WHERE b.vehicleNumber = :vehicleNumber ORDER BY b.startTime DESC")
	List<Booking> findByVehicleNumberOrderByStartTimeDesc(@Param("vehicleNumber") String vehicleNumber);
	
	@Query("SELECT b FROM Booking b JOIN FETCH b.slot WHERE b.driverName = :driverName ORDER BY b.startTime DESC")
	List<Booking> findByDriverNameOrderByStartTimeDesc(@Param("driverName") String driverName);
}




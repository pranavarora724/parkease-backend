package com.parkease.parkease_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false)
	private String driverName;

	@NotBlank
	@Column(nullable = false)
	private String vehicleNumber;

	@NotNull
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "slot_id")
	private Slot slot;

	@NotNull
	@Column(nullable = false)
	private OffsetDateTime startTime;

	@NotNull
	@Future
	@Column(nullable = false)
	private OffsetDateTime endTime;

	@Column(nullable = false)
	private Double amount;

	@Column(nullable = false)
	private String status = "PENDING";

	@Column(nullable = false)
	private OffsetDateTime createdAt = OffsetDateTime.now();

	public Long getId() { return id; }
	public String getDriverName() { return driverName; }
	public void setDriverName(String driverName) { this.driverName = driverName; }
	public String getVehicleNumber() { return vehicleNumber; }
	public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
	public Slot getSlot() { return slot; }
	public void setSlot(Slot slot) { this.slot = slot; }
	public OffsetDateTime getStartTime() { return startTime; }
	public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }
	public OffsetDateTime getEndTime() { return endTime; }
	public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }
	public Double getAmount() { return amount; }
	public void setAmount(Double amount) { this.amount = amount; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
}



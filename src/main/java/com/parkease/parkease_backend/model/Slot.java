package com.parkease.parkease_backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "slots")
public class Slot {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String code;

	@Column(nullable = false)
	private String locationDescription;

	@Column(nullable = false)
	private Integer level;

	@Column(nullable = false)
	private Boolean available = true;

	@Column(nullable = false)
	private Double pricePerHour;

	@Column(nullable = false)
	private OffsetDateTime createdAt = OffsetDateTime.now();

	public Long getId() { return id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getLocationDescription() { return locationDescription; }
	public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }
	public Integer getLevel() { return level; }
	public void setLevel(Integer level) { this.level = level; }
	public Boolean getAvailable() { return available; }
	public void setAvailable(Boolean available) { this.available = available; }
	public Double getPricePerHour() { return pricePerHour; }
	public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
}




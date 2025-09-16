package com.parkease.parkease_backend.service;

import com.parkease.parkease_backend.model.Booking;
import com.parkease.parkease_backend.model.Slot;
import com.parkease.parkease_backend.repository.BookingRepository;
import com.parkease.parkease_backend.repository.SlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
	private final BookingRepository bookingRepository;
	private final SlotRepository slotRepository;

	public BookingService(BookingRepository bookingRepository, SlotRepository slotRepository) {
		this.bookingRepository = bookingRepository;
		this.slotRepository = slotRepository;
	}

	public List<Slot> getAllSlots() {
		return slotRepository.findAll();
	}

	public List<Slot> getAvailableSlots() {
		return slotRepository.findByAvailableTrue();
	}

	@Transactional
	public Booking createBooking(Long slotId, String driverName, String vehicleNumber, OffsetDateTime start, OffsetDateTime end) {
		Slot slot = slotRepository.findById(slotId).orElseThrow();
		if (!Boolean.TRUE.equals(slot.getAvailable())) {
			throw new IllegalStateException("Slot not available");
		}
		long hours = Math.max(1, Duration.between(start, end).toHours());
		double amount = hours * slot.getPricePerHour();
		Booking booking = new Booking();
		booking.setSlot(slot);
		booking.setDriverName(driverName);
		booking.setVehicleNumber(vehicleNumber);
		booking.setStartTime(start);
		booking.setEndTime(end);
		booking.setAmount(amount);
		// Keep slot available until payment confirmation
		return bookingRepository.save(booking);
	}

	public List<Booking> getBookingsForVehicle(String vehicleNumber) {
		return bookingRepository.findByVehicleNumberOrderByStartTimeDesc(vehicleNumber);
	}

	public List<Booking> getBookingsForDriver(String driverName) {
		return bookingRepository.findByDriverNameOrderByStartTimeDesc(driverName);
	}

	@Transactional
	public void cancelBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow();
		booking.setStatus("CANCELLED");
		
		// Make the slot available again when booking is cancelled
		Slot slot = booking.getSlot();
		if (slot != null) {
			slot.setAvailable(true);
			slotRepository.save(slot);
			System.out.println("Slot " + slot.getCode() + " is now available after booking cancellation");
		}
		
		bookingRepository.save(booking);
	}

	@Transactional
	public Booking confirmPayment(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
			.orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
		
		if ("CONFIRMED".equals(booking.getStatus())) {
			throw new RuntimeException("Booking already confirmed");
		}
		
		booking.setStatus("CONFIRMED");
		Slot slot = booking.getSlot();
		slot.setAvailable(false);
		slotRepository.save(slot);
		return bookingRepository.save(booking);
	}

	// Admin methods
	public List<Booking> getAllBookings() {
		System.out.println("Getting all bookings...");
		List<Booking> bookings = bookingRepository.findAll();
		System.out.println("Found " + bookings.size() + " bookings");
		return bookings;
	}

	public Map<String, Object> getAdminStats() {
		List<Booking> allBookings = bookingRepository.findAll();
		List<Slot> allSlots = slotRepository.findAll();
		
		long totalBookings = allBookings.size();
		long confirmedBookings = allBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count();
		long cancelledBookings = allBookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count();
		long pendingBookings = allBookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
		
		long totalSlots = allSlots.size();
		long availableSlots = allSlots.stream().filter(s -> Boolean.TRUE.equals(s.getAvailable())).count();
		long occupiedSlots = totalSlots - availableSlots;
		
		double totalRevenue = allBookings.stream()
			.filter(b -> "CONFIRMED".equals(b.getStatus()))
			.mapToDouble(Booking::getAmount)
			.sum();
		
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalBookings", totalBookings);
		stats.put("confirmedBookings", confirmedBookings);
		stats.put("cancelledBookings", cancelledBookings);
		stats.put("pendingBookings", pendingBookings);
		stats.put("totalSlots", totalSlots);
		stats.put("availableSlots", availableSlots);
		stats.put("occupiedSlots", occupiedSlots);
		stats.put("totalRevenue", totalRevenue);
		
		return stats;
	}

	@Transactional
	public Slot createSlot(String code, Integer level, String locationDescription, Double pricePerHour) {
		// Check if slot code already exists
		if (slotRepository.findByCode(code).isPresent()) {
			throw new RuntimeException("Slot with code '" + code + "' already exists");
		}
		
		System.out.println("Creating new slot: " + code + ", Level: " + level + ", Price: " + pricePerHour);
		
		Slot slot = new Slot();
		slot.setCode(code);
		slot.setLevel(level);
		slot.setLocationDescription(locationDescription);
		slot.setPricePerHour(pricePerHour);
		slot.setAvailable(true);
		
		Slot savedSlot = slotRepository.save(slot);
		System.out.println("Slot created successfully with ID: " + savedSlot.getId());
		
		return savedSlot;
	}

	@Transactional
	public void deleteSlot(Long slotId) {
		Slot slot = slotRepository.findById(slotId).orElseThrow();
		// Check if slot has active bookings
		List<Booking> activeBookings = bookingRepository.findAll().stream()
			.filter(b -> b.getSlot().getId().equals(slotId) && "CONFIRMED".equals(b.getStatus()))
			.toList();
		
		if (!activeBookings.isEmpty()) {
			throw new RuntimeException("Cannot delete slot with active bookings");
		}
		
		slotRepository.delete(slot);
	}

	@Transactional
	public void makeSlotAvailable(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> 
			new RuntimeException("Booking not found with id: " + bookingId));
		
		// Delete the cancelled booking record
		bookingRepository.delete(booking);
		
		// Make the slot available
		Slot slot = booking.getSlot();
		if (slot != null) {
			slot.setAvailable(true);
			slotRepository.save(slot);
			System.out.println("Cancelled booking removed and slot " + slot.getCode() + " made available by admin");
		}
	}

	// User management and activity tracking
	public List<Map<String, Object>> getAllUsers() {
		List<Booking> allBookings = bookingRepository.findAll();
		Map<String, Map<String, Object>> userMap = new HashMap<>();
		
		for (Booking booking : allBookings) {
			String driverName = booking.getDriverName();
			String vehicleNumber = booking.getVehicleNumber();
			
			userMap.computeIfAbsent(driverName, k -> {
				Map<String, Object> user = new HashMap<>();
				user.put("driverName", driverName);
				user.put("vehicleNumber", vehicleNumber);
				user.put("totalBookings", 0L);
				user.put("confirmedBookings", 0L);
				user.put("totalSpent", 0.0);
				user.put("lastBooking", null);
				return user;
			});
			
			Map<String, Object> user = userMap.get(driverName);
			user.put("totalBookings", (Long) user.get("totalBookings") + 1);
			
			if ("CONFIRMED".equals(booking.getStatus())) {
				user.put("confirmedBookings", (Long) user.get("confirmedBookings") + 1);
				user.put("totalSpent", (Double) user.get("totalSpent") + booking.getAmount());
			}
			
			if (user.get("lastBooking") == null || 
				booking.getStartTime().toString().compareTo((String) user.get("lastBooking")) > 0) {
				user.put("lastBooking", booking.getStartTime().toString());
			}
		}
		
		return new ArrayList<>(userMap.values());
	}

	public List<Map<String, Object>> getPaymentHistory() {
		List<Booking> confirmedBookings = bookingRepository.findAll().stream()
			.filter(b -> "CONFIRMED".equals(b.getStatus()))
			.sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
			.toList();
		
		return confirmedBookings.stream().map(booking -> {
			Map<String, Object> payment = new HashMap<>();
			payment.put("id", booking.getId());
			payment.put("driverName", booking.getDriverName());
			payment.put("vehicleNumber", booking.getVehicleNumber());
			payment.put("slotCode", booking.getSlot().getCode());
			payment.put("amount", booking.getAmount());
			payment.put("paymentDate", booking.getStartTime().toString());
			payment.put("duration", Duration.between(booking.getStartTime(), booking.getEndTime()).toHours());
			return payment;
		}).toList();
	}

	public List<Map<String, Object>> getRecentActivity() {
		List<Booking> recentBookings = bookingRepository.findAll().stream()
			.sorted((a, b) -> {
				// Sort by creation time if available, otherwise by start time
				return b.getStartTime().compareTo(a.getStartTime());
			})
			.limit(20)
			.toList();
		
		return recentBookings.stream().map(booking -> {
			Map<String, Object> activity = new HashMap<>();
			activity.put("id", booking.getId());
			activity.put("type", getActivityType(booking));
			activity.put("driverName", booking.getDriverName());
			activity.put("vehicleNumber", booking.getVehicleNumber());
			activity.put("slotCode", booking.getSlot().getCode());
			activity.put("status", booking.getStatus());
			activity.put("amount", booking.getAmount());
			activity.put("timestamp", booking.getStartTime().toString());
			activity.put("description", getActivityDescription(booking));
			return activity;
		}).toList();
	}

	private String getActivityType(Booking booking) {
		switch (booking.getStatus()) {
			case "CONFIRMED": return "PAYMENT_COMPLETED";
			case "CANCELLED": return "BOOKING_CANCELLED";
			default: return "BOOKING_CREATED";
		}
	}

	private String getActivityDescription(Booking booking) {
		switch (booking.getStatus()) {
			case "CONFIRMED": 
				return String.format("%s completed payment of ₹%.2f for slot %s", 
					booking.getDriverName(), booking.getAmount(), booking.getSlot().getCode());
			case "CANCELLED":
				return String.format("%s cancelled booking for slot %s", 
					booking.getDriverName(), booking.getSlot().getCode());
			default:
				return String.format("%s created booking for slot %s", 
					booking.getDriverName(), booking.getSlot().getCode());
		}
	}
}



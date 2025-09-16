package com.parkease.parkease_backend.controller;

import com.parkease.parkease_backend.model.Booking;
import com.parkease.parkease_backend.model.Slot;
import com.parkease.parkease_backend.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {

	private final BookingService bookingService;

	public ApiController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@GetMapping("/slots")
	public List<Slot> getAllSlots() {
		return bookingService.getAllSlots();
	}

	@GetMapping("/slots/available")
	public List<Slot> getAvailableSlots() {
		return bookingService.getAvailableSlots();
	}

	@PostMapping("/bookings")
	public ResponseEntity<Booking> createBooking(@Valid @RequestBody CreateBookingRequest req) {
		Booking booking = bookingService.createBooking(
			req.slotId(), req.driverName(), req.vehicleNumber(), req.startTime(), req.endTime());
		return ResponseEntity.ok(booking);
	}

	@GetMapping("/bookings")
	public ResponseEntity<List<Booking>> bookingsByVehicle(@RequestParam(required = false) String vehicleNumber) {
		try {
			List<Booking> bookings = bookingService.getBookingsForVehicle(vehicleNumber);
			return ResponseEntity.ok(bookings);
		} catch (Exception e) {
			System.err.println("Error fetching bookings for vehicle " + vehicleNumber + ": " + e.getMessage());
			return ResponseEntity.ok(List.of()); // Return empty list instead of error
		}
	}

	@GetMapping("/bookings/my")
	public ResponseEntity<List<Booking>> getMyBookings(HttpServletRequest request) {
		try {
			// Extract user info from JWT token or session
			String driverName = (String) request.getAttribute("driverName");
			if (driverName == null) {
				// Fallback to get from localStorage equivalent or return empty
				return ResponseEntity.ok(List.of());
			}
			List<Booking> bookings = bookingService.getBookingsForDriver(driverName);
			return ResponseEntity.ok(bookings);
		} catch (Exception e) {
			System.err.println("Error fetching user bookings: " + e.getMessage());
			return ResponseEntity.ok(List.of());
		}
	}

	// Admin endpoints
	@GetMapping("/admin/bookings")
	public ResponseEntity<List<Booking>> getAllBookings() {
		try {
			System.out.println("Admin bookings endpoint called");
			List<Booking> bookings = bookingService.getAllBookings();
			System.out.println("Returning " + bookings.size() + " bookings");
			return ResponseEntity.ok(bookings);
		} catch (Exception e) {
			System.err.println("Error fetching all bookings: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.ok(List.of());
		}
	}

	@GetMapping("/admin/stats")
	public ResponseEntity<Map<String, Object>> getAdminStats() {
		try {
			System.out.println("Admin stats endpoint called");
			Map<String, Object> stats = bookingService.getAdminStats();
			System.out.println("Stats: " + stats);
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			System.err.println("Error fetching admin stats: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.ok(Map.of());
		}
	}

	@PostMapping("/admin/slots")
	public ResponseEntity<Slot> createSlot(@Valid @RequestBody CreateSlotRequest req) {
		try {
			System.out.println("Admin create slot endpoint called with: " + req);
			Slot slot = bookingService.createSlot(req.code(), req.level(), req.locationDescription(), req.pricePerHour());
			System.out.println("Slot created successfully: " + slot.getCode());
			return ResponseEntity.ok(slot);
		} catch (RuntimeException e) {
			System.err.println("Error creating slot: " + e.getMessage());
			return ResponseEntity.badRequest().body(null);
		} catch (Exception e) {
			System.err.println("Unexpected error creating slot: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/admin/slots/{id}")
	public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
		try {
			bookingService.deleteSlot(id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/admin/users")
	public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
		try {
			List<Map<String, Object>> users = bookingService.getAllUsers();
			return ResponseEntity.ok(users);
		} catch (Exception e) {
			System.err.println("Error fetching users: " + e.getMessage());
			return ResponseEntity.ok(List.of());
		}
	}

	@GetMapping("/admin/payments")
	public ResponseEntity<List<Map<String, Object>>> getPaymentHistory() {
		try {
			List<Map<String, Object>> payments = bookingService.getPaymentHistory();
			return ResponseEntity.ok(payments);
		} catch (Exception e) {
			System.err.println("Error fetching payment history: " + e.getMessage());
			return ResponseEntity.ok(List.of());
		}
	}

	@GetMapping("/admin/activity")
	public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
		try {
			List<Map<String, Object>> activity = bookingService.getRecentActivity();
			return ResponseEntity.ok(activity);
		} catch (Exception e) {
			System.err.println("Error fetching recent activity: " + e.getMessage());
			return ResponseEntity.ok(List.of());
		}
	}

	@DeleteMapping("/bookings/{id}")
	public ResponseEntity<Void> cancel(@PathVariable Long id) {
		bookingService.cancelBooking(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/admin/bookings/{id}/make-available")
	public ResponseEntity<Void> makeSlotAvailable(@PathVariable Long id) {
		try {
			System.out.println("Admin making slot available for booking ID: " + id);
			bookingService.makeSlotAvailable(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			System.err.println("Error making slot available: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/bookings/{id}/confirm-payment")
	public ResponseEntity<Booking> confirm(@PathVariable Long id) {
		try {
			Booking confirmedBooking = bookingService.confirmPayment(id);
			return ResponseEntity.ok(confirmedBooking);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	public record CreateBookingRequest(
		@NotNull Long slotId,
		@NotBlank String driverName,
		@NotBlank String vehicleNumber,
		@NotNull OffsetDateTime startTime,
		@NotNull OffsetDateTime endTime
	) {}

	public record CreateSlotRequest(
		@NotBlank String code,
		@NotNull Integer level,
		@NotBlank String locationDescription,
		@NotNull Double pricePerHour
	) {}
}



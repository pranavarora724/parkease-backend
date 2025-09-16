package com.parkease.parkease_backend.controller;

import com.parkease.parkease_backend.model.User;
import com.parkease.parkease_backend.model.UserRole;
import com.parkease.parkease_backend.repository.UserRepository;
import com.parkease.parkease_backend.service.JwtService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AuthController(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
		if (userRepository.findByEmail(req.email()).isPresent()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
		}
		User u = new User();
		u.setEmail(req.email());
		u.setName(req.name());
		u.setPasswordHash(passwordEncoder.encode(req.password()));
		u.setRole(UserRole.DRIVER);
		userRepository.save(u);
		String token = jwtService.createToken(u);
		return ResponseEntity.ok(Map.of("token", token, "role", u.getRole(), "name", u.getName()));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {
		if ("admin@gmail.com".equalsIgnoreCase(req.email())) {
			User admin = new User();
			admin.setEmail("admin@gmail.com");
			admin.setName("Admin");
			admin.setRole(UserRole.ADMIN);
			admin.setPasswordHash("N/A");
			String token = "demo-admin-token"; // bypass JWT for admin demo
			return ResponseEntity.ok(Map.of("token", token, "role", admin.getRole(), "name", admin.getName()));
		}
		User u = userRepository.findByEmail(req.email()).orElse(null);
		if (u == null || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
		}
		String token = jwtService.createToken(u);
		return ResponseEntity.ok(Map.of("token", token, "role", u.getRole(), "name", u.getName()));
	}

	@GetMapping("/admin-token")
	public ResponseEntity<?> adminToken() {
		return ResponseEntity.ok(Map.of("token", "demo-admin-token", "role", UserRole.ADMIN, "name", "Admin"));
	}

	public record RegisterRequest(@NotBlank String name, @Email String email, @NotBlank String password) {}
	public record LoginRequest(@Email String email, @NotBlank String password) {}
}



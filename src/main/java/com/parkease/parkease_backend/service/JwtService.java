package com.parkease.parkease_backend.service;

import com.parkease.parkease_backend.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

	// Base64 for: my_super_secure_jwt_key_32_bytes_1234567890123456
	@Value("${parkease.jwt.secret:bXlfc3VwZXJfc2VjdXJlX2p3dF9rZXlfMzJfYnl0ZXNfMTIzNDU2Nzg5MDEyMzQ1Ng==}")
	private String secret;

	@Value("${parkease.jwt.ttlMs:86400000}")
	private long ttlMs;

	private Key getKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}

	public String createToken(User user) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + ttlMs);
		String subject = user.getEmail() != null ? user.getEmail() : String.valueOf(user.getId());
		return Jwts.builder()
			.setSubject(subject)
			.addClaims(Map.of("email", user.getEmail(), "role", user.getRole().name(), "name", user.getName()))
			.setIssuedAt(now)
			.setExpiration(exp)
			.signWith(getKey(), SignatureAlgorithm.HS256)
			.compact();
	}
}



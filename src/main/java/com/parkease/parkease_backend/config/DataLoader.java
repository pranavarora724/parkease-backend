package com.parkease.parkease_backend.config;

import com.parkease.parkease_backend.model.Slot;
import com.parkease.parkease_backend.repository.SlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
	@Bean
	CommandLineRunner seedSlots(SlotRepository slotRepository) {
		return args -> {
			if (slotRepository.count() == 0) {
				for (int i = 1; i <= 12; i++) {
					Slot s = new Slot();
					s.setCode("S" + i);
					s.setLevel(1 + (i - 1) / 6);
					s.setLocationDescription("Near pillar " + i);
					s.setPricePerHour(30.0 + (i % 3) * 10);
					s.setAvailable(true);
					slotRepository.save(s);
				}
			}
		};
	}
}




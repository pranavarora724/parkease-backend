-- Sample parking slots for ParkEase
INSERT IGNORE INTO slots (id, code, location_description, level, available, price_per_hour, created_at) VALUES
(1, 'A-101', 'Near Main Entrance', 1, true, 50.0, NOW()),
(2, 'A-102', 'Near Main Entrance', 1, true, 50.0, NOW()),
(3, 'A-103', 'Near Main Entrance', 1, true, 50.0, NOW()),
(4, 'A-104', 'Near Main Entrance', 1, false, 50.0, NOW()),
(5, 'B-201', 'Near Elevator', 2, true, 60.0, NOW()),
(6, 'B-202', 'Near Elevator', 2, true, 60.0, NOW()),
(7, 'B-203', 'Near Elevator', 2, true, 60.0, NOW()),
(8, 'B-204', 'Near Elevator', 2, true, 60.0, NOW()),
(9, 'C-301', 'Premium Section', 3, true, 75.0, NOW()),
(10, 'C-302', 'Premium Section', 3, true, 75.0, NOW()),
(11, 'C-303', 'Premium Section', 3, false, 75.0, NOW()),
(12, 'D-401', 'Rooftop Level', 4, true, 40.0, NOW()),
(13, 'D-402', 'Rooftop Level', 4, true, 40.0, NOW()),
(14, 'D-403', 'Rooftop Level', 4, true, 40.0, NOW()),
(15, 'E-501', 'VIP Section', 5, true, 100.0, NOW());

-- Sample admin user
INSERT IGNORE INTO users (id, email, name, password_hash, role) VALUES
(1, 'admin@gmail.com', 'Admin User', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN');

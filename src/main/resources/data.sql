-- Tạo 3 user mẫu
INSERT INTO users (user_id, full_name, email, password, created_at, is_verified, two_factor_enabled)
VALUES
    ('u1', 'Nguyễn Văn An', 'an@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW(), TRUE, FALSE),
    ('u2', 'Trần Thị Bình', 'binh@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW(), TRUE, FALSE),
    ('u3', 'Lê Văn Cường', 'cuong@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW(), TRUE, FALSE);

-- Tạo 5 đánh giá mẫu
INSERT INTO reviews (user_id, service, content, rating, verified, approved, created_at)
VALUES
    ('u1', 'Nhi khoa', 'Bác sĩ rất tận tâm, giải thích rõ ràng, con tôi khỏi bệnh nhanh.', 5, TRUE, TRUE, '2025-11-01 10:00:00'),
    ('u2', 'Nha khoa', 'Phòng khám sạch sẽ, bác sĩ nhẹ tay, không đau như tôi tưởng.', 4, TRUE, TRUE, '2025-11-02 14:30:00'),
    ('u3', 'Nội khoa', 'Chờ hơi lâu nhưng bác sĩ khám kỹ, kê đơn đúng bệnh.', 4, TRUE, TRUE, '2025-11-03 09:15:00'),
    ('u1', 'Da liễu', 'Đã khám 3 lần, tình trạng da cải thiện rõ rệt. Cảm ơn bác sĩ!', 5, TRUE, TRUE, '2025-11-04 16:00:00'),
    ('u2', 'Sản phụ khoa', 'Bác sĩ tư vấn nhiệt tình, giá hợp lý, sẽ quay lại.', 5, TRUE, TRUE, '2025-11-05 11:20:00');
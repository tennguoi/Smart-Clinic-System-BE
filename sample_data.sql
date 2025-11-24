
-- 3. SERVICES (Top Services Data)
INSERT INTO SERVICES (service_id, name, category, price, is_active, photo_url) VALUES
('S001', 'Khám Tổng Quát', 'Consultation', 200000, 1, 'https://img.freepik.com/free-vector/doctor-character-background_1270-84.jpg'),
('S002', 'Xét Nghiệm Máu', 'Test', 150000, 1, 'https://img.freepik.com/free-vector/blood-test-concept-illustration_114360-1209.jpg'),
('S003', 'Siêu Âm Tim', 'Test', 300000, 1, 'https://img.freepik.com/free-vector/ultrasound-concept-illustration_114360-1210.jpg'),
('S004', 'Nhổ Răng', 'Procedure', 500000, 1, 'https://img.freepik.com/free-vector/dental-care-concept-illustration_114360-1211.jpg'),
('S005', 'Tẩy Trắng Răng', 'Procedure', 1000000, 1, 'https://img.freepik.com/free-vector/teeth-whitening-concept-illustration_114360-1212.jpg');

-- 4. PATIENTS (New Patients & Total Patients)
INSERT INTO patients (patient_id, full_name, phone, dob, gender, created_at) VALUES
(UUID(), 'Nguyen Van A', '0901000001', '1990-01-01', 'male', NOW()), -- New Today
(UUID(), 'Tran Thi B', '0901000002', '1995-02-02', 'female', NOW()), -- New Today
(UUID(), 'Le Van C', '0901000003', '1988-03-03', 'male', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(UUID(), 'Pham Thi D', '0901000004', '2000-04-04', 'female', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(UUID(), 'Hoang Van E', '0901000005', '1985-05-05', 'male', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 5. APPOINTMENTS (Appointments Trend & Today)
-- Status: Pending, Confirmed, Cancelled
INSERT INTO appointments (appointment_id, patient_name, phone, appointment_time, status, created_at, appointment_code) VALUES
-- Today (3 appts)
(UUID(), 'Nguyen Van A', '0901000001', NOW(), 'Pending', NOW(), 'APT-001'),
(UUID(), 'Tran Thi B', '0901000002', DATE_ADD(NOW(), INTERVAL 1 HOUR), 'Confirmed', NOW(), 'APT-002'),
(UUID(), 'Guest Patient', '0999999999', DATE_ADD(NOW(), INTERVAL 2 HOUR), 'Pending', NOW(), 'APT-003'),

-- Yesterday (2 appts)
(UUID(), 'Le Van C', '0901000003', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 1 DAY), 'APT-004'),
(UUID(), 'Pham Thi D', '0901000004', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Cancelled', DATE_SUB(NOW(), INTERVAL 1 DAY), 'APT-005'),

-- Day -2 (4 appts)
(UUID(), 'Hoang Van E', '0901000005', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 2 DAY), 'APT-006'),
(UUID(), 'Nguyen Van A', '0901000001', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 2 DAY), 'APT-007'),
(UUID(), 'Tran Thi B', '0901000002', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 2 DAY), 'APT-008'),
(UUID(), 'Guest 2', '0988888888', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Cancelled', DATE_SUB(NOW(), INTERVAL 2 DAY), 'APT-009'),

-- Day -3 to -6 (Random)
(UUID(), 'Le Van C', '0901000003', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 3 DAY), 'APT-010'),
(UUID(), 'Pham Thi D', '0901000004', DATE_SUB(NOW(), INTERVAL 4 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 4 DAY), 'APT-011'),
(UUID(), 'Hoang Van E', '0901000005', DATE_SUB(NOW(), INTERVAL 5 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 5 DAY), 'APT-012'),
(UUID(), 'Nguyen Van A', '0901000001', DATE_SUB(NOW(), INTERVAL 6 DAY), 'Confirmed', DATE_SUB(NOW(), INTERVAL 6 DAY), 'APT-013');

-- 6. INVOICES & DETAILS (Revenue Trend & Top Services)
-- Note: Using hardcoded UUIDs for invoices to link details easily in this script.
-- In reality, you'd select the IDs.

-- Invoice 1: Today - 350k (S001 + S002)
INSERT INTO invoices (invoice_id, invoice_code, patient_id, created_by, created_at, total_amount, paid_amount, status) 
SELECT UUID(), 'INV-001', p.patient_id, '11111111-1111-1111-1111-111111111111', NOW(), 350000, 350000, 'PAID'
FROM patients p WHERE p.phone = '0901000001' LIMIT 1;

INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S001', 1, 200000, 200000 FROM invoices i WHERE i.invoice_code = 'INV-001';
INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S002', 1, 150000, 150000 FROM invoices i WHERE i.invoice_code = 'INV-001';

-- Invoice 2: Yesterday - 500k (S004)
INSERT INTO invoices (invoice_id, invoice_code, patient_id, created_by, created_at, total_amount, paid_amount, status) 
SELECT UUID(), 'INV-002', p.patient_id, '11111111-1111-1111-1111-111111111111', DATE_SUB(NOW(), INTERVAL 1 DAY), 500000, 500000, 'PAID'
FROM patients p WHERE p.phone = '0901000003' LIMIT 1;

INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S004', 1, 500000, 500000 FROM invoices i WHERE i.invoice_code = 'INV-002';

-- Invoice 3: Day -2 - 1.3M (S003 + S005)
INSERT INTO invoices (invoice_id, invoice_code, patient_id, created_by, created_at, total_amount, paid_amount, status) 
SELECT UUID(), 'INV-003', p.patient_id, '11111111-1111-1111-1111-111111111111', DATE_SUB(NOW(), INTERVAL 2 DAY), 1300000, 1300000, 'PAID'
FROM patients p WHERE p.phone = '0901000005' LIMIT 1;

INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S003', 1, 300000, 300000 FROM invoices i WHERE i.invoice_code = 'INV-003';
INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S005', 1, 1000000, 1000000 FROM invoices i WHERE i.invoice_code = 'INV-003';

-- Invoice 4: Day -3 - 200k (S001)
INSERT INTO invoices (invoice_id, invoice_code, patient_id, created_by, created_at, total_amount, paid_amount, status) 
SELECT UUID(), 'INV-004', p.patient_id, '11111111-1111-1111-1111-111111111111', DATE_SUB(NOW(), INTERVAL 3 DAY), 200000, 200000, 'PAID'
FROM patients p WHERE p.phone = '0901000003' LIMIT 1;

INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S001', 1, 200000, 200000 FROM invoices i WHERE i.invoice_code = 'INV-004';

-- Invoice 5: Day -4 - 150k (S002)
INSERT INTO invoices (invoice_id, invoice_code, patient_id, created_by, created_at, total_amount, paid_amount, status) 
SELECT UUID(), 'INV-005', p.patient_id, '11111111-1111-1111-1111-111111111111', DATE_SUB(NOW(), INTERVAL 4 DAY), 150000, 150000, 'PAID'
FROM patients p WHERE p.phone = '0901000004' LIMIT 1;

INSERT INTO invoice_details (invoice_id, service_id, quantity, unit_price, amount)
SELECT i.invoice_id, 'S002', 1, 150000, 150000 FROM invoices i WHERE i.invoice_code = 'INV-005';

-- V2__Seed_Data.sql
-- Seed core catalogue data so the booking flow is functional out of the box.
-- Idempotent: safe to keep in the migration history (uses fixed UUIDs + ON CONFLICT).

-- Services -------------------------------------------------------------------
INSERT INTO services (id, name, description, duration_minutes, price, is_active) VALUES
    ('5e000001-0000-4000-8000-000000000001', '2KCUT Signature',  'Precision hair styling paired with a traditional hot-towel straight-razor finish.', 60, 5000.00, TRUE),
    ('5e000001-0000-4000-8000-000000000002', 'Classic Haircut',  'Tailored cut and style to suit your face shape and lifestyle.',                    30, 2500.00, TRUE),
    ('5e000001-0000-4000-8000-000000000003', 'Beard Sculpting',  'Expert beard shaping with premium oils and detailing.',                            30, 2000.00, TRUE),
    ('5e000001-0000-4000-8000-000000000004', 'Hot Towel Shave',  'Traditional straight-razor shave with hot towels and essential oils.',             30, 2200.00, TRUE),
    ('5e000001-0000-4000-8000-000000000005', 'Hair & Beard Combo','Full grooming: precision cut plus beard sculpting.',                              75, 6500.00, TRUE),
    ('5e000001-0000-4000-8000-000000000006', 'Kids Cut',         'Patient, friendly cuts for the youngest gentlemen (under 12).',                    20, 1500.00, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Barber profile users -------------------------------------------------------
-- Barbers are modelled as users (one-to-one). These rows are profile holders.
INSERT INTO users (id, email, first_name, last_name, phone_number) VALUES
    ('b0000001-0000-4000-8000-000000000001', 'kamal@2kcut.lk', 'Kamal', 'Perera',   '0777603514'),
    ('b0000001-0000-4000-8000-000000000002', 'nuwan@2kcut.lk', 'Nuwan', 'Silva',    '0760948345'),
    ('b0000001-0000-4000-8000-000000000003', 'ravi@2kcut.lk',  'Ravi',  'Fernando', NULL)
ON CONFLICT (id) DO NOTHING;

-- Barbers --------------------------------------------------------------------
INSERT INTO barbers (id, user_id, bio, profile_image_url, is_active) VALUES
    ('ba000001-0000-4000-8000-000000000001', 'b0000001-0000-4000-8000-000000000001', 'Master barber with 20+ years redefining grooming in Galle.', '/images/barber.png', TRUE),
    ('ba000001-0000-4000-8000-000000000002', 'b0000001-0000-4000-8000-000000000002', 'Specialist in modern fades and contemporary styling.',       '/images/barber.png', TRUE),
    ('ba000001-0000-4000-8000-000000000003', 'b0000001-0000-4000-8000-000000000003', 'Classic shaves and beard sculpting craftsman.',              '/images/barber.png', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Barber schedules -----------------------------------------------------------
-- day_of_week: 1 (Mon) .. 7 (Sun). Mirrors the salon's published opening hours.
INSERT INTO barber_schedules (barber_id, day_of_week, start_time, end_time, is_working_day)
SELECT b.id, d.day_of_week, d.start_time, d.end_time, d.is_working_day
FROM barbers b
CROSS JOIN (VALUES
    (1, TIME '09:00', TIME '20:00', TRUE),
    (2, TIME '09:00', TIME '20:00', TRUE),
    (3, TIME '09:00', TIME '20:00', TRUE),
    (4, TIME '09:00', TIME '20:00', TRUE),
    (5, TIME '09:00', TIME '20:00', TRUE),
    (6, TIME '08:30', TIME '21:00', TRUE),
    (7, TIME '09:00', TIME '18:00', TRUE)
) AS d(day_of_week, start_time, end_time, is_working_day)
WHERE b.id IN (
    'ba000001-0000-4000-8000-000000000001',
    'ba000001-0000-4000-8000-000000000002',
    'ba000001-0000-4000-8000-000000000003'
)
ON CONFLICT (barber_id, day_of_week) DO NOTHING;

-- MySQL-compatible seed data (INSERT IGNORE prevents duplicates on restart)

INSERT IGNORE INTO students (name, email) VALUES
('Alice Johnson', 'alice.johnson@example.com'),
('Bob Smith', 'bob.smith@example.com'),
('Charlie Brown', 'charlie.brown@example.com'),
('Diana Prince', 'diana.prince@example.com'),
('Ethan Hunt', 'ethan.hunt@example.com');

INSERT IGNORE INTO events (name, description, event_date, event_time, department, type, location, capacity) VALUES
('Spring Boot Workshop', 'A comprehensive hands-on workshop on building production-ready web applications with Spring Boot, Spring Data JPA, and Thymeleaf.', '2026-05-15', '10:00:00', 'Computer Science', 'Workshop', 'CS Lab 301', 40),
('Annual Tech Symposium', 'University-wide technology symposium featuring guest speakers from leading tech companies, panel discussions, and networking sessions.', '2026-06-10', '09:00:00', 'Engineering', 'Symposium', 'Main Auditorium', 200),
('Campus Hackathon 2026', 'A 48-hour coding marathon to build innovative solutions for real campus problems. Open to all departments. Prizes worth 50K.', '2026-07-20', '08:00:00', 'Computer Science', 'Hackathon', 'Innovation Hub', 100),
('Art & Design Expo', 'Annual exhibition showcasing outstanding student artwork, digital designs, and installations from across the Fine Arts department.', '2026-08-05', '11:00:00', 'Fine Arts', 'Exhibition', 'Gallery Hall', 150),
('Data Science Masterclass', 'Deep dive into machine learning fundamentals, data visualization, and practical applications using Python and real-world datasets.', '2026-05-28', '14:00:00', 'Computer Science', 'Workshop', 'CS Lab 204', 35),
('Cultural Fest 2026', 'Three-day campus cultural festival with performances, competitions, food stalls and entertainment. The biggest annual student event.', '2026-09-15', '16:00:00', 'Student Affairs', 'Cultural', 'Open Grounds', 500),
('Research Paper Writing Seminar', 'Learn the art of writing publishable research papers. Covers structure, citations, peer review process, and journal selection.', '2026-06-02', '13:00:00', 'Library Sciences', 'Seminar', 'Seminar Hall B', 60),
('Inter-College Cricket Tournament', 'Annual inter-college cricket tournament. Teams from 8 colleges compete over 5 days. Registrations open for players and volunteers.', '2026-10-01', '07:00:00', 'Sports', 'Sports', 'University Stadium', 120);

INSERT IGNORE INTO registrations (student_id, event_id, registration_date, attended, qr_code_data) VALUES
(1, 1, NOW(), false, 'REG:1|USER:1|EVENT:1'),
(1, 2, NOW(), false, 'REG:2|USER:1|EVENT:2'),
(2, 1, NOW(), false, 'REG:3|USER:2|EVENT:1'),
(3, 3, NOW(), false, 'REG:4|USER:3|EVENT:3'),
(4, 2, NOW(), false, 'REG:5|USER:4|EVENT:2'),
(4, 4, NOW(), false, 'REG:6|USER:4|EVENT:4'),
(5, 3, NOW(), false, 'REG:7|USER:5|EVENT:3'),
(2, 5, NOW(), false, 'REG:8|USER:2|EVENT:5'),
(3, 6, NOW(), false, 'REG:9|USER:3|EVENT:6');

-- Seed gamification points
INSERT IGNORE INTO student_points (student_id, total_points, badges) VALUES
(1, 30, 'Active Participant'),
(2, 20, ''),
(3, 15, ''),
(4, 20, ''),
(5, 10, '');

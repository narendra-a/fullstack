INSERT INTO students (name, email) VALUES
('Alice Johnson', 'alice.johnson@example.com'),
('Bob Smith', 'bob.smith@example.com'),
('Charlie Brown', 'charlie.brown@example.com'),
('Diana Prince', 'diana.prince@example.com'),
('Ethan Hunt', 'ethan.hunt@example.com');

INSERT INTO events (name, description, event_date, department, type, location, capacity) VALUES
('Spring Boot Workshop', 'A comprehensive hands-on workshop on building production-ready web applications with Spring Boot, Spring Data JPA, and Thymeleaf.', '2026-05-15', 'Computer Science', 'Workshop', 'CS Lab 301', 40),
('Annual Tech Symposium', 'University-wide technology symposium featuring guest speakers from leading tech companies, panel discussions, and networking sessions.', '2026-06-10', 'Engineering', 'Symposium', 'Main Auditorium', 200),
('Campus Hackathon 2026', 'A 48-hour coding marathon to build innovative solutions for real campus problems. Open to all departments. Prizes worth 50K.', '2026-07-20', 'Computer Science', 'Hackathon', 'Innovation Hub', 100),
('Art & Design Expo', 'Annual exhibition showcasing outstanding student artwork, digital designs, and installations from across the Fine Arts department.', '2026-08-05', 'Fine Arts', 'Exhibition', 'Gallery Hall', 150),
('Data Science Masterclass', 'Deep dive into machine learning fundamentals, data visualization, and practical applications using Python and real-world datasets.', '2026-05-28', 'Computer Science', 'Workshop', 'CS Lab 204', 35),
('Cultural Fest 2026', 'Three-day campus cultural festival with performances, competitions, food stalls and entertainment. The biggest annual student event.', '2026-09-15', 'Student Affairs', 'Cultural', 'Open Grounds', 500),
('Research Paper Writing Seminar', 'Learn the art of writing publishable research papers. Covers structure, citations, peer review process, and journal selection.', '2026-06-02', 'Library Sciences', 'Seminar', 'Seminar Hall B', 60),
('Inter-College Cricket Tournament', 'Annual inter-college cricket tournament. Teams from 8 colleges compete over 5 days. Registrations open for players and volunteers.', '2026-10-01', 'Sports', 'Sports', 'University Stadium', 120);

INSERT INTO registrations (student_id, event_id, registration_date) VALUES
(1, 1, CURRENT_TIMESTAMP),
(1, 2, CURRENT_TIMESTAMP),
(2, 1, CURRENT_TIMESTAMP),
(3, 3, CURRENT_TIMESTAMP),
(4, 2, CURRENT_TIMESTAMP),
(4, 4, CURRENT_TIMESTAMP),
(5, 3, CURRENT_TIMESTAMP),
(2, 5, CURRENT_TIMESTAMP),
(3, 6, CURRENT_TIMESTAMP);

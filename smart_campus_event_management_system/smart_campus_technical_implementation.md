# Smart Campus Event Management System — Detailed Technical Implementation

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Architecture](#3-project-architecture)
4. [Database Design](#4-database-design)
5. [Domain / Entity Layer](#5-domain--entity-layer)
6. [Repository Layer](#6-repository-layer)
7. [Service Layer](#7-service-layer)
8. [Controller Layer](#8-controller-layer)
9. [Security Configuration](#9-security-configuration)
10. [View Layer — Thymeleaf Templates](#10-view-layer--thymeleaf-templates)
11. [Application Configuration](#11-application-configuration)
12. [Seed Data](#12-seed-data)
13. [Exception Handling](#13-exception-handling)
14. [Request Flow Diagrams](#14-request-flow-diagrams)
15. [Data Flow & Business Rules](#15-data-flow--business-rules)
16. [How to Run](#16-how-to-run)

---

## 1. Project Overview

The **Smart Campus Event Management System** is a full-stack web application built with **Spring Boot 3.3** that allows:

- **Students** — browse events on a public landing page, self-register/login via a lightweight session (name + email), one-click register/cancel for events, and view their personal event list.
- **Administrators** — log in via Spring Security form auth, create/edit/delete events with full validation, filter the event list, and view registration analytics.

The app runs **entirely in-memory** (H2 database) making it zero-config for demonstration and development.

---

## 2. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.0 |
| Web | Spring MVC | (included in Boot) |
| Persistence | Spring Data JPA + Hibernate | (included in Boot) |
| Database | H2 (in-memory) | (runtime) |
| Security | Spring Security 6 | (included in Boot) |
| Templating | Thymeleaf + thymeleaf-extras-springsecurity6 | (included in Boot) |
| Validation | Jakarta Bean Validation (Hibernate Validator) | (included in Boot) |
| Build | Maven | 3.x |
| Utilities | Lombok | optional |

### Dependencies (`pom.xml`)
```xml
spring-boot-starter-data-jpa       <!-- JPA + Hibernate ORM -->
spring-boot-starter-security       <!-- Spring Security 6 -->
spring-boot-starter-thymeleaf      <!-- Server-side template engine -->
spring-boot-starter-validation     <!-- Bean Validation -->
spring-boot-starter-web            <!-- Spring MVC + Embedded Tomcat -->
thymeleaf-extras-springsecurity6   <!-- sec:authorize tags in templates -->
h2                                 <!-- In-memory RDBMS (runtime scope) -->
lombok                             <!-- Boilerplate reduction (optional) -->
spring-boot-starter-test           <!-- JUnit 5, Mockito -->
spring-security-test               <!-- MockMvc security testing -->
```

---

## 3. Project Architecture

The project follows the **classic MVC layered architecture**:

```
┌──────────────────────────────────────────────────────────────┐
│                        BROWSER / CLIENT                       │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTP Request
                             ▼
┌──────────────────────────────────────────────────────────────┐
│               SPRING SECURITY FILTER CHAIN                    │
│  • Permit public routes (/, /student/**, /events/**)         │
│  • Require ROLE_ADMIN for /admin/**                          │
│  • Session-based form login for admins                       │
└────────────────────────────┬─────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                           │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────┐  │
│  │ StudentController│  │ AdminController   │  │AuthControll│  │
│  │ (session auth)  │  │ (@RequestMapping  │  │ er (login) │  │
│  └────────┬────────┘  │  /admin)          │  └────────────┘  │
│           │           └──────────┬───────┘                   │
└───────────┼──────────────────────┼───────────────────────────┘
            │                      │
            ▼                      ▼
┌──────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                            │
│         ┌─────────────────┐  ┌──────────────────┐            │
│         │   EventService  │  │RegistrationService│            │
│         └────────┬────────┘  └────────┬─────────┘            │
└──────────────────┼───────────────────┼──────────────────────┘
                   │                   │
                   ▼                   ▼
┌──────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                           │
│   ┌──────────────┐  ┌────────────────────┐  ┌─────────────┐  │
│   │EventRepository│  │RegistrationRepo    │  │StudentRepo  │  │
│   │ (JpaRepository│  │ (JpaRepository +   │  │ (JpaRepo +  │  │
│   │  + @Query)   │  │  custom @Queries)  │  │ findByEmail)│  │
│   └──────────────┘  └────────────────────┘  └─────────────┘  │
└───────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────┐
│               H2 IN-MEMORY DATABASE                           │
│   Tables: STUDENTS | EVENTS | REGISTRATIONS                  │
└──────────────────────────────────────────────────────────────┘
            │
            ▼
┌──────────────────────────────────────────────────────────────┐
│               THYMELEAF VIEW LAYER (HTML)                     │
│   12 templates: index, student_dashboard, event_detail,      │
│   register, my_events, admin_events, admin_event_form,       │
│   admin_stats, login, student_login, register, error, layout │
└──────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.smartcampus
├── SmartCampusApplication.java         ← @SpringBootApplication entry point
├── config/
│   └── SecurityConfig.java             ← Spring Security 6 filter chain
├── controller/
│   ├── AuthController.java             ← GET /login (admin login page)
│   ├── StudentController.java          ← Student portal (session-based)
│   └── AdminController.java            ← Admin CRUD (/admin/**)
├── entity/
│   ├── Event.java                      ← @Entity: events table
│   ├── Student.java                    ← @Entity: students table
│   └── Registration.java               ← @Entity: registrations (join table)
├── repository/
│   ├── EventRepository.java            ← JpaRepository + JPQL @Query
│   ├── StudentRepository.java          ← JpaRepository + findByEmail
│   └── RegistrationRepository.java     ← JpaRepository + custom @Queries
├── service/
│   ├── EventService.java               ← Business logic for events
│   └── RegistrationService.java        ← Registration & capacity business logic
└── exception/
    └── GlobalExceptionHandler.java     ← @ControllerAdvice for all exceptions
```

---

## 4. Database Design

### Entity-Relationship Diagram

```
┌────────────────┐        ┌──────────────────────┐        ┌──────────────────┐
│   STUDENTS     │        │    REGISTRATIONS      │        │     EVENTS       │
├────────────────┤        ├──────────────────────┤        ├──────────────────┤
│ id (PK, AUTO) │◄───────│ student_id (FK)       │───────►│ id (PK, AUTO)   │
│ name NOT NULL  │        │ event_id   (FK)       │        │ name NOT NULL    │
│ email UNIQUE   │        │ registration_date     │        │ description      │
└────────────────┘        │ id (PK, AUTO)         │        │ event_date       │
                          └──────────────────────┘        │ department       │
                                                           │ type             │
                                                           │ location         │
                                                           │ capacity (INT)   │
                                                           └──────────────────┘
```

### Relationships
- `Student` ↔ `Event`: **Many-to-Many** implemented via the `Registration` join entity (not a @ManyToMany annotation — it's a proper join entity with extra field `registration_date`).
- `Registration.student`: `@ManyToOne(fetch = FetchType.LAZY)` — avoids N+1 on list queries.
- `Registration.event`: `@ManyToOne(fetch = FetchType.LAZY)`.

### Key Constraints
| Constraint | Implementation |
|---|---|
| Unique student email | `@Column(unique = true)` on `Student.email` |
| Duplicate registration prevention | `existsByEventAndStudentEmail()` check in service before save |
| Capacity enforcement | `countByEvent(event) >= event.getCapacity()` check in service |
| Auto-timestamp on registration | `@PrePersist` callback sets `registrationDate = LocalDateTime.now()` |

---

## 5. Domain / Entity Layer

### `Event.java`
```java
@Entity @Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min=3, max=100)  private String name;
    @NotBlank @Size(max=500)         private String description;
    @NotNull                         private LocalDate eventDate;
    @NotBlank                        private String department;
    @NotBlank                        private String type;
                                     private String location;
    @Min(1)                          private Integer capacity;
    // ... getters/setters
}
```

**Key design decisions:**
- `capacity` is `Integer` (nullable) — `null` means unlimited.
- `type` is a free `String` (e.g., "Workshop", "Hackathon", "Cultural") — no enum, giving flexibility for admin to add new types.
- `eventDate` is `LocalDate` (date-only, no time component).

### `Student.java`
```java
@Entity @Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank                    private String name;
    @NotBlank @Email
    @Column(unique = true)       private String email;
    // ... getters/setters
}
```

**Design note:** Students are persisted on first registration. If a student re-uses the same email, the existing record is reused (`findByEmail().orElseGet(() -> save(student))`).

### `Registration.java`
```java
@Entity @Table(name = "registrations")
public class Registration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private LocalDateTime registrationDate;

    @PrePersist
    public void prePersist() {
        this.registrationDate = LocalDateTime.now();
    }
}
```

---

## 6. Repository Layer

### `EventRepository`
```java
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:date IS NULL OR e.eventDate >= :date)")
    List<Event> findByFilters(@Param("department") String department,
                              @Param("type")       String type,
                              @Param("date")       LocalDate date);
}
```
- **JPQL dynamic filter** — null parameters are treated as "no filter" using the `IS NULL OR` pattern. Avoids the complexity of `Specification`/`Criteria API` for this use case.

### `StudentRepository`
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
}
```

### `RegistrationRepository`
```java
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentEmail(String email);
    boolean existsByEventAndStudentEmail(Event event, String email);
    long countByEvent(Event event);

    @Query("SELECT r.event.id FROM Registration r WHERE r.student.email = :email")
    List<Long> findRegisteredEventIdsByEmail(@Param("email") String email);

    @Query("SELECT r.event.name, COUNT(r) as cnt FROM Registration r " +
           "GROUP BY r.event.name ORDER BY cnt DESC")
    List<Object[]> countRegistrationsPerEvent();

    @Query("SELECT r.event.id, COUNT(r) FROM Registration r GROUP BY r.event.id")
    List<Object[]> countRegistrationsGroupedByEventId();
}
```

**Key queries explained:**
| Method | Purpose |
|---|---|
| `findByStudentEmail` | Get all registrations for "My Events" page — Spring Data derived query |
| `existsByEventAndStudentEmail` | Duplicate check before inserting a registration |
| `countByEvent` | Capacity enforcement: compare against `event.capacity` |
| `findRegisteredEventIdsByEmail` | Returns `List<Long>` of event IDs — used in templates to show "Registered" badge |
| `countRegistrationsPerEvent` | Stats page: per-event counts ordered by popularity |
| `countRegistrationsGroupedByEventId` | Admin table: show seat count alongside each event |

---

## 7. Service Layer

### `EventService`
```java
@Service
public class EventService {
    @Autowired private EventRepository eventRepository;

    public List<Event> getAllEvents() { return eventRepository.findAll(); }

    public List<Event> findEventsFiltered(String department, String type, LocalDate date) {
        if (department != null && department.isEmpty()) department = null;
        if (type != null && type.isEmpty()) type = null;
        return eventRepository.findByFilters(department, type, date);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event saveEvent(Event event) { return eventRepository.save(event); }
    public void deleteEvent(Long id) { eventRepository.deleteById(id); }
}
```

### `RegistrationService` — Core Business Logic

```java
@Service
public class RegistrationService {

    public void registerStudentForEvent(Long eventId, Student studentInput) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Rule 1: Duplicate registration guard
        if (registrationRepository.existsByEventAndStudentEmail(event, studentInput.getEmail())) {
            throw new RuntimeException("You are already registered for this event.");
        }

        // Rule 2: Capacity enforcement
        if (event.getCapacity() != null) {
            long currentCount = registrationRepository.countByEvent(event);
            if (currentCount >= event.getCapacity()) {
                throw new RuntimeException("This event is full. No more seats available.");
            }
        }

        // Rule 3: Upsert student — reuse existing record if email found
        Student student = studentRepository.findByEmail(studentInput.getEmail())
                .orElseGet(() -> studentRepository.save(studentInput));

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setStudent(student);
        registrationRepository.save(registration);
    }

    public void cancelRegistration(Long eventId, String email) {
        // Find the specific registration by email + event, then delete
        List<Registration> registrations = registrationRepository.findByStudentEmail(email);
        Registration target = registrations.stream()
                .filter(r -> r.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registration not found."));
        registrationRepository.delete(target);
    }

    public Map<Long, Long> getRegistrationCountsPerEvent() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : registrationRepository.countRegistrationsGroupedByEventId()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }
    // ... other methods
}
```

**Business rules enforced by `RegistrationService`:**
1. **Duplicate guard** — a student cannot register twice for the same event.
2. **Capacity check** — registrations blocked when `count >= capacity`.
3. **Student upsert** — if the email already exists in `STUDENTS`, the existing row is reused (no duplicates). Otherwise a new student is persisted.
4. **Atomic cancel** — finds and deletes the specific `Registration` record.

---

## 8. Controller Layer

### `AuthController` — Admin Login Page
```java
@Controller
public class AuthController {
    @GetMapping("/login")
    public String login() { return "login"; }
}
```
Simply renders the admin login form. The actual authentication POST is handled transparently by Spring Security's `UsernamePasswordAuthenticationFilter`.

---

### `StudentController` — Student Portal (285 lines)

Uses **HTTP Session** for lightweight student authentication (name + email pair — no password required).

#### Session Constants
```java
private static final String SESSION_STUDENT_NAME  = "studentName";
private static final String SESSION_STUDENT_EMAIL = "studentEmail";
```

#### Endpoint Map

| Method | URL | Auth | Description |
|---|---|---|---|
| GET | `/` | Public | Landing page — all events + registered IDs for session user |
| GET | `/student/login` | Public | Render login form; redirect to dashboard if already in session |
| POST | `/student/login` | Public | Validate name+email, store in session, redirect to dashboard |
| GET | `/student/logout` | Student | Remove session attributes, redirect to `/` |
| GET | `/student/dashboard` | Student (session) | Dashboard with stats, filtered events, registered IDs |
| GET | `/events/{id}` | Public | Event detail page with capacity info + registration status |
| GET | `/events/register/{id}` | Public/Student | If session exists → one-click register & redirect; else show form |
| POST | `/student/register` | Public (form) | Guest registration form submission |
| POST | `/student/cancel-registration` | Student (session) | Cancel a registration |
| GET | `/student/my-events` | Student/Public | View registrations by email (session or query param) |

#### Key Implementation — One-Click vs. Form Registration
```java
@GetMapping("/events/register/{id}")
public String showRegistrationForm(@PathVariable Long id, HttpSession session, ...) {
    String sessionName  = (String) session.getAttribute(SESSION_STUDENT_NAME);
    String sessionEmail = (String) session.getAttribute(SESSION_STUDENT_EMAIL);

    if (sessionName != null && sessionEmail != null) {
        // SIGNED-IN PATH: skip the form, register directly
        try {
            Student student = new Student();
            student.setName(sessionName);
            student.setEmail(sessionEmail);
            registrationService.registerStudentForEvent(id, student);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            return "redirect:/student/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/dashboard";
        }
    }
    // GUEST PATH: show registration form
    model.addAttribute("event", eventService.getEventById(id));
    model.addAttribute("student", new Student());
    return "register";
}
```

#### Dashboard Stats Calculation
The dashboard computes live stats from the event list in the controller:
```java
long workshopsCount = allEvents.stream()
    .filter(e -> "Workshop".equals(e.getType()) || "Seminar".equals(e.getType()))
    .count();
long hackathonCount = allEvents.stream()
    .filter(e -> "Hackathon".equals(e.getType()) || "Cultural".equals(e.getType()))
    .count();
long deptCount = allEvents.stream()
    .map(Event::getDepartment)
    .filter(d -> d != null && !d.isBlank())
    .distinct().count();
```

---

### `AdminController` — Admin Event Management

Secured at URL level (`/admin/**` → `ROLE_ADMIN`).

| Method | URL | Description |
|---|---|---|
| GET | `/admin/events` | List events with optional `?department=&type=&date=` filters |
| GET | `/admin/events/new` | Show blank event creation form |
| POST | `/admin/events` | Save/update event (Bean Validation with `@Valid`) |
| GET | `/admin/events/edit/{id}` | Pre-populate form with existing event data |
| GET | `/admin/events/delete/{id}` | Delete event by ID, redirect to list |
| GET | `/admin/stats` | Registration analytics page |

**Form validation** uses `@Valid` + `BindingResult`:
```java
@PostMapping("/admin/events")
public String saveEvent(@Valid @ModelAttribute("event") Event event, BindingResult result) {
    if (result.hasErrors()) return "admin_event_form";  // re-render form with errors
    eventService.saveEvent(event);
    return "redirect:/admin/events";
}
```

---

## 9. Security Configuration

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                          // Disabled for simplicity
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2 console iframe
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/", "/events/**", "/student/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/admin/events", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
```

### Two-Role Authentication Model
| Actor | Authentication | Security |
|---|---|---|
| **Admin** | Spring Security in-memory user (`admin`/`admin123`, `ROLE_ADMIN`) defined in `application.properties` | Full Spring Security session, `ROLE_ADMIN` enforced at URL level |
| **Student** | Custom lightweight session (`studentName`, `studentEmail` attributes) | No password, no Spring Security — just `HttpSession` |

> [!NOTE]
> The student auth is intentionally a "soft login" — no passwords, no Spring Security — modelling a campus SSO where identity is trusted. Only admin routes need real security.

### Admin Credentials (application.properties)
```properties
spring.security.user.name=admin
spring.security.user.password=admin123
spring.security.user.roles=ADMIN
```

---

## 10. View Layer — Thymeleaf Templates

### Template List

| Template | Route | Role |
|---|---|---|
| `index.html` | `/` | Public landing page — event cards grid |
| `student_login.html` | `/student/login` | Student name+email login form |
| `student_dashboard.html` | `/student/dashboard` | Protected student portal with stats & event listing |
| `event_detail.html` | `/events/{id}` | Full event detail with capacity & register/cancel CTA |
| `register.html` | `/events/register/{id}` | Guest registration form |
| `my_events.html` | `/student/my-events` | List of student's registered events |
| `login.html` | `/login` | Admin Spring Security login form |
| `admin_events.html` | `/admin/events` | Admin event table with filter and CRUD actions |
| `admin_event_form.html` | `/admin/events/new` `/admin/events/edit/{id}` | Create/Edit event form |
| `admin_stats.html` | `/admin/stats` | Registration analytics table |
| `layout.html` | (fragment) | Shared navbar fragment |
| `error.html` | (error) | Global error display |

### Key Thymeleaf Patterns

**Conditional "Registered" badge using pre-fetched ID list:**
```html
<!-- In the controller, registeredEventIds is a List<Long> -->
<span th:if="${#lists.contains(registeredEventIds, event.id)}" class="badge-registered">
    ✓ Registered
</span>
```

**Seats remaining calculation in templates:**
```html
<span th:if="${event.capacity != null}">
    [[${event.capacity - (regCounts[event.id] ?: 0)}]] seats left
</span>
```

**Flash message handling:**
```html
<div th:if="${successMessage}" class="alert-success" th:text="${successMessage}"></div>
<div th:if="${errorMessage}"   class="alert-error"   th:text="${errorMessage}"></div>
```

**Admin-only elements using Spring Security dialect:**
```html
<div sec:authorize="isAuthenticated()"> <!-- admin nav items --> </div>
```

**Form binding with validation errors:**
```html
<input type="text" th:field="*{name}" />
<span th:errors="*{name}" class="error-text"></span>
```

---

## 11. Application Configuration

### `application.properties`
```properties
# Application name
spring.application.name=event-management

# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:smartcampusdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# H2 Web Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Hibernate DDL
spring.jpa.hibernate.ddl-auto=update     ← Creates/updates schema automatically
spring.jpa.show-sql=true                 ← Logs all SQL to console
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.org.springframework.web=INFO
logging.level.org.springframework.security=INFO

# Admin credentials (in-memory)
spring.security.user.name=admin
spring.security.user.password=admin123
spring.security.user.roles=ADMIN

# Ensure data.sql runs AFTER Hibernate creates the schema
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

> [!IMPORTANT]
> `spring.jpa.defer-datasource-initialization=true` is **critical**. Without it, `data.sql` runs before Hibernate creates the tables and inserts fail.

---

## 12. Seed Data

`src/main/resources/data.sql` is automatically executed on startup:

### Students (5)
```sql
INSERT INTO students (name, email) VALUES
('Alice Johnson', 'alice.johnson@example.com'),
('Bob Smith', 'bob.smith@example.com'), ...
```

### Events (8)
| Event | Type | Capacity |
|---|---|---|
| Spring Boot Workshop | Workshop | 40 |
| Annual Tech Symposium | Symposium | 200 |
| Campus Hackathon 2026 | Hackathon | 100 |
| Art & Design Expo | Exhibition | 150 |
| Data Science Masterclass | Workshop | 35 |
| Cultural Fest 2026 | Cultural | 500 |
| Research Paper Writing Seminar | Seminar | 60 |
| Inter-College Cricket Tournament | Sports | 120 |

### Pre-Seeded Registrations (9)
Registrations connecting students to events, giving realistic data for the stats dashboard from first launch.

---

## 13. Exception Handling

### `GlobalExceptionHandler`
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";   // → error.html
    }
}
```

The `error.html` template displays the message from any unhandled exception. Business exceptions (duplicate registrations, full events, event not found) are thrown as `RuntimeException` from the service layer and caught either:
1. **In the controller** with `try/catch` → flash message redirect (graceful UX).
2. **By GlobalExceptionHandler** → error page (safety net).

---

## 14. Request Flow Diagrams

### Student Registration Flow
```
Student clicks "Register" on dashboard
            │
            ▼
GET /events/register/{id}
            │
    ┌───────┴────────┐
    │               │
Session exists?  No session
    │               │
    ▼               ▼
One-click      Show register.html
register       (guest form)
    │               │
    ▼               ▼
POST /student/  POST /student/
register        register
        │
        ▼
RegistrationService.registerStudentForEvent()
        │
   ┌────┴────────────────────────┐
   │                             │
Already       Capacity           OK
Registered?   Full?
   │              │               │
   ▼              ▼               ▼
RuntimeEx    RuntimeEx      Save Registration
   │              │               │
   └──────────────┘               ▼
          │               Flash "Success"
          ▼               Redirect → dashboard
   Flash "Error"
   Redirect → dashboard
```

### Admin Event Lifecycle
```
Admin logs in               →  Spring Security validates credentials
Admin → /admin/events       →  AdminController.listEvents() + optional filters
Admin → /admin/events/new   →  AdminController.createEventForm() → blank form
Admin fills form + submits  →  POST /admin/events
                                 │
                           @Valid validation
                            ┌────┴────┐
                         Errors     Pass
                            │          │
                     Re-render    eventService.saveEvent(event)
                     form with    → eventRepository.save(event)
                     errors       → Redirect /admin/events
```

---

## 15. Data Flow & Business Rules

### Complete Business Rule Summary

| Rule | Enforcement Point | Mechanism |
|---|---|---|
| Only admins can create/edit/delete events | `SecurityConfig` + `AdminController` | `.requestMatchers("/admin/**").hasRole("ADMIN")` |
| Student must have name & email to sign in | `StudentController.studentLoginSubmit()` | Manual null/format check |
| Student can only register once per event | `RegistrationService.registerStudentForEvent()` | `existsByEventAndStudentEmail()` |
| Events with capacity cannot exceed seat count | `RegistrationService` | `countByEvent(event) >= event.getCapacity()` |
| Event fields are validated on create/edit | `AdminController.saveEvent()` + Entity annotations | `@Valid`, `BindingResult`, `@NotBlank`, `@Size`, `@Min` |
| Registration timestamp is auto-set | `Registration.@PrePersist` | `registrationDate = LocalDateTime.now()` |
| Unique student email in DB | `Student` entity | `@Column(unique = true)` |
| H2 console accessible during dev | `SecurityConfig` | `frameOptions.disable()` + permit `/h2-console/**` |

### Model Attributes Passed to Templates

#### `student_dashboard.html`
| Attribute | Type | Purpose |
|---|---|---|
| `events` | `List<Event>` | Filtered event list to display |
| `totalEvents` | `int` | Stat card: total event count |
| `workshopsCount` | `long` | Stat card: workshops + seminars |
| `hackathonCount` | `long` | Stat card: hackathons + cultural |
| `deptCount` | `long` | Stat card: unique departments |
| `registeredEventIds` | `List<Long>` | Set of event IDs student is registered for |
| `regCounts` | `Map<Long,Long>` | event.id → registration count (for seats left) |
| `myRegistrations` | `List<Registration>` | Student's registered events (sidebar) |
| `selectedType` | `String` | Currently active filter type |
| `studentName` | `String` | Displayed in navbar/greeting |
| `studentEmail` | `String` | Used for form submissions |

---

## 16. How to Run

### Prerequisites
- Java 21+
- Maven 3.x (or use included `mvnw` wrapper)

### Run the Application
```powershell
cd c:\smart_campus_event_management_system
mvn spring-boot:run
```
Or on Windows using the wrapper:
```powershell
.\mvnw spring-boot:run
```

### Access the Application
| URL | Description | Credentials |
|---|---|---|
| `http://localhost:8080/` | Public landing page | None |
| `http://localhost:8080/student/login` | Student login | Any name + valid email |
| `http://localhost:8080/login` | Admin login | `admin` / `admin123` |
| `http://localhost:8080/admin/events` | Admin events (after login) | — |
| `http://localhost:8080/admin/stats` | Registration analytics | — |
| `http://localhost:8080/h2-console` | H2 DB console | JDBC URL: `jdbc:h2:mem:smartcampusdb`, user: `sa`, pw: `password` |

### What Happens at Startup
1. Hibernate reads all `@Entity` classes and runs DDL (`create-update`) → creates `STUDENTS`, `EVENTS`, `REGISTRATIONS` tables.
2. `data.sql` runs and inserts 5 students, 8 events, and 9 registrations.
3. Spring Security configures in-memory user `admin`/`admin123`.
4. Embedded Tomcat starts on port `8080`.

---

> **Built with:** Spring Boot 3.3 · Java 21 · Spring Security 6 · Spring Data JPA · H2 · Thymeleaf · Maven

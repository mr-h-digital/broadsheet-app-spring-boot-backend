<div align="center">

<img src="./assets/broadsheet-app-logo.png" width="120" alt="WPC Broadsheet Logo">

# WPC Broadsheet — Spring Boot Backend

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![H2](https://img.shields.io/badge/H2-In--Memory_(Dev)-1F618D)](https://www.h2database.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Production_Ready-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

**REST API backend for the WPC Broadsheet Manager Android application.**  
Manages residents, meal captures, billing calculations, and user access for **Western Province Caterers** across multiple retirement sites.


</div>

---

## Overview

This Spring Boot backend serves as the cloud sync layer for the [WPC Broadsheet Manager Android app](https://github.com/mr-h-digital/broadsheet-app-android-frontend). It exposes a secure REST API that the Android app connects to via Retrofit, enabling:

- **Centralised authentication** with JWT tokens (replaces offline-only Room DB login)
- **Cloud sync** of meal captures, resident data, and pricing configs across devices
- **Server-side billing calculations** mirroring the exact Excel broadsheet formulas
- **Role-based access control** enforced at the API level
- **Consolidated reporting** across all sites in a single request

---

## Features

- **JWT Authentication** — stateless `Bearer` token auth, SHA-256 password hashing matching the Android client exactly
- **Billing Calculator** — server-side port of the Android `BillingCalculator`, including VAT, T/A Bakkies, and compulsory deduction logic
- **Full CRUD** — residents, sites, meal entries, pricing configs, and users
- **Audit Trail** — immutable append-only log for all resident lifecycle events (CREATED, UPDATED, DEACTIVATED, REACTIVATED, RELOCATED)
- **Soft Deletes** — users and residents are never hard-deleted; billing history is always preserved
- **Multi-Site Reporting** — per-site and consolidated all-sites monthly billing reports
- **Seed Data** — auto-seeded on first boot with all 72 residents, 3 sites, default user accounts, January 2026 meal entries, and per-site pricing configs
- **Dev-Friendly** — H2 in-memory database with console UI; swap to PostgreSQL for production with a single profile flag

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Framework | Spring Boot 3.3 |
| Language | Java 21 |
| Build Tool | Maven |
| Security | Spring Security + JJWT (HS256) |
| Persistence | Spring Data JPA + Hibernate |
| Database (Dev) | H2 In-Memory |
| Database (Prod) | PostgreSQL (configured, plug-and-play) |
| Validation | Jakarta Bean Validation |
| Utilities | Lombok, Jackson |

---

## Project Structure

```
src/main/java/co/za/mrhdigital/wpcbroadsheet/
│
├── WpcBroadsheetApplication.java          ← Spring Boot entry point
│
├── model/                                 ← JPA entities & enums
│   ├── UserEntity.java
│   ├── SiteEntity.java
│   ├── ResidentEntity.java
│   ├── ResidentAuditEntity.java
│   ├── MealEntryEntity.java
│   ├── MealPricingEntity.java
│   ├── Composite PKs (ResidentId, MealEntryId, MealPricingId)
│   └── Enums: UserRole, ResidentType, MealType, AuditAction
│
├── repository/                            ← Spring Data JPA interfaces
│   ├── UserRepository.java
│   ├── SiteRepository.java
│   ├── ResidentRepository.java
│   ├── ResidentAuditRepository.java
│   ├── MealEntryRepository.java
│   └── MealPricingRepository.java
│
├── dto/                                   ← Request / Response DTOs
│   ├── LoginRequest / LoginResponse / ApiUser
│   ├── SiteDto / SiteRequest
│   ├── ResidentDto / ResidentRequest / ResidentAuditDto
│   ├── MealEntryDto / MealPricingDto
│   ├── UserDto / UserRequest
│   └── ResidentMonthlyBillingDto / SiteMonthlyReportDto
│
├── service/                               ← Business logic
│   ├── BillingCalculatorService.java      ← Excel formula parity
│   ├── MealEntryService.java
│   ├── MealPricingService.java
│   ├── ResidentService.java
│   ├── SiteService.java
│   └── UserService.java
│
├── controller/                            ← REST endpoints
│   ├── AuthController.java                ← /api/auth
│   ├── SiteController.java                ← /api/sites
│   ├── ResidentController.java            ← /api/residents
│   ├── MealEntryController.java           ← /api/meal-entries
│   ├── MealPricingController.java         ← /api/pricing
│   ├── ReportController.java              ← /api/reports
│   └── UserController.java               ← /api/users
│
├── security/
│   ├── JwtService.java                    ← Token generation & validation
│   └── JwtAuthFilter.java                 ← Stateless Bearer token filter
│
└── config/
    ├── SecurityConfig.java                ← Spring Security filter chain
    ├── DataSeeder.java                    ← First-boot seed (idempotent)
    └── GlobalExceptionHandler.java        ← Unified error responses
```

---

## API Reference

### Authentication

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Public | Login — returns JWT + user profile |
| `GET` | `/api/auth/me` | Bearer | Returns the authenticated user's profile |

**Login Request:**
```json
{
  "email": "admin@wpc.co.za",
  "password": "<sha256-hex-of-password>"
}
```
> The Android app automatically SHA-256 hashes the password before sending. When calling the API directly (e.g. from Postman), hash the raw password first.

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "admin1",
    "name": "System Admin",
    "email": "admin@wpc.co.za",
    "role": "ADMIN",
    "phone": "",
    "siteId": null,
    "avatarUrl": null
  }
}
```

---

### Sites

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/sites` | All | List all active sites |
| `GET` | `/api/sites/all` | Admin / Ops | List all sites including inactive |
| `GET` | `/api/sites/{id}` | All | Get a single site |
| `POST` | `/api/sites` | Admin | Create a new site |
| `PUT` | `/api/sites/{id}` | Admin | Update a site |
| `DELETE` | `/api/sites/{id}` | Admin | Soft-deactivate a site |

---

### Residents

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/residents?siteId=&includeInactive=` | All | List residents |
| `GET` | `/api/residents/{siteId}/{unitNumber}` | All | Get a single resident |
| `GET` | `/api/residents/{siteId}/{unitNumber}/audit` | All | Full audit trail |
| `POST` | `/api/residents` | All | Create a resident |
| `PUT` | `/api/residents/{siteId}/{unitNumber}` | All | Update a resident |
| `POST` | `/api/residents/{siteId}/{unitNumber}/deactivate` | Admin / Ops | Soft-deactivate |
| `POST` | `/api/residents/{siteId}/{unitNumber}/reactivate` | Admin / Ops | Reactivate |
| `POST` | `/api/residents/{siteId}/{unitNumber}/relocate` | Admin / Ops | Move to another site |

---

### Meal Entries

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/meal-entries/{siteId}/{year}/{month}` | All | All entries for a site/month |
| `GET` | `/api/meal-entries/{siteId}/{unitNumber}/{year}/{month}` | All | Single resident entry |
| `POST` | `/api/meal-entries` | All | Merge-upsert counts (additive) |
| `PUT` | `/api/meal-entries` | All | Full replace of counts |

**Meal entry body:**
```json
{
  "siteId": "lizane",
  "unitNumber": "009",
  "year": 2026,
  "month": 1,
  "counts": {
    "COURSE_1": 12,
    "COURSE_2": 7,
    "SUN_1_COURSE": 7,
    "TA_BAKKIES": 10
  }
}
```

---

### Pricing Config

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/pricing/{siteId}/{year}/{month}` | All | Get pricing for a period |
| `GET` | `/api/pricing/{siteId}/history` | All | Full pricing history for a site |
| `POST` | `/api/pricing` | Admin / Ops | Upsert pricing config |

---

### Billing Reports

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/reports/{siteId}/{year}/{month}` | All | Full billing report for one site |
| `GET` | `/api/reports/all/{year}/{month}` | All | Consolidated report — all sites |

---

### Users (Admin Panel)

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users` | Admin | List all active users |
| `GET` | `/api/users/unit-managers?siteId=` | Admin / Ops | Unit managers (optionally filtered by site) |
| `GET` | `/api/users/{id}` | Admin / Self | Get a user |
| `POST` | `/api/users` | Admin | Create a user |
| `PUT` | `/api/users/{id}` | Admin / Self | Update a user |
| `DELETE` | `/api/users/{id}` | Admin | Soft-deactivate a user |
| `POST` | `/api/users/{id}/relocate` | Admin | Reassign a Unit Manager to a new site |

---

## Billing Formula

The `BillingCalculatorService` mirrors the Excel broadsheet formulas exactly, matching the Android `BillingCalculator`:

```
1. Sum monthly meal counts per type for each resident
2. Subtotal (excl. VAT) = Σ (count × price_per_type)
3. VAT = Subtotal × 15%
4. T/A Bakkies Total = bakkies_count × R5.00  (already incl. VAT)
5. Compulsory Deduction = R246.00  (fixed — applied to ALL residents)
6. Final Total = Subtotal + VAT + Bakkies − R246.00
   (negative final = credit)
```

> All prices are stored and transmitted **exclusive of VAT**. VAT is always calculated at 15%.

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run (Development)

```bash
git clone https://github.com/mr-h-digital/broadsheet-app-spring-boot-backend.git
cd broadsheet-app-spring-boot-backend
mvn spring-boot:run
```

The server starts on **`http://localhost:8080`**.  
The Android emulator connects via **`http://10.0.2.2:8080`** (already configured in `ApiClient.kt`).

### H2 Console (Dev Only)

Browse the in-memory database at:
```
http://localhost:8080/h2-console
JDBC URL:  jdbc:h2:mem:wpcbroadsheet
Username:  sa
Password:  (leave blank)
```

### Build JAR

```bash
mvn clean package
java -jar target/broadsheet-app-springboot-backend-1.0.0.jar
```

---

## Default Seed Accounts

The database is seeded automatically on first boot. Default password for all accounts is **`wpc2026`**.

> **Important:** When calling the API directly (e.g. Postman), the password field must be the **SHA-256 hex hash** of the raw password. The hash of `wpc2026` is:
> ```
> 1a4b8e57c8d1a2b9e4f3c7d6a0e5b2f8c3d9a1e6b4f7c2d5a8e0b3f6c9d2a5e8
> ```
> *(Hash it yourself: `echo -n "wpc2026" | sha256sum`)*

| Email | Role | Site Access |
| :--- | :--- | :--- |
| `admin@wpc.co.za` | Admin | All sites |
| `chernay@wpc.co.za` | Operations Manager | All sites |
| `vanrooyen@wpc.co.za` | Unit Manager | Lizane Village |
| `nothnagel@wpc.co.za` | Unit Manager | Bakkies Estate |

---

## Roles & Permissions

| Action | Unit Manager | Ops Manager | Admin |
| :--- | :---: | :---: | :---: |
| View sites & residents | ✅ | ✅ | ✅ |
| Capture meal entries | ✅ | ✅ | ✅ |
| Create / edit residents | ✅ | ✅ | ✅ |
| Deactivate / relocate residents | ❌ | ✅ | ✅ |
| Edit pricing config | ❌ | ✅ | ✅ |
| Manage users | ❌ | ❌ | ✅ |
| Create / deactivate sites | ❌ | ❌ | ✅ |

---

## Production Deployment

### 1. Switch to PostgreSQL

Uncomment the PostgreSQL section in `application-prod.properties` and configure your database credentials.

### 2. Set a Strong JWT Secret

The JWT secret must be **at least 32 bytes**. Use an environment variable:

```bash
JWT_SECRET=your-very-long-random-production-secret
```

Or pass it as a system property:

```bash
java -jar app.jar --jwt.secret=your-secret
```

### 3. Run with the Production Profile

```bash
java -jar target/broadsheet-app-springboot-backend-1.0.0.jar \
  --spring.profiles.active=prod
```

### 4. Schema Management

Set `spring.jpa.hibernate.ddl-auto=validate` in production (already the default in `application-prod.properties`). Use Flyway or Liquibase for migrations.

---

## Android Client Connection

The Android app (`ApiClient.kt`) is pre-configured to connect to this backend:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"  // emulator → localhost
```

For a real device or deployed server, update `BASE_URL` to your server's IP or domain.

---

## Roadmap

- [ ] PostgreSQL migration scripts (Flyway)
- [ ] Push notification triggers (FCM)
- [ ] Avatar upload endpoint (cloud storage)
- [ ] PDF/CSV report generation endpoint
- [ ] Background sync endpoint (batch meal entry upload)
- [ ] Refresh token support
- [ ] Swagger / OpenAPI documentation

---

## Related Repository

- **Android Frontend:** [broadsheet-app-android-frontend](https://github.com/mr-h-digital/broadsheet-app-android-frontend)

---

<div align="center">

<br>

*Built and maintained by*

<a href="https://mrhdigital.co.za">
  <img src="./assets/mrhdigital-logo.png" width="280" alt="Mr. H Digital">
</a>

<br>

**Digital Solutions for Growing Businesses**

[mrhdigital.co.za](https://mrhdigital.co.za)

<br>

</div>

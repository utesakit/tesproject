# TES Project – Server (Kotlin + PostgreSQL)

## Project Structure

```text
server/src/main/kotlin/com/tes
├── Application.kt              # Ktor server setup and main entry point
│
├── api
│   ├── auth
│   │   ├── AuthRoutes.kt       # /auth/register, /auth/login endpoint
│   │   └── AuthDtos.kt         # RegisterRequest, LoginRequest, UserResponse, MessageResponse
│   │
│   └── health
│       └── HealthRoutes.kt     # /health endpoint
│
├── config
│   ├── DatabaseConfig.kt       # Creates Database instance (Ktorm + PostgreSQL)
│   └── DatabaseInitializer.kt  # Creates tables (Data Definition Language)
│
├── data
│   └── shared
│       ├── UsersTable.kt       # Ktorm table mapping for "users"
│       ├── UserRepository.kt   # Repository interface
│       ├── DbUserRepository.kt # Repository implementation using Ktorm
│       └── UserMapper.kt       # Maps DB rows ↔ User ↔ UserResponse
│
└── domain
    ├── auth
    │   └── AuthService.kt      # Registration validation, email check, authentication + exceptions
    │
    ├── health
    │   ├── Health.kt           # Health data class
    │   └── HealthService.kt    # Health logic
    │
    └── shared
        └── User.kt             # Domain model for users
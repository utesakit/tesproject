# TES Project – Server (Kotlin + PostgreSQL)

Damit der Server funktioniert muss eine PostgreSQL-Datenbank extern laufen.


Anschließend sollte der Unterordner `server` als Gradle-Projekt in der IDE verknüpft werden.


Den Server kann man dann entweder direkt in `Main.kt` über den grünen Pfeil am linken Rand bei `fun main()` starten oder im Terminal mit:

```bash
./gradlew run
```

Der Server läuft anschließend auf `http://localhost:8080`.

Die Health-API kann man im Browser unter `http://localhost:8080/health` überprüfen.

Die Register-/Login-Endpoints kann man mithilfe der Datei `auth-test.http` im Ordner `test` in der IDE einer HTTP-POST-Requests testen.

## Project Structure

```text
server/
├── build.gradle.kts                      # Gradle build config for the Ktor server module
├── settings.gradle.kts                   # Gradle settings for the server module
├── gradlew / gradlew.bat                 # Gradle wrapper scripts
└── src/main/kotlin/com/tes
    ├── Main.kt                           # Ktor server setup and main entry point
    │
    ├── api
    │   ├── auth
    │   │   ├── AuthRoutes.kt             # /auth/register, /auth/login, /auth/refresh endpoints
    │   │   └── AuthDtos.kt               # RegisterRequest, LoginRequest, UserResponse, AuthResponse, ...
    │   │                                 # plus MessageResponse, RefreshTokenRequest, RefreshTokenResponse
    │   │
    │   └── health
    │       └── HealthRoutes.kt          # /health endpoint, returns status + uptime
    │
    ├── config
    │   ├── DatabaseConfig.kt            # Creates Ktorm Database instance (PostgreSQL)
    │   └── DatabaseInitializer.kt       # Initializes/updates "users" & "refresh_tokens" tables
    │
    ├── data
    │   ├── auth
    │   │   ├── RefreshTokenRepository.kt    # Repository interface for refresh tokens
    │   │   ├── DbRefreshTokenRepository.kt  # Implementation using Ktorm + PostgreSQL
    │   │   └── RefreshTokensTable.kt        # Ktorm table mapping for "refresh_tokens"
    │   │
    │   └── user
    │       ├── UsersTable.kt                # Ktorm table mapping for "users"
    │       ├── UserRepository.kt            # Repository interface for user persistence
    │       ├── DbUserRepository.kt          # Repository implementation using Ktorm + PostgreSQL
    │       └── UserMapper.kt                # Maps DB rows <=> User (domain) <=> UserResponse (API)
    │
    └── domain
        ├── auth
        │   ├── AuthService.kt           # Business logic: registration, login, validation, token issuing
        │   └── TokenService.kt          # JWT access/refresh token generation & verification
        │
        ├── health
        │   ├── Health.kt                # Health domain model
        │   └── HealthService.kt         # Provides health status based on server uptime
        │
        └── user
            └── User.kt                  # Domain model for users

debian/
└── Caddyfile                           # Example Caddy config (reverse proxy → Ktor backend)

test/
└── auth-test.http                      # HTTP client script for testing auth endpoints
```
# TES Project – Server (Kotlin + PostgreSQL)

Damit der Server funktioniert muss eine PostgreSQL-Datenbank extern laufen.


Anschließend sollte der Unterordner `server` als Gradle-Projekt in der IDE verknüpft werden.


Den Server kann man dann entweder direkt in `Main.kt` über den grünen Pfeil am linken Rand bei `fun main()` starten oder im Terminal mit:

```bash
./gradlew run
```

### Server URL

Der Server läuft anschließend auf `http://localhost:8080`.

### Health API

Die Health-API kann man im Browser unter `http://localhost:8080/health` überprüfen.

### Auth API (Register / Login & JWT)

Die Authentifizierungs-Endpoints testest man über `auth-test.http` im Ordner `test` in der IDE.  
Beim Registrierein/Einloggen erhält man JWT Access- und Refresh-Tokens, die man in weiteren Requests (z. B. `group-test.http`) im Header `Authorization: Bearer <accessToken>` verwendest.

### Group API (Groups & Membership)

Die Gruppen-Endpunkte testest man über `group-test.http` im Ordner `test`.  
Dort sind typische Operationen abgedeckt: Gruppen erstellen, per Einladungscode beitreten, Mitglieder entfernen, Gruppen verlassen und Gruppen löschen.

## Project Structure as 23.11.2025 18:00

```text
server/
├── build.gradle.kts                      # Gradle build config for the Ktor server module
├── settings.gradle.kts                   # Gradle settings for the server module
├── gradlew / gradlew.bat                 # Gradle wrapper scripts
└── src/main/kotlin/com/tes
    ├── Main.kt                           # Ktor server setup and main entry point
    │                                     # - creates Database
    │                                     # - wires repositories + services
    │                                     # - registers auth, health, and group routes
    │
    ├── api                               # HTTP API layer (Ktor routes + DTOs)
    │   ├── auth
    │   │   ├── AuthRoutes.kt             # /auth/register, /auth/login, /auth/refresh endpoints
    │   │   ├── AuthDtos.kt               # RegisterRequest, LoginRequest, UserResponse, AuthResponse, ...
    │   │   └── JwtAuth.kt                # extractUserIdFromToken, requireAuthenticatedUserId (JWT helper)
    │   │
    │   ├── groups
    │   │   ├── GroupRoutes.kt            # /groups, /groups/join, /groups/{id}/leave, /groups/{id}/members/{memberId}
    │   │   └── GroupDtos.kt              # CreateGroupRequest, JoinGroupRequest, GroupResponse, GroupsResponse
    │   │
    │   └── health
    │       └── HealthRoutes.kt           # /health endpoint, returns status + uptime
    │
    ├── config
    │   ├── DatabaseConfig.kt             # Creates Ktorm Database instance (PostgreSQL, local dev config)
    │   └── DatabaseInitializer.kt        # Initializes/updates tables:
    │                                     #   - users
    │                                     #   - refresh_tokens
    │                                     #   - groups
    │                                     #   - group_members
    │
    ├── data                              #  Repositories + Ktorm table mappings
    │   ├── auth
    │   │   ├── RefreshTokenRepository.kt    # Repository interface for refresh tokens
    │   │   ├── DbRefreshTokenRepository.kt  # Implementation using Ktorm + PostgreSQL
    │   │   └── RefreshTokenTable.kt         # Ktorm table mapping for "refresh_tokens"
    │   │
    │   ├── groups
    │   │   ├── GroupsTable.kt            # Ktorm table mapping for "groups"
    │   │   ├── GroupMembersTable.kt      # Ktorm table mapping for "group_members"
    │   │   ├── GroupRepository.kt        # Repository interface for groups + memberships
    │   │   ├── DbGroupRepository.kt      # Implementation using Ktorm + PostgreSQL
    │   │   └── GroupMapper.kt            # Maps DB rows <=> Group / GroupMember (domain)
    │   │
    │   └── user
    │       ├── UsersTable.kt             # Ktorm table mapping for "users"
    │       ├── UserRepository.kt         # Repository interface for user persistence
    │       ├── DbUserRepository.kt       # Repository implementation using Ktorm + PostgreSQL
    │       └── UserMapper.kt             # Maps DB rows <=> User (domain) <=> UserResponse (API)
    │
    └── domain                            # Business logic & domain models
        ├── auth
        │   ├── AuthService.kt            # Registration, login, validation, token issuing
        │   └── TokenService.kt           # JWT access/refresh token generation & verification
        │
        ├── groups
        │   ├── Group.kt                  # Domain models: Group, GroupMember
        │   └── GroupService.kt           # Business logic: create/join/leave/delete groups, remove members
        │
        ├── health
        │   ├── Health.kt                 # Health domain model
        │   └── HealthService.kt          # Provides health status based on server uptime
        │
        └── user
            └── User.kt                   # Domain model for users
            
debian/
└── Caddyfile                           # Example Caddy config (reverse proxy → Ktor backend)

test/
└── auth-test.http                      # HTTP client script for testing auth endpoints
```
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

### API-Dokumentation: https://app.swaggerhub.com/apis/germany-b19/TES1/1.0.0

## Dependencies [aktualisiert 23.11.2025]
```text
dependencies/
├── io.ktor:ktor-server-core-jvm:$ktorVersion                 # Basis-Ktor-APIs (Routing, Request/Response)
├── io.ktor:ktor-server-netty-jvm:$ktorVersion                # Netty-Engine, um Ktor als HTTP-Server zu starten

├── io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion  # JSON-Serialisierung in Ktor
├── io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion  # Content-Negotiation (JSON rein/raus)
├── org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3    # JSON (De-)Serialisierung für Kotlin-Datenklassen

├── ch.qos.logback:logback-classic:1.5.6                      # Logging-Backend (Konsolen-/Dateilogs)

├── org.ktorm:ktorm-core:$ktormVersion                        # Ktorm für SQL-Zugriffe in Kotlin
├── org.postgresql:postgresql:$postgresDriverVersion          # PostgreSQL Treiber

├── io.ktor:ktor-server-auth-jvm:$ktorVersion                 # Authentifizierungs-Support in Ktor
├── io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion             # JWT-Auth-Integration für Ktor
├── com.auth0:java-jwt:4.4.0                                  # Erzeugen und Prüfen von JWTs

└── org.mindrot:jbcrypt:0.4                                   # BCrypt zum sicheren Hashen von Passwörtern

```

## Datenbank-Tabellen [aktualisiert 23.11.2025]
```text
Es werden insgesamt 4 Tabellen verwendet:
    - users             # speichert Benutzer
    - refresh_tokens    # speichert Refresh-Tokens für JWT
    - groups            # speichert Gruppen
    - group_members     # Verknüpfungstabelle zwischen Benutzern und Gruppen

Tabelle "users":
    - id                SERIAL           Primary Key            # Eindeutige Benutzer-ID
    - first_name        VARCHAR(100)     NOT NULL               # Vorname
    - last_name         VARCHAR(100)     NOT NULL               # Nachname
    - email             VARCHAR(255)     NOT NULL, UNIQUE       # E-Mail-Adresse (zum Login)
    - password_hash     VARCHAR(255)     NOT NULL               # Gehashter Passwort-String (BCrypt)

Tabelle "refresh_tokens":
    - id        SERIAL          Primary Key                                      # Eindeutige ID des Refresh-Tokens
    - user_id   INTEGER         NOT NULL, FK => users(id), ON DELETE CASCADE     # Referenz auf den Benutzer
    - token     VARCHAR(500)    NOT NULL, UNIQUE                                 # Der eigentliche Refresh-Token-String
    
Tabelle "group_members":
    - id        SERIAL         Primary Key                                        # Eindeutige ID der Mitgliedschaft
    - group_id  INTEGER        NOT NULL, FK => groups(id), ON DELETE CASCADE      # Referenz auf die Gruppe
    - user_id   INTEGER        NOT NULL, FK => users(id), ON DELETE CASCADE       # Referenz auf den Benutzer
    
```

## Projekt Struktur [aktualisiert 23.11.2025]
```text
server/
├── build.gradle.kts                      # Gradle-Konfiguration für den Server
├── settings.gradle.kts                   # Gradle-Grundeinstellungen
├── gradlew / gradlew.bat                 # Gradle-Startskripte (Linux/Mac / Windows)
└── src/main/kotlin/com/tes
    ├── Main.kt                           # Startpunkt des Servers (startet Ktor & registriert alle Routen)
    │
    ├── api                               # HTTP-Schnittstelle (Routen + Datenobjekte)
    │   ├── auth
    │   │   ├── AuthRoutes.kt             # Endpoints: /auth/register, /auth/login, /auth/refresh
    │   │   ├── AuthDtos.kt               # Datenklassen für Requests/Responses (Register, Login, User, Tokens)
    │   │   └── JwtAuth.kt                # Hilfsfunktionen für JWT-Auslesen und -Prüfung
    │   │
    │   ├── groups
    │   │   ├── GroupRoutes.kt            # Endpoints: /groups, /groups/join, /groups/{id}/leave, ...
    │   │   └── GroupDtos.kt              # Datenklassen für Gruppen-Requests und -Responses
    │   │
    │   └── health
    │       └── HealthRoutes.kt           # Endpoint: /health (zeigt, ob der Server läuft)
    │
    ├── config
    │   ├── DatabaseConfig.kt             # Verbindet sich mit PostgreSQL (Datenbank)
    │   └── DatabaseInitializer.kt        # Legt Tabellen an / aktualisiert sie:
    │                                     #   - users
    │                                     #   - refresh_tokens
    │                                     #   - groups
    │                                     #   - group_members
    │
    ├── data                              # Zugriff auf die Datenbank (Repositorys + Tabellen)
    │   ├── auth
    │   │   ├── RefreshTokenRepository.kt    # Schnittstelle für Refresh-Tokens
    │   │   ├── DbRefreshTokenRepository.kt  # Umsetzung mit Ktorm + PostgreSQL
    │   │   └── RefreshTokenTable.kt         # Tabellenbeschreibung "refresh_tokens"
    │   │
    │   ├── groups
    │   │   ├── GroupsTable.kt              # Tabellenbeschreibung "groups"
    │   │   ├── GroupMembersTable.kt        # Tabellenbeschreibung "group_members"
    │   │   ├── GroupRepository.kt          # Schnittstelle für Gruppen + Mitgliedschaften
    │   │   ├── DbGroupRepository.kt        # Umsetzung mit Ktorm + PostgreSQL
    │   │   └── GroupMapper.kt              # Wandelt DB-Zeilen in Group/GroupMember-Objekte um
    │   │
    │   └── user
    │       ├── UsersTable.kt               # Tabellenbeschreibung "users"
    │       ├── UserRepository.kt           # Schnittstelle für Benutzerzugriff
    │       ├── DbUserRepository.kt         # Umsetzung mit Ktorm + PostgreSQL
    │       └── UserMapper.kt               # Wandelt DB-Zeilen in User-Objekte und API-Antworten um
    │
    └── domain                              # Fachlogik & Kernmodelle
        ├── auth
        │   ├── AuthService.kt              # Registrierung, Login, Prüfen von Zugangsdaten, Tokens ausgeben
        │   └── TokenService.kt             # Erstellen und Prüfen von JWT-Access- und Refresh-Tokens
        │
        ├── groups
        │   ├── Group.kt                    # Datenmodelle: Group und GroupMember
        │   └── GroupService.kt             # Gruppenlogik: erstellen, beitreten, verlassen, löschen, Mitglieder entfernen
        │
        ├── health
        │   ├── Health.kt                   # Datenmodell für den Gesundheitszustand des Servers
        │   └── HealthService.kt            # Berechnet Status und Laufzeit des Servers
        │
        └── user
            └── User.kt                     # Datenmodell für Benutzer

debian/
└── Caddyfile                              # Beispiel-Konfiguration für einen Reverse Proxy (Caddy → Ktor-Server)

test/
├── auth-test.http                         # Beispiel-HTTP-Requests zum Testen der Auth-API (Register/Login/JWT)
└── group-test.http                        # Beispiel-HTTP-Requests zum Testen der Gruppen-API (erstellen, beitreten, etc.)

```

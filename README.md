# TES Project – Server (Kotlin + PostgreSQL)
- **README.md & Kommentare wurden automatisiert erstellt**
---

## 1. Voraussetzungen

- **Java / JDK:**
    - JDK **21** (über `jvmToolchain(21)` konfiguriert)
- **Datenbank:**
    - PostgreSQL läuft extern, Standard-Konfiguration im Code:
        - URL: `jdbc:postgresql://localhost:5432/postgres`
        - User: `postgres`
        - Passwort: `AndroidAppA1!`
    - Anpassbar in: `server/src/main/kotlin/com/tes/config/DatabaseConfig.kt`
- **Entwicklungsumgebung:**
    - Projekt entwickelt und getestet mit **IntelliJ IDEA**
- **Build-Tool:**
    - Gradle Wrapper im Projekt (`gradlew`/`gradlew.bat`), kein extra Gradle nötig

---

## 2. Server starten

1. **PostgreSQL starten**
    - Stelle sicher, dass die Zugangsdaten zur DB wie oben stimmen (oder in `DatabaseConfig.kt` anpassen).

2. **Projekt öffnen**
    - Ordner `tesprojectgithub/server` in IntelliJ als Gradle-Projekt öffnen.

3. **Server starten**
    - In IntelliJ:  
      `Main.kt` öffnen → bei `fun main()` auf den grünen Pfeil klicken  
      **oder**
    - Im Terminal (im Unterordner `server`):
      ```bash
      ./gradlew run
      ```

4. **Server-URL**
    - Läuft anschließend unter: `http://localhost:8080`
    - Health-Check: `http://localhost:8080/health`

---

## 3. REST-APIs

### 3.1 Health API

- `GET /health`  
  → Gibt JSON mit Status und Uptime des Servers zurück.  
  Test einfach im Browser!

---

### 3.2 Auth API (Register / Login / Token-Refresh)

Alle Auth-Endpunkte erwarten JSON und liefern JSON.

- **POST `/auth/register`**  
  Registriert einen neuen Benutzer.

  Beispiel-Request:
  ```json
  {
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max@example.com",
    "password": "geheim123"
  }
  ```

  Antwort enthält:
    - `accessToken`
    - `refreshToken`
    - `user` (id, firstName, lastName, email)


- **POST `/auth/login`**  
  Loggt einen bestehenden Benutzer ein.  
  Request:
  ```json
  {
    "email": "max@example.com",
    "password": "geheim123"
  }
  ```
  Antwort: wieder `accessToken`, `refreshToken`, `user`.


- **POST `/auth/refresh`**  
  Erneuert Access- und Refresh-Token.

  Request:
  ```json
  {
    "refreshToken": "..."
  }
  ```

**Wichtig für geschützte Endpunkte:**  
Den Access-Token im Header mitsenden:

```http
Authorization: Bearer <accessToken>
```

---

### 3.3 Group API (Gruppen & Mitgliedschaften)

Alle Endpunkte hier sind **authentifiziert** (JWT im Header).

- **POST `/groups`**  
  Erstellt eine neue Gruppe für den eingeloggten Benutzer (wird Admin).  
  Request:
  ```json
  { "name": "WG Küche" }
  ```

- **POST `/groups/join`**  
  Tritt einer Gruppe über Einladungscode bei.  
  Request:
  ```json
  { "invitationCode": "ABC123" }
  ```

- **GET `/groups`**  
  Listet alle Gruppen des eingeloggten Benutzers.

- **POST `/groups/{groupId}/leave`**  
  Aktueller Benutzer verlässt die angegebene Gruppe.

- **DELETE `/groups/{groupId}`**  
  Löscht eine Gruppe komplett (nur Admin).

- **DELETE `/groups/{groupId}/members/{memberId}`**  
  Entfernt ein Mitglied aus der Gruppe (nur Admin).

---

### 3.4 Externe API-Dokumentation (Swagger)

Zusätzliche API-Beschreibung:
> __**https://app.swaggerhub.com/apis/germany-b19/TES1/1.0.0**__

---

## 4. APIs testen mit den Dateien im Ordner `/test`

Im Projekt-Root gibt es:

```text
test/
├── auth-test.http
└── group-test.http
```

> **Wichtig:**  
> Die `.http`-Dateien enthalten feste Testdaten bzgl. IDs.  
> **Vor jedem Testlauf** sollte die Datenbank geleert werden!

### 4.1 In IntelliJ IDEA

1. Datei `auth-test.http` öffnen.
2. Oben beim gewünschten Request auf "▶" klicken.
3. IntelliJ schickt den HTTP-Request an `http://localhost:8080`.

- `auth-test.http`:
    - Registrierung
    - Login
    - Token-Refresh

- `group-test.http`:
    - Gruppe anlegen
    - mit Einladungscode beitreten
    - Gruppen auflisten
    - Gruppe verlassen / löschen
    - Mitglieder entfernen

Die Dateien enthalten fertige Beispiel-Requests (inkl. JSON-Body).  
Bei Gruppen-Requests musst du den `Authorization`-Header mit einem gültigen Access-Token befüllen.

---

## 5. Dependencies (Server-Libraries)

Definiert in `server/build.gradle.kts`:

```text
io.ktor:ktor-server-core-jvm                       # Basis-Ktor-APIs (Routing, Request/Response)
io.ktor:ktor-server-netty-jvm                      # Netty-Engine, um Ktor als HTTP-Server zu starten

io.ktor:ktor-serialization-kotlinx-json-jvm        # JSON-Serialisierung in Ktor
io.ktor:ktor-server-content-negotiation-jvm        # Content-Negotiation (JSON rein/raus)
org.jetbrains.kotlinx:kotlinx-serialization-json   # JSON (De-)Serialisierung für Kotlin-Datenklassen

ch.qos.logback:logback-classic                     # Logging-Backend (Konsolen-/Dateilogs)

org.ktorm:ktorm-core                               # Ktorm für SQL-Zugriffe in Kotlin
org.postgresql:postgresql                          # PostgreSQL Treiber

io.ktor:ktor-server-auth-jvm                       # Authentifizierungs-Support in Ktor
io.ktor:ktor-server-auth-jwt-jvm                   # JWT-Auth-Integration für Ktor
com.auth0:java-jwt                                 # Erzeugen und Prüfen von JWTs

org.mindrot:jbcrypt                                # BCrypt zum sicheren Hashen von Passwörtern
```

---

## 6. Datenbank-Tabellen

Beim Serverstart legt `DatabaseInitializer` alle Tabellen an (falls nicht vorhanden).

Es gibt **vier** Tabellen:

- **`users`**
    - `id` – SERIAL, Primary Key
    - `first_name` – VARCHAR(100), NOT NULL
    - `last_name` – VARCHAR(100), NOT NULL
    - `email` – VARCHAR(255), UNIQUE, NOT NULL
    - `password_hash` – VARCHAR(255), NOT NULL (BCrypt-Hash)

- **`refresh_tokens`**
    - `id` – SERIAL, Primary Key
    - `user_id` – INTEGER, NOT NULL, FK → `users(id)`
    - `token` – VARCHAR(500), UNIQUE, NOT NULL

- **`groups`**
    - `id` – SERIAL, Primary Key
    - `name` – VARCHAR(100), NOT NULL
    - `invitation_code` – VARCHAR(6), UNIQUE, NOT NULL
    - `admin_id` – INTEGER, NOT NULL, FK → `users(id)`

- **`group_members`**
    - `id` – SERIAL, Primary Key
    - `group_id` – INTEGER, NOT NULL, FK → `groups(id)`
    - `user_id` – INTEGER, NOT NULL, FK → `users(id)`

---

## 7. Ordnerstruktur (Server)

Wichtige Teile der Struktur:

```text
tesprojectgithub/
├── server/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradlew / gradlew.bat
│   └── src/main/kotlin/com/tes
│       ├── Main.kt              # Startpunkt, startet Ktor-Server auf Port 8080
│       │
│       ├── api                  # HTTP-Routen + DTOs
│       │   ├── auth             # /auth/register, /auth/login, /auth/refresh
│       │   ├── groups           # /groups, /groups/join, ...
│       │   └── health           # /health
│       │
│       ├── config               # DB-Verbindung + Schema-Initialisierung
│       │   ├── DatabaseConfig.kt
│       │   └── DatabaseInitializer.kt
│       │
│       ├── data                 # Repositories + Tabellen (Ktorm)
│       │   ├── auth             # Refresh-Tokens
│       │   ├── groups           # Gruppen & Mitglieder
│       │   └── user             # Benutzer
│       │
│       └── domain               # Fachlogik & Kernmodelle
│           ├── auth             # AuthService, TokenService
│           ├── groups           # GroupService, Group-Modelle
│           ├── health           # HealthService, Health
│           └── user             # User-Modell
│
├── debian/
│   └── Caddyfile                # Beispiel: Caddy als Reverse Proxy vor dem Server
│
└── test/
    ├── auth-test.http           # Tests für Auth-API
    └── group-test.http          # Tests für Group-API
```

# Bulletin Gestion - Backend

Gestion des bulletins de notes et résultats scolaires.

## Architecture

Projet Spring Boot 3.2 (Java 17) structuré de façon identique au projet IT Access Manager :

- **Sécurité** : Spring Security + JWT (`com.bulletin.security`), filtre `JwtAuthenticationFilter`, `UserPrincipal`, `UserPrincipalService`, resolver `@CurrentUser`.
- **Authentification** : `AuthController` (`/auth/token`, `/auth/me`, `/auth/init-admin`, `/auth/status`).
- **Config** : `SecurityConfig`, `OpenApiConfig` (Swagger), `WebConfig`, `RailwayEnvironmentPostProcessor`.
- **Swagger** : springdoc-openapi disponible sur `/swagger-ui.html` (auth Bearer JWT).
- **Healthcheck** : `/health` et `/auth/status` (utilisé par Railway).
- **Base de données** : PostgreSQL + Flyway (`src/main/resources/db/migration`).
- **Déploiement** : Docker + Railway (`railway.json`, `railway.toml`) et Render (`render.yaml`).

## Démarrage local

```bash
docker compose up -d db
cp .env.example .env
./mvnw spring-boot:run
```

## Endpoints

- `GET  /health`        → healthcheck
- `GET  /auth/status`   → healthcheck Railway
- `POST /auth/token`    → login (JSON ou form), retourne un JWT
- `GET  /auth/me`       → utilisateur courant
- `POST /auth/init-admin?initKey=...` → création du premier admin
- `GET  /swagger-ui.html` → documentation API

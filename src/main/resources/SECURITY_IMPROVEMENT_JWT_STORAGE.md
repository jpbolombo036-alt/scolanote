# Guide d'amélioration : Stockage sécurisé du JWT

Ce document sert de mémo pour la migration du stockage du JWT de `localStorage` vers des cookies `HttpOnly`. Cette modification est cruciale pour renforcer la sécurité de l'application ScolaNote.

**Objectif :** Remplacer le stockage du JWT dans le `localStorage` du navigateur par un cookie `HttpOnly` et `Secure` géré par le backend.

---

## 1. Pourquoi ce changement ? (L'aspect sécurité)

Le stockage actuel dans le `localStorage` est une pratique courante mais présente une faille de sécurité majeure :

- **Vulnérabilité aux attaques XSS (Cross-Site Scripting) :** Si un attaquant parvient à injecter du code JavaScript malveillant dans votre frontend, ce script peut lire l'intégralité du `localStorage` et voler le JWT de l'utilisateur. Avec ce token, il peut usurper son identité.

### La solution : Cookies `HttpOnly`

Un cookie avec l'attribut `HttpOnly` est inaccessible depuis le code JavaScript du navigateur.

- **Protection contre le vol :** Même en cas d'attaque XSS, le script malveillant ne peut pas lire le cookie contenant le JWT.
- **Gestion par le navigateur :** Le navigateur se charge d'envoyer automatiquement le cookie à chaque requête vers le backend, de manière transparente.

> **Point d'attention :** L'utilisation de cookies pour l'authentification rend l'application vulnérable aux attaques **CSRF (Cross-Site Request Forgery)**. Il sera **impératif** d'activer et de configurer la protection CSRF de Spring Security en même temps.

---

## 2. Plan d'action - Backend (Spring Boot)

Le backend devient responsable de la création et de l'invalidation du cookie.

### Étape 1 : Modifier le `AuthController` pour créer le cookie

Au lieu de retourner le token dans le JSON, nous allons le placer dans un cookie.

**Exemple de modification dans `AuthController` :**
```java
// Dans votre méthode de login (ex: /auth/token)

// 1. Générez votre token comme avant
String jwt = jwtTokenProvider.generateToken(authentication);

// 2. Créez un cookie HttpOnly
ResponseCookie jwtCookie = ResponseCookie.from("jwt-token", jwt)
    .httpOnly(true)
    .secure(true) // Mettre à 'true' en production (nécessite HTTPS)
    .path("/")
    .maxAge(24 * 60 * 60) // 24 heures, comme votre expiration de token
    // .domain("votredomaine.com") // À configurer en production
    .build();

// 3. Ajoutez le cookie à la réponse et retournez les infos utilisateur
return ResponseEntity.ok()
    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
    .body(new JwtAuthenticationResponse(true, "Connexion réussie", userPrincipal)); // Ne plus inclure le token ici
```

### Étape 2 : Adapter le `JwtAuthenticationFilter`

Le filtre de sécurité doit maintenant lire le token depuis les cookies et non plus depuis l'en-tête `Authorization`.

**Exemple de modification dans `JwtAuthenticationFilter` :**
```java
private String getJwtFromRequest(HttpServletRequest request) {
    // Lire depuis les cookies
    if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
            if ("jwt-token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
    }

    // Garder le fallback sur l'en-tête peut être utile pour les tests API (Swagger, Postman)
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
    }

    return null;
}
```

### Étape 3 : Créer un endpoint de déconnexion

Le frontend ne peut pas supprimer un cookie `HttpOnly`. Il doit appeler un endpoint backend qui le fera.

**Exemple à ajouter dans `AuthController` :**
```java
@PostMapping("/logout")
public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = ResponseCookie.from("jwt-token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0) // Expire le cookie immédiatement
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(Map.of("message", "Vous avez été déconnecté."));
}
```

### Étape 4 : Activer la protection CSRF (Crucial !)

Dans votre `SecurityConfig`, activez la protection CSRF en utilisant un `CookieCsrfTokenRepository`.

**Exemple dans `SecurityConfig` :**
```java
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

// ... dans votre méthode securityFilterChain(HttpSecurity http)

http
    .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
    );

// Le frontend devra lire le cookie "XSRF-TOKEN" et l'envoyer
// dans l'en-tête "X-XSRF-TOKEN" pour chaque requête modifiant l'état (POST, PUT, DELETE).
```

---

## 3. Plan d'action - Frontend (Vue.js)

Le frontend n'a plus à manipuler le token JWT directement.

### Étape 1 : Configurer Axios

1.  **Activer `withCredentials`** pour que Axios envoie les cookies avec les requêtes.
2.  **Supprimer l'intercepteur** qui ajoutait l'en-tête `Authorization`.
3.  **Ajouter un intercepteur** pour la protection CSRF.

**Exemple de mise à jour de `src/api/axios.ts` :**
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // Très important !
});

// L'ancien intercepteur pour le 'Bearer' token est à supprimer.

// Nouvel intercepteur pour la gestion des erreurs 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Logique de déconnexion (ex: vider le store auth, rediriger vers /login)
      // useAuthStore().logout();
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
// Note: La gestion du token X-XSRF-TOKEN est souvent gérée automatiquement par Axios
// s'il trouve le cookie XSRF-TOKEN. À vérifier.
```

### Étape 2 : Adapter le Store Pinia (`useAuthStore`)

Le store ne stocke plus le token, mais l'état de l'utilisateur.

**Exemple de mise à jour de `src/stores/auth.ts` :**
```typescript
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '@/api/axios';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null);

  const isAuthenticated = computed(() => !!user.value);

  // Appeler cette fonction au démarrage de l'app (dans App.vue)
  async function fetchUser() {
    try {
      const { data } = await api.get('/auth/me');
      user.value = data;
    } catch (error) {
      user.value = null;
    }
  }

  async function login(credentials) {
    await api.post('/auth/token', credentials); // Le cookie est positionné par le backend
    await fetchUser(); // On récupère le profil pour mettre à jour l'état
  }

  async function logout() {
    await api.post('/auth/logout'); // Appelle le backend pour supprimer le cookie
    user.value = null;
  }

  return { user, isAuthenticated, fetchUser, login, logout };
});
```

---

## 4. Résumé des Tâches

1.  **Backend :**
    -   [ ] Mettre à jour `AuthController` pour envoyer un cookie `HttpOnly` au login.
    -   [ ] Créer un endpoint `/auth/logout` pour invalider le cookie.
    -   [ ] Modifier `JwtAuthenticationFilter` pour lire le token depuis les cookies.
    -   [ ] Activer et configurer la protection CSRF dans `SecurityConfig`.
2.  **Frontend :**
    -   [ ] Configurer Axios avec `withCredentials: true`.
    -   [ ] Supprimer l'intercepteur `Authorization: Bearer`.
    -   [ ] Mettre à jour le `authStore` pour qu'il se base sur l'utilisateur (`/auth/me`) et non plus sur un token local.
    -   [ ] Mettre à jour la fonction `logout` pour qu'elle appelle l'API.
    -   [ ] Appeler `fetchUser()` au chargement de l'application pour vérifier si une session est déjà active.
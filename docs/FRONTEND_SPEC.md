# Cahier des charges - Application Web Bulletin Scolaire RDC

## 1. Présentation du projet

Application de gestion des bulletins scolaires pour les écoles en République Démocratique du Congo (RDC).
Stack backend : Spring Boot 3, Java 17, PostgreSQL, Flyway, JWT, MapStruct, Lombok, Swagger/OpenAPI.

L'application gère :
- Les écoles et leur administration automatique
- Les années scolaires, trimestres et périodes
- Les élèves, professeurs, classes et matières
- Les évaluations et notes
- Les présences/retards et disciplines
- La génération automatique des bulletins avec calcul de moyennes, rangs, mentions et décisions
- La génération de PDF de bulletins
- La clôture des périodes avec validation
- Un système de rôles et permissions fin

---

## 2. Architecture

### Structure du projet
```
src/main/java/com/bulletin/
├── config/                    # Configuration Spring, DataInitializer
├── controller/
│   ├── school/               # Écoles, années, niveaux, sections, options, classes, trimestres, périodes
│   ├── curriculum/           # Matières, curriculum, affectations enseignants
│   ├── grade/                # Évaluations, notes
│   ├── tracking/             # Présences, disciplines
│   ├── bulletin/             # Bulletins, PDF, workflow
│   ├── people/               # Élèves, professeurs, inscriptions
│   └── user/                 # Utilisateurs, rôles
├── dto/
│   ├── school/               # Request/Response DTOs
│   ├── curriculum/
│   ├── grade/
│   ├── tracking/
│   ├── bulletin/
│   └── people/
├── entity/                   # Entités JPA
├── exception/                # Gestion des exceptions
├── mapper/                   # MapStruct mappers
├── repository/               # Spring Data JPA repositories
├── security/                 # JWT, SecurityUtils, UserPrincipal
├── service/
│   ├── bulletin/             # Calculatrice bulletin, PDF
│   └── ...                   # Services métier
└── BulletinGestionApplication.java
```

### Base de données
- PostgreSQL
- Migrations Flyway (V1 à V14)
- Soft delete sur toutes les entités (`deleted_at`)
- Timestamps `created_at`, `updated_at` sur toutes les entités

---

## 3. Modèle de données

### Entités principales

#### School (École)
- id, nom (requis), code (requis), province (requis), communeTerritoire (requis), adresse, telephone, email (requis), logo
- Relation : 1 → N AcademicYear

#### AcademicYear (Année scolaire)
- id, school (FK), libelle (requis), dateDebut, dateFin, active
- Champs verrouillage : verrouille, dateVerrouillage, verrouillePar

#### Level (Niveau)
- id, nom, ordre

#### Section (Section)
- id, nom

#### Option (Option)
- id, nom, section (FK)

#### Classroom (Classe)
- id, academicYear (FK), level (FK), section (FK), option (FK), reportTemplate (FK)
- nom (requis), capacite, titulaireId, active

#### Subject (Matière)
- id, nom (requis), code (requis)

#### CurriculumSubject (Matière dans le curriculum)
- id, level (FK), subject (FK), coefficient (requis)

#### Teacher (Professeur)
- id, nom (requis), postnom, prenom, specialite, telephone, email, dateNaissance, lieuNaissance, adresse, etat (requis)

#### Student (Élève)
- id, matricule (requis, unique), nom (requis), postnom, prenom, sexe, dateNaissance, lieuNaissance, adresse, telephoneParent, emailParent, etat (requis)

#### TeachingAssignment (Affectation enseignement)
- id, teacher (FK), classroom (FK), subject (FK)

#### Enrollment (Inscription)
- id, student (FK), classroom (FK), dateInscription, numeroOrdre, etat

#### Trimester (Trimestre)
- id, academicYear (FK), nom (requis), ordre, dateDebut, dateFin
- Relation : 1 → N Period

#### Period (Période)
- id, trimester (FK), nom (requis), ordre, type (PERIODE ou EXAMEN), dateDebut, dateFin
- Champs verrouillage : verrouille (défaut false), dateVerrouillage, verrouillePar

#### AssessmentType (Type d'évaluation)
- id, nom (requis), coefficient (requis)

#### Assessment (Évaluation)
- id, assignment (FK), assessmentType (FK), period (FK), titre (requis), date, noteMax, publie

#### Grade (Note)
- id, assessment (FK), student (FK), note, absence, observation

#### Attendance (Présence)
- id, student (FK), period (FK), date (requis), retard, absence, motif

#### Discipline
- id, student (FK), period (FK), conduite, application, observation

#### ReportCard (Bulletin)
- id, enrollment (FK), period (FK)
- pourcentage, totalPoints, maximumPoints, rang, mention, decision
- totalAbsences, totalRetards, conduite, application
- dateGeneration, pdfUrl, statut (BROUILLON/VALIDE_PREFET/VALIDE_DIRECTEUR/SIGNE/PUBLIE)
- valideParPrefetAt, valideParDirecteurAt, signeAt, publieAt, signatureUrl

#### ReportCardDetail (Détail bulletin)
- id, reportCard (FK), subject (FK)
- coefficient, moyenne, points, maximum, pourcentage
- rangMatiere, observation

#### User
- id, username (requis, unique), email, password (requis), enabled (défaut true)
- telephone, createdAt, updatedAt, deletedAt

#### Role
- id, nom (requis, unique)

#### UserRole
- id, user (FK), role (FK), createdAt, updatedAt

#### UserTeacher (Lien utilisateur-professeur)
- id, user (FK), teacher (FK)

---

## 4. Système de rôles et permissions

### Rôles
- SUPER_ADMIN : accès total, crée les écoles
- ADMIN : gestion de l'école
- DIRECTEUR : validation des bulletins, gestion pédagogique
- PREFET : surveillance disciplinaire, validation bulletins
- ENSEIGNANT : encodage notes/évaluations, consultation

### Permissions par ressource

| Ressource | Lecture | Création | Modification | Suppression |
|-----------|---------|----------|--------------|-------------|
| École | Tous | SUPER_ADMIN | SUPER_ADMIN | SUPER_ADMIN |
| Année scolaire | Tous | Direction | Direction | Direction |
| Trimestre | Tous | Direction | Direction | Direction |
| Période | Tous | Direction | Direction | Direction |
| Classe | Tous | Direction | Direction | Direction |
| Matière | Tous | Direction | Direction | Direction |
| Professeur | Tous | Direction | Direction | Direction |
| Élève | Tous | Direction | Direction | Direction |
| Inscription | Tous | Direction | Direction | Direction |
| Évaluation | Propriétaire + Direction | Professeur concerné | Professeur concerné | Professeur concerné |
| Note | Propriétaire + Direction | Professeur concerné | Professeur concerné | Professeur concerné |
| Présence | Direction | Direction | Direction | Direction |
| Discipline | Direction | Direction | Direction | Direction |
| Bulletin | Direction | Direction | - | - |

### Règles spéciales
- Un professeur ne peut créer/modifier/supprimer que les évaluations/notes de ses propres affectations (`TeachingAssignment`)
- La direction (SUPER_ADMIN, ADMIN, DIRECTEUR, PREFET) a accès à tout
- Une période verrouillée ne peut plus être modifiée (sauf par la direction)
- La génération de bulletin verrouille automatiquement la période
- Quand toutes les périodes de tous les trimestres d'une année sont verrouillées, l'année est verrouillée

---

## 5. API REST

### Authentification
```
POST /auth/token
Body: { "username": "string", "password": "string" }
Response: { "accessToken": "string", "tokenType": "Bearer", "expiresIn": 86400 }
```

### Format des réponses
- Succès : `200 OK`, `201 Created`
- Erreur : `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`
- Erreurs de validation : message détaillé par champ

### Pagination
- Paramètres : `?page=0&size=20&sort=nom,asc`
- Réponse paginée : `{ "content": [...], "page": 0, "size": 20, "totalElements": 100, "totalPages": 5 }`

### Endpoints disponibles

#### Écoles
- `POST /api/ecoles` - Créer (SUPER_ADMIN)
- `GET /api/ecoles/{id}` - Détail
- `GET /api/ecoles?page=0&size=10` - Liste paginée
- `GET /api/ecoles/all` - Liste complète
- `PUT /api/ecoles/{id}` - Modifier (SUPER_ADMIN)
- `DELETE /api/ecoles/{id}` - Supprimer (SUPER_ADMIN)

#### Années scolaires
- `POST /api/annees-academiques` - Créer (Direction)
- `GET /api/annees-academiques/{id}` - Détail
- `GET /api/annees-academiques?page=0&size=10` - Liste paginée
- `GET /api/annees-academiques/all` - Liste complète
- `GET /api/annees-academiques/ecole/{ecoleId}` - Par école
- `PUT /api/annees-academiques/{id}` - Modifier (Direction)
- `DELETE /api/annees-academiques/{id}` - Supprimer (Direction)

#### Trimestres
- `POST /api/trimestres` - Créer (Direction)
- `GET /api/trimestres/{id}` - Détail
- `GET /api/trimestres?page=0&size=10` - Liste paginée
- `GET /api/trimestres/all` - Liste complète
- `GET /api/trimestres/annee-academique/{anneeId}` - Par année
- `PUT /api/trimestres/{id}` - Modifier (Direction)
- `DELETE /api/trimestres/{id}` - Supprimer (Direction)

#### Périodes
- `POST /api/periodes` - Créer (Direction)
- `GET /api/periodes/{id}` - Détail
- `GET /api/periodes?page=0&size=10` - Liste paginée
- `GET /api/periodes/all` - Liste complète
- `GET /api/periodes/trimestre/{trimestreId}` - Par trimestre
- `GET /api/periodes/verrouillees` - Verrouillées
- `GET /api/periodes/ouvertes` - Ouvertes
- `GET /api/periodes/{id}/valider` - Validation verrouillage
- `PUT /api/periodes/{id}` - Modifier (Direction)
- `DELETE /api/periodes/{id}` - Supprimer (Direction)
- `POST /api/periodes/{id}/verrouiller` - Verrouiller (Direction)
- `POST /api/periodes/{id}/deverrouiller` - Déverrouiller (Direction)

#### Classes
- `POST /api/salles` - Créer (Direction)
- `GET /api/salles/{id}` - Détail
- `GET /api/salles?page=0&size=10` - Liste paginée
- `GET /api/salles/all` - Liste complète
- `GET /api/salles/annee-academique/{anneeId}` - Par année
- `PUT /api/salles/{id}` - Modifier (Direction)
- `DELETE /api/salles/{id}` - Supprimer (Direction)

#### Matières
- `POST /api/matieres` - Créer (Direction)
- `GET /api/matieres/{id}` - Détail
- `GET /api/matieres?page=0&size=10` - Liste paginée
- `GET /api/matieres/all` - Liste complète
- `PUT /api/matieres/{id}` - Modifier (Direction)
- `DELETE /api/matieres/{id}` - Supprimer (Direction)

#### Professeurs
- `POST /api/enseignants` - Créer (Direction)
- `GET /api/enseignants/{id}` - Détail
- `GET /api/enseignants?page=0&size=10` - Liste paginée
- `GET /api/enseignants/all` - Liste complète
- `PUT /api/enseignants/{id}` - Modifier (Direction)
- `DELETE /api/enseignants/{id}` - Supprimer (Direction)

#### Élèves
- `POST /api/eleves` - Créer (Direction)
- `GET /api/eleves/{id}` - Détail
- `GET /api/eleves?page=0&size=10` - Liste paginée
- `GET /api/eleves/all` - Liste complète
- `PUT /api/eleves/{id}` - Modifier (Direction)
- `DELETE /api/eleves/{id}` - Supprimer (Direction)

#### Inscriptions
- `POST /api/inscriptions` - Créer (Direction)
- `GET /api/inscriptions/{id}` - Détail
- `GET /api/inscriptions?page=0&size=10` - Liste paginée
- `GET /api/inscriptions/all` - Liste complète
- `GET /api/inscriptions/eleve/{eleveId}` - Par élève
- `GET /api/inscriptions/salle/{salleId}` - Par classe
- `PUT /api/inscriptions/{id}` - Modifier (Direction)
- `DELETE /api/inscriptions/{id}` - Supprimer (Direction)

#### Affectations enseignement
- `POST /api/attributions-enseignement` - Créer (Direction)
- `GET /api/attributions-enseignement/{id}` - Détail
- `GET /api/attributions-enseignement` - Liste
- `GET /api/attributions-enseignement/classe/{classeId}` - Par classe
- `PUT /api/attributions-enseignement/{id}` - Modifier (Direction)
- `DELETE /api/attributions-enseignement/{id}` - Supprimer (Direction)

#### Types d'évaluation
- `POST /api/types-evaluations` - Créer (Direction)
- `GET /api/types-evaluations/{id}` - Détail
- `GET /api/types-evaluations` - Liste
- `PUT /api/types-evaluations/{id}` - Modifier (Direction)
- `DELETE /api/types-evaluations/{id}` - Supprimer (Direction)

#### Évaluations
- `POST /api/evaluations` - Créer (Professeur concerné)
- `GET /api/evaluations/{id}` - Détail
- `GET /api/evaluations` - Liste
- `GET /api/evaluations/assignment/{assignmentId}` - Par affectation
- `GET /api/evaluations/period/{periodId}` - Par période
- `PUT /api/evaluations/{id}` - Modifier (Professeur concerné)
- `DELETE /api/evaluations/{id}` - Supprimer (Professeur concerné)

#### Notes
- `POST /api/notes` - Créer (Professeur concerné)
- `GET /api/notes/{id}` - Détail
- `GET /api/notes` - Liste
- `GET /api/notes/assessment/{assessmentId}` - Par évaluation
- `GET /api/notes/student/{studentId}` - Par élève
- `PUT /api/notes/{id}` - Modifier (Professeur concerné)
- `DELETE /api/notes/{id}` - Supprimer (Professeur concerné)

#### Présences
- `POST /api/presences` - Créer (Direction)
- `GET /api/presences/{id}` - Détail
- `GET /api/presences` - Liste
- `GET /api/presences/student/{studentId}` - Par élève
- `PUT /api/presences/{id}` - Modifier (Direction)
- `DELETE /api/presences/{id}` - Supprimer (Direction)

#### Disciplines
- `POST /api/disciplines` - Créer (Direction)
- `GET /api/disciplines/{id}` - Détail
- `GET /api/disciplines` - Liste
- `GET /api/disciplines/student/{studentId}` - Par élève
- `GET /api/disciplines/period/{periodId}` - Par période
- `PUT /api/disciplines/{id}` - Modifier (Direction)
- `DELETE /api/disciplines/{id}` - Supprimer (Direction)

#### Bulletins
- `POST /api/bulletins/generer` - Générer les bulletins (Direction)
- `GET /api/bulletins/{id}` - Détail
- `GET /api/bulletins/inscription/{inscriptionId}` - Par inscription
- `GET /api/bulletins/period/{periodId}` - Par période
- `POST /api/bulletins/{id}/pdf` - Générer/ télécharger PDF

---

## 6. Logique métier clé

### Création d'école
1. L'utilisateur envoie nom, code, province, communeTerritoire, adresse, telephone, email, logo
2. L'école est créée
3. Un utilisateur admin est créé automatiquement avec :
   - username = email de l'école
   - password = "123456"
   - rôle = SUPER_ADMIN

### Gestion des trimestres et périodes
- 3 trimestres par année scolaire
- Chaque trimestre contient 2 périodes + 1 examen
- Les périodes sont verrouillables individuellement

### Clôture de période
1. Vérifications avant verrouillage :
   - Au moins une évaluation existe
   - Toutes les notes sont encodées
2. Verrouillage de la période
3. Si toutes les périodes de tous les trimestres sont verrouillées → verrouillage de l'année

### Génération de bulletin
1. Récupérer la classe et la période
2. Pour chaque élève de la classe :
   - Calculer la moyenne par matière (pondérée par coefficients évaluation × coefficient matière)
   - Calculer le rang par matière
   - Calculer le pourcentage global
   - Déterminer le rang général
   - Déterminer la mention et la décision
   - Récupérer absences, retards, discipline
3. Sauvegarder le bulletin et ses détails
4. Verrouiller la période
5. Vérifier si l'année peut être verrouillée

### Calcul des moyennes
```
moyenneMatiere = Σ(note × coefficientEvaluation) / Σ(coefficientEvaluation)
pointsMatiere = moyenneMatiere × coefficientMatiere
pourcentageMatiere = pointsMatiere / Σ(noteMax × coefficientMatiere) × 100

totalPoints = Σ(pointsMatiere)
maximumPoints = Σ(maximumMatiere)
pourcentageGlobal = totalPoints / maximumPoints × 100
```

### Mentions
- EXCELLENT : ≥ 85%
- TRES BIEN : ≥ 70%
- BIEN : ≥ 60%
- SATISFACTION : ≥ 50%
- ECHEC : < 50%

### Décision
- ADMIS : ≥ 50%
- ECHEC : < 50%

---

## 7. Sécurité

### Authentification
- JWT avec expiration 24h
- Token dans header : `Authorization: Bearer <token>`

### Autorisation
- Vérification du rôle dans les controllers et services
- `SecurityUtils` fournit les méthodes :
  - `isSuperAdmin()`, `isAdminRole()`, `isDirecteur()`, `isPrefet()`, `isEnseignant()`, `isDirection()`

### Règles métier
- Les professeurs ne modifient que leurs propres évaluations/notes
- Les périodes verrouillées ne sont modifiables que par la direction
- La génération de bulletin est réservée à la direction ou au titulaire de classe

---

## 8. Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bulletin
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  upload:
    dir: /tmp/uploads
  bulletin:
    mention:
      excellent: 85
      tres-bien: 70
      bien: 60
      satisfaction: 50
    decision:
      admis: 50
  jwt:
    secret: votre_secret_jwt_min_256_bits
    expiration: 86400000
```

### Variables d'environnement
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_UPLOAD_DIR`

---

## 9. Tests

### Données de test
Script PowerShell `test-data.ps1` qui crée :
- 1 école
- 1 année scolaire
- 1 niveau, 1 section, 1 option, 1 classe
- 4 matières
- 1 professeur + 1 affectation
- 3 types d'évaluation
- 3 trimestres + 9 périodes
- 5 élèves + 5 inscriptions
- 3 évaluations + 15 notes
- Présences et disciplines

### Tests manuels
1. Démarrer l'application
2. Ouvrir Swagger : `http://localhost:8000/swagger-ui.html`
3. Se connecter avec l'admin auto créé (`email_ecole` / `123456`)
4. Créer les données de test via `test-data.ps1`
5. Générer un bulletin
6. Vérifier le PDF

---

## 10. Architecture frontend recommandée (Vue.js)

### Stack
- **Framework** : Vue 3 (Composition API, `<script setup>`)
- **Langage** : TypeScript
- **Bundler** : Vite
- **UI** : Tailwind CSS + composants UI (ex: shadcn-vue ou PrimeVue)
- **State** : Pinia
- **Router** : Vue Router 4
- **HTTP** : Axios
- **Forms** : Zod + React Hook Form équivalent (ex: VeeValidate)
- **PDF** : `@vue-pdf` ou téléchargement via endpoint backend
- **Icons** : `lucide-vue` ou `@iconify/vue`

### Structure du projet
```
frontend/
├── .env                        # Variables d'environnement
├── index.html
├── package.json
├── vite.config.ts
├── tailwind.config.ts
├── tsconfig.json
├── public/
│   └── favicon.ico
└── src/
    ├── main.ts                 # Point d'entrée
    ├── App.vue                 # Layout global + RouterView
    ├── assets/
    │   └── styles/
    │       ├── main.css        # Tailwind + styles globaux
    │       └── theme.css
    ├── config/
    │   └── env.ts              # Variables d'environnement typées
    ├── types/
    │   ├── school.ts
    │   ├── academic-year.ts
    │   ├── trimester.ts
    │   ├── period.ts
    │   ├── classroom.ts
    │   ├── subject.ts
    │   ├── teacher.ts
    │   ├── student.ts
    │   ├── enrollment.ts
    │   ├── teaching-assignment.ts
    │   ├── assessment-type.ts
    │   ├── assessment.ts
    │   ├── grade.ts
    │   ├── attendance.ts
    │   ├── discipline.ts
    │   ├── report-card.ts
    │   ├── report-card-detail.ts
    │   ├── user.ts
    │   ├── role.ts
    │   └── api.ts              # Types génériques API (Page, Response, Error)
    ├── api/
    │   ├── axios.ts            # Instance Axios + interceptors
    │   ├── auth.ts             # /auth/token
    │   ├── schools.ts
    │   ├── academic-years.ts
    │   ├── trimesters.ts
    │   ├── periods.ts
    │   ├── classrooms.ts
    │   ├── subjects.ts
    │   ├── teachers.ts
    │   ├── students.ts
    │   ├── enrollments.ts
    │   ├── teaching-assignments.ts
    │   ├── assessment-types.ts
    │   ├── assessments.ts
    │   ├── grades.ts
    │   ├── attendances.ts
    │   ├── disciplines.ts
    │   ├── report-cards.ts
    │   ├── users.ts
    │   ├── roles.ts
    │   └── user-teachers.ts
    ├── stores/
    │   ├── auth.ts             # Pinia store auth (token, user, roles)
    │   ├── school.ts
    │   ├── academic-year.ts
    │   ├── trimester.ts
    │   ├── period.ts
    │   ├── classroom.ts
    │   ├── subject.ts
    │   ├── teacher.ts
    │   ├── student.ts
    │   ├── enrollment.ts
    │   ├── teaching-assignment.ts
    │   ├── assessment-type.ts
    │   ├── assessment.ts
    │   ├── grade.ts
    │   ├── attendance.ts
    │   ├── discipline.ts
    │   ├── report-card.ts
    │   └── ui.ts               # Loading, notifications, modals
    ├── composables/
    │   ├── useApi.ts           # Wrapper fetch avec gestion erreurs
    │   ├── useAuth.ts
    │   ├── usePermissions.ts   # hasRole, canEdit, etc.
    │   ├ usePagination.ts
    │   └── useFilters.ts
    ├── layouts/
    │   ├── AuthLayout.vue      # Login (pas de sidebar)
    │   ├── MainLayout.vue      # Sidebar + Header + contenu
    │   └── components/
    │       ├── Sidebar.vue
    │       ├── Header.vue
    │       └── Breadcrumbs.vue
    ├── pages/
    │   ├── Login.vue
    │   ├── Dashboard.vue
    │   ├── schools/
    │   │   ├── SchoolsPage.vue
    │   │   ├── SchoolForm.vue
    │   │   └── SchoolDetailPage.vue
    │   ├── academic-years/
    │   ├── trimesters/
    │   ├── periods/
    │   ├── classrooms/
    │   ├── subjects/
    │   ├── teachers/
    │   ├── students/
    │   ├── enrollments/
    │   ├── teaching-assignments/
    │   ├── assessment-types/
    │   ├── assessments/
    │   ├── grades/
    │   ├── attendances/
    │   ├── disciplines/
    │   ├── report-cards/
    │   │   ├── ReportCardsPage.vue
    │   │   ├── ReportCardDetailPage.vue
    │   │   └── ReportCardPdfView.vue
    │   ├── users/
    │   └── roles/
    ├── components/
    │   ├── common/
    │   │   ├── ConfirmDialog.vue
    │   │   ├── LoadingSpinner.vue
    │   │   ├── ToastNotification.vue
    │   │   ├── EmptyState.vue
    │   │   └── Pagination.vue
    │   ├── layout/
    │   │   ├── Sidebar.vue
    │   │   ├── Header.vue
    │   │   └── Breadcrumbs.vue
    │   ├── school/
    │   │   ├── SchoolCard.vue
    │   │   ├── SchoolForm.vue
    │   │   └── SchoolTable.vue
    │   ├── academic-year/
    │   ├── trimester/
    │   ├── period/
    │   ├── classroom/
    │   ├── subject/
    │   ├── teacher/
    │   ├── student/
    │   ├── enrollment/
    │   ├── teaching-assignment/
    │   ├── assessment-type/
    │   ├── assessment/
    │   ├── grade/
    │   ├── attendance/
    │   ├── discipline/
    │   └── report-card/
    │       ├── ReportCardPdfPreview.vue
    │       ├── ReportCardSummary.vue
    │       └── GradeInput.vue
    ├── router/
    │   ├── index.ts
    │   └── routes.ts
    ├── utils/
    │   ├── format.ts           # Dates, nombres, pourcentages
    │   ├── permissions.ts      # Helpers permissions
    │   └── constants.ts        # Rôles, statuts bulletins, etc.
    └── vite-env.d.ts
```

### Modèle de données TypeScript

Exemples de types à créer dans `src/types/` :

```typescript
// school.ts
export interface School {
  id: number
  nom: string
  code: string
  province: string
  communeTerritoire: string
  adresse?: string
  telephone?: string
  email: string
  logo?: string
  createdAt?: string
  updatedAt?: string
}

// period.ts
export interface Period {
  id: number
  trimesterId: number
  nom: string
  ordre?: number
  type: 'PERIODE' | 'EXAMEN'
  dateDebut?: string
  dateFin?: string
  verrouille: boolean
  dateVerrouillage?: string
  verrouillePar?: string
  createdAt?: string
  updatedAt?: string
}

// report-card.ts
export interface ReportCard {
  id: number
  enrollmentId: number
  periodId: number
  pourcentage?: number
  totalPoints?: number
  maximumPoints?: number
  rang?: number
  mention?: string
  decision?: string
  totalAbsences?: number
  totalRetards?: number
  conduite?: string
  application?: string
  dateGeneration?: string
  pdfUrl?: string
  statut?: 'BROUILLON' | 'VALIDE_PREFET' | 'VALIDE_DIRECTEUR' | 'SIGNE' | 'PUBLIE'
  details?: ReportCardDetail[]
}

export interface ReportCardDetail {
  id: number
  subjectId?: number
  subjectNom?: string
  subjectCode?: string
  coefficient?: number
  moyenne?: number
  rangMatiere?: number
  points?: number
  maximum?: number
  pourcentage?: number
  observation?: string
}

// PaginatedResponse.ts
export interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
```

### Configuration Axios

Fichier `src/api/axios.ts` :
```typescript
import axios from 'axios'
import { API_BASE_URL } from '../config/env'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      window.dispatchEvent(new CustomEvent('session-expired'))
    }
    return Promise.reject(error)
  }
)

export { API_BASE_URL }
export default api
```

### Pinia stores (exemple)

```typescript
// stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'
import type { User, LoginRequest, LoginResponse } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)
  const roles = ref<string[]>([])

  const isAuthenticated = computed(() => !!token.value)
  const isDirection = computed(() => roles.value.some(r => ['SUPER_ADMIN', 'ADMIN', 'DIRECTEUR', 'PREFET'].includes(r)))
  const isSuperAdmin = computed(() => roles.value.includes('SUPER_ADMIN'))
  const isEnseignant = computed(() => roles.value.includes('ENSEIGNANT'))

  async function login(credentials: LoginRequest): Promise<LoginResponse> {
    const { data } = await api.post('/auth/token', credentials)
    token.value = data.accessToken
    localStorage.setItem('token', data.accessToken)
    await fetchProfile()
    return data
  }

  async function fetchProfile() {
    // Appeler /api/me ou décoder le JWT
  }

  function logout() {
    token.value = null
    user.value = null
    roles.value = []
    localStorage.removeItem('token')
  }

  return { token, user, roles, isAuthenticated, isDirection, isSuperAdmin, isEnseignant, login, logout }
})
```

### Routing

Fichier `src/router/routes.ts` :
```typescript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/pages/Login.vue'), meta: { guest: true } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('@/pages/Dashboard.vue') },
      { path: 'ecoles', component: () => import('@/pages/schools/SchoolsPage.vue') },
      { path: 'ecoles/:id', component: () => import('@/pages/schools/SchoolDetailPage.vue') },
      { path: 'annees-academiques', component: () => import('@/pages/academic-years/AcademicYearsPage.vue') },
      { path: 'trimestres', component: () => import('@/pages/trimesters/TrimestersPage.vue') },
      { path: 'periodes', component: () => import('@/pages/periods/PeriodsPage.vue') },
      { path: 'salles', component: () => import('@/pages/classrooms/ClassroomsPage.vue') },
      { path: 'matieres', component: () => import('@/pages/subjects/SubjectsPage.vue') },
      { path: 'enseignants', component: () => import('@/pages/teachers/TeachersPage.vue') },
      { path: 'eleves', component: () => import('@/pages/students/StudentsPage.vue') },
      { path: 'inscriptions', component: () => import('@/pages/enrollments/EnrollmentsPage.vue') },
      { path: 'attributions', component: () => import('@/pages/teaching-assignments/TeachingAssignmentsPage.vue') },
      { path: 'types-evaluations', component: () => import('@/pages/assessment-types/AssessmentTypesPage.vue') },
      { path: 'evaluations', component: () => import('@/pages/assessments/AssessmentsPage.vue') },
      { path: 'notes', component: () => import('@/pages/grades/GradesPage.vue') },
      { path: 'presences', component: () => import('@/pages/attendances/AttendancesPage.vue') },
      { path: 'disciplines', component: () => import('@/pages/disciplines/DisciplinesPage.vue') },
      { path: 'bulletins', component: () => import('@/pages/report-cards/ReportCardsPage.vue') },
      { path: 'bulletins/:id', component: () => import('@/pages/report-cards/ReportCardDetailPage.vue') },
      { path: 'bulletins/:id/pdf', component: () => import('@/pages/report-cards/ReportCardPdfView.vue') },
      { path: 'users', component: () => import('@/pages/users/UsersPage.vue') },
      { path: 'roles', component: () => import('@/pages/roles/RolesPage.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) return next('/login')
  if (to.meta.guest && authStore.isAuthenticated) return next('/dashboard')
  next()
})

export default router
```

### Composables utiles

```typescript
// composables/usePermissions.ts
export function usePermissions() {
  const authStore = useAuthStore()

  function canCreate() { return authStore.isDirection }
  function canEdit() { return authStore.isDirection }
  function canDelete() { return authStore.isSuperAdmin }
  function canGenerateBulletins() { return authStore.isDirection }

  return { canCreate, canEdit, canDelete, canGenerateBulletins }
}
```

### Composants UI réutilisables

- `PageHeader.vue` — Titre + breadcrumb + actions
- `DataTable.vue` — Tableau avec pagination, tri, filtres, sélection
- `FormDrawer.vue` — Drawer formulaire create/edit
- `ConfirmDialog.vue` — Confirmation suppression
- `Pagination.vue` — Contrôle pagination
- `StatCard.vue` — Carte statistique dashboard
- `RoleGuard.vue` — `<RoleGuard allowed="DIRECTEUR">...</RoleGuard>`

### Pages principales

- `Login.vue` — Formulaire login, redirection auto
- `Dashboard.vue` — KPIs : élèves, classes, bulletins générés, périodes verrouillées
- `ReportCardsPage.vue` — Liste + génération par classe/période
- `ReportCardDetailPage.vue` — Vue détaillée avec détails par matière
- `GradesPage.vue` — Encodage notes par évaluation (vue professeur)
- `PeriodsPage.vue` — Liste + actions verrouiller/déverrouiller/valider

---

## 11. Points d'attention pour le développeur frontend

### Authentification
- Stocker le JWT dans le localStorage
- Ajouter le header `Authorization: Bearer <token>` à toutes les requêtes
- Rediriger vers login si 401
- Écouter l'event `session-expired` pour déconnexion automatique

### Gestion des erreurs
- 400 : erreurs de validation (champs manquants, format invalide)
- 401 : non connecté
- 403 : permissions insuffisantes
- 404 : ressource non trouvée
- 500 : erreur serveur

### Pagination
- Toujours utiliser les endpoints paginés pour les listes
- Garder les paramètres `page`, `size`, `sort` dans l'état ou l'URL

### Dates
- Format ISO 8601 : `2025-09-01`
- Les dates sont en UTC dans l'API
- Afficher en format local dans l'UI

### Uploads
- Les logos et PDFs sont stockés dans `app.upload.dir`
- URLs relatives retournées : `uploads/bulletins/bulletin-1.pdf`

### Rôles
- Masquer/afficher les boutons selon le rôle de l'utilisateur
- Côté frontend, récupérer les rôles depuis le Pinia store auth
- Utiliser `v-if="authStore.isDirection"` pour les actions réservées

---

## 12. Fichiers de migration SQL

- V1 : tables core (users, roles, schools, academic_years, etc.)
- V3 : assessments et tracking
- V4 : report_cards et auth_logs
- V8 : création tables trimesters et periods
- V9 : migration données terms → trimesters/periods
- V10 : ajout colonnes period_id
- V11 : suppression anciennes colonnes term_id
- V12 : ajout province et commune_territoire à schools
- V13 : ajout verrouillage à periods
- V14 : ajout verrouillage à academic_years

---

## 13. Technologies utilisées (Backend)

- Java 17
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- Swagger/OpenAPI 3
- iText (PDF)
- Maven

---

## 14. Instructions de lancement (Backend)

```bash
# Installer PostgreSQL et créer la base
createdb bulletin

# Configurer application.yml
# Lancer l'application
mvn spring-boot:run

# Ou avec profil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Swagger UI
# http://localhost:8000/swagger-ui.html
```

---

## 15. Contact et support

Ce document est destiné à un agent AI pour la génération du frontend Vue.js. Pour toute question sur le backend, consulter le code source et la documentation Swagger.


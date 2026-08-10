// src/types.ts (nouveau contenu ou ajout à un fichier existant)

// --- Types existants (exemples) ---
export interface LoginRequest {
  username?: string;
  email?: string;
  telephone?: string;
  password?: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email?: string;
  telephone?: string;
  roles: string[];
  permissions: string[];
  schoolId?: number;
  passwordResetRequired: boolean;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface ReportCardRequest {
  classroomId: number;
  periodId: number;
}

export interface ReportCardResponse {
  id: number;
  enrollmentId: number;
  periodId: number;
  pourcentage: number;
  totalPoints: number;
  maximumPoints: number;
  rang: number;
  mention: string;
  decision: string;
  totalAbsences: number;
  totalRetards: number;
  conduite?: string;
  application?: string;
  dateGeneration: string;
  pdfUrl?: string;
  statut: string;
  studentNom: string;
  trimesterNom: string;
  details: ReportCardDetailResponse[];
}

export interface ReportCardDetailResponse {
  id: number;
  subjectId: number;
  subjectNom: string;
  subjectCode: string;
  coefficient: number;
  moyenne: number;
  rangMatiere: number;
  points: number;
  maximum: number;
  pourcentage: number;
  appreciation?: string;
}

export interface ReportCardWorkflowResponse {
  message: string;
  reportCardId: number;
  newStatut: string;
}

export interface ReportCardActionRequest {
  // Peut contenir des données supplémentaires pour la validation/signature
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: { sorted: boolean; unsorted: boolean; empty: boolean };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: { sorted: boolean; unsorted: boolean; empty: boolean };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// --- Nouveaux types pour les bulletins annuels ---
export interface AcademicYearReportCardResponse {
  id: number;
  enrollment: EnrollmentResponse; // Assurez-vous que EnrollmentResponse est défini
  academicYearId: number;
  academicYearLibelle: string;
  pourcentage?: number;
  totalPoints?: number;
  maximumPoints?: number;
  rang?: number;
  mention?: string;
  decision?: string;
  totalAbsences?: number;
  totalRetards?: number;
  conduite?: string;
  application?: string;
  dateGeneration?: string;
  pdfUrl?: string;
  statut: string;
  studentNomComplet: string;
  classroomNom: string;
  details: AcademicYearReportCardDetailResponse[];
}

export interface AcademicYearReportCardDetailResponse {
  id: number;
  subjectId: number;
  subjectNom: string;
  subjectCode: string;
  coefficient?: number;
  moyenne?: number;
  points?: number;
  maximum?: number;
  pourcentage?: number;
  rangMatiere?: number;
  observation?: string;
}

export interface AcademicYearGenerationRequest {
  academicYearId: number;
  classroomId: number;
}

// Exemple de EnrollmentResponse (à adapter si vous avez déjà un type plus complet)
export interface EnrollmentResponse {
  id: number;
  // ... autres champs pertinents de l'inscription
}
package com.bulletin.service;

import com.bulletin.dto.people.TeacherRequest;
import com.bulletin.dto.people.TeacherResponse;
import com.bulletin.entity.Teacher;
import com.bulletin.entity.User;
import com.bulletin.entity.UserTeacher;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TeacherMapper;
import com.bulletin.repository.TeacherRepository;
import com.bulletin.repository.UserTeacherRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final SecurityUtils securityUtils;
    private final AccountProvisioningService accountProvisioningService;
    private final UserTeacherRepository userTeacherRepository;

    private boolean isSuperAdmin() {
        return securityUtils.isSuperAdmin();
    }

    private Long requireSchoolId() {
        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            throw new SecurityException("École non définie pour l'utilisateur connecté");
        }
        return schoolId;
    }

    @Transactional
    public TeacherResponse createTeacher(TeacherRequest request) {
        Long schoolId = requireSchoolId();
        Teacher teacher = teacherMapper.toEntity(request);
        teacher.setSchoolId(schoolId);
        Teacher saved = teacherRepository.save(teacher);
        log.info("Professeur créé: {}", saved.getId());

        // Un professeur EST un utilisateur : on crée automatiquement son compte (rôle ENSEIGNANT)
        // + le lien UserTeacher. L'email est obligatoire (validé dans TeacherRequest).
        provisionTeacherAccount(saved);

        return teacherMapper.toResponse(saved);
    }

    /**
     * Crée le compte utilisateur du professeur (rôle ENSEIGNANT) + le lien UserTeacher.
     * Le compte : username = email, mot de passe par défaut (changement obligatoire à la 1ère connexion).
     * Un e-mail de bienvenue est envoyé (asynchrone). N'impacte jamais la création du professeur.
     */
    private void provisionTeacherAccount(Teacher teacher) {
        try {
            if (teacher.getEmail() == null || teacher.getEmail().isBlank()) {
                return; // sécurité : ne devrait pas arriver (email obligatoire dans TeacherRequest)
            }
            String fullName = teacher.getNom()
                    + (teacher.getPostnom() != null ? " " + teacher.getPostnom() : "")
                    + (teacher.getPrenom() != null ? " " + teacher.getPrenom() : "");

            // 1. Créer le compte utilisateur (rôle ENSEIGNANT) + e-mail de bienvenue
            User user = accountProvisioningService.provisionAccount(
                    teacher.getEmail(), "ENSEIGNANT", teacher.getSchoolId(), fullName.trim());

            // 2. Créer le lien UserTeacher (si pas déjà existant)
            boolean linkExists = userTeacherRepository.existsByUser_IdAndTeacher_Id(user.getId(), teacher.getId());
            if (!linkExists) {
                userTeacherRepository.save(UserTeacher.builder()
                        .user(user)
                        .teacher(teacher)
                        .build());
                log.info("Lien user-professeur créé automatiquement: user={} teacher={}", user.getId(), teacher.getId());
            }
        } catch (Exception e) {
            // La création du compte ne doit JAMAIS faire échouer la création du professeur.
            log.warn("Impossible de provisionner le compte du professeur {} : {}", teacher.getId(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(Long id) {
        return teacherMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<TeacherResponse> getAccessibleTeachers(Pageable pageable) {
        if (isSuperAdmin()) {
            return teacherRepository.findAll(sanitizePageable(pageable))
                    .map(this::toResponseSafely);
        }
        return teacherRepository.findBySchoolId(requireSchoolId(), sanitizePageable(pageable))
                .map(this::toResponseSafely);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAccessibleTeachers() {
        if (isSuperAdmin()) {
            return teacherRepository.findAll().stream()
                    .map(this::toResponseSafely)
                    .toList();
        }
        return teacherRepository.findBySchoolId(requireSchoolId()).stream()
                .map(this::toResponseSafely)
                .toList();
    }

    /**
     * Mapping résilient d'une entité Teacher vers TeacherResponse.
     * Une entité qui échoue au mapping ne fait pas échouer toute la liste (retourne null, filtré).
     */
    private TeacherResponse toResponseSafely(Teacher teacher) {
        try {
            return teacherMapper.toResponse(teacher);
        } catch (Exception e) {
            log.warn("Impossible de mapper le professeur {} : {}", teacher != null ? teacher.getId() : "null", e.getMessage());
            return null;
        }
    }

    /**
     * Sécurise le Pageable : ignore les tris (Sort) sur des propriétés invalides
     * qui causeraient une PropertyReferenceException (erreur 500) côté Spring Data.
     * Conserve page/size, force un tri valide par défaut sur "nom".
     */
    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable == null) {
            return Pageable.unpaged();
        }
        // Force un tri valide connu pour éviter les tris sur des propriétés inexistantes.
        var sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "nom");
        return org.springframework.data.domain.PageRequest.of(
                Math.max(0, pageable.getPageNumber()),
                Math.max(1, pageable.getPageSize()),
                sort);
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = findById(id);
        teacherMapper.updateEntity(request, teacher);
        Teacher saved = teacherRepository.save(teacher);
        log.info("Professeur mis à jour: {}", saved.getId());
        return teacherMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = findById(id);
        teacher.setDeletedAt(java.time.LocalDateTime.now());
        teacherRepository.save(teacher);
        log.info("Professeur supprimé (soft): {}", id);
    }

    public Teacher findById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professeur non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(teacher.getSchoolId());
        return teacher;
    }
}

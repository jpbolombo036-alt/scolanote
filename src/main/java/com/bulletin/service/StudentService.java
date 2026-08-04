package com.bulletin.service;

import com.bulletin.dto.people.StudentRequest;
import com.bulletin.dto.people.StudentResponse;
import com.bulletin.entity.Student;
import com.bulletin.entity.User;
import com.bulletin.entity.UserStudent;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.StudentMapper;
import com.bulletin.repository.StudentRepository;
import com.bulletin.repository.UserStudentRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final SecurityUtils securityUtils;
    private final AccountProvisioningService accountProvisioningService;
    private final UserStudentRepository userStudentRepository;

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
    public StudentResponse createStudent(StudentRequest request) {
        Student student = studentMapper.toEntity(request);
        student.setSchoolId(requireSchoolId());
        if (request.getMatricule() == null || request.getMatricule().isBlank()) {
            student.setMatricule(generateStudentMatricule(student.getSchoolId()));
        }
        normalizeEtat(student);
        Student saved = studentRepository.save(student);
        log.info("Élève créé: {}", saved.getId());

        // Si un email parent est renseigné : on crée automatiquement le compte du parent
        // (rôle PARENT) + le lien UserStudent. Le parent consulte les bulletins de son enfant.
        provisionParentAccount(saved);

        return studentMapper.toResponse(saved);
    }

    /**
     * Crée le compte utilisateur du parent (rôle PARENT) + le lien UserStudent.
     * Le compte : username = emailParent, mot de passe par défaut (changement obligatoire à la 1ère connexion).
     * Un e-mail de bienvenue est envoyé (asynchrone). N'impacte jamais la création de l'élève.
     */
    private void provisionParentAccount(Student student) {
        try {
            String emailParent = student.getEmailParent();
            if (emailParent == null || emailParent.isBlank()) {
                return; // pas d'email parent → pas de compte à créer
            }

            String studentName = student.getNom()
                    + (student.getPostnom() != null ? " " + student.getPostnom() : "")
                    + (student.getPrenom() != null ? " " + student.getPrenom() : "");

            // 1. Créer le compte utilisateur du parent (rôle PARENT) + e-mail de bienvenue
            User parent = accountProvisioningService.provisionAccount(
                    emailParent, "PARENT", student.getSchoolId(), "Parent de " + studentName.trim());

            // 2. Créer le lien UserStudent (si pas déjà existant)
            boolean linkExists = userStudentRepository.existsByUser_IdAndStudent_Id(parent.getId(), student.getId());
            if (!linkExists) {
                userStudentRepository.save(UserStudent.builder()
                        .user(parent)
                        .student(student)
                        .build());
                log.info("Lien user-élève créé automatiquement: parent={} student={}", parent.getId(), student.getId());
            }
        } catch (Exception e) {
            // La création du compte parent ne doit JAMAIS faire échouer la création de l'élève.
            log.warn("Impossible de provisionner le compte parent pour l'élève {} : {}", student.getId(), e.getMessage());
        }
    }

    private String generateStudentMatricule(Long schoolId) {
        long nextIndex = studentRepository.countBySchoolId(schoolId) + 1;
        String matricule;
        do {
            matricule = String.format("E%s-%04d", schoolId, nextIndex++);
        } while (studentRepository.existsByMatricule(matricule));
        return matricule;
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        return studentMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAccessibleStudents(Pageable pageable) {
        if (isSuperAdmin()) {
            return studentRepository.findAll(pageable)
                    .map(studentMapper::toResponse);
        }
        return studentRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(studentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAccessibleStudents() {
        if (isSuperAdmin()) {
            return studentRepository.findAll().stream()
                    .map(studentMapper::toResponse)
                    .toList();
        }
        return studentRepository.findBySchoolId(requireSchoolId()).stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findById(id);
        studentMapper.updateEntity(request, student);
        normalizeEtat(student);
        Student saved = studentRepository.save(student);
        log.info("Élève mis à jour: {}", saved.getId());
        return studentMapper.toResponse(saved);
    }

    /**
     * Normalise le champ etat via l'enum EtatPersonne.
     * Accepte le nom de l'enum ou le libellé (insensible à la casse/accents),
     * stocke le libellé canonique, et applique "Actif" par défaut si vide.
     */
    private void normalizeEtat(Student student) {
        String etat = student.getEtat();
        if (etat == null || etat.isBlank()) {
            student.setEtat(com.bulletin.entity.enums.EtatPersonne.defaultValue());
        } else {
            com.bulletin.entity.enums.EtatPersonne e = com.bulletin.entity.enums.EtatPersonne.from(etat);
            student.setEtat(e != null ? e.getLibelle() : etat);
        }
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findById(id);
        student.setDeletedAt(java.time.LocalDateTime.now());
        studentRepository.save(student);
        log.info("Élève supprimé (soft): {}", id);
    }

    public Student findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(student.getSchoolId());
        return student;
    }
}

package com.bulletin.service;

import com.bulletin.dto.school.ClassroomRequest;
import com.bulletin.dto.school.ClassroomResponse;
import com.bulletin.entity.*;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.ClassroomMapper;
import com.bulletin.repository.*;
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
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final AcademicYearRepository academicYearRepository;
    private final LevelRepository levelRepository;
    private final SectionRepository sectionRepository;
    private final OptionRepository optionRepository;
    private final ReportTemplateRepository reportTemplateRepository;
    private final ClassroomMapper classroomMapper;
    private final SecurityUtils securityUtils;

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
    public ClassroomResponse createClassroom(ClassroomRequest request) {
        Classroom classroom = classroomMapper.toEntity(request);
        classroom.setAcademicYear(findAcademicYear(request.getAcademicYearId()));
        classroom.setLevel(findLevel(request.getLevelId()));
        classroom.setSection(findSection(request.getSectionId()));
        if (request.getOptionId() != null) {
            classroom.setOption(findOption(request.getOptionId()));
        }
        if (request.getReportTemplateId() != null) {
            classroom.setReportTemplate(findReportTemplate(request.getReportTemplateId()));
        }
        Classroom saved = classroomRepository.save(classroom);
        log.info("Classe créée: {}", saved.getId());
        return classroomMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClassroomResponse getClassroom(Long id) {
        return classroomMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<ClassroomResponse> getAccessibleClassrooms(Pageable pageable) {
        if (isSuperAdmin()) {
            return classroomRepository.findAll(pageable)
                    .map(classroom -> {
                        if (classroom.getAcademicYear() == null || classroom.getLevel() == null
                                || classroom.getSection() == null || classroom.getOption() == null
                                || classroom.getReportTemplate() == null) {
                            return null;
                        }
                        return classroomMapper.toResponse(classroom);
                    });
        }

        List<Long> academicYearIds = academicYearRepository.findBySchoolId(requireSchoolId()).stream()
                .map(com.bulletin.entity.AcademicYear::getId)
                .toList();

        return classroomRepository.findByAcademicYearIdIn(academicYearIds, pageable)
                .map(classroom -> {
                    if (classroom.getAcademicYear() == null || classroom.getLevel() == null
                            || classroom.getSection() == null || classroom.getOption() == null
                            || classroom.getReportTemplate() == null) {
                        return null;
                    }
                    return classroomMapper.toResponse(classroom);
                });
    }

    @Transactional(readOnly = true)
    public List<ClassroomResponse> getAccessibleClassrooms() {
        if (isSuperAdmin()) {
            return classroomRepository.findAll().stream()
                    .map(classroom -> {
                        if (classroom.getAcademicYear() == null || classroom.getLevel() == null
                                || classroom.getSection() == null || classroom.getOption() == null
                                || classroom.getReportTemplate() == null) {
                            return null;
                        }
                        return classroomMapper.toResponse(classroom);
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        List<Long> academicYearIds = academicYearRepository.findBySchoolId(requireSchoolId()).stream()
                .map(com.bulletin.entity.AcademicYear::getId)
                .toList();

        return classroomRepository.findByAcademicYearIdIn(academicYearIds).stream()
                .map(classroom -> {
                    if (classroom.getAcademicYear() == null || classroom.getLevel() == null
                            || classroom.getSection() == null || classroom.getOption() == null
                            || classroom.getReportTemplate() == null) {
                        return null;
                    }
                    return classroomMapper.toResponse(classroom);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassroomResponse> getClassroomsByAcademicYear(Long academicYearId) {
        return classroomRepository.findByAcademicYearId(academicYearId).stream()
                .map(classroom -> {
                    if (classroom.getAcademicYear() == null || classroom.getLevel() == null
                            || classroom.getSection() == null || classroom.getOption() == null
                            || classroom.getReportTemplate() == null) {
                        return null;
                    }
                    return classroomMapper.toResponse(classroom);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public ClassroomResponse updateClassroom(Long id, ClassroomRequest request) {
        Classroom classroom = findById(id);
        classroomMapper.updateEntity(request, classroom);
        classroom.setAcademicYear(findAcademicYear(request.getAcademicYearId()));
        classroom.setLevel(findLevel(request.getLevelId()));
        classroom.setSection(findSection(request.getSectionId()));
        if (request.getOptionId() != null) {
            classroom.setOption(findOption(request.getOptionId()));
        }
        if (request.getReportTemplateId() != null) {
            classroom.setReportTemplate(findReportTemplate(request.getReportTemplateId()));
        }
        Classroom saved = classroomRepository.save(classroom);
        log.info("Classe mise à jour: {}", saved.getId());
        return classroomMapper.toResponse(saved);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = findById(id);
        classroom.setDeletedAt(java.time.LocalDateTime.now());
        classroomRepository.save(classroom);
        log.info("Classe supprimée (soft): {}", id);
    }

    public Classroom findById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + id));
    }

    private AcademicYear findAcademicYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID: " + id));
    }

    private Level findLevel(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau non trouvé avec l'ID: " + id));
    }

    private Section findSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section non trouvée avec l'ID: " + id));
    }

    private Option findOption(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option non trouvée avec l'ID: " + id));
    }

    private ReportTemplate findReportTemplate(Long id) {
        return reportTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modèle de bulletin non trouvé avec l'ID: " + id));
    }
}

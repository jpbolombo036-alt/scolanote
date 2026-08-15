package com.bulletin.service;

import com.bulletin.dto.curriculum.CurriculumSubjectRequest;
import com.bulletin.dto.curriculum.CurriculumSubjectResponse;
import com.bulletin.entity.Curriculum;
import com.bulletin.entity.CurriculumSubject;
import com.bulletin.entity.Level;
import com.bulletin.entity.Option;
import com.bulletin.entity.Section;
import com.bulletin.entity.Subject;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.CurriculumSubjectMapper;
import com.bulletin.repository.CurriculumRepository;
import com.bulletin.repository.CurriculumSubjectRepository;
import com.bulletin.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumSubjectService {

    private final CurriculumSubjectRepository curriculumSubjectRepository;
    private final CurriculumRepository curriculumRepository;
    private final SubjectRepository subjectRepository;
    private final CurriculumSubjectMapper curriculumSubjectMapper;
    private final AutoOrdreService autoOrdreService;

    @Transactional
    public CurriculumSubjectResponse createCurriculumSubject(CurriculumSubjectRequest request) {
        Curriculum curriculum = findCurriculum(request.getCurriculumId());
        Subject subject = findSubject(request.getSubjectId());
        Long schoolId = schoolIdOf(curriculum);
        // L'ordre est calculé côté serveur (max + 1) : la valeur fournie par le client est ignorée.
        CurriculumSubjectResponse response = AutoOrdreRetry.retry(autoOrdreService,
                () -> curriculumSubjectRepository.maxOrdreByCurriculumIdAndSchoolId(request.getCurriculumId(), schoolId),
                ordre -> {
                    CurriculumSubject cs = curriculumSubjectMapper.toEntity(request);
                    cs.setCurriculum(curriculum);
                    cs.setSubject(subject);
                    cs.setSchoolId(schoolId);
                    cs.setOrdre(ordre);
                    CurriculumSubject saved = curriculumSubjectRepository.save(cs);
                    log.info("Matière de programme créée: {}", saved.getId());
                    return curriculumSubjectMapper.toResponse(saved);
                });
        return response;
    }

    @Transactional(readOnly = true)
    public CurriculumSubjectResponse getCurriculumSubject(Long id) {
        return curriculumSubjectMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<CurriculumSubjectResponse> getAllCurriculumSubjects() {
        return curriculumSubjectRepository.findAll().stream()
                .map(curriculumSubjectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumSubjectResponse> getByCurriculum(Long curriculumId) {
        return curriculumSubjectRepository.findByCurriculumId(curriculumId).stream()
                .map(curriculumSubjectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumSubjectResponse> getBySubject(Long subjectId) {
        return curriculumSubjectRepository.findBySubjectId(subjectId).stream()
                .map(curriculumSubjectMapper::toResponse)
                .toList();
    }

    @Transactional
    public CurriculumSubjectResponse updateCurriculumSubject(Long id, CurriculumSubjectRequest request) {
        CurriculumSubject curriculumSubject = findById(id);
        Integer existingOrdre = curriculumSubject.getOrdre();
        curriculumSubjectMapper.updateEntity(request, curriculumSubject);
        curriculumSubject.setOrdre(existingOrdre); // l'ordre est immuable après création
        Curriculum curriculum = findCurriculum(request.getCurriculumId());
        Subject subject = findSubject(request.getSubjectId());
        curriculumSubject.setCurriculum(curriculum);
        curriculumSubject.setSubject(subject);
        curriculumSubject.setSchoolId(schoolIdOf(curriculum));
        CurriculumSubject saved = curriculumSubjectRepository.save(curriculumSubject);
        log.info("Matière de programme mise à jour: {}", saved.getId());
        return curriculumSubjectMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCurriculumSubject(Long id) {
        CurriculumSubject curriculumSubject = findById(id);
        curriculumSubjectRepository.delete(curriculumSubject);
        log.info("Matière de programme supprimée: {}", id);
    }

    public CurriculumSubject findById(Long id) {
        return curriculumSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière de programme non trouvée avec l'ID: " + id));
    }

    private Curriculum findCurriculum(Long id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme non trouvé avec l'ID: " + id));
    }

    private Subject findSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière non trouvée avec l'ID: " + id));
    }

    /**
     * SchoolId d'un programme : dérivé de son level (puis section, puis option),
     * car la table curricula n'a pas de school_id direct.
     */
    private Long schoolIdOf(Curriculum curriculum) {
        Level level = curriculum.getLevel();
        if (level != null && level.getSchoolId() != null) {
            return level.getSchoolId();
        }
        Section section = curriculum.getSection();
        if (section != null && section.getSchoolId() != null) {
            return section.getSchoolId();
        }
        Option option = curriculum.getOption();
        if (option != null && option.getSchoolId() != null) {
            return option.getSchoolId();
        }
        return null;
    }
}

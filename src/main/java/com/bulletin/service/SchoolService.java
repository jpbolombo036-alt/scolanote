package com.bulletin.service;

import com.bulletin.dto.school.SchoolRequest;
import com.bulletin.dto.school.SchoolResponse;
import com.bulletin.entity.School;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.SchoolMapper;
import com.bulletin.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolMapper schoolMapper;

    @Transactional
    public SchoolResponse createSchool(SchoolRequest request) {
        School school = schoolMapper.toEntity(request);
        School saved = schoolRepository.save(school);
        log.info("École créée: {}", saved.getId());
        return schoolMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SchoolResponse getSchool(Long id) {
        return schoolMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(schoolMapper::toResponse)
                .toList();
    }

    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolRequest request) {
        School school = findById(id);
        schoolMapper.updateEntity(request, school);
        School saved = schoolRepository.save(school);
        log.info("École mise à jour: {}", saved.getId());
        return schoolMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = findById(id);
        school.setDeletedAt(java.time.LocalDateTime.now());
        schoolRepository.save(school);
        log.info("École supprimée (soft): {}", id);
    }

    public School findById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("École non trouvée avec l'ID: " + id));
    }
}

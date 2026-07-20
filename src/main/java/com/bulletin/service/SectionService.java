package com.bulletin.service;

import com.bulletin.dto.school.SectionRequest;
import com.bulletin.dto.school.SectionResponse;
import com.bulletin.entity.Section;
import com.bulletin.mapper.SectionMapper;
import com.bulletin.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;

    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        Section section = sectionMapper.toEntity(request);
        Section saved = sectionRepository.save(section);
        log.info("Section créée: {}", saved.getId());
        return sectionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SectionResponse getSection(Long id) {
        return sectionMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getAllSections() {
        return sectionRepository.findAll().stream()
                .map(sectionMapper::toResponse)
                .toList();
    }

    @Transactional
    public SectionResponse updateSection(Long id, SectionRequest request) {
        Section section = findById(id);
        sectionMapper.updateEntity(request, section);
        Section saved = sectionRepository.save(section);
        log.info("Section mise à jour: {}", saved.getId());
        return sectionMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSection(Long id) {
        Section section = findById(id);
        section.setDeletedAt(java.time.LocalDateTime.now());
        sectionRepository.save(section);
        log.info("Section supprimée (soft): {}", id);
    }

    public Section findById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new com.bulletin.exception.ResourceNotFoundException("Section non trouvée avec l'ID: " + id));
    }
}

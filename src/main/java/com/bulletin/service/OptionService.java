package com.bulletin.service;

import com.bulletin.dto.school.OptionRequest;
import com.bulletin.dto.school.OptionResponse;
import com.bulletin.entity.Option;
import com.bulletin.entity.Section;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.OptionMapper;
import com.bulletin.repository.OptionRepository;
import com.bulletin.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionService {

    private final OptionRepository optionRepository;
    private final SectionRepository sectionRepository;
    private final OptionMapper optionMapper;

    @Transactional
    public OptionResponse createOption(OptionRequest request) {
        Option option = optionMapper.toEntity(request);
        option.setSection(findSection(request.getSectionId()));
        Option saved = optionRepository.save(option);
        log.info("Option créée: {}", saved.getId());
        return optionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OptionResponse getOption(Long id) {
        return optionMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getAllOptions() {
        return optionRepository.findAll().stream()
                .map(optionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getOptionsBySection(Long sectionId) {
        return optionRepository.findBySectionId(sectionId).stream()
                .map(optionMapper::toResponse)
                .toList();
    }

    @Transactional
    public OptionResponse updateOption(Long id, OptionRequest request) {
        Option option = findById(id);
        optionMapper.updateEntity(request, option);
        option.setSection(findSection(request.getSectionId()));
        Option saved = optionRepository.save(option);
        log.info("Option mise à jour: {}", saved.getId());
        return optionMapper.toResponse(saved);
    }

    @Transactional
    public void deleteOption(Long id) {
        Option option = findById(id);
        option.setDeletedAt(java.time.LocalDateTime.now());
        optionRepository.save(option);
        log.info("Option supprimée (soft): {}", id);
    }

    public Option findById(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option non trouvée avec l'ID: " + id));
    }

    private Section findSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section non trouvée avec l'ID: " + id));
    }
}

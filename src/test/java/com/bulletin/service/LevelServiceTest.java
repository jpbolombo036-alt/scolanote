package com.bulletin.service;

import com.bulletin.dto.school.LevelRequest;
import com.bulletin.dto.school.LevelResponse;
import com.bulletin.entity.Level;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.LevelMapper;
import com.bulletin.repository.LevelRepository;
import com.bulletin.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LevelServiceTest {

    @Mock
    LevelRepository levelRepository;
    @Mock
    LevelMapper levelMapper;
    @Mock
    SecurityUtils securityUtils;

    AutoOrdreService autoOrdreService;
    LevelService levelService;

    @BeforeEach
    void setUp() {
        autoOrdreService = new AutoOrdreService();
        levelService = new LevelService(levelRepository, levelMapper, securityUtils, autoOrdreService);
    }

    private static Level level(Long id, String nom, Long schoolId, Integer ordre) {
        Level l = Level.builder().nom(nom).schoolId(schoolId).ordre(ordre).build();
        l.setId(id);
        return l;
    }

    @Test
    void createLevel_computesOrdreServerSide() {
        Long schoolId = 1L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        when(levelRepository.maxOrdreBySchoolId(schoolId)).thenReturn(5);

        LevelRequest request = LevelRequest.builder().nom("L1").ordre(99).build();

        Level levelFromMapper = level(null, "L1", null, null);
        Level savedLevel = level(10L, "L1", schoolId, 6);
        LevelResponse response = LevelResponse.builder().id(10L).nom("L1").ordre(6).build();

        when(levelMapper.toEntity(request)).thenReturn(levelFromMapper);
        when(levelRepository.save(any(Level.class))).thenReturn(savedLevel);
        when(levelMapper.toResponse(savedLevel)).thenReturn(response);

        LevelResponse result = levelService.createLevel(request);

        assertEquals(6, result.getOrdre());

        ArgumentCaptor<Level> captor = ArgumentCaptor.forClass(Level.class);
        verify(levelRepository).save(captor.capture());
        Level captured = captor.getValue();
        assertEquals(schoolId, captured.getSchoolId());
        assertEquals(6, captured.getOrdre());
    }

    @Test
    void createLevel_setsOrdreToOneWhenScopeIsEmpty() {
        Long schoolId = 1L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        when(levelRepository.maxOrdreBySchoolId(schoolId)).thenReturn((Integer) null);

        LevelRequest request = LevelRequest.builder().nom("L1").build();

        Level levelFromMapper = level(null, "L1", null, null);
        Level savedLevel = level(10L, "L1", schoolId, 1);
        LevelResponse response = LevelResponse.builder().id(10L).nom("L1").ordre(1).build();

        when(levelMapper.toEntity(request)).thenReturn(levelFromMapper);
        when(levelRepository.save(any(Level.class))).thenReturn(savedLevel);
        when(levelMapper.toResponse(savedLevel)).thenReturn(response);

        LevelResponse result = levelService.createLevel(request);

        assertEquals(1, result.getOrdre());
    }

    @Test
    void createLevel_throwsSecurityExceptionWhenSchoolIdIsNull() {
        when(securityUtils.getCurrentSchoolId()).thenReturn(null);

        LevelRequest request = LevelRequest.builder().nom("L1").build();

        assertThrows(SecurityException.class, () -> levelService.createLevel(request));
    }

    @Test
    void updateLevel_preservesExistingOrdre() {
        Long schoolId = 1L;
        Long levelId = 10L;

        Level existing = level(levelId, "Old", schoolId, 7);
        Level saved = level(levelId, "New", schoolId, 7);
        LevelResponse response = LevelResponse.builder().id(levelId).nom("New").ordre(7).build();

        when(levelRepository.findById(levelId)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            LevelRequest req = invocation.getArgument(0);
            Level target = invocation.getArgument(1);
            target.setNom(req.getNom());
            return null;
        }).when(levelMapper).updateEntity(any(LevelRequest.class), eq(existing));
        when(levelRepository.save(existing)).thenReturn(saved);
        when(levelMapper.toResponse(saved)).thenReturn(response);

        LevelRequest request = LevelRequest.builder().nom("New").build();
        LevelResponse result = levelService.updateLevel(levelId, request);

        assertEquals(7, result.getOrdre());
        assertEquals(7, existing.getOrdre());
    }

    @Test
    void getAccessibleLevels_superAdminReturnsAll() {
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        Level l1 = level(1L, "A", 1L, 1);
        Level l2 = level(2L, "B", 2L, 2);
        LevelResponse r1 = LevelResponse.builder().id(1L).nom("A").ordre(1).build();
        LevelResponse r2 = LevelResponse.builder().id(2L).nom("B").ordre(2).build();

        when(levelRepository.findAll()).thenReturn(List.of(l1, l2));
        when(levelMapper.toResponse(l1)).thenReturn(r1);
        when(levelMapper.toResponse(l2)).thenReturn(r2);

        List<LevelResponse> result = levelService.getAccessibleLevels();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getAccessibleLevels_nonSuperAdminFiltersBySchoolId() {
        Long schoolId = 1L;
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);

        Level l1 = level(1L, "A", schoolId, 1);
        Level l2 = level(2L, "B", schoolId, 2);
        LevelResponse r1 = LevelResponse.builder().id(1L).nom("A").ordre(1).build();
        LevelResponse r2 = LevelResponse.builder().id(2L).nom("B").ordre(2).build();

        when(levelRepository.findBySchoolId(schoolId)).thenReturn(List.of(l1, l2));
        when(levelMapper.toResponse(l1)).thenReturn(r1);
        when(levelMapper.toResponse(l2)).thenReturn(r2);

        List<LevelResponse> result = levelService.getAccessibleLevels();

        assertEquals(2, result.size());
        verify(levelRepository).findBySchoolId(schoolId);
    }

    @Test
    void getAccessibleLevels_nonSuperAdminThrowsWhenSchoolIdNull() {
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.getCurrentSchoolId()).thenReturn(null);

        assertThrows(SecurityException.class, () -> levelService.getAccessibleLevels());
    }

    @Test
    void getLevel_returnsResponseById() {
        Long schoolId = 1L;
        Long levelId = 10L;
        Level level = level(levelId, "L1", schoolId, 1);
        LevelResponse response = LevelResponse.builder().id(levelId).nom("L1").ordre(1).build();

        when(levelRepository.findById(levelId)).thenReturn(Optional.of(level));
        when(levelMapper.toResponse(level)).thenReturn(response);

        LevelResponse result = levelService.getLevel(levelId);

        assertEquals(levelId, result.getId());
        assertEquals("L1", result.getNom());
    }

    @Test
    void getLevel_throwsWhenNotFound() {
        Long levelId = 99L;
        Long schoolId = 1L;

        when(levelRepository.findById(levelId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> levelService.getLevel(levelId));
    }

    @Test
    void deleteLevel_softDeletes() {
        Long levelId = 10L;
        Long schoolId = 1L;
        Level existing = level(levelId, "L1", schoolId, 1);

        when(levelRepository.findById(levelId)).thenReturn(Optional.of(existing));

        levelService.deleteLevel(levelId);

        assertNotNull(existing.getDeletedAt());
        verify(levelRepository).save(existing);
    }

    @Test
    void createLevel_retriesOnConflict() {
        Long schoolId = 1L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        when(levelRepository.maxOrdreBySchoolId(schoolId)).thenReturn((Integer) null, (Integer) null);

        LevelRequest request = LevelRequest.builder().nom("L1").build();

        Level levelFromMapper = level(null, "L1", null, null);
        Level savedLevel = level(10L, "L1", schoolId, 1);
        LevelResponse response = LevelResponse.builder().id(10L).nom("L1").ordre(1).build();

        when(levelMapper.toEntity(request)).thenReturn(levelFromMapper);
        when(levelMapper.toResponse(savedLevel)).thenReturn(response);

        AtomicInteger saveAttempts = new AtomicInteger();
        when(levelRepository.save(any(Level.class)))
                .thenAnswer(inv -> {
                    if (saveAttempts.incrementAndGet() == 1) {
                        throw new DataIntegrityViolationException("conflict");
                    }
                    return savedLevel;
                });

        LevelResponse result = levelService.createLevel(request);
        assertEquals(1, result.getOrdre());
        verify(levelRepository, atLeast(2)).save(any(Level.class));
    }
}

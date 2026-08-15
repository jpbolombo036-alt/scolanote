package com.bulletin.service;

import com.bulletin.dto.school.TrimesterRequest;
import com.bulletin.dto.school.TrimesterResponse;
import com.bulletin.entity.AcademicYear;
import com.bulletin.entity.Trimester;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.TrimesterMapper;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.TrimesterRepository;
import com.bulletin.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrimesterServiceTest {

    @Mock
    TrimesterRepository trimesterRepository;
    @Mock
    AcademicYearRepository academicYearRepository;
    @Mock
    TrimesterMapper trimesterMapper;
    @Mock
    SecurityUtils securityUtils;

    AutoOrdreService autoOrdreService;
    TrimesterService trimesterService;

    @BeforeEach
    void setUp() {
        autoOrdreService = new AutoOrdreService();
        trimesterService = new TrimesterService(
                trimesterRepository, academicYearRepository, trimesterMapper, securityUtils, autoOrdreService);
    }

    private static AcademicYear academicYear(Long id, String libelle) {
        AcademicYear ay = AcademicYear.builder().libelle(libelle).build();
        ay.setId(id);
        return ay;
    }

    private static Trimester trimester(Long id, String nom, Long schoolId, Integer ordre) {
        Trimester t = Trimester.builder().nom(nom).schoolId(schoolId).ordre(ordre).build();
        t.setId(id);
        return t;
    }

    @Test
    void createTrimester_computesOrdreServerSide() {
        Long schoolId = 1L;
        Long ayId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        AcademicYear ay = academicYear(ayId, "AY1");
        when(academicYearRepository.findById(ayId)).thenReturn(Optional.of(ay));
        when(trimesterRepository.maxOrdreByAcademicYearIdAndSchoolId(ayId, schoolId)).thenReturn(3);

        TrimesterRequest request = TrimesterRequest.builder()
                .academicYearId(ayId).nom("T1").ordre(99).dateDebut(LocalDate.now()).build();

        Trimester fromMapper = Trimester.builder().nom("T1").build();
        Trimester saved = trimester(10L, "T1", schoolId, 4);
        saved.setAcademicYear(ay);
        TrimesterResponse response = TrimesterResponse.builder().id(10L).nom("T1").ordre(4)
                .academicYearId(ayId).academicYearLibelle("AY1").build();

        when(trimesterMapper.toEntity(request)).thenReturn(fromMapper);
        when(trimesterRepository.save(any(Trimester.class))).thenReturn(saved);
        when(trimesterMapper.toResponse(saved)).thenReturn(response);

        TrimesterResponse result = trimesterService.createTrimester(request);

        assertEquals(4, result.getOrdre());

        ArgumentCaptor<Trimester> captor = ArgumentCaptor.forClass(Trimester.class);
        verify(trimesterRepository).save(captor.capture());
        Trimester captured = captor.getValue();
        assertEquals(schoolId, captured.getSchoolId());
        assertEquals(ay, captured.getAcademicYear());
        assertEquals(4, captured.getOrdre());
    }

    @Test
    void createTrimester_setsOrdreToOneWhenScopeIsEmpty() {
        Long schoolId = 1L;
        Long ayId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        AcademicYear ay = academicYear(ayId, "AY1");
        when(academicYearRepository.findById(ayId)).thenReturn(Optional.of(ay));
        when(trimesterRepository.maxOrdreByAcademicYearIdAndSchoolId(ayId, schoolId)).thenReturn((Integer) null);

        TrimesterRequest request = TrimesterRequest.builder().academicYearId(ayId).nom("T1").build();

        Trimester fromMapper = Trimester.builder().nom("T1").build();
        Trimester saved = trimester(10L, "T1", schoolId, 1);
        saved.setAcademicYear(ay);
        TrimesterResponse response = TrimesterResponse.builder().id(10L).nom("T1").ordre(1).academicYearId(ayId).build();

        when(trimesterMapper.toEntity(request)).thenReturn(fromMapper);
        when(trimesterRepository.save(any(Trimester.class))).thenReturn(saved);
        when(trimesterMapper.toResponse(saved)).thenReturn(response);

        TrimesterResponse result = trimesterService.createTrimester(request);

        assertEquals(1, result.getOrdre());
    }

    @Test
    void createTrimester_throwsSecurityExceptionWhenSchoolIdNull() {
        when(securityUtils.getCurrentSchoolId()).thenReturn(null);
        TrimesterRequest request = TrimesterRequest.builder().academicYearId(5L).nom("T1").build();
        assertThrows(SecurityException.class, () -> trimesterService.createTrimester(request));
    }

    @Test
    void createTrimester_throwsWhenAcademicYearNotFound() {
        Long schoolId = 1L;
        Long ayId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        when(academicYearRepository.findById(ayId)).thenReturn(Optional.empty());

        TrimesterRequest request = TrimesterRequest.builder().academicYearId(ayId).nom("T1").build();

        assertThrows(ResourceNotFoundException.class, () -> trimesterService.createTrimester(request));
    }

    @Test
    void updateTrimester_preservesExistingOrdre() {
        Long schoolId = 1L;
        Long trimesterId = 10L;
        AcademicYear ay = academicYear(5L, "AY1");

        Trimester existing = trimester(trimesterId, "T1", schoolId, 2);
        existing.setAcademicYear(ay);
        Trimester saved = trimester(trimesterId, "T2", schoolId, 2);
        saved.setAcademicYear(ay);
        TrimesterResponse response = TrimesterResponse.builder().id(trimesterId).nom("T2").ordre(2).academicYearId(5L).build();

        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.of(existing));
        when(academicYearRepository.findById(5L)).thenReturn(Optional.of(ay));
        doAnswer(inv -> {
            TrimesterRequest req = inv.getArgument(0);
            Trimester target = inv.getArgument(1);
            target.setNom(req.getNom());
            return null;
        }).when(trimesterMapper).updateEntity(any(TrimesterRequest.class), eq(existing));
        when(trimesterRepository.save(existing)).thenReturn(saved);
        when(trimesterMapper.toResponse(saved)).thenReturn(response);

        TrimesterRequest request = TrimesterRequest.builder().academicYearId(5L).nom("T2").build();
        TrimesterResponse result = trimesterService.updateTrimester(trimesterId, request);

        assertEquals(2, result.getOrdre());
        assertEquals(2, existing.getOrdre());
    }

    @Test
    void getAccessibleTrimesters_superAdminReturnsAll() {
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        AcademicYear ay1 = academicYear(1L, "AY1");
        AcademicYear ay2 = academicYear(2L, "AY2");
        Trimester t1 = trimester(1L, "T1", 1L, 1);
        t1.setAcademicYear(ay1);
        Trimester t2 = trimester(2L, "T2", 2L, 2);
        t2.setAcademicYear(ay2);
        TrimesterResponse r1 = TrimesterResponse.builder().id(1L).nom("T1").ordre(1).academicYearId(1L).build();
        TrimesterResponse r2 = TrimesterResponse.builder().id(2L).nom("T2").ordre(2).academicYearId(2L).build();

        when(trimesterRepository.findAll()).thenReturn(List.of(t1, t2));
        when(trimesterMapper.toResponse(t1)).thenReturn(r1);
        when(trimesterMapper.toResponse(t2)).thenReturn(r2);

        List<TrimesterResponse> result = trimesterService.getAccessibleTrimesters();

        assertEquals(2, result.size());
    }

    @Test
    void getAccessibleTrimesters_nonSuperAdminFiltersBySchoolId() {
        Long schoolId = 1L;
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);

        AcademicYear ay = academicYear(1L, "AY1");
        Trimester t1 = trimester(1L, "T1", schoolId, 1);
        t1.setAcademicYear(ay);
        Trimester t2 = trimester(2L, "T2", schoolId, 2);
        t2.setAcademicYear(ay);
        TrimesterResponse r1 = TrimesterResponse.builder().id(1L).nom("T1").ordre(1).build();
        TrimesterResponse r2 = TrimesterResponse.builder().id(2L).nom("T2").ordre(2).build();

        when(trimesterRepository.findBySchoolId(schoolId)).thenReturn(List.of(t1, t2));
        when(trimesterMapper.toResponse(t1)).thenReturn(r1);
        when(trimesterMapper.toResponse(t2)).thenReturn(r2);

        List<TrimesterResponse> result = trimesterService.getAccessibleTrimesters();

        assertEquals(2, result.size());
        verify(trimesterRepository).findBySchoolId(schoolId);
    }

    @Test
    void getAccessibleTrimesters_throwsWhenSchoolIdNull() {
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.getCurrentSchoolId()).thenReturn(null);

        assertThrows(SecurityException.class, () -> trimesterService.getAccessibleTrimesters());
    }

    @Test
    void getAccessibleTrimesters_page_superAdmin() {
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        AcademicYear ay = academicYear(1L, "AY1");
        Trimester t1 = trimester(1L, "T1", 1L, 1);
        t1.setAcademicYear(ay);
        TrimesterResponse r1 = TrimesterResponse.builder().id(1L).nom("T1").ordre(1).academicYearId(1L).build();

        Page<Trimester> page = new PageImpl<>(List.of(t1));
        when(trimesterRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(trimesterMapper.toResponse(t1)).thenReturn(r1);

        Page<TrimesterResponse> result = trimesterService.getAccessibleTrimesters(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getTrimestersByAcademicYear_returnsFiltered() {
        Long ayId = 5L;
        AcademicYear ay = academicYear(ayId, "AY1");
        Trimester t1 = trimester(1L, "T1", 1L, 1);
        t1.setAcademicYear(ay);
        Trimester t2 = trimester(2L, "T2", 1L, 2);
        t2.setAcademicYear(ay);
        TrimesterResponse r1 = TrimesterResponse.builder().id(1L).nom("T1").ordre(1).academicYearId(ayId).build();
        TrimesterResponse r2 = TrimesterResponse.builder().id(2L).nom("T2").ordre(2).academicYearId(ayId).build();

        when(trimesterRepository.findByAcademicYearId(ayId)).thenReturn(List.of(t1, t2));
        when(trimesterMapper.toResponse(t1)).thenReturn(r1);
        when(trimesterMapper.toResponse(t2)).thenReturn(r2);

        List<TrimesterResponse> result = trimesterService.getTrimestersByAcademicYear(ayId);

        assertEquals(2, result.size());
    }

    @Test
    void getTrimestersByAcademicYear_filtersNullAcademicYear() {
        Trimester t = Trimester.builder().nom("T1").build();
        t.setId(1L);
        when(trimesterRepository.findByAcademicYearId(5L)).thenReturn(List.of(t));

        List<TrimesterResponse> result = trimesterService.getTrimestersByAcademicYear(5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteTrimester_softDeletes() {
        Long trimesterId = 10L;
        Long schoolId = 1L;
        AcademicYear ay = academicYear(5L, "AY1");
        Trimester existing = trimester(trimesterId, "T1", schoolId, 1);
        existing.setAcademicYear(ay);

        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.of(existing));

        trimesterService.deleteTrimester(trimesterId);

        assertNotNull(existing.getDeletedAt());
        verify(trimesterRepository).save(existing);
    }

    @Test
    void createTrimester_retriesOnConflict() {
        Long schoolId = 1L;
        Long ayId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        AcademicYear ay = academicYear(ayId, "AY1");
        when(academicYearRepository.findById(ayId)).thenReturn(Optional.of(ay));
        when(trimesterRepository.maxOrdreByAcademicYearIdAndSchoolId(ayId, schoolId)).thenReturn((Integer) null, (Integer) null);

        TrimesterRequest request = TrimesterRequest.builder().academicYearId(ayId).nom("T1").build();

        Trimester fromMapper = Trimester.builder().nom("T1").build();
        Trimester saved = trimester(10L, "T1", schoolId, 1);
        saved.setAcademicYear(ay);
        TrimesterResponse response = TrimesterResponse.builder().id(10L).nom("T1").ordre(1).academicYearId(ayId).build();

        when(trimesterMapper.toEntity(request)).thenReturn(fromMapper);
        when(trimesterMapper.toResponse(saved)).thenReturn(response);

        AtomicInteger saveAttempts = new AtomicInteger();
        when(trimesterRepository.save(any(Trimester.class)))
                .thenAnswer(inv -> {
                    if (saveAttempts.incrementAndGet() == 1) {
                        throw new DataIntegrityViolationException("conflict");
                    }
                    return saved;
                });

        TrimesterResponse result = trimesterService.createTrimester(request);
        assertEquals(1, result.getOrdre());
        verify(trimesterRepository, atLeast(2)).save(any(Trimester.class));
    }
}

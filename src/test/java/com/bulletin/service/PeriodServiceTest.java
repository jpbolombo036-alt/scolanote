package com.bulletin.service;

import com.bulletin.dto.school.PeriodRequest;
import com.bulletin.dto.school.PeriodResponse;
import com.bulletin.entity.Period;
import com.bulletin.entity.Period.PeriodType;
import com.bulletin.entity.Trimester;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.PeriodMapper;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.PeriodRepository;
import com.bulletin.repository.TrimesterRepository;
import com.bulletin.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodServiceTest {

    @Mock
    PeriodRepository periodRepository;
    @Mock
    TrimesterRepository trimesterRepository;
    @Mock
    AcademicYearRepository academicYearRepository;
    @Mock
    PeriodMapper periodMapper;
    @Mock
    SecurityUtils securityUtils;

    AutoOrdreService autoOrdreService;
    PeriodService periodService;

    @BeforeEach
    void setUp() {
        autoOrdreService = new AutoOrdreService();
        periodService = new PeriodService(
                periodRepository, trimesterRepository, academicYearRepository,
                periodMapper, securityUtils, autoOrdreService);
    }

    private static Trimester trimester(Long id, Long schoolId) {
        Trimester t = Trimester.builder().nom("T1").schoolId(schoolId).ordre(1).build();
        t.setId(id);
        return t;
    }

    private static Period period(Long id, String nom, Long schoolId, Integer ordre, Trimester trimester) {
        Period p = Period.builder().nom(nom).schoolId(schoolId).ordre(ordre).type(PeriodType.PERIODE).build();
        p.setId(id);
        p.setTrimester(trimester);
        return p;
    }

    @Test
    void createPeriod_computesOrdreServerSide() {
        Long schoolId = 1L;
        Long trimesterId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        Trimester trimester = trimester(trimesterId, schoolId);
        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.of(trimester));
        when(periodRepository.maxOrdreByTrimesterIdAndSchoolId(trimesterId, schoolId)).thenReturn(2);

        PeriodRequest request = PeriodRequest.builder()
                .trimesterId(trimesterId).nom("P1").ordre(99)
                .type(PeriodType.PERIODE).dateDebut(LocalDate.now()).build();

        Period fromMapper = Period.builder().nom("P1").type(PeriodType.PERIODE).build();
        Period saved = period(10L, "P1", schoolId, 3, trimester);
        PeriodResponse response = PeriodResponse.builder().id(10L).nom("P1").ordre(3)
                .trimesterId(trimesterId).trimesterNom("T1").type(PeriodType.PERIODE).build();

        when(periodMapper.toEntity(request)).thenReturn(fromMapper);
        when(periodRepository.save(any(Period.class))).thenReturn(saved);
        when(periodMapper.toResponse(saved)).thenReturn(response);

        PeriodResponse result = periodService.createPeriod(request);

        assertEquals(3, result.getOrdre());

        ArgumentCaptor<Period> captor = ArgumentCaptor.forClass(Period.class);
        verify(periodRepository).save(captor.capture());
        Period captured = captor.getValue();
        assertEquals(schoolId, captured.getSchoolId());
        assertEquals(trimester, captured.getTrimester());
        assertEquals(3, captured.getOrdre());
        assertFalse(captured.isVerrouille());
    }

    @Test
    void createPeriod_setsOrdreToOneWhenScopeIsEmpty() {
        Long schoolId = 1L;
        Long trimesterId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        Trimester trimester = trimester(trimesterId, schoolId);
        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.of(trimester));
        when(periodRepository.maxOrdreByTrimesterIdAndSchoolId(trimesterId, schoolId)).thenReturn((Integer) null);

        PeriodRequest request = PeriodRequest.builder().trimesterId(trimesterId).nom("P1").build();

        Period fromMapper = Period.builder().nom("P1").build();
        Period saved = period(10L, "P1", schoolId, 1, trimester);
        PeriodResponse response = PeriodResponse.builder().id(10L).nom("P1").ordre(1).trimesterId(trimesterId).build();

        when(periodMapper.toEntity(request)).thenReturn(fromMapper);
        when(periodRepository.save(any(Period.class))).thenReturn(saved);
        when(periodMapper.toResponse(saved)).thenReturn(response);

        PeriodResponse result = periodService.createPeriod(request);

        assertEquals(1, result.getOrdre());
    }

    @Test
    void createPeriod_throwsWhenTrimesterNotFound() {
        Long schoolId = 1L;
        Long trimesterId = 99L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.empty());

        PeriodRequest request = PeriodRequest.builder().trimesterId(trimesterId).nom("P1").build();

        assertThrows(ResourceNotFoundException.class, () -> periodService.createPeriod(request));
    }

    @Test
    void createPeriod_throwsSecurityExceptionWhenSchoolIdNull() {
        when(securityUtils.getCurrentSchoolId()).thenReturn(null);
        PeriodRequest request = PeriodRequest.builder().trimesterId(5L).nom("P1").build();
        assertThrows(SecurityException.class, () -> periodService.createPeriod(request));
    }

    @Test
    void updatePeriod_preservesExistingOrdre() {
        Long schoolId = 1L;
        Long periodId = 10L;
        Long trimesterId = 5L;
        Trimester trimester = trimester(trimesterId, schoolId);

        Period existing = period(periodId, "P1", schoolId, 3, trimester);
        Period saved = period(periodId, "P2", schoolId, 3, trimester);
        PeriodResponse response = PeriodResponse.builder().id(periodId).nom("P2").ordre(3)
                .trimesterId(trimesterId).trimesterNom("T1").build();

        when(periodRepository.findById(periodId)).thenReturn(Optional.of(existing));
        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.of(trimester));
        doAnswer(inv -> {
            PeriodRequest req = inv.getArgument(0);
            Period target = inv.getArgument(1);
            target.setNom(req.getNom());
            return null;
        }).when(periodMapper).updateEntity(any(PeriodRequest.class), eq(existing));
        when(periodRepository.save(existing)).thenReturn(saved);
        when(periodMapper.toResponse(saved)).thenReturn(response);

        PeriodRequest request = PeriodRequest.builder().trimesterId(trimesterId).nom("P2").build();
        PeriodResponse result = periodService.updatePeriod(periodId, request);

        assertEquals(3, result.getOrdre());
        assertEquals(3, existing.getOrdre());
    }

    @Test
    void getAccessiblePeriods_superAdminReturnsAll() {
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        Trimester t = trimester(1L, 1L);
        Period p1 = period(1L, "P1", 1L, 1, t);
        Period p2 = period(2L, "P2", 2L, 2, t);
        PeriodResponse r1 = PeriodResponse.builder().id(1L).nom("P1").ordre(1).trimesterId(1L).trimesterNom("T1").build();
        PeriodResponse r2 = PeriodResponse.builder().id(2L).nom("P2").ordre(2).trimesterId(1L).trimesterNom("T1").build();

        when(periodRepository.findAll()).thenReturn(List.of(p1, p2));
        when(periodMapper.toResponse(p1)).thenReturn(r1);
        when(periodMapper.toResponse(p2)).thenReturn(r2);

        List<PeriodResponse> result = periodService.getAccessiblePeriods();

        assertEquals(2, result.size());
    }

    @Test
    void getAccessiblePeriods_nonSuperAdminFiltersBySchoolId() {
        Long schoolId = 1L;
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);

        Trimester t = trimester(1L, schoolId);
        Period p1 = period(1L, "P1", schoolId, 1, t);
        PeriodResponse r1 = PeriodResponse.builder().id(1L).nom("P1").ordre(1).trimesterId(1L).trimesterNom("T1").build();

        when(periodRepository.findBySchoolId(schoolId)).thenReturn(List.of(p1));
        when(periodMapper.toResponse(p1)).thenReturn(r1);

        List<PeriodResponse> result = periodService.getAccessiblePeriods();

        assertEquals(1, result.size());
        verify(periodRepository).findBySchoolId(schoolId);
    }

    @Test
    void getPeriodsByTrimester_returnsFiltered() {
        Long trimesterId = 5L;
        Trimester t = trimester(trimesterId, 1L);
        Period p1 = period(1L, "P1", 1L, 1, t);
        Period p2 = period(2L, "P2", 1L, 2, t);
        PeriodResponse r1 = PeriodResponse.builder().id(1L).nom("P1").ordre(1).trimesterId(trimesterId).trimesterNom("T1").build();
        PeriodResponse r2 = PeriodResponse.builder().id(2L).nom("P2").ordre(2).trimesterId(trimesterId).trimesterNom("T1").build();

        when(periodRepository.findByTrimesterId(trimesterId)).thenReturn(List.of(p1, p2));
        when(periodMapper.toResponse(p1)).thenReturn(r1);
        when(periodMapper.toResponse(p2)).thenReturn(r2);

        List<PeriodResponse> result = periodService.getPeriodsByTrimester(trimesterId);

        assertEquals(2, result.size());
    }

    @Test
    void getPeriodsByTrimester_filtersNullTrimester() {
        Period p = Period.builder().nom("P1").build();
        p.setId(1L);
        when(periodRepository.findByTrimesterId(5L)).thenReturn(List.of(p));

        List<PeriodResponse> result = periodService.getPeriodsByTrimester(5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getVerrouillees_returnsOnlyLocked() {
        Trimester t = trimester(1L, 1L);
        Period p1 = period(1L, "P1", 1L, 1, t);
        p1.setVerrouille(true);
        Period p2 = period(2L, "P2", 1L, 2, t);
        p2.setVerrouille(true);
        PeriodResponse r1 = PeriodResponse.builder().id(1L).nom("P1").ordre(1).verrouille(true).build();
        PeriodResponse r2 = PeriodResponse.builder().id(2L).nom("P2").ordre(2).verrouille(true).build();

        when(periodRepository.findByVerrouilleTrue()).thenReturn(List.of(p1, p2));
        when(periodMapper.toResponse(p1)).thenReturn(r1);
        when(periodMapper.toResponse(p2)).thenReturn(r2);

        List<PeriodResponse> result = periodService.getVerrouillees();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.isVerrouille()));
    }

    @Test
    void getOuvertes_returnsOnlyOpen() {
        Trimester t = trimester(1L, 1L);
        Period p1 = period(1L, "P1", 1L, 1, t);
        p1.setVerrouille(false);
        PeriodResponse r1 = PeriodResponse.builder().id(1L).nom("P1").ordre(1).verrouille(false).build();

        when(periodRepository.findByVerrouilleFalse()).thenReturn(List.of(p1));
        when(periodMapper.toResponse(p1)).thenReturn(r1);

        List<PeriodResponse> result = periodService.getOuvertes();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isVerrouille());
    }

    @Test
    void deletePeriod_softDeletes() {
        Long periodId = 10L;
        Long schoolId = 1L;
        Trimester t = trimester(1L, schoolId);
        Period existing = period(periodId, "P1", schoolId, 1, t);

        when(periodRepository.findById(periodId)).thenReturn(Optional.of(existing));

        periodService.deletePeriod(periodId);

        assertNotNull(existing.getDeletedAt());
        verify(periodRepository).save(existing);
    }

    @Test
    void createPeriod_retriesOnConflict() {
        Long schoolId = 1L;
        Long trimesterId = 5L;
        when(securityUtils.getCurrentSchoolId()).thenReturn(schoolId);
        Trimester trimester = trimester(trimesterId, schoolId);
        when(trimesterRepository.findById(trimesterId)).thenReturn(Optional.of(trimester));
        when(periodRepository.maxOrdreByTrimesterIdAndSchoolId(trimesterId, schoolId)).thenReturn((Integer) null, (Integer) null);

        PeriodRequest request = PeriodRequest.builder().trimesterId(trimesterId).nom("P1").build();

        Period fromMapper = Period.builder().nom("P1").build();
        Period saved = period(10L, "P1", schoolId, 1, trimester);
        PeriodResponse response = PeriodResponse.builder().id(10L).nom("P1").ordre(1).trimesterId(trimesterId).build();

        when(periodMapper.toEntity(request)).thenReturn(fromMapper);
        when(periodMapper.toResponse(saved)).thenReturn(response);

        AtomicInteger saveAttempts = new AtomicInteger();
        when(periodRepository.save(any(Period.class)))
                .thenAnswer(inv -> {
                    if (saveAttempts.incrementAndGet() == 1) {
                        throw new DataIntegrityViolationException("conflict");
                    }
                    return saved;
                });

        PeriodResponse result = periodService.createPeriod(request);
        assertEquals(1, result.getOrdre());
        verify(periodRepository, atLeast(2)).save(any(Period.class));
    }
}

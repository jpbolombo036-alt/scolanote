package com.bulletin.service;

import com.bulletin.dto.curriculum.CurriculumSubjectRequest;
import com.bulletin.dto.curriculum.CurriculumSubjectResponse;
import com.bulletin.entity.Curriculum;
import com.bulletin.entity.CurriculumSubject;
import com.bulletin.entity.Level;
import com.bulletin.entity.Subject;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.CurriculumSubjectMapper;
import com.bulletin.repository.CurriculumRepository;
import com.bulletin.repository.CurriculumSubjectRepository;
import com.bulletin.repository.SubjectRepository;
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
class CurriculumSubjectServiceTest {

    @Mock
    CurriculumSubjectRepository curriculumSubjectRepository;
    @Mock
    CurriculumRepository curriculumRepository;
    @Mock
    SubjectRepository subjectRepository;
    @Mock
    CurriculumSubjectMapper curriculumSubjectMapper;

    AutoOrdreService autoOrdreService;
    CurriculumSubjectService curriculumSubjectService;

    @BeforeEach
    void setUp() {
        autoOrdreService = new AutoOrdreService();
        curriculumSubjectService = new CurriculumSubjectService(
                curriculumSubjectRepository, curriculumRepository, subjectRepository,
                curriculumSubjectMapper, autoOrdreService);
    }

    private static Level level(Long schoolId) {
        return Level.builder().nom("L1").schoolId(schoolId).build();
    }

    private static Curriculum curriculum(Long id, Long schoolId) {
        Level lvl = level(schoolId);
        Curriculum c = Curriculum.builder().nom("C1").level(lvl).build();
        c.setId(id);
        return c;
    }

    private static Subject subject(Long id, Long schoolId) {
        Subject s = Subject.builder().nom("S1").code("S01").schoolId(schoolId).build();
        s.setId(id);
        return s;
    }

    private static CurriculumSubject cs(Long id, Curriculum curriculum, Subject subject, Long schoolId, Integer ordre) {
        CurriculumSubject cs = CurriculumSubject.builder()
                .curriculum(curriculum).subject(subject).schoolId(schoolId)
                .coefficient(3).ordre(ordre).obligatoire(true).build();
        cs.setId(id);
        return cs;
    }

    @Test
    void createCurriculumSubject_computesOrdreServerSide() {
        Long schoolId = 1L;
        Long curriculumId = 5L;
        Long subjectId = 10L;
        Curriculum curriculum = curriculum(curriculumId, schoolId);
        Subject subject = subject(subjectId, schoolId);

        when(curriculumRepository.findById(curriculumId)).thenReturn(Optional.of(curriculum));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(curriculumSubjectRepository.maxOrdreByCurriculumIdAndSchoolId(curriculumId, schoolId)).thenReturn(4);

        CurriculumSubjectRequest request = CurriculumSubjectRequest.builder()
                .curriculumId(curriculumId).subjectId(subjectId)
                .coefficient(3).ordre(99).obligatoire(true).build();

        CurriculumSubject fromMapper = CurriculumSubject.builder().coefficient(3).obligatoire(true).build();
        CurriculumSubject saved = cs(20L, curriculum, subject, schoolId, 5);
        CurriculumSubjectResponse response = CurriculumSubjectResponse.builder()
                .id(20L).curriculumId(curriculumId).subjectId(subjectId)
                .curriculumNom("C1").subjectNom("S1").subjectCode("S01")
                .coefficient(3).ordre(5).obligatoire(true).build();

        when(curriculumSubjectMapper.toEntity(request)).thenReturn(fromMapper);
        when(curriculumSubjectRepository.save(any(CurriculumSubject.class))).thenReturn(saved);
        when(curriculumSubjectMapper.toResponse(saved)).thenReturn(response);

        CurriculumSubjectResponse result = curriculumSubjectService.createCurriculumSubject(request);

        assertEquals(5, result.getOrdre());

        ArgumentCaptor<CurriculumSubject> captor = ArgumentCaptor.forClass(CurriculumSubject.class);
        verify(curriculumSubjectRepository).save(captor.capture());
        CurriculumSubject captured = captor.getValue();
        assertEquals(schoolId, captured.getSchoolId());
        assertEquals(curriculum, captured.getCurriculum());
        assertEquals(subject, captured.getSubject());
        assertEquals(5, captured.getOrdre());
    }

    @Test
    void createCurriculumSubject_setsOrdreToOneWhenScopeIsEmpty() {
        Long schoolId = 1L;
        Long curriculumId = 5L;
        Long subjectId = 10L;
        Curriculum curriculum = curriculum(curriculumId, schoolId);
        Subject subject = subject(subjectId, schoolId);

        when(curriculumRepository.findById(curriculumId)).thenReturn(Optional.of(curriculum));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(curriculumSubjectRepository.maxOrdreByCurriculumIdAndSchoolId(curriculumId, schoolId)).thenReturn((Integer) null);

        CurriculumSubjectRequest request = CurriculumSubjectRequest.builder()
                .curriculumId(curriculumId).subjectId(subjectId).build();

        CurriculumSubject fromMapper = CurriculumSubject.builder().build();
        CurriculumSubject saved = cs(20L, curriculum, subject, schoolId, 1);
        CurriculumSubjectResponse response = CurriculumSubjectResponse.builder()
                .id(20L).curriculumId(curriculumId).subjectId(subjectId).ordre(1).build();

        when(curriculumSubjectMapper.toEntity(request)).thenReturn(fromMapper);
        when(curriculumSubjectRepository.save(any(CurriculumSubject.class))).thenReturn(saved);
        when(curriculumSubjectMapper.toResponse(saved)).thenReturn(response);

        CurriculumSubjectResponse result = curriculumSubjectService.createCurriculumSubject(request);

        assertEquals(1, result.getOrdre());
    }

    @Test
    void createCurriculumSubject_throwsWhenCurriculumNotFound() {
        Long curriculumId = 99L;
        when(curriculumRepository.findById(curriculumId)).thenReturn(Optional.empty());

        CurriculumSubjectRequest request = CurriculumSubjectRequest.builder()
                .curriculumId(curriculumId).subjectId(10L).build();

        assertThrows(ResourceNotFoundException.class, () -> curriculumSubjectService.createCurriculumSubject(request));
    }

    @Test
    void createCurriculumSubject_throwsWhenSubjectNotFound() {
        Long curriculumId = 5L;
        Long subjectId = 99L;
        Curriculum curriculum = curriculum(curriculumId, 1L);
        when(curriculumRepository.findById(curriculumId)).thenReturn(Optional.of(curriculum));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        CurriculumSubjectRequest request = CurriculumSubjectRequest.builder()
                .curriculumId(curriculumId).subjectId(subjectId).build();

        assertThrows(ResourceNotFoundException.class, () -> curriculumSubjectService.createCurriculumSubject(request));
    }

    @Test
    void updateCurriculumSubject_preservesExistingOrdre() {
        Long schoolId = 1L;
        Long csId = 20L;
        Long curriculumId = 5L;
        Long subjectId = 10L;

        Curriculum curriculum = curriculum(curriculumId, schoolId);
        Subject subject = subject(subjectId, schoolId);
        CurriculumSubject existing = cs(csId, curriculum, subject, schoolId, 7);
        CurriculumSubject saved = cs(csId, curriculum, subject, schoolId, 7);
        saved.setCoefficient(3);
        saved.setObligatoire(true);
        CurriculumSubjectResponse response = CurriculumSubjectResponse.builder()
                .id(csId).curriculumId(curriculumId).subjectId(subjectId).ordre(7).coefficient(3).obligatoire(true).build();

        when(curriculumSubjectRepository.findById(csId)).thenReturn(Optional.of(existing));
        when(curriculumRepository.findById(curriculumId)).thenReturn(Optional.of(curriculum));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        doAnswer(inv -> {
            CurriculumSubjectRequest req = inv.getArgument(0);
            CurriculumSubject target = inv.getArgument(1);
            target.setCoefficient(req.getCoefficient());
            target.setObligatoire(req.isObligatoire());
            return null;
        }).when(curriculumSubjectMapper).updateEntity(any(CurriculumSubjectRequest.class), eq(existing));
        when(curriculumSubjectRepository.save(existing)).thenReturn(saved);
        when(curriculumSubjectMapper.toResponse(saved)).thenReturn(response);

        CurriculumSubjectRequest request = CurriculumSubjectRequest.builder()
                .curriculumId(curriculumId).subjectId(subjectId).coefficient(3).obligatoire(true).build();
        CurriculumSubjectResponse result = curriculumSubjectService.updateCurriculumSubject(csId, request);

        assertEquals(7, result.getOrdre());
        assertEquals(7, existing.getOrdre());
    }

    @Test
    void getAllCurriculumSubjects_returnsAll() {
        CurriculumSubject cs1 = CurriculumSubject.builder().build();
        cs1.setId(1L);
        CurriculumSubject cs2 = CurriculumSubject.builder().build();
        cs2.setId(2L);
        CurriculumSubjectResponse r1 = CurriculumSubjectResponse.builder().id(1L).ordre(1).build();
        CurriculumSubjectResponse r2 = CurriculumSubjectResponse.builder().id(2L).ordre(2).build();

        when(curriculumSubjectRepository.findAll()).thenReturn(List.of(cs1, cs2));
        when(curriculumSubjectMapper.toResponse(cs1)).thenReturn(r1);
        when(curriculumSubjectMapper.toResponse(cs2)).thenReturn(r2);

        List<CurriculumSubjectResponse> result = curriculumSubjectService.getAllCurriculumSubjects();

        assertEquals(2, result.size());
    }

    @Test
    void getByCurriculum_returnsFiltered() {
        Long curriculumId = 5L;
        CurriculumSubject cs1 = CurriculumSubject.builder().build();
        cs1.setId(1L);
        CurriculumSubject cs2 = CurriculumSubject.builder().build();
        cs2.setId(2L);
        CurriculumSubjectResponse r1 = CurriculumSubjectResponse.builder().id(1L).ordre(1).build();
        CurriculumSubjectResponse r2 = CurriculumSubjectResponse.builder().id(2L).ordre(2).build();

        when(curriculumSubjectRepository.findByCurriculumId(curriculumId)).thenReturn(List.of(cs1, cs2));
        when(curriculumSubjectMapper.toResponse(cs1)).thenReturn(r1);
        when(curriculumSubjectMapper.toResponse(cs2)).thenReturn(r2);

        List<CurriculumSubjectResponse> result = curriculumSubjectService.getByCurriculum(curriculumId);

        assertEquals(2, result.size());
    }

    @Test
    void getBySubject_returnsFiltered() {
        Long subjectId = 10L;
        CurriculumSubject cs1 = CurriculumSubject.builder().build();
        cs1.setId(1L);
        CurriculumSubjectResponse r1 = CurriculumSubjectResponse.builder().id(1L).ordre(1).build();

        when(curriculumSubjectRepository.findBySubjectId(subjectId)).thenReturn(List.of(cs1));
        when(curriculumSubjectMapper.toResponse(cs1)).thenReturn(r1);

        List<CurriculumSubjectResponse> result = curriculumSubjectService.getBySubject(subjectId);

        assertEquals(1, result.size());
    }

    @Test
    void getCurriculumSubject_returnsResponseById() {
        Long csId = 20L;
        CurriculumSubject cs = CurriculumSubject.builder().build();
        cs.setId(csId);
        CurriculumSubjectResponse response = CurriculumSubjectResponse.builder().id(csId).ordre(1).build();

        when(curriculumSubjectRepository.findById(csId)).thenReturn(Optional.of(cs));
        when(curriculumSubjectMapper.toResponse(cs)).thenReturn(response);

        CurriculumSubjectResponse result = curriculumSubjectService.getCurriculumSubject(csId);

        assertEquals(csId, result.getId());
    }

    @Test
    void getCurriculumSubject_throwsWhenNotFound() {
        Long csId = 99L;
        when(curriculumSubjectRepository.findById(csId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> curriculumSubjectService.getCurriculumSubject(csId));
    }

    @Test
    void deleteCurriculumSubject_hardDeletes() {
        Long csId = 20L;
        CurriculumSubject existing = CurriculumSubject.builder().build();
        existing.setId(csId);

        when(curriculumSubjectRepository.findById(csId)).thenReturn(Optional.of(existing));

        curriculumSubjectService.deleteCurriculumSubject(csId);

        verify(curriculumSubjectRepository).delete(existing);
        verify(curriculumSubjectRepository, never()).save(any());
    }

    @Test
    void deleteCurriculumSubject_throwsWhenNotFound() {
        Long csId = 99L;
        when(curriculumSubjectRepository.findById(csId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> curriculumSubjectService.deleteCurriculumSubject(csId));
    }

    @Test
    void createCurriculumSubject_retriesOnConflict() {
        Long schoolId = 1L;
        Long curriculumId = 5L;
        Long subjectId = 10L;
        Curriculum curriculum = curriculum(curriculumId, schoolId);
        Subject subject = subject(subjectId, schoolId);

        when(curriculumRepository.findById(curriculumId)).thenReturn(Optional.of(curriculum));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(curriculumSubjectRepository.maxOrdreByCurriculumIdAndSchoolId(curriculumId, schoolId)).thenReturn((Integer) null, (Integer) null);

        CurriculumSubjectRequest request = CurriculumSubjectRequest.builder()
                .curriculumId(curriculumId).subjectId(subjectId).build();

        CurriculumSubject fromMapper = CurriculumSubject.builder().build();
        CurriculumSubject saved = cs(20L, curriculum, subject, schoolId, 1);
        CurriculumSubjectResponse response = CurriculumSubjectResponse.builder()
                .id(20L).curriculumId(curriculumId).subjectId(subjectId).ordre(1).build();

        when(curriculumSubjectMapper.toEntity(request)).thenReturn(fromMapper);
        when(curriculumSubjectMapper.toResponse(saved)).thenReturn(response);

        AtomicInteger saveAttempts = new AtomicInteger();
        when(curriculumSubjectRepository.save(any(CurriculumSubject.class)))
                .thenAnswer(inv -> {
                    if (saveAttempts.incrementAndGet() == 1) {
                        throw new DataIntegrityViolationException("conflict");
                    }
                    return saved;
                });

        CurriculumSubjectResponse result = curriculumSubjectService.createCurriculumSubject(request);
        assertEquals(1, result.getOrdre());
        verify(curriculumSubjectRepository, atLeast(2)).save(any(CurriculumSubject.class));
    }
}

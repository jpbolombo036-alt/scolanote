package com.bulletin.service;

import com.bulletin.dto.tracking.AttendanceRequest;
import com.bulletin.dto.tracking.AttendanceResponse;
import com.bulletin.entity.Attendance;
import com.bulletin.entity.Student;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.AttendanceMapper;
import com.bulletin.repository.AttendanceRepository;
import com.bulletin.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {
        Attendance attendance = attendanceMapper.toEntity(request);
        attendance.setStudent(findStudent(request.getStudentId()));
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Présence créée: {}", saved.getId());
        return attendanceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getAttendance(Long id) {
        return attendanceMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAllAttendances() {
        return attendanceRepository.findAll().stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {
        Attendance attendance = findById(id);
        attendanceMapper.updateEntity(request, attendance);
        attendance.setStudent(findStudent(request.getStudentId()));
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Présence mise à jour: {}", saved.getId());
        return attendanceMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAttendance(Long id) {
        Attendance attendance = findById(id);
        attendance.setDeletedAt(java.time.LocalDateTime.now());
        attendanceRepository.save(attendance);
        log.info("Présence supprimée (soft): {}", id);
    }

    public Attendance findById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Présence non trouvée avec l'ID: " + id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
    }
}

package com.backend.clublago.asistencias;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.backend.clublago.inscripciones.InscripcionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendances")
public class AsistenciaController {

	private static final int MINUTOS_EDICION = 15;

	private final AsistenciaRepository attendanceRepository;
	private final InscripcionRepository enrollmentRepository;

	public AsistenciaController(AsistenciaRepository attendanceRepository, InscripcionRepository enrollmentRepository) {
		this.attendanceRepository = attendanceRepository;
		this.enrollmentRepository = enrollmentRepository;
	}

	@GetMapping
	List<Asistencia> findAll(
		@RequestParam(required = false) Long scheduleId,
		@RequestParam(required = false) LocalDate date,
		@RequestParam(required = false) Long studentId
	) {
		if (studentId != null) {
			return attendanceRepository.findByInscripcionAlumnoIdOrderByFechaAsistenciaDesc(studentId);
		}
		if (scheduleId != null && date != null) {
			return attendanceRepository.findByFechaAsistenciaAndInscripcionHorarioIdOrderByInscripcionAlumnoNombre(date, scheduleId);
		}
		return attendanceRepository.findAll();
	}

	@GetMapping("/roll-call")
	List<FilaPaseLista> rollCall(@RequestParam Long scheduleId, @RequestParam LocalDate date) {
		var records = attendanceRepository.findByFechaAsistenciaAndInscripcionHorarioIdOrderByInscripcionAlumnoNombre(date, scheduleId);
		Map<Long, Asistencia> recordsByEnrollment = records.stream()
			.collect(Collectors.toMap(attendance -> attendance.getEnrollment().getId(), Function.identity()));
		LocalDateTime editableUntil = editableUntil(records);
		boolean locked = isLocked(records, LocalDateTime.now());
		return enrollmentRepository.findByHorarioIdAndActivoTrueOrderByAlumnoNombre(scheduleId)
			.stream()
			.map(enrollment -> {
				var attendance = recordsByEnrollment.get(enrollment.getId());
				return new FilaPaseLista(
					enrollment.getId(),
					attendance == null ? null : attendance.getId(),
					enrollment.getStudent().getId(),
					enrollment.getStudent().getName(),
					enrollment.getStudent().getStatus(),
					enrollment.getFrequencyPerWeek(),
					enrollment.getSelectedDays(),
					attendance == null ? EstatusAsistencia.FALTA : attendance.getStatus(),
					attendance == null ? "" : attendance.getObservations(),
					attendance == null ? null : attendance.getSavedAt(),
					editableUntil,
					locked
				);
			})
			.toList();
	}

	@PostMapping("/roll-call")
	ResponseEntity<List<FilaPaseLista>> saveRollCall(@RequestBody PaseListaRequest request) {
		if (request.records() == null || request.records().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		var records = attendanceRepository.findByFechaAsistenciaAndInscripcionHorarioIdOrderByInscripcionAlumnoNombre(
			request.attendanceDate(),
			request.scheduleId()
		);
		LocalDateTime now = LocalDateTime.now();
		if (isLocked(records, now)) {
			return ResponseEntity.status(423).build();
		}
		LocalDateTime savedAt = savedAt(records);
		if (savedAt == null) {
			savedAt = now;
		}
		for (AsistenciaRequest item : request.records()) {
			var enrollment = enrollmentRepository.findById(item.enrollmentId());
			if (enrollment.isEmpty() || !enrollment.get().getSchedule().getId().equals(request.scheduleId())) {
				return ResponseEntity.badRequest().build();
			}
			var attendance = attendanceRepository
				.findByInscripcionIdAndFechaAsistencia(item.enrollmentId(), request.attendanceDate())
				.orElseGet(Asistencia::new);
			attendance.setEnrollment(enrollment.get());
			attendance.setAttendanceDate(request.attendanceDate());
			attendance.setStatus(item.status());
			attendance.setObservations(item.observations());
			attendance.setSavedAt(attendance.getSavedAt() == null ? savedAt : attendance.getSavedAt());
			attendanceRepository.save(attendance);
		}
		return ResponseEntity.ok(rollCall(request.scheduleId(), request.attendanceDate()));
	}

	@PostMapping
	ResponseEntity<Asistencia> upsert(@RequestBody AsistenciaRequest request) {
		var enrollment = enrollmentRepository.findById(request.enrollmentId());
		if (enrollment.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		var records = attendanceRepository.findByFechaAsistenciaAndInscripcionHorarioIdOrderByInscripcionAlumnoNombre(
			request.attendanceDate(),
			enrollment.get().getSchedule().getId()
		);
		if (isLocked(records, LocalDateTime.now())) {
			return ResponseEntity.status(423).build();
		}
		var attendance = attendanceRepository
			.findByInscripcionIdAndFechaAsistencia(request.enrollmentId(), request.attendanceDate())
			.orElseGet(Asistencia::new);
		attendance.setEnrollment(enrollment.get());
		attendance.setAttendanceDate(request.attendanceDate());
		attendance.setStatus(request.status());
		attendance.setObservations(request.observations());
		if (attendance.getSavedAt() == null) {
			LocalDateTime savedAt = savedAt(records);
			attendance.setSavedAt(savedAt == null ? LocalDateTime.now() : savedAt);
		}
		return ResponseEntity.ok(attendanceRepository.save(attendance));
	}

	private boolean isLocked(List<Asistencia> records, LocalDateTime now) {
		LocalDateTime editableUntil = editableUntil(records);
		return editableUntil != null && now.isAfter(editableUntil);
	}

	private LocalDateTime editableUntil(List<Asistencia> records) {
		LocalDateTime savedAt = savedAt(records);
		return savedAt == null ? null : savedAt.plusMinutes(MINUTOS_EDICION);
	}

	private LocalDateTime savedAt(List<Asistencia> records) {
		return records.stream()
			.map(Asistencia::getSavedAt)
			.filter(savedAt -> savedAt != null)
			.min(LocalDateTime::compareTo)
			.orElse(null);
	}
}

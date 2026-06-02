package com.backend.clublago.asistencias;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
	List<Asistencia> findByFechaAsistenciaAndInscripcionHorarioIdOrderByInscripcionAlumnoNombre(LocalDate attendanceDate, Long scheduleId);
	List<Asistencia> findByInscripcionAlumnoIdOrderByFechaAsistenciaDesc(Long studentId);
	List<Asistencia> findByInscripcionHorarioDisciplinaId(Long disciplineId);
	List<Asistencia> findByInscripcionHorarioMaestroId(Long teacherId);
	Optional<Asistencia> findByInscripcionIdAndFechaAsistencia(Long enrollmentId, LocalDate attendanceDate);
	long countByEstatus(EstatusAsistencia status);
}

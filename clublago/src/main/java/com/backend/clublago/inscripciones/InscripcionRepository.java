package com.backend.clublago.inscripciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
	List<Inscripcion> findAllByOrderByAlumnoNombre();
	List<Inscripcion> findByActivoTrueOrderByAlumnoNombre();
	List<Inscripcion> findByHorarioIdAndActivoTrueOrderByAlumnoNombre(Long scheduleId);
	List<Inscripcion> findByAlumnoIdOrderByHorarioNombre(Long studentId);
	long countByActivoTrue();
	long countByHorarioIdAndActivoTrue(Long scheduleId);
}

package com.backend.clublago.alumnos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
	List<Alumno> findByActivoTrueOrderByNombre();
	long countByActivoTrue();
}

package com.backend.clublago.maestros;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaestroRepository extends JpaRepository<Maestro, Long> {
	List<Maestro> findByActivoTrueOrderByNombre();

	Optional<Maestro> findByUsuarioIgnoreCaseAndContrasenaAndActivoTrue(String username, String password);
}

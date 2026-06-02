package com.backend.clublago.horarios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
	List<Horario> findByActivoTrueOrderByNombre();
	List<Horario> findByDisciplinaIdAndActivoTrueOrderByHoraInicio(Long disciplineId);
}

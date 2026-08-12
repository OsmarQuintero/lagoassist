package com.backend.clublago.disciplinas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
	List<Disciplina> findByActivoTrueOrderByNombre();
}

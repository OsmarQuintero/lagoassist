package com.backend.clublago.horarios;

import java.util.LinkedHashSet;
import java.util.List;

import com.backend.clublago.disciplinas.DisciplinaRepository;
import com.backend.clublago.maestros.MaestroRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class HorarioController {

	private final HorarioRepository horarioRepository;
	private final DisciplinaRepository disciplinaRepository;
	private final MaestroRepository maestroRepository;

	public HorarioController(
		HorarioRepository horarioRepository,
		DisciplinaRepository disciplinaRepository,
		MaestroRepository maestroRepository
	) {
		this.horarioRepository = horarioRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.maestroRepository = maestroRepository;
	}

	@GetMapping
	List<Horario> findAll(
		@RequestParam(defaultValue = "false") boolean activeOnly,
		@RequestParam(required = false) Long disciplineId
	) {
		if (disciplineId != null) {
			return horarioRepository.findByDisciplinaIdAndActivoTrueOrderByHoraInicio(disciplineId);
		}
		return activeOnly ? horarioRepository.findByActivoTrueOrderByNombre() : horarioRepository.findAll();
	}

	@PostMapping
	ResponseEntity<Horario> create(@RequestBody HorarioRequest request) {
		return save(new Horario(), request);
	}

	@PutMapping("/{id}")
	ResponseEntity<Horario> update(@PathVariable Long id, @RequestBody HorarioRequest request) {
		return horarioRepository.findById(id)
			.map(horario -> save(horario, request))
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		return horarioRepository.findById(id)
			.map(horario -> {
				horario.setActive(false);
				horarioRepository.save(horario);
				return ResponseEntity.noContent().<Void>build();
			})
			.orElse(ResponseEntity.notFound().build());
	}

	private ResponseEntity<Horario> save(Horario horario, HorarioRequest request) {
		var disciplina = disciplinaRepository.findById(request.disciplineId());
		var maestro = maestroRepository.findById(request.teacherId());
		if (disciplina.isEmpty() || maestro.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		horario.setName(request.name());
		horario.setDiscipline(disciplina.get());
		horario.setTeacher(maestro.get());
		horario.setDays(request.days() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.days()));
		horario.setStartTime(request.startTime());
		horario.setEndTime(request.endTime());
		horario.setCapacity(request.capacity());
		horario.setActive(request.active());
		return ResponseEntity.ok(horarioRepository.save(horario));
	}
}

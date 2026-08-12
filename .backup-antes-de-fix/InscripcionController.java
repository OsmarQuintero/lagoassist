package com.backend.clublago.inscripciones;

import java.util.LinkedHashSet;
import java.util.List;

import com.backend.clublago.horarios.HorarioRepository;
import com.backend.clublago.alumnos.AlumnoRepository;

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
@RequestMapping("/api/enrollments")
public class InscripcionController {

	private final InscripcionRepository inscripcionRepository;
	private final AlumnoRepository alumnoRepository;
	private final HorarioRepository horarioRepository;

	public InscripcionController(
		InscripcionRepository inscripcionRepository,
		AlumnoRepository alumnoRepository,
		HorarioRepository horarioRepository
	) {
		this.inscripcionRepository = inscripcionRepository;
		this.alumnoRepository = alumnoRepository;
		this.horarioRepository = horarioRepository;
	}

	@GetMapping
	List<Inscripcion> findAll(@RequestParam(required = false) Long scheduleId, @RequestParam(required = false) Long studentId) {
		if (scheduleId != null) {
			return inscripcionRepository.findByHorarioIdAndActivoTrueOrderByAlumnoNombre(scheduleId);
		}
		if (studentId != null) {
			return inscripcionRepository.findByAlumnoIdOrderByHorarioNombre(studentId);
		}
		return inscripcionRepository.findAllByOrderByAlumnoNombre();
	}

	@PostMapping
	ResponseEntity<Inscripcion> create(@RequestBody InscripcionRequest request) {
		return save(new Inscripcion(), request);
	}

	@PutMapping("/{id}")
	ResponseEntity<Inscripcion> update(@PathVariable Long id, @RequestBody InscripcionRequest request) {
		return inscripcionRepository.findById(id)
			.map(inscripcion -> save(inscripcion, request))
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		return inscripcionRepository.findById(id)
			.map(inscripcion -> {
				inscripcion.setActive(false);
				inscripcionRepository.save(inscripcion);
				return ResponseEntity.noContent().<Void>build();
			})
			.orElse(ResponseEntity.notFound().build());
	}

	private ResponseEntity<Inscripcion> save(Inscripcion inscripcion, InscripcionRequest request) {
		var alumno = alumnoRepository.findById(request.studentId());
		var horario = horarioRepository.findById(request.scheduleId());
		if (alumno.isEmpty() || horario.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		long inscritos = inscripcionRepository.countByHorarioIdAndActivoTrue(request.scheduleId());
		if (inscripcion.getId() == null && horario.get().getCapacity() > 0 && inscritos >= horario.get().getCapacity()) {
			return ResponseEntity.badRequest().build();
		}
		inscripcion.setStudent(alumno.get());
		inscripcion.setSchedule(horario.get());
		inscripcion.setFrequencyPerWeek(request.frequencyPerWeek());
		inscripcion.setSelectedDays(request.selectedDays() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.selectedDays()));
		inscripcion.setActive(request.active());
		return ResponseEntity.ok(inscripcionRepository.save(inscripcion));
	}
}

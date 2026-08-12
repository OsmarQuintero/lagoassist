package com.backend.clublago.alumnos;

import java.util.List;

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
@RequestMapping("/api/students")
public class AlumnoController {

	private final AlumnoRepository repository;

	public AlumnoController(AlumnoRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	List<Alumno> findAll(@RequestParam(defaultValue = "false") boolean activeOnly) {
		return activeOnly ? repository.findByActivoTrueOrderByNombre() : repository.findAll();
	}

	@PostMapping
	Alumno create(@RequestBody Alumno student) {
		student.setId(null);
		return repository.save(student);
	}

	@PutMapping("/{id}")
	ResponseEntity<Alumno> update(@PathVariable Long id, @RequestBody Alumno request) {
		return repository.findById(id)
			.map(student -> {
				student.setName(request.getName());
				student.setActionNumber(request.getActionNumber());
				student.setPhone(request.getPhone());
				student.setEmail(request.getEmail());
				student.setStatus(request.getStatus());
				student.setActive(request.isActive());
				return ResponseEntity.ok(repository.save(student));
			})
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		return repository.findById(id)
			.map(student -> {
				student.setActive(false);
				student.setStatus(EstatusAlumno.BAJA);
				repository.save(student);
				return ResponseEntity.noContent().<Void>build();
			})
			.orElse(ResponseEntity.notFound().build());
	}
}

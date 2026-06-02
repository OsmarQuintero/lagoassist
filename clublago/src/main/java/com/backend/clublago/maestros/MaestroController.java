package com.backend.clublago.maestros;

import java.util.LinkedHashSet;
import java.util.List;

import com.backend.clublago.disciplinas.DisciplinaRepository;

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
@RequestMapping("/api/teachers")
public class MaestroController {

	private final MaestroRepository maestroRepository;
	private final DisciplinaRepository disciplinaRepository;

	public MaestroController(MaestroRepository maestroRepository, DisciplinaRepository disciplinaRepository) {
		this.maestroRepository = maestroRepository;
		this.disciplinaRepository = disciplinaRepository;
	}

	@GetMapping
	List<Maestro> findAll(@RequestParam(defaultValue = "false") boolean activeOnly) {
		return activeOnly ? maestroRepository.findByActivoTrueOrderByNombre() : maestroRepository.findAll();
	}

	@PostMapping
	Maestro create(@RequestBody MaestroRequest request) {
		return maestroRepository.save(apply(new Maestro(), request));
	}

	@PostMapping("/login")
	ResponseEntity<Maestro> login(@RequestBody MaestroLoginRequest request) {
		return maestroRepository.findByUsuarioIgnoreCaseAndContrasenaAndActivoTrue(
			clean(request.username()),
			request.password()
		)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.status(401).build());
	}

	@PutMapping("/{id}")
	ResponseEntity<Maestro> update(@PathVariable Long id, @RequestBody MaestroRequest request) {
		return maestroRepository.findById(id)
			.map(maestro -> ResponseEntity.ok(maestroRepository.save(apply(maestro, request))))
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		return maestroRepository.findById(id)
			.map(maestro -> {
				maestro.setActive(false);
				maestroRepository.save(maestro);
				return ResponseEntity.noContent().<Void>build();
			})
			.orElse(ResponseEntity.notFound().build());
	}

	private Maestro apply(Maestro maestro, MaestroRequest request) {
		maestro.setName(request.name());
		maestro.setPhone(request.phone());
		maestro.setEmail(request.email());
		maestro.setUsername(clean(request.username()));
		if (maestro.getId() == null || !isBlank(request.password())) {
			maestro.setPassword(request.password());
		}
		maestro.setActive(request.active());
		var disciplinaIds = request.disciplineIds() == null ? List.<Long>of() : request.disciplineIds();
		maestro.setDisciplines(new LinkedHashSet<>(disciplinaRepository.findAllById(disciplinaIds)));
		return maestro;
	}

	private String clean(String value) {
		return value == null ? "" : value.trim().toLowerCase();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

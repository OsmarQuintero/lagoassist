package com.backend.clublago.disciplinas;

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
@RequestMapping("/api/disciplines")
public class DisciplinaController {

	private final DisciplinaRepository repository;

	public DisciplinaController(DisciplinaRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	List<Disciplina> findAll(@RequestParam(defaultValue = "false") boolean activeOnly) {
		return activeOnly ? repository.findByActivoTrueOrderByNombre() : repository.findAll();
	}

	@PostMapping
	Disciplina create(@RequestBody Disciplina discipline) {
		discipline.setId(null);
		return repository.save(discipline);
	}

	@PutMapping("/{id}")
	ResponseEntity<Disciplina> update(@PathVariable Long id, @RequestBody Disciplina request) {
		return repository.findById(id)
			.map(discipline -> {
				discipline.setName(request.getName());
				discipline.setActivityType(request.getActivityType());
				discipline.setActive(request.isActive());
				return ResponseEntity.ok(repository.save(discipline));
			})
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		return repository.findById(id)
			.map(discipline -> {
				discipline.setActive(false);
				repository.save(discipline);
				return ResponseEntity.noContent().<Void>build();
			})
			.orElse(ResponseEntity.notFound().build());
	}

}

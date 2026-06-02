package com.backend.clublago.disciplinas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "disciplinas")
public class Disciplina {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre")
	private String nombre;

	@Column(name = "tipo_actividad")
	private String tipoActividad;

	@Column(name = "activo")
	private boolean activo = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return nombre;
	}

	public void setName(String nombre) {
		this.nombre = nombre;
	}

	public String getActivityType() {
		return tipoActividad;
	}

	public void setActivityType(String tipoActividad) {
		this.tipoActividad = tipoActividad;
	}

	public boolean isActive() {
		return activo;
	}

	public void setActive(boolean activo) {
		this.activo = activo;
	}
}

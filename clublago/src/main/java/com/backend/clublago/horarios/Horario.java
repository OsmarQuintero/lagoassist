package com.backend.clublago.horarios;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

import com.backend.clublago.disciplinas.Disciplina;
import com.backend.clublago.maestros.Maestro;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "horarios")
public class Horario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre")
	private String nombre;

	@ManyToOne(optional = false)
	@JoinColumn(name = "disciplina_id")
	private Disciplina disciplina;

	@ManyToOne(optional = false)
	@JoinColumn(name = "maestro_id")
	private Maestro maestro;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "horario_dias", joinColumns = @JoinColumn(name = "horario_id"))
	@Column(name = "dia")
	private Set<DiaSemana> dias = new LinkedHashSet<>();

	@Column(name = "hora_inicio")
	private LocalTime horaInicio;

	@Column(name = "hora_fin")
	private LocalTime horaFin;

	@Column(name = "capacidad")
	private int capacidad;

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

	public Disciplina getDiscipline() {
		return disciplina;
	}

	public void setDiscipline(Disciplina disciplina) {
		this.disciplina = disciplina;
	}

	public Maestro getTeacher() {
		return maestro;
	}

	public void setTeacher(Maestro maestro) {
		this.maestro = maestro;
	}

	public Set<DiaSemana> getDays() {
		return dias;
	}

	public void setDays(Set<DiaSemana> dias) {
		this.dias = dias;
	}

	public LocalTime getStartTime() {
		return horaInicio;
	}

	public void setStartTime(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getEndTime() {
		return horaFin;
	}

	public void setEndTime(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	public int getCapacity() {
		return capacidad;
	}

	public void setCapacity(int capacidad) {
		this.capacidad = capacidad;
	}

	public boolean isActive() {
		return activo;
	}

	public void setActive(boolean activo) {
		this.activo = activo;
	}
}

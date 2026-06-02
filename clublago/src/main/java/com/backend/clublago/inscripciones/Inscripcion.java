package com.backend.clublago.inscripciones;

import java.util.LinkedHashSet;
import java.util.Set;

import com.backend.clublago.horarios.Horario;
import com.backend.clublago.horarios.DiaSemana;
import com.backend.clublago.alumnos.Alumno;

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
@Table(name = "inscripciones")
public class Inscripcion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "alumno_id")
	private Alumno alumno;

	@ManyToOne(optional = false)
	@JoinColumn(name = "horario_id")
	private Horario horario;

	@Column(name = "frecuencia_semanal")
	private int frecuenciaSemanal;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "inscripcion_dias", joinColumns = @JoinColumn(name = "inscripcion_id"))
	@Column(name = "dia")
	private Set<DiaSemana> diasSeleccionados = new LinkedHashSet<>();

	@Column(name = "activo")
	private boolean activo = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Alumno getStudent() {
		return alumno;
	}

	public void setStudent(Alumno alumno) {
		this.alumno = alumno;
	}

	public Horario getSchedule() {
		return horario;
	}

	public void setSchedule(Horario horario) {
		this.horario = horario;
	}

	public int getFrequencyPerWeek() {
		return frecuenciaSemanal;
	}

	public void setFrequencyPerWeek(int frecuenciaSemanal) {
		this.frecuenciaSemanal = frecuenciaSemanal;
	}

	public Set<DiaSemana> getSelectedDays() {
		return diasSeleccionados;
	}

	public void setSelectedDays(Set<DiaSemana> diasSeleccionados) {
		this.diasSeleccionados = diasSeleccionados;
	}

	public boolean isActive() {
		return activo;
	}

	public void setActive(boolean activo) {
		this.activo = activo;
	}
}

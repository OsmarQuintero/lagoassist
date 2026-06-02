package com.backend.clublago.enrollment;

import java.time.DayOfWeek;
import java.util.LinkedHashSet;
import java.util.Set;

import com.backend.clublago.schedule.ClassSchedule;
import com.backend.clublago.student.Student;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Enrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private Student alumno;

	@ManyToOne(optional = false)
	private ClassSchedule schedule;

	private int frecuenciaSemanal;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<DayOfWeek> diasSeleccionados = new LinkedHashSet<>();

	private boolean active = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Student getAlumno() {
		return alumno;
	}

	public void setAlumno(Student alumno) {
		this.alumno = alumno;
	}

	public ClassSchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(ClassSchedule schedule) {
		this.schedule = schedule;
	}

	public int getFrecuenciaSemanal() {
		return frecuenciaSemanal;
	}

	public void setFrecuenciaSemanal(int frecuenciaSemanal) {
		this.frecuenciaSemanal = frecuenciaSemanal			;
	}

	public Set<DayOfWeek> getDiasSeleccionados() {
		return diasSeleccionados;
	}

	public void setDiasSeleccionados(Set<DayOfWeek> diasSeleccionados) {
		this.diasSeleccionados = diasSeleccionados;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}

package com.backend.clublago.asistencias;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.backend.clublago.inscripciones.Inscripcion;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "asistencias", uniqueConstraints = @UniqueConstraint(columnNames = { "inscripcion_id", "fecha_asistencia" }))
public class Asistencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "inscripcion_id")
	private Inscripcion inscripcion;

	@Column(name = "fecha_asistencia")
	private LocalDate fechaAsistencia;

	@Enumerated(EnumType.STRING)
	@Column(name = "estatus")
	private EstatusAsistencia estatus = EstatusAsistencia.PRESENTE;

	@Column(name = "observaciones")
	private String observaciones;

	@Column(name = "guardado_en")
	private LocalDateTime guardadoEn;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Inscripcion getEnrollment() {
		return inscripcion;
	}

	public void setEnrollment(Inscripcion inscripcion) {
		this.inscripcion = inscripcion;
	}

	public LocalDate getAttendanceDate() {
		return fechaAsistencia;
	}

	public void setAttendanceDate(LocalDate fechaAsistencia) {
		this.fechaAsistencia = fechaAsistencia;
	}

	public EstatusAsistencia getStatus() {
		return estatus;
	}

	public void setStatus(EstatusAsistencia estatus) {
		this.estatus = estatus;
	}

	public String getObservations() {
		return observaciones;
	}

	public void setObservations(String observaciones) {
		this.observaciones = observaciones;
	}

	public LocalDateTime getSavedAt() {
		return guardadoEn;
	}

	public void setSavedAt(LocalDateTime guardadoEn) {
		this.guardadoEn = guardadoEn;
	}
}

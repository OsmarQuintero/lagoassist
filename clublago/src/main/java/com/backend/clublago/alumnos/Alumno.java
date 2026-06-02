package com.backend.clublago.alumnos;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "alumnos")
public class Alumno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre")
	private String nombre;

	@Column(name = "numero_accion")
	private String numeroAccion;

	@Column(name = "telefono")
	private String telefono;

	@Column(name = "correo")
	private String correo;

	@Enumerated(EnumType.STRING)
	@Column(name = "estatus")
	private EstatusAlumno estatus = EstatusAlumno.ACTIVO;

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

	public String getActionNumber() {
		return numeroAccion;
	}

	public void setActionNumber(String numeroAccion) {
		this.numeroAccion = numeroAccion;
	}

	public String getPhone() {
		return telefono;
	}

	public void setPhone(String telefono) {
		this.telefono = telefono;
	}

	public String getEmail() {
		return correo;
	}

	public void setEmail(String correo) {
		this.correo = correo;
	}

	public EstatusAlumno getStatus() {
		return estatus;
	}

	public void setStatus(EstatusAlumno estatus) {
		this.estatus = estatus;
	}

	public boolean isActive() {
		return activo;
	}

	public void setActive(boolean activo) {
		this.activo = activo;
	}
}

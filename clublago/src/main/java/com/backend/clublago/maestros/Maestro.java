package com.backend.clublago.maestros;

import java.util.LinkedHashSet;
import java.util.Set;

import com.backend.clublago.disciplinas.Disciplina;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "maestros")
public class Maestro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre")
	private String nombre;

	@Column(name = "telefono")
	private String telefono;

	@Column(name = "correo")
	private String correo;

	@Column(name = "usuario")
	private String usuario;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "contrasena")
	private String contrasena;

	@Column(name = "activo")
	private boolean activo = true;

	@ManyToMany
	@JoinTable(
		name = "maestro_disciplina",
		joinColumns = @JoinColumn(name = "maestro_id"),
		inverseJoinColumns = @JoinColumn(name = "disciplina_id")
	)
	private Set<Disciplina> disciplinas = new LinkedHashSet<>();

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

	public String getUsername() {
		return usuario;
	}

	public void setUsername(String usuario) {
		this.usuario = usuario;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String getPassword() {
		return contrasena;
	}

	public void setPassword(String contrasena) {
		this.contrasena = contrasena;
	}

	public boolean isActive() {
		return activo;
	}

	public void setActive(boolean activo) {
		this.activo = activo;
	}

	public Set<Disciplina> getDisciplines() {
		return disciplinas;
	}

	public void setDisciplines(Set<Disciplina> disciplinas) {
		this.disciplinas = disciplinas;
	}
}

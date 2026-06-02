package com.backend.clublago.configuracion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

import com.backend.clublago.asistencias.Asistencia;
import com.backend.clublago.asistencias.AsistenciaRepository;
import com.backend.clublago.asistencias.EstatusAsistencia;
import com.backend.clublago.disciplinas.Disciplina;
import com.backend.clublago.disciplinas.DisciplinaRepository;
import com.backend.clublago.inscripciones.Inscripcion;
import com.backend.clublago.inscripciones.InscripcionRepository;
import com.backend.clublago.horarios.DiaSemana;
import com.backend.clublago.horarios.Horario;
import com.backend.clublago.horarios.HorarioRepository;
import com.backend.clublago.alumnos.Alumno;
import com.backend.clublago.alumnos.AlumnoRepository;
import com.backend.clublago.alumnos.EstatusAlumno;
import com.backend.clublago.maestros.Maestro;
import com.backend.clublago.maestros.MaestroRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {

	private final DisciplinaRepository disciplinaRepository;
	private final MaestroRepository maestroRepository;
	private final HorarioRepository horarioRepository;
	private final AlumnoRepository alumnoRepository;
	private final InscripcionRepository inscripcionRepository;
	private final AsistenciaRepository asistenciaRepository;
	private final boolean seedEnabled;

	public DatosIniciales(
		DisciplinaRepository disciplinaRepository,
		MaestroRepository maestroRepository,
		HorarioRepository horarioRepository,
		AlumnoRepository alumnoRepository,
		InscripcionRepository inscripcionRepository,
		AsistenciaRepository asistenciaRepository,
		@Value("${app.seed.enabled:true}") boolean seedEnabled
	) {
		this.disciplinaRepository = disciplinaRepository;
		this.maestroRepository = maestroRepository;
		this.horarioRepository = horarioRepository;
		this.alumnoRepository = alumnoRepository;
		this.inscripcionRepository = inscripcionRepository;
		this.asistenciaRepository = asistenciaRepository;
		this.seedEnabled = seedEnabled;
	}

	@Override
	public void run(String... args) {
		if (!seedEnabled) {
			return;
		}
		if (disciplinaRepository.count() > 0) {
			return;
		}

		Disciplina pilates = disciplina("Pilates", "Recreativa");
		Disciplina karate = disciplina("Karate", "Deportiva");
		Disciplina natacion = disciplina("Natacion", "Acuatica");
		disciplinaRepository.saveAll(Lists.of(pilates, karate, natacion));

		Maestro cristina = maestro("Cristina Manzanares", "333-100-1000", "cristina@clubdelago.mx", "cristina", "maestro123", pilates);
		Maestro sensei = maestro("Roberto Alvarez", "333-200-2000", "karate@clubdelago.mx", "roberto", "maestro123", karate);
		maestroRepository.saveAll(Lists.of(cristina, sensei));

		Horario pilatesMatutino = horario(
			"Pilates Matutino",
			pilates,
			cristina,
			Set.of(DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.VIERNES),
			LocalTime.of(8, 0),
			LocalTime.of(9, 0),
			12
		);
		Horario karateTarde = horario(
			"Karate Infantil",
			karate,
			sensei,
			Set.of(DiaSemana.MARTES, DiaSemana.JUEVES),
			LocalTime.of(17, 0),
			LocalTime.of(18, 0),
			18
		);
		horarioRepository.saveAll(Lists.of(pilatesMatutino, karateTarde));

		Alumno maria = alumno("Maria Lopez", "A-1001", "333-300-3000", "maria@example.com", EstatusAlumno.ACTIVO);
		Alumno ana = alumno("Ana Torres", "A-1002", "333-400-4000", "ana@example.com", EstatusAlumno.ACTIVO);
		Alumno sofia = alumno("Sofia Ruiz", "A-1003", "333-500-5000", "sofia@example.com", EstatusAlumno.ADEUDO);
		alumnoRepository.saveAll(Lists.of(maria, ana, sofia));

		Inscripcion mariaPilates = inscripcion(maria, pilatesMatutino, 2, Set.of(DiaSemana.LUNES, DiaSemana.MIERCOLES));
		Inscripcion anaPilates = inscripcion(ana, pilatesMatutino, 3, Set.of(DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
		Inscripcion sofiaPilates = inscripcion(sofia, pilatesMatutino, 1, Set.of(DiaSemana.VIERNES));
		inscripcionRepository.saveAll(Lists.of(mariaPilates, anaPilates, sofiaPilates));

		LocalDate hoy = LocalDate.now();
		asistenciaRepository.save(asistencia(mariaPilates, hoy, EstatusAsistencia.PRESENTE, ""));
		asistenciaRepository.save(asistencia(anaPilates, hoy, EstatusAsistencia.PRESENTE, ""));
		asistenciaRepository.save(asistencia(sofiaPilates, hoy, EstatusAsistencia.FALTA, "No aviso"));
	}

	private Disciplina disciplina(String nombre, String tipoActividad) {
		Disciplina disciplina = new Disciplina();
		disciplina.setName(nombre);
		disciplina.setActivityType(tipoActividad);
		return disciplina;
	}

	private Maestro maestro(String nombre, String telefono, String correo, String usuario, String contrasena, Disciplina disciplina) {
		Maestro maestro = new Maestro();
		maestro.setName(nombre);
		maestro.setPhone(telefono);
		maestro.setEmail(correo);
		maestro.setUsername(usuario);
		maestro.setPassword(contrasena);
		maestro.setDisciplines(new LinkedHashSet<>(Set.of(disciplina)));
		return maestro;
	}

	private Horario horario(
		String nombre,
		Disciplina disciplina,
		Maestro maestro,
		Set<DiaSemana> dias,
		LocalTime horaInicio,
		LocalTime horaFin,
		int capacidad
	) {
		Horario horario = new Horario();
		horario.setName(nombre);
		horario.setDiscipline(disciplina);
		horario.setTeacher(maestro);
		horario.setDays(new LinkedHashSet<>(dias));
		horario.setStartTime(horaInicio);
		horario.setEndTime(horaFin);
		horario.setCapacity(capacidad);
		return horario;
	}

	private Alumno alumno(String nombre, String numeroAccion, String telefono, String correo, EstatusAlumno estatus) {
		Alumno alumno = new Alumno();
		alumno.setName(nombre);
		alumno.setActionNumber(numeroAccion);
		alumno.setPhone(telefono);
		alumno.setEmail(correo);
		alumno.setStatus(estatus);
		return alumno;
	}

	private Inscripcion inscripcion(Alumno alumno, Horario horario, int frecuencia, Set<DiaSemana> dias) {
		Inscripcion inscripcion = new Inscripcion();
		inscripcion.setStudent(alumno);
		inscripcion.setSchedule(horario);
		inscripcion.setFrequencyPerWeek(frecuencia);
		inscripcion.setSelectedDays(new LinkedHashSet<>(dias));
		return inscripcion;
	}

	private Asistencia asistencia(Inscripcion inscripcion, LocalDate fecha, EstatusAsistencia estatus, String observaciones) {
		Asistencia asistencia = new Asistencia();
		asistencia.setEnrollment(inscripcion);
		asistencia.setAttendanceDate(fecha);
		asistencia.setStatus(estatus);
		asistencia.setObservations(observaciones);
		return asistencia;
	}

	private static final class Lists {
		@SafeVarargs
		private static <T> java.util.List<T> of(T... items) {
			return java.util.Arrays.asList(items);
		}
	}
}

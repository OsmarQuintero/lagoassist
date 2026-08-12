package com.backend.clublago.asistencias;

import java.time.Instant;
import java.util.Set;

import com.backend.clublago.alumnos.EstatusAlumno;
import com.backend.clublago.horarios.DiaSemana;

public record FilaPaseLista(
	Long enrollmentId,
	Long attendanceId,
	Long studentId,
	String studentName,
	EstatusAlumno studentStatus,
	int frequencyPerWeek,
	Set<DiaSemana> selectedDays,
	EstatusAsistencia status,
	String observations,
	Instant savedAt,
	Instant editableUntil,
	boolean locked
) {
}

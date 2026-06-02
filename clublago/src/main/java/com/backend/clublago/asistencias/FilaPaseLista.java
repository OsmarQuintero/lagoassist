package com.backend.clublago.asistencias;

import java.time.LocalDateTime;
import java.util.Set;

import com.backend.clublago.horarios.DiaSemana;

public record FilaPaseLista(
	Long enrollmentId,
	Long attendanceId,
	Long studentId,
	String studentName,
	int frequencyPerWeek,
	Set<DiaSemana> selectedDays,
	EstatusAsistencia status,
	String observations,
	LocalDateTime savedAt,
	LocalDateTime editableUntil,
	boolean locked
) {
}

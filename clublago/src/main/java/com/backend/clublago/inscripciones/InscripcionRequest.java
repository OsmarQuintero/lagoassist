package com.backend.clublago.inscripciones;

import java.util.Set;

import com.backend.clublago.horarios.DiaSemana;

public record InscripcionRequest(
	Long studentId,
	Long scheduleId,
	int frequencyPerWeek,
	Set<DiaSemana> selectedDays,
	boolean active
) {
}

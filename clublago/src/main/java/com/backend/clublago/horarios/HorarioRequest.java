package com.backend.clublago.horarios;

import java.time.LocalTime;
import java.util.Set;

public record HorarioRequest(
	String name,
	Long disciplineId,
	Long teacherId,
	Set<DiaSemana> days,
	LocalTime startTime,
	LocalTime endTime,
	int capacity,
	boolean active
) {
}

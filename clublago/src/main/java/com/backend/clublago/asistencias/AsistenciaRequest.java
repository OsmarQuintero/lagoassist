package com.backend.clublago.asistencias;

import java.time.LocalDate;

public record AsistenciaRequest(
	Long enrollmentId,
	LocalDate attendanceDate,
	EstatusAsistencia status,
	String observations
) {
}

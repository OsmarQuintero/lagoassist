package com.backend.clublago.asistencias;

import java.time.LocalDate;
import java.util.List;

public record PaseListaRequest(
	Long scheduleId,
	LocalDate attendanceDate,
	List<AsistenciaRequest> records
) {
}

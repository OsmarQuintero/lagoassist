package com.backend.clublago.reportes;

public record FilaReporteAsistencia(
	String label,
	long total,
	long present,
	long late,
	long absent,
	long justified,
	double attendancePercentage
) {
}

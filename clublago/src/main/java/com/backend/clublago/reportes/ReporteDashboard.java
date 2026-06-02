package com.backend.clublago.reportes;

public record ReporteDashboard(
	long activeDisciplines,
	long activeTeachers,
	long activeSchedules,
	long activeStudents,
	long activeEnrollments,
	long totalAttendanceRecords,
	long presentRecords,
	long lateRecords,
	long absentRecords,
	long justifiedRecords,
	double attendancePercentage
) {
}

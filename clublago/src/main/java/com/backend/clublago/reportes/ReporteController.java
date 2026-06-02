package com.backend.clublago.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.backend.clublago.asistencias.Asistencia;
import com.backend.clublago.asistencias.AsistenciaRepository;
import com.backend.clublago.asistencias.EstatusAsistencia;
import com.backend.clublago.disciplinas.DisciplinaRepository;
import com.backend.clublago.inscripciones.InscripcionRepository;
import com.backend.clublago.horarios.HorarioRepository;
import com.backend.clublago.alumnos.Alumno;
import com.backend.clublago.alumnos.AlumnoRepository;
import com.backend.clublago.maestros.MaestroRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReporteController {

	private final DisciplinaRepository disciplineRepository;
	private final MaestroRepository teacherRepository;
	private final HorarioRepository scheduleRepository;
	private final AlumnoRepository studentRepository;
	private final InscripcionRepository enrollmentRepository;
	private final AsistenciaRepository attendanceRepository;

	public ReporteController(
		DisciplinaRepository disciplineRepository,
		MaestroRepository teacherRepository,
		HorarioRepository scheduleRepository,
		AlumnoRepository studentRepository,
		InscripcionRepository enrollmentRepository,
		AsistenciaRepository attendanceRepository
	) {
		this.disciplineRepository = disciplineRepository;
		this.teacherRepository = teacherRepository;
		this.scheduleRepository = scheduleRepository;
		this.studentRepository = studentRepository;
		this.enrollmentRepository = enrollmentRepository;
		this.attendanceRepository = attendanceRepository;
	}

	@GetMapping("/dashboard")
	ReporteDashboard dashboard() {
		long present = attendanceRepository.countByEstatus(EstatusAsistencia.PRESENTE);
		long late = attendanceRepository.countByEstatus(EstatusAsistencia.RETARDO);
		long absent = attendanceRepository.countByEstatus(EstatusAsistencia.FALTA);
		long justified = attendanceRepository.countByEstatus(EstatusAsistencia.JUSTIFICADO);
		long total = present + late + absent + justified;
		double attendancePercentage = total == 0 ? 0 : ((present + late) * 100.0) / total;
		return new ReporteDashboard(
			disciplineRepository.findByActivoTrueOrderByNombre().size(),
			teacherRepository.findByActivoTrueOrderByNombre().size(),
			scheduleRepository.findByActivoTrueOrderByNombre().size(),
			studentRepository.countByActivoTrue(),
			enrollmentRepository.countByActivoTrue(),
			total,
			present,
			late,
			absent,
			justified,
			Math.round(attendancePercentage * 10.0) / 10.0
		);
	}

	@GetMapping("/attendance")
	List<FilaReporteAsistencia> attendance(@RequestParam(defaultValue = "discipline") String groupBy) {
		return attendanceRows(groupBy);
	}

	@GetMapping("/attendance/export")
	ResponseEntity<byte[]> exportAttendance(
		@RequestParam(defaultValue = "discipline") String groupBy,
		@RequestParam(defaultValue = "xlsx") String format
	) throws IOException, DocumentException {
		List<FilaReporteAsistencia> rows = attendanceRows(groupBy);
		boolean pdf = "pdf".equalsIgnoreCase(format);
		byte[] file = pdf ? toPdf(groupBy, rows) : toExcel(groupBy, rows);
		String extension = pdf ? "pdf" : "xlsx";
		String contentType = pdf
			? MediaType.APPLICATION_PDF_VALUE
			: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-asistencias-" + groupBy + "." + extension)
			.contentType(MediaType.parseMediaType(contentType))
			.body(file);
	}

	private List<FilaReporteAsistencia> attendanceRows(String groupBy) {
		Function<Asistencia, String> classifier = switch (groupBy) {
			case "teacher" -> attendance -> attendance.getEnrollment().getSchedule().getTeacher().getName();
			case "schedule" -> attendance -> attendance.getEnrollment().getSchedule().getName();
			case "student" -> attendance -> studentLabel(attendance.getEnrollment().getStudent());
			default -> attendance -> attendance.getEnrollment().getSchedule().getDiscipline().getName();
		};
		Map<String, List<Asistencia>> grouped = attendanceRepository.findAll()
			.stream()
			.collect(Collectors.groupingBy(classifier));
		return grouped.entrySet()
			.stream()
			.map(entry -> toRow(entry.getKey(), entry.getValue()))
			.sorted((left, right) -> left.label().compareToIgnoreCase(right.label()))
			.toList();
	}

	private byte[] toExcel(String groupBy, List<FilaReporteAsistencia> rows) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Reporte");
			Row title = sheet.createRow(0);
			title.createCell(0).setCellValue("Reporte de asistencias por " + groupLabel(groupBy));

			Row header = sheet.createRow(2);
			String[] headers = { "Grupo", "Total", "Presente", "Retardo", "Falta", "Justificada", "Porcentaje" };
			for (int index = 0; index < headers.length; index++) {
				header.createCell(index).setCellValue(headers[index]);
			}

			for (int index = 0; index < rows.size(); index++) {
				FilaReporteAsistencia report = rows.get(index);
				Row row = sheet.createRow(index + 3);
				row.createCell(0).setCellValue(report.label());
				row.createCell(1).setCellValue(report.total());
				row.createCell(2).setCellValue(report.present());
				row.createCell(3).setCellValue(report.late());
				row.createCell(4).setCellValue(report.absent());
				row.createCell(5).setCellValue(report.justified());
				row.createCell(6).setCellValue(report.attendancePercentage());
			}

			for (int index = 0; index < headers.length; index++) {
				sheet.autoSizeColumn(index);
			}
			workbook.write(output);
			return output.toByteArray();
		}
	}

	private byte[] toPdf(String groupBy, List<FilaReporteAsistencia> rows) throws DocumentException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document document = new Document();
		PdfWriter.getInstance(document, output);
		document.open();
		document.add(new Paragraph("Reporte de asistencias por " + groupLabel(groupBy)));
		document.add(new Paragraph(" "));

		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(100);
		String[] headers = { "Grupo", "Total", "Presente", "Retardo", "Falta", "Just.", "%" };
		for (String header : headers) {
			table.addCell(header);
		}
		for (FilaReporteAsistencia row : rows) {
			table.addCell(row.label());
			table.addCell(String.valueOf(row.total()));
			table.addCell(String.valueOf(row.present()));
			table.addCell(String.valueOf(row.late()));
			table.addCell(String.valueOf(row.absent()));
			table.addCell(String.valueOf(row.justified()));
			table.addCell(row.attendancePercentage() + "%");
		}
		document.add(table);
		document.close();
		return output.toByteArray();
	}

	private FilaReporteAsistencia toRow(String etiqueta, List<Asistencia> registros) {
		long presentes = count(registros, EstatusAsistencia.PRESENTE);
		long retardos = count(registros, EstatusAsistencia.RETARDO);
		long faltas = count(registros, EstatusAsistencia.FALTA);
		long justificados = count(registros, EstatusAsistencia.JUSTIFICADO);
		long total = registros.size();
		double porcentaje = total == 0 ? 0 : ((presentes + retardos) * 100.0) / total;
		return new FilaReporteAsistencia(etiqueta, total, presentes, retardos, faltas, justificados, Math.round(porcentaje * 10.0) / 10.0);
	}

	private long count(List<Asistencia> registros, EstatusAsistencia estatus) {
		return registros.stream().filter(asistencia -> asistencia.getStatus() == estatus).count();
	}

	private String studentLabel(Alumno alumno) {
		return isBlank(alumno.getActionNumber()) ? alumno.getName() : alumno.getName() + " (" + alumno.getActionNumber() + ")";
	}

	private String groupLabel(String groupBy) {
		return switch (groupBy) {
			case "teacher" -> "maestro";
			case "schedule" -> "horario";
			case "student" -> "alumno";
			default -> "disciplina";
		};
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit, PLATFORM_ID, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { environment } from '../environments/environment';

type AttendanceStatus = 'PRESENTE' | 'FALTA' | 'RETARDO' | 'JUSTIFICADO';
type StudentStatus = 'ACTIVO' | 'BAJA' | 'SUSPENDIDO' | 'ADEUDO';
type UserRole = 'admin' | 'teacher';

interface Discipline {
  id: number;
  name: string;
  activityType: string;
  active: boolean;
}

interface Teacher {
  id: number;
  name: string;
  phone: string;
  email: string;
  username: string;
  active: boolean;
  disciplines: Discipline[];
}

interface ClassSchedule {
  id: number;
  name: string;
  discipline: Discipline;
  teacher: Teacher;
  days: string[];
  startTime: string;
  endTime: string;
  capacity: number;
  active: boolean;
}

interface Student {
  id: number;
  name: string;
  actionNumber: string;
  phone: string;
  email: string;
  status: StudentStatus;
  active: boolean;
}

interface Enrollment {
  id: number;
  student: Student;
  schedule: ClassSchedule;
  frequencyPerWeek: number;
  selectedDays: string[];
  active: boolean;
}

interface RollCallRow {
  enrollmentId: number;
  attendanceId: number | null;
  studentId: number;
  studentName: string;
  frequencyPerWeek: number;
  selectedDays: string[];
  status: AttendanceStatus;
  observations: string;
}

interface DashboardReport {
  activeDisciplines: number;
  activeTeachers: number;
  activeSchedules: number;
  activeStudents: number;
  activeEnrollments: number;
  totalAttendanceRecords: number;
  presentRecords: number;
  lateRecords: number;
  absentRecords: number;
  justifiedRecords: number;
  attendancePercentage: number;
}

interface AttendanceReportRow {
  label: string;
  total: number;
  present: number;
  late: number;
  absent: number;
  justified: number;
  attendancePercentage: number;
}

const today = new Date().toISOString().slice(0, 10);

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly api = environment.apiUrl;
  private readonly sessionKey = 'lagoassist.session';

  readonly days = [
    { value: 'LUNES', label: 'Lunes' },
    { value: 'MARTES', label: 'Martes' },
    { value: 'MIERCOLES', label: 'Miércoles' },
    { value: 'JUEVES', label: 'Jueves' },
    { value: 'VIERNES', label: 'Viernes' },
    { value: 'SABADO', label: 'Sábado' },
    { value: 'DOMINGO', label: 'Domingo' },
  ];
  readonly statuses: AttendanceStatus[] = ['PRESENTE', 'FALTA', 'RETARDO', 'JUSTIFICADO'];
  readonly studentStatuses: StudentStatus[] = ['ACTIVO', 'BAJA', 'SUSPENDIDO', 'ADEUDO'];

  tab = signal('asistencias');
  message = signal('');
  isAuthenticated = signal(false);
  currentRole = signal<UserRole>('admin');
  loginError = signal('');
  disciplines = signal<Discipline[]>([]);
  teachers = signal<Teacher[]>([]);
  schedules = signal<ClassSchedule[]>([]);
  students = signal<Student[]>([]);
  enrollments = signal<Enrollment[]>([]);
  rollCall = signal<RollCallRow[]>([]);
  dashboard = signal<DashboardReport | null>(null);
  reportRows = signal<AttendanceReportRow[]>([]);
  selectedTeacherId = signal(0);

  disciplineForm = { name: '', activityType: 'Deportiva', active: true };
  teacherForm = {
    name: '',
    phone: '',
    email: '',
    username: '',
    password: '',
    disciplineIds: [] as number[],
    active: true,
  };
  scheduleForm = {
    name: '',
    disciplineId: 0,
    teacherId: 0,
    days: [] as string[],
    startTime: '08:00',
    endTime: '09:00',
    capacity: 10,
    active: true,
  };
  studentForm = { name: '', actionNumber: '', phone: '', email: '', status: 'ACTIVO' as StudentStatus, active: true };
  enrollmentForm = { studentId: 0, scheduleId: 0, frequencyPerWeek: 1, selectedDays: [] as string[], active: true };
  attendanceFilters = { scheduleId: 0, date: today };
  reportGroup = 'discipline';
  loginForm = { username: '', password: '' };

  capacityText = computed(() => {
    const schedule = this.schedules().find((item) => item.id === Number(this.attendanceFilters.scheduleId));
    if (!schedule) return '';
    const used = this.enrollments().filter((item) => item.schedule.id === schedule.id).length;
    return `${used}/${schedule.capacity}`;
  });
  teacherSchedules = computed(() =>
    this.schedules().filter((schedule) => schedule.teacher.id === Number(this.selectedTeacherId())),
  );
  teacherEnrollments = computed(() => {
    const scheduleIds = new Set(this.teacherSchedules().map((schedule) => schedule.id));
    return this.enrollments().filter((enrollment) => scheduleIds.has(enrollment.schedule.id));
  });
  teacherStudentsCount = computed(() => new Set(this.teacherEnrollments().map((enrollment) => enrollment.student.id)).size);
  teacherCapacity = computed(() => this.teacherSchedules().reduce((total, schedule) => total + schedule.capacity, 0));
  teacherOccupancyPercentage = computed(() => {
    const capacity = this.teacherCapacity();
    return capacity === 0 ? 0 : Math.round((this.teacherEnrollments().length * 1000) / capacity) / 10;
  });
  selectedTeacher = computed(() =>
    this.teachers().find((teacher) => teacher.id === Number(this.selectedTeacherId())),
  );
  attendanceSchedules = computed(() => (this.currentRole() === 'teacher' ? this.teacherSchedules() : this.schedules()));

  constructor(private readonly http: HttpClient, @Inject(PLATFORM_ID) private readonly platformId: object) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      const session = this.readSession();
      this.isAuthenticated.set(Boolean(session));
      this.currentRole.set(session?.role ?? 'admin');
      this.selectedTeacherId.set(session?.teacherId ?? 0);
      this.tab.set(this.currentRole() === 'admin' ? 'dashboard-admin' : 'dashboard-teacher');
      if (this.isAuthenticated()) {
        this.loadAll();
      }
    }
  }

  login() {
    const username = this.loginForm.username.trim().toLowerCase();
    if (username === 'admin' && this.loginForm.password === 'admin123') {
      this.loginError.set('');
      this.isAuthenticated.set(true);
      this.currentRole.set('admin');
      this.selectedTeacherId.set(0);
      this.tab.set('dashboard-admin');
      localStorage.setItem(this.sessionKey, JSON.stringify({ role: 'admin' }));
      this.loadAll();
      return;
    }

    this.http.post<Teacher>(`${this.api}/teachers/login`, this.loginForm).subscribe({
      next: (teacher) => {
        this.loginError.set('');
        this.isAuthenticated.set(true);
        this.currentRole.set('teacher');
        this.selectedTeacherId.set(teacher.id);
        this.tab.set('dashboard-teacher');
        localStorage.setItem(this.sessionKey, JSON.stringify({ role: 'teacher', teacherId: teacher.id }));
        this.loadAll();
      },
      error: () => this.loginError.set('Usuario o contraseña incorrectos'),
    });
  }

  logout() {
    localStorage.removeItem(this.sessionKey);
    this.isAuthenticated.set(false);
    this.loginForm = { username: '', password: '' };
    this.currentRole.set('admin');
    this.selectedTeacherId.set(0);
    this.tab.set('dashboard-admin');
  }

  loadAll() {
    this.loadDisciplines();
    this.loadTeachers();
    this.loadSchedules();
    this.loadStudents();
    this.loadEnrollments();
    this.loadDashboard();
    this.loadAttendanceReport();
  }

  loadDisciplines() {
    this.http.get<Discipline[]>(`${this.api}/disciplines`).subscribe((data) => this.disciplines.set(data));
  }

  loadTeachers() {
    this.http.get<Teacher[]>(`${this.api}/teachers`).subscribe((data) => {
      this.teachers.set(data);
      if (!this.selectedTeacherId() && data.length) {
        this.selectedTeacherId.set(data[0].id);
      }
    });
  }

  loadSchedules() {
    this.http.get<ClassSchedule[]>(`${this.api}/schedules`).subscribe((data) => {
      this.schedules.set(data);
      const availableSchedules = this.attendanceSchedules();
      const selectedScheduleExists = availableSchedules.some((schedule) => schedule.id === Number(this.attendanceFilters.scheduleId));
      if (!selectedScheduleExists && availableSchedules.length) {
        this.attendanceFilters.scheduleId = availableSchedules[0].id;
      }
      if (!this.scheduleForm.disciplineId && data.length) this.scheduleForm.disciplineId = data[0].discipline.id;
      if (!this.scheduleForm.teacherId && data.length) this.scheduleForm.teacherId = data[0].teacher.id;
      if (!this.enrollmentForm.scheduleId && data.length) this.enrollmentForm.scheduleId = data[0].id;
      this.loadRollCall();
    });
  }

  loadStudents() {
    this.http.get<Student[]>(`${this.api}/students`).subscribe((data) => {
      this.students.set(data);
      if (!this.enrollmentForm.studentId && data.length) this.enrollmentForm.studentId = data[0].id;
    });
  }

  loadEnrollments() {
    this.http.get<Enrollment[]>(`${this.api}/enrollments`).subscribe((data) => this.enrollments.set(data));
  }

  loadRollCall() {
    if (!this.attendanceFilters.scheduleId) return;
    this.http
      .get<RollCallRow[]>(`${this.api}/attendances/roll-call`, {
        params: { scheduleId: this.attendanceFilters.scheduleId, date: this.attendanceFilters.date },
      })
      .subscribe((data) => this.rollCall.set(data));
  }

  loadDashboard() {
    this.http.get<DashboardReport>(`${this.api}/reports/dashboard`).subscribe((data) => this.dashboard.set(data));
  }

  loadAttendanceReport() {
    this.http
      .get<AttendanceReportRow[]>(`${this.api}/reports/attendance`, { params: { groupBy: this.reportGroup } })
      .subscribe((data) => this.reportRows.set(data));
  }

  saveDiscipline() {
    this.http.post<Discipline>(`${this.api}/disciplines`, this.disciplineForm).subscribe(() => {
      this.disciplineForm = { name: '', activityType: 'Deportiva', active: true };
      this.done('Disciplina guardada');
    });
  }

  saveTeacher() {
    this.http.post<Teacher>(`${this.api}/teachers`, this.teacherForm).subscribe(() => {
      this.teacherForm = {
        name: '',
        phone: '',
        email: '',
        username: '',
        password: '',
        disciplineIds: [],
        active: true,
      };
      this.done('Maestro guardado');
    });
  }

  saveSchedule() {
    this.http.post<ClassSchedule>(`${this.api}/schedules`, this.scheduleForm).subscribe(() => {
      this.scheduleForm = { ...this.scheduleForm, name: '', capacity: 10, days: [] };
      this.done('Horario guardado');
    });
  }

  saveStudent() {
    this.http.post<Student>(`${this.api}/students`, this.studentForm).subscribe(() => {
      this.studentForm = { name: '', actionNumber: '', phone: '', email: '', status: 'ACTIVO', active: true };
      this.done('Alumno guardado');
    });
  }

  saveEnrollment() {
    this.http.post<Enrollment>(`${this.api}/enrollments`, this.enrollmentForm).subscribe({
      next: () => {
        this.enrollmentForm = { ...this.enrollmentForm, frequencyPerWeek: 1, selectedDays: [] };
        this.done('Inscripcion guardada');
      },
      error: () => this.message.set('No se pudo inscribir: revisa cupo y datos.'),
    });
  }

  saveAttendance(row: RollCallRow, status: AttendanceStatus) {
    const body = {
      enrollmentId: row.enrollmentId,
      attendanceDate: this.attendanceFilters.date,
      status,
      observations: row.observations,
    };
    this.http.post(`${this.api}/attendances`, body).subscribe(() => {
      row.status = status;
      this.loadDashboard();
      this.loadAttendanceReport();
    });
  }

  deactivate(path: string, id: number) {
    this.http.delete(`${this.api}/${path}/${id}`).subscribe(() => this.done('Registro desactivado'));
  }

  toggleSelection<T>(list: T[], value: T) {
    const index = list.indexOf(value);
    index >= 0 ? list.splice(index, 1) : list.push(value);
  }

  isSelected<T>(list: T[], value: T) {
    return list.includes(value);
  }

  formatDays(days: string[]) {
    return days.map((day) => this.dayLabel(day)).join(', ');
  }

  dayLabel(value: string) {
    return this.days.find((day) => day.value === value)?.label ?? value;
  }

  enrollmentCountForSchedule(scheduleId: number) {
    return this.enrollments().filter((enrollment) => enrollment.schedule.id === scheduleId).length;
  }

  downloadAttendanceReport(format: 'xlsx' | 'pdf') {
    this.http
      .get(`${this.api}/reports/attendance/export`, {
        params: { groupBy: this.reportGroup, format },
        responseType: 'blob',
      })
      .subscribe((file) => {
        const filename = `reporte-asistencias-${this.reportGroup}.${format}`;
        this.downloadBlob(file, filename);
      });
  }

  setTab(tab: string) {
    this.tab.set(tab);
    if (tab === 'reportes') {
      this.loadAttendanceReport();
    }
  }

  private readSession(): { role: UserRole; teacherId?: number } | null {
    const raw = localStorage.getItem(this.sessionKey);
    if (!raw) return null;
    try {
      const session = JSON.parse(raw) as { role?: UserRole; teacherId?: number };
      return session.role === 'teacher' || session.role === 'admin'
        ? { role: session.role, teacherId: session.teacherId }
        : null;
    } catch {
      return raw === 'active' ? { role: 'admin' } : null;
    }
  }

  private done(text: string) {
    this.message.set(text);
    this.loadAll();
  }

  private downloadBlob(file: Blob, filename: string) {
    if (!isPlatformBrowser(this.platformId)) return;
    const url = URL.createObjectURL(file);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }
}

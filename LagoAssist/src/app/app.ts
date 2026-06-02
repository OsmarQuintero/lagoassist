import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit, PLATFORM_ID, ViewEncapsulation, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MenuLateral } from './components/menu-lateral/menu-lateral';
import { PanelAdmin } from './components/panel-admin/panel-admin';
import { PanelAlumnos } from './components/panel-alumnos/panel-alumnos';
import { PanelCatalogos } from './components/panel-catalogos/panel-catalogos';
import { PanelInscripciones } from './components/panel-inscripciones/panel-inscripciones';
import { PanelLogin } from './components/panel-login/panel-login';
import { PanelMaestro } from './components/panel-maestro/panel-maestro';
import { PanelReportes } from './components/panel-reportes/panel-reportes';
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
  savedAt: string | null;
  editableUntil: string | null;
  locked: boolean;
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

interface TeacherScheduleGroup {
  schedule: ClassSchedule;
  enrollments: Enrollment[];
  studentCount: number;
  occupancyPercentage: number;
}

interface TeacherDisciplineGroup {
  discipline: Discipline;
  schedules: TeacherScheduleGroup[];
  studentCount: number;
  enrollmentCount: number;
  capacity: number;
}

function localDateInput(date = new Date()) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

@Component({
  selector: 'app-root',
  imports: [
    CommonModule,
    FormsModule,
    MenuLateral,
    PanelAdmin,
    PanelAlumnos,
    PanelCatalogos,
    PanelInscripciones,
    PanelLogin,
    PanelMaestro,
    PanelReportes,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css',
  encapsulation: ViewEncapsulation.None,
})
export class App implements OnInit, OnDestroy {
  private readonly api = environment.apiUrl;
  private readonly sessionKey = 'lagoassist.session';
  private clockTimer: number | null = null;
  readonly state = this;

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
  readonly rollCallStatuses: AttendanceStatus[] = ['PRESENTE', 'FALTA'];
  readonly studentStatuses: StudentStatus[] = ['ACTIVO', 'BAJA', 'SUSPENDIDO', 'ADEUDO'];

  tab = signal('dashboard-admin');
  message = signal('');
  isAuthenticated = signal(false);
  currentRole = signal<UserRole>('admin');
  sidebarOpen = signal(false);
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
  selectedTeacherScheduleId = signal<number | null>(null);
  currentDateTime = signal(new Date());
  editingDisciplineId = signal<number | null>(null);
  editingTeacherId = signal<number | null>(null);
  editingStudentId = signal<number | null>(null);
  editingScheduleId = signal<number | null>(null);
  editingEnrollmentId = signal<number | null>(null);

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
  attendanceFilters = { scheduleId: 0, date: localDateInput() };
  reportGroup = 'discipline';
  reportFilters = { from: '', to: '' };
  loginForm = { username: '', password: '' };
  studentSearch = signal('');
  studentStatusFilter = signal('TODOS');
  studentActiveFilter = signal('TODOS');

  capacityText = computed(() => {
    const schedule = this.schedules().find((item) => item.id === Number(this.attendanceFilters.scheduleId));
    if (!schedule) return '';
    const used = this.enrollments().filter((item) => item.active && item.schedule.id === schedule.id).length;
    return `${used}/${schedule.capacity}`;
  });
  teacherSchedules = computed(() =>
    this.schedules().filter((schedule) => schedule.teacher.id === Number(this.selectedTeacherId())),
  );
  teacherEnrollments = computed(() => {
    const scheduleIds = new Set(this.teacherSchedules().map((schedule) => schedule.id));
    return this.enrollments().filter((enrollment) => enrollment.active && scheduleIds.has(enrollment.schedule.id));
  });
  teacherStudentsCount = computed(() => new Set(this.teacherEnrollments().map((enrollment) => enrollment.student.id)).size);
  teacherCapacity = computed(() => this.teacherSchedules().reduce((total, schedule) => total + schedule.capacity, 0));
  teacherOccupancyPercentage = computed(() => {
    const capacity = this.teacherCapacity();
    return capacity === 0 ? 0 : Math.round((this.teacherEnrollments().length * 1000) / capacity) / 10;
  });
  teacherDisciplineGroups = computed(() => {
    const teacher = this.selectedTeacher();
    if (!teacher) return [];

    const schedules = this.teacherSchedules();
    const disciplines = new Map<number, Discipline>();
    teacher.disciplines.forEach((discipline) => disciplines.set(discipline.id, discipline));
    schedules.forEach((schedule) => disciplines.set(schedule.discipline.id, schedule.discipline));

    return Array.from(disciplines.values()).map((discipline) => {
      const disciplineSchedules = schedules.filter((schedule) => schedule.discipline.id === discipline.id);
      const scheduleGroups = disciplineSchedules.map((schedule) => {
        const enrollments = this.teacherEnrollments().filter((enrollment) => enrollment.schedule.id === schedule.id);
        const occupancyPercentage = schedule.capacity === 0 ? 0 : Math.round((enrollments.length * 1000) / schedule.capacity) / 10;
        return {
          schedule,
          enrollments,
          studentCount: new Set(enrollments.map((enrollment) => enrollment.student.id)).size,
          occupancyPercentage,
        };
      });
      const allEnrollments = scheduleGroups.flatMap((group) => group.enrollments);
      return {
        discipline,
        schedules: scheduleGroups,
        studentCount: new Set(allEnrollments.map((enrollment) => enrollment.student.id)).size,
        enrollmentCount: allEnrollments.length,
        capacity: disciplineSchedules.reduce((total, schedule) => total + schedule.capacity, 0),
      };
    });
  });
  selectedTeacher = computed(() =>
    this.teachers().find((teacher) => teacher.id === Number(this.selectedTeacherId())),
  );
  attendanceSchedules = computed(() => (this.currentRole() === 'teacher' ? this.teacherSchedules() : this.schedules()));
  selectedTeacherSchedule = computed(() =>
    this.teacherSchedules().find((schedule) => schedule.id === this.selectedTeacherScheduleId()) ?? null,
  );
  filteredStudents = computed(() => {
    const query = this.normalize(this.studentSearch());
    return this.students().filter((student) => {
      const matchesQuery =
        !query ||
        this.normalize(student.name).includes(query) ||
        this.normalize(student.actionNumber).includes(query) ||
        this.normalize(student.phone).includes(query) ||
        this.normalize(student.email).includes(query);
      const statusFilter = this.studentStatusFilter();
      const activeFilter = this.studentActiveFilter();
      const matchesStatus = statusFilter === 'TODOS' || student.status === statusFilter;
      const matchesActivity =
        activeFilter === 'TODOS' ||
        (activeFilter === 'ACTIVOS' && student.active) ||
        (activeFilter === 'BAJAS' && !student.active);
      return matchesQuery && matchesStatus && matchesActivity;
    });
  });
  currentDateText = computed(() =>
    this.currentDateTime().toLocaleDateString('es-MX', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    }),
  );
  currentTimeText = computed(() =>
    this.currentDateTime().toLocaleTimeString('es-MX', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }),
  );

  constructor(private readonly http: HttpClient, @Inject(PLATFORM_ID) private readonly platformId: object) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.startClock();
      this.syncAttendanceDateToToday(false);
      const session = this.readSession();
      this.isAuthenticated.set(Boolean(session));
      this.currentRole.set(session?.role ?? 'admin');
      this.selectedTeacherId.set(session?.teacherId ?? 0);
      this.selectedTeacherScheduleId.set(null);
      this.tab.set(this.currentRole() === 'admin' ? 'dashboard-admin' : 'dashboard-teacher');
      if (this.isAuthenticated()) {
        this.loadAll();
      }
    }
  }

  ngOnDestroy() {
    if (this.clockTimer !== null) {
      window.clearInterval(this.clockTimer);
    }
  }

  login() {
    const username = this.loginForm.username.trim().toLowerCase();
    if (username === 'admin' && this.loginForm.password === 'admin123') {
      this.loginError.set('');
      this.isAuthenticated.set(true);
      this.currentRole.set('admin');
      this.selectedTeacherId.set(0);
      this.selectedTeacherScheduleId.set(null);
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
        this.selectedTeacherScheduleId.set(null);
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
    this.selectedTeacherScheduleId.set(null);
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
    this.http.get<Discipline[]>(`${this.api}/disciplines`).subscribe((data) => {
      this.disciplines.set(data);
      if (!this.scheduleForm.disciplineId && data.length) {
        this.scheduleForm.disciplineId = data[0].id;
      }
      this.ensureScheduleTeacher();
    });
  }

  loadTeachers() {
    this.http.get<Teacher[]>(`${this.api}/teachers`).subscribe((data) => {
      this.teachers.set(data);
      if (!this.selectedTeacherId() && data.length) {
        this.selectedTeacherId.set(data[0].id);
      }
      this.ensureScheduleTeacher();
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
      this.ensureScheduleTeacher();
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
      .get<AttendanceReportRow[]>(`${this.api}/reports/attendance`, { params: this.reportParams() })
      .subscribe((data) => this.reportRows.set(data));
  }

  saveDiscipline() {
    const id = this.editingDisciplineId();
    const request = id
      ? this.http.put<Discipline>(`${this.api}/disciplines/${id}`, this.disciplineForm)
      : this.http.post<Discipline>(`${this.api}/disciplines`, this.disciplineForm);
    request.subscribe(() => {
      this.cancelDisciplineEdit();
      this.done(id ? 'Disciplina actualizada' : 'Disciplina guardada');
    });
  }

  saveTeacher() {
    const id = this.editingTeacherId();
    const request = id
      ? this.http.put<Teacher>(`${this.api}/teachers/${id}`, this.teacherForm)
      : this.http.post<Teacher>(`${this.api}/teachers`, this.teacherForm);
    request.subscribe(() => {
      this.cancelTeacherEdit();
      this.done(id ? 'Maestro actualizado' : 'Maestro guardado');
    });
  }

  saveSchedule() {
    const id = this.editingScheduleId();
    const request = id
      ? this.http.put<ClassSchedule>(`${this.api}/schedules/${id}`, this.scheduleForm)
      : this.http.post<ClassSchedule>(`${this.api}/schedules`, this.scheduleForm);
    request.subscribe(() => {
      this.cancelScheduleEdit();
      this.done(id ? 'Horario actualizado' : 'Horario guardado');
    });
  }

  saveStudent() {
    const id = this.editingStudentId();
    const request = id
      ? this.http.put<Student>(`${this.api}/students/${id}`, this.studentForm)
      : this.http.post<Student>(`${this.api}/students`, this.studentForm);
    request.subscribe(() => {
      this.cancelStudentEdit();
      this.done(id ? 'Alumno actualizado' : 'Alumno guardado');
    });
  }

  saveEnrollment() {
    const id = this.editingEnrollmentId();
    const request = id
      ? this.http.put<Enrollment>(`${this.api}/enrollments/${id}`, this.enrollmentForm)
      : this.http.post<Enrollment>(`${this.api}/enrollments`, this.enrollmentForm);
    request.subscribe({
      next: () => {
        this.cancelEnrollmentEdit();
        this.done(id ? 'Inscripcion actualizada' : 'Inscripcion guardada');
      },
      error: () => this.message.set('No se pudo inscribir: revisa cupo y datos.'),
    });
  }

  saveAttendance(row: RollCallRow, status: AttendanceStatus) {
    if (this.rollCallLocked()) return;
    row.status = status;
  }

  openTeacherSchedule(schedule: ClassSchedule) {
    this.selectedTeacherScheduleId.set(schedule.id);
    this.attendanceFilters.scheduleId = schedule.id;
    this.syncAttendanceDateToToday(false);
    this.loadRollCall();
  }

  closeTeacherSchedule() {
    this.selectedTeacherScheduleId.set(null);
  }

  saveRollCall() {
    if (!this.attendanceFilters.scheduleId || this.rollCallLocked()) return;
    this.syncAttendanceDateToToday(false);
    const body = {
      scheduleId: this.attendanceFilters.scheduleId,
      attendanceDate: this.attendanceFilters.date,
      records: this.rollCall().map((row) => ({
        enrollmentId: row.enrollmentId,
        attendanceDate: this.attendanceFilters.date,
        status: row.status,
        observations: row.observations,
      })),
    };
    this.http.post<RollCallRow[]>(`${this.api}/attendances/roll-call`, body).subscribe({
      next: (data) => {
        this.rollCall.set(data);
        this.message.set('Lista guardada. Tienes 15 minutos para hacer cambios.');
        this.loadDashboard();
        this.loadAttendanceReport();
      },
      error: (response) => {
        this.message.set(response.status === 423 ? 'La lista ya está cerrada y no puede modificarse.' : 'No se pudo guardar la lista.');
        this.loadRollCall();
      },
    });
  }

  rollCallLocked() {
    return this.rollCall().some((row) => row.locked);
  }

  rollCallSaved() {
    return this.rollCall().some((row) => Boolean(row.savedAt));
  }

  rollCallEditableUntil() {
    return this.rollCall().find((row) => row.editableUntil)?.editableUntil ?? null;
  }

  rollCallStatusText() {
    if (!this.rollCall().length) {
      return 'Sin alumnos para este horario';
    }
    if (this.rollCallLocked()) {
      return 'Lista cerrada';
    }
    const editableUntil = this.rollCallEditableUntil();
    if (editableUntil) {
      return `Editable hasta ${this.formatDateTime(editableUntil)}`;
    }
    return 'Sin guardar';
  }

  formatDateTime(value: string) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return date.toLocaleString('es-MX', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  syncAttendanceDateToToday(reload = true) {
    const realDate = localDateInput();
    if (this.attendanceFilters.date === realDate) return;
    this.attendanceFilters.date = realDate;
    if (reload) {
      this.loadRollCall();
    }
  }

  deactivate(path: string, id: number) {
    this.http.delete(`${this.api}/${path}/${id}`).subscribe(() => this.done('Registro desactivado'));
  }

  reactivate(path: string, id: number) {
    const item = this.catalogItem(path, id);
    if (!item) return;

    this.http.put(`${this.api}/${path}/${id}`, this.catalogPayload(path, item, true)).subscribe(() => this.done('Registro reactivado'));
  }

  editDiscipline(item: Discipline) {
    this.editingDisciplineId.set(item.id);
    this.disciplineForm = { name: item.name, activityType: item.activityType, active: item.active };
  }

  cancelDisciplineEdit() {
    this.editingDisciplineId.set(null);
    this.disciplineForm = { name: '', activityType: 'Deportiva', active: true };
  }

  editTeacher(item: Teacher) {
    this.editingTeacherId.set(item.id);
    this.teacherForm = {
      name: item.name,
      phone: item.phone,
      email: item.email,
      username: item.username,
      password: '',
      disciplineIds: item.disciplines.map((discipline) => discipline.id),
      active: item.active,
    };
  }

  cancelTeacherEdit() {
    this.editingTeacherId.set(null);
    this.teacherForm = {
      name: '',
      phone: '',
      email: '',
      username: '',
      password: '',
      disciplineIds: [],
      active: true,
    };
  }

  editStudent(item: Student) {
    this.editingStudentId.set(item.id);
    this.studentForm = {
      name: item.name,
      actionNumber: item.actionNumber,
      phone: item.phone,
      email: item.email,
      status: item.status,
      active: item.active,
    };
  }

  cancelStudentEdit() {
    this.editingStudentId.set(null);
    this.studentForm = { name: '', actionNumber: '', phone: '', email: '', status: 'ACTIVO', active: true };
  }

  editSchedule(item: ClassSchedule) {
    this.editingScheduleId.set(item.id);
    this.scheduleForm = {
      name: item.name,
      disciplineId: item.discipline.id,
      teacherId: item.teacher.id,
      days: [...item.days],
      startTime: item.startTime,
      endTime: item.endTime,
      capacity: item.capacity,
      active: item.active,
    };
  }

  cancelScheduleEdit() {
    this.editingScheduleId.set(null);
    this.scheduleForm = {
      name: '',
      disciplineId: this.disciplines()[0]?.id ?? 0,
      teacherId: 0,
      days: [],
      startTime: '08:00',
      endTime: '09:00',
      capacity: 10,
      active: true,
    };
    this.ensureScheduleTeacher();
  }

  editEnrollment(item: Enrollment) {
    this.editingEnrollmentId.set(item.id);
    this.enrollmentForm = {
      studentId: item.student.id,
      scheduleId: item.schedule.id,
      frequencyPerWeek: item.frequencyPerWeek,
      selectedDays: [...item.selectedDays],
      active: item.active,
    };
  }

  cancelEnrollmentEdit() {
    this.editingEnrollmentId.set(null);
    this.enrollmentForm = {
      studentId: this.students()[0]?.id ?? 0,
      scheduleId: this.schedules()[0]?.id ?? 0,
      frequencyPerWeek: 1,
      selectedDays: [],
      active: true,
    };
  }

  private catalogItem(path: string, id: number) {
    if (path === 'disciplines') return this.disciplines().find((record) => record.id === id);
    if (path === 'teachers') return this.teachers().find((record) => record.id === id);
    if (path === 'students') return this.students().find((record) => record.id === id);
    if (path === 'schedules') return this.schedules().find((record) => record.id === id);
    if (path === 'enrollments') return this.enrollments().find((record) => record.id === id);
    return undefined;
  }

  private catalogPayload(path: string, item: any, active: boolean) {
    if (path === 'teachers') {
      return {
        name: item.name,
        phone: item.phone,
        email: item.email,
        username: item.username,
        password: '',
        active,
        disciplineIds: item.disciplines.map((discipline: Discipline) => discipline.id),
      };
    }
    if (path === 'students') {
      return { ...item, active, status: active && item.status === 'BAJA' ? 'ACTIVO' : item.status };
    }
    if (path === 'schedules') {
      return {
        name: item.name,
        disciplineId: item.discipline.id,
        teacherId: item.teacher.id,
        days: item.days,
        startTime: item.startTime,
        endTime: item.endTime,
        capacity: item.capacity,
        active,
      };
    }
    if (path === 'enrollments') {
      return {
        studentId: item.student.id,
        scheduleId: item.schedule.id,
        frequencyPerWeek: item.frequencyPerWeek,
        selectedDays: item.selectedDays,
        active,
      };
    }
    return { ...item, active };
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

  disciplineNames(disciplines: Discipline[]) {
    return disciplines.length ? disciplines.map((discipline) => discipline.name).join(', ') : 'Sin disciplinas';
  }

  scheduleTeacherOptions() {
    const disciplineId = Number(this.scheduleForm.disciplineId);
    const selectedTeacherId = Number(this.scheduleForm.teacherId);
    if (!disciplineId) return [];
    return this.teachers().filter((teacher) => {
      const teachesDiscipline = teacher.disciplines.some((discipline) => discipline.id === disciplineId);
      return teachesDiscipline && (teacher.active || teacher.id === selectedTeacherId);
    });
  }

  onScheduleDisciplineChange() {
    this.ensureScheduleTeacher();
  }

  enrollmentCountForSchedule(scheduleId: number) {
    return this.enrollments().filter((enrollment) => enrollment.active && enrollment.schedule.id === scheduleId).length;
  }

  studentEnrollments(studentId: number) {
    return this.enrollments().filter((enrollment) => enrollment.active && enrollment.student.id === studentId);
  }

  studentClassSummary(studentId: number) {
    const classes = this.studentEnrollments(studentId).map((enrollment) => enrollment.schedule.name);
    return classes.length ? classes.join(', ') : 'Sin clases inscritas';
  }

  downloadAttendanceReport(format: 'xlsx' | 'pdf') {
    this.http
      .get(`${this.api}/reports/attendance/export`, {
        params: this.reportParams(format),
        responseType: 'blob',
      })
      .subscribe((file) => {
        const filename = `reporte-asistencias-${this.reportGroup}.${format}`;
        this.downloadBlob(file, filename);
      });
  }

  private reportParams(format?: 'xlsx' | 'pdf') {
    return {
      groupBy: this.reportGroup,
      ...(format ? { format } : {}),
      ...(this.reportFilters.from ? { from: this.reportFilters.from } : {}),
      ...(this.reportFilters.to ? { to: this.reportFilters.to } : {}),
    };
  }

  private ensureScheduleTeacher() {
    const options = this.scheduleTeacherOptions();
    const selectedTeacherExists = options.some((teacher) => teacher.id === Number(this.scheduleForm.teacherId));
    this.scheduleForm.teacherId = selectedTeacherExists ? Number(this.scheduleForm.teacherId) : options[0]?.id ?? 0;
  }

  setTab(tab: string) {
    this.tab.set(tab);
    this.sidebarOpen.set(false);
    if (tab === 'reportes') {
      this.loadAttendanceReport();
    }
  }

  toggleSidebar() {
    this.sidebarOpen.update((open) => !open);
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

  private startClock() {
    this.currentDateTime.set(new Date());
    this.clockTimer = window.setInterval(() => {
      this.currentDateTime.set(new Date());
      const realDate = localDateInput();
      if (this.attendanceFilters.date !== realDate && this.selectedTeacherSchedule()) {
        this.syncAttendanceDateToToday();
      }
    }, 1000);
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

  private normalize(value: string | null | undefined) {
    return (value ?? '')
      .toString()
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }
}

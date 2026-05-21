import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-asistencias',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-asistencias.html',
})
export class PanelAsistencias {
  @Input({ required: true }) state!: any;
}

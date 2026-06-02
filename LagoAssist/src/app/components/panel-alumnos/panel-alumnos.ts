import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-alumnos',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-alumnos.html',
})
export class PanelAlumnos {
  @Input({ required: true }) state!: any;
}

import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-reportes',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-reportes.html',
})
export class PanelReportes {
  @Input({ required: true }) state!: any;
}

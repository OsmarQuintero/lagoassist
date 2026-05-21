import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-inscripciones',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-inscripciones.html',
})
export class PanelInscripciones {
  @Input({ required: true }) state!: any;
}

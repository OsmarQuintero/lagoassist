import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-maestro',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-maestro.html',
})
export class PanelMaestro {
  @Input({ required: true }) state!: any;
}

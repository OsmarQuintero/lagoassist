import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-panel-maestro',
  imports: [CommonModule],
  templateUrl: './panel-maestro.html',
})
export class PanelMaestro {
  @Input({ required: true }) state!: any;
}

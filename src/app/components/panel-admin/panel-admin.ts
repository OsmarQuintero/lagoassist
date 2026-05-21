import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-panel-admin',
  imports: [CommonModule],
  templateUrl: './panel-admin.html',
})
export class PanelAdmin {
  @Input({ required: true }) state!: any;
}

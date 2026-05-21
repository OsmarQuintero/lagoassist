import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-catalogos',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-catalogos.html',
})
export class PanelCatalogos {
  @Input({ required: true }) state!: any;
}

import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-panel-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './panel-login.html',
})
export class PanelLogin {
  @Input({ required: true }) state!: any;
}

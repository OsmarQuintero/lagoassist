import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-menu-lateral',
  imports: [CommonModule],
  templateUrl: './menu-lateral.html',
})
export class MenuLateral {
  @Input({ required: true }) state!: any;
}

import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { DemoAuthService } from './core/auth/demo-auth.service';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.component.html'
})
export class AppComponent {
  readonly router = inject(Router);
  private readonly auth = inject(DemoAuthService);
  sidebarOpen = false;
  logout(): void { this.auth.logout(); void this.router.navigateByUrl('/login'); }
  readonly navigation = [
    { label: 'General', items: [{ label: 'Dashboard', icon: '▦', path: '/' }] },
    { label: 'GESTIÓN', items: [
      { label: 'Socios', icon: '♙', path: '/socios' },
      { label: 'Membresías', icon: '▤', path: '/membresias' },
      { label: 'Inventario', icon: '▣', path: '/inventario' },
      { label: 'Productos', icon: '◫', path: '/productos' },
      { label: 'Ventas', icon: '↗', path: '/ventas' }
    ] },
    { label: 'CONTROL', items: [{ label: 'Finanzas', icon: '◷', path: '/finanzas' }] }
  ];
}

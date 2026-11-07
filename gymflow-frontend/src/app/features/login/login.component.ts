import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DemoAuthService } from '../../core/auth/demo-auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <main class="login-screen">
      <section class="login-visual" aria-label="Bienvenido a GymFlow">
        <div class="login-brand-mark">GF</div>
        <div class="login-visual-content">
          <span class="login-kicker">GYM MANAGEMENT PLATFORM</span>
          <h1>Tu gimnasio.<br><span>En movimiento.</span></h1>
          <p>Administra cada detalle de tu gimnasio desde un solo lugar.</p>
          <div class="login-feature-row">
            <div><strong>Socios</strong><span>Gestiona tus miembros</span></div>
            <div><strong>Inventario</strong><span>Controla tus productos</span></div>
            <div><strong>Finanzas</strong><span>Consulta tus reportes</span></div>
          </div>
        </div>
        <div class="login-visual-foot">© 2026 GymFlow · Demo de portafolio</div>
      </section>
      <section class="login-panel">
        <div class="login-panel-top"><span class="login-mini-brand"><b>G</b> GymFlow</span><span class="demo-pill"><i></i> DEMO</span></div>
        <div class="login-form-wrap">
          <div class="login-icon">↗</div>
          <p class="login-eyebrow">ACCESO A LA PLATAFORMA</p>
          <h2>¡Bienvenido de nuevo!</h2>
          <p class="login-subtitle">Inicia sesión para explorar tu panel de administración.</p>
          <form (ngSubmit)="submit()" #loginForm="ngForm">
            <label for="email">Correo electrónico</label>
            <input id="email" name="email" type="email" [(ngModel)]="email" autocomplete="username" required />
            <label for="password">Contraseña</label>
            <div class="login-password-field"><input id="password" name="password" [type]="showPassword ? 'text' : 'password'" [(ngModel)]="password" autocomplete="current-password" required /><button type="button" (click)="showPassword = !showPassword" [attr.aria-label]="showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'">{{ showPassword ? 'Ocultar' : 'Ver' }}</button></div>
            @if (errorMessage) { <p class="login-error" role="alert">{{ errorMessage }}</p> }
            <button class="login-submit" type="submit" [disabled]="!loginForm.valid">Entrar al sistema <span>→</span></button>
          </form>
          <div class="demo-credentials"><div><span class="credential-label">USUARIO DE DEMOSTRACIÓN</span><strong>{{ demoEmail }}</strong></div><button type="button" (click)="fillCredentials()">Usar acceso <span>↗</span></button><div class="credential-password"><span class="credential-label">CONTRASEÑA</span><strong>{{ demoPassword }}</strong></div></div>
          <p class="login-demo-note"><span>✦</span> Acceso de demostración con datos ficticios. No se realizan cobros reales.</p>
        </div>
        <div class="login-panel-foot">GymFlow <span>·</span> Gestión inteligente para gimnasios</div>
      </section>
    </main>
  `
})
export class LoginComponent {
  private readonly auth = inject(DemoAuthService);
  private readonly router = inject(Router);
  readonly demoEmail = 'demo@gymflow.com';
  readonly demoPassword = 'GymFlowDemo2026';
  email = this.demoEmail;
  password = this.demoPassword;
  showPassword = false;
  errorMessage = '';

  fillCredentials(): void { this.email = this.demoEmail; this.password = this.demoPassword; this.errorMessage = ''; }
  submit(): void {
    if (this.auth.login(this.email, this.password)) {
      this.errorMessage = '';
      void this.router.navigateByUrl('/');
    } else {
      this.errorMessage = 'Las credenciales no son correctas. Usa el acceso de demostración.';
    }
  }
}

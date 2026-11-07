import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CreateMember, GymFlowApiService, Member } from '../../core/services/gymflow-api.service';

@Component({
  selector: 'app-members', standalone: true, imports: [CommonModule, FormsModule],
  template: `
  <section class="page-content simple-page">
    <p class="eyebrow">GESTIÓN DE CLIENTES</p>
    <div class="simple-heading"><div><h1>Socios</h1><p class="page-subtitle">Administra el registro y estado de los socios de tu gimnasio.</p></div><button class="button button-primary" (click)="startCreate()">＋ Nuevo socio</button></div>
    @if (error) { <div class="panel" role="alert"><strong>No se pudo completar la operación.</strong><p>{{ error }}</p><button class="button button-secondary" (click)="loadMembers()">Reintentar</button></div> }
    @if (success) { <div class="panel success-message" role="status">{{ success }}</div> }
    @if (showForm) {
      <form class="panel quick-form" (ngSubmit)="createMember()">
        <h2>{{ editingId ? 'Editar socio' : 'Registrar socio' }}</h2>
        <div class="form-grid">
          <label>Número de socio<input name="memberNumber" [(ngModel)]="form.memberNumber" required maxlength="30" placeholder="SOC-0001"></label>
          <label>Nombre completo<input name="fullName" [(ngModel)]="form.fullName" required maxlength="140" placeholder="Nombre y apellidos"></label>
          <label>Correo electrónico<input name="email" [(ngModel)]="form.email" type="email" maxlength="180" placeholder="socio@correo.com"></label>
          <label>Teléfono<input name="phone" [(ngModel)]="form.phone" maxlength="30" placeholder="2281234567"></label>
        </div>
        <div class="form-actions"><button class="button button-secondary" type="button" (click)="cancelForm()">Cancelar</button><button class="button button-primary" type="submit" [disabled]="saving">{{ saving ? 'Guardando…' : (editingId ? 'Guardar cambios' : 'Guardar socio') }}</button></div>
      </form>
    }
    <div class="stats-grid compact-stats">
      <article class="stat-card"><span class="stat-label">Socios registrados</span><div class="stat-value">{{ members.length }}</div></article>
      <article class="stat-card"><span class="stat-label">Activos</span><div class="stat-value">{{ activeMembers }}</div></article>
      <article class="stat-card"><span class="stat-label">Gimnasio</span><div class="stat-value stat-value-small">{{ gymName || 'Conectando…' }}</div></article>
    </div>
    <div class="panel members-table-panel">
      <div class="table-heading"><div><h2>Directorio de socios</h2><p class="page-subtitle">Información consultada desde la API de GymFlow.</p></div><button class="button button-secondary" (click)="loadMembers()" [disabled]="loading">↻ Actualizar</button></div>
      @if (loading) { <p class="loading-state">Cargando socios…</p> }
      @else if (!members.length) { <div class="empty-state"><span class="empty-icon">♙</span><h2>Aún no hay socios registrados</h2><p>Registra el primero con el botón “Nuevo socio”.</p></div> }
      @else {
        <div class="table-scroll"><table><thead><tr><th>NÚMERO</th><th>SOCIO</th><th>CORREO</th><th>TELÉFONO</th><th>ESTADO</th><th>ACCIONES</th></tr></thead><tbody>
          @for (member of members; track member.id) { <tr><td>{{ member.memberNumber }}</td><td><strong>{{ member.fullName }}</strong></td><td>{{ member.email || '—' }}</td><td>{{ member.phone || '—' }}</td><td><span class="status-pill" [class.status-inactive]="member.status !== 'ACTIVE'">{{ member.status === 'ACTIVE' ? 'Activo' : member.status }}</span></td><td><button class="button button-secondary" type="button" (click)="startEdit(member)">Editar</button></td></tr> }
        </tbody></table></div>
      }
    </div>
  </section>`
})
export class MembersComponent implements OnInit {
  private readonly api = inject(GymFlowApiService);
  members: Member[] = [];
  gymId = '';
  gymName = '';
  showForm = false;
  editingId: string | null = null;
  loading = false;
  saving = false;
  error = '';
  success = '';
  form: CreateMember = this.emptyForm();

  ngOnInit(): void {
    this.api.getOrCreateDemoGym().subscribe({
      next: gym => { this.gymId = gym.id; this.gymName = gym.name; this.loadMembers(); },
      error: err => this.error = this.describeError(err)
    });
  }

  get activeMembers(): number { return this.members.filter(member => member.status === 'ACTIVE').length; }

  loadMembers(): void {
    if (!this.gymId) return;
    this.loading = true; this.error = '';
    this.api.getMembers(this.gymId).pipe(finalize(() => this.loading = false)).subscribe({
      next: members => this.members = members,
      error: err => this.error = this.describeError(err)
    });
  }

  startCreate(): void { this.editingId = null; this.form = this.emptyForm(); this.error = ''; this.success = ''; this.showForm = true; }
  startEdit(member: Member): void { this.editingId = member.id; this.form = { memberNumber: member.memberNumber, fullName: member.fullName, email: member.email || '', phone: member.phone || '' }; this.error = ''; this.success = ''; this.showForm = true; }
  cancelForm(): void { this.showForm = false; this.editingId = null; this.form = this.emptyForm(); this.error = ''; }

  createMember(): void {
    if (!this.gymId || this.saving) return;
    this.saving = true; this.error = ''; this.success = '';
    const payload = { ...this.form, email: this.form.email.trim(), phone: this.form.phone.trim() };
    const request = this.editingId ? this.api.updateMember(this.gymId, this.editingId, payload) : this.api.createMember(this.gymId, payload);
    request.pipe(finalize(() => this.saving = false)).subscribe({
      next: member => {
        this.members = this.editingId ? this.members.map(current => current.id === member.id ? member : current) : [member, ...this.members]; this.showForm = false;
        this.success = this.editingId ? `Los datos de ${member.fullName} se actualizaron correctamente.` : `El socio ${member.fullName} se registró correctamente.`; this.form = this.emptyForm(); this.editingId = null;
      },
      error: err => this.error = this.describeError(err)
    });
  }

  private emptyForm(): CreateMember { return { memberNumber: '', fullName: '', email: '', phone: '' }; }
  private describeError(err: any): string {
    if (err?.status === 401 || err?.status === 403) return 'La API rechazó la autenticación. Verifica las credenciales locales de desarrollo del backend.';
    if (err?.status === 0) return 'No se pudo conectar con el backend. Confirma que Spring Boot esté activo en el puerto 8080.';
    if (err?.status === 409) return 'Ese número de socio ya está registrado en este gimnasio. Introduce otro número.';
    return err?.error?.message || err?.error?.detail || `Error HTTP ${err?.status || 'desconocido'}. Revisa la consola del backend.`;
  }
}

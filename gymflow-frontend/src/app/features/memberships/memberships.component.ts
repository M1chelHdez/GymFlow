import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { CreateMembershipPlan, GymFlowApiService, Member, Membership, MembershipPlan } from '../../core/services/gymflow-api.service';

@Component({
  selector: 'app-memberships', standalone: true, imports: [CommonModule, FormsModule],
  template: `
  <section class="page-content simple-page">
    <p class="eyebrow">GESTIÓN DE MEMBRESÍAS</p>
    <div class="simple-heading"><div><h1>Membresías</h1><p class="page-subtitle">Configura planes y asigna una vigencia a cada socio.</p></div><div class="heading-actions"><button class="button button-secondary" (click)="showPlanForm = !showPlanForm">＋ Nuevo plan</button><button class="button button-primary" (click)="showAssignForm = !showAssignForm">＋ Asignar membresía</button></div></div>
    @if (error) { <div class="panel error-message" role="alert"><strong>No se pudo completar la operación.</strong><p>{{ error }}</p><button class="button button-secondary" (click)="loadAll()">Reintentar</button></div> }
    @if (success) { <div class="panel success-message" role="status">{{ success }}</div> }
    @if (showPlanForm) {
      <form class="panel quick-form" (ngSubmit)="createPlan()"><h2>Crear plan</h2><div class="form-grid">
        <label>Nombre del plan<input name="planName" [(ngModel)]="planForm.name" required maxlength="100" placeholder="Mensual estándar"></label>
        <label>Precio (MXN)<input name="price" [(ngModel)]="planForm.price" required type="number" min="0" step="0.01"></label>
        <label>Duración<input name="durationValue" [(ngModel)]="planForm.durationValue" required type="number" min="1" max="3650"></label>
        <label>Unidad<select name="durationUnit" [(ngModel)]="planForm.durationUnit"><option value="DAY">Días</option><option value="WEEK">Semanas</option><option value="MONTH">Meses</option><option value="YEAR">Años</option></select></label>
        <label class="full-field">Descripción<input name="description" [(ngModel)]="planForm.description" maxlength="500" placeholder="Acceso a las instalaciones"></label>
      </div><div class="form-actions"><button class="button button-secondary" type="button" (click)="showPlanForm = false">Cancelar</button><button class="button button-primary" type="submit" [disabled]="saving">{{ saving ? 'Guardando…' : 'Guardar plan' }}</button></div></form>
    }
    @if (showAssignForm) {
      <form class="panel quick-form" (ngSubmit)="assignMembership()"><h2>Asignar membresía</h2><div class="form-grid">
        <label>Socio<select name="memberId" [(ngModel)]="assignForm.memberId" required><option value="">Selecciona un socio</option>@for (member of members; track member.id) { <option [value]="member.id">{{ member.memberNumber }} — {{ member.fullName }}</option> }</select></label>
        <label>Plan<select name="planId" [(ngModel)]="assignForm.planId" required><option value="">Selecciona un plan</option>@for (plan of activePlans; track plan.id) { <option [value]="plan.id">{{ plan.name }} — {{ plan.price | currency:'MXN':'symbol-narrow' }}</option> }</select></label>
        <label>Fecha de inicio<input name="startDate" [(ngModel)]="assignForm.startDate" type="date" required></label>
      </div><p class="form-hint">La fecha de vencimiento se calcula automáticamente según la duración del plan. Esta operación registra la membresía; todavía no procesa pagos.</p><div class="form-actions"><button class="button button-secondary" type="button" (click)="showAssignForm = false">Cancelar</button><button class="button button-primary" type="submit" [disabled]="saving || !members.length || !activePlans.length">{{ saving ? 'Guardando…' : 'Asignar membresía' }}</button></div></form>
    }
    <div class="stats-grid compact-stats"><article class="stat-card"><span class="stat-label">Membresías registradas</span><div class="stat-value">{{ memberships.length }}</div></article><article class="stat-card"><span class="stat-label">Vigentes</span><div class="stat-value">{{ activeMemberships }}</div></article><article class="stat-card"><span class="stat-label">Planes activos</span><div class="stat-value">{{ activePlans.length }}</div></article></div>
    <div class="panel members-table-panel"><div class="table-heading"><div><h2>Directorio de membresías</h2><p class="page-subtitle">Vigencias consultadas desde la API de GymFlow.</p></div><button class="button button-secondary" (click)="loadAll()" [disabled]="loading">↻ Actualizar</button></div>
      @if (loading) { <p class="loading-state">Cargando membresías…</p> }
      @else if (!memberships.length) { <div class="empty-state"><span class="empty-icon">▤</span><h2>Aún no hay membresías</h2><p>Crea un plan y asígnalo a uno de los socios registrados.</p></div> }
      @else { <div class="table-scroll"><table><thead><tr><th>SOCIO</th><th>PLAN</th><th>INICIO</th><th>VENCIMIENTO</th><th>PRECIO</th><th>ESTADO</th></tr></thead><tbody>@for (membership of memberships; track membership.id) { <tr><td><strong>{{ membership.memberName }}</strong><br><small>{{ membership.memberNumber }}</small></td><td>{{ membership.planName }}</td><td>{{ membership.startDate | date:'dd/MM/yyyy' }}</td><td>{{ membership.endDate | date:'dd/MM/yyyy' }}</td><td>{{ membership.agreedPrice | currency:'MXN':'symbol-narrow' }}</td><td><span class="status-pill" [class.status-inactive]="membership.status !== 'ACTIVE'">{{ membership.status === 'ACTIVE' ? 'Vigente' : membership.status === 'EXPIRED' ? 'Vencida' : 'Cancelada' }}</span></td></tr> }</tbody></table></div> }
    </div>
    <div class="panel plan-list-panel"><div class="table-heading"><div><h2>Planes disponibles</h2><p class="page-subtitle">Catálogo de planes de este gimnasio.</p></div></div>@if (!plans.length) { <p class="loading-state">Todavía no hay planes. Crea el primero con “Nuevo plan”.</p> } @else { <div class="plan-grid">@for (plan of plans; track plan.id) { <article class="plan-card"><div class="plan-card-top"><span class="plan-symbol">✦</span><span class="status-pill" [class.status-inactive]="plan.status !== 'ACTIVE'">{{ plan.status === 'ACTIVE' ? 'Activo' : 'Inactivo' }}</span></div><h3>{{ plan.name }}</h3><p>{{ plan.description || 'Plan de membresía GymFlow' }}</p><div class="plan-price">{{ plan.price | currency:'MXN':'symbol-narrow' }} <small>/ {{ durationLabel(plan) }}</small></div></article> }</div> }</div>
  </section>`
})
export class MembershipsComponent implements OnInit {
  private readonly api = inject(GymFlowApiService);
  gymId = ''; members: Member[] = []; plans: MembershipPlan[] = []; memberships: Membership[] = [];
  showPlanForm = false; showAssignForm = false; loading = false; saving = false; error = ''; success = '';
  planForm: CreateMembershipPlan = this.emptyPlan();
  assignForm = { memberId: '', planId: '', startDate: this.todayIso() };
  ngOnInit(): void { this.loading = true; this.api.getOrCreateDemoGym().subscribe({ next: gym => { this.gymId = gym.id; this.loadAll(); }, error: e => { this.loading = false; this.error = this.describeError(e); } }); }
  get activePlans(): MembershipPlan[] { return this.plans.filter(p => p.status === 'ACTIVE'); }
  get activeMemberships(): number { const today = new Date().toLocaleDateString('en-CA'); return this.memberships.filter(m => m.status === 'ACTIVE' && m.startDate <= today && m.endDate > today).length; }
  loadAll(): void { if (!this.gymId) return; this.loading = true; this.error = ''; forkJoin({members: this.api.getMembers(this.gymId), plans: this.api.getMembershipPlans(this.gymId), memberships: this.api.getMemberships(this.gymId)}).pipe(finalize(() => this.loading = false)).subscribe({next: data => { this.members = data.members; this.plans = data.plans; this.memberships = data.memberships; }, error: e => this.error = this.describeError(e)}); }
  createPlan(): void { if (this.saving) return; this.saving = true; this.error = ''; this.success = ''; const payload = {...this.planForm, name: this.planForm.name.trim(), description: this.planForm.description.trim(), price: Number(this.planForm.price), durationValue: Number(this.planForm.durationValue)}; this.api.createMembershipPlan(this.gymId, payload).pipe(finalize(() => this.saving = false)).subscribe({next: plan => { this.plans = [...this.plans, plan].sort((a,b) => a.name.localeCompare(b.name)); this.showPlanForm = false; this.success = `El plan “${plan.name}” se creó correctamente.`; this.planForm = this.emptyPlan(); }, error: e => this.error = this.describeError(e)}); }
  assignMembership(): void { if (this.saving) return; this.saving = true; this.error = ''; this.success = ''; this.api.createMembership(this.gymId, this.assignForm).pipe(finalize(() => this.saving = false)).subscribe({next: m => { this.memberships = [m, ...this.memberships]; this.showAssignForm = false; this.success = `Membresía asignada a ${m.memberName} hasta el ${m.endDate}.`; this.assignForm = {memberId:'', planId:'', startDate:this.todayIso()}; }, error: e => this.error = this.describeError(e)}); }
  durationLabel(plan: MembershipPlan): string { const units: Record<string,string> = {DAY:'día(s)',WEEK:'semana(s)',MONTH:'mes(es)',YEAR:'año(s)'}; return `${plan.durationValue} ${units[plan.durationUnit]}`; }
  private todayIso(): string { const d = new Date(); const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000); return local.toISOString().slice(0, 10); }
  private emptyPlan(): CreateMembershipPlan { return {name:'', description:'', durationValue:1, durationUnit:'MONTH', price:0}; }
  private describeError(err: any): string { if (err?.status === 0) return 'No se pudo conectar con el backend. Confirma que Spring Boot esté activo en el puerto 8080.'; if (err?.status === 401 || err?.status === 403) return 'La API rechazó la solicitud. Verifica la configuración local del backend.'; if (err?.status === 409) return err?.error?.message || 'El registro entra en conflicto con datos existentes.'; return err?.error?.message || err?.error?.detail || `Error HTTP ${err?.status || 'desconocido'}. Revisa la consola del backend.`; }
}

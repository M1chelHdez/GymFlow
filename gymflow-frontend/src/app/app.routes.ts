import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MembersComponent } from './features/members/members.component';
import { InventoryComponent } from './features/inventory/inventory.component';
import { ProductsComponent } from './features/products/products.component';
import { MembershipsComponent } from './features/memberships/memberships.component';
import { SalesComponent } from './features/sales/sales.component';
import { PlaceholderComponent } from './layout/placeholder.component';
import { ReportsComponent } from './features/reports/reports.component';
import { LoginComponent } from './features/login/login.component';
import { demoAuthGuard } from './core/auth/demo-auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Iniciar sesión | GymFlow' },
  { path: '', canActivate: [demoAuthGuard], component: DashboardComponent, title: 'Dashboard | GymFlow' },
  { path: 'socios', canActivate: [demoAuthGuard], component: MembersComponent, title: 'Socios | GymFlow' },
  { path: 'inventario', canActivate: [demoAuthGuard], component: InventoryComponent, title: 'Inventario | GymFlow' },
  { path: 'membresias', canActivate: [demoAuthGuard], component: MembershipsComponent, title: 'Membresías | GymFlow' },
  { path: 'productos', canActivate: [demoAuthGuard], component: ProductsComponent, title: 'Productos | GymFlow' },
  { path: 'ventas', canActivate: [demoAuthGuard], component: SalesComponent, title: 'Ventas | GymFlow' },
  { path: 'finanzas', canActivate: [demoAuthGuard], component: ReportsComponent, title: 'Reportes | GymFlow' },
  { path: '**', redirectTo: '' }
];

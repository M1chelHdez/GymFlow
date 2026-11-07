import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
@Component({selector:'app-dashboard',standalone:true,imports:[RouterLink],templateUrl:'./dashboard.component.html'})
export class DashboardComponent {
  readonly bars = [34,48,42,65,50,73,58,82,66,91,75,100];
  readonly activities = [
    { initials:'CR', name:'Carlos Ramírez', detail:'Renovación de membresía', time:'09:42 AM', amount:'$599.00', color:'orange' },
    { initials:'MG', name:'Mariana Gómez', detail:'Nueva membresía · Plan mensual', time:'09:18 AM', amount:'$699.00', color:'purple' },
    { initials:'JP', name:'Jorge Pérez', detail:'Compra en punto de venta', time:'08:56 AM', amount:'$145.00', color:'blue' },
    { initials:'AL', name:'Andrea López', detail:'Renovación de membresía', time:'08:31 AM', amount:'$599.00', color:'green' }
  ];
}

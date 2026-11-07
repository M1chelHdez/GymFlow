import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { CreateSale, GymFlowApiService, Member, Product, Sale, StockBalance } from '../../core/services/gymflow-api.service';

type PaymentMethod = 'CASH' | 'CARD' | 'TRANSFER' | 'OTHER';
@Component({ selector: 'app-sales', standalone: true, imports: [CommonModule, FormsModule], template: `
<section class="page-content simple-page">
  <p class="eyebrow">PUNTO DE VENTA</p>
  <div class="simple-heading"><div><h1>Ventas</h1><p class="page-subtitle">Registra ventas de productos y descuenta las existencias automáticamente.</p></div><button class="button button-primary" (click)="showForm=!showForm">＋ Nueva venta</button></div>
  @if(error){<div class="panel error-message" role="alert"><strong>No se pudo completar la operación.</strong><p>{{error}}</p><button class="button button-secondary" (click)="load()">Reintentar</button></div>}
  @if(success){<div class="panel success-message" role="status"><span class="success-icon">✓</span>{{success}}</div>}
  @if(showForm){<form class="panel quick-form" (ngSubmit)="save()"><h2>Registrar venta</h2>
    <div class="form-grid"><label>Socio (opcional)<select name="memberId" [(ngModel)]="memberId"><option value="">Venta a público general</option>@for(m of members;track m.id){<option [value]="m.id">{{m.memberNumber}} — {{m.fullName}}</option>}</select></label><label>Método de pago<select name="paymentMethod" [(ngModel)]="paymentMethod" required><option value="CASH">Efectivo</option><option value="CARD">Tarjeta (registro de prueba)</option><option value="TRANSFER">Transferencia</option><option value="OTHER">Otro</option></select></label>
    <label>Producto<select name="productId" [(ngModel)]="productId" required><option value="">Selecciona un producto</option>@for(p of availableProducts;track p.id){<option [value]="p.id">{{p.sku}} — {{p.name}} ({{stockFor(p.id)}} disponibles)</option>}</select></label><label>Cantidad<input name="quantity" [(ngModel)]="quantity" type="number" min="1" step="1" required /></label></div>
    <div class="form-actions"><span></span><button class="button button-secondary" type="button" (click)="addItem()" [disabled]="!productId || quantity<1">Agregar producto</button></div>
    @if(cart.length){<div class="table-scroll cart-table"><table><thead><tr><th>PRODUCTO</th><th>CANTIDAD</th><th>PRECIO UNITARIO</th><th>IMPORTE</th><th></th></tr></thead><tbody>@for(item of cart;track item.productId){<tr><td><strong>{{item.name}}</strong><small class="cell-description">{{item.sku}}</small></td><td>{{item.quantity}}</td><td>{{item.price|currency:'MXN':'symbol':'1.2-2'}}</td><td><strong>{{item.price*item.quantity|currency:'MXN':'symbol':'1.2-2'}}</strong></td><td><button class="button button-secondary" type="button" (click)="removeItem(item.productId)">Quitar</button></td></tr>}</tbody></table></div>
    <div class="sale-total"><span>Total de la venta</span><strong>{{cartTotal|currency:'MXN':'symbol':'1.2-2'}}</strong></div>}
    <p class="form-hint">La venta y los movimientos de inventario se guardan en una sola transacción. Si no hay existencias suficientes, se rechaza la operación completa. Los métodos de pago son registros manuales de demostración; no se realiza ningún cobro real.</p>
    <div class="form-actions"><button class="button button-secondary" type="button" (click)="cancel()">Cancelar</button><button class="button button-primary" type="submit" [disabled]="saving||!cart.length">{{saving?'Registrando…':'Confirmar venta'}}</button></div>
    @if(!availableProducts.length){<p class="form-hint">No hay productos activos con existencias. Registra productos y existencias desde sus módulos correspondientes.</p>}
  </form>}
  <div class="stats-grid compact-stats"><article class="stat-card"><span class="stat-label">Ventas registradas</span><div class="stat-value">{{sales.length}}</div></article><article class="stat-card"><span class="stat-label">Importe acumulado</span><div class="stat-value stat-value-small">{{totalSales|currency:'MXN':'symbol':'1.2-2'}}</div></article><article class="stat-card"><span class="stat-label">Unidades vendidas</span><div class="stat-value">{{unitsSold}}</div></article></div>
  <div class="panel members-table-panel"><div class="table-heading"><div><h2>Historial de ventas</h2><p class="page-subtitle">Operaciones registradas desde la API de GymFlow.</p></div><button class="button button-secondary" (click)="load()" [disabled]="loading">↻ Actualizar</button></div>
    @if(loading){<p class="loading-state">Cargando ventas…</p>}@else if(!sales.length){<div class="empty-state"><span class="empty-icon">↗</span><h2>Aún no hay ventas registradas</h2><p>Registra una venta de prueba para comprobar que las existencias disminuyen automáticamente.</p></div>}@else{<div class="table-scroll"><table><thead><tr><th>FECHA</th><th>FOLIO</th><th>SOCIO</th><th>ARTÍCULOS</th><th>MÉTODO DE PAGO</th><th>TOTAL</th><th>ESTADO</th></tr></thead><tbody>@for(s of sales;track s.id){<tr><td>{{s.createdAt|date:'short'}}</td><td><strong>{{s.folio}}</strong></td><td>{{s.memberName||'Público general'}}</td><td>{{s.itemCount}}</td><td>{{paymentLabel(s.paymentMethod)}}</td><td><strong>{{s.total|currency:'MXN':'symbol':'1.2-2'}}</strong></td><td><span class="status-pill status-paid">{{s.status==='COMPLETED'?'Completada':s.status}}</span></td></tr>}</tbody></table></div>}
  </div>
</section>` })
export class SalesComponent implements OnInit {
  private api=inject(GymFlowApiService); gymId=''; products:Product[]=[]; members:Member[]=[]; balances:StockBalance[]=[]; sales:Sale[]=[];
  loading=false; saving=false; showForm=false; error=''; success=''; memberId=''; productId=''; quantity=1; paymentMethod:PaymentMethod='CASH';
  cart:{productId:string;name:string;sku:string;quantity:number;price:number}[]=[];
  ngOnInit(){this.api.getOrCreateDemoGym().subscribe({next:g=>{this.gymId=g.id;this.load();},error:e=>this.error=this.message(e)});}
  get availableProducts(){return this.products.filter(p=>p.status==='ACTIVE'&&this.stockFor(p.id)>0);}
  get cartTotal(){return this.cart.reduce((s,i)=>s+i.price*i.quantity,0);}
  get totalSales(){return this.sales.reduce((s,v)=>s+v.total,0);}
  get unitsSold(){return this.sales.reduce((s,v)=>s+v.itemCount,0);}
  stockFor(id:string){return this.balances.find(b=>b.productId===id)?.quantity??0;}
  load(){if(!this.gymId)return;this.loading=true;this.error='';forkJoin({products:this.api.getProducts(this.gymId),members:this.api.getMembers(this.gymId),balances:this.api.getStockBalances(this.gymId),sales:this.api.getSales(this.gymId)}).pipe(finalize(()=>this.loading=false)).subscribe({next:r=>{this.products=r.products;this.members=r.members.filter(m=>m.status==='ACTIVE');this.balances=r.balances;this.sales=r.sales;},error:e=>this.error=this.message(e)});}
  addItem(){const p=this.products.find(x=>x.id===this.productId);if(!p||this.quantity<1)return;const current=this.cart.find(x=>x.productId===p.id);const total=(current?.quantity||0)+Number(this.quantity);if(total>this.stockFor(p.id)){this.error=`Solo hay ${this.stockFor(p.id)} unidad(es) disponibles de ${p.name}.`;return;}if(current)current.quantity=total;else this.cart.push({productId:p.id,name:p.name,sku:p.sku,quantity:Number(this.quantity),price:p.salePrice});this.error='';this.productId='';this.quantity=1;}
  removeItem(id:string){this.cart=this.cart.filter(x=>x.productId!==id);}
  cancel(){this.showForm=false;this.cart=[];this.error='';}
  save(){if(!this.gymId||!this.cart.length)return;const payload:CreateSale={memberId:this.memberId||null,paymentMethod:this.paymentMethod,items:this.cart.map(i=>({productId:i.productId,quantity:i.quantity}))};this.saving=true;this.error='';this.success='';this.api.createSale(this.gymId,payload).pipe(finalize(()=>this.saving=false)).subscribe({next:s=>{this.success=`Venta registrada correctamente por ${s.total.toLocaleString('es-MX',{style:'currency',currency:'MXN'})}. Las existencias se actualizaron automáticamente.`;this.cart=[];this.memberId='';this.showForm=false;this.load();},error:e=>this.error=this.message(e)});}
  paymentLabel(m:string){return ({CASH:'Efectivo',CARD:'Tarjeta (registro)',TRANSFER:'Transferencia',OTHER:'Otro'} as Record<string,string>)[m]||m;}
  private message(e:any){return e?.error?.detail||e?.error?.message||e?.error?.title||'Verifica que el backend esté activo y vuelve a intentarlo.';}
}

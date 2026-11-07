import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Gym {
  id: string;
  name: string;
  timezone: string;
  currency: string;
  status: string;
}

export interface Member {
  id: string;
  gymId: string;
  memberNumber: string;
  fullName: string;
  email: string | null;
  phone: string | null;
  status: string;
  createdAt: string;
}

export interface CreateMember {
  memberNumber: string;
  fullName: string;
  email: string;
  phone: string;
}

export interface MembershipPlan {
  id: string; gymId: string; name: string; description: string | null;
  durationValue: number; durationUnit: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR';
  price: number; status: 'ACTIVE' | 'INACTIVE'; createdAt: string;
}
export interface CreateMembershipPlan {
  name: string; description: string; durationValue: number;
  durationUnit: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR'; price: number;
}
export interface Membership {
  id: string; gymId: string; memberId: string; memberNumber: string; memberName: string;
  planId: string; planName: string; startDate: string; endDate: string; status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  agreedPrice: number; createdAt: string;
}
export interface CreateMembership { memberId: string; planId: string; startDate: string; }
export interface Product {
  id: string; gymId: string; categoryId: string | null; categoryName: string | null;
  sku: string; name: string; description: string | null; costPrice: number; salePrice: number;
  minimumStock: number; status: 'ACTIVE' | 'INACTIVE'; createdAt: string;
}
export interface CreateProduct {
  sku: string; name: string; description: string; categoryId: string | null;
  costPrice: number; salePrice: number; minimumStock: number;
}
export interface ProductChange { id: string; fieldName: string; oldValue: string | null; newValue: string | null; changedBy: string; changedAt: string; }
export interface StockBalance {
  productId: string; sku: string; productName: string; quantity: number; minimumStock: number; lowStock: boolean;
}
export interface StockMovement {
  id: string; gymId: string; productId: string; productName: string;
  type: 'INITIAL' | 'IN' | 'OUT' | 'ADJUSTMENT'; quantity: number; reason: string | null; createdAt: string;
}
export interface CreateStockMovement { productId: string; type: 'INITIAL' | 'IN' | 'OUT'; quantity: number; reason: string; }

@Injectable({ providedIn: 'root' })
export class GymFlowApiService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiBaseUrl;

  getOrCreateDemoGym(): Observable<Gym> {
    return this.http.get<Gym[]>(`${this.api}/gyms`).pipe(
      switchMap(gyms => gyms.length
        ? of(gyms[0])
        : this.http.post<Gym>(`${this.api}/gyms`, {
            name: 'GymFlow Demo', timezone: 'America/Mexico_City', currency: 'MXN'
          }))
    );
  }

  getMembers(gymId: string): Observable<Member[]> {
    return this.http.get<Member[]>(`${this.api}/gyms/${gymId}/members`);
  }

  createMember(gymId: string, member: CreateMember): Observable<Member> {
    return this.http.post<Member>(`${this.api}/gyms/${gymId}/members`, member);
  }
  updateMember(gymId: string, memberId: string, member: CreateMember): Observable<Member> { return this.http.put<Member>(`${this.api}/gyms/${gymId}/members/${memberId}`, member); }

  getMembershipPlans(gymId: string): Observable<MembershipPlan[]> {
    return this.http.get<MembershipPlan[]>(`${this.api}/gyms/${gymId}/membership-plans`);
  }
  createMembershipPlan(gymId: string, plan: CreateMembershipPlan): Observable<MembershipPlan> {
    return this.http.post<MembershipPlan>(`${this.api}/gyms/${gymId}/membership-plans`, plan);
  }
  getMemberships(gymId: string): Observable<Membership[]> {
    return this.http.get<Membership[]>(`${this.api}/gyms/${gymId}/memberships`);
  }
  createMembership(gymId: string, membership: CreateMembership): Observable<Membership> {
    return this.http.post<Membership>(`${this.api}/gyms/${gymId}/memberships`, membership);
  }
  getProducts(gymId: string): Observable<Product[]> { return this.http.get<Product[]>(`${this.api}/gyms/${gymId}/products`); }
  createProduct(gymId: string, product: CreateProduct): Observable<Product> { return this.http.post<Product>(`${this.api}/gyms/${gymId}/products`, product); }
  updateProduct(gymId: string, productId: string, product: CreateProduct): Observable<Product> { return this.http.put<Product>(`${this.api}/gyms/${gymId}/products/${productId}`, product); }
  getProductHistory(gymId: string, productId: string): Observable<ProductChange[]> { return this.http.get<ProductChange[]>(`${this.api}/gyms/${gymId}/products/${productId}/history`); }
  getStockBalances(gymId: string): Observable<StockBalance[]> { return this.http.get<StockBalance[]>(`${this.api}/gyms/${gymId}/inventory/balances`); }
  getStockMovements(gymId: string): Observable<StockMovement[]> { return this.http.get<StockMovement[]>(`${this.api}/gyms/${gymId}/inventory/movements`); }
  createStockMovement(gymId: string, movement: CreateStockMovement): Observable<StockMovement> { return this.http.post<StockMovement>(`${this.api}/gyms/${gymId}/inventory/movements`, movement); }
  getSales(gymId: string): Observable<Sale[]> { return this.http.get<Sale[]>(`${this.api}/gyms/${gymId}/sales`); }
  createSale(gymId: string, sale: CreateSale): Observable<Sale> { return this.http.post<Sale>(`${this.api}/gyms/${gymId}/sales`, sale); }
}

export interface Sale {
  id: string; folio: string; gymId: string; memberId: string | null; memberName: string | null;
  status: string; paymentMethod: 'CASH' | 'CARD' | 'TRANSFER' | 'OTHER'; paymentStatus: string;
  subtotal: number; discountTotal: number; total: number; itemCount: number; createdAt: string;
}
export interface CreateSale { memberId: string | null; paymentMethod: 'CASH' | 'CARD' | 'TRANSFER' | 'OTHER'; items: { productId: string; quantity: number }[]; }

import { Injectable } from '@angular/core';

const SESSION_KEY = 'gymflow-demo-session';
const DEMO_EMAIL = 'demo@gymflow.com';
const DEMO_PASSWORD = 'GymFlowDemo2026';

@Injectable({ providedIn: 'root' })
export class DemoAuthService {
  isAuthenticated(): boolean {
    return sessionStorage.getItem(SESSION_KEY) === 'authenticated';
  }

  login(email: string, password: string): boolean {
    if (email.trim().toLowerCase() !== DEMO_EMAIL || password !== DEMO_PASSWORD) return false;
    sessionStorage.setItem(SESSION_KEY, 'authenticated');
    return true;
  }

  logout(): void {
    sessionStorage.removeItem(SESSION_KEY);
  }
}

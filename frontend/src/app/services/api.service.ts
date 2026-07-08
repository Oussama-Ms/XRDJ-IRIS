import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountingTreatment } from '../models/treatment.model';
import { SystemHealth } from '../models/system-health.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = 'http://localhost:8080/api';
  private http = inject(HttpClient);

  // Dashboard endpoints
  getTreatments(): Observable<AccountingTreatment[]> {
    return this.http.get<AccountingTreatment[]>(`${this.apiUrl}/dashboard/treatments`);
  }

  getRejectionsSummary(): Observable<{[key: string]: number}> {
    return this.http.get<{[key: string]: number}>(`${this.apiUrl}/dashboard/rejections-summary`);
  }

  // Admin endpoints
  getSystemHealth(): Observable<SystemHealth> {
    return this.http.get<SystemHealth>(`${this.apiUrl}/admin/health`);
  }

  triggerIngestion(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/admin/trigger-ingestion`, {});
  }

  purgeCache(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/admin/purge-cache`, {});
  }

  reprocessTreatment(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/admin/reprocess/${id}`, {});
  }
}

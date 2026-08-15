import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountingTreatment } from '../models/treatment.model';
import { SystemHealth } from '../models/system-health.model';
import { AnomalyRecord } from '../models/anomaly-record.model';

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

  getRejectionsSummary(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/dashboard/rejections-summary`);
  }

  getAnomalies(): Observable<AnomalyRecord[]> {
    return this.http.get<AnomalyRecord[]>(`${this.apiUrl}/dashboard/anomalies`);
  }

  // Admin endpoints
  getSystemHealth(): Observable<SystemHealth> {
    return this.http.get<SystemHealth>(`${this.apiUrl}/admin/health`);
  }

  triggerIngestion(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/admin/trigger-ingestion`, {});
  }

  clearData(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/clear-data`);
  }

  reprocessTreatment(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/admin/reprocess/${id}`, {});
  }
}

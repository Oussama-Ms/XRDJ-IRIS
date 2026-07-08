import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserDashboardComponent } from '../user-dashboard/user-dashboard.component';
import { ApiService } from '../../services/api.service';
import { SystemHealth } from '../../models/system-health.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UserDashboardComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private apiService = inject(ApiService);
  health: SystemHealth | null = null;

  ngOnInit(): void {
    this.loadHealth();
  }

  loadHealth(): void {
    this.apiService.getSystemHealth().subscribe(data => {
      this.health = data;
    });
  }

  triggerIngestion(): void {
    this.apiService.triggerIngestion().subscribe(() => {
      console.log('Triggered manual ingestion.');
      this.refreshData();
    });
  }

  purgeCache(): void {
    this.apiService.purgeCache().subscribe(() => {
      console.log('Purged rejected cache.');
      this.refreshData();
    });
  }

  reprocess(id: string): void {
    this.apiService.reprocessTreatment(id).subscribe(() => {
      console.log(`Reprocessed treatment ${id}`);
      this.refreshData();
    });
  }

  // To trigger reloads in the child UserDashboardComponent,
  // we would typically use an event bus, state management, or viewchild.
  // For simplicity, we just reload the whole page to reflect mocked backend state.
  refreshData(): void {
    window.location.reload();
  }
}

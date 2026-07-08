import { Component, inject, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { SystemHealth } from '../../models/system-health.model';

@Component({
  selector: 'app-import-data',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './import-data.component.html',
  styleUrl: './import-data.component.css'
})
export class ImportDataComponent implements OnInit {
  private apiService = inject(ApiService);
  health: SystemHealth | null = null;
  loading = false;
  successMsg = '';

  ngOnInit() {
    this.loadHealth();
  }

  loadHealth() {
    this.apiService.getSystemHealth().subscribe(data => this.health = data);
  }

  triggerIngestion() {
    this.loading = true;
    this.apiService.triggerIngestion().subscribe(() => {
      this.loading = false;
      this.successMsg = 'Manual ingestion triggered successfully.';
      this.loadHealth();
      setTimeout(() => this.successMsg = '', 3000);
    });
  }

  purgeCache() {
    this.apiService.purgeCache().subscribe(() => {
      this.successMsg = 'Rejected cache purged.';
      this.loadHealth();
      setTimeout(() => this.successMsg = '', 3000);
    });
  }
}

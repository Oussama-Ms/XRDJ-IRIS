import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ApiService } from '../../services/api.service';
import { AccountingTreatment } from '../../models/treatment.model';

@Component({
  selector: 'app-visualize',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './visualize.component.html',
  styleUrl: './visualize.component.css'
})
export class VisualizeComponent implements OnInit {
  protected apiService = inject(ApiService);
  treatments: AccountingTreatment[] = [];
  
  // Pie chart
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { display: true, position: 'bottom' } },
  };
  public pieChartData: ChartData<'pie', number[], string | string[]> = { labels: [], datasets: [{ data: [] }] };
  public pieChartType: ChartType = 'pie';

  // Bar chart
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: { x: {}, y: { beginAtZero: true } },
    plugins: { legend: { display: true } },
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  public availableFluxTypes: string[] = [];
  public selectedFluxTypes: { [key: string]: boolean } = {};

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.apiService.getTreatments().subscribe(data => {
      this.treatments = data;
      this.updateBarChart(data);
    });

    this.apiService.getRejectionsSummary().subscribe(data => {
      this.availableFluxTypes = Object.keys(data);
      this.availableFluxTypes.forEach(ft => {
        if (this.selectedFluxTypes[ft] === undefined) {
          this.selectedFluxTypes[ft] = true;
        }
      });
      this.updatePieChart(data);
    });
  }

  updateBarChart(data: AccountingTreatment[]) {
    const dates = [...new Set(data.map(t => new Date(t.dateTraitement).toLocaleDateString()))];
    const traites = dates.map(d => data.filter(t => new Date(t.dateTraitement).toLocaleDateString() === d).reduce((sum, t) => sum + t.nbCreTraites, 0));
    const rejetes = dates.map(d => data.filter(t => new Date(t.dateTraitement).toLocaleDateString() === d).reduce((sum, t) => sum + t.nbCreRejetes, 0));

    this.barChartData = {
      labels: dates,
      datasets: [
        { data: traites, label: 'CRE Traités', backgroundColor: '#3b82f6', borderRadius: 4 },
        { data: rejetes, label: 'CRE Rejetés', backgroundColor: '#ef4444', borderRadius: 4 }
      ]
    };
  }

  updatePieChart(data: { [key: string]: number }) {
    const labels = [];
    const values = [];
    for (const key of Object.keys(data)) {
      if (this.selectedFluxTypes[key]) {
        labels.push(key);
        values.push(data[key]);
      }
    }
    
    this.pieChartData = {
      labels: labels,
      datasets: [{
        data: values,
        backgroundColor: ['#ef4444', '#f59e0b', '#3b82f6', '#10b981']
      }]
    };
  }

  onFilterChange() {
    this.apiService.getRejectionsSummary().subscribe(data => this.updatePieChart(data));
  }
}

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ApiService } from '../../services/api.service';
import { AccountingTreatment } from '../../models/treatment.model';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit {
  protected apiService = inject(ApiService);

  treatments: AccountingTreatment[] = [];
  
  // Pie chart
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true, position: 'top' },
    }
  };
  public pieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: [],
    datasets: [{ data: [] }]
  };
  public pieChartType: ChartType = 'pie';

  // Bar chart
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: { x: {}, y: { beginAtZero: true } },
    plugins: {
      legend: { display: true },
    }
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: []
  };

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
        { data: traites, label: 'CRE Traités', backgroundColor: '#4bc0c0' },
        { data: rejetes, label: 'CRE Rejetés', backgroundColor: '#ff6384' }
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
        backgroundColor: ['#ff6384', '#36a2eb', '#ffce56', '#4bc0c0']
      }]
    };
  }

  onFilterChange() {
    // Reload pie chart data with new filters
    this.apiService.getRejectionsSummary().subscribe(data => {
      this.updatePieChart(data);
    });
  }
}

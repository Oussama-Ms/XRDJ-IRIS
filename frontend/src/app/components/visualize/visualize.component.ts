import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ApiService } from '../../services/api.service';
import { AccountingTreatment } from '../../models/treatment.model';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-visualize',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective, TranslateModule],
  providers: [DatePipe],
  templateUrl: './visualize.component.html',
  styleUrl: './visualize.component.css'
})
export class VisualizeComponent implements OnInit {
  protected apiService = inject(ApiService);
  public translate = inject(TranslateService);
  private datePipe = inject(DatePipe);

  treatments: AccountingTreatment[] = [];
  filteredTreatments: AccountingTreatment[] = [];

  // Pagination
  currentPage: number = 1;
  pageSize: number = 5;

  // Filters
  startDate: string = '';
  endDate: string = '';
  selectedFluxName: string = '';
  fluxNames: string[] = [];

  // Pie chart
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: { legend: { display: false } } // the image has legend on left, custom legend maybe, but I will put it false or top
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
    plugins: { legend: { display: true, position: 'top' } }
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  public availableFluxTypes: string[] = [];
  public selectedFluxTypes: { [key: string]: boolean } = {};

  ngOnInit(): void {
    this.pieChartOptions!.plugins!.legend = { display: true, position: 'left' };
    this.loadData();
  }

  loadData(): void {
    this.apiService.getTreatments().subscribe((data) => {
      this.treatments = data;
      this.filteredTreatments = [...this.treatments];
      this.fluxNames = ['CRE', 'EC'];
      this.updateBarChart(this.filteredTreatments);
    });

    this.apiService.getRejectionsSummary().subscribe((data) => {
      this.availableFluxTypes = ['CRE', 'EC'];
      this.availableFluxTypes.forEach((ft) => {
        if (this.selectedFluxTypes[ft] === undefined) {
          this.selectedFluxTypes[ft] = true;
        }
      });
      // Ensure data has 0 values if missing, so pie chart renders them if they have 0
      const completeData = {
        CRE: data['CRE'] || 0,
        EC: data['EC'] || 0
      };
      this.updatePieChart(completeData);
    });
  }

  get paginatedTreatments(): AccountingTreatment[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.filteredTreatments.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredTreatments.length / this.pageSize) || 1;
  }

  nextPage() {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  previousPage() {
    if (this.currentPage > 1) this.currentPage--;
  }

  applyFilters() {
    this.filteredTreatments = this.treatments.filter((t) => {
      let match = true;
      const tDate = new Date(t.dateTraitement);

      if (this.startDate) {
        const start = new Date(this.startDate);
        if (tDate < start) match = false;
      }
      if (this.endDate) {
        const end = new Date(this.endDate);
        // set end to end of day
        end.setHours(23, 59, 59, 999);
        if (tDate > end) match = false;
      }
      if (this.selectedFluxName && this.selectedFluxName !== '') {
        if (t.nomApplication !== this.selectedFluxName) match = false;
      }
      return match;
    });

    this.currentPage = 1;
    this.updateBarChart(this.filteredTreatments);

    // Filter pie chart dynamically based on visible items
    const rejectionsByFlux: { [key: string]: number } = { CRE: 0, EC: 0 };
    this.filteredTreatments.forEach((t) => {
      if (rejectionsByFlux[t.nomApplication] === undefined) rejectionsByFlux[t.nomApplication] = 0;
      rejectionsByFlux[t.nomApplication] += t.nbCreRejetes;
    });
    this.updatePieChart(rejectionsByFlux);
  }

  showCRE = true;
  showEC = true;

  onBarChartFilterChange() {
    this.updateBarChart(this.filteredTreatments);
  }

  updateBarChart(data: AccountingTreatment[]) {
    const chartData = data.filter((t) => {
      if (t.nomApplication === 'CRE' && !this.showCRE) return false;
      if (t.nomApplication === 'EC' && !this.showEC) return false;
      return true;
    });

    const dates = [
      ...new Set(chartData.map((t) => new Date(t.dateTraitement).toLocaleDateString()))
    ];
    const traites = dates.map((d) =>
      chartData
        .filter((t) => new Date(t.dateTraitement).toLocaleDateString() === d)
        .reduce((sum, t) => sum + t.nbCreTraites, 0)
    );
    const rejetes = dates.map((d) =>
      chartData
        .filter((t) => new Date(t.dateTraitement).toLocaleDateString() === d)
        .reduce((sum, t) => sum + t.nbCreRejetes, 0)
    );
    const recus = dates.map((d) =>
      chartData
        .filter((t) => new Date(t.dateTraitement).toLocaleDateString() === d)
        .reduce((sum, t) => sum + t.nbCreRecus, 0)
    );

    this.translate.get(['TABLE.RECEIVED', 'TABLE.TREATED', 'TABLE.REJECTED']).subscribe((res) => {
      this.barChartData = {
        labels: dates,
        datasets: [
          { data: recus, label: res['TABLE.RECEIVED'] || 'Reçu', backgroundColor: '#A29691' },
          { data: traites, label: res['TABLE.TREATED'] || 'Traité', backgroundColor: '#FFD700' },
          { data: rejetes, label: res['TABLE.REJECTED'] || 'Rejeté', backgroundColor: '#4E3F3A' }
        ]
      };
    });
  }

  updatePieChart(data: { [key: string]: number }) {
    const labels = [];
    const values = [];
    for (const key of Object.keys(data)) {
      if (this.selectedFluxTypes[key] !== false) {
        labels.push(key);
        values.push(data[key]);
      }
    }

    this.pieChartData = {
      labels: labels,
      datasets: [
        {
          data: values,
          backgroundColor: ['#FFD700', '#4E3F3A', '#D9C8B4', '#8B7355', '#6B5B53']
        }
      ]
    };
  }

  onFilterChange() {
    this.applyFilters();
  }

  switchLang(lang: string) {
    this.translate.use(lang);
    this.updateBarChart(this.filteredTreatments);
  }

  exportPDF() {
    this.translate
      .get([
        'DASHBOARD.MACRO_TABLE',
        'TABLE.DATE',
        'TABLE.TIME',
        'TABLE.FLUX_NAME',
        'TABLE.CRE_FILE',
        'TABLE.CRE_RECEIVED',
        'TABLE.CRE_TREATED',
        'TABLE.CRE_REJECTED',
        'TABLE.ME_GENERATED',
        'TABLE.STATUS'
      ])
      .subscribe((res) => {
        const doc = new jsPDF('landscape');

        const head = [
          [
            res['TABLE.DATE'],
            res['TABLE.TIME'],
            res['TABLE.FLUX_NAME'],
            res['TABLE.CRE_FILE'],
            res['TABLE.CRE_RECEIVED'],
            res['TABLE.CRE_TREATED'],
            res['TABLE.CRE_REJECTED'],
            res['TABLE.ME_GENERATED'],
            res['TABLE.STATUS']
          ]
        ];

        const body = this.filteredTreatments.map((t) => [
          this.datePipe.transform(t.dateTraitement, 'dd/MM/yyyy'),
          this.datePipe.transform(t.dateTraitement, 'HH:mm:ss'),
          t.nomApplication,
          t.typeFlux,
          t.nbCreRecus.toString(),
          t.nbCreTraites.toString(),
          t.nbCreRejetes.toString(),
          t.nbMeGeneres.toString(),
          this.translate.instant(
            'STATUS.' +
              (t.statut === 'Traité complètement'
                ? 'TRAITE_COMPLETEMENT'
                : t.statut === 'Rejeté partiellement'
                  ? 'REJETE_PARTIELLEMENT'
                  : t.statut === 'Rejeté complètement'
                    ? 'REJETE_COMPLETEMENT'
                    : '')
          ) || t.statut
        ]);

        doc.text(res['DASHBOARD.MACRO_TABLE'], 14, 15);
        autoTable(doc, {
          head: head,
          body: body,
          startY: 20,
          theme: 'striped',
          styles: { fontSize: 8 },
          headStyles: { fillColor: [78, 63, 58] } // #4E3F3A in RGB
        });

        doc.save('export_macro.pdf');
      });
  }

  exportExcel() {
    this.translate
      .get([
        'TABLE.DATE',
        'TABLE.TIME',
        'TABLE.FLUX_NAME',
        'TABLE.CRE_FILE',
        'TABLE.CRE_RECEIVED',
        'TABLE.CRE_TREATED',
        'TABLE.CRE_REJECTED',
        'TABLE.ME_GENERATED',
        'TABLE.STATUS'
      ])
      .subscribe((res) => {
        const headers = [
          res['TABLE.DATE'],
          res['TABLE.TIME'],
          res['TABLE.FLUX_NAME'],
          res['TABLE.CRE_FILE'],
          res['TABLE.CRE_RECEIVED'],
          res['TABLE.CRE_TREATED'],
          res['TABLE.CRE_REJECTED'],
          res['TABLE.ME_GENERATED'],
          res['TABLE.STATUS']
        ];

        const data = this.filteredTreatments.map((t) => [
          this.datePipe.transform(t.dateTraitement, 'dd/MM/yyyy'),
          this.datePipe.transform(t.dateTraitement, 'HH:mm:ss'),
          t.nomApplication,
          t.typeFlux,
          t.nbCreRecus,
          t.nbCreTraites,
          t.nbCreRejetes,
          t.nbMeGeneres,
          this.translate.instant(
            'STATUS.' +
              (t.statut === 'Traité complètement'
                ? 'TRAITE_COMPLETEMENT'
                : t.statut === 'Rejeté partiellement'
                  ? 'REJETE_PARTIELLEMENT'
                  : t.statut === 'Rejeté complètement'
                    ? 'REJETE_COMPLETEMENT'
                    : '')
          ) || t.statut
        ]);

        import('xlsx').then((XLSX) => {
          const sheet = XLSX.utils.aoa_to_sheet([headers, ...data]);
          const wb = XLSX.utils.book_new();
          XLSX.utils.book_append_sheet(wb, sheet, 'Macro Treatments');
          XLSX.writeFile(wb, 'export_macro.xlsx');
        });
      });
  }
}

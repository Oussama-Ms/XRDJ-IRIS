import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AnomalyRecord } from '../../models/anomaly-record.model';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-stock-rejets',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './stock-rejets.component.html',
  styleUrl: './stock-rejets.component.css'
})
export class StockRejetsComponent implements OnInit {
  protected apiService = inject(ApiService);
  public translate = inject(TranslateService);
  
  anomalies: AnomalyRecord[] = [];
  filteredAnomalies: AnomalyRecord[] = [];
  
  // Pagination
  currentPage: number = 1;
  pageSize: number = 10;

  // Filters
  searchTerm: string = '';
  
  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.apiService.getAnomalies().subscribe(data => {
      this.anomalies = data;
      this.filteredAnomalies = [...this.anomalies];
    });
  }

  get paginatedAnomalies(): AnomalyRecord[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.filteredAnomalies.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAnomalies.length / this.pageSize) || 1;
  }

  nextPage() {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  previousPage() {
    if (this.currentPage > 1) this.currentPage--;
  }

  applyFilters() {
    if (!this.searchTerm) {
      this.filteredAnomalies = [...this.anomalies];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredAnomalies = this.anomalies.filter(a => 
        (a.batchid && a.batchid.toLowerCase().includes(term)) ||
        (a.idCre && a.idCre.toLowerCase().includes(term)) ||
        (a.codeErreur && a.codeErreur.toLowerCase().includes(term)) ||
        (a.texteErreur && a.texteErreur.toLowerCase().includes(term))
      );
    }
    this.currentPage = 1;
  }

  switchLang(lang: string) {
    this.translate.use(lang);
  }

  exportPDF() {
    this.translate.get([
      'STOCK_REJETS.TITLE', 'TABLE.CRE_TYPE', 'TABLE.COD_APP', 'TABLE.BATCHID', 'TABLE.ID_CRE', 'TABLE.CODE_ERREUR', 'TABLE.TEXTE_ERREUR', 'TABLE.NBR_RECYCLAGE', 'TABLE.DAR_DATE', 'TABLE.DAR_TIME', 'TABLE.COD_LOT', 'TABLE.COD_AGENCE', 'TABLE.DAT_OPERAT', 'TABLE.DEVISE_OP', 'TABLE.MNT_OPERAT', 'TABLE.CODE_RECYCLAGE', 'TABLE.CODE_PHASE', 'TABLE.CODE_DOMAINE', 'TABLE.TEXTE_COMP_ERREUR', 'TABLE.EMETTEUR', 'TABLE.NUM_CRE_ERR', 'TABLE.CODE_CRE', 'TABLE.VERSION_CRE', 'TABLE.CODE_INSTANCE', 'TABLE.NUM_ANO', 'TABLE.TYP_ANO', 'TABLE.NIV_GENE', 'TABLE.ORIGINE_ANO', 'TABLE.IND_ENREG_CRE', 'TABLE.CODE_ENREG', 'TABLE.TYPE_REGLE', 'TABLE.CODE_REGLE_ENREG', 'TABLE.DEB_VERSION_REGLE_ENREG', 'TABLE.FIN_VERSION_REGLE_ENREG', 'TABLE.CODE_REGLE_ME', 'TABLE.DEB_VERSION_REGLE_ME', 'TABLE.FIN_VERSION_REGLE_ME', 'TABLE.CODE', 'TABLE.IDF_VACATION', 'TABLE.IDF_ETAPE', 'TABLE.DATE_VACATION', 'TABLE.HEURE_VACATION', 'TABLE.LOT', 'TABLE.NIV_DETECT', 'TABLE.CODE_PRIO_SCHEMA', 'TABLE.CODE_SCHEMA', 'TABLE.NUM_SEQ_GARN', 'TABLE.ADR_GARN', 'TABLE.CODE_FORMAT_ME', 'TABLE.MNEMO_MODULE', 'TABLE.CODE_ETAT_AUTOM', 'TABLE.LG_ENREG', 'TABLE.ENREG'
    ]).subscribe(res => {
      // Use 'a0' page format so that all 52 columns can comfortably fit horizontally
      const doc = new jsPDF({ orientation: 'landscape', format: 'a0' });
      
      const head = [[
        res['TABLE.CRE_TYPE'], res['TABLE.COD_APP'], res['TABLE.BATCHID'], res['TABLE.ID_CRE'], res['TABLE.CODE_ERREUR'], res['TABLE.TEXTE_ERREUR'], res['TABLE.NBR_RECYCLAGE'], res['TABLE.DAR_DATE'], res['TABLE.DAR_TIME'], res['TABLE.COD_LOT'], res['TABLE.COD_AGENCE'], res['TABLE.DAT_OPERAT'], res['TABLE.DEVISE_OP'], res['TABLE.MNT_OPERAT'], res['TABLE.CODE_RECYCLAGE'], res['TABLE.CODE_PHASE'], res['TABLE.CODE_DOMAINE'], res['TABLE.TEXTE_COMP_ERREUR'], res['TABLE.EMETTEUR'], res['TABLE.NUM_CRE_ERR'], res['TABLE.CODE_CRE'], res['TABLE.VERSION_CRE'], res['TABLE.CODE_INSTANCE'], res['TABLE.NUM_ANO'], res['TABLE.TYP_ANO'], res['TABLE.NIV_GENE'], res['TABLE.ORIGINE_ANO'], res['TABLE.IND_ENREG_CRE'], res['TABLE.CODE_ENREG'], res['TABLE.TYPE_REGLE'], res['TABLE.CODE_REGLE_ENREG'], res['TABLE.DEB_VERSION_REGLE_ENREG'], res['TABLE.FIN_VERSION_REGLE_ENREG'], res['TABLE.CODE_REGLE_ME'], res['TABLE.DEB_VERSION_REGLE_ME'], res['TABLE.FIN_VERSION_REGLE_ME'], res['TABLE.CODE'], res['TABLE.IDF_VACATION'], res['TABLE.IDF_ETAPE'], res['TABLE.DATE_VACATION'], res['TABLE.HEURE_VACATION'], res['TABLE.LOT'], res['TABLE.NIV_DETECT'], res['TABLE.CODE_PRIO_SCHEMA'], res['TABLE.CODE_SCHEMA'], res['TABLE.NUM_SEQ_GARN'], res['TABLE.ADR_GARN'], res['TABLE.CODE_FORMAT_ME'], res['TABLE.MNEMO_MODULE'], res['TABLE.CODE_ETAT_AUTOM'], res['TABLE.LG_ENREG'], res['TABLE.ENREG']
      ]];

      const body = this.filteredAnomalies.map(a => [
        a.creType || '', a.codApp || '', a.batchid || '', a.idCre || '', a.codeErreur || '', a.texteErreur || '', a.nbrRecyclage || '', a.darDate || '', a.darTime || '', a.codLot || '', a.codAgence || '', a.datOperat || '', a.deviseOp || '', a.mntOperat || '', a.codeRecyclage || '', a.codePhase || '', a.codeDomaine || '', a.texteCompErreur || '', a.emetteur || '', a.numCreErr || '', a.codeCre || '', a.versionCre || '', a.codeInstance || '', a.numAno || '', a.typAno || '', a.nivGene || '', a.origineAno || '', a.indEnregCre || '', a.codeEnreg || '', a.typeRegle || '', a.codeRegleEnreg || '', a.debVersionRegleEnreg || '', a.finVersionRegleEnreg || '', a.codeRegleMe || '', a.debVersionRegleMe || '', a.finVersionRegleMe || '', a.code || '', a.idfVacation || '', a.idfEtape || '', a.dateVacation || '', a.heureVacation || '', a.lot || '', a.nivDetect || '', a.codePrioSchema || '', a.codeSchema || '', a.numSeqGarn || '', a.adrGarn || '', a.codeFormatMe || '', a.mnemoModule || '', a.codeEtatAutom || '', a.lgEnreg || '', a.enreg || ''
      ]);

      doc.text(res['STOCK_REJETS.TITLE'] || 'Stock Rejets', 14, 15);
      autoTable(doc, {
        head: head,
        body: body,
        startY: 20,
        theme: 'striped',
        styles: { fontSize: 8 },
        headStyles: { fillColor: [59, 130, 246] }
      });
      
      doc.save('stock_rejets.pdf');
    });
  }

  exportExcel() {
    this.translate.get([
      'TABLE.CRE_TYPE', 'TABLE.COD_APP', 'TABLE.BATCHID', 'TABLE.ID_CRE', 'TABLE.CODE_ERREUR', 'TABLE.TEXTE_ERREUR', 'TABLE.NBR_RECYCLAGE', 'TABLE.DAR_DATE', 'TABLE.DAR_TIME', 'TABLE.COD_LOT', 'TABLE.COD_AGENCE', 'TABLE.DAT_OPERAT', 'TABLE.DEVISE_OP', 'TABLE.MNT_OPERAT', 'TABLE.CODE_RECYCLAGE', 'TABLE.CODE_PHASE', 'TABLE.CODE_DOMAINE', 'TABLE.TEXTE_COMP_ERREUR', 'TABLE.EMETTEUR', 'TABLE.NUM_CRE_ERR', 'TABLE.CODE_CRE', 'TABLE.VERSION_CRE', 'TABLE.CODE_INSTANCE', 'TABLE.NUM_ANO', 'TABLE.TYP_ANO', 'TABLE.NIV_GENE', 'TABLE.ORIGINE_ANO', 'TABLE.IND_ENREG_CRE', 'TABLE.CODE_ENREG', 'TABLE.TYPE_REGLE', 'TABLE.CODE_REGLE_ENREG', 'TABLE.DEB_VERSION_REGLE_ENREG', 'TABLE.FIN_VERSION_REGLE_ENREG', 'TABLE.CODE_REGLE_ME', 'TABLE.DEB_VERSION_REGLE_ME', 'TABLE.FIN_VERSION_REGLE_ME', 'TABLE.CODE', 'TABLE.IDF_VACATION', 'TABLE.IDF_ETAPE', 'TABLE.DATE_VACATION', 'TABLE.HEURE_VACATION', 'TABLE.LOT', 'TABLE.NIV_DETECT', 'TABLE.CODE_PRIO_SCHEMA', 'TABLE.CODE_SCHEMA', 'TABLE.NUM_SEQ_GARN', 'TABLE.ADR_GARN', 'TABLE.CODE_FORMAT_ME', 'TABLE.MNEMO_MODULE', 'TABLE.CODE_ETAT_AUTOM', 'TABLE.LG_ENREG', 'TABLE.ENREG'
    ]).subscribe(res => {
      const headers = [
        res['TABLE.CRE_TYPE'], res['TABLE.COD_APP'], res['TABLE.BATCHID'], res['TABLE.ID_CRE'], res['TABLE.CODE_ERREUR'], res['TABLE.TEXTE_ERREUR'], res['TABLE.NBR_RECYCLAGE'], res['TABLE.DAR_DATE'], res['TABLE.DAR_TIME'], res['TABLE.COD_LOT'], res['TABLE.COD_AGENCE'], res['TABLE.DAT_OPERAT'], res['TABLE.DEVISE_OP'], res['TABLE.MNT_OPERAT'], res['TABLE.CODE_RECYCLAGE'], res['TABLE.CODE_PHASE'], res['TABLE.CODE_DOMAINE'], res['TABLE.TEXTE_COMP_ERREUR'], res['TABLE.EMETTEUR'], res['TABLE.NUM_CRE_ERR'], res['TABLE.CODE_CRE'], res['TABLE.VERSION_CRE'], res['TABLE.CODE_INSTANCE'], res['TABLE.NUM_ANO'], res['TABLE.TYP_ANO'], res['TABLE.NIV_GENE'], res['TABLE.ORIGINE_ANO'], res['TABLE.IND_ENREG_CRE'], res['TABLE.CODE_ENREG'], res['TABLE.TYPE_REGLE'], res['TABLE.CODE_REGLE_ENREG'], res['TABLE.DEB_VERSION_REGLE_ENREG'], res['TABLE.FIN_VERSION_REGLE_ENREG'], res['TABLE.CODE_REGLE_ME'], res['TABLE.DEB_VERSION_REGLE_ME'], res['TABLE.FIN_VERSION_REGLE_ME'], res['TABLE.CODE'], res['TABLE.IDF_VACATION'], res['TABLE.IDF_ETAPE'], res['TABLE.DATE_VACATION'], res['TABLE.HEURE_VACATION'], res['TABLE.LOT'], res['TABLE.NIV_DETECT'], res['TABLE.CODE_PRIO_SCHEMA'], res['TABLE.CODE_SCHEMA'], res['TABLE.NUM_SEQ_GARN'], res['TABLE.ADR_GARN'], res['TABLE.CODE_FORMAT_ME'], res['TABLE.MNEMO_MODULE'], res['TABLE.CODE_ETAT_AUTOM'], res['TABLE.LG_ENREG'], res['TABLE.ENREG']
      ];

      const data = this.filteredAnomalies.map(a => [
        a.creType || '', a.codApp || '', a.batchid || '', a.idCre || '', a.codeErreur || '', a.texteErreur || '', a.nbrRecyclage || '', a.darDate || '', a.darTime || '', a.codLot || '', a.codAgence || '', a.datOperat || '', a.deviseOp || '', a.mntOperat || '', a.codeRecyclage || '', a.codePhase || '', a.codeDomaine || '', a.texteCompErreur || '', a.emetteur || '', a.numCreErr || '', a.codeCre || '', a.versionCre || '', a.codeInstance || '', a.numAno || '', a.typAno || '', a.nivGene || '', a.origineAno || '', a.indEnregCre || '', a.codeEnreg || '', a.typeRegle || '', a.codeRegleEnreg || '', a.debVersionRegleEnreg || '', a.finVersionRegleEnreg || '', a.codeRegleMe || '', a.debVersionRegleMe || '', a.finVersionRegleMe || '', a.code || '', a.idfVacation || '', a.idfEtape || '', a.dateVacation || '', a.heureVacation || '', a.lot || '', a.nivDetect || '', a.codePrioSchema || '', a.codeSchema || '', a.numSeqGarn || '', a.adrGarn || '', a.codeFormatMe || '', a.mnemoModule || '', a.codeEtatAutom || '', a.lgEnreg || '', a.enreg || ''
      ]);

      const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet([headers, ...data]);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, 'Stock Rejets');
      XLSX.writeFile(wb, 'stock_rejets.xlsx');
    });
  }
}

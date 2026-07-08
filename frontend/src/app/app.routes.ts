import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { LayoutComponent } from './components/layout/layout.component';
import { VisualizeComponent } from './components/visualize/visualize.component';
import { ImportDataComponent } from './components/import-data/import-data.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { 
    path: 'app', 
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'visualize', pathMatch: 'full' },
      { path: 'visualize', component: VisualizeComponent },
      { 
        path: 'import', 
        component: ImportDataComponent,
        canActivate: [roleGuard],
        data: { role: 'ROLE_ADMIN' }
      }
    ]
  },
  { path: '**', redirectTo: '/login' }
];

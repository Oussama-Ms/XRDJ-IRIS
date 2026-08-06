import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { WebSocketService } from '../../services/websocket.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, TranslateModule],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  wsService = inject(WebSocketService);
  
  alerts: string[] = [];
  showNotifications = false;
  private sub?: Subscription;

  ngOnInit() {
    this.sub = this.wsService.alerts$.subscribe(msg => {
      this.alerts.unshift(msg); // Add to top
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
  }

  ngOnDestroy() {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  logout() {
    this.authService.logout();
  }
}

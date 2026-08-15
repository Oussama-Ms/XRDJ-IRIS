import {
  Component,
  ElementRef,
  ViewChild,
  AfterViewInit,
  OnDestroy,
  HostListener,
  inject
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements AfterViewInit, OnDestroy {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  @ViewChild('particleCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  private ctx!: CanvasRenderingContext2D;
  private particles: any[] = [];
  private animationFrameId: number = 0;
  private mouse = { x: -1000, y: -1000 };

  loginForm: FormGroup = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  errorMsg: string | null = null;
  loading = false;

  @HostListener('mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    this.mouse.x = event.clientX;
    this.mouse.y = event.clientY;
  }

  @HostListener('window:resize')
  onResize() {
    this.initCanvas();
  }

  ngAfterViewInit() {
    this.initCanvas();
    this.animate();
  }

  ngOnDestroy() {
    cancelAnimationFrame(this.animationFrameId);
  }

  private initCanvas() {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    this.ctx = canvas.getContext('2d')!;
    this.particles = [];

    const numParticles = Math.floor((canvas.width * canvas.height) / 10000);

    for (let i = 0; i < numParticles; i++) {
      this.particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - 0.5) * 1,
        vy: (Math.random() - 0.5) * 1,
        radius: Math.random() * 2 + 1
      });
    }
  }

  private animate() {
    this.ctx.clearRect(
      0,
      0,
      this.canvasRef.nativeElement.width,
      this.canvasRef.nativeElement.height
    );

    for (let i = 0; i < this.particles.length; i++) {
      let p = this.particles[i];
      p.x += p.vx;
      p.y += p.vy;

      if (p.x < 0 || p.x > this.canvasRef.nativeElement.width) p.vx = -p.vx;
      if (p.y < 0 || p.y > this.canvasRef.nativeElement.height) p.vy = -p.vy;

      // Mouse interaction
      const dx = this.mouse.x - p.x;
      const dy = this.mouse.y - p.y;
      const dist = Math.sqrt(dx * dx + dy * dy);

      if (dist < 100) {
        p.x -= dx * 0.05;
        p.y -= dy * 0.05;
      }

      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      this.ctx.fillStyle = 'rgba(255, 215, 0, 0.4)'; // Gold particles
      this.ctx.fill();

      // Connect particles
      for (let j = i + 1; j < this.particles.length; j++) {
        let p2 = this.particles[j];
        let dx2 = p.x - p2.x;
        let dy2 = p.y - p2.y;
        let dist2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

        if (dist2 < 100) {
          this.ctx.beginPath();
          this.ctx.moveTo(p.x, p.y);
          this.ctx.lineTo(p2.x, p2.y);
          this.ctx.strokeStyle = `rgba(255, 215, 0, ${0.2 - dist2 / 500})`;
          this.ctx.lineWidth = 0.5;
          this.ctx.stroke();
        }
      }
    }
    this.animationFrameId = requestAnimationFrame(() => this.animate());
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.errorMsg = null;

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigate(['/app/visualize']);
      },
      error: (err) => {
        this.errorMsg = 'Invalid username or password.';
        this.loading = false;
      }
    });
  }
}

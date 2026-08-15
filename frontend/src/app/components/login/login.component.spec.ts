import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['login']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty values', () => {
    expect(component.loginForm.value).toEqual({ username: '', password: '' });
    expect(component.loginForm.invalid).toBeTrue();
  });

  it('should display error when login fails', () => {
    component.loginForm.setValue({ username: 'test', password: 'wrongpassword' });
    mockAuthService.login.and.returnValue(throwError(() => new Error('Invalid')));
    
    component.onSubmit();
    
    expect(component.errorMsg).toBe('Invalid username or password.');
    expect(component.loading).toBeFalse();
  });

  it('should navigate to visualize on successful login', () => {
    component.loginForm.setValue({ username: 'admin', password: 'password123' });
    mockAuthService.login.and.returnValue(of({ token: 'fake-jwt', id: 1, username: 'admin', role: 'ROLE_ADMIN' }));
    
    component.onSubmit();
    
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/app/visualize']);
  });
});

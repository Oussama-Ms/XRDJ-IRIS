import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const requiredRole = route.data['role'];
  const userRole = authService.getRole();
  
  if (userRole === requiredRole || userRole === 'ROLE_ADMIN') {
    return true;
  }
  
  return router.parseUrl('/app/visualize');
};

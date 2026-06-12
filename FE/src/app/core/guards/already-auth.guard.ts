import { CanActivateFn } from '@angular/router';

export const alreadyAuthGuard: CanActivateFn = () => {
  return true;
};

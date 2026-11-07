import { HttpInterceptorFn } from '@angular/common/http';

/** Local development only. Replace with JWT authentication before deployment. */
export const devBasicAuthInterceptor: HttpInterceptorFn = (request, next) => {
  const credentials = btoa('devadmin:ChangeMe-OnlyForLocal!');
  return next(request.clone({ setHeaders: { Authorization: `Basic ${credentials}` } }));
};

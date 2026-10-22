import { Routes } from '@angular/router';
import { authGuard, superAdminGuard } from './core/guards';

export const routes: Routes = [
  // পাবলিক লিংক - সবাই এখান থেকে ফরম পূরণ করবে
  { path: '', loadComponent: () => import('./pages/register.component').then((m) => m.RegisterComponent) },
  { path: 'admin/login', loadComponent: () => import('./pages/login.component').then((m) => m.LoginComponent) },
  {
    path: 'admin',
    canActivate: [authGuard],
    loadComponent: () => import('./admin/admin-layout.component').then((m) => m.AdminLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'members' },
      { path: 'members', loadComponent: () => import('./admin/member-list.component').then((m) => m.MemberListComponent) },
      { path: 'members/:id', loadComponent: () => import('./admin/member-edit.component').then((m) => m.MemberEditComponent) },
      { path: 'zilas', canActivate: [superAdminGuard], loadComponent: () => import('./admin/zilas.component').then((m) => m.ZilasComponent) },
      { path: 'users', canActivate: [superAdminGuard], loadComponent: () => import('./admin/users.component').then((m) => m.UsersComponent) },
      { path: 'password', loadComponent: () => import('./admin/change-password.component').then((m) => m.ChangePasswordComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];

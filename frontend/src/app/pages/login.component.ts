import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/bn';
import { OrgHeaderComponent } from '../shared/org-header.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, OrgHeaderComponent],
  template: `
    <app-org-header subtitle="এডমিন প্যানেল" />
    <main class="container" style="max-width: 420px; padding-top: 32px;">
      <form class="card" (ngSubmit)="login()" #f="ngForm">
        <h2 style="margin-bottom: 16px;">লগইন</h2>
        @if (error) { <div class="alert alert-error">{{ error }}</div> }
        <div class="field mb">
          <label for="u">ইউজারনেম</label>
          <input id="u" type="text" name="username" [(ngModel)]="username" required autocomplete="username">
        </div>
        <div class="field mb">
          <label for="p">পাসওয়ার্ড</label>
          <input id="p" type="password" name="password" [(ngModel)]="password" required autocomplete="current-password">
        </div>
        <button class="btn btn-block" type="submit" [disabled]="loading || !username || !password">
          {{ loading ? 'অপেক্ষা করুন...' : 'লগইন করুন' }}
        </button>
      </form>
      <p style="text-align: center;"><a routerLink="/">← সদস্য ফরমে ফিরে যান</a></p>
    </main>
  `,
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  loading = false;
  error = '';

  constructor() {
    if (this.auth.isLoggedIn()) this.router.navigate(['/admin/members']);
  }

  login(): void {
    this.loading = true;
    this.error = '';
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => this.router.navigate(['/admin/members']),
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.error = errorMessage(err);
      },
    });
  }
}

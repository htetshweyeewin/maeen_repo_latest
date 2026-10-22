import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <div class="container-wide bar">
        <div class="brand">
          <strong>যুবসংঘ ডিজিটাল</strong>
          <span>{{ auth.user()?.orgZilaName ? auth.user()?.orgZilaName + ' যেলা' : 'কেন্দ্রীয়' }} এডমিন প্যানেল</span>
        </div>
        <nav>
          <a routerLink="/admin/members" routerLinkActive="active">সদস্য তালিকা</a>
          @if (auth.isSuperAdmin()) {
            <a routerLink="/admin/zilas" routerLinkActive="active">সাংগঠনিক যেলা</a>
            <a routerLink="/admin/users" routerLinkActive="active">এডমিন</a>
          }
          <a routerLink="/admin/password" routerLinkActive="active">পাসওয়ার্ড</a>
        </nav>
        <div class="spacer"></div>
        <div class="user">
          <button type="button" class="btn btn-sm btn-outline" (click)="copyLink()">
            {{ copied ? '✓ কপি হয়েছে' : 'ফরমের লিংক কপি' }}
          </button>
          <span class="who">{{ auth.user()?.fullName }}</span>
          <button type="button" class="btn btn-sm btn-outline" (click)="auth.logout()">লগআউট</button>
        </div>
      </div>
    </header>
    <main class="container-wide" style="padding-top: 20px; padding-bottom: 40px;">
      <router-outlet />
    </main>
  `,
  styles: `
    .topbar { background: var(--primary-dark); color: #fff; border-bottom: 3px solid var(--accent); }
    .bar { display: flex; align-items: center; gap: 18px; flex-wrap: wrap; padding-top: 10px; padding-bottom: 10px; }
    .brand { display: flex; flex-direction: column; line-height: 1.2; }
    .brand span { font-size: .85rem; opacity: .85; }
    nav { display: flex; gap: 4px; flex-wrap: wrap; }
    nav a { color: #fff; text-decoration: none; padding: 6px 12px; border-radius: 6px; opacity: .85; }
    nav a:hover { background: rgba(255,255,255,.1); opacity: 1; }
    nav a.active { background: rgba(255,255,255,.18); opacity: 1; font-weight: 600; }
    .user { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
    .who { font-size: .9rem; }
  `,
})
export class AdminLayoutComponent implements OnInit {
  auth = inject(AuthService);
  copied = false;

  ngOnInit(): void {
    this.auth.refreshMe();
  }

  copyLink(): void {
    const link = location.origin + '/';
    navigator.clipboard?.writeText(link).then(
      () => {
        this.copied = true;
        setTimeout(() => (this.copied = false), 2000);
      },
      () => prompt('পাবলিক ফরমের লিংক:', link),
    );
  }
}

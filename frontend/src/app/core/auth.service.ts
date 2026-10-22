import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginResponse, UserInfo } from './models';

const TOKEN_KEY = 'jsm.token';
const USER_KEY = 'jsm.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly _token = signal<string | null>(read(TOKEN_KEY));
  readonly user = signal<UserInfo | null>(parse(read(USER_KEY)));
  readonly isLoggedIn = computed(() => !!this._token());
  readonly isSuperAdmin = computed(() => this.user()?.role === 'SUPER_ADMIN');

  get token(): string | null {
    return this._token();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { username, password }).pipe(
      tap((res) => {
        this._token.set(res.token);
        this.user.set(res.user);
        write(TOKEN_KEY, res.token);
        write(USER_KEY, JSON.stringify(res.user));
      }),
    );
  }

  refreshMe(): void {
    this.http.get<UserInfo>('/api/auth/me').subscribe((u) => {
      this.user.set(u);
      write(USER_KEY, JSON.stringify(u));
    });
  }

  logout(redirect = true): void {
    this._token.set(null);
    this.user.set(null);
    write(TOKEN_KEY, null);
    write(USER_KEY, null);
    if (redirect) this.router.navigate(['/admin/login']);
  }
}

function read(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string | null): void {
  try {
    if (value === null) localStorage.removeItem(key);
    else localStorage.setItem(key, value);
  } catch {
    /* প্রাইভেট মোডে localStorage না থাকলে শুধু এই সেশনে লগইন থাকবে */
  }
}

function parse(json: string | null): UserInfo | null {
  try {
    return json ? (JSON.parse(json) as UserInfo) : null;
  } catch {
    return null;
  }
}

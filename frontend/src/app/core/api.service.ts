import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import {
  AdminUser, AdminUserRequest, GeoOption, Member, MemberQuery, MemberRequest, Meta, OrgZila, Page, Stats,
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private meta$?: Observable<Meta>;
  private geoCache = new Map<string, Observable<GeoOption[]>>();

  // ---------- পাবলিক ----------

  meta(): Observable<Meta> {
    return (this.meta$ ??= this.http.get<Meta>('/api/public/meta').pipe(shareReplay(1)));
  }

  /** parentId না দিলে বিভাগসমূহ; নইলে তার অধীনস্থ এলাকা */
  geo(parentId?: number | null): Observable<GeoOption[]> {
    const key = String(parentId ?? 'root');
    let obs = this.geoCache.get(key);
    if (!obs) {
      const params = parentId ? new HttpParams().set('parentId', parentId) : undefined;
      obs = this.http.get<GeoOption[]>('/api/public/geo', { params }).pipe(shareReplay(1));
      this.geoCache.set(key, obs);
    }
    return obs;
  }

  submitMember(req: MemberRequest): Observable<{ id: number; message: string }> {
    return this.http.post<{ id: number; message: string }>('/api/public/members', req);
  }

  // ---------- সদস্য (এডমিন) ----------

  members(query: MemberQuery): Observable<Page<Member>> {
    return this.http.get<Page<Member>>('/api/admin/members', { params: toParams(query) });
  }

  stats(query: MemberQuery): Observable<Stats> {
    const { status, page, size, ...rest } = query;
    return this.http.get<Stats>('/api/admin/members/stats', { params: toParams(rest) });
  }

  exportCsv(query: MemberQuery): Observable<Blob> {
    const { page, size, ...rest } = query;
    return this.http.get('/api/admin/members/export', { params: toParams(rest), responseType: 'blob' });
  }

  member(id: number): Observable<Member> {
    return this.http.get<Member>(`/api/admin/members/${id}`);
  }

  updateMember(id: number, req: MemberRequest): Observable<Member> {
    return this.http.put<Member>(`/api/admin/members/${id}`, req);
  }

  approve(id: number): Observable<Member> {
    return this.http.post<Member>(`/api/admin/members/${id}/approve`, {});
  }

  reject(id: number, reason: string | null): Observable<Member> {
    return this.http.post<Member>(`/api/admin/members/${id}/reject`, { reason });
  }

  deleteMember(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/members/${id}`);
  }

  // ---------- সাংগঠনিক যেলা ও এডমিন (কেন্দ্রীয় এডমিন) ----------

  zilas(): Observable<OrgZila[]> {
    return this.http.get<OrgZila[]>('/api/admin/zilas');
  }

  saveZila(id: number | null, body: { name: string; upazilaIds: number[] }): Observable<OrgZila> {
    return id
      ? this.http.put<OrgZila>(`/api/admin/zilas/${id}`, body)
      : this.http.post<OrgZila>('/api/admin/zilas', body);
  }

  deleteZila(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/zilas/${id}`);
  }

  users(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>('/api/admin/users');
  }

  saveUser(id: number | null, body: AdminUserRequest): Observable<AdminUser> {
    return id
      ? this.http.put<AdminUser>(`/api/admin/users/${id}`, body)
      : this.http.post<AdminUser>('/api/admin/users', body);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/users/${id}`);
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>('/api/auth/change-password', { currentPassword, newPassword });
  }
}

function toParams(query: object): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== null && value !== undefined && value !== '') params = params.set(key, String(value));
  }
  return params;
}

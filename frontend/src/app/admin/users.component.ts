import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { errorMessage } from '../core/bn';
import { AdminUser, AdminUserRequest, OrgZila } from '../core/models';

const EMPTY: AdminUserRequest = { username: '', fullName: '', password: '', role: 'ZILA_ADMIN', orgZilaId: null, active: true };

/** এডমিন অ্যাকাউন্ট ব্যবস্থাপনা (কেন্দ্রীয় এডমিন) - প্রতিটি যেলার জন্য যেলা এডমিন তৈরি */
@Component({
  selector: 'app-users',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1 class="mb" style="font-size: 1.4rem;">এডমিন অ্যাকাউন্ট</h1>
    @if (error) { <div class="alert alert-error">{{ error }}</div> }

    <div class="layout">
      <div class="table-wrap">
        <table class="table">
          <thead><tr><th>নাম</th><th>ইউজারনেম</th><th>ভূমিকা</th><th>যেলা</th><th>অবস্থা</th><th></th></tr></thead>
          <tbody>
            @for (u of users; track u.id) {
              <tr>
                <td>{{ u.fullName }}</td>
                <td>{{ u.username }}</td>
                <td>{{ u.roleLabel }}</td>
                <td>{{ u.orgZilaName ?? '—' }}</td>
                <td>
                  <span class="badge" [class.badge-APPROVED]="u.active" [class.badge-REJECTED]="!u.active">
                    {{ u.active ? 'সক্রিয়' : 'নিষ্ক্রিয়' }}
                  </span>
                </td>
                <td>
                  <div class="actions">
                    <button type="button" class="btn btn-sm btn-outline" (click)="edit(u)">সম্পাদনা</button>
                    <button type="button" class="btn btn-sm btn-danger" (click)="remove(u)">মুছুন</button>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <form class="card" (ngSubmit)="save()">
        <h2 class="section-title">{{ editingId ? 'এডমিন সম্পাদনা' : 'নতুন এডমিন' }}</h2>
        <div class="field mb">
          <label>নাম<span class="req">*</span></label>
          <input type="text" name="fullName" [(ngModel)]="form.fullName" required>
        </div>
        <div class="field mb">
          <label>ইউজারনেম (ইংরেজিতে)<span class="req">*</span></label>
          <input type="text" name="username" [(ngModel)]="form.username" required autocomplete="off">
        </div>
        <div class="field mb">
          <label>পাসওয়ার্ড @if (!editingId) { <span class="req">*</span> }</label>
          <input type="password" name="password" [(ngModel)]="form.password" autocomplete="new-password"
                 [placeholder]="editingId ? 'পরিবর্তন না করলে খালি রাখুন' : 'কমপক্ষে ৬ অক্ষর'">
        </div>
        <div class="field mb">
          <label>ভূমিকা<span class="req">*</span></label>
          <select name="role" [(ngModel)]="form.role">
            <option value="ZILA_ADMIN">যেলা এডমিন</option>
            <option value="SUPER_ADMIN">কেন্দ্রীয় এডমিন</option>
          </select>
        </div>
        @if (form.role === 'ZILA_ADMIN') {
          <div class="field mb">
            <label>সাংগঠনিক যেলা<span class="req">*</span></label>
            <select name="orgZilaId" [(ngModel)]="form.orgZilaId">
              <option [ngValue]="null">-- যেলা নির্বাচন করুন --</option>
              @for (z of zilas; track z.id) { <option [ngValue]="z.id">{{ z.name }}</option> }
            </select>
          </div>
        }
        <label class="check mb"><input type="checkbox" name="active" [(ngModel)]="form.active"> সক্রিয়</label>
        <div class="row mt">
          <button class="btn" type="submit" [disabled]="busy">সংরক্ষণ করুন</button>
          @if (editingId) { <button class="btn btn-outline" type="button" (click)="resetForm()">বাতিল</button> }
        </div>
      </form>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr); gap: 18px; align-items: start; }
    @media (max-width: 900px) { .layout { grid-template-columns: minmax(0, 1fr); } }
  `,
})
export class UsersComponent implements OnInit {
  private api = inject(ApiService);

  users: AdminUser[] = [];
  zilas: OrgZila[] = [];
  form: AdminUserRequest = { ...EMPTY };
  editingId: number | null = null;
  busy = false;
  error = '';

  ngOnInit(): void {
    this.load();
    this.api.zilas().subscribe((z) => (this.zilas = z));
  }

  load(): void {
    this.api.users().subscribe({
      next: (u) => (this.users = u),
      error: (err: HttpErrorResponse) => (this.error = errorMessage(err)),
    });
  }

  edit(u: AdminUser): void {
    this.editingId = u.id;
    this.form = { username: u.username, fullName: u.fullName, password: '', role: u.role, orgZilaId: u.orgZilaId, active: u.active };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { ...EMPTY };
  }

  save(): void {
    this.busy = true;
    this.error = '';
    const body = { ...this.form, password: this.form.password || null };
    this.api.saveUser(this.editingId, body).subscribe({
      next: () => {
        this.busy = false;
        this.resetForm();
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.busy = false;
        const fieldErrors = Object.values(err.error?.errors ?? {}) as string[];
        this.error = fieldErrors.length ? fieldErrors.join('। ') : errorMessage(err);
      },
    });
  }

  remove(u: AdminUser): void {
    if (!confirm(`"${u.fullName}" এডমিন অ্যাকাউন্ট মুছে ফেলতে চান?`)) return;
    this.api.deleteUser(u.id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => (this.error = errorMessage(err)),
    });
  }
}

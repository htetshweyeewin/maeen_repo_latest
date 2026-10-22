import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, ViewChild, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { BnDatePipe, errorMessage } from '../core/bn';
import { Member, MemberRequest, OrgZila } from '../core/models';
import { MemberFormComponent } from '../shared/member-form.component';

/** সদস্যের তথ্য দেখা, পরিবর্তন, অনুমোদন ও বাতিল */
@Component({
  selector: 'app-member-edit',
  standalone: true,
  imports: [RouterLink, MemberFormComponent, BnDatePipe],
  template: `
    <div class="row mb">
      <a routerLink="/admin/members" class="btn btn-sm btn-outline">← তালিকায় ফিরুন</a>
      <h1 style="font-size: 1.3rem;">সদস্যের তথ্য</h1>
    </div>

    @if (error) { <div class="alert alert-error">{{ error }}</div> }
    @if (notice) { <div class="alert alert-success">{{ notice }}</div> }

    @if (member; as m) {
      <div class="card section status-bar">
        <div>
          <span class="badge badge-{{ m.status }}">{{ m.statusLabel }}</span>
          <strong style="margin-left: 8px;">{{ m.name }}</strong>
          <div class="muted small">
            নিবন্ধন: {{ m.createdAt | bnDate: true }}
            · সাংগঠনিক যেলা: {{ m.orgZilaName ?? 'নির্ধারিত হয়নি' }}
            @if (m.reviewedBy) { · সর্বশেষ যাচাই: {{ m.reviewedBy }} ({{ m.reviewedAt | bnDate: true }}) }
          </div>
          @if (m.rejectReason) { <div class="small" style="color: var(--danger)">বাতিলের কারণ: {{ m.rejectReason }}</div> }
        </div>
        <div class="spacer"></div>
        <div class="row">
          @if (m.status !== 'APPROVED') {
            <button type="button" class="btn btn-success" [disabled]="busy" (click)="approve()">✓ অনুমোদন করুন</button>
          }
          @if (m.status !== 'REJECTED') {
            <button type="button" class="btn btn-danger" [disabled]="busy" (click)="reject()">✕ বাতিল করুন</button>
          }
          @if (auth.isSuperAdmin()) {
            <button type="button" class="btn btn-outline" [disabled]="busy" (click)="remove()">মুছে ফেলুন</button>
          }
        </div>
      </div>

      @if (!auth.isSuperAdmin() || zilas) {
        <app-member-form [initial]="m" [zilas]="zilas" [submitting]="busy" submitLabel="পরিবর্তন সংরক্ষণ করুন"
                         (save)="save($event)" />
      }
    } @else if (!error) {
      <p class="muted">লোড হচ্ছে...</p>
    }
  `,
  styles: `
    .status-bar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
    .small { font-size: .88rem; margin-top: 4px; }
  `,
})
export class MemberEditComponent implements OnInit {
  /** রাউট প্যারামিটার :id */
  @Input() id!: string;
  @ViewChild(MemberFormComponent) formComponent?: MemberFormComponent;

  auth = inject(AuthService);
  private api = inject(ApiService);
  private router = inject(Router);

  member: Member | null = null;
  zilas: OrgZila[] | null = null;
  busy = false;
  error = '';
  notice = '';

  ngOnInit(): void {
    if (this.auth.isSuperAdmin()) this.api.zilas().subscribe((z) => (this.zilas = z));
    this.api.member(+this.id).subscribe({
      next: (m) => (this.member = m),
      error: (err: HttpErrorResponse) => (this.error = errorMessage(err)),
    });
  }

  save(req: MemberRequest): void {
    this.run(this.api.updateMember(+this.id, req), 'তথ্য সংরক্ষণ করা হয়েছে', true);
  }

  approve(): void {
    this.run(this.api.approve(+this.id), 'সদস্য অনুমোদিত হয়েছে');
  }

  reject(): void {
    const reason = prompt('বাতিল করার কারণ (ঐচ্ছিক):', '');
    if (reason === null) return;
    this.run(this.api.reject(+this.id, reason.trim() || null), 'সদস্যের তথ্য বাতিল করা হয়েছে');
  }

  remove(): void {
    if (!confirm('এই সদস্যের তথ্য স্থায়ীভাবে মুছে ফেলতে চান? এটি আর ফেরত আনা যাবে না।')) return;
    this.busy = true;
    this.api.deleteMember(+this.id).subscribe({
      next: () => this.router.navigate(['/admin/members']),
      error: (err: HttpErrorResponse) => {
        this.busy = false;
        this.error = errorMessage(err);
      },
    });
  }

  private run(request: ReturnType<ApiService['approve']>, message: string, fromForm = false): void {
    this.busy = true;
    this.error = '';
    this.notice = '';
    request.subscribe({
      next: (m) => {
        this.busy = false;
        this.notice = message;
        if (fromForm) {
          this.member = m;
        } else if (this.member) {
          // স্ট্যাটাস বদলালে ফরম নতুন করে না বানিয়ে শুধু স্ট্যাটাস তথ্য হালনাগাদ
          Object.assign(this.member, {
            status: m.status, statusLabel: m.statusLabel, rejectReason: m.rejectReason,
            reviewedBy: m.reviewedBy, reviewedAt: m.reviewedAt,
          });
        }
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (err: HttpErrorResponse) => {
        this.busy = false;
        this.error = errorMessage(err);
        if (fromForm) this.formComponent?.showServerErrors(err);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
    });
  }
}

import { HttpErrorResponse } from '@angular/common/http';
import { Component, ViewChild, inject } from '@angular/core';
import { ApiService } from '../core/api.service';
import { errorMessage } from '../core/bn';
import { MemberRequest } from '../core/models';
import { MemberFormComponent } from '../shared/member-form.component';
import { OrgHeaderComponent } from '../shared/org-header.component';

/** পাবলিক লিংক: যে কেউ লগইন ছাড়া সদস্য ফরম পূরণ করতে পারবে */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [MemberFormComponent, OrgHeaderComponent],
  template: `
    <app-org-header subtitle="যুবসংঘ ডিজিটাল - সদস্য ফরম" />

    <main class="container" style="padding-top: 20px; padding-bottom: 32px;">
      @if (successMessage) {
        <div class="card success">
          <div class="tick">✓</div>
          <h2>জাযাকাল্লাহু খাইরান!</h2>
          <p>{{ successMessage }}</p>
          <button class="btn" type="button" (click)="again()">আরেকটি ফরম পূরণ করুন</button>
        </div>
      } @else {
        <div class="alert alert-warning">
          সব তথ্য বাংলায় লিখুন। তারকা (<span style="color: var(--danger)">*</span>) চিহ্নিত ঘরগুলো অবশ্যই পূরণ করতে হবে।
        </div>
        @if (error) { <div class="alert alert-error">{{ error }}</div> }
        <app-member-form [submitting]="submitting" submitLabel="ফরম জমা দিন" (save)="submit($event)" />
      }
    </main>

    <footer class="muted" style="text-align: center; padding: 0 16px 24px; font-size: .9rem;">
      বাংলাদেশ আহলেহাদীছ যুবসংঘ
    </footer>
  `,
  styles: `
    .success { text-align: center; padding: 40px 20px; }
    .success h2 { color: var(--primary-dark); margin: 10px 0; }
    .success p { margin: 0 0 20px; }
    .tick {
      width: 64px; height: 64px; margin: 0 auto; border-radius: 50%;
      display: grid; place-items: center; font-size: 2rem;
      background: var(--success-soft); color: var(--success);
    }
  `,
})
export class RegisterComponent {
  @ViewChild(MemberFormComponent) formComponent?: MemberFormComponent;
  private api = inject(ApiService);

  submitting = false;
  error = '';
  successMessage = '';

  submit(req: MemberRequest): void {
    this.submitting = true;
    this.error = '';
    this.api.submitMember(req).subscribe({
      next: (res) => {
        this.submitting = false;
        this.successMessage = res.message;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (err: HttpErrorResponse) => {
        this.submitting = false;
        this.error = errorMessage(err);
        this.formComponent?.showServerErrors(err);
      },
    });
  }

  again(): void {
    this.successMessage = '';
    window.scrollTo({ top: 0 });
  }
}

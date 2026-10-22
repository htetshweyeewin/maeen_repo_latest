import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { errorMessage } from '../core/bn';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [FormsModule],
  template: `
    <form class="card" style="max-width: 440px;" (ngSubmit)="save()">
      <h2 class="section-title">পাসওয়ার্ড পরিবর্তন</h2>
      @if (error) { <div class="alert alert-error">{{ error }}</div> }
      @if (done) { <div class="alert alert-success">পাসওয়ার্ড পরিবর্তন হয়েছে</div> }
      <div class="field mb">
        <label>বর্তমান পাসওয়ার্ড</label>
        <input type="password" name="current" [(ngModel)]="current" autocomplete="current-password" required>
      </div>
      <div class="field mb">
        <label>নতুন পাসওয়ার্ড</label>
        <input type="password" name="next" [(ngModel)]="next" autocomplete="new-password" required>
        <span class="hint">কমপক্ষে ৬ অক্ষর</span>
      </div>
      <div class="field mb">
        <label>নতুন পাসওয়ার্ড আবার লিখুন</label>
        <input type="password" name="confirm" [(ngModel)]="confirm" autocomplete="new-password" required>
      </div>
      <button class="btn" type="submit" [disabled]="busy || !current || !next">পরিবর্তন করুন</button>
    </form>
  `,
})
export class ChangePasswordComponent {
  private api = inject(ApiService);
  current = '';
  next = '';
  confirm = '';
  busy = false;
  done = false;
  error = '';

  save(): void {
    this.done = false;
    if (this.next.length < 6) {
      this.error = 'নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে';
      return;
    }
    if (this.next !== this.confirm) {
      this.error = 'নতুন পাসওয়ার্ড দুটি মেলেনি';
      return;
    }
    this.busy = true;
    this.error = '';
    this.api.changePassword(this.current, this.next).subscribe({
      next: () => {
        this.busy = false;
        this.done = true;
        this.current = this.next = this.confirm = '';
      },
      error: (err: HttpErrorResponse) => {
        this.busy = false;
        const fieldErrors = Object.values(err.error?.errors ?? {}) as string[];
        this.error = fieldErrors[0] ?? errorMessage(err);
      },
    });
  }
}

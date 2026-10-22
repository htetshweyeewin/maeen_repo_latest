import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { BnPipe, errorMessage } from '../core/bn';
import { GeoOption, OrgZila, UpazilaRef } from '../core/models';

/**
 * সাংগঠনিক যেলা ব্যবস্থাপনা (কেন্দ্রীয় এডমিন)।
 * প্রতিটি যেলার অধীনে কোন কোন উপযেলা তা নির্ধারণ করলে সদস্যরা স্বয়ংক্রিয়ভাবে সেই যেলায় যুক্ত হয়।
 */
@Component({
  selector: 'app-zilas',
  standalone: true,
  imports: [FormsModule, BnPipe],
  template: `
    <h1 class="mb" style="font-size: 1.4rem;">সাংগঠনিক যেলা</h1>
    @if (error) { <div class="alert alert-error">{{ error }}</div> }

    <div class="layout">
      <div class="table-wrap">
        <table class="table">
          <thead><tr><th>যেলা</th><th>অধীনস্থ উপযেলা</th><th>সদস্য</th><th></th></tr></thead>
          <tbody>
            @for (z of zilas; track z.id) {
              <tr>
                <td><strong>{{ z.name }}</strong></td>
                <td>
                  @for (u of z.upazilas; track u.id) { <span class="chip">{{ u.name }}</span> }
                  @empty { <span class="muted">—</span> }
                </td>
                <td>{{ z.memberCount | bn }}</td>
                <td>
                  <div class="actions">
                    <button type="button" class="btn btn-sm btn-outline" (click)="edit(z)">সম্পাদনা</button>
                    <button type="button" class="btn btn-sm btn-danger" (click)="remove(z)">মুছুন</button>
                  </div>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="4" class="muted" style="text-align: center; padding: 24px;">কোনো যেলা নেই</td></tr>
            }
          </tbody>
        </table>
      </div>

      <form class="card" (ngSubmit)="save()">
        <h2 class="section-title">{{ editingId ? 'যেলা সম্পাদনা' : 'নতুন যেলা' }}</h2>
        <div class="field mb">
          <label>যেলার নাম<span class="req">*</span></label>
          <input type="text" name="name" [(ngModel)]="name" placeholder="যেমন: ময়মনসিংহ দক্ষিণ" required>
        </div>

        <div class="field mb">
          <label>অধীনস্থ উপযেলা</label>
          <div class="row">
            <select name="division" [(ngModel)]="divisionId" (ngModelChange)="loadDistricts()" style="flex: 1 1 140px;">
              <option [ngValue]="null">বিভাগ</option>
              @for (d of divisions; track d.id) { <option [ngValue]="d.id">{{ d.name }}</option> }
            </select>
            <select name="district" [(ngModel)]="districtId" (ngModelChange)="loadUpazilas()" style="flex: 1 1 140px;">
              <option [ngValue]="null">যেলা</option>
              @for (d of districts; track d.id) { <option [ngValue]="d.id">{{ d.name }}</option> }
            </select>
          </div>
          @if (upazilas.length) {
            <div class="pick">
              @for (u of upazilas; track u.id) {
                <label class="check" [class.muted]="takenBy(u.id)">
                  <input type="checkbox" [checked]="isSelected(u.id)" [disabled]="!!takenBy(u.id)" (change)="toggle(u)">
                  {{ u.name }} @if (takenBy(u.id); as t) { <small>({{ t }})</small> }
                </label>
              }
            </div>
          }
          <div class="mt">
            @for (u of selected; track u.id) {
              <span class="chip">{{ u.name }} <button type="button" (click)="unselect(u.id)" aria-label="সরান">×</button></span>
            } @empty { <span class="muted small">কোনো উপযেলা নির্বাচন করা হয়নি</span> }
          </div>
        </div>

        <div class="row">
          <button class="btn" type="submit" [disabled]="busy || !name.trim()">সংরক্ষণ করুন</button>
          @if (editingId) { <button class="btn btn-outline" type="button" (click)="resetForm()">বাতিল</button> }
        </div>
      </form>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr); gap: 18px; align-items: start; }
    @media (max-width: 900px) { .layout { grid-template-columns: minmax(0, 1fr); } }
    .chip { display: inline-flex; align-items: center; gap: 4px; background: var(--primary-soft); color: var(--primary-dark);
            border-radius: 999px; padding: 1px 10px; margin: 2px; font-size: .88rem; }
    .chip button { border: 0; background: none; cursor: pointer; color: inherit; font-size: 1rem; padding: 0; }
    .pick { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 6px; margin-top: 10px;
            padding: 10px; border: 1px solid var(--border-soft); border-radius: 8px; max-height: 260px; overflow: auto; }
    .small { font-size: .88rem; }
  `,
})
export class ZilasComponent implements OnInit {
  private api = inject(ApiService);

  zilas: OrgZila[] = [];
  error = '';
  busy = false;

  editingId: number | null = null;
  name = '';
  selected: UpazilaRef[] = [];

  divisions: GeoOption[] = [];
  districts: GeoOption[] = [];
  upazilas: GeoOption[] = [];
  divisionId: number | null = null;
  districtId: number | null = null;
  private districtName = '';

  ngOnInit(): void {
    this.load();
    this.api.geo().subscribe((d) => (this.divisions = d));
  }

  load(): void {
    this.api.zilas().subscribe({
      next: (z) => (this.zilas = z),
      error: (err: HttpErrorResponse) => (this.error = errorMessage(err)),
    });
  }

  loadDistricts(): void {
    this.districts = [];
    this.upazilas = [];
    this.districtId = null;
    if (this.divisionId) this.api.geo(this.divisionId).subscribe((d) => (this.districts = d));
  }

  loadUpazilas(): void {
    this.upazilas = [];
    this.districtName = this.districts.find((d) => d.id === this.districtId)?.name ?? '';
    if (this.districtId) this.api.geo(this.districtId).subscribe((d) => (this.upazilas = d));
  }

  /** অন্য যেলার অধীনে থাকলে সেই যেলার নাম */
  takenBy(upazilaId: number): string | null {
    const z = this.zilas.find((x) => x.id !== this.editingId && x.upazilas.some((u) => u.id === upazilaId));
    return z?.name ?? null;
  }

  isSelected(id: number): boolean {
    return this.selected.some((u) => u.id === id);
  }

  toggle(u: GeoOption): void {
    if (this.isSelected(u.id)) this.unselect(u.id);
    else this.selected = [...this.selected, { id: u.id, name: u.name, districtName: this.districtName }];
  }

  unselect(id: number): void {
    this.selected = this.selected.filter((u) => u.id !== id);
  }

  edit(z: OrgZila): void {
    this.editingId = z.id;
    this.name = z.name;
    this.selected = [...z.upazilas];
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  resetForm(): void {
    this.editingId = null;
    this.name = '';
    this.selected = [];
  }

  save(): void {
    this.busy = true;
    this.error = '';
    this.api.saveZila(this.editingId, { name: this.name.trim(), upazilaIds: this.selected.map((u) => u.id) }).subscribe({
      next: () => {
        this.busy = false;
        this.resetForm();
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.busy = false;
        this.error = errorMessage(err);
      },
    });
  }

  remove(z: OrgZila): void {
    if (!confirm(`"${z.name}" যেলা মুছে ফেলতে চান? এর সদস্যরা "যেলা নির্ধারিত হয়নি" তালিকায় চলে যাবে।`)) return;
    this.api.deleteZila(z.id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => (this.error = errorMessage(err)),
    });
  }
}

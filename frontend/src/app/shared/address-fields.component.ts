import { Component, DestroyRef, Input, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { BnPipe, errorOf } from '../core/bn';
import { Address, GeoOption } from '../core/models';

/** ঠিকানার ফর্ম গ্রুপ তৈরি (স্থায়ী ও বর্তমান ঠিকানা উভয়ের জন্য) */
export function addressGroup(fb: FormBuilder, a?: Address | null): FormGroup {
  return fb.group({
    divisionId: [a?.divisionId ?? null, Validators.required],
    districtId: [a?.districtId ?? null, Validators.required],
    upazilaId: [a?.upazilaId ?? null, Validators.required],
    unionId: [a?.unionId ?? null],
    ward: [a?.ward ?? null, Validators.required],
    postOffice: [a?.postOffice ?? '', Validators.maxLength(120)],
    village: [a?.village ?? '', [Validators.required, Validators.maxLength(255)]],
  });
}

/**
 * বিভাগ → যেলা → উপযেলা → ইউনিয়ন → ওয়ার্ড ক্রমানুসারে ড্রপডাউন।
 * উপরের ঘর বদলালে নিচের ঘরগুলো খালি হয়ে নতুন তালিকা লোড হয়।
 */
@Component({
  selector: 'app-address-fields',
  standalone: true,
  imports: [ReactiveFormsModule, BnPipe],
  template: `
    <div class="grid" [formGroup]="group">
      <div class="field">
        <label>বিভাগ<span class="req">*</span></label>
        <select formControlName="divisionId">
          <option [ngValue]="null">-- বিভাগ নির্বাচন করুন --</option>
          @for (o of divisions; track o.id) { <option [ngValue]="o.id">{{ o.name }}</option> }
        </select>
        @if (err('divisionId'); as e) { <span class="error">{{ e }}</span> }
      </div>

      <div class="field">
        <label>যেলা<span class="req">*</span></label>
        <select formControlName="districtId">
          <option [ngValue]="null">-- যেলা নির্বাচন করুন --</option>
          @for (o of districts; track o.id) { <option [ngValue]="o.id">{{ o.name }}</option> }
        </select>
        @if (err('districtId'); as e) { <span class="error">{{ e }}</span> }
      </div>

      <div class="field">
        <label>উপযেলা<span class="req">*</span></label>
        <select formControlName="upazilaId">
          <option [ngValue]="null">-- উপযেলা নির্বাচন করুন --</option>
          @for (o of upazilas; track o.id) { <option [ngValue]="o.id">{{ o.name }}</option> }
        </select>
        @if (err('upazilaId'); as e) { <span class="error">{{ e }}</span> }
      </div>

      <div class="field">
        <label>ইউনিয়ন @if (unions.length) { <span class="req">*</span> }</label>
        <select formControlName="unionId">
          <option [ngValue]="null">{{ group.value.upazilaId && !unions.length ? '-- প্রযোজ্য নয় --' : '-- ইউনিয়ন নির্বাচন করুন --' }}</option>
          @for (o of unions; track o.id) { <option [ngValue]="o.id">{{ o.name }}</option> }
        </select>
        @if (err('unionId'); as e) { <span class="error">{{ e }}</span> }
      </div>

      <div class="field">
        <label>ওয়ার্ড<span class="req">*</span></label>
        <select formControlName="ward">
          <option [ngValue]="null">-- ওয়ার্ড নির্বাচন করুন --</option>
          @for (w of wards; track w) { <option [ngValue]="w">{{ w | bn }} নং ওয়ার্ড</option> }
        </select>
        @if (err('ward'); as e) { <span class="error">{{ e }}</span> }
      </div>

      <div class="field">
        <label>পোস্ট</label>
        <input type="text" formControlName="postOffice" placeholder="পোস্ট অফিসের নাম">
        @if (err('postOffice'); as e) { <span class="error">{{ e }}</span> }
      </div>

      <div class="field span-2">
        <label>গ্রাম/পাড়া/মহল্লা<span class="req">*</span></label>
        <input type="text" formControlName="village" placeholder="গ্রাম/পাড়া/মহল্লার নাম">
        @if (err('village'); as e) { <span class="error">{{ e }}</span> }
      </div>
    </div>
  `,
})
export class AddressFieldsComponent implements OnInit {
  @Input({ required: true }) group!: FormGroup;

  private api = inject(ApiService);
  private destroyRef = inject(DestroyRef);

  readonly wards = [1, 2, 3, 4, 5, 6, 7, 8, 9];
  divisions: GeoOption[] = [];
  districts: GeoOption[] = [];
  upazilas: GeoOption[] = [];
  unions: GeoOption[] = [];

  ngOnInit(): void {
    const c = this.group.controls;
    this.api.geo().subscribe((d) => (this.divisions = d));

    // সম্পাদনার সময় আগের নির্বাচন অনুযায়ী তালিকা লোড
    this.load(c['divisionId'].value, (d) => (this.districts = d));
    this.load(c['districtId'].value, (d) => (this.upazilas = d));
    this.loadUnions(c['upazilaId'].value);

    this.cascade('divisionId', 'districtId', (d) => (this.districts = d));
    this.cascade('districtId', 'upazilaId', (d) => (this.upazilas = d));
    c['upazilaId'].valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((id) => {
      c['unionId'].setValue(null);
      this.loadUnions(id);
    });
  }

  err(name: string): string | null {
    return errorOf(this.group.get(name));
  }

  private cascade(parent: string, child: string, setList: (d: GeoOption[]) => void): void {
    this.group.controls[parent].valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((id) => {
      setList([]);
      this.group.controls[child].setValue(null);
      this.load(id, setList);
    });
  }

  private load(parentId: number | null, setList: (d: GeoOption[]) => void): void {
    if (parentId) this.api.geo(parentId).subscribe(setList);
  }

  /** যে উপযেলায় ইউনিয়ন আছে সেখানে ইউনিয়ন আবশ্যক */
  private loadUnions(upazilaId: number | null): void {
    this.unions = [];
    const ctrl = this.group.controls['unionId'];
    ctrl.clearValidators();
    ctrl.updateValueAndValidity({ emitEvent: false });
    if (!upazilaId) return;
    this.api.geo(upazilaId).subscribe((d) => {
      this.unions = d;
      if (d.length) ctrl.setValidators(Validators.required);
      ctrl.updateValueAndValidity({ emitEvent: false });
    });
  }
}

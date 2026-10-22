import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { applyServerErrors, errorOf, mobileValidator, scrollToFirstInvalid } from '../core/bn';
import { DesignationOption, Member, MemberRequest, Meta, OrgZila } from '../core/models';
import { AddressFieldsComponent, addressGroup } from './address-fields.component';

/** সদস্য ফরম - পাবলিক লিংক ও এডমিন সম্পাদনা উভয় জায়গায় ব্যবহৃত */
@Component({
  selector: 'app-member-form',
  standalone: true,
  imports: [ReactiveFormsModule, AddressFieldsComponent],
  templateUrl: './member-form.component.html',
})
export class MemberFormComponent implements OnInit {
  @Input() initial: Member | null = null;
  /** কেন্দ্রীয় এডমিনের জন্য সাংগঠনিক যেলা নির্বাচনের ঘর দেখাবে */
  @Input() zilas: OrgZila[] | null = null;
  @Input() submitting = false;
  @Input() submitLabel = 'জমা দিন';
  @Output() save = new EventEmitter<MemberRequest>();

  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private destroyRef = inject(DestroyRef);

  readonly today = new Date().toISOString().slice(0, 10);
  readonly educationSuggestions = [
    'দাখিল', 'আলিম', 'ফাজিল', 'কামিল', 'এস.এস.সি', 'এইচ.এস.সি', 'ডিপ্লোমা', 'স্নাতক (অনার্স)', 'স্নাতক (পাস)',
    'স্নাতকোত্তর', 'দাওরায়ে হাদীছ', 'অধ্যয়নরত',
  ];

  meta: Meta | null = null;
  designations: DesignationOption[] = [];
  form!: FormGroup;

  ngOnInit(): void {
    const m = this.initial;
    this.form = this.fb.group({
      name: [m?.name ?? '', [Validators.required, Validators.maxLength(120)]],
      fatherName: [m?.fatherName ?? '', [Validators.required, Validators.maxLength(120)]],
      motherName: [m?.motherName ?? '', [Validators.required, Validators.maxLength(120)]],
      dateOfBirth: [m?.dateOfBirth ?? null, Validators.required],
      bloodGroup: [m?.bloodGroup ?? null],
      education: [m?.education ?? '', Validators.maxLength(200)],
      email: [m?.email ?? '', [Validators.email, Validators.maxLength(150)]],
      mobile1: [m?.mobile1 ?? '', [Validators.required, mobileValidator]],
      mobile1Call: [m ? m.mobile1Call : true],
      mobile1WhatsApp: [m?.mobile1WhatsApp ?? false],
      mobile2: [m?.mobile2 ?? '', mobileValidator],
      mobile2Call: [m?.mobile2Call ?? false],
      mobile2WhatsApp: [m?.mobile2WhatsApp ?? false],
      permanentAddress: addressGroup(this.fb, m?.permanentAddress),
      presentSameAsPermanent: [m?.presentSameAsPermanent ?? false],
      presentAddress: addressGroup(this.fb, m && !m.presentSameAsPermanent ? m.presentAddress : null),
      responsibilityLevel: [m?.responsibilityLevel ?? null],
      designation: [m?.designation ?? null],
      orgRank: [m?.orgRank ?? null, Validators.required],
      oathDate: [m?.oathDate ?? null],
      orgZilaId: [m?.orgZilaManual ? (m.orgZilaId ?? 0) : null],
    });

    this.togglePresent(this.form.value.presentSameAsPermanent);
    this.form.controls['presentSameAsPermanent'].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((same: boolean) => this.togglePresent(same));

    this.form.controls['responsibilityLevel'].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.updateDesignations());

    this.api.meta().subscribe((meta) => {
      this.meta = meta;
      this.updateDesignations();
    });
  }

  get permanentGroup(): FormGroup {
    return this.form.controls['permanentAddress'] as FormGroup;
  }

  get presentGroup(): FormGroup {
    return this.form.controls['presentAddress'] as FormGroup;
  }

  err(path: string): string | null {
    return errorOf(this.form.get(path));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      scrollToFirstInvalid();
      return;
    }
    const v = this.form.getRawValue();
    const blank = (s: string | null) => (s && s.trim() ? s.trim() : null);
    const request: MemberRequest = {
      name: v.name.trim(),
      fatherName: v.fatherName.trim(),
      motherName: v.motherName.trim(),
      dateOfBirth: v.dateOfBirth,
      bloodGroup: v.bloodGroup,
      education: blank(v.education),
      email: blank(v.email),
      mobile1: v.mobile1,
      mobile1Call: v.mobile1Call,
      mobile1WhatsApp: v.mobile1WhatsApp,
      mobile2: blank(v.mobile2),
      mobile2Call: v.mobile2Call,
      mobile2WhatsApp: v.mobile2WhatsApp,
      permanentAddress: v.permanentAddress,
      presentSameAsPermanent: v.presentSameAsPermanent,
      presentAddress: v.presentSameAsPermanent ? null : v.presentAddress,
      responsibilityLevel: v.responsibilityLevel,
      designation: v.responsibilityLevel ? v.designation : null,
      orgRank: v.orgRank,
      oathDate: v.oathDate || null,
    };
    if (this.zilas) request.orgZilaId = v.orgZilaId;
    this.save.emit(request);
  }

  /** সার্ভার থেকে আসা ফিল্ড ত্রুটি দেখানো */
  showServerErrors(err: HttpErrorResponse): void {
    applyServerErrors(this.form, err);
    scrollToFirstInvalid();
  }

  private togglePresent(same: boolean): void {
    if (same) this.presentGroup.disable({ emitEvent: false });
    else this.presentGroup.enable({ emitEvent: false });
  }

  /** নির্বাচিত স্তরে যে পদগুলো আছে শুধু সেগুলো দেখানো (ফরমের ১১ নং টেবিল অনুযায়ী) */
  private updateDesignations(): void {
    const level = this.form.value.responsibilityLevel as string | null;
    const designation = this.form.controls['designation'];
    this.designations = level && this.meta ? this.meta.designations.filter((d) => d.levels.includes(level)) : [];
    if (!level) {
      designation.setValue(null, { emitEvent: false });
      designation.clearValidators();
    } else {
      if (this.meta && designation.value && !this.designations.some((d) => d.code === designation.value)) {
        designation.setValue(null, { emitEvent: false });
      }
      designation.setValidators(Validators.required);
    }
    designation.updateValueAndValidity({ emitEvent: false });
  }
}

import { HttpErrorResponse } from '@angular/common/http';
import { Pipe, PipeTransform } from '@angular/core';
import { AbstractControl, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';

const BN_DIGITS = '০১২৩৪৫৬৭৮৯';

export function toBnDigits(value: unknown): string {
  if (value === null || value === undefined) return '';
  return String(value).replace(/[0-9]/g, (d) => BN_DIGITS[+d]);
}

export function toAsciiDigits(value: string): string {
  return value.replace(/[০-৯]/g, (d) => String(BN_DIGITS.indexOf(d)));
}

/** ইংরেজি অঙ্ককে বাংলা অঙ্কে দেখায়: {{ 123 | bn }} → ১২৩ */
@Pipe({ name: 'bn', standalone: true })
export class BnPipe implements PipeTransform {
  transform(value: unknown): string {
    return toBnDigits(value);
  }
}

/** 2026-10-22 বা ISO সময় → ২২-১০-২০২৬ (time=true হলে সময়সহ) */
@Pipe({ name: 'bnDate', standalone: true })
export class BnDatePipe implements PipeTransform {
  transform(value: string | null | undefined, time = false): string {
    if (!value) return '';
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      const [y, m, d] = value.split('-');
      return toBnDigits(`${d}-${m}-${y}`);
    }
    const dt = new Date(value);
    const pad = (n: number) => String(n).padStart(2, '0');
    let s = `${pad(dt.getDate())}-${pad(dt.getMonth() + 1)}-${dt.getFullYear()}`;
    if (time) s += ` ${pad(dt.getHours())}:${pad(dt.getMinutes())}`;
    return toBnDigits(s);
  }
}

/** বাংলা/ইংরেজি অঙ্ক, স্পেস, ড্যাশ, +88 সহ মোবাইল নম্বর → 01XXXXXXXXX অথবা null */
export function normalizeMobile(raw: string | null | undefined): string | null {
  if (!raw || !raw.trim()) return null;
  let digits = toAsciiDigits(raw).replace(/\D/g, '');
  if (digits.startsWith('880')) digits = digits.substring(2);
  return digits;
}

export const mobileValidator: ValidatorFn = (c: AbstractControl): ValidationErrors | null => {
  const n = normalizeMobile(c.value);
  if (n === null) return null; // আবশ্যকতা Validators.required দেখবে
  return /^01[3-9]\d{8}$/.test(n) ? null : { mobile: true };
};

const MESSAGES: Record<string, string> = {
  required: 'এই ঘরটি পূরণ করুন',
  email: 'সঠিক ই-মেইল দিন',
  mobile: 'সঠিক মোবাইল নম্বর দিন (যেমন: ০১৭XXXXXXXX)',
  maxlength: 'লেখা অনেক বড় হয়ে গেছে',
  minlength: 'লেখা অনেক ছোট',
};

/** কন্ট্রোলের প্রথম ত্রুটির বাংলা বার্তা (touched হলে) */
export function errorOf(control: AbstractControl | null): string | null {
  if (!control || !control.errors || !(control.touched || control.dirty)) return null;
  const key = Object.keys(control.errors)[0];
  if (key === 'server') return control.errors['server'];
  return MESSAGES[key] ?? 'তথ্য সঠিক নয়';
}

/** সার্ভারের ফিল্ড-ভিত্তিক ত্রুটি ("permanentAddress.unionId") ফরমের কন্ট্রোলে বসানো */
export function applyServerErrors(form: FormGroup, err: HttpErrorResponse): void {
  const errors: Record<string, string> = err.error?.errors ?? {};
  for (const [path, message] of Object.entries(errors)) {
    const control = form.get(path);
    if (control) {
      control.setErrors({ server: message });
      control.markAsTouched();
    }
  }
}

export function errorMessage(err: HttpErrorResponse): string {
  if (err.status === 0) return 'সার্ভারের সাথে সংযোগ করা যাচ্ছে না। ইন্টারনেট সংযোগ পরীক্ষা করুন।';
  return err.error?.message ?? 'কিছু একটা সমস্যা হয়েছে, আবার চেষ্টা করুন';
}

export function scrollToFirstInvalid(): void {
  setTimeout(() => {
    const el = document.querySelector('form .ng-invalid:not(form):not(div)') as HTMLElement | null;
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    el?.focus({ preventScroll: true });
  });
}

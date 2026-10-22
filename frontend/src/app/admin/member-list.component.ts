import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, filter, forkJoin, interval, merge, of, startWith, switchMap } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { BnDatePipe, BnPipe, errorMessage } from '../core/bn';
import { Member, MemberQuery, MemberStatus, Meta, OrgZila, Page, Stats } from '../core/models';

const LIVE_INTERVAL_MS = 15000;

/** সদস্য তালিকা - যেলা এডমিন নিজের যেলার, কেন্দ্রীয় এডমিন সবার লাইভ তালিকা দেখবে */
@Component({
  selector: 'app-member-list',
  standalone: true,
  imports: [FormsModule, RouterLink, BnPipe, BnDatePipe],
  templateUrl: './member-list.component.html',
  styleUrl: './member-list.component.css',
})
export class MemberListComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(ApiService);
  private destroyRef = inject(DestroyRef);

  query: Required<Pick<MemberQuery, 'q' | 'status' | 'orgRank' | 'level'>> & { orgZilaId: number | null } = {
    q: '', status: '', orgZilaId: null, orgRank: '', level: '',
  };
  page = 0;
  readonly size = 25;

  data: Page<Member> | null = null;
  stats: Stats | null = null;
  zilas: OrgZila[] = [];
  meta: Meta | null = null;

  live = true;
  loading = false;
  lastUpdated: Date | null = null;
  error = '';
  busyId: number | null = null;

  private refresh$ = new Subject<void>();
  private search$ = new Subject<string>();

  ngOnInit(): void {
    this.api.meta().subscribe((m) => (this.meta = m));
    if (this.auth.isSuperAdmin()) this.api.zilas().subscribe((z) => (this.zilas = z));

    merge(
      this.refresh$.pipe(startWith(undefined)),
      interval(LIVE_INTERVAL_MS).pipe(filter(() => this.live && !document.hidden && this.busyId === null)),
    )
      .pipe(
        switchMap(() => {
          this.loading = true;
          const q = this.currentQuery();
          return forkJoin({ page: this.api.members(q), stats: this.api.stats(q) }).pipe(
            catchError((err: HttpErrorResponse) => {
              this.error = errorMessage(err);
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((res) => {
        this.loading = false;
        if (!res) return;
        this.error = '';
        this.data = res.page;
        this.stats = res.stats;
        this.lastUpdated = new Date();
      });

    this.search$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((q) => {
        this.query.q = q;
        this.reload();
      });
  }

  onSearch(value: string): void {
    this.search$.next(value.trim());
  }

  setStatus(status: MemberStatus | ''): void {
    this.query.status = status;
    this.reload();
  }

  reload(): void {
    this.page = 0;
    this.refresh$.next();
  }

  refresh(): void {
    this.refresh$.next();
  }

  goTo(page: number): void {
    this.page = page;
    this.refresh$.next();
  }

  serial(i: number): number {
    return this.page * this.size + i + 1;
  }

  approve(m: Member): void {
    this.act(m, this.api.approve(m.id));
  }

  reject(m: Member): void {
    const reason = prompt(`"${m.name}" এর তথ্য বাতিল করার কারণ (ঐচ্ছিক):`, '');
    if (reason === null) return;
    this.act(m, this.api.reject(m.id, reason.trim() || null));
  }

  exportCsv(): void {
    this.api.exportCsv(this.currentQuery()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `sodosso-talika-${new Date().toISOString().slice(0, 10)}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err: HttpErrorResponse) => (this.error = errorMessage(err)),
    });
  }

  whatsAppLink(mobile: string): string {
    return `https://wa.me/88${mobile}`;
  }

  private act(m: Member, request: ReturnType<ApiService['approve']>): void {
    this.busyId = m.id;
    request.subscribe({
      next: () => {
        this.busyId = null;
        this.refresh$.next();
      },
      error: (err: HttpErrorResponse) => {
        this.busyId = null;
        this.error = errorMessage(err);
      },
    });
  }

  private currentQuery(): MemberQuery {
    return { ...this.query, page: this.page, size: this.size };
  }
}

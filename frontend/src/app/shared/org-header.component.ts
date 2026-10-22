import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-org-header',
  standalone: true,
  template: `
    <header class="org-header">
      <div class="bismillah" lang="ar">بِسْمِ ٱللَّٰهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>
      <h1>বাংলাদেশ আহলেহাদীছ যুবসংঘ</h1>
      @if (subtitle) { <p>{{ subtitle }}</p> }
    </header>
  `,
  styles: `
    .org-header {
      text-align: center;
      color: #fff;
      padding: 22px 16px 26px;
      background: linear-gradient(135deg, var(--primary-dark), var(--primary));
      border-bottom: 4px solid var(--accent);
    }
    .bismillah { font-size: 1.15rem; opacity: .9; margin-bottom: 4px; }
    h1 { font-size: clamp(1.4rem, 4.5vw, 2rem); font-weight: 700; }
    p { margin: 6px 0 0; font-size: 1.05rem; opacity: .95; }
  `,
})
export class OrgHeaderComponent {
  @Input() subtitle = '';
}

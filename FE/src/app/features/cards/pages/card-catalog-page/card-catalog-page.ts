import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-card-catalog-page',
  template: `
    <main class="card-catalog-page">
      <h2>Card Catalog</h2>
      <p>Search and browse cards</p>
    </main>
  `,
  styles: [`
    :host { display: block; }
    .card-catalog-page { padding: 1.5rem; }
    h2 { margin: 0 0 0.5rem; }
    p { margin: 0; color: #4b5563; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardCatalogPage {}

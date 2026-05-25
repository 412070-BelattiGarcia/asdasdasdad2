import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-deck-builder-page',
  template: `
    <main class="deck-builder-page">
      <h2>Deck Builder</h2>
      <p>Build your deck here</p>
    </main>
  `,
  styles: [`
    :host { display: block; }
    .deck-builder-page { padding: 1.5rem; }
    h2 { margin: 0 0 0.5rem; }
    p { margin: 0; color: #4b5563; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeckBuilderPage {}

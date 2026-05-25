import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-deck-list-page',
  template: `
    <main class="deck-list-page">
      <h2>Deck List</h2>
      <p>Your decks will appear here</p>
    </main>
  `,
  styles: [`
    :host { display: block; }
    .deck-list-page { padding: 1.5rem; }
    h2 { margin: 0 0 0.5rem; }
    p { margin: 0; color: #4b5563; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeckListPage {}

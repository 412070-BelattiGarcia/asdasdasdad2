import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-match-page',
  template: `
    <main class="match-page">
      <h2>Match</h2>
      <p>Game board will appear here</p>
    </main>
  `,
  styles: [`
    :host { display: block; }
    .match-page { padding: 1.5rem; }
    h2 { margin: 0 0 0.5rem; }
    p { margin: 0; color: #4b5563; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MatchPage {}

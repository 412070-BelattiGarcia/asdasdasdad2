import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-lobby-page',
  template: `
    <main class="lobby-page">
      <h2>Lobby</h2>
      <p>Create or join a match</p>
    </main>
  `,
  styles: [`
    :host { display: block; }
    .lobby-page { padding: 1.5rem; }
    h2 { margin: 0 0 0.5rem; }
    p { margin: 0; color: #4b5563; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LobbyPage {}

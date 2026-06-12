import { ChangeDetectionStrategy, Component, effect, ElementRef, input, viewChild } from '@angular/core';
import { GameEventDto } from '../../../../shared/models/game-action.models';

@Component({
  selector: 'app-game-log',
  standalone: true,
  template: `
    <div class="game-log" #logContainer>
      @if (events().length === 0) {
        <p class="empty">No hay eventos aún</p>
      } @else {
        @for (event of displayedEvents; track $index) {
          <div class="event-entry">{{ event.message }}</div>
        }
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .game-log {
      max-height: 200px; overflow-y: auto;
      background: #0f172a; color: #cbd5e1;
      padding: 0.75rem; border-radius: 0.5rem;
      font-size: 0.8125rem;
    }
    .empty { color: #64748b; text-align: center; padding: 1rem; margin: 0; }
    .event-entry {
      padding: 0.25rem 0; border-bottom: 1px solid #1e293b;
    }
    .event-entry:last-child { border-bottom: none; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameLogComponent {
  readonly events = input<GameEventDto[]>([]);
  private readonly logContainer = viewChild<ElementRef<HTMLElement>>('logContainer');

  constructor() {
    effect(() => {
      this.events();
      this.logContainer()?.nativeElement.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  get displayedEvents(): GameEventDto[] {
    return this.events().slice(-10).reverse();
  }
}

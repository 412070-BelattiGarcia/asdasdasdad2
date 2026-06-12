import { ChangeDetectionStrategy, Component, inject, OnInit, output, signal } from '@angular/core';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { MatchApiService, MatchResponse } from '../../../../core/api/match-api.service';

@Component({
  selector: 'app-match-list',
  standalone: true,
  imports: [ButtonComponent],
  template: `
    <div class="match-list">
      <div class="match-list-header">
        <h3>Partidas disponibles</h3>
        <app-button variant="ghost" (click)="onRefresh()">↻ Actualizar</app-button>
      </div>

      @if (matches().length === 0) {
        <p class="empty">No hay partidas disponibles.</p>
      } @else {
        <div class="match-list-items">
          @for (match of matches(); track match.id) {
            <div class="match-row">
              <span class="match-host">{{ match.hostName }}</span>
              <span class="match-status">Esperando jugador</span>
              <app-button variant="secondary" (click)="matchSelected.emit(match.id)">
                Usar este
              </app-button>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .match-list { padding: 1rem; border: 1px solid #e5e7eb; border-radius: 0.5rem; }
    .match-list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    h3 { margin: 0; font-size: 1.125rem; }
    .empty { color: #6b7280; text-align: center; padding: 2rem 0; margin: 0; }
    .match-row { display: flex; align-items: center; gap: 1rem; padding: 0.5rem 0; border-bottom: 1px solid #f3f4f6; }
    .match-row:last-child { border-bottom: none; }
    .match-host { font-size: 0.875rem; flex: 1; }
    .match-status { color: #6b7280; font-size: 0.875rem; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MatchListComponent implements OnInit {
  private readonly matchApi = inject(MatchApiService);

  readonly matchSelected = output<string>();
  readonly matches = signal<{ id: string; status: string; hostName: string }[]>([]);

  ngOnInit(): void {
    this.loadMatches();
  }

  onRefresh(): void {
    this.loadMatches();
  }

  private loadMatches(): void {
    this.matchApi.listMatches().subscribe({
      next: (response: MatchResponse[]) => {
        this.matches.set(
          response.map((m: MatchResponse) => ({
            id: m.id,
            status: m.status,
            hostName: m.players?.[0]?.displayName ?? 'Unknown',
          }))
        );
      },
      error: () => {
        this.matches.set([]);
      },
    });
  }
}

import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RankingEntry } from '../../../../shared/models/ranking.models';
import { RankingApiService } from '../../../../core/api/ranking-api.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-ranking-page',
  imports: [LoadingSpinnerComponent],
  template: `
    <main class="ranking-page">
      <h1 class="title">Ranking de Victorias</h1>

      @if (loading()) {
        <app-loading-spinner />
      } @else {
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Jugador</th>
                <th>Victorias</th>
                <th>Derrotas</th>
                <th>Win Rate</th>
                <th>Racha</th>
                <th>Máx Racha</th>
              </tr>
            </thead>
            <tbody>
              @if (error(); as msg) {
                <tr>
                  <td class="empty-row" colspan="7">{{ msg }}</td>
                </tr>
              } @else if (entries().length === 0) {
                <tr>
                  <td class="empty-row" colspan="7">Ningún jugador tiene partidas registradas aún.</td>
                </tr>
              } @else {
                @for (entry of entries(); track entry.playerId) {
                  <tr>
                    <td class="rank">{{ entry.rank }}</td>
                    <td class="player">{{ entry.displayName }}</td>
                    <td class="wins">{{ entry.totalWins }}</td>
                    <td class="losses">{{ entry.totalLosses }}</td>
                    <td class="rate">{{ (entry.winRate * 100).toFixed(0) }}%</td>
                    <td class="streak" [class.hot]="entry.currentWinStreak >= 3">
                      {{ entry.currentWinStreak > 0 ? entry.currentWinStreak + ' en racha' : '-' }}
                    </td>
                    <td class="max-streak">{{ entry.maxWinStreak > 0 ? entry.maxWinStreak : '-' }}</td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>
      }
    </main>
  `,
  styles: [`
    :host { display: block; }
    .ranking-page { padding: 1.5rem; max-width: 800px; margin: 0 auto; }
    .title { font-size: 1.5rem; font-weight: 700; margin: 0 0 1.5rem; color: #1e293b; }
    .table-wrapper { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; background: white; border-radius: 0.5rem; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    th { background: #1e293b; color: white; padding: 0.75rem 1rem; text-align: left; font-weight: 600; font-size: 0.875rem; }
    td { padding: 0.75rem 1rem; border-bottom: 1px solid #e2e8f0; font-size: 0.875rem; }
    tr:last-child td { border-bottom: none; }
    tr:hover td { background: #f8fafc; }
    .empty-row { text-align: center; color: #64748b; padding: 2rem 1rem; font-style: italic; }
    .rank { font-weight: 700; color: #64748b; width: 3rem; }
    .player { font-weight: 600; color: #1e293b; }
    .wins { color: #16a34a; font-weight: 600; }
    .losses { color: #dc2626; }
    .rate { color: #64748b; }
    .streak { font-weight: 600; }
    .streak.hot { color: #ea580c; }
    .max-streak { color: #64748b; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RankingPage {
  private readonly rankingApi = inject(RankingApiService);

  readonly entries = signal<RankingEntry[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.loadRanking();
  }

  private loadRanking(): void {
    this.loading.set(true);
    this.error.set(null);
    this.rankingApi.getRanking().subscribe({
      next: (data: RankingEntry[]) => {
        this.entries.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message ?? 'Error al cargar el ranking.');
        this.loading.set(false);
      },
    });
  }
}

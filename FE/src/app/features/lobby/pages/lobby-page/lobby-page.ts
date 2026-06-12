import { ChangeDetectionStrategy, Component, inject, viewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { MatchStateService } from '../../../match/services/match-state.service';
import { MatchFacadeService } from '../../../match/services/match-facade.service';
import { MatchCreateComponent } from '../../components/match-create/match-create.component';
import { MatchJoinComponent } from '../../components/match-join/match-join.component';
import { MatchListComponent } from '../../components/match-list/match-list.component';

@Component({
  selector: 'app-lobby-page',
  standalone: true,
  imports: [MatchCreateComponent, MatchJoinComponent, MatchListComponent, RouterLink],
  template: `
    <main class="lobby-page">
      @if (!authService.isAuthenticated()) {
        <div class="auth-prompt">
          <p>Iniciá sesión para jugar</p>
          <a routerLink="/auth/register" class="btn-link">Ir a registro</a>
        </div>
      } @else {
        <div class="lobby-grid">
          <div class="lobby-left">
            <app-match-create
              [playerId]="authService.playerId() ?? ''"
              [preSelectedDeckId]="preSelectedDeckId"
              (created)="onMatchCreated($event)"
            />
          </div>
          <div class="lobby-right">
            <app-match-list (matchSelected)="onMatchSelected($event)" />
          </div>
        </div>
        <div class="lobby-bottom">
          <app-match-join
            [playerId]="authService.playerId() ?? ''"
            (joined)="onMatchJoined($event)"
          />
        </div>
      }
    </main>
  `,
  styles: [`
    :host { display: block; }
    .lobby-page { padding: 1.5rem; max-width: 960px; margin: 0 auto; }
    .auth-prompt { text-align: center; padding: 3rem 1rem; }
    .auth-prompt p { font-size: 1.125rem; color: #374151; margin: 0 0 1rem; }
    .btn-link { display: inline-block; padding: 0.5rem 1.5rem; background: #2563eb; color: white; text-decoration: none; border-radius: 0.375rem; }
    .lobby-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .lobby-bottom { }
    @media (max-width: 640px) { .lobby-grid { grid-template-columns: 1fr; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LobbyPage {
  private readonly matchState = inject(MatchStateService);
  private readonly matchFacade = inject(MatchFacadeService);
  private readonly router = inject(Router);

  protected readonly authService = inject(AuthService);
  protected preSelectedDeckId: string | null = null;
  private readonly matchJoin = viewChild(MatchJoinComponent);

  constructor() {
    const params = new URLSearchParams(window.location.search);
    this.preSelectedDeckId = params.get('deckId');
  }

  onMatchCreated(response: { id: string }): void {
    this.matchState.reset();
    this.matchFacade.reset();
    this.router.navigate(['/match', response.id]);
  }

  onMatchJoined(response: { id: string }): void {
    this.matchState.reset();
    this.matchFacade.reset();
    this.router.navigate(['/match', response.id]);
  }

  onMatchSelected(matchId: string): void {
    this.matchJoin()?.setMatchId(matchId);
  }
}

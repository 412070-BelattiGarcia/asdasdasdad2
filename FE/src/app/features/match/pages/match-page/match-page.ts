import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { CardRepositoryService } from '../../../../core/services/card-repository.service';
import { MatchStateService } from '../../services/match-state.service';
import { MatchInteractionService } from '../../services/match-interaction.service';
import { GameActionDispatcherService } from '../../services/game-action-dispatcher.service';
import { MatchHeaderComponent } from '../../components/match-header/match-header.component';
import { PlayerAreaComponent } from '../../components/player-area/player-area.component';
import { OpponentAreaComponent } from '../../components/opponent-area/opponent-area.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { HandZoneComponent } from '../../components/hand-zone/hand-zone.component';
import { ActionPanelComponent } from '../../components/action-panel/action-panel.component';
import { VictoryOverlayComponent } from '../../components/victory-overlay/victory-overlay.component';
import { SetupOverlayComponent } from '../../components/setup-overlay/setup-overlay.component';
import { CardDetailResponse } from '../../../../shared/models/card.models';
import { PublicPokemonSlotModel, PrivateHandCardModel } from '../../../../shared/models/game-state.models';
import { GameActionType, GameEventDto } from '../../../../shared/models/game-action.models';

@Component({
  selector: 'app-match-page',
  imports: [
    DragDropModule,
    MatchHeaderComponent, PlayerAreaComponent, OpponentAreaComponent,
    LoadingSpinnerComponent, HandZoneComponent, ActionPanelComponent, VictoryOverlayComponent,
    SetupOverlayComponent,
  ],
  host: {
    '(document:keydown.escape)': 'onEscapeKey()',
  },
  template: `
    @if (matchState.publicState(); as state) {
      <main class="match-page">
        <app-match-header [publicState]="state" [myPlayerId]="matchState.myPlayerId()" />

        @if (state.status !== 'SETUP') {
          @if (opponentPlayerState(); as opponent) {
            <app-opponent-area
              [playerState]="opponent"
              [validTargets]="selectionState().validTargets"
              [selectionMode]="selectionState().mode"
              (pokemonClicked)="onPokemonClicked($event)"
            />
          } @else {
            <div class="waiting-banner">
              <p>Esperando oponente...</p>
              <app-loading-spinner />
            </div>
          }
        } @else {
          <div class="setup-opponent-placeholder">
            <p>El oponente está preparando su tablero...</p>
          </div>
        }

        @if (myPlayerState(); as me) {
          <app-player-area
            [playerState]="me"
            [privateState]="matchState.privateState()"
            [validTargets]="selectionState().validTargets"
            [selectionMode]="selectionState().mode"
            [dragEnabled]="canDragToBench()"
            [connectedDropListIds]="[handZoneDroplistId]"
            (pokemonClicked)="onPokemonClicked($event)"
            (benchDropped)="onHandToBenchDropped($event)"
            (energyDropped)="onEnergyDropped($event)"
            (evolutionDropped)="onEvolutionDropped($event)"
            (attackSelected)="onActionSelected($event)"
            (retreatSelected)="onRetreatInitiated()"
          />
        }

        @if (state.status !== 'SETUP' && matchState.privateState(); as priv) {
          <app-hand-zone
            [hand]="priv.hand"
            [selectionMode]="selectionState().mode"
            [selectedHandIndex]="selectionState().selectedHandIndex"
            [dragEnabled]="canDragToBench()"
            [connectedDropListIds]="benchSlotDroplistIds()"
            (cardClicked)="onHandCardClicked($event.card, $event.handIndex)"
          />
        }

        @if (state.status !== 'SETUP') {
          <app-action-panel (actionSelected)="onActionSelected($event)" />
        }

        @if (state.status === 'FINISHED') {
          <app-victory-overlay
            [winnerPlayerId]="state.winnerPlayerId ?? null"
            [myPlayerId]="matchState.myPlayerId()"
            (returnToLobby)="onReturnToLobby()"
          />
        }

        @if (coinFlipPhase(); as phase) {
          <div class="coinflip-overlay">
            <div class="coinflip-content">
              @if (phase === 'spinning') {
                <div class="coin-container">
                  <div class="coin" [class.flip]="phase === 'spinning'">
                    <div class="coin-face front">P</div>
                    <div class="coin-face back">T</div>
                  </div>
                </div>
                <span class="coinflip-hint">Lanzando moneda...</span>
              } @else {
                <div class="coin-container">
                  <div class="coin result">
                    <div class="coin-face front">{{ coinFlipWinnerIsMe() ? 'T' : 'P' }}</div>
                  </div>
                </div>
                <span class="coinflip-text">{{ coinFlipWinnerIsMe() ? 'Tú' : 'El oponente' }} comienza la partida</span>
              }
            </div>
          </div>
        }

        @if (showInitialMulliganDialog()) {
          <div class="mulligan-draw-bar">
            <div class="mulligan-draw-info">
              <span class="mulligan-draw-text">
                Tu mano no tiene Pokémon Basic.
                Si hacés <strong>mulligan</strong>, tu oponente podrá robar una carta extra.
              </span>
            </div>
            <div class="mulligan-draw-actions">
              <button class="btn-draw" (click)="onInitialMulliganDecision('MULLIGAN')">Hacer mulligan</button>
            </div>
          </div>
        }

        @if (showMulliganDrawDialog()) {
          <div class="mulligan-draw-bar">
            <div class="mulligan-draw-info">
              <span class="mulligan-draw-text">
                Tenés <strong>{{ handCardCount() }}</strong> carta(s) en mano.
                ¿Querés robar <strong>{{ mulliganDrawCount() }}</strong> más?
              </span>
              <span
                class="mulligan-draw-timer"
                [class.urgent]="mulliganDrawTimeLeft() > 0 && mulliganDrawTimeLeft() <= 10000"
              >{{ mulliganDrawTimeLeftFormatted() }}</span>
            </div>
            <div class="mulligan-draw-actions">
              <button class="btn-draw" (click)="onMulliganDrawDecision(true)">Robar</button>
              <button class="btn-skip" (click)="onMulliganDrawDecision(false)">No robar</button>
            </div>
          </div>
        }

        @if (matchState.currentPhase() === 'DRAW' && matchState.isMyTurn()) {
          <div class="draw-dim-overlay"></div>
        }

        @if (state.status === 'SETUP'; as priv) {
          <app-setup-overlay
            [myPlayerState]="myPlayerState()"
            [opponentSetupConfirmed]="opponentSetupConfirmed()"
            [mySetupConfirmed]="mySetupConfirmed()"
            [mulliganDrawPending]="showMulliganDrawDialog()"
            [initialMulliganPending]="showInitialMulliganDialog()"
            [cardDefs]="allCardDefs()"
            (activeDropped)="onActiveDropped($event)"
            (benchDropped)="onBenchDropped($event)"
            (fieldCardRemoved)="onFieldDragStarted($event)"
            (confirmSetup)="onConfirmSetup()"
          />
        }
      </main>
    } @else {
      <main class="match-page loading">
        <app-loading-spinner />
      </main>
    }
  `,
  styles: [`
    :host { display: block; }
    .match-page { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; max-width: 900px; margin: 0 auto; }
    .match-page.loading { min-height: 60vh; display: flex; align-items: center; justify-content: center; }
    .waiting-banner { text-align: center; padding: 2rem; border: 2px dashed #475569; border-radius: 0.5rem; color: #94a3b8; }
    .waiting-banner p { margin: 0 0 1rem; font-size: 1.125rem; }
    .setup-opponent-placeholder { text-align: center; padding: 1.5rem; border: 1px dashed #475569; border-radius: 0.5rem; color: #64748b; font-size: 0.875rem; }
    .mulligan-banner {
      position: fixed; top: 0; left: 0; right: 0; z-index: 150;
      background: #1e293b; border: 1px solid #f59e0b; padding: 0.75rem 1rem;
    }
    .mulligan-msg { margin: 0.25rem 0; color: #fcd34d; font-size: 0.875rem; font-weight: 600; }
    .mulligan-msg:first-child { margin-top: 0; }
    .mulligan-msg:last-child { margin-bottom: 0; }
    .mulligan-draw-bar {
      position: fixed; bottom: 0; left: 0; right: 0; z-index: 150;
      background: #1e293b; border-top: 2px solid #f59e0b;
      padding: 0.75rem 1.5rem;
      display: flex; align-items: center; justify-content: space-between;
      gap: 1rem;
      animation: mulligan-slide-up 0.2s ease-out;
      box-shadow: 0 -4px 12px rgba(0,0,0,0.4);
    }
    .mulligan-draw-info {
      display: flex; align-items: center; gap: 1rem; flex: 1;
    }
    .mulligan-draw-text { color: #e2e8f0; font-size: 0.9375rem; }
    .mulligan-draw-text strong { color: #fbbf24; }
    .mulligan-draw-timer {
      font-family: monospace; font-size: 1.125rem; font-weight: 700;
      color: #94a3b8; min-width: 3rem; text-align: right;
    }
    .mulligan-draw-timer.urgent { color: #ef4444; animation: mulligan-pulse 0.5s ease-in-out infinite; }
    .mulligan-draw-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
    .mulligan-draw-actions button {
      padding: 0.5rem 1.25rem; font-size: 0.875rem; font-weight: 700;
      border: none; border-radius: 0.375rem; cursor: pointer;
      transition: opacity 0.15s, background 0.15s;
    }
    .mulligan-draw-actions button:hover { opacity: 0.9; }
    .btn-draw { background: #22c55e; color: #fff; }
    .btn-skip { background: #64748b; color: #fff; }
    @keyframes mulligan-slide-up {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }
    @keyframes mulligan-pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }
    .coinflip-overlay {
      position: fixed; inset: 0; z-index: 200;
      background: rgba(0,0,0,0.75);
      display: flex; align-items: center; justify-content: center;
    }
    .coinflip-content {
      display: flex; flex-direction: column; align-items: center; gap: 1rem;
      padding: 2rem; border-radius: 0.75rem;
      background: #1e293b; border: 2px solid #fbbf24;
      animation: coinflip-fadein 0.3s ease-out;
    }
    .coin-container {
      perspective: 400px;
      width: 80px; height: 80px;
    }
    .coin {
      width: 80px; height: 80px;
      position: relative;
      transform-style: preserve-3d;
      border-radius: 50%;
    }
    .coin.flip {
      animation: coinflip-toss 0.6s ease-in-out infinite;
    }
    .coin.result .coin-face {
      background: linear-gradient(135deg, #fbbf24, #f59e0b);
      color: #78350f;
    }
    .coin-face {
      position: absolute; inset: 0;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.75rem; font-weight: 900;
      backface-visibility: hidden;
      border: 3px solid #d97706;
      background: linear-gradient(135deg, #fcd34d, #f59e0b);
      color: #92400e;
      box-shadow: inset 0 -3px 6px rgba(0,0,0,0.2), 0 0 12px rgba(251,191,36,0.4);
    }
    .coin-face.back { transform: rotateY(180deg); }
    .coinflip-hint { font-size: 1rem; color: #94a3b8; }
    .coinflip-text { font-size: 1.25rem; font-weight: 700; color: #f1f5f9; text-align: center; animation: coinflip-fadein 0.3s ease-out; }
    @keyframes coinflip-fadein { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
    @keyframes coinflip-toss {
      0% { transform: rotateY(0deg); }
      100% { transform: rotateY(720deg); }
    }
    .draw-dim-overlay {
      position: fixed;
      inset: 0;
      top: auto;
      bottom: 120px;
      background: rgba(0, 0, 0, 0.55);
      z-index: 50;
      pointer-events: none;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MatchPage implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly cardRepo = inject(CardRepositoryService);
  private readonly dispatcher = inject(GameActionDispatcherService);
  protected readonly matchState = inject(MatchStateService);
  protected readonly interactionService = inject(MatchInteractionService);

  private matchId: string | null = null;
  private mulliganTimerInterval: ReturnType<typeof setInterval> | null = null;

  readonly myPlayerState = computed(() => {
    const state = this.matchState.publicState();
    const myId = this.matchState.myPlayerId();
    if (!state || !myId) return null;
    return state.players.find(p => p.playerId === myId) ?? null;
  });

  readonly opponentPlayerState = computed(() => {
    const state = this.matchState.publicState();
    const myId = this.matchState.myPlayerId();
    if (!state || !myId) return null;
    return state.players.find(p => p.playerId !== myId) ?? null;
  });

  readonly opponentSetupConfirmed = computed(() => {
    const opponent = this.opponentPlayerState();
    return opponent?.setupConfirmed ?? false;
  });

  readonly selectionState = computed(() => this.interactionService.selection());

  readonly canDragToBench = computed(() => {
    const state = this.matchState.publicState();
    if (!state) return false;
    return state.status === 'ACTIVE'
        && state.phase === 'MAIN'
        && this.matchState.isMyTurn();
  });

  protected readonly handZoneDroplistId = 'hand-zone-droplist';
  protected readonly benchSlotDroplistIds = computed(() => {
    const ids = ['active-slot'];
    for (let i = 0; i < 5; i++) {
      ids.push('bench-slot-' + i);
    }
    return ids;
  });

  private readonly allPokemonInstanceIds = computed(() => {
    const me = this.myPlayerState();
    if (!me) return [];
    const ids: string[] = [];
    if (me.activePokemon) ids.push(me.activePokemon.instanceId);
    for (const poke of me.bench) {
      if (poke) ids.push(poke.instanceId);
    }
    return ids;
  });

  private readonly benchInstanceIds = computed(() => {
    const me = this.myPlayerState();
    if (!me) return [];
    return me.bench.filter((p): p is NonNullable<typeof p> => p !== null).map(p => p.instanceId);
  });

  readonly allCardDefs = computed(() => {
    const map = new Map<string, CardDetailResponse | null>();
    const state = this.matchState.publicState();
    const priv = this.matchState.privateState();
    if (state) {
      for (const player of state.players) {
        if (player.activePokemon) map.set(player.activePokemon.cardId, this.cardRepo.getFromCache(player.activePokemon.cardId));
        for (const poke of player.bench) {
          if (poke) map.set(poke.cardId, this.cardRepo.getFromCache(poke.cardId));
        }
      }
    }
    if (priv?.hand) {
      for (const card of priv.hand) {
        if (!map.has(card.cardId)) map.set(card.cardId, this.cardRepo.getFromCache(card.cardId));
      }
    }
    return map;
  });

  readonly mySetupConfirmed = computed(() => {
    const me = this.myPlayerState();
    return me?.setupConfirmed ?? false;
  });

  readonly mulliganMessages = computed(() => {
    const state = this.matchState.publicState();
    if (!state || state.status !== 'SETUP') return [];
    const myId = this.matchState.myPlayerId();
    if (!myId) return [];
    const msgs: string[] = [];
    for (const player of state.players) {
      const count = player.mulliganCount ?? 0;
      if (count <= 0) continue;
      if (player.playerId === myId) {
        msgs.push(`Hiciste mulligan — tu mano fue reemplazada`);
      } else {
        msgs.push(`El oponente hizo mulligan`);
      }
    }
    return msgs;
  });

  readonly coinFlipPhase = signal<'spinning' | 'result' | null>(null);
  readonly coinFlipWinnerIsMe = signal(false);

  readonly mulliganDrawCount = computed(() => {
    return this.matchState.privateState()?.pendingMulliganDrawCount ?? 0;
  });

  readonly showInitialMulliganDialog = computed(() => {
    const state = this.matchState.publicState();
    const myId = this.matchState.myPlayerId();
    if (!state || state.status !== 'SETUP' || !myId) return false;
    return state.pendingInitialMulliganPlayers?.includes(myId) ?? false;
  });

  readonly showMulliganDrawDialog = computed(() => {
    const state = this.matchState.publicState();
    if (!state || state.status !== 'SETUP') return false;
    return state.mulliganDrawPending === true && this.mulliganDrawCount() > 0;
  });

  readonly handCardCount = computed(() => {
    return this.matchState.privateState()?.hand?.length ?? 0;
  });

  readonly mulliganDrawDeadlineMs = computed(() => {
    const deadline = this.matchState.publicState()?.mulliganDrawDeadline;
    if (!deadline) return 0;
    return new Date(deadline).getTime();
  });

  readonly mulliganDrawTimeLeft = signal(0);

  readonly mulliganDrawTimeLeftFormatted = computed(() => {
    const ms = this.mulliganDrawTimeLeft();
    if (ms <= 0) return '—';
    const secs = Math.ceil(ms / 1000);
    return `${secs}s`;
  });

  private previousStatus: string | null = null;

  /**
   * Returns instance IDs of Pokemon in play (active + bench) whose cardDef.name
   * matches the `evolvesFrom` of the card at the given handIndex.
   * Used for EVOLVE_POKEMON target selection.
   */
  private getEligiblePokemonInstanceIds(handIndex: number): string[] {
    const hand = this.matchState.privateState()?.hand;
    if (!hand) return [];
    const card = hand[handIndex];
    if (!card) return [];

    const cardDef = this.cardRepo.getFromCache(card.cardId);
    if (!cardDef?.evolvesFrom || !cardDef.stage || cardDef.stage === 'BASIC') return [];

    const myState = this.myPlayerState();
    if (!myState) return [];

    const eligibleIds: string[] = [];

    // Check active Pokemon
    if (myState.activePokemon) {
      const activeDef = this.cardRepo.getFromCache(myState.activePokemon.cardId);
      if (activeDef?.name === cardDef.evolvesFrom) {
        eligibleIds.push(myState.activePokemon.instanceId);
      }
    }

    // Check bench Pokemon
    for (const poke of myState.bench) {
      if (!poke) continue;
      const benchDef = this.cardRepo.getFromCache(poke.cardId);
      if (benchDef?.name === cardDef.evolvesFrom) {
        eligibleIds.push(poke.instanceId);
      }
    }

    return eligibleIds;
  }

  constructor() {
    effect(() => {
      const publicState = this.matchState.publicState();
      if (!publicState) return;

      const cardIds = new Set<string>();
      for (const player of publicState.players) {
        if (player.activePokemon) cardIds.add(player.activePokemon.cardId);
        for (const poke of player.bench) {
          if (poke) cardIds.add(poke.cardId);
        }
      }

      // Preload hand card cardDefs (needed for evolution stage check, trainer subtypes, etc.)
      const privateState = this.matchState.privateState();
      if (privateState?.hand) {
        for (const card of privateState.hand) {
          cardIds.add(card.cardId);
        }
      }

      this.cardRepo.preload(Array.from(cardIds));
    });

    effect(() => {
      const publicState = this.matchState.publicState();
      if (!publicState) return;

      const status = publicState.status;
      const previous = this.previousStatus;

      if (previous === 'SETUP' && status === 'ACTIVE' && !this.coinFlipPhase()) {
        const myId = this.matchState.myPlayerId();
        if (publicState.firstPlayerId && myId) {
          this.coinFlipWinnerIsMe.set(publicState.firstPlayerId === myId);
          this.coinFlipPhase.set('spinning');
          setTimeout(() => {
            this.coinFlipPhase.set('result');
            setTimeout(() => this.coinFlipPhase.set(null), 1500);
          }, 1500);
        }
      }

      this.previousStatus = status;
    });

    effect(() => {
      const deadlineMs = this.mulliganDrawDeadlineMs();
      if (deadlineMs > 0) {
        if (this.mulliganTimerInterval) clearInterval(this.mulliganTimerInterval);
        const tick = () => this.mulliganDrawTimeLeft.set(Math.max(0, deadlineMs - Date.now()));
        tick();
        this.mulliganTimerInterval = setInterval(tick, 200);
      } else {
        if (this.mulliganTimerInterval) {
          clearInterval(this.mulliganTimerInterval);
          this.mulliganTimerInterval = null;
        }
        this.mulliganDrawTimeLeft.set(0);
      }
    });


  }

  ngOnInit(): void {
    const playerId = this.authService.playerId();
    if (!playerId) {
      this.router.navigate(['/lobby']);
      return;
    }

    this.matchId = this.route.snapshot.params['id'] ?? null;
    if (!this.matchId) {
      this.router.navigate(['/lobby']);
      return;
    }

    this.matchState.initialize(this.matchId);
  }

  ngOnDestroy(): void {
    if (this.mulliganTimerInterval) {
      clearInterval(this.mulliganTimerInterval);
    }
    this.matchState.reset();
  }

  protected onPokemonClicked(event: PublicPokemonSlotModel | { benchIndex: number }): void {
    const state = this.matchState.publicState();
    if (!state || state.status === 'SETUP') return;

    const mode = this.selectionState().mode;
    const myId = this.matchState.myPlayerId();
    if (!this.matchId || !myId) return;

    if (mode === 'SELECT_BENCH_SLOT') {
      const benchIndex = 'benchIndex' in event ? event.benchIndex : -1;
      if (benchIndex < 0) return;

      const benchSlot = this.myPlayerState()?.bench[benchIndex];
      if (benchSlot !== null && benchSlot !== undefined) return;

      const selectedHandIndex = this.selectionState().selectedHandIndex;
      if (selectedHandIndex === null) return;

      console.warn(`[DEBUG] onPokemonClicked SELECT_BENCH_SLOT: handIndex=${selectedHandIndex}, benchIndex=${benchIndex}`);
      this.matchState.optimisticallyRemoveCardFromHand(selectedHandIndex);
      this.dispatcher.putBasicOnBench(this.matchId, myId, selectedHandIndex, benchIndex);
      return;
    }

    if (mode === 'SELECT_RETREAT_TARGET') {
      const benchIndex = 'benchIndex' in event ? event.benchIndex : -1;
      if (benchIndex < 0) return;

      const benchSlot = this.myPlayerState()?.bench[benchIndex];
      if (!benchSlot) return;

      const myId = this.matchState.myPlayerId();
      if (!this.matchId || !myId) return;
      this.dispatcher.retreatActive(this.matchId, myId, benchIndex);
      return;
    }

    if (mode === 'SELECT_TARGET_POKEMON') {
      const state = this.matchState.publicState();
      if (!state || state.status !== 'ACTIVE' || state.phase !== 'MAIN' || !this.matchState.isMyTurn()) {
        console.warn(`[DEBUG] onPokemonClicked SELECT_TARGET_POKEMON: blocked by state/phase/turn`);
        return;
      }

      let targetInstanceId: string | null = null;

      if ('instanceId' in event) {
        targetInstanceId = event.instanceId;
        console.warn(`[DEBUG] SELECT_TARGET_POKEMON: active target instanceId=${targetInstanceId}`);
      } else {
        const benchLen = this.myPlayerState()?.bench.length ?? 0;
        const benchSlot = this.myPlayerState()?.bench[event.benchIndex];
        console.warn(`[DEBUG] SELECT_TARGET_POKEMON: bench benchIndex=${event.benchIndex}, bench.length=${benchLen}, slot=${benchSlot?.cardId ?? 'null'} instanceId=${benchSlot?.instanceId ?? 'null'}`);
        targetInstanceId = benchSlot?.instanceId ?? null;
      }

      if (!targetInstanceId) {
        console.warn(`[DEBUG] onPokemonClicked SELECT_TARGET_POKEMON: no targetInstanceId`);
        return;
      }

      const selectedHandIndex = this.selectionState().selectedHandIndex;
      if (selectedHandIndex === null) {
        console.warn(`[DEBUG] onPokemonClicked SELECT_TARGET_POKEMON: no selectedHandIndex`);
        return;
      }

      const hand = this.matchState.privateState()?.hand;
      const card = hand?.[selectedHandIndex];
      if (!card) {
        console.warn(`[DEBUG] onPokemonClicked SELECT_TARGET_POKEMON: card not found at handIndex ${selectedHandIndex}`);
        return;
      }

      const supertype = card.supertype;
      const cardDef = this.cardRepo.getFromCache(card.cardId);
      console.warn(`[DEBUG] onPokemonClicked SELECT_TARGET_POKEMON: supertype=${supertype}, target=${targetInstanceId}, handIndex=${selectedHandIndex}`);
      this.matchState.optimisticallyRemoveCardFromHand(selectedHandIndex);
      if (supertype === 'ENERGY') {
        this.dispatcher.attachEnergy(this.matchId, myId, selectedHandIndex, targetInstanceId);
      } else if (supertype === 'TRAINER' && cardDef?.subtypes?.includes('POKEMON_TOOL')) {
        this.dispatcher.attachTool(this.matchId, myId, selectedHandIndex, targetInstanceId);
      } else {
        this.dispatcher.evolvePokemon(this.matchId, myId, selectedHandIndex, targetInstanceId);
      }
      return;
    }
  }

  protected onHandCardClicked(card: PrivateHandCardModel, handIndex: number): void {
    const state = this.matchState.publicState();
    if (!state || state.status === 'SETUP') return;

    const mode = this.selectionState().mode;
    if (mode !== 'NONE') return;

    const supertype = card.supertype;

    if (supertype === 'POKEMON') {
      const cardDef = this.cardRepo.getFromCache(card.cardId);

      // If cardDef has a stage other than BASIC, it's an evolution card
      if (cardDef?.stage && cardDef.stage !== 'BASIC') {
        const eligibleIds = this.getEligiblePokemonInstanceIds(handIndex);
        if (eligibleIds.length === 0) return; // No valid Pokemon to evolve
        this.interactionService.enterSelectTargetPokemon(handIndex, eligibleIds);
        return;
      }

      // BASIC (or unknown cardDef) → put on bench
      this.interactionService.enterSelectBenchSlot(handIndex, []);
      return;
    }

    if (supertype === 'ENERGY') {
      if (!state || state.status !== 'ACTIVE' || state.phase !== 'MAIN' || !this.matchState.isMyTurn()) return;
      this.interactionService.enterSelectTargetPokemon(handIndex, this.allPokemonInstanceIds());
      return;
    }

    if (supertype === 'TRAINER') {
      const cardDef = this.cardRepo.getFromCache(card.cardId);
      if (cardDef?.subtypes?.includes('POKEMON_TOOL')) {
        this.interactionService.enterSelectTargetPokemon(handIndex, this.allPokemonInstanceIds());
        return;
      }
      const myId = this.matchState.myPlayerId();
      if (this.matchId && myId) {
        this.matchState.optimisticallyRemoveCardFromHand(handIndex);
        this.dispatcher.playTrainer(this.matchId, myId, handIndex);
      }
    }
  }

  protected onActiveDropped(cardInstanceId: string): void {
    const myId = this.matchState.myPlayerId();
    if (this.matchId && myId) {
      this.matchState.optimisticallyRemoveCardByInstanceId(cardInstanceId);
      this.dispatcher.placeActive(this.matchId, myId, cardInstanceId);
    }
  }

  protected onBenchDropped(event: { cardInstanceId: string; benchIndex: number }): void {
    const myId = this.matchState.myPlayerId();
    if (this.matchId && myId) {
      this.matchState.optimisticallyRemoveCardByInstanceId(event.cardInstanceId);
      this.dispatcher.placeBench(this.matchId, myId, event.cardInstanceId, event.benchIndex);
    }
  }

  protected onHandToBenchDropped(event: { handIndex: number; benchIndex: number }): void {
    const state = this.matchState.publicState();
    if (!state || state.status !== 'ACTIVE' || state.phase !== 'MAIN') return;

    const myId = this.matchState.myPlayerId();
    if (!this.matchId || !myId) return;

    const benchSlot = this.myPlayerState()?.bench[event.benchIndex];
    if (benchSlot !== null && benchSlot !== undefined) return;

    console.warn(`[DEBUG] onHandToBenchDropped: handIndex=${event.handIndex}, benchIndex=${event.benchIndex}`);
    this.matchState.optimisticallyRemoveCardFromHand(event.handIndex);
    this.dispatcher.putBasicOnBench(this.matchId, myId, event.handIndex, event.benchIndex);
  }

  protected onEnergyDropped(event: { handIndex: number; targetInstanceId: string }): void {
    const myId = this.matchState.myPlayerId();
    if (!this.matchId || !myId) return;
    const state = this.matchState.publicState();
    if (!state || state.status !== 'ACTIVE' || state.phase !== 'MAIN' || !this.matchState.isMyTurn()) return;
    this.matchState.optimisticallyRemoveCardFromHand(event.handIndex);
    this.dispatcher.attachEnergy(this.matchId, myId, event.handIndex, event.targetInstanceId);
  }

  protected onEvolutionDropped(event: { handIndex: number; targetInstanceId: string }): void {
    const myId = this.matchState.myPlayerId();
    if (!this.matchId || !myId) return;
    const state = this.matchState.publicState();
    if (!state || state.status !== 'ACTIVE' || state.phase !== 'MAIN' || !this.matchState.isMyTurn()) return;
    const hand = this.matchState.privateState()?.hand;
    const card = hand?.[event.handIndex];
    if (!card || card.supertype !== 'POKEMON') return;
    const cardDef = this.cardRepo.getFromCache(card.cardId);
    if (!cardDef?.stage || cardDef.stage === 'BASIC') return;
    this.matchState.optimisticallyRemoveCardFromHand(event.handIndex);
    this.dispatcher.evolvePokemon(this.matchId, myId, event.handIndex, event.targetInstanceId);
  }

  protected onFieldDragStarted(instanceId: string): void {
    const myId = this.matchState.myPlayerId();
    if (!this.matchId || !myId) return;
    const me = this.myPlayerState();
    if (!me) return;
    if (me.activePokemon?.instanceId === instanceId) {
      this.dispatcher.removeActive(this.matchId, myId);
    } else {
      this.dispatcher.removeBench(this.matchId, myId, instanceId);
    }
  }

  protected onConfirmSetup(): void {
    const myId = this.matchState.myPlayerId();
    if (this.matchId && myId) {
      this.dispatcher.confirmSetup(this.matchId, myId);
    }
  }

  protected onInitialMulliganDecision(decision: 'MULLIGAN' | 'KEEP'): void {
    const myId = this.matchState.myPlayerId();
    if (this.matchId && myId) {
      this.dispatcher.resolveInitialMulligan(this.matchId, myId, decision);
    }
  }

  protected onMulliganDrawDecision(drawCards: boolean): void {
    const myId = this.matchState.myPlayerId();
    if (this.matchId && myId) {
      this.dispatcher.resolveMulliganDraw(this.matchId, myId, drawCards);
    }
  }

  protected onRetreatInitiated(): void {
    if (!this.matchId) return;
    const myId = this.matchState.myPlayerId();
    if (!myId) return;
    this.interactionService.enterSelectRetreatTarget(this.benchInstanceIds());
  }

  protected onActionSelected(action: { type: GameActionType; payload?: Record<string, unknown> }): void {
    if (!this.matchId) return;

    if (action.type === 'RETREAT_ACTIVE') {
      this.interactionService.enterSelectRetreatTarget(this.benchInstanceIds());
      return;
    }

    this.dispatcher.dispatchAction(this.matchId, undefined, action.type, action.payload);
  }

  protected onDrawCard(): void {
    if (!this.matchId) return;
    const myId = this.matchState.myPlayerId();
    if (!myId) return;
    this.dispatcher.dispatchAction(this.matchId, myId, 'DRAW_CARD', {});
  }

  protected onReturnToLobby(): void {
    this.matchState.reset();
    this.router.navigate(['/lobby']);
  }

  protected onEscapeKey(): void {
    if (this.interactionService.isSelecting()) {
      this.interactionService.cancelSelection();
    }
  }
}

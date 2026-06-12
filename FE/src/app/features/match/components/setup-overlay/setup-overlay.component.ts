import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { DragDropModule, CdkDragDrop } from '@angular/cdk/drag-drop';
import { PrivateHandCardModel, PublicPlayerStateModel, PublicPokemonSlotModel } from '../../../../shared/models/game-state.models';
import { CardDetailResponse } from '../../../../shared/models/card.models';
import { CardImagePipe } from '../../../../shared/pipes/card-image.pipe';
import { MatchStateService } from '../../services/match-state.service';

@Component({
  selector: 'app-setup-overlay',
  standalone: true,
  imports: [DragDropModule, CardImagePipe],
  template: `
    <div class="setup-backdrop">
      @if (opponentMulliganRevealedCards().length > 0 || myMulliganRevealedCards().length > 0) {
        <div class="mulligan-revealed-section">
          <h3 class="revealed-section-title">Cartas descartadas por mulligan</h3>
          @if (opponentMulliganRevealedCards().length > 0) {
            <div class="revealed-group">
              <span class="revealed-label">Cartas descartadas por el oponente</span>
              <div class="scroll-wrapper">
                <button class="scroll-btn scroll-left" (click)="scrollLeft(opponentScroll)">
                  ◀
                </button>
                <div #opponentScroll class="revealed-cards-row" (scroll)="onScroll(opponentScroll)">
                  @for (revealGroup of opponentMulliganRevealedCards(); track $index) {
                    @for (cardId of revealGroup; track $index) {
                      <div class="revealed-card">
                        <img [src]="cardId | cardImage" alt="" class="mini-img" />
                        <span class="revealed-card-name">{{ cardName(cardId) }}</span>
                      </div>
                    }
                    @if (!$last) { <span class="reveal-separator">|</span> }
                  }
                </div>
                <button class="scroll-btn scroll-right" (click)="scrollRight(opponentScroll)">
                  ▶
                </button>
              </div>
            </div>
          }
          @if (myMulliganRevealedCards().length > 0) {
            <div class="revealed-group">
              <span class="revealed-label">Cartas descartadas por ti</span>
              <div class="scroll-wrapper">
                <button class="scroll-btn scroll-left" (click)="scrollLeft(myScroll)">
                  ◀
                </button>
                <div #myScroll class="revealed-cards-row" (scroll)="onScroll(myScroll)">
                  @for (revealGroup of myMulliganRevealedCards(); track $index) {
                    @for (cardId of revealGroup; track $index) {
                      <div class="revealed-card">
                        <img [src]="cardId | cardImage" alt="" class="mini-img" />
                        <span class="revealed-card-name">{{ cardName(cardId) }}</span>
                      </div>
                    }
                    @if (!$last) { <span class="reveal-separator">|</span> }
                  }
                </div>
                <button class="scroll-btn scroll-right" (click)="scrollRight(myScroll)">
                  ▶
                </button>
              </div>
            </div>
          }
        </div>
      }

      <div class="setup-panel" cdkDropListGroup>
        <h2 class="setup-title">Configura tu Pokémon Inicial</h2>

        <div class="section-label">POKÉMON ACTIVO</div>
        <div
          class="drop-zone active-zone"
          cdkDropList
          (cdkDropListDropped)="onActiveDrop($event)"
        >
          @if (activePokemon(); as active) {
            <div class="field-card">
              <img [src]="active.cardId | cardImage" alt="" class="mini-img" />
              <span class="field-card-name">{{ cardName(active.cardId) }}</span>
              <button class="remove-btn" (click)="onFieldCardRemove(active.instanceId); $event.stopPropagation()" title="Quitar de activo">✕</button>
            </div>
          } @else {
            <span class="placeholder">Arrastra aquí tu Pokémon activo</span>
          }
        </div>

        <div class="section-label">BANCA</div>
        <div class="bench-row">
          @for (slot of benchSlots(); track $index; let idx = $index) {
            <div
              class="drop-zone bench-slot"
              cdkDropList
              (cdkDropListDropped)="onBenchDrop($event, idx)"
            >
              @if (slot; as poke) {
                <div class="field-card">
                  <img [src]="poke.cardId | cardImage" alt="" class="mini-img" />
                  <span class="field-card-name">{{ cardName(poke.cardId) }}</span>
                  <button class="remove-btn" (click)="onFieldCardRemove(poke.instanceId); $event.stopPropagation()" title="Quitar de banca">✕</button>
                </div>
              } @else {
                <span class="placeholder">+</span>
              }
            </div>
          }
        </div>

        <div class="section-label">TUS CARTAS</div>
        <div class="hand-zone" cdkDropList>
          @for (card of hand(); track card.instanceId) {
            <div
              class="hand-card draggable-hand-card"
              cdkDrag
              [cdkDragData]="card.instanceId"
              [cdkDragDisabled]="!isBasicPokemon(card)"
              [class.non-draggable]="!isBasicPokemon(card)"
            >
              <img [src]="card.cardId | cardImage" alt="{{ card.name }}" class="mini-img" />
              <span class="hand-card-name">{{ card.name }}</span>
              @if (!isBasicPokemon(card)) {
                <div class="no-drop-badge" title="No es un Pokémon básico">⛔</div>
              }
            </div>
          }
        </div>

        <div class="setup-actions">
          <button
            class="btn-confirm"
            [disabled]="!canConfirm()"
            (click)="onConfirm()"
          >
            {{ mySetupConfirmed() ? 'Esperando oponente...' : 'Confirmar' }}
          </button>
          <div class="opponent-status">
            @if (opponentSetupConfirmed()) {
              <span class="opponent-ready">✓ Tu oponente está listo</span>
            } @else if (opponentResolvingMulligan()) {
              <span class="opponent-waiting">⏳ El oponente está resolviendo su mulligan...</span>
            } @else {
              <span class="opponent-waiting">⏳ Esperando oponente...</span>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .setup-backdrop {
      position: fixed; inset: 0; z-index: 100;
      background: rgba(0,0,0,0.7);
      display: flex; flex-direction: column; align-items: center; justify-content: flex-start;
      padding: 1rem; overflow-y: auto;
    }
    .mulligan-revealed-section {
      width: 100%; max-width: 600px;
      background: #0f172a; border: 1px solid #f59e0b; border-radius: 0.5rem;
      padding: 0.75rem; margin-bottom: 0.75rem; flex-shrink: 0;
    }
    .revealed-section-title {
      margin: 0 0 0.5rem; font-size: 0.8125rem; font-weight: 700; color: #fcd34d;
    }
    .revealed-group {
      display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 0.375rem;
    }
    .revealed-group:last-child { margin-bottom: 0; }
    .revealed-label {
      font-size: 0.75rem; font-weight: 600; color: #94a3b8;
    }
    .scroll-wrapper {
      display: flex; align-items: center; gap: 0.25rem;
      position: relative;
    }
    .scroll-btn {
      flex-shrink: 0; width: 22px; height: 22px; border-radius: 50%;
      border: 1px solid #475569; background: #1e293b; color: #94a3b8;
      font-size: 0.625rem; line-height: 1; cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      padding: 0; transition: background 0.15s, color 0.15s;
    }
    .scroll-btn:hover { background: #334155; color: #f1f5f9; }
    .revealed-cards-row {
      display: flex; gap: 0.5rem; align-items: center;
      overflow-x: auto; flex-wrap: nowrap;
      scroll-behavior: smooth; padding: 0.25rem 0;
      -webkit-overflow-scrolling: touch;
    }
    .revealed-cards-row::-webkit-scrollbar { height: 4px; }
    .revealed-cards-row::-webkit-scrollbar-track { background: transparent; }
    .revealed-cards-row::-webkit-scrollbar-thumb { background: #475569; border-radius: 2px; }
    .revealed-card { display: flex; flex-direction: column; align-items: center; gap: 0.125rem; flex-shrink: 0; }
    .revealed-card .mini-img { width: 45px; aspect-ratio: 3/4; border-radius: 0.25rem; background: #334155; }
    .revealed-card-name { font-size: 0.5625rem; font-weight: 600; color: #e2e8f0; max-width: 50px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-align: center; }
    .reveal-separator { color: #f59e0b; font-size: 0.875rem; font-weight: 700; flex-shrink: 0; }
    .setup-panel {
      background: #1e293b; border: 1px solid #475569; border-radius: 0.75rem;
      padding: 1.5rem; max-width: 600px; width: 100%;
      display: flex; flex-direction: column; gap: 0.5rem;
    }
    .setup-title { margin: 0 0 0.5rem; font-size: 1.25rem; font-weight: 800; color: #f1f5f9; text-align: center; }
    .section-label { font-size: 0.75rem; font-weight: 600; color: #64748b; text-transform: uppercase; margin-top: 0.5rem; }

    .drop-zone {
      border: 2px dashed #475569; border-radius: 0.5rem;
      min-height: 80px; display: flex; align-items: center; justify-content: center;
      transition: border-color 0.15s, background 0.15s;
    }
    .drop-zone.cdk-drop-list-dragover { border-color: #22d3ee; background: #0f172a; }
    .drop-zone .placeholder { color: #64748b; font-size: 0.875rem; }

    .active-zone { min-height: 100px; }
    .bench-row { display: flex; gap: 0.5rem; }
    .bench-slot { flex: 1; min-width: 0; min-height: 100px; }

    .field-card {
      display: flex; flex-direction: column; align-items: center; gap: 0.25rem;
      padding: 0.5rem; position: relative;
    }
    .field-card-name { font-size: 0.6875rem; font-weight: 600; color: #e2e8f0; }

    .remove-btn {
      position: absolute; top: 2px; right: 2px;
      width: 20px; height: 20px; border-radius: 50%;
      border: none; background: #ef4444; color: #fff;
      font-size: 0.75rem; line-height: 1; cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      padding: 0; opacity: 0; transition: opacity 0.15s;
    }
    .field-card:hover .remove-btn { opacity: 1; }

    .hand-zone {
      display: flex; gap: 0.5rem; padding: 0.5rem;
      background: #0f172a; border-radius: 0.5rem; border: 1px solid #334155;
      overflow-x: auto; flex-wrap: nowrap;
    }
    .draggable-hand-card {
      display: flex; flex-direction: column; align-items: center; gap: 0.25rem;
      padding: 0.375rem; border: 2px solid #475569; border-radius: 0.375rem;
      background: #1e293b; min-width: 70px; width: 70px;
      cursor: grab; transition: opacity 0.15s, border-color 0.15s;
      color: #e2e8f0; position: relative;
      touch-action: none;
    }
    .draggable-hand-card:active { cursor: grabbing; }
    .draggable-hand-card.cdk-drag-dragging { opacity: 0.3; }
    .draggable-hand-card.non-draggable {
      cursor: not-allowed; opacity: 0.55;
      border-color: #991b1b;
    }

    .mini-img {
      width: 50px; aspect-ratio: 3/4; object-fit: cover;
      border-radius: 0.25rem; background: #334155;
    }
    .hand-card-name { font-size: 0.625rem; font-weight: 600; text-align: center; line-height: 1.2; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    .no-drop-badge {
      position: absolute; top: 2px; right: 2px;
      font-size: 0.875rem; line-height: 1;
      filter: drop-shadow(0 1px 2px rgba(0,0,0,0.5));
    }

    .setup-actions {
      display: flex; align-items: center; justify-content: center; gap: 1rem;
      margin-top: 0.75rem; padding-top: 0.75rem; border-top: 1px solid #334155;
    }
    .btn-confirm {
      padding: 0.75rem 2rem; font-size: 1rem; font-weight: 700; border: none;
      border-radius: 0.5rem; cursor: pointer; background: #3b82f6; color: #fff;
      transition: opacity 0.15s, background 0.15s;
    }
    .btn-confirm:disabled { opacity: 0.5; cursor: not-allowed; background: #64748b; }
    .btn-confirm:hover:not(:disabled) { background: #2563eb; }
    .opponent-status { font-size: 0.875rem; font-weight: 600; }
    .opponent-ready { color: #22c55e; }
    .opponent-waiting { color: #f59e0b; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SetupOverlayComponent {
  private readonly matchState = inject(MatchStateService);

  readonly myPlayerState = input<PublicPlayerStateModel | null>(null);
  readonly opponentSetupConfirmed = input(false);
  readonly mySetupConfirmed = input(false);
  readonly mulliganDrawPending = input(false);
  readonly initialMulliganPending = input(false);
  readonly cardDefs = input<Map<string, CardDetailResponse | null>>(new Map());

  readonly activeDropped = output<string>();
  readonly benchDropped = output<{ cardInstanceId: string; benchIndex: number }>();
  readonly fieldCardRemoved = output<string>();
  readonly confirmSetup = output<void>();

  readonly hand = computed(() => this.matchState.privateState()?.hand ?? []);

  readonly activePokemon = computed(() => this.myPlayerState()?.activePokemon ?? null);

  readonly benchSlots = computed<(PublicPokemonSlotModel | null)[]>(() => {
    const me = this.myPlayerState();
    const bench = me?.bench ?? [];
    const result: (PublicPokemonSlotModel | null)[] = [...bench];
    while (result.length < 5) result.push(null);
    return result;
  });

  readonly opponentResolvingMulligan = computed(() => {
    const pub = this.matchState.publicState();
    if (!pub?.pendingInitialMulliganPlayers?.length) return false;
    const myId = this.matchState.myPlayerId();
    if (!myId) return false;
    return !pub.pendingInitialMulliganPlayers.includes(myId);
  });

  readonly opponentMulliganRevealedCards = computed(() => {
    const pub = this.matchState.publicState();
    const myId = this.matchState.myPlayerId();
    if (!pub || !myId) return [];
    const opponent = pub.players.find(p => p.playerId !== myId);
    return opponent?.mulliganRevealedCards ?? [];
  });

  readonly myMulliganRevealedCards = computed(() => {
    const pub = this.matchState.publicState();
    const myId = this.matchState.myPlayerId();
    if (!pub || !myId) return [];
    const me = pub.players.find(p => p.playerId === myId);
    return me?.mulliganRevealedCards ?? [];
  });

  readonly canConfirm = computed(() => {
    const me = this.myPlayerState();
    if (!me) return false;
    if (me.setupConfirmed) return false;
    if (this.mulliganDrawPending()) return false;
    if (this.initialMulliganPending()) return false;
    if (this.opponentResolvingMulligan()) return false;
    return me.activePokemon !== null;
  });

  protected isBasicPokemon(card: PrivateHandCardModel): boolean {
    if (card.supertype !== 'POKEMON') return false;
    const def = this.cardDefs().get(card.cardId);
    if (!def) return false;
    return !def.stage || def.stage === 'BASIC';
  }

  protected cardName(cardId: string): string {
    const def = this.cardDefs().get(cardId);
    return def?.name ?? '?';
  }

  protected onActiveDrop(event: CdkDragDrop<string>): void {
    this.activeDropped.emit(event.item.data);
  }

  protected onBenchDrop(event: CdkDragDrop<string>, benchIndex: number): void {
    this.benchDropped.emit({ cardInstanceId: event.item.data, benchIndex });
  }

  protected onFieldCardRemove(instanceId: string): void {
    this.fieldCardRemoved.emit(instanceId);
  }

  protected onConfirm(): void {
    this.confirmSetup.emit();
  }

  protected scrollLeft(el: HTMLDivElement): void {
    el.scrollBy({ left: -200, behavior: 'smooth' });
  }

  protected scrollRight(el: HTMLDivElement): void {
    el.scrollBy({ left: 200, behavior: 'smooth' });
  }

  protected onScroll(el: HTMLDivElement): void {
    // no-op; can be used to toggle button visibility if needed
  }
}

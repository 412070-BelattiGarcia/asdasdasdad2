import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { PublicPlayerStateModel, PrivatePlayerStateModel, PublicPokemonSlotModel } from '../../../../shared/models/game-state.models';
import { CardDetailResponse, EnergyType } from '../../../../shared/models/card.models';
import { CardRepositoryService } from '../../../../core/services/card-repository.service';
import { PokemonSlotComponent } from '../pokemon-slot/pokemon-slot.component';
import { BenchZoneComponent } from '../bench-zone/bench-zone.component';
import { PrizeZoneComponent } from '../prize-zone/prize-zone.component';
import { SelectionMode } from '../../../../shared/models/ui-state.models';
import { GameActionType } from '../../../../shared/models/game-action.models';
import { EnergyIconPipe } from '../../../../shared/pipes/energy-icon.pipe';
import { MatchStateService } from '../../services/match-state.service';
import { MatchInteractionService } from '../../services/match-interaction.service';

@Component({
  selector: 'app-player-area',
  standalone: true,
  imports: [DragDropModule, PokemonSlotComponent, BenchZoneComponent, PrizeZoneComponent, EnergyIconPipe],
  template: `
    @if (playerState(); as player) {
      <div class="player-area">
        @if (privateState(); as priv) {
          <div class="hand-info">Mano: {{ priv.hand.length }} cartas</div>
        }

        <div class="section-label">ACTIVO</div>
        <div class="active-zone">
          @if (player.activePokemon; as active) {
            <div
              class="active-slot-droplist"
              cdkDropList
              id="active-slot"
              [cdkDropListConnectedTo]="connectedDropListIds()"
              [cdkDropListDisabled]="!dragEnabled()"
              (cdkDropListDropped)="onActiveDrop($event)"
            >
              <app-pokemon-slot
                [pokemon]="active"
                [cardDef]="cardDefs().get(active.instanceId) ?? null"
                [isActive]="true"
                [isOwn]="true"
                [isHighlighted]="validTargets().includes(active.instanceId)"
                (slotClicked)="onPokemonClicked($event)"
              />
            </div>
          } @else {
            <span class="empty-active-text">Sin Pokémon activo</span>
          }
        </div>

        @if (matchState.isMyTurn() && player.activePokemon && attacksWithAvailability().length > 0) {
          <div class="section-label">ATAQUES</div>
          <div class="attack-buttons">
            @for (attack of attacksWithAvailability(); track attack.index) {
              <button
                class="btn btn-attack"
                [class.available]="attack.met"
                [class.unavailable]="!attack.met"
                [disabled]="interaction.isSelecting() || interaction.actionInProgress() || !attack.met"
                [title]="!attack.met ? 'Energía insuficiente' : ''"
                (click)="selectAttack(attack.index)"
              >
                <div class="attack-cost-row">
                  @for (e of attack.cost; track $index) {
                    <img [src]="e | energyIcon" alt="{{ e }}" class="cost-icon"
                      [class.met]="isEnergyMet(attack.cost, $index)"
                      [class.unmet]="!isEnergyMet(attack.cost, $index)" />
                  }
                </div>
                <span class="attack-name">{{ attack.name }}</span>
                <span class="attack-damage">{{ attack.damage || '—' }}</span>
              </button>
            }
          </div>
        }

        @if (matchState.isMyTurn() && player.activePokemon; as act) {
          @let rInfo = retreatInfo();
          @if (rInfo) {
            <div class="section-label">RETIRADA</div>
            <div class="retreat-row">
              <div class="retreat-cost">
                @for (e of rInfo.cost; track $index) {
                  <img [src]="e | energyIcon" alt="{{ e }}" class="cost-icon"
                    [class.met]="isEnergyMet(rInfo.cost, $index)"
                    [class.unmet]="!isEnergyMet(rInfo.cost, $index)" />
                }
                @if (rInfo.cost.length === 0) {
                  <span class="retreat-free-text">Gratis</span>
                }
              </div>
              <button
                class="btn btn-retreat"
                [class.available]="rInfo.canRetreat"
                [class.unavailable]="!rInfo.canRetreat"
                [disabled]="interaction.isSelecting() || interaction.actionInProgress() || !rInfo.canRetreat"
                [title]="!rInfo.canRetreat ? (rInfo.hasBench ? 'Energía insuficiente' : 'Sin Pokémon en banca') : ''"
                (click)="selectRetreat()"
              >Retirar</button>
            </div>
          }
        }

        <div class="section-label">BANCA</div>
        <app-bench-zone
          [bench]="normalizedBench()"
          [cardDefs]="cardDefs()"
          [isOwn]="true"
          [validTargets]="validTargets()"
          [selectionMode]="selectionMode()"
          [dragEnabled]="dragEnabled()"
          [connectedDropListIds]="connectedDropListIds()"
           (slotClicked)="pokemonClicked.emit($event)"
           (benchDropped)="benchDropped.emit($event)"
            (energyDropped)="onEnergyDropped($event)"
            (evolutionDropped)="onEvolutionDropped($event)"
         />

        <div class="section-label">PREMIOS</div>
        <app-prize-zone [prizeCount]="prizeCount()" [totalPrizeCount]="player.totalPrizeCount ?? 6" [isOwn]="true" />
      </div>
    }
  `,
  styles: [`
    :host { display: block; }
    .player-area { padding: 0.75rem; border: 1px solid #334155; border-radius: 0.5rem; background: #0f172a; }
    .hand-info { margin-bottom: 0.5rem; font-size: 0.875rem; color: #94a3b8; }
    .section-label { font-size: 0.75rem; font-weight: 600; color: #64748b; text-transform: uppercase; margin: 0.75rem 0 0.375rem; }
    .section-label:first-of-type { margin-top: 0; }
    .empty-active-text { display: block; padding: 1rem; text-align: center; color: #64748b; }
    .attack-buttons { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.5rem; }
    .btn {
      padding: 0.5rem 1rem; border-radius: 0.375rem; border: 1px solid #475569;
      background: #0f172a; color: #e2e8f0; font-size: 0.8125rem; font-weight: 600;
      cursor: pointer; transition: background 0.15s, border-color 0.15s; font-family: inherit;
    }
    .btn:disabled { opacity: 0.4; cursor: not-allowed; }
    .btn-attack { background: #991b1b; border-color: #991b1b; color: #fff; }
    .btn-attack.available { background: #991b1b; border-color: #991b1b; }
    .btn-attack.available:hover:not(:disabled) { background: #b91c1c; }
    .btn-attack.unavailable { background: #450a0a; border-color: #450a0a; opacity: 0.5; }
    .attack-cost-row { display: inline-flex; gap: 0.125rem; margin-right: 0.25rem; vertical-align: middle; }
    .cost-icon { width: 1rem; height: 1rem; }
    .cost-icon.met { filter: brightness(1.3); }
    .cost-icon.unmet { filter: grayscale(1) opacity(0.4); }
    .attack-name { margin-right: 0.5rem; }
    .attack-damage { opacity: 0.8; }
    .retreat-row { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem; }
    .retreat-cost { display: inline-flex; gap: 0.125rem; align-items: center; }
    .retreat-free-text { font-size: 0.75rem; color: #94a3b8; }
    .btn-retreat { padding: 0.375rem 0.75rem; border-radius: 0.375rem; border: 1px solid #475569; font-size: 0.75rem; font-weight: 600; cursor: pointer; font-family: inherit; transition: background 0.15s, border-color 0.15s; }
    .btn-retreat.available { background: #6366f1; border-color: #6366f1; color: #fff; }
    .btn-retreat.available:hover:not(:disabled) { background: #818cf8; }
    .btn-retreat.unavailable { background: #1e1b4b; border-color: #1e1b4b; opacity: 0.5; color: #a5b4fc; }
    .btn-retreat:disabled { opacity: 0.4; cursor: not-allowed; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlayerAreaComponent {
  private readonly cardRepo = inject(CardRepositoryService);
  protected readonly matchState = inject(MatchStateService);
  protected readonly interaction = inject(MatchInteractionService);

  readonly playerState = input.required<PublicPlayerStateModel>();
  readonly privateState = input<PrivatePlayerStateModel | null>(null);
  readonly validTargets = input<string[]>([]);
  readonly selectionMode = input<SelectionMode>('NONE');
  readonly dragEnabled = input(false);
  readonly connectedDropListIds = input<string[]>([]);

  readonly pokemonClicked = output<PublicPokemonSlotModel | { benchIndex: number }>();
  readonly benchDropped = output<{ handIndex: number; benchIndex: number }>();
  readonly energyDropped = output<{ handIndex: number; targetInstanceId: string }>();
  readonly evolutionDropped = output<{ handIndex: number; targetInstanceId: string }>();
  readonly attackSelected = output<{ type: GameActionType; payload?: Record<string, unknown> }>();
  readonly retreatSelected = output<void>();

  readonly cardDefs = computed(() => {
    const player = this.playerState();
    const map = new Map<string, CardDetailResponse | null>();
    const addCard = (poke: PublicPokemonSlotModel | null) => {
      if (!poke) return;
      map.set(poke.instanceId, this.cardRepo.getFromCache(poke.cardId));
    };
    addCard(player.activePokemon);
    for (const poke of player.bench) addCard(poke);
    return map;
  });

  readonly prizeCount = computed(() => this.playerState().prizes.length);

  readonly normalizedBench = computed<(PublicPokemonSlotModel | null)[]>(() => {
    const bench = this.playerState().bench;
    const result: (PublicPokemonSlotModel | null)[] = [...bench];
    while (result.length < 5) result.push(null);
    return result;
  });

  protected readonly attacksWithAvailability = computed(() => {
    const active = this.playerState()?.activePokemon;
    if (!active) return [];
    const cardDef = this.cardDefs().get(active.instanceId);
    const attacks = cardDef?.attacks ?? [];
    const available = this.matchState.activePokemonEnergyTypes();
    console.warn(`[DEBUG] attacksWithAvailability: active=${active.instanceId}, cardDef=${cardDef?.name}, ` +
      `attacks=${attacks.length}, available=[${available.join(',')}]`);
    return attacks.map(a => {
      const met = this.checkCost(a.cost, available);
      console.warn(`[DEBUG] attack idx=${a.index} name="${a.name}" cost=[${a.cost}] available=[${available}] result=${met}`);
      return { ...a, met };
    });
  });

  protected checkCost(cost: EnergyType[], available: EnergyType[]): boolean {
    if (!cost || cost.length === 0) return true;
    if (!available || available.length === 0) return false;
    const remaining = available.map(e => e.toUpperCase());
    let colorsNeeded = 0;
    for (const e of cost) {
      const upper = e.toUpperCase();
      if (upper === 'COLORLESS') { colorsNeeded++; }
      else {
        const idx = remaining.indexOf(upper);
        if (idx === -1) return false;
        remaining.splice(idx, 1);
      }
    }
    return remaining.length >= colorsNeeded;
  }

  protected isEnergyMet(cost: EnergyType[], index: number): boolean {
    const available = this.matchState.activePokemonEnergyTypes();
    if (!cost || index >= cost.length) return true;
    const seen = cost.slice(0, index + 1);
    const needed = seen.filter(e => e.toUpperCase() !== 'COLORLESS');
    const neededColors = seen.filter(e => e.toUpperCase() === 'COLORLESS').length;
    const remaining = available.map(e => e.toUpperCase());
    for (const n of needed) {
      const idx = remaining.indexOf(n.toUpperCase());
      if (idx !== -1) remaining.splice(idx, 1);
    }
    return remaining.length >= neededColors;
  }

  protected selectAttack(attackIndex: number): void {
    const targetId = this.matchState.opponentActivePokemon()?.instanceId;
    if (!targetId) return;
    this.attackSelected.emit({
      type: 'DECLARE_ATTACK',
      payload: { attackIndex, targetPokemonInstanceId: targetId },
    });
  }

  protected readonly retreatInfo = computed(() => {
    const active = this.playerState()?.activePokemon;
    if (!active) return null;
    const cost = this.matchState.activePokemonRetreatCost();
    const available = this.matchState.activePokemonEnergyTypes();
    const hasBench = this.playerState().bench.length > 0;
    const met = this.checkCost(cost, available);
    return { cost, met, hasBench, canRetreat: met && hasBench };
  });

  protected selectRetreat(): void {
    const info = this.retreatInfo();
    if (!info?.canRetreat) return;
    this.retreatSelected.emit();
  }

  protected onPokemonClicked(pokemon: PublicPokemonSlotModel): void {
    this.pokemonClicked.emit(pokemon);
  }

  protected onEnergyDropped(event: { handIndex: number; targetInstanceId: string }): void {
    this.energyDropped.emit(event);
  }

  protected onEvolutionDropped(event: { handIndex: number; targetInstanceId: string }): void {
    this.evolutionDropped.emit(event);
  }

  protected onActiveDrop(event: CdkDragDrop<unknown>): void {
    const data = event.item.data as { handIndex: number; supertype: string } | undefined;
    if (!data) return;
    const active = this.playerState()?.activePokemon;
    if (!active) return;
    if (data.supertype === 'ENERGY') {
      this.energyDropped.emit({ handIndex: data.handIndex, targetInstanceId: active.instanceId });
    } else if (data.supertype === 'POKEMON') {
      this.evolutionDropped.emit({ handIndex: data.handIndex, targetInstanceId: active.instanceId });
    }
  }
}

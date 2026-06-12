import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { PublicPlayerStateModel, PublicPokemonSlotModel } from '../../../../shared/models/game-state.models';
import { CardDetailResponse } from '../../../../shared/models/card.models';
import { CardRepositoryService } from '../../../../core/services/card-repository.service';
import { PokemonSlotComponent } from '../pokemon-slot/pokemon-slot.component';
import { BenchZoneComponent } from '../bench-zone/bench-zone.component';
import { PrizeZoneComponent } from '../prize-zone/prize-zone.component';
import { SelectionMode } from '../../../../shared/models/ui-state.models';

@Component({
  selector: 'app-opponent-area',
  standalone: true,
  imports: [PokemonSlotComponent, BenchZoneComponent, PrizeZoneComponent],
  template: `
    @if (playerState(); as player) {
      <div class="opponent-area">
        <div class="section-label">OPONENTE</div>

        <div class="section-sub-label">PREMIOS</div>
        <app-prize-zone [prizeCount]="prizeCount()" [totalPrizeCount]="player.totalPrizeCount ?? 6" [isOwn]="false" />

        <div class="section-sub-label">BANCA</div>
        <app-bench-zone
          [bench]="normalizedBench()"
          [cardDefs]="cardDefs()"
          [isOwn]="false"
          [validTargets]="validTargets()"
          [selectionMode]="selectionMode()"
          (slotClicked)="pokemonClicked.emit($event)"
        />

        <div class="section-sub-label">ACTIVO</div>
        @if (player.activePokemon; as active) {
          <app-pokemon-slot
            [pokemon]="active"
            [cardDef]="cardDefs().get(active.instanceId) ?? null"
            [isActive]="true"
            [isOwn]="false"
            [isHighlighted]="validTargets().includes(active.instanceId)"
            (slotClicked)="onPokemonClicked($event)"
          />
        } @else {
          <div class="empty-active">Sin Pokémon activo</div>
        }
      </div>
    }
  `,
  styles: [`
    :host { display: block; }
    .opponent-area { padding: 0.75rem; border: 1px solid #334155; border-radius: 0.5rem; background: #0f172a; }
    .section-label { font-size: 0.75rem; font-weight: 600; color: #facc15; text-transform: uppercase; margin-bottom: 0.5rem; }
    .section-sub-label { font-size: 0.75rem; font-weight: 600; color: #64748b; text-transform: uppercase; margin: 0.75rem 0 0.375rem; }
    .section-sub-label:first-of-type { margin-top: 0.5rem; }
    .empty-active { padding: 1rem; text-align: center; color: #64748b; border: 1px dashed #475569; border-radius: 0.5rem; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OpponentAreaComponent {
  private readonly cardRepo = inject(CardRepositoryService);

  readonly playerState = input.required<PublicPlayerStateModel>();
  readonly validTargets = input<string[]>([]);
  readonly selectionMode = input<SelectionMode>('NONE');

  readonly pokemonClicked = output<PublicPokemonSlotModel | { benchIndex: number }>();

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

  protected onPokemonClicked(pokemon: PublicPokemonSlotModel): void {
    this.pokemonClicked.emit(pokemon);
  }
}

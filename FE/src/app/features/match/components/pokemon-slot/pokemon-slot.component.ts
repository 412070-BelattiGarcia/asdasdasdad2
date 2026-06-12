import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { PublicPokemonSlotModel } from '../../../../shared/models/game-state.models';
import { CardDetailResponse } from '../../../../shared/models/card.models';
import { CardImagePipe } from '../../../../shared/pipes/card-image.pipe';
import { ConditionIconPipe } from '../../../../shared/pipes/condition-icon.pipe';
import { EnergyIconPipe } from '../../../../shared/pipes/energy-icon.pipe';

@Component({
  selector: 'app-pokemon-slot',
  standalone: true,
  imports: [CardImagePipe, ConditionIconPipe, EnergyIconPipe],
  host: { '(click)': 'onClick()' },
  template: `
    @let card = cardDef();
    <div
      class="pokemon-slot"
      [class.active-border]="isActive()"
      [class.highlighted]="isHighlighted() && !isActive()"
      [class.opponent]="!isOwn()"
      [class.loading]="!card"
      [class.evolved-this-turn]="pokemon().evolvedThisTurn === true"
    >
      <div class="slot-body">
        <div class="card-img-wrapper">
          <img [src]="pokemon().cardId | cardImage" alt="{{ card?.name ?? pokemon().cardId }}" class="card-img" />
          @if (pokemon().evolvedThisTurn === true) {
            <div class="evolution-badge">EVO</div>
          }
        </div>
        @if (pokemon().attachedCards.length > 0) {
          <div class="energy-area">
            <span class="energy-label">Energías</span>
            <div class="energy-row">
              @for (energy of pokemon().attachedCards; track $index) {
                <div class="energy-token">
                  <img [src]="energy | energyIcon" alt="{{ energy }}" class="energy-icon" />
                </div>
              }
            </div>
          </div>
        }
        <div class="info">
          <p class="name">{{ card?.name ?? pokemon().cardId }}</p>
          @if (card?.hp != null) {
            <div class="hp-row">
              <span class="hp">HP: {{ (card!.hp! - pokemon().damageCounters) }}/{{ card!.hp }}</span>
              <div class="hp-bar">
                <div class="hp-fill" [style.width.%]="hpPercent()" [class.green]="hpPercent() > 50" [class.yellow]="hpPercent() > 25 && hpPercent() <= 50" [class.red]="hpPercent() <= 25"></div>
              </div>
            </div>
          }
          <div class="conditions-row">
            @for (condition of pokemon().specialConditions; track $index) {
              <img [src]="condition | conditionIcon" alt="{{ condition }}" class="condition-icon" />
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; cursor: pointer; }
    .pokemon-slot { border: 2px solid #334155; border-radius: 0.5rem; padding: 0.5rem; background: #1e293b; transition: border-color 0.15s; }
    .pokemon-slot.active-border { border-color: #f59e0b; border-width: 3px; }
    .pokemon-slot.highlighted { border-color: #06b6d4; border-style: dashed; border-width: 2px; }
    .pokemon-slot.opponent { opacity: 0.85; transform: scaleX(-1); }
    .pokemon-slot.loading { opacity: 0.6; display: flex; align-items: center; justify-content: center; min-height: 150px; }
    .pokemon-slot.opponent .info { transform: scaleX(-1); }
    .pokemon-slot.opponent .energy-area { transform: scaleX(-1); }
    .slot-body { display: flex; flex-direction: column; align-items: center; gap: 0.5rem; }
    .card-img { width: 100%; max-width: 130px; aspect-ratio: 3/4; object-fit: cover; border-radius: 0.25rem; background: #334155; display: block; margin: 0 auto; }
    .energy-area { width: 100%; }
    .energy-label { display: block; font-size: 0.625rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.25rem; }
    .energy-row { display: flex; gap: 0.375rem; flex-wrap: wrap; }
    .energy-token { width: 2.5rem; height: 2.5rem; border-radius: 0.375rem; background: #0f172a; border: 1px solid #475569; display: flex; align-items: center; justify-content: center; }
    .energy-icon { width: 1.5rem; height: 1.5rem; }
    .info { width: 100%; }
    .name { margin: 0; font-size: 0.75rem; font-weight: 600; color: #f1f5f9; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .hp-row { display: flex; align-items: center; gap: 0.5rem; margin-top: 0.25rem; }
    .hp { font-size: 0.6875rem; color: #94a3b8; white-space: nowrap; }
    .hp-bar { flex: 1; height: 0.375rem; background: #334155; border-radius: 0.1875rem; overflow: hidden; }
    .hp-fill { height: 100%; border-radius: 0.1875rem; transition: width 0.2s; }
    .hp-fill.green { background: #22c55e; }
    .hp-fill.yellow { background: #eab308; }
    .hp-fill.red { background: #ef4444; }
    .conditions-row { display: flex; gap: 0.25rem; margin-top: 0.25rem; flex-wrap: wrap; }
    .condition-icon { width: 1rem; height: 1rem; }
    .pokemon-slot.evolved-this-turn { border-color: #7c3aed; border-width: 3px; }
    .card-img-wrapper { position: relative; width: 100%; max-width: 130px; margin: 0 auto; }
    .evolution-badge {
      position: absolute; top: -4px; right: -4px;
      background: #7c3aed; color: #fff;
      font-size: 0.5625rem; font-weight: 800;
      padding: 2px 5px; border-radius: 0.25rem;
      border: 1px solid #a78bfa;
      line-height: 1;
      z-index: 10;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PokemonSlotComponent {
  readonly pokemon = input.required<PublicPokemonSlotModel>();
  readonly cardDef = input<CardDetailResponse | null>(null);
  readonly isActive = input(false);
  readonly isOwn = input(true);
  readonly isHighlighted = input(false);
  readonly isBenchSlot = input(false);

  readonly slotClicked = output<PublicPokemonSlotModel>();

  readonly hpPercent = computed(() => {
    const card = this.cardDef();
    const poke = this.pokemon();
    if (!card?.hp) return 100;
    const maxHp = card.hp;
    const currentHp = maxHp - poke.damageCounters;
    return Math.max(0, (currentHp / maxHp) * 100);
  });

  onClick(): void {
    if (!this.cardDef()) return;
    this.slotClicked.emit(this.pokemon());
  }
}

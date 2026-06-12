import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DragDropModule, CdkDragDrop } from '@angular/cdk/drag-drop';
import { PublicPokemonSlotModel } from '../../../../shared/models/game-state.models';
import { CardDetailResponse } from '../../../../shared/models/card.models';
import { PokemonSlotComponent } from '../pokemon-slot/pokemon-slot.component';
import { SelectionMode } from '../../../../shared/models/ui-state.models';

@Component({
  selector: 'app-bench-zone',
  standalone: true,
  imports: [DragDropModule, PokemonSlotComponent],
  template: `
    <div class="bench-zone">
      @for (slot of bench(); track $index; let idx = $index) {
        <div
          class="bench-slot-wrapper"
          cdkDropList
          [id]="'bench-slot-' + idx"
          [cdkDropListConnectedTo]="connectedDropListIds()"
          [cdkDropListDisabled]="!dragEnabled()"
          (cdkDropListDropped)="onDrop($event, idx, slot)"
        >
          @if (slot; as poke) {
            <app-pokemon-slot
              [pokemon]="poke"
              [cardDef]="cardDefs().get(poke.instanceId) ?? null"
              [isOwn]="isOwn()"
              [isHighlighted]="validTargets().includes(poke.instanceId)"
              [isBenchSlot]="true"
              (slotClicked)="onOccupiedClick(idx, $event)"
            />
          } @else {
            <div
              class="bench-slot empty"
              [class.clickable]="selectionMode() === 'SELECT_BENCH_SLOT'"
              (click)="onEmptyClick(idx)"
            >
              —
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .bench-zone { display: flex; gap: 0.5rem; }
    .bench-slot-wrapper { flex: 1; min-width: 0; }
    .bench-slot-wrapper.cdk-drop-list-dragover .bench-slot.empty {
      border-color: #22d3ee; background: #1e293b;
    }
    .bench-slot {
      border: 2px dashed #475569; border-radius: 0.5rem;
      display: flex; align-items: center; justify-content: center;
      min-height: 150px; color: #64748b; font-size: 1.5rem;
    }
    .bench-slot.empty.clickable { border-color: #06b6d4; cursor: pointer; }
    .bench-slot.empty.clickable:hover { border-color: #22d3ee; background: #1e293b; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BenchZoneComponent {
  readonly bench = input.required<(PublicPokemonSlotModel | null)[]>();
  readonly cardDefs = input<Map<string, CardDetailResponse | null>>(new Map());
  readonly isOwn = input(false);
  readonly validTargets = input<string[]>([]);
  readonly selectionMode = input<SelectionMode>('NONE');
  readonly dragEnabled = input(false);
  readonly connectedDropListIds = input<string[]>([]);

  readonly slotClicked = output<{ benchIndex: number }>();
  readonly benchDropped = output<{ handIndex: number; benchIndex: number }>();
  readonly energyDropped = output<{ handIndex: number; targetInstanceId: string }>();
  readonly evolutionDropped = output<{ handIndex: number; targetInstanceId: string }>();

  protected onDrop(event: CdkDragDrop<unknown>, benchIndex: number, slot: PublicPokemonSlotModel | null): void {
    const data = event.item.data as { handIndex: number; supertype: string } | undefined;
    if (!data || typeof data.handIndex !== 'number') return;
    if (data.supertype === 'ENERGY') {
      if (!slot) return;
      this.energyDropped.emit({ handIndex: data.handIndex, targetInstanceId: slot.instanceId });
    } else if (data.supertype === 'POKEMON') {
      if (slot) {
        this.evolutionDropped.emit({ handIndex: data.handIndex, targetInstanceId: slot.instanceId });
      } else {
        this.benchDropped.emit({ handIndex: data.handIndex, benchIndex });
      }
    }
  }

  protected onOccupiedClick(index: number, _pokemon: PublicPokemonSlotModel): void {
    console.warn(`[DEBUG] onOccupiedClick: idx=${index}, pokemon.instanceId=${_pokemon.instanceId}, pokemon.cardId=${_pokemon.cardId}`);
    this.slotClicked.emit({ benchIndex: index });
  }

  protected onEmptyClick(index: number): void {
    if (this.selectionMode() === 'SELECT_BENCH_SLOT') {
      this.slotClicked.emit({ benchIndex: index });
    }
  }
}

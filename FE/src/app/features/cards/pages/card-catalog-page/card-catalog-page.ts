import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CardCatalogFacadeService } from '../../services/card-catalog-facade.service';
import { SearchBarComponent } from '../../../../shared/components/search-bar/search-bar.component';
import { CardFilterComponent, FilterOption } from '../../../../shared/components/card-filter/card-filter.component';
import { CardViewComponent } from '../../../../shared/components/card-view/card-view.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';

@Component({
  selector: 'app-card-catalog-page',
  imports: [
    RouterLink,
    SearchBarComponent,
    CardFilterComponent,
    CardViewComponent,
    PaginationComponent,
    LoadingSpinnerComponent,
    ButtonComponent,
  ],
  template: `
    <main class="p-6">
      <h1 class="mb-6 text-2xl font-bold">Catálogo de Cartas</h1>

      <div class="mb-6 flex flex-col gap-4 sm:flex-row">
        <div class="flex-1">
          <app-search-bar
            placeholder="Buscar por nombre..."
            (queryChange)="facade.setQuery($event)"
          />
        </div>
        <div class="w-full sm:w-48">
          <app-card-filter
            [options]="filterOptions"
            [selected]="facade.supertype()"
            (filterChange)="facade.setSupertype($event)"
          />
        </div>
      </div>

      @if (facade.loading()) {
        <app-loading-spinner />
      } @else if (facade.error()) {
        <div class="flex flex-col items-center gap-4 py-12">
          <p class="text-red-600">{{ facade.error() }}</p>
          <app-button variant="secondary" (click)="facade.search()">Reintentar</app-button>
        </div>
      } @else if (facade.cards().length === 0) {
        <div class="py-12 text-center">
          <p class="text-gray-500">No se encontraron cartas</p>
        </div>
      } @else {
        <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
          @for (card of facade.cards(); track card.id) {
            <a [routerLink]="['/cards', card.id]" class="block transition-opacity hover:opacity-80">
              <app-card-view [card]="card" />
            </a>
          }
        </div>

        <div class="mt-8 flex justify-center">
          <app-pagination
            [currentPage]="facade.page()"
            [totalPages]="facade.totalPages()"
            (pageChange)="facade.setPage($event)"
          />
        </div>
      }
    </main>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardCatalogPage {
  protected readonly facade = inject(CardCatalogFacadeService);

  protected readonly filterOptions: FilterOption[] = [
    { label: 'Todas', value: '' },
    { label: 'Pokemon', value: 'POKEMON' },
    { label: 'Energía', value: 'ENERGY' },
    { label: 'Entrenador', value: 'TRAINER' },
  ];

  constructor() {
    this.facade.search();
  }
}

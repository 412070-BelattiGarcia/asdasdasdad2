import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  template: `
    @if (totalPages() > 1) {
      <nav class="flex items-center gap-1">
        <button
          class="rounded-lg border bg-white px-3 py-2 text-sm hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
          [disabled]="currentPage() === 0"
          (click)="goToPage(currentPage() - 1)"
        >
          Anterior
        </button>

        @for (page of visiblePages(); track page) {
          <button
            class="rounded-lg border px-3 py-2 text-sm"
            [class]="page === currentPage() ? 'bg-blue-600 text-white' : 'bg-white hover:bg-gray-100'"
            (click)="goToPage(page)"
          >
            {{ page + 1 }}
          </button>
        }

        <button
          class="rounded-lg border bg-white px-3 py-2 text-sm hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
          [disabled]="currentPage() === totalPages() - 1"
          (click)="goToPage(currentPage() + 1)"
        >
          Siguiente
        </button>
      </nav>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginationComponent {
  currentPage = input.required<number>();
  totalPages = input.required<number>();
  pageChange = output<number>();

  readonly visiblePages = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    const windowStart = Math.max(0, current - 2);
    const windowEnd = Math.min(total, windowStart + 5);
    return Array.from({ length: windowEnd - windowStart }, (_, i) => windowStart + i);
  });

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.currentPage()) {
      this.pageChange.emit(page);
    }
  }
}

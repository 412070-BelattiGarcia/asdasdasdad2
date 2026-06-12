import {
  Component,
  HostListener,
  OnInit,
  OnDestroy,
  inject,
  input,
  signal,
  computed,
} from '@angular/core';
import { Router, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';

export interface MenuCard {
  label: string;
  route: string;
  subtitle: () => string | null;
}

const SPLASH_KEY    = 'splash-seen';
const IDLE_MS       = 3 * 60 * 1000;

@Component({
  selector: 'app-home-page',
  standalone: true,
  templateUrl: './home-page.html',
  styleUrls: ['./home-page.css'],
})
export class HomePage implements OnInit, OnDestroy {

  bgImage     = input<string>('assets/images/bg-lavender-town.jpeg');
  gameVersion = input<string>('Version Grupo-08');
  footerText  = input<string>('© 2026 PokeStudios. Todos los derechos reservados.\n' +
    'Pokémon es una marca registrada de Nintendo / The Pokémon Company.\n');

  showSplash    = signal<boolean>(!localStorage.getItem(SPLASH_KEY));
  transitioning = signal<boolean>(false);

  displayName  = signal<string>('');
  deckCount    = signal<number | null>(null);
  cardCount    = signal<number | null>(null);
  loadingStats = signal<boolean>(false);
  statsError   = signal<boolean>(false);

  menuCards = computed<MenuCard[]>(() => [
    {
      label:    'JUGAR',
      route:    '/lobby',
      subtitle: () => 'Encontra o crea una partida',
    },
    {
      label:    'MIS MAZOS',
      route:    '/decks',
      subtitle: () =>
        this.deckCount() !== null ? `${this.deckCount()} mazos guardados` : null,
    },
    {
      label:    'CATALOGO',
      route:    '/cards',
      subtitle: () =>
        this.cardCount() !== null ? `${this.cardCount()} cartas disponibles` : null,
    },
    {
      label:    'RANKING',
      route:    '/ranking',
      subtitle: () => 'Tabla de posiciones',
    },
  ]);

  private readonly router = inject(Router);
  private idleTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadStats();
    if (!this.showSplash()) {
      this.startIdleTimer();
    }
    this.router.events.pipe(
      filter(e => e instanceof NavigationStart)
    ).subscribe(() => {
      localStorage.setItem(SPLASH_KEY, '1');
    });
  }


  ngOnDestroy(): void {
    this.clearIdleTimer();
  }

  @HostListener('window:keydown')
  onKeyDown(): void {
    this.resetIdleTimer();
    this.triggerTransition();
  }

  @HostListener('window:mousemove')
  @HostListener('window:pointerdown')
  onActivity(): void {
    this.resetIdleTimer();
  }

  @HostListener('click')
  onClick(): void {
    this.resetIdleTimer();
    this.triggerTransition();
  }

  private triggerTransition(): void {
    if (!this.showSplash() || this.transitioning()) return;
    this.transitioning.set(true);
    localStorage.setItem(SPLASH_KEY, '1');
    setTimeout(() => {
      this.showSplash.set(false);
      this.transitioning.set(false);
      this.startIdleTimer();
    }, 350);
  }

  private startIdleTimer(): void {
    this.clearIdleTimer();
    this.idleTimer = setTimeout(() => {
      localStorage.removeItem(SPLASH_KEY);
      this.showSplash.set(true);
    }, IDLE_MS);
  }

  private resetIdleTimer(): void {
    if (!this.showSplash()) {
      this.startIdleTimer();
    }
  }

  private clearIdleTimer(): void {
    if (this.idleTimer !== null) {
      clearTimeout(this.idleTimer);
      this.idleTimer = null;
    }
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    localStorage.removeItem(SPLASH_KEY);
    this.clearIdleTimer();
    this.router.navigate(['/auth/login']);
  }

  private loadUserInfo(): void {
    this.displayName.set('Entrenador');
  }

  loadStats(): void {
    this.loadingStats.set(true);
    this.statsError.set(false);

    setTimeout(() => {
      try {
        this.deckCount.set(3);
        this.cardCount.set(251);
        this.loadingStats.set(false);
      } catch {
        this.statsError.set(true);
        this.loadingStats.set(false);
      }
    }, 800);
  }
}

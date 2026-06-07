import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { UserApiService } from '../api/user-api.service';
import { PlayerApiService } from '../api/player-api.service';
import { CreateUserRequest, UserResponse } from '../../shared/models/user.models';
import { PlayerResponse } from '../../shared/models/player.models';

const AUTH_STORAGE_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userApi = inject(UserApiService);
  private readonly playerApi = inject(PlayerApiService);

  private readonly _user = signal<UserResponse | null>(null);
  private readonly _player = signal<PlayerResponse | null>(null);
  private readonly _isAuthenticated = signal(false);

  readonly user = this._user.asReadonly();
  readonly player = this._player.asReadonly();
  readonly playerId = computed(() => this._player()?.id ?? null);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  constructor() {
    this.loadState();
  }

  register(request: CreateUserRequest): Observable<UserResponse> {
    return this.userApi.register(request).pipe(
      tap((user) => {
        this._user.set(user);
        this._isAuthenticated.set(true);
        this.loadPlayer(user.playerId);
        this.saveState(user);
      }),
    );
  }

  login(email: string, password: string): Observable<UserResponse> {
    return this.userApi.login({ email, password }).pipe(
      tap((user) => {
        this._user.set(user);
        this._isAuthenticated.set(true);
        this.loadPlayer(user.playerId);
        this.saveState(user);
      }),
    );
  }

  logout(): void {
    this._user.set(null);
    this._player.set(null);
    this._isAuthenticated.set(false);
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }

  loadPlayer(playerId: string): void {
    this.playerApi.getById(playerId).subscribe({
      next: (player) => this._player.set(player),
      error: () => this._player.set(null),
    });
  }

  private saveState(user: UserResponse): void {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
  }

  private loadState(): void {
    try {
      const stored = localStorage.getItem(AUTH_STORAGE_KEY);
      if (stored) {
        const user: UserResponse = JSON.parse(stored);
        this._user.set(user);
        this._isAuthenticated.set(true);
        this.loadPlayer(user.playerId);
      }
    } catch {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }
}

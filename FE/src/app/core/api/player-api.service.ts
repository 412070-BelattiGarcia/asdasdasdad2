import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { PlayerResponse, UpdatePlayerRequest } from '../../shared/models/player.models';

@Injectable({ providedIn: 'root' })
export class PlayerApiService {
  private readonly apiClient = inject(ApiClientService);

  listAll(): Observable<PlayerResponse[]> {
    return this.apiClient.get<PlayerResponse[]>('/players');
  }

  getById(id: string): Observable<PlayerResponse> {
    return this.apiClient.get<PlayerResponse>(`/players/${id}`);
  }

  update(id: string, request: UpdatePlayerRequest): Observable<PlayerResponse> {
    return this.apiClient.put<PlayerResponse>(`/players/${id}`, request);
  }
}

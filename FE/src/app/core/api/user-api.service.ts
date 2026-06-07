import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { CreateUserRequest, LoginRequest, UserResponse } from '../../shared/models/user.models';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly apiClient = inject(ApiClientService);

  register(request: CreateUserRequest): Observable<UserResponse> {
    return this.apiClient.post<UserResponse>('/users/register', request);
  }

  login(request: LoginRequest): Observable<UserResponse> {
    return this.apiClient.post<UserResponse>('/users/login', request);
  }

  getById(id: string): Observable<UserResponse> {
    return this.apiClient.get<UserResponse>(`/users/${id}`);
  }

  listAll(): Observable<UserResponse[]> {
    return this.apiClient.get<UserResponse[]>('/users');
  }
}

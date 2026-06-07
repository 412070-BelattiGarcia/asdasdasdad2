export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
  playerId: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

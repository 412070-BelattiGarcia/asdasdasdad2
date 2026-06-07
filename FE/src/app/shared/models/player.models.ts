export interface PlayerResponse {
  id: string;
  displayName: string;
  userId: string;
  createdAt: string;
}

export interface UpdatePlayerRequest {
  displayName: string;
}

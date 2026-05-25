import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/decks', pathMatch: 'full' },
  {
    path: 'cards',
    loadChildren: () => import('./features/cards/routes').then((m) => m.cardRoutes),
  },
  {
    path: 'decks',
    loadChildren: () => import('./features/decks/routes').then((m) => m.deckRoutes),
  },
  {
    path: 'lobby',
    loadChildren: () => import('./features/lobby/routes').then((m) => m.lobbyRoutes),
  },
  {
    path: 'match',
    loadChildren: () => import('./features/match/routes').then((m) => m.matchRoutes),
  },
  { path: '**', redirectTo: '/decks' },
];

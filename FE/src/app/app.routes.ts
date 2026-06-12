import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
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
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/routes').then((m) => m.authRoutes),
  },
  {
    path: 'home',
    loadChildren: () => import('./features/home/routes').then((m) => m.homeRoutes),
  },
  {
    path: 'ranking',
    loadChildren: () => import('./features/ranking/routes').then((m) => m.rankingRoutes),
  },
  { path: '**', redirectTo: '/home' },
];

export type SelectionMode =
  | 'NONE'
  | 'SELECT_BENCH_SLOT'
  | 'SELECT_TARGET_POKEMON'
  | 'SELECT_ATTACK'
  | 'SELECT_RETREAT_TARGET'
  | 'SELECT_ENERGIES_TO_DISCARD'
  | 'SETUP_ACTIVE'
  | 'SETUP_BENCH';

export interface SelectionState {
  mode: SelectionMode;
  selectedHandIndex: number | null;
  selectedInstanceId: string | null;
  validTargets: string[];
}

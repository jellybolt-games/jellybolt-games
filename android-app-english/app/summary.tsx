import { useLocalSearchParams, useRouter } from 'expo-router';

import { DEFAULT_SET_LENGTH } from '../src/game/rounds';
import { SetSummary } from '../src/screens/SetSummary';
import type { RoundMode } from '../src/screens/RoundResult';

type SummaryParams = {
  mode?: RoundMode;
  stars?: string;
  practiced?: string;
  correctCount?: string;
  bestStreak?: string;
};

export default function SummaryRoute() {
  const router = useRouter();
  const params = useLocalSearchParams<SummaryParams>();
  const mode = coerceMode(params.mode);
  const stars = parseParam(params.stars, 1);
  const practiced = parseParam(params.practiced, DEFAULT_SET_LENGTH);
  const correctCount = parseParam(params.correctCount, 0);
  const bestStreak = parseParam(params.bestStreak, 0);

  return (
    <SetSummary
      stars={stars}
      practiced={practiced}
      correctCount={correctCount}
      bestStreak={bestStreak}
      onPlayAgain={() => router.replace(routeForMode(mode))}
      onHome={() => router.replace('/')}
    />
  );
}

function coerceMode(mode: RoundMode | undefined): RoundMode {
  if (mode === 'speak' || mode === 'numbers' || mode === 'letters') {
    return mode;
  }

  return 'choice';
}

function routeForMode(mode: RoundMode): '/choice' | '/speak' | '/numbers' | '/letters' {
  switch (mode) {
    case 'speak':
      return '/speak';
    case 'numbers':
      return '/numbers';
    case 'letters':
      return '/letters';
    case 'choice':
    default:
      return '/choice';
  }
}

function parseParam(value: string | string[] | undefined, fallback: number): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

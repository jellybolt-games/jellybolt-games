import { useLocalSearchParams, useRouter } from 'expo-router';

import { DEFAULT_SET_LENGTH, calculateSetStars } from '../src/game/rounds';
import { RoundResult, type ResultOutcome, type RoundMode } from '../src/screens/RoundResult';

type ResultParams = {
  mode?: RoundMode;
  outcome?: ResultOutcome;
  word?: string;
  correctAnswer?: string;
  wasCorrect?: string;
  nextIndex?: string;
  correctCount?: string;
  currentStreak?: string;
  bestStreak?: string;
};

export default function ResultRoute() {
  const router = useRouter();
  const params = useLocalSearchParams<ResultParams>();
  const mode = coerceMode(params.mode);
  const outcome = coerceOutcome(params.outcome, params.wasCorrect);
  const nextIndex = parseParam(params.nextIndex, 1);
  const correctCount = parseParam(params.correctCount, outcome === 'correct' ? 1 : 0);
  const currentStreak = parseParam(params.currentStreak, outcome === 'correct' ? 1 : 0);
  const bestStreak = parseParam(params.bestStreak, currentStreak);

  const onNext = () => {
    if (nextIndex >= DEFAULT_SET_LENGTH) {
      router.replace({
        pathname: '/summary',
        params: {
          mode,
          practiced: String(DEFAULT_SET_LENGTH),
          correctCount: String(correctCount),
          bestStreak: String(bestStreak),
          stars: String(calculateSetStars(correctCount, DEFAULT_SET_LENGTH)),
        },
      });
      return;
    }

    router.replace({
      pathname: routeForMode(mode),
      params: {
        index: String(nextIndex),
        correctCount: String(correctCount),
        currentStreak: String(currentStreak),
        bestStreak: String(bestStreak),
      },
    });
  };

  return (
    <RoundResult
      mode={mode}
      word={params.word ?? ''}
      correctAnswer={params.correctAnswer ?? ''}
      outcome={outcome}
      onNext={onNext}
    />
  );
}

function coerceMode(mode: RoundMode | undefined): RoundMode {
  if (mode === 'speak' || mode === 'numbers' || mode === 'letters') {
    return mode;
  }

  return 'choice';
}

function coerceOutcome(outcome: ResultOutcome | undefined, wasCorrect: string | undefined): ResultOutcome {
  if (outcome === 'practice' || outcome === 'wrong' || outcome === 'correct') {
    return outcome;
  }

  return wasCorrect === 'true' ? 'correct' : 'wrong';
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

export const DEFAULT_SET_LENGTH = 10;

export type RandomSource = () => number;

export type RoundStats = {
  index: number;
  correctCount: number;
  currentStreak: number;
  bestStreak: number;
};

export type SessionPicker<T extends { id: string }> = {
  next: () => T;
  optionsFor: (correctItem: T, getAnswer: (item: T) => string, count?: number) => string[];
};

export function createSessionPicker<T extends { id: string }>(items: readonly T[], random: RandomSource = Math.random): SessionPicker<T> {
  if (items.length === 0) {
    throw new Error('Cannot create a session picker for an empty list.');
  }

  let deck = shuffle(items, random);
  let cursor = 0;
  let previousId: string | undefined;

  const reshuffle = () => {
    deck = shuffle(items, random);
    cursor = 0;

    if (previousId && deck.length > 1 && deck[0]?.id === previousId) {
      [deck[0], deck[1]] = [deck[1], deck[0]];
    }
  };

  return {
    next(): T {
      if (cursor >= deck.length) {
        reshuffle();
      }

      const item = deck[cursor] ?? deck[0];
      cursor += 1;
      previousId = item.id;
      return item;
    },
    optionsFor(correctItem: T, getAnswer: (item: T) => string, count = 4): string[] {
      const correctAnswer = getAnswer(correctItem);
      const distractors = unique(
        shuffle(
          items
            .filter((item) => item.id !== correctItem.id)
            .map(getAnswer)
            .filter((answer) => answer !== correctAnswer),
          random,
        ),
      ).slice(0, Math.max(0, count - 1));

      return shuffle([correctAnswer, ...distractors], random);
    },
  };
}

export function parseRoundStats(params: Partial<Record<string, string | string[]>>): RoundStats {
  return {
    index: parseNumberParam(params.index, 0),
    correctCount: parseNumberParam(params.correctCount, 0),
    currentStreak: parseNumberParam(params.currentStreak, 0),
    bestStreak: parseNumberParam(params.bestStreak, 0),
  };
}

export function nextStats(stats: RoundStats, wasCorrect: boolean): RoundStats {
  const currentStreak = wasCorrect ? stats.currentStreak + 1 : 0;

  return {
    index: stats.index + 1,
    correctCount: wasCorrect ? stats.correctCount + 1 : stats.correctCount,
    currentStreak,
    bestStreak: Math.max(stats.bestStreak, currentStreak),
  };
}

export function calculateSetStars(correctCount: number, practicedCount: number): number {
  if (practicedCount <= 0) {
    return 1;
  }

  return Math.max(1, Math.min(3, Math.ceil((correctCount / practicedCount) * 3)));
}

export function shuffle<T>(items: readonly T[], random: RandomSource = Math.random): T[] {
  const copy = [...items];

  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }

  return copy;
}

function unique(items: string[]): string[] {
  return Array.from(new Set(items));
}

function parseNumberParam(value: string | string[] | undefined, fallback: number): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

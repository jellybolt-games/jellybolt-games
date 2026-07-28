export type WordLike = {
  id: string;
  hebrew: string;
};

export type RandomSource = () => number;

export type WordPickerOptions = {
  seed?: number;
  random?: RandomSource;
};

export function createSeededRandom(seed: number): RandomSource {
  let state = seed >>> 0;

  return () => {
    state = (state * 1664525 + 1013904223) >>> 0;
    return state / 0x100000000;
  };
}

export function pickNextWord<T extends { id: string }>(
  words: readonly T[],
  previousId?: string,
  random: RandomSource = Math.random,
): T {
  if (words.length === 0) {
    throw new Error('Cannot pick from an empty word list.');
  }

  if (words.length === 1) {
    return words[0];
  }

  const candidates = words.filter((word) => word.id !== previousId);
  const index = Math.floor(random() * candidates.length);
  return candidates[index] ?? candidates[0];
}

export function pickHebrewOptions<T extends WordLike>(
  words: readonly T[],
  correctWord: T,
  count = 4,
  random: RandomSource = Math.random,
): string[] {
  const wrongAnswers = shuffle(
    words.filter((word) => word.id !== correctWord.id).map((word) => word.hebrew),
    random,
  ).slice(0, Math.max(0, count - 1));

  return shuffle([correctWord.hebrew, ...wrongAnswers], random);
}

export function createWordPicker<T extends WordLike>(
  words: readonly T[],
  options: WordPickerOptions = {},
) {
  const random = options.seed === undefined ? options.random ?? Math.random : createSeededRandom(options.seed);
  let previousId: string | undefined;

  return {
    next(): T {
      const word = pickNextWord(words, previousId, random);
      previousId = word.id;
      return word;
    },
    optionsFor(word: T, count = 4): string[] {
      return pickHebrewOptions(words, word, count, random);
    },
  };
}

function shuffle<T>(items: readonly T[], random: RandomSource): T[] {
  const copy = [...items];

  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }

  return copy;
}

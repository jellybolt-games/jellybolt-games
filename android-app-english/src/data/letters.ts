export const LETTERS = ['A', 'B', 'C', 'D', 'E', 'G', 'H', 'I', 'J', 'L', 'M', 'N', 'O', 'P', 'S', 'T'] as const;

export type Letter = (typeof LETTERS)[number];

export type LetterItem = {
  id: string;
  upper: Letter;
  lower: Lowercase<Letter>;
  ttsText: Letter;
};

export const LETTER_ITEMS: LetterItem[] = LETTERS.map((upper) => ({
  id: `letter-${upper.toLowerCase()}`,
  upper,
  lower: upper.toLowerCase() as Lowercase<Letter>,
  ttsText: upper,
}));

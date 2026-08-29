import { WORDS } from './words';

export type WordEntry = {
  id: string;
  english: string;
  hebrew: string;
};

export const WORD_ENTRIES: WordEntry[] = WORDS.map((word, index) => ({
  id: `word-${index}-${word.en.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`,
  english: word.en,
  hebrew: word.he,
}));

export type NumberItem = {
  id: string;
  digit: number;
  en: string;
  ttsText: string;
};

export const NUMBERS_0_TO_12: NumberItem[] = [
  { id: 'number-0', digit: 0, en: 'zero', ttsText: 'zero' },
  { id: 'number-1', digit: 1, en: 'one', ttsText: 'one' },
  { id: 'number-2', digit: 2, en: 'two', ttsText: 'two' },
  { id: 'number-3', digit: 3, en: 'three', ttsText: 'three' },
  { id: 'number-4', digit: 4, en: 'four', ttsText: 'four' },
  { id: 'number-5', digit: 5, en: 'five', ttsText: 'five' },
  { id: 'number-6', digit: 6, en: 'six', ttsText: 'six' },
  { id: 'number-7', digit: 7, en: 'seven', ttsText: 'seven' },
  { id: 'number-8', digit: 8, en: 'eight', ttsText: 'eight' },
  { id: 'number-9', digit: 9, en: 'nine', ttsText: 'nine' },
  { id: 'number-10', digit: 10, en: 'ten', ttsText: 'ten' },
  { id: 'number-11', digit: 11, en: 'eleven', ttsText: 'eleven' },
  { id: 'number-12', digit: 12, en: 'twelve', ttsText: 'twelve' },
];

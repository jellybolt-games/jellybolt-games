export const palette = {
  primary: '#2F6BFF',
  success: '#24B26B',
  gentleError: '#FF6B6B',
  background: '#FFF8E7',
  accent: '#FFC857',
  text: '#1F2A44',
  white: '#FFFFFF',
  card: '#FFFFFF',
  muted: '#5F6B85',
  border: '#F2DCA7',
  primarySoft: '#EAF0FF',
  accentSoft: '#FFF0BF',
  errorSoft: '#FFE8E8',
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 14,
  lg: 20,
  xl: 28,
  xxl: 36,
} as const;

export const radius = {
  md: 16,
  lg: 22,
  xl: 30,
  pill: 999,
} as const;

export const shadow = {
  shadowColor: palette.text,
  shadowOffset: { width: 0, height: 5 },
  shadowOpacity: 0.12,
  shadowRadius: 10,
  elevation: 4,
} as const;

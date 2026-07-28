import AsyncStorage from '@react-native-async-storage/async-storage';

export const GAME_STATE_KEY = 'jellybolt-english-state';

export type GameState = {
  starsTotal: number;
  streak: number;
  lastSessionTimestamp: string | null;
  soundOn: boolean;
};

export const DEFAULT_GAME_STATE: GameState = {
  starsTotal: 0,
  streak: 0,
  lastSessionTimestamp: null,
  soundOn: true,
};

export async function loadGameState(): Promise<GameState> {
  const raw = await AsyncStorage.getItem(GAME_STATE_KEY);

  if (!raw) {
    return DEFAULT_GAME_STATE;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<GameState>;
    return {
      starsTotal: typeof parsed.starsTotal === 'number' && parsed.starsTotal >= 0 ? parsed.starsTotal : 0,
      streak: typeof parsed.streak === 'number' && parsed.streak >= 0 ? parsed.streak : 0,
      lastSessionTimestamp:
        typeof parsed.lastSessionTimestamp === 'string' ? parsed.lastSessionTimestamp : DEFAULT_GAME_STATE.lastSessionTimestamp,
      soundOn: typeof parsed.soundOn === 'boolean' ? parsed.soundOn : true,
    };
  } catch {
    return DEFAULT_GAME_STATE;
  }
}

export async function saveGameState(nextState: GameState): Promise<void> {
  await AsyncStorage.setItem(GAME_STATE_KEY, JSON.stringify(nextState));
}

export async function recordAnswer(wasCorrect: boolean): Promise<GameState> {
  const current = await loadGameState();
  const nextState: GameState = {
    ...current,
    starsTotal: wasCorrect ? current.starsTotal + 1 : current.starsTotal,
    streak: wasCorrect ? current.streak + 1 : 0,
    lastSessionTimestamp: new Date().toISOString(),
  };

  await saveGameState(nextState);
  return nextState;
}

export async function recordPractice(): Promise<GameState> {
  const current = await loadGameState();
  const nextState: GameState = {
    ...current,
    lastSessionTimestamp: new Date().toISOString(),
  };

  await saveGameState(nextState);
  return nextState;
}

export async function setSoundEnabled(soundOn: boolean): Promise<GameState> {
  const current = await loadGameState();
  const nextState: GameState = {
    ...current,
    soundOn,
    lastSessionTimestamp: new Date().toISOString(),
  };

  await saveGameState(nextState);
  return nextState;
}

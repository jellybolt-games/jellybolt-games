import * as Speech from 'expo-speech';

const CHILD_FRIENDLY_RATE = 0.85;

export function speakEnglish(text: string): void {
  const trimmed = text.trim();

  if (!trimmed) {
    return;
  }

  Speech.stop();
  Speech.speak(trimmed, {
    language: 'en-US',
    rate: CHILD_FRIENDLY_RATE,
    pitch: 1,
  });
}

export function stopSpeech(): void {
  Speech.stop();
}

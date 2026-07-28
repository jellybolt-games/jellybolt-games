import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';

import { speakEnglish, stopSpeech } from '../audio/tts';
import { WORD_ENTRIES, type WordEntry } from '../data/wordEntries';
import { tapFeedback } from '../game/feedback';
import { DEFAULT_SET_LENGTH, createSessionPicker, parseRoundStats } from '../game/rounds';
import { he } from '../i18n/he';
import { loadGameState, recordPractice } from '../storage/progress';
import { palette, radius, shadow, spacing } from '../theme';

const speakPicker = createSessionPicker(WORD_ENTRIES);

export function SpeakRound() {
  const router = useRouter();
  const params = useLocalSearchParams<{ index?: string; correctCount?: string; currentStreak?: string; bestStreak?: string }>();
  const stats = parseRoundStats(params);
  const [soundOn, setSoundOn] = useState<boolean | null>(null);
  const [word] = useState<WordEntry>(() => speakPicker.next());

  useEffect(() => {
    let mounted = true;
    loadGameState()
      .then((state) => {
        if (mounted) {
          setSoundOn(state.soundOn);
        }
      })
      .catch(() => setSoundOn(true));

    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (soundOn) {
      speakEnglish(word.english);
    }

    return stopSpeech;
  }, [soundOn, word.english]);

  const replay = () => {
    tapFeedback();
    if (soundOn) {
      speakEnglish(word.english);
    }
  };

  const completePractice = () => {
    tapFeedback();
    void recordPractice().catch(() => undefined);

    router.push({
      pathname: '/result',
      params: {
        mode: 'speak',
        outcome: 'practice',
        word: word.english,
        correctAnswer: word.hebrew,
        wasCorrect: 'false',
        nextIndex: String(stats.index + 1),
        correctCount: String(stats.correctCount),
        currentStreak: String(stats.currentStreak),
        bestStreak: String(stats.bestStreak),
      },
    });
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <View style={styles.topRow}>
          <Text style={styles.progress}>{Math.min(stats.index + 1, DEFAULT_SET_LENGTH)} / {DEFAULT_SET_LENGTH}</Text>
          <Text style={styles.streak}>{stats.currentStreak >= 3 ? '🔥 ' : ''}{he.streak}: {stats.currentStreak}</Text>
        </View>

        <Text style={styles.prompt}>{he.speakMode}</Text>
        <Pressable style={({ pressed }) => [styles.wordCard, pressed && styles.pressed]} onPress={replay}>
          <Text style={styles.englishWord}>{word.english}</Text>
          <Text style={styles.hebrewHint}>{word.hebrew}</Text>
          <Text style={styles.listenAgain}>🔊 {he.listenAgain}</Text>
        </Pressable>

        <Pressable style={({ pressed }) => [styles.micButton, pressed && styles.micButtonPressed]} onPress={completePractice}>
          <Text style={styles.micText}>{he.tapToSpeak}</Text>
          <Text style={styles.placeholderText}>{he.speakPlaceholder}</Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: palette.background,
  },
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: spacing.xl,
  },
  topRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginBottom: spacing.lg,
  },
  progress: {
    color: palette.primary,
    fontSize: 18,
    fontWeight: '900',
    writingDirection: 'ltr',
  },
  streak: {
    backgroundColor: palette.white,
    borderRadius: radius.pill,
    color: palette.text,
    fontSize: 16,
    fontWeight: '900',
    overflow: 'hidden',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    writingDirection: 'rtl',
  },
  prompt: {
    color: palette.text,
    fontSize: 22,
    fontWeight: '900',
    marginBottom: spacing.md,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  wordCard: {
    alignItems: 'center',
    backgroundColor: palette.white,
    borderColor: palette.success,
    borderRadius: radius.xl,
    borderWidth: 3,
    marginBottom: spacing.xl,
    padding: spacing.xl,
    ...shadow,
  },
  pressed: {
    transform: [{ scale: 0.98 }],
  },
  englishWord: {
    color: palette.text,
    fontSize: 50,
    fontWeight: '900',
    textAlign: 'center',
    textTransform: 'uppercase',
    writingDirection: 'ltr',
  },
  hebrewHint: {
    color: palette.muted,
    fontSize: 22,
    fontWeight: '800',
    marginTop: spacing.sm,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  listenAgain: {
    color: palette.primary,
    fontSize: 17,
    fontWeight: '900',
    marginTop: spacing.md,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  micButton: {
    alignItems: 'center',
    backgroundColor: palette.success,
    borderRadius: radius.xl,
    minHeight: 108,
    padding: spacing.lg,
    ...shadow,
  },
  micButtonPressed: {
    transform: [{ scale: 0.98 }],
  },
  micText: {
    color: palette.white,
    fontSize: 27,
    fontWeight: '900',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  placeholderText: {
    color: palette.white,
    fontSize: 15,
    fontWeight: '700',
    lineHeight: 21,
    marginTop: spacing.sm,
    opacity: 0.95,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
});

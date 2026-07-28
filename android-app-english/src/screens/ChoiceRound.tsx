import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';

import { speakEnglish, stopSpeech } from '../audio/tts';
import { WORD_ENTRIES, type WordEntry } from '../data/wordEntries';
import { tapFeedback, successFeedback, warningFeedback } from '../game/feedback';
import { DEFAULT_SET_LENGTH, createSessionPicker, nextStats, parseRoundStats } from '../game/rounds';
import { he } from '../i18n/he';
import { loadGameState, recordAnswer } from '../storage/progress';
import { palette, radius, shadow, spacing } from '../theme';

const choicePicker = createSessionPicker(WORD_ENTRIES);

type ChoiceRoundState = {
  word: WordEntry;
  options: string[];
};

export function ChoiceRound() {
  const router = useRouter();
  const params = useLocalSearchParams<{ index?: string; correctCount?: string; currentStreak?: string; bestStreak?: string }>();
  const stats = parseRoundStats(params);
  const [soundOn, setSoundOn] = useState<boolean | null>(null);
  const [round] = useState<ChoiceRoundState>(() => createRound());

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
      speakEnglish(round.word.english);
    }

    return stopSpeech;
  }, [round.word.english, soundOn]);

  const replay = () => {
    tapFeedback();
    if (soundOn) {
      speakEnglish(round.word.english);
    }
  };

  const answer = (selectedAnswer: string) => {
    tapFeedback();
    const wasCorrect = selectedAnswer === round.word.hebrew;
    const updatedStats = nextStats(stats, wasCorrect);

    if (wasCorrect) {
      successFeedback();
    } else {
      warningFeedback();
    }

    void recordAnswer(wasCorrect).catch(() => undefined);

    router.push({
      pathname: '/result',
      params: {
        mode: 'choice',
        outcome: wasCorrect ? 'correct' : 'wrong',
        word: round.word.english,
        correctAnswer: round.word.hebrew,
        wasCorrect: String(wasCorrect),
        nextIndex: String(updatedStats.index),
        correctCount: String(updatedStats.correctCount),
        currentStreak: String(updatedStats.currentStreak),
        bestStreak: String(updatedStats.bestStreak),
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

        <Text style={styles.prompt}>{he.chooseTranslation}</Text>
        <Pressable style={({ pressed }) => [styles.wordCard, pressed && styles.pressed]} onPress={replay}>
          <Text style={styles.englishWord}>{round.word.english}</Text>
          <Text style={styles.listenAgain}>🔊 {he.listenAgain}</Text>
        </Pressable>

        <View style={styles.options}>
          {round.options.map((option) => (
            <Pressable
              key={option}
              style={({ pressed }) => [styles.optionButton, pressed && styles.optionButtonPressed]}
              onPress={() => answer(option)}
            >
              <Text style={styles.optionText}>{option}</Text>
            </Pressable>
          ))}
        </View>
      </View>
    </SafeAreaView>
  );
}

function createRound(): ChoiceRoundState {
  const word = choicePicker.next();
  return {
    word,
    options: choicePicker.optionsFor(word, (item) => item.hebrew, 4),
  };
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
    backgroundColor: palette.card,
    borderColor: palette.primary,
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
    fontSize: 46,
    fontWeight: '900',
    letterSpacing: 1,
    textAlign: 'center',
    textTransform: 'uppercase',
    writingDirection: 'ltr',
  },
  listenAgain: {
    color: palette.primary,
    fontSize: 17,
    fontWeight: '900',
    marginTop: spacing.md,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  options: {
    gap: spacing.md,
  },
  optionButton: {
    backgroundColor: palette.white,
    borderColor: palette.border,
    borderRadius: radius.lg,
    borderWidth: 2,
    minHeight: 58,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    ...shadow,
  },
  optionButtonPressed: {
    backgroundColor: palette.primarySoft,
    transform: [{ scale: 0.98 }],
  },
  optionText: {
    color: palette.text,
    fontSize: 24,
    fontWeight: '900',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
});

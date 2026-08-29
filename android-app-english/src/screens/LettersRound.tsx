import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';

import { speakEnglish, stopSpeech } from '../audio/tts';
import { LETTER_ITEMS, type LetterItem } from '../data/letters';
import { tapFeedback, successFeedback, warningFeedback } from '../game/feedback';
import { DEFAULT_SET_LENGTH, createSessionPicker, nextStats, parseRoundStats } from '../game/rounds';
import { he } from '../i18n/he';
import { loadGameState, recordAnswer } from '../storage/progress';
import { palette, radius, shadow, spacing } from '../theme';

type LetterRoundVariant = 'upperToLower' | 'lowerToUpper';

type LettersRoundState = {
  item: LetterItem;
  options: string[];
  variant: LetterRoundVariant;
};

const lettersPicker = createSessionPicker(LETTER_ITEMS);

export function LettersRound() {
  const router = useRouter();
  const params = useLocalSearchParams<{ index?: string; correctCount?: string; currentStreak?: string; bestStreak?: string }>();
  const stats = parseRoundStats(params);
  const [soundOn, setSoundOn] = useState<boolean | null>(null);
  const [round] = useState<LettersRoundState>(() => createRound(stats.index));
  const correctOption = round.variant === 'upperToLower' ? round.item.lower : round.item.upper;

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
      speakEnglish(round.item.ttsText);
    }

    return stopSpeech;
  }, [round.item.ttsText, soundOn]);

  const replay = () => {
    tapFeedback();
    if (soundOn) {
      speakEnglish(round.item.ttsText);
    }
  };

  const answer = (selectedAnswer: string) => {
    tapFeedback();
    const wasCorrect = selectedAnswer === correctOption;
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
        mode: 'letters',
        outcome: wasCorrect ? 'correct' : 'wrong',
        word: round.variant === 'upperToLower' ? round.item.upper : round.item.lower,
        correctAnswer: `${round.item.upper} ↔ ${round.item.lower}`,
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

        <Text style={styles.title}>{he.lettersMode}</Text>
        <Text style={styles.prompt}>{round.variant === 'upperToLower' ? he.lettersUpperPrompt : he.lettersLowerPrompt}</Text>
        <Pressable style={({ pressed }) => [styles.promptCard, pressed && styles.pressed]} onPress={replay}>
          <Text style={styles.bigPrompt}>{round.variant === 'upperToLower' ? round.item.upper : round.item.lower}</Text>
          <Text style={styles.listenAgain}>🔊 {he.replayLetter}</Text>
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

function createRound(index: number): LettersRoundState {
  const item = lettersPicker.next();
  const variant: LetterRoundVariant = index % 2 === 0 ? 'upperToLower' : 'lowerToUpper';
  const options = lettersPicker.optionsFor(
    item,
    (candidate) => (variant === 'upperToLower' ? candidate.lower : candidate.upper),
    4,
  );

  return { item, options, variant };
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
    marginBottom: spacing.md,
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
  title: {
    color: palette.text,
    fontSize: 30,
    fontWeight: '900',
    marginBottom: spacing.xs,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  prompt: {
    color: palette.muted,
    fontSize: 19,
    fontWeight: '800',
    marginBottom: spacing.md,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  promptCard: {
    alignItems: 'center',
    backgroundColor: palette.white,
    borderColor: palette.gentleError,
    borderRadius: radius.xl,
    borderWidth: 3,
    marginBottom: spacing.xl,
    padding: spacing.xl,
    ...shadow,
  },
  pressed: {
    transform: [{ scale: 0.98 }],
  },
  bigPrompt: {
    color: palette.text,
    fontSize: 72,
    fontWeight: '900',
    textAlign: 'center',
    textTransform: 'none',
    writingDirection: 'ltr',
  },
  listenAgain: {
    color: palette.primary,
    fontSize: 17,
    fontWeight: '900',
    marginTop: spacing.sm,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  options: {
    gap: spacing.md,
  },
  optionButton: {
    alignItems: 'center',
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
    backgroundColor: palette.errorSoft,
    transform: [{ scale: 0.98 }],
  },
  optionText: {
    color: palette.text,
    fontSize: 30,
    fontWeight: '900',
    textAlign: 'center',
    writingDirection: 'ltr',
  },
});

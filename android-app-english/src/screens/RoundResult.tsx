import { useEffect, useRef } from 'react';
import { Animated, Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';

import { ConfettiBurst } from '../components/ConfettiBurst';
import { Zivvy } from '../components/Zivvy';
import { tapFeedback } from '../game/feedback';
import { he } from '../i18n/he';
import { palette, radius, shadow, spacing } from '../theme';

export type RoundMode = 'choice' | 'speak' | 'numbers' | 'letters';
export type ResultOutcome = 'correct' | 'wrong' | 'practice';

type RoundResultProps = {
  mode: RoundMode;
  word: string;
  correctAnswer: string;
  outcome: ResultOutcome;
  onNext: () => void;
};

export function RoundResult({ mode, word, correctAnswer, outcome, onNext }: RoundResultProps) {
  const isCorrect = outcome === 'correct';
  const isPractice = outcome === 'practice';
  const answerDirection = mode === 'choice' || mode === 'speak' ? 'rtl' : 'ltr';
  const wobble = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (outcome !== 'wrong') {
      return;
    }

    Animated.sequence([
      Animated.timing(wobble, { duration: 70, toValue: 1, useNativeDriver: true }),
      Animated.timing(wobble, { duration: 90, toValue: -1, useNativeDriver: true }),
      Animated.timing(wobble, { duration: 80, toValue: 0, useNativeDriver: true }),
    ]).start();
  }, [outcome, wobble]);

  const next = () => {
    tapFeedback();
    onNext();
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <ConfettiBurst active={isCorrect} />
        <Animated.View
          style={[
            styles.card,
            {
              transform: [
                {
                  translateX: wobble.interpolate({ inputRange: [-1, 1], outputRange: [-8, 8] }),
                },
              ],
            },
          ]}
        >
          <Zivvy mood={isCorrect || isPractice ? 'happy' : 'blink'} size={132} />
          <Text style={styles.icon}>{isPractice ? '🎤' : isCorrect ? '✅' : '✨'}</Text>
          <Text style={[styles.title, !isCorrect && !isPractice && styles.tryTitle]}>
            {isPractice ? he.practiceDone : isCorrect ? he.wellDone : he.tryAgain}
          </Text>
          <Text style={styles.word}>{word}</Text>
          <Text style={styles.answerLabel}>{he.correctAnswer}</Text>
          <Text style={[styles.answer, { writingDirection: answerDirection }]}>{correctAnswer}</Text>

          <Pressable style={({ pressed }) => [styles.nextButton, pressed && styles.nextButtonPressed]} onPress={next}>
            <Text style={styles.nextText}>{he.next}</Text>
          </Pressable>
        </Animated.View>
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
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    padding: spacing.xl,
  },
  card: {
    alignItems: 'center',
    backgroundColor: palette.white,
    borderColor: palette.border,
    borderRadius: radius.xl,
    borderWidth: 2,
    padding: spacing.xl,
    width: '100%',
    ...shadow,
  },
  icon: {
    fontSize: 42,
    marginTop: -spacing.sm,
  },
  title: {
    color: palette.success,
    fontSize: 34,
    fontWeight: '900',
    marginBottom: spacing.md,
    marginTop: spacing.sm,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  tryTitle: {
    color: palette.gentleError,
  },
  word: {
    color: palette.text,
    fontSize: 32,
    fontWeight: '900',
    marginBottom: spacing.md,
    textAlign: 'center',
    textTransform: 'uppercase',
    writingDirection: 'ltr',
  },
  answerLabel: {
    color: palette.muted,
    fontSize: 18,
    fontWeight: '800',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  answer: {
    color: palette.text,
    fontSize: 30,
    fontWeight: '900',
    marginBottom: spacing.xl,
    marginTop: spacing.sm,
    textAlign: 'center',
  },
  nextButton: {
    backgroundColor: palette.primary,
    borderRadius: radius.lg,
    minWidth: 190,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
  },
  nextButtonPressed: {
    transform: [{ scale: 0.98 }],
  },
  nextText: {
    color: palette.white,
    fontSize: 22,
    fontWeight: '900',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
});

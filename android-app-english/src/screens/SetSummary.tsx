import { Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';

import { Zivvy } from '../components/Zivvy';
import { tapFeedback } from '../game/feedback';
import { he } from '../i18n/he';
import { palette, radius, shadow, spacing } from '../theme';

type SetSummaryProps = {
  stars: number;
  practiced: number;
  correctCount: number;
  bestStreak: number;
  onPlayAgain: () => void;
  onHome: () => void;
};

export function SetSummary({ stars, practiced, correctCount, bestStreak, onPlayAgain, onHome }: SetSummaryProps) {
  const visibleStars = Math.max(1, Math.min(stars, 3));
  const replay = () => {
    tapFeedback();
    onPlayAgain();
  };
  const home = () => {
    tapFeedback();
    onHome();
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <View style={styles.card}>
          <Zivvy mood="happy" size={128} />
          <Text style={styles.title}>{he.setComplete}</Text>
          <Text style={styles.subtitle}>{he.earnedStars}</Text>
          <Text style={styles.stars}>{'⭐'.repeat(visibleStars)}</Text>

          <View style={styles.statsGrid}>
            <Stat label={he.practicedItems} value={String(practiced)} />
            <Stat label={he.correctCount} value={String(correctCount)} />
            <Stat label={he.bestStreak} value={bestStreak >= 3 ? `🔥 ${bestStreak}` : String(bestStreak)} />
          </View>

          <Pressable style={({ pressed }) => [styles.button, pressed && styles.buttonPressed]} onPress={replay}>
            <Text style={styles.buttonText}>{he.playAgain}</Text>
          </Pressable>
          <Pressable style={({ pressed }) => [styles.homeButton, pressed && styles.buttonPressed]} onPress={home}>
            <Text style={styles.homeButtonText}>{he.backHome}</Text>
          </Pressable>
        </View>
      </View>
    </SafeAreaView>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.statCard}>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
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
  title: {
    color: palette.text,
    fontSize: 36,
    fontWeight: '900',
    marginBottom: spacing.sm,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  subtitle: {
    color: palette.muted,
    fontSize: 20,
    fontWeight: '800',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  stars: {
    fontSize: 52,
    marginBottom: spacing.lg,
    marginTop: spacing.sm,
    textAlign: 'center',
  },
  statsGrid: {
    flexDirection: 'row-reverse',
    gap: spacing.sm,
    marginBottom: spacing.xl,
    width: '100%',
  },
  statCard: {
    alignItems: 'center',
    backgroundColor: palette.primarySoft,
    borderRadius: radius.md,
    flex: 1,
    padding: spacing.md,
  },
  statValue: {
    color: palette.primary,
    fontSize: 23,
    fontWeight: '900',
    textAlign: 'center',
    writingDirection: 'ltr',
  },
  statLabel: {
    color: palette.text,
    fontSize: 12,
    fontWeight: '800',
    marginTop: spacing.xs,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  button: {
    backgroundColor: palette.primary,
    borderRadius: radius.lg,
    marginBottom: spacing.md,
    minWidth: 210,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
  },
  homeButton: {
    backgroundColor: palette.accent,
    borderRadius: radius.lg,
    minWidth: 210,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
  },
  buttonPressed: {
    transform: [{ scale: 0.98 }],
  },
  buttonText: {
    color: palette.white,
    fontSize: 22,
    fontWeight: '900',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  homeButtonText: {
    color: palette.text,
    fontSize: 22,
    fontWeight: '900',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
});

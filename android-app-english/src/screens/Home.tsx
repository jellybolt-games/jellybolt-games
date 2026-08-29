import { useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';

import { Zivvy } from '../components/Zivvy';
import { tapFeedback } from '../game/feedback';
import { he } from '../i18n/he';
import { DEFAULT_GAME_STATE, loadGameState, setSoundEnabled, type GameState } from '../storage/progress';
import { palette, radius, shadow, spacing } from '../theme';

type ModeButtonProps = {
  title: string;
  subtitle: string;
  emoji: string;
  color: string;
  textColor?: string;
  onPress: () => void;
};

export function Home() {
  const router = useRouter();
  const [gameState, setGameState] = useState<GameState>(DEFAULT_GAME_STATE);

  useEffect(() => {
    let mounted = true;

    loadGameState()
      .then((state) => {
        if (mounted) {
          setGameState(state);
        }
      })
      .catch(() => undefined);

    return () => {
      mounted = false;
    };
  }, []);

  const openMode = (pathname: '/choice' | '/speak' | '/numbers' | '/letters') => {
    tapFeedback();
    router.push(pathname);
  };

  const toggleSound = () => {
    tapFeedback();
    const nextSound = !gameState.soundOn;
    setGameState((current) => ({ ...current, soundOn: nextSound }));
    void setSoundEnabled(nextSound).then(setGameState).catch(() => undefined);
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <View style={styles.headerRow}>
          <Pressable style={({ pressed }) => [styles.soundPill, pressed && styles.pressed]} onPress={toggleSound}>
            <Text style={styles.soundText}>{gameState.soundOn ? `🔊 ${he.soundOn}` : `🔇 ${he.soundOff}`}</Text>
          </Pressable>
          <Text style={styles.brand}>⚡ {he.appTitle}</Text>
        </View>

        <View style={styles.mascotWrap}>
          <Zivvy mood={gameState.streak >= 3 ? 'happy' : 'neutral'} size={130} />
        </View>

        <Text style={styles.title}>{he.homeTitle}</Text>
        <Text style={styles.subtitle}>{he.homeSubtitle}</Text>

        <View style={styles.actions}>
          <ModeButton
            title={he.choiceMode}
            subtitle="English → עברית"
            emoji="🎯"
            color={palette.primary}
            onPress={() => openMode('/choice')}
          />
          <ModeButton
            title={he.speakMode}
            subtitle="שומעים וחוזרים בקול"
            emoji="🎤"
            color={palette.success}
            onPress={() => openMode('/speak')}
          />
          <ModeButton
            title={he.numbersMode}
            subtitle="0–12"
            emoji="🔢"
            color={palette.accent}
            textColor={palette.text}
            onPress={() => openMode('/numbers')}
          />
          <ModeButton
            title={he.lettersMode}
            subtitle="A/a עד T/t"
            emoji="🔤"
            color={palette.gentleError}
            onPress={() => openMode('/letters')}
          />
        </View>

        <View style={styles.progressPill}>
          <Text style={styles.progressText}>⭐ {he.totalStars}: {gameState.starsTotal}</Text>
          <Text style={styles.progressText}>{gameState.streak >= 3 ? '🔥 ' : ''}{he.streak}: {gameState.streak}</Text>
        </View>
      </View>
    </SafeAreaView>
  );
}

function ModeButton({ title, subtitle, emoji, color, textColor = palette.white, onPress }: ModeButtonProps) {
  return (
    <Pressable style={({ pressed }) => [styles.card, { backgroundColor: color }, pressed && styles.cardPressed]} onPress={onPress}>
      <Text style={styles.cardEmoji}>{emoji}</Text>
      <View style={styles.cardText}>
        <Text style={[styles.cardTitle, { color: textColor }]}>{title}</Text>
        <Text style={[styles.cardSubtitle, { color: textColor }]}>{subtitle}</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: palette.background,
  },
  container: {
    flex: 1,
    padding: spacing.xl,
    justifyContent: 'center',
  },
  headerRow: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: spacing.md,
  },
  brand: {
    color: palette.primary,
    fontSize: 17,
    fontWeight: '900',
    textAlign: 'right',
    writingDirection: 'ltr',
  },
  soundPill: {
    backgroundColor: palette.white,
    borderColor: palette.border,
    borderRadius: radius.pill,
    borderWidth: 1,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  soundText: {
    color: palette.text,
    fontSize: 13,
    fontWeight: '800',
    writingDirection: 'rtl',
  },
  mascotWrap: {
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  title: {
    color: palette.text,
    fontSize: 34,
    fontWeight: '900',
    marginBottom: spacing.sm,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  subtitle: {
    color: palette.muted,
    fontSize: 18,
    lineHeight: 26,
    marginBottom: spacing.xl,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  actions: {
    gap: spacing.md,
  },
  card: {
    alignItems: 'center',
    borderRadius: radius.lg,
    flexDirection: 'row-reverse',
    gap: spacing.lg,
    minHeight: 76,
    padding: spacing.lg,
    ...shadow,
  },
  cardPressed: {
    transform: [{ scale: 0.97 }],
  },
  pressed: {
    transform: [{ scale: 0.97 }],
  },
  cardEmoji: {
    fontSize: 34,
  },
  cardText: {
    flex: 1,
  },
  cardTitle: {
    fontSize: 25,
    fontWeight: '900',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  cardSubtitle: {
    fontSize: 14,
    fontWeight: '700',
    marginTop: spacing.xs,
    opacity: 0.9,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  progressPill: {
    alignItems: 'center',
    backgroundColor: palette.white,
    borderColor: palette.border,
    borderRadius: radius.pill,
    borderWidth: 1,
    flexDirection: 'row-reverse',
    gap: spacing.md,
    justifyContent: 'center',
    marginTop: spacing.xl,
    padding: spacing.md,
  },
  progressText: {
    color: palette.text,
    fontSize: 16,
    fontWeight: '900',
    writingDirection: 'rtl',
  },
});

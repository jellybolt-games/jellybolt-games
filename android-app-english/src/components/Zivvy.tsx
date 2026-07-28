import { StyleSheet, Text, View } from 'react-native';

import { palette, shadow } from '../theme';

export type ZivvyMood = 'neutral' | 'happy' | 'blink';

type ZivvyProps = {
  mood?: ZivvyMood;
  size?: number;
};

export function Zivvy({ mood = 'neutral', size = 118 }: ZivvyProps) {
  const scale = size / 118;

  return (
    <View style={[styles.stage, { width: size, height: size }]} accessibilityLabel="Zivvy mascot">
      <View style={[styles.sparkTail, { transform: [{ rotate: '18deg' }, { scale }] }]} />
      <View style={[styles.body, { transform: [{ scale }] }]}>
        <View style={styles.eyeRow}>
          <View style={[styles.eye, mood === 'blink' && styles.eyeBlink]} />
          <View style={[styles.eye, mood === 'blink' && styles.eyeBlink]} />
        </View>
        <Text style={[styles.mouth, mood === 'happy' && styles.happyMouth]}>{mood === 'blink' ? '—' : '⌣'}</Text>
        <Text style={styles.bolt}>⚡</Text>
      </View>
      <View style={[styles.fin, { transform: [{ rotate: '-18deg' }, { scale }] }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  stage: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  body: {
    alignItems: 'center',
    backgroundColor: palette.primary,
    borderColor: palette.accent,
    borderRadius: 44,
    borderWidth: 4,
    height: 88,
    justifyContent: 'center',
    width: 96,
    ...shadow,
  },
  sparkTail: {
    backgroundColor: palette.accent,
    borderRadius: 16,
    height: 34,
    left: 8,
    position: 'absolute',
    top: 13,
    width: 48,
  },
  fin: {
    backgroundColor: palette.success,
    borderRadius: 12,
    bottom: 11,
    height: 24,
    position: 'absolute',
    right: 10,
    width: 44,
  },
  eyeRow: {
    flexDirection: 'row',
    gap: 16,
    marginTop: 8,
  },
  eye: {
    backgroundColor: palette.white,
    borderRadius: 99,
    height: 18,
    width: 18,
  },
  eyeBlink: {
    height: 4,
    marginTop: 7,
  },
  mouth: {
    color: palette.white,
    fontSize: 25,
    fontWeight: '900',
    lineHeight: 28,
    marginTop: -2,
  },
  happyMouth: {
    fontSize: 30,
  },
  bolt: {
    bottom: 5,
    fontSize: 20,
    position: 'absolute',
    right: 13,
  },
});

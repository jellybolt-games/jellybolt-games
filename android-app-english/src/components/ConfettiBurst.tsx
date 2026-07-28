import { useEffect, useRef } from 'react';
import { Animated, StyleSheet, View } from 'react-native';

import { palette } from '../theme';

const PIECES = [
  { x: 10, dx: -52, dy: -72, color: palette.primary },
  { x: 18, dx: -28, dy: -96, color: palette.accent },
  { x: 28, dx: -12, dy: -64, color: palette.success },
  { x: 40, dx: 8, dy: -104, color: palette.gentleError },
  { x: 50, dx: 26, dy: -78, color: palette.primary },
  { x: 62, dx: 46, dy: -100, color: palette.accent },
  { x: 72, dx: 64, dy: -70, color: palette.success },
  { x: 82, dx: 18, dy: -118, color: palette.gentleError },
  { x: 90, dx: 70, dy: -88, color: palette.primary },
] as const;

type ConfettiBurstProps = {
  active: boolean;
};

export function ConfettiBurst({ active }: ConfettiBurstProps) {
  const progress = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (!active) {
      return;
    }

    progress.setValue(0);
    Animated.timing(progress, {
      duration: 900,
      toValue: 1,
      useNativeDriver: true,
    }).start();
  }, [active, progress]);

  if (!active) {
    return null;
  }

  return (
    <View pointerEvents="none" style={styles.root}>
      {PIECES.map((piece, index) => (
        <Animated.View
          key={`${piece.x}-${piece.color}`}
          style={[
            styles.piece,
            {
              backgroundColor: piece.color,
              left: `${piece.x}%`,
              opacity: progress.interpolate({ inputRange: [0, 0.72, 1], outputRange: [0, 1, 0] }),
              transform: [
                { translateX: progress.interpolate({ inputRange: [0, 1], outputRange: [0, piece.dx] }) },
                { translateY: progress.interpolate({ inputRange: [0, 1], outputRange: [0, piece.dy] }) },
                { rotate: progress.interpolate({ inputRange: [0, 1], outputRange: ['0deg', `${120 + index * 24}deg`] }) },
                { scale: progress.interpolate({ inputRange: [0, 0.35, 1], outputRange: [0.45, 1.1, 0.6] }) },
              ],
            },
          ]}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    height: 150,
    left: 0,
    overflow: 'visible',
    position: 'absolute',
    right: 0,
    top: 50,
    zIndex: 20,
  },
  piece: {
    borderRadius: 999,
    height: 12,
    position: 'absolute',
    top: 100,
    width: 12,
  },
});

import React, { useState, useEffect } from 'react';
import {
  Text,
  StyleSheet,
  TouchableOpacity,
  View,
  Animated,
  Easing,
  Dimensions,
} from 'react-native';

interface AnimatedToggleButtonProps {
  isGooglePay: boolean;
  onToggle: () => void;
}

export function AnimatedToggleButton({
  isGooglePay,
  onToggle,
}: AnimatedToggleButtonProps): React.JSX.Element {
  const slideAnimation = useState(new Animated.Value(isGooglePay ? 0 : 1))[0];
  const scaleAnimation = useState(new Animated.Value(1))[0];

  // Calcular largura dinâmica baseada na tela
  const screenWidth = Dimensions.get('window').width;
  const containerPadding = 40; // 20px de cada lado
  const buttonWidth = screenWidth - containerPadding;
  const indicatorWidth = buttonWidth / 2;

  // Sincronizar animação com a prop isGooglePay quando ela muda externamente
  useEffect(() => {
    console.log('🔄 [Toggle] Prop isGooglePay mudou para:', isGooglePay);
    Animated.timing(slideAnimation, {
      toValue: isGooglePay ? 0 : 1,
      duration: 300,
      easing: Easing.out(Easing.cubic),
      useNativeDriver: true,
    }).start();
  }, [isGooglePay, slideAnimation]);

  const toggleAnimation = (): void => {
    // Animação de escala (pressionar)
    Animated.sequence([
      Animated.timing(scaleAnimation, {
        toValue: 0.95,
        duration: 100,
        useNativeDriver: true,
      }),
      Animated.timing(scaleAnimation, {
        toValue: 1,
        duration: 100,
        useNativeDriver: true,
      }),
    ]).start();

    // Animação de deslizamento
    Animated.timing(slideAnimation, {
      toValue: isGooglePay ? 1 : 0,
      duration: 300,
      easing: Easing.out(Easing.cubic),
      useNativeDriver: true,
    }).start();

    onToggle();
  };

  const slideTranslateX = slideAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: [0, indicatorWidth - 9], // Metade da largura total do botão
  });

  return (
    <View style={styles.animatedToggleContainer}>
      <TouchableOpacity
        style={[styles.animatedToggleButton, { width: buttonWidth }]}
        onPress={toggleAnimation}
        activeOpacity={0.8}
      >
        <Animated.View
          style={[
            styles.animatedToggleBackground,
            {
              transform: [{ scale: scaleAnimation }],
            },
          ]}
        >
          <Animated.View
            style={[
              styles.animatedToggleIndicator,
              {
                width: indicatorWidth,
                transform: [{ translateX: slideTranslateX }],
              },
            ]}
          />
          <View style={styles.animatedToggleLabels}>
            <Text
              style={[
                styles.animatedToggleLabel,
                isGooglePay && styles.animatedToggleLabelActive,
              ]}
            >
              Google Pay
            </Text>
            <Text
              style={[
                styles.animatedToggleLabel,
                !isGooglePay && styles.animatedToggleLabelActive,
              ]}
            >
              Samsung Pay
            </Text>
          </View>
        </Animated.View>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  animatedToggleContainer: {
    alignItems: 'center',
    paddingVertical: 20,
    paddingHorizontal: 20,
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  animatedToggleButton: {
    height: 50,
  },
  animatedToggleBackground: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    borderRadius: 25,
    borderWidth: 2,
    borderColor: '#e0e0e0',
    position: 'relative',
    overflow: 'hidden',
  },
  animatedToggleIndicator: {
    position: 'absolute',
    top: 2,
    left: 2,
    height: 42,
    backgroundColor: '#4285F4',
    borderRadius: 21,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  animatedToggleLabels: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
  },
  animatedToggleLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#666',
    textAlign: 'center',
    flex: 1,
  },
  animatedToggleLabelActive: {
    color: 'white',
    fontWeight: 'bold',
  },
});

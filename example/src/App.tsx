import React, { useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { AnimatedToggleButton } from './components/AnimatedToggleButton';
import { GooglePayExample } from './components/GooglePayExample';
import { SamsungPayExample } from './components/SamsungPayExample';

export default function App(): React.JSX.Element {
  const [isGooglePay, setIsGooglePay] = useState(true);

  const handleToggle = (): void => {
    setIsGooglePay(!isGooglePay);
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#f5f5f5' }}>
      {/* Botão animado para alternar entre Google Pay e Samsung Pay */}
      <AnimatedToggleButton isGooglePay={isGooglePay} onToggle={handleToggle} />

      {/* Renderizar componente baseado no estado */}
      {isGooglePay ? <GooglePayExample /> : <SamsungPayExample />}
    </SafeAreaView>
  );
}

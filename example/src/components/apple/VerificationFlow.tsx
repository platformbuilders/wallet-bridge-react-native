// In-App Verification: a Wallet abre o app pelo appLaunchURL do pass, o app
// faz o ID&V, pede o activationData ao emissor e chama activatePass.
//
// No example, a URL vem do apple-wallet-app-mock:
//   builderswalletexample://apple-wallet/verify?deviceAccountIdentifier=<DPANID>
import { useEffect, useState } from 'react';
import { Text, TextInput } from 'react-native';
import { AppleWalletModule } from '@platformbuilders/wallet-bridge-react-native';
import { requestActivationData } from './issuerHostMock';
import { Button, Card, Row, styles } from './ui';

export function VerificationFlow({
  deviceAccountIdentifier: initial,
  onDone,
}: {
  deviceAccountIdentifier: string;
  onDone: () => void;
}): React.JSX.Element {
  const [deviceAccountIdentifier, setDeviceAccountIdentifier] =
    useState(initial);
  const [result, setResult] = useState<string | null>(null);
  const [running, setRunning] = useState(false);

  useEffect(() => setDeviceAccountIdentifier(initial), [initial]);

  const activate = async () => {
    setRunning(true);
    setResult(null);
    try {
      const activationData = await requestActivationData(
        deviceAccountIdentifier,
      );
      const activated = await AppleWalletModule.activatePass(
        deviceAccountIdentifier,
        activationData,
      );
      setResult(`activatePass → ${activated}`);
      onDone();
    } catch (error) {
      const { code, message } = error as { code?: string; message?: string };
      setResult(`${code ?? 'ERROR'}: ${message ?? String(error)}`);
    } finally {
      setRunning(false);
    }
  };

  return (
    <Card title="In-App Verification">
      <Text style={styles.muted}>
        DPANID do pass pendente (da lista de passes ou da URL do app mock).
      </Text>
      <TextInput
        style={[styles.mono, styles.card]}
        value={deviceAccountIdentifier}
        onChangeText={setDeviceAccountIdentifier}
        autoCapitalize="none"
        autoCorrect={false}
        placeholder="deviceAccountIdentifier"
      />
      <Button
        title={running ? 'Ativando…' : 'Ativar pass'}
        onPress={activate}
        disabled={running || deviceAccountIdentifier.length === 0}
      />
      {result && <Row label="Resultado" value={result} />}
    </Card>
  );
}

// Passes do iPhone e do Apple Watch, com o que o app manda ao backend:
// DPANID, FPANID, estado de ativação e device. Os valores são selecionáveis
// (copiar o DPANID para o apple-wallet-app-mock).
import { useCallback, useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import {
  AppleWalletModule,
  type ApplePaymentPass,
} from '@platformbuilders/wallet-bridge-react-native';
import { Button, Card, Row, styles } from './ui';

export function PassesList({
  refreshKey,
  onVerify,
}: {
  refreshKey: number;
  onVerify: (deviceAccountIdentifier: string) => void;
}): React.JSX.Element {
  const [passes, setPasses] = useState<ApplePaymentPass[]>([]);
  const [watchPaired, setWatchPaired] = useState<boolean | null>(null);

  const refresh = useCallback(async () => {
    // A lib devolve as duas listas do PassKit separadas; juntar é do app.
    const [local, remote] = await Promise.all([
      AppleWalletModule.listPasses(),
      AppleWalletModule.listRemotePasses(),
    ]);
    setPasses([...local, ...remote]);
    setWatchPaired(await AppleWalletModule.isWatchPaired());
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh, refreshKey]);

  return (
    <Card title={`Passes (${passes.length})`}>
      <Row label="Apple Watch pareado" value={watchPaired} />
      <Button title="Atualizar" variant="secondary" onPress={refresh} />
      {passes.map((pass) => (
        <View key={pass.serialNumber} style={styles.card}>
          <Row
            label="device"
            value={`${pass.deviceName}${pass.isRemote ? ' (remoto)' : ''}`}
          />
          <Row label="estado" value={pass.passActivationState} />
          <Row label="FPANID" value={pass.primaryAccountIdentifier} />
          <Row label="DPANID" value={pass.deviceAccountIdentifier} />
          <Row label="final" value={pass.primaryAccountNumberSuffix} />
          {pass.passActivationState === 'REQUIRES_ACTIVATION' && (
            <Button
              title="Ativar (In-App Verification)"
              onPress={() => onVerify(pass.deviceAccountIdentifier)}
            />
          )}
        </View>
      ))}
      {passes.length === 0 && (
        <Text style={styles.muted}>
          Nenhum pass. No mock, escolha um cenário com passes semeados.
        </Text>
      )}
    </Card>
  );
}

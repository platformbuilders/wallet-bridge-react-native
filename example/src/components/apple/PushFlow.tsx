// In-App Provisioning (push): sheet do PassKit → passo 1 → emissor → passo 2.
import { useState } from 'react';
import { Text } from 'react-native';
import {
  AppleProvisioningStatus,
  AppleWalletModule,
  useAppleWalletListener,
  type AppleProvisioningConfig,
  type AppleProvisioningResultEvent,
} from '@platformbuilders/wallet-bridge-react-native';
import { requestSecurePayload } from './issuerHostMock';
import { Button, Card, Row, styles } from './ui';

// Cartão de teste: nenhum dado real. O FPANID casa com os cenários do mock.
const TEST_CARD: AppleProvisioningConfig = {
  cardholderName: 'NOME DO PORTADOR',
  primaryAccountSuffix: '1111',
  localizedDescription: 'Cartão de teste',
  paymentNetwork: 'Visa',
  primaryAccountIdentifier: 'FAKE_FPANID_1',
  expiration: '12/30',
};

const errorText = (error: unknown) => {
  const { code, message } = error as { code?: string; message?: string };
  return `${code ?? 'ERROR'}: ${message ?? String(error)}`;
};

export function PushFlow({
  onLog,
}: {
  onLog: (message: string) => void;
}): React.JSX.Element {
  const [availability, setAvailability] = useState<{
    canAddPaymentPass: boolean;
    canAddSecureElementPass: boolean;
  } | null>(null);
  const [stages, setStages] = useState<AppleProvisioningStatus[]>([]);
  const [result, setResult] = useState<AppleProvisioningResultEvent | null>(
    null,
  );
  const [error, setError] = useState<string | null>(null);
  const [running, setRunning] = useState(false);

  useAppleWalletListener({
    onProvisioningCertificates: async (event) => {
      onLog(`Passo 1 (${event.certificates.length} certificados) → emissor`);
      try {
        const payload = await requestSecurePayload(event);
        await AppleWalletModule.submitProvisioningData(payload);
        onLog('Passo 2 entregue ao PassKit');
      } catch (submitError) {
        setError(errorText(submitError));
      }
    },
    onProvisioningResult: (event) => {
      setStages((current) => [...current, event.status]);
      const terminal = [
        AppleProvisioningStatus.ADDED,
        AppleProvisioningStatus.CANCELED,
        AppleProvisioningStatus.FAILED,
      ].includes(event.status);
      if (terminal) {
        setResult(event);
        setRunning(false);
      }
    },
  });

  const checkAvailability = async () => {
    setError(null);
    // A lib só repassa o PassKit; decidir se mostra o botão é do app.
    const [device, card] = await Promise.all([
      AppleWalletModule.canAddPaymentPass(),
      AppleWalletModule.canAddSecureElementPass(
        TEST_CARD.primaryAccountIdentifier ?? '',
      ),
    ]);
    setAvailability({
      canAddPaymentPass: device,
      canAddSecureElementPass: card,
    });
  };

  const start = async () => {
    setError(null);
    setResult(null);
    setStages([]);
    setRunning(true);
    try {
      const requestId =
        await AppleWalletModule.startInAppProvisioning(TEST_CARD);
      onLog(`startInAppProvisioning → ${requestId}`);
    } catch (startError) {
      setRunning(false);
      setError(errorText(startError));
    }
  };

  const cancel = async () => {
    await AppleWalletModule.cancelProvisioning();
  };

  return (
    <Card title="In-App Provisioning (push)">
      <Text style={styles.muted}>
        Cartão de teste •{TEST_CARD.primaryAccountSuffix} (
        {TEST_CARD.primaryAccountIdentifier}). O emissor é falso: só o mock
        aceita o passo 2.
      </Text>
      <Button
        title="Checar disponibilidade"
        variant="secondary"
        onPress={checkAvailability}
      />
      {availability && (
        <>
          <Row
            label="canAddPaymentPass"
            value={availability.canAddPaymentPass}
          />
          <Row
            label="canAddSecureElementPass"
            value={availability.canAddSecureElementPass}
          />
        </>
      )}
      <Button
        title={running ? 'Em andamento…' : 'Adicionar à Apple Wallet'}
        onPress={start}
        disabled={running}
      />
      {running && <Button title="Cancelar" variant="danger" onPress={cancel} />}
      {stages.length > 0 && <Row label="Estágios" value={stages.join(' → ')} />}
      {result && (
        <>
          <Row label="status" value={result.status} />
          <Row label="errorCode" value={result.errorCode} />
          <Row
            label="pass (DPANID)"
            value={result.pass?.deviceAccountIdentifier}
          />
        </>
      )}
      {error && <Text style={styles.error}>{error}</Text>}
    </Card>
  );
}

// Tela Apple Wallet do example, separada por fluxo: Push (In-App
// Provisioning), Verificação (In-App Verification) e Passes. Em build Debug
// sem entitlement (Simulator), a lib usa o mock nativo; o painel de mock
// troca o cenário.
import { useEffect, useState } from 'react';
import { Linking, ScrollView, StyleSheet, Text } from 'react-native';
import {
  AppleWalletModule,
  useAppleWalletLogListener,
} from '@platformbuilders/wallet-bridge-react-native';
import { MockPanel } from './apple/MockPanel';
import { PassesList } from './apple/PassesList';
import { PushFlow } from './apple/PushFlow';
import { VerificationFlow } from './apple/VerificationFlow';
import { Card, Chips, Row, styles as ui } from './apple/ui';

const FLOWS = ['Push', 'Verificação', 'Passes'] as const;
type Flow = (typeof FLOWS)[number];

/** DPANID de `<scheme>://apple-wallet/verify?deviceAccountIdentifier=...`. */
const verifyTarget = (url: string | null | undefined): string | null => {
  if (!url?.includes('apple-wallet/verify')) return null;
  const match = /[?&]deviceAccountIdentifier=([^&]+)/.exec(url);
  return match?.[1] ? decodeURIComponent(match[1]) : null;
};

export function AppleWalletExample(): React.JSX.Element {
  const [flow, setFlow] = useState<Flow>('Push');
  const [environment, setEnvironment] = useState<string | null>(null);
  const [verifyDpan, setVerifyDpan] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);
  const [logs, setLogs] = useState<string[]>([]);

  const addLog = (message: string) =>
    setLogs((current) => [message, ...current].slice(0, 30));
  const refreshPasses = () => setRefreshKey((key) => key + 1);

  useAppleWalletLogListener((event) =>
    addLog(`[${event.level}] ${event.tag}: ${event.message}`),
  );

  useEffect(() => {
    const loadEnvironment = async () =>
      setEnvironment(await AppleWalletModule.getEnvironment());
    void loadEnvironment();
  }, []);

  // In-App Verification: a "Wallet" (app mock) abre o example pela URL.
  useEffect(() => {
    const open = (url: string | null | undefined) => {
      const dpan = verifyTarget(url);
      if (!dpan) return;
      setVerifyDpan(dpan);
      setFlow('Verificação');
    };
    const openInitialURL = async () => open(await Linking.getInitialURL());
    void openInitialURL();
    const subscription = Linking.addEventListener('url', ({ url }) =>
      open(url),
    );
    return () => subscription.remove();
  }, []);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Apple Wallet</Text>
      <Row label="getEnvironment()" value={environment} />

      <MockPanel onChange={refreshPasses} />

      <Chips options={FLOWS} value={flow} onChange={setFlow} />

      {flow === 'Push' && <PushFlow onLog={addLog} />}
      {flow === 'Verificação' && (
        <VerificationFlow
          deviceAccountIdentifier={verifyDpan}
          onDone={refreshPasses}
        />
      )}
      {flow === 'Passes' && (
        <PassesList
          refreshKey={refreshKey}
          onVerify={(dpan) => {
            setVerifyDpan(dpan);
            setFlow('Verificação');
          }}
        />
      )}

      {logs.length > 0 && (
        <Card title="Logs">
          {logs.map((log, index) => (
            <Text key={index} style={ui.mono}>
              {log}
            </Text>
          ))}
        </Card>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 12,
    color: '#333',
  },
});

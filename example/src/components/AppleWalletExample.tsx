import { useEffect, useState } from 'react';
import {
  NativeEventEmitter,
  NativeModules,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

const { AppleWallet } = NativeModules;

type TestResult = {
  moduleFound: boolean;
  constants?: Record<string, unknown>;
  environment?: string;
  openWallet?: boolean;
  error?: string;
};

type LogEntry = { level: string; tag: string; message: string };

export function AppleWalletExample(): React.JSX.Element {
  const [result, setResult] = useState<TestResult | null>(null);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [running, setRunning] = useState(false);

  // Liga o log nativo → JS para validar também o evento da ponte.
  useEffect(() => {
    if (!AppleWallet) return undefined;

    const emitter = new NativeEventEmitter(AppleWallet);
    const eventName =
      AppleWallet.getConstants?.().EVENT_LOG ?? 'AppleWalletLog';
    const subscription = emitter.addListener(eventName, (payload) => {
      const event = payload as LogEntry;
      setLogs((current) => [event, ...current].slice(0, 20));
    });
    AppleWallet.setLogListener().catch(() => undefined);

    return () => {
      subscription.remove();
      AppleWallet.removeLogListener().catch(() => undefined);
    };
  }, []);

  const runTest = async (): Promise<void> => {
    if (!AppleWallet) {
      setResult({ moduleFound: false });
      return;
    }

    setRunning(true);
    try {
      const constants = AppleWallet.getConstants?.() ?? {};
      const environment: string = await AppleWallet.getEnvironment();
      const openWallet: boolean = await AppleWallet.openWallet();
      setResult({ moduleFound: true, constants, environment, openWallet });
    } catch (error) {
      setResult({ moduleFound: true, error: String(error) });
    } finally {
      setRunning(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Apple Wallet</Text>

      <TouchableOpacity
        style={[styles.button, running && styles.buttonDisabled]}
        onPress={runTest}
        disabled={running}
      >
        <Text style={styles.buttonText}>
          {running ? 'Testando...' : 'Testar módulo AppleWallet'}
        </Text>
      </TouchableOpacity>

      {result && (
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Resultado</Text>
          <Row label="Módulo encontrado" value={result.moduleFound} />
          {result.environment !== undefined && (
            <Row label="getEnvironment()" value={result.environment} />
          )}
          {result.openWallet !== undefined && (
            <Row label="openWallet()" value={result.openWallet} />
          )}
          {result.error && <Text style={styles.error}>{result.error}</Text>}
          {result.constants && (
            <>
              <Text style={styles.cardSubtitle}>Constantes</Text>
              {Object.entries(result.constants).map(([key, value]) => (
                <Row key={key} label={key} value={String(value)} />
              ))}
            </>
          )}
        </View>
      )}

      {logs.length > 0 && (
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Logs nativos</Text>
          {logs.map((log, index) => (
            <Text key={index} style={styles.log}>
              [{log.level}] {log.tag}: {log.message}
            </Text>
          ))}
        </View>
      )}
    </ScrollView>
  );
}

function Row({
  label,
  value,
}: {
  label: string;
  value: string | boolean;
}): React.JSX.Element {
  return (
    <View style={styles.row}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Text style={styles.rowValue}>{String(value)}</Text>
    </View>
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
    marginBottom: 20,
    color: '#333',
  },
  button: {
    backgroundColor: '#000',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 15,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: 8,
    padding: 16,
    marginBottom: 15,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 12,
  },
  cardSubtitle: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#333',
    marginTop: 12,
    marginBottom: 6,
  },
  row: {
    paddingVertical: 4,
  },
  rowLabel: {
    fontSize: 12,
    color: '#666',
  },
  rowValue: {
    fontSize: 14,
    color: '#333',
  },
  error: {
    color: 'red',
    fontSize: 14,
    marginTop: 8,
  },
  log: {
    fontSize: 12,
    color: '#333',
    fontFamily: 'Menlo',
    marginBottom: 4,
  },
});

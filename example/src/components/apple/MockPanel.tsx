// Controle do mock nativo (build Debug), pelo entrypoint de testes da lib.
import { useCallback, useEffect, useState } from 'react';
import { Text } from 'react-native';
import {
  AppleWalletImplementation,
  AppleWalletMockControl,
  AppleWalletMockScenario,
  type AppleWalletMockState,
} from '@platformbuilders/wallet-bridge-react-native/testing';
import { Button, Card, Chips, Row, styles } from './ui';

const SCENARIOS = Object.values(AppleWalletMockScenario);

export function MockPanel({
  onChange,
}: {
  onChange: () => void;
}): React.JSX.Element {
  const [state, setState] = useState<AppleWalletMockState | null>(null);
  const [implementation, setImplementation] =
    useState<AppleWalletImplementation | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      const active = await AppleWalletMockControl.getImplementation();
      setImplementation(active);
      setState(
        active === AppleWalletImplementation.MOCK
          ? await AppleWalletMockControl.getState()
          : null,
      );
      setError(null);
    } catch (refreshError) {
      setError(String((refreshError as Error).message ?? refreshError));
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const switchTo = async (name: AppleWalletImplementation) => {
    try {
      await AppleWalletMockControl.setImplementation(name);
    } catch (switchError) {
      setError(String((switchError as Error).message ?? switchError));
    }
    await refresh();
    onChange();
  };

  const selectScenario = async (scenario: AppleWalletMockScenario) => {
    setState(await AppleWalletMockControl.configure({ scenario }));
    onChange();
  };

  const reset = async () => {
    await AppleWalletMockControl.reset();
    await refresh();
    onChange();
  };

  return (
    <Card title="Implementação">
      <Row label="ativa" value={implementation} />
      <Chips
        options={Object.values(AppleWalletImplementation)}
        value={implementation ?? AppleWalletImplementation.REAL}
        onChange={switchTo}
      />
      {state && (
        <>
          <Text style={styles.muted}>Cenário do mock</Text>
          <Chips
            options={SCENARIOS}
            value={state.scenario}
            onChange={selectScenario}
          />
          <Row label="Watch pareado" value={state.isWatchPaired} />
          <Row label="passes semeados" value={state.passes.length} />
          <Button title="Resetar mock" variant="secondary" onPress={reset} />
        </>
      )}
      {error && <Text style={styles.error}>{error}</Text>}
    </Card>
  );
}

// Componentes visuais compartilhados pelas telas Apple do example.
import type { ReactNode } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

export function Card({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}): React.JSX.Element {
  return (
    <View style={styles.card}>
      <Text style={styles.cardTitle}>{title}</Text>
      {children}
    </View>
  );
}

export function Button({
  title,
  onPress,
  disabled = false,
  variant = 'primary',
}: {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  variant?: 'primary' | 'secondary' | 'danger';
}): React.JSX.Element {
  return (
    <TouchableOpacity
      style={[
        styles.button,
        styles[variant],
        disabled && styles.buttonDisabled,
      ]}
      onPress={onPress}
      disabled={disabled}
    >
      <Text style={styles.buttonText}>{title}</Text>
    </TouchableOpacity>
  );
}

export function Row({
  label,
  value,
}: {
  label: string;
  value: string | number | boolean | undefined | null;
}): React.JSX.Element {
  return (
    <View style={styles.row}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Text style={styles.rowValue} selectable>
        {String(value ?? '—')}
      </Text>
    </View>
  );
}

export function Chips<T extends string>({
  options,
  value,
  onChange,
}: {
  options: readonly T[];
  value: T;
  onChange: (value: T) => void;
}): React.JSX.Element {
  return (
    <View style={styles.chips}>
      {options.map((option) => (
        <TouchableOpacity
          key={option}
          style={[styles.chip, option === value && styles.chipActive]}
          onPress={() => onChange(option)}
        >
          <Text
            style={[styles.chipText, option === value && styles.chipTextActive]}
          >
            {option}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

export const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 12,
  },
  button: {
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 10,
    alignItems: 'center',
    marginBottom: 8,
  },
  primary: { backgroundColor: '#000' },
  secondary: { backgroundColor: '#555' },
  danger: { backgroundColor: '#b3261e' },
  buttonDisabled: { opacity: 0.4 },
  buttonText: { color: '#fff', fontWeight: '600' },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 4,
    gap: 12,
  },
  rowLabel: { color: '#666', fontSize: 13 },
  rowValue: { color: '#111', fontSize: 13, flexShrink: 1, textAlign: 'right' },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginBottom: 8 },
  chip: {
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#999',
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  chipActive: { backgroundColor: '#000', borderColor: '#000' },
  chipText: { fontSize: 11, color: '#333' },
  chipTextActive: { color: '#fff' },
  error: { color: '#b3261e', fontSize: 13, marginTop: 4 },
  muted: { color: '#666', fontSize: 12, marginBottom: 8 },
  mono: { fontFamily: 'Menlo', fontSize: 11, color: '#333' },
});

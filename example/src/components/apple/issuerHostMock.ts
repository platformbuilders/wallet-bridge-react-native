// ============================================================================
// EMISSOR FALSO (só para o example)
// ============================================================================
// No app real, o app manda o passo 1 (certificados + nonce + nonceSignature)
// ao backend do emissor e recebe de volta os 3 blobs do passo 2. A lib não
// chama backend nenhum.
//
// Aqui o "emissor" devolve blobs falsos (Base64 de textos "FAKE_"), no mesmo
// formato que o app recebe: { activationData, encryptedPassData,
// ephemeralPublicKey }. Só funciona com o mock nativo; o PassKit real rejeita.

import type {
  AppleProvisioningCertificatesEvent,
  AppleSecurePayload,
} from '@platformbuilders/wallet-bridge-react-native';

const base64 = (text: string): string => {
  const chars =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
  let output = '';
  for (let index = 0; index < text.length; index += 3) {
    const a = text.charCodeAt(index);
    const b = text.charCodeAt(index + 1);
    const c = text.charCodeAt(index + 2);
    const triple = (a << 16) | ((b || 0) << 8) | (c || 0);
    output += chars[(triple >> 18) & 63];
    output += chars[(triple >> 12) & 63];
    output += Number.isNaN(b) ? '=' : chars[(triple >> 6) & 63];
    output += Number.isNaN(c) ? '=' : chars[triple & 63];
  }
  return output;
};

const wait = (ms: number) =>
  new Promise<void>((resolve) => setTimeout(resolve, ms));

/** "Endpoint de push provisioning" do emissor falso. */
export const requestSecurePayload = async (
  event: AppleProvisioningCertificatesEvent,
): Promise<AppleSecurePayload> => {
  await wait(500);
  const request = event.requestId;
  return {
    activationData: base64(`FAKE_ACTIVATION_DATA|${request}`),
    encryptedPassData: base64(`FAKE_ENCRYPTED_PASS_DATA|${request}`),
    ephemeralPublicKey: base64(`FAKE_EPHEMERAL_PUBLIC_KEY|${request}`),
  };
};

/** "Endpoint de ativação" do emissor falso (In-App Verification). */
export const requestActivationData = async (
  deviceAccountIdentifier: string,
): Promise<string> => {
  await wait(300);
  return base64(`FAKE_ACTIVATION_DATA|${deviceAccountIdentifier}`);
};

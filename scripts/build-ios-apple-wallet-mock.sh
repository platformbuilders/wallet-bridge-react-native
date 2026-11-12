#!/bin/bash

# Builda e instala o apple-wallet-app-mock no simulador iOS aberto.
# Espelha build-android-google-wallet-mock.sh: gera projeto -> build -> install -> launch.
#
# Uso:
#   ./scripts/build-ios-apple-wallet-mock.sh
#   APPLE_WALLET_MOCK_TARGET_SCHEME=meuapp ./scripts/build-ios-apple-wallet-mock.sh

set -e
set -o pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MOCK_DIR="$ROOT_DIR/apple-wallet-app-mock"
PROJECT="$MOCK_DIR/AppleWalletMock.xcodeproj"
SCHEME="AppleWalletMock"
BUNDLE_ID="${APPLE_WALLET_MOCK_BUNDLE_ID:-com.builders.applewalletmock}"
DERIVED="$MOCK_DIR/build"

echo "🚀 Build do apple-wallet-app-mock..."

if ! command -v xcodebuild &> /dev/null; then
  echo "❌ xcodebuild não encontrado. Instale o Xcode."
  exit 1
fi

DEVICE_ID=$(xcrun simctl list devices booted -j \
  | python3 -c "import json,sys; d=json.load(sys.stdin)['devices']; ids=[x['udid'] for v in d.values() for x in v if x.get('state')=='Booted']; print(ids[0] if ids else '')")

if [ -z "$DEVICE_ID" ]; then
  echo "❌ Nenhum simulador iOS aberto. Abra um (ou rode 'yarn example ios') e tente de novo."
  exit 1
fi
echo "📱 Simulador: $DEVICE_ID"

# O projeto não é versionado: é sempre regenerado a partir dos fontes.
echo "🔧 Gerando projeto Xcode..."
(cd "$ROOT_DIR" && BUNDLE_GEMFILE=example/Gemfile bundle exec ruby apple-wallet-app-mock/generate-project.rb)

rm -rf "$DERIVED"

echo "🔨 Buildando..."
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration Debug \
  -destination "id=$DEVICE_ID" \
  -derivedDataPath "$DERIVED" \
  build \
  | grep -E "error:|BUILD SUCCEEDED|BUILD FAILED" || true

APP_PATH="$DERIVED/Build/Products/Debug-iphonesimulator/$SCHEME.app"
if [ ! -f "$APP_PATH/$SCHEME" ]; then
  echo "❌ Binário não foi gerado em $APP_PATH/$SCHEME"
  exit 1
fi

echo "📱 Instalando..."
xcrun simctl install "$DEVICE_ID" "$APP_PATH"
xcrun simctl launch "$DEVICE_ID" "$BUNDLE_ID" || echo "⚠️  Instalado, mas não abriu sozinho."

echo "✅ apple-wallet-app-mock instalado."
echo ""
echo "💡 Mesmo fluxo pela CLI:"
echo "   xcrun simctl openurl $DEVICE_ID \\"
echo "     \"builderswalletexample://apple-wallet/verify?deviceAccountIdentifier=<DPANID>\""

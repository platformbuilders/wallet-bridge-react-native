#!/bin/bash

# Script para buildar e rodar o app iOS example no simulador
# Executa: yarn install -> pod install -> react-native run-ios
#
# No iOS a lib entra no example pelo `root: '..'` do example/react-native.config.js,
# então não precisa de yalc: mudança nativa só pede rebuild.
#
# Uso:
#   ./scripts/build-ios-example.sh                      # simulador padrão, Debug
#   ./scripts/build-ios-example.sh --device "iPhone 17"  # simulador pelo nome
#   ./scripts/build-ios-example.sh --udid <UDID>         # simulador ou iPhone físico pelo UDID
#   ./scripts/build-ios-example.sh --release             # configuração Release
#   ./scripts/build-ios-example.sh --clean               # apaga ios/build e reinstala os pods
#   ./scripts/build-ios-example.sh --skip-pods           # pula o pod install
#
# iPhone físico (precisa de assinatura; o time não fica salvo no projeto):
#   ./scripts/build-ios-example.sh --udid <UDID> --team <TEAM_ID>
#   ./scripts/build-ios-example.sh --udid <UDID> --team <TEAM_ID> --bundle-id <ID>
#   IOS_DEVELOPMENT_TEAM=<TEAM_ID> ./scripts/build-ios-example.sh --udid <UDID>
#   ./scripts/build-ios-example.sh --list-devices        # lista iPhones e UDIDs

set -e # Para o script se algum comando falhar

DEVICE=""
UDID=""
TEAM="${IOS_DEVELOPMENT_TEAM:-}"
BUNDLE_ID=""
MODE="Debug"
CLEAN=false
SKIP_PODS=false

while [ $# -gt 0 ]; do
  case "$1" in
    --device) DEVICE="$2"; shift 2 ;;
    --device=*) DEVICE="${1#*=}"; shift ;;
    --udid) UDID="$2"; shift 2 ;;
    --udid=*) UDID="${1#*=}"; shift ;;
    --release) MODE="Release"; shift ;;
    --clean) CLEAN=true; shift ;;
    --skip-pods) SKIP_PODS=true; shift ;;
    --team) TEAM="$2"; shift 2 ;;
    --team=*) TEAM="${1#*=}"; shift ;;
    --bundle-id) BUNDLE_ID="$2"; shift 2 ;;
    --bundle-id=*) BUNDLE_ID="${1#*=}"; shift ;;
    --list-devices) xcrun xctrace list devices 2>/dev/null | sed -n '/== Devices ==/,/== Simulators ==/p' | grep -v '=='; exit 0 ;;
    -h|--help) sed -n '3,22p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "❌ Opção desconhecida: $1 (use --help)"; exit 1 ;;
  esac
done

if [ "$(uname)" != "Darwin" ]; then
  echo "❌ Build iOS só roda no macOS."
  exit 1
fi

# UDID de iPhone físico sem time: o build falha na assinatura.
if [ -n "$UDID" ] && [ -z "$TEAM" ] && ! xcrun simctl list devices | grep -q "$UDID"; then
  echo "⚠️  $UDID não é um simulador. Para iPhone físico passe --team <TEAM_ID>"
  echo "   (ou IOS_DEVELOPMENT_TEAM). Times disponíveis nos seus certificados:"
  security find-identity -v -p codesigning 2>/dev/null \
    | sed -n 's/.*"\(.*\)"/\1/p' \
    | while read -r identity; do
        team=$(security find-certificate -c "$identity" -p 2>/dev/null \
          | openssl x509 -noout -subject 2>/dev/null \
          | sed -n 's/.*OU *= *\([A-Z0-9]*\).*/\1/p')
        echo "   - $team ($identity)"
      done
  exit 1
fi

echo "🚀 Iniciando build do app iOS example ($MODE)..."

# Navega para o diretório example (relativo ao script)
cd "$(dirname "$0")/../example"

echo "📥 Instalando dependências..."
yarn install

if [ "$CLEAN" = true ]; then
  echo "🧹 Limpando build e pods do iOS..."
  rm -rf ios/build ios/Pods
fi

if [ "$SKIP_PODS" = false ]; then
  echo "📦 Instalando pods..."
  cd ios
  # Prefere o CocoaPods do Gemfile (mesma versão que o run-ios usa) e cai no
  # global só se não houver Bundler.
  if [ -f ../Gemfile ] && command -v bundle >/dev/null 2>&1; then
    bundle install
    bundle exec pod install
  elif command -v pod >/dev/null 2>&1; then
    pod install
  else
    echo "❌ CocoaPods não encontrado. Instale com: brew install cocoapods"
    exit 1
  fi
  cd ..
fi

RUN_ARGS=(--mode "$MODE")
if [ -n "$UDID" ]; then
  RUN_ARGS+=(--udid "$UDID")
elif [ -n "$DEVICE" ]; then
  RUN_ARGS+=(--simulator "$DEVICE")
fi

# Assinatura para iPhone físico. Vai só na linha de comando do xcodebuild, sem
# alterar o project.pbxproj (o time de cada dev não entra no commit).
EXTRA_PARAMS=""
if [ -n "$TEAM" ]; then
  EXTRA_PARAMS="DEVELOPMENT_TEAM=$TEAM CODE_SIGN_STYLE=Automatic -allowProvisioningUpdates"
fi
if [ -n "$BUNDLE_ID" ]; then
  EXTRA_PARAMS="$EXTRA_PARAMS PRODUCT_BUNDLE_IDENTIFIER=$BUNDLE_ID"
fi
if [ -n "$EXTRA_PARAMS" ]; then
  RUN_ARGS+=(--extra-params "$EXTRA_PARAMS")
fi


echo "🔨 Buildando o app iOS..."
npx react-native run-ios "${RUN_ARGS[@]}"

echo "✅ Build do app iOS example concluído com sucesso!"

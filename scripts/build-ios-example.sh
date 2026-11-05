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
#   ./scripts/build-ios-example.sh --udid <UDID>         # simulador pelo UDID
#   ./scripts/build-ios-example.sh --release             # configuração Release
#   ./scripts/build-ios-example.sh --clean               # apaga ios/build e reinstala os pods
#   ./scripts/build-ios-example.sh --skip-pods           # pula o pod install

set -e # Para o script se algum comando falhar

DEVICE=""
UDID=""
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
    -h|--help) sed -n '3,16p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "❌ Opção desconhecida: $1 (use --help)"; exit 1 ;;
  esac
done

if [ "$(uname)" != "Darwin" ]; then
  echo "❌ Build iOS só roda no macOS."
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
  if [ -f ../Gemfile ] && command -v bundle >/dev/null 2>&1; then
    bundle install
    bundle exec pod install
  else
    pod install
  fi
  cd ..
fi

RUN_ARGS=(--mode "$MODE")
if [ -n "$UDID" ]; then
  RUN_ARGS+=(--udid "$UDID")
elif [ -n "$DEVICE" ]; then
  RUN_ARGS+=(--simulator "$DEVICE")
fi

echo "🔨 Buildando o app iOS..."
npx react-native run-ios "${RUN_ARGS[@]}"

echo "✅ Build do app iOS example concluído com sucesso!"

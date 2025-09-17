#!/bin/bash

# Script para trocar o package name do app de exemplo
# Uso: ./change-package-name-app-example.sh --package-name=com.test.new

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Função para mostrar ajuda
show_help() {
    echo "Script para trocar o package name do app de exemplo"
    echo ""
    echo "Uso:"
    echo "  $0 --package-name=COM.TEST.NEW"
    echo ""
    echo "Exemplos:"
    echo "  $0 --package-name=com.walletapp.example"
    echo "  $0 --package-name=br.com.pefisa.pefisa.hml"
    echo "  $0 --package-name=com.test.new"
    echo ""
    echo "Opções:"
    echo "  --package-name=NAME    Novo package name (obrigatório)"
    echo "  --help, -h             Mostrar esta ajuda"
    echo ""
}

# Verificar se está na pasta correta
if [ ! -f "package.json" ] || [ ! -d "example/android" ]; then
    error "Execute este script na pasta raiz do projeto react-native-builders-wallet"
    exit 1
fi

# Parse dos argumentos
PACKAGE_NAME=""
HELP=false

for arg in "$@"; do
    case $arg in
        --package-name=*)
            PACKAGE_NAME="${arg#*=}"
            ;;
        --help|-h)
            HELP=true
            ;;
        *)
            error "Argumento desconhecido: $arg"
            show_help
            exit 1
            ;;
    esac
done

# Mostrar ajuda se solicitado
if [ "$HELP" = true ]; then
    show_help
    exit 0
fi

# Verificar se o package name foi fornecido
if [ -z "$PACKAGE_NAME" ]; then
    error "Package name é obrigatório"
    show_help
    exit 1
fi

# Validar formato do package name
if [[ ! "$PACKAGE_NAME" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$ ]]; then
    error "Package name inválido. Use o formato: com.example.app"
    exit 1
fi

# Diretório base do exemplo
EXAMPLE_DIR="example/android"

# Detectar package name atual do settings.gradle
CURRENT_PACKAGE=$(grep "rootProject.name" "$EXAMPLE_DIR/settings.gradle" | sed "s/.*rootProject.name = '\(.*\)'.*/\1/")

# Se não conseguir detectar do settings.gradle, tentar dos arquivos Java
if [ -z "$CURRENT_PACKAGE" ]; then
    CURRENT_PACKAGE=$(find "$JAVA_DIR" -name "*.kt" -o -name "*.java" | head -1 | xargs grep "package " | sed "s/package \(.*\)/\1/" | head -1)
fi

if [ -z "$CURRENT_PACKAGE" ]; then
    error "Não foi possível detectar o package name atual"
    exit 1
fi

# Verificar se o package name é o mesmo
if [ "$CURRENT_PACKAGE" = "$PACKAGE_NAME" ]; then
    warning "Package name já é '$PACKAGE_NAME'. Nenhuma alteração necessária."
    exit 0
fi

log "Trocando package name de '$CURRENT_PACKAGE' para '$PACKAGE_NAME'"
APP_DIR="$EXAMPLE_DIR/app"
JAVA_DIR="$APP_DIR/src/main/java"

# Função para criar estrutura de diretórios baseada no package name
create_package_structure() {
    local package_name="$1"
    local base_dir="$2"
    
    # Converter package name em caminho de diretório
    local package_path=$(echo "$package_name" | tr '.' '/')
    local full_path="$base_dir/$package_path"
    
    mkdir -p "$full_path"
    
    echo "$full_path"
}

# Função para substituir package name em arquivo
replace_package_in_file() {
    local file_path="$1"
    local old_package="$2"
    local new_package="$3"
    
    if [ -f "$file_path" ]; then
        log "Atualizando $file_path"
        sed -i.bak "s/$old_package/$new_package/g" "$file_path"
        rm -f "$file_path.bak"
    fi
}

# Função para substituir package name em arquivo Gradle
replace_package_in_gradle() {
    local file_path="$1"
    local old_package="$2"
    local new_package="$3"
    
    if [ -f "$file_path" ]; then
        log "Atualizando $file_path"
        # Escapar pontos para regex
        local old_escaped=$(echo "$old_package" | sed 's/\./\\./g')
        local new_escaped=$(echo "$new_package" | sed 's/\./\\./g')
        
        sed -i.bak "s/$old_escaped/$new_escaped/g" "$file_path"
        rm -f "$file_path.bak"
    fi
}

# 1. Atualizar settings.gradle
log "Atualizando settings.gradle"
replace_package_in_gradle "$EXAMPLE_DIR/settings.gradle" "$CURRENT_PACKAGE" "$PACKAGE_NAME"

# 2. Atualizar build.gradle do app
log "Atualizando build.gradle do app"
replace_package_in_gradle "$APP_DIR/build.gradle" "$CURRENT_PACKAGE" "$PACKAGE_NAME"

# 2.1. Atualizar namespace no build.gradle (além do applicationId)
log "Atualizando namespace no build.gradle"
sed -i.bak "s/namespace \".*\"/namespace \"$PACKAGE_NAME\"/g" "$APP_DIR/build.gradle"
rm -f "$APP_DIR/build.gradle.bak"

# 2.2. Atualizar applicationId no build.gradle
log "Atualizando applicationId no build.gradle"
sed -i.bak "s/applicationId \".*\"/applicationId \"$PACKAGE_NAME\"/g" "$APP_DIR/build.gradle"
rm -f "$APP_DIR/build.gradle.bak"

# 3. Atualizar AndroidManifest.xml
log "Atualizando AndroidManifest.xml"
replace_package_in_file "$APP_DIR/src/main/AndroidManifest.xml" "$CURRENT_PACKAGE" "$PACKAGE_NAME"

# 4. Criar nova estrutura de diretórios
log "Criando nova estrutura de diretórios"
NEW_JAVA_DIR=$(create_package_structure "$PACKAGE_NAME" "$JAVA_DIR" 2>/dev/null)

# 5. Mover arquivos para nova estrutura
log "Movendo arquivos para nova estrutura"
OLD_PACKAGE_PATH=$(echo "$CURRENT_PACKAGE" | tr '.' '/')
OLD_JAVA_DIR="$JAVA_DIR/$OLD_PACKAGE_PATH"

if [ -d "$OLD_JAVA_DIR" ]; then
    # Mover arquivos
    mv "$OLD_JAVA_DIR"/* "$NEW_JAVA_DIR/"
    
    # Remover diretórios antigos vazios (de trás para frente)
    current_dir="$OLD_JAVA_DIR"
    while [ "$current_dir" != "$JAVA_DIR" ] && [ -d "$current_dir" ]; do
        rmdir "$current_dir" 2>/dev/null || break
        current_dir=$(dirname "$current_dir")
    done
else
    warning "Diretório antigo não encontrado: $OLD_JAVA_DIR"
fi

# 6. Atualizar arquivos Java/Kotlin
log "Atualizando arquivos Java/Kotlin"
for file in "$NEW_JAVA_DIR"/*.kt "$NEW_JAVA_DIR"/*.java; do
    if [ -f "$file" ]; then
        # Atualizar package name
        replace_package_in_file "$file" "$CURRENT_PACKAGE" "$PACKAGE_NAME"
        
        # Atualizar imports que referenciam o package antigo
        sed -i.bak "s/import $CURRENT_PACKAGE\./import $PACKAGE_NAME\./g" "$file"
        rm -f "$file.bak"
    fi
done

# 7. Atualizar gradle.properties se existir
if [ -f "$EXAMPLE_DIR/gradle.properties" ]; then
    log "Atualizando gradle.properties"
    replace_package_in_gradle "$EXAMPLE_DIR/gradle.properties" "$CURRENT_PACKAGE" "$PACKAGE_NAME"
fi

# 8. Atualizar build.gradle principal se necessário
if [ -f "$EXAMPLE_DIR/build.gradle" ]; then
    log "Atualizando build.gradle principal"
    replace_package_in_gradle "$EXAMPLE_DIR/build.gradle" "$CURRENT_PACKAGE" "$PACKAGE_NAME"
fi

# 8.1. Atualizar proguard-rules.pro para manter BuildConfig acessível
if [ -f "$APP_DIR/proguard-rules.pro" ]; then
    log "Atualizando regras do Proguard para manter BuildConfig acessível"
    
    # Remove regras antigas de BuildConfig específicas
    sed -i.bak '/^-keep class.*\.BuildConfig { \*; }$/d' "$APP_DIR/proguard-rules.pro"
    
    # Adiciona regra genérica para qualquer namespace se não existir
    if ! grep -q "keep class \*\*\.BuildConfig" "$APP_DIR/proguard-rules.pro"; then
        # Insere após a linha de comentário "Add any project specific keep options here:"
        sed -i.bak '/^# Add any project specific keep options here:/a\
# Keep BuildConfig for any namespace (supports dynamic package changes)\
-keep class **.BuildConfig { *; }' "$APP_DIR/proguard-rules.pro"
        log "Regra genérica do Proguard adicionada para manter BuildConfig de qualquer namespace"
    else
        log "Regra genérica do Proguard já existe para BuildConfig"
    fi
    
    rm -f "$APP_DIR/proguard-rules.pro.bak"
fi

# 9. Verificar se há outros arquivos que referenciam o package name
log "Verificando outros arquivos que podem referenciar o package name"
find "$EXAMPLE_DIR" -type f \( -name "*.xml" -o -name "*.gradle" -o -name "*.properties" -o -name "*.kt" -o -name "*.java" \) -exec grep -l "$CURRENT_PACKAGE" {} \; | while read file; do
    if [ -f "$file" ]; then
        log "Atualizando referências em $file"
        replace_package_in_file "$file" "$CURRENT_PACKAGE" "$PACKAGE_NAME"
    fi
done

# 10. Atualizar variáveis do GoogleWalletAppMock
MOCK_GRADLE_PROPERTIES="GoogleWalletAppMock/gradle.properties"
if [ -f "$MOCK_GRADLE_PROPERTIES" ]; then
    log "Atualizando variáveis do GoogleWalletAppMock"
    TARGET_ACTION="${PACKAGE_NAME}.action.ACTIVATE_TOKEN"
    
    # Atualizar targetAppPackage
    sed -i.bak "s/targetAppPackage=.*/targetAppPackage=$PACKAGE_NAME/" "$MOCK_GRADLE_PROPERTIES"
    # Atualizar targetAppAction
    sed -i.bak "s/targetAppAction=.*/targetAppAction=$TARGET_ACTION/" "$MOCK_GRADLE_PROPERTIES"
    rm -f "$MOCK_GRADLE_PROPERTIES.bak"
    
    success "GoogleWalletAppMock atualizado para target: $PACKAGE_NAME"
else
    warning "Arquivo gradle.properties do mock não encontrado: $MOCK_GRADLE_PROPERTIES"
fi

# 11. Limpar e rebuild
log "Limpando build anterior e arquivos gerados"
cd "$EXAMPLE_DIR"

# Limpar build do Gradle
./gradlew clean > /dev/null 2>&1 || true

# Remover toda a pasta app/build e outros arquivos gerados
log "Removendo toda a pasta app/build e arquivos gerados"
rm -rf app/build/ 2>/dev/null || true
rm -rf build/ 2>/dev/null || true
rm -rf .gradle/ 2>/dev/null || true

cd - > /dev/null

success "Package name alterado com sucesso!"
success "Novo package name: $PACKAGE_NAME"
success "Estrutura de diretórios: $NEW_JAVA_DIR"

echo ""
log "Próximos passos:"
echo "1. cd example/android"
echo "2. ./gradlew clean"
echo "3. cd .."
echo "4. yarn android"
echo "5. cd ../GoogleWalletAppMock"
echo "6. ./gradlew assembleDebug"
echo "7. adb install app/build/outputs/apk/debug/app-debug.apk"
echo "8. Teste o app com o novo package name"

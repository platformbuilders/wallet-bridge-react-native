# Scripts de Limpeza

Este diretório contém scripts para limpeza e reinstalação do projeto.

## Scripts Disponíveis

### 🚀 Scripts Master (Executam tudo)

#### `clean-all.sh` - Limpeza completa de tudo
```bash
./scripts/clean-all.sh
```
- Limpa lib principal + example
- Remove todos os caches
- Reinstala tudo do zero

#### `clean-all-yarn.sh` - Limpeza rápida de tudo
```bash
./scripts/clean-all-yarn.sh
```
- Limpeza rápida (apenas Yarn)
- Lib principal + example

### 📦 Scripts da Lib Principal

#### `clean-lib.sh` - Limpeza completa da lib
```bash
./scripts/clean-lib.sh
```
- Limpa cache do Yarn
- Remove node_modules
- Limpa builds
- Reinstala dependências
- Executa `yarn prepare` (build da lib)

#### `clean-lib-yarn.sh` - Limpeza rápida da lib
```bash
./scripts/clean-lib-yarn.sh
```
- Limpeza rápida (apenas Yarn)
- Remove node_modules
- Reinstala dependências
- Executa `yarn prepare` (build da lib)

### 📱 Scripts do Example

#### `clean-example-full.sh` - Limpeza completa do example
```bash
./scripts/clean-example-full.sh
```
- Limpa cache do Yarn
- Limpa cache do React Native
- Limpa builds Android
- Remove node_modules
- Reinstala dependências
- Reinstala lib localmente

#### `clean-example-yarn.sh` - Limpeza rápida do example
```bash
./scripts/clean-example-yarn.sh
```
- Limpeza rápida (apenas Yarn)
- Remove node_modules
- Reinstala dependências
- Reinstala lib localmente

## Uso Recomendado

### Para desenvolvimento diário:
```bash
./scripts/clean-all-yarn.sh
```

### Para limpeza completa (quando há problemas):
```bash
./scripts/clean-all.sh
```

### Para limpar apenas a lib:
```bash
./scripts/clean-lib.sh
```

### Para limpar apenas o example:
```bash
./scripts/clean-example-full.sh
```

## Notas

- Todos os scripts são executáveis a partir da raiz do projeto
- Os scripts automaticamente navegam para os diretórios corretos
- Use `clean-all.sh` quando houver problemas persistentes
- Use `clean-all-yarn.sh` para limpeza rápida durante desenvolvimento

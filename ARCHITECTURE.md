# Arquitetura dos Módulos de Wallet

## Diagrama da Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                    React Native App                            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                BuildersWalletModule                            │
│              (Interface Unificada)                             │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│              WalletModuleFactory                               │
│            (Detecção de SDKs + Factory)                        │
└─────────────────────┬───────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼───────┐ ┌──▼──────┐ ┌────▼────────┐
│ GoogleTapAnd  │ │Samsung  │ │   Stub      │
│ PayAdapter    │ │PayAdapter│ │WalletModule │
└───────┬───────┘ └──┬──────┘ └─────────────┘
        │            │
        │            │
┌───────▼───────┐ ┌──▼──────┐
│GoogleTapAnd   │ │Samsung  │
│PayModule      │ │PayModule│
│(Original)     │ │(Original)│
└───────────────┘ └─────────┘
```

## Fluxo de Funcionamento

1. **Inicialização**: `BuildersWalletPackage` registra todos os módulos disponíveis
2. **Detecção**: `WalletModuleFactory` verifica quais SDKs estão disponíveis
3. **Criação**: Factory cria adapters apropriados baseados nos SDKs disponíveis
4. **Delegação**: `BuildersWalletModule` delega chamadas para o adapter ativo
5. **Adaptação**: Adapters convertem chamadas da interface comum para os módulos originais

## Módulos Registrados

### Sempre Disponível
- `BuildersWalletModule` - Interface unificada

### Condicionalmente Disponível
- `GoogleTapAndPayModule` - Se Google Pay SDK estiver disponível
- `SamsungPayModule` - Se Samsung Pay SDK estiver disponível

## Padrões Utilizados

### 1. Bridge Pattern
- **Problema**: Interfaces diferentes entre Google Pay e Samsung Pay
- **Solução**: Adapters que implementam `WalletModuleInterface`
- **Benefício**: Interface unificada sem modificar módulos originais

### 2. Factory Pattern
- **Problema**: Criação condicional de módulos baseada em SDKs disponíveis
- **Solução**: `WalletModuleFactory` com detecção automática
- **Benefício**: Configuração automática sem intervenção do usuário

### 3. Adapter Pattern
- **Problema**: Adaptar interfaces existentes para interface comum
- **Solução**: `GoogleTapAndPayAdapter` e `SamsungPayAdapter`
- **Benefício**: Reutilização de código existente

## Vantagens da Arquitetura

1. **Modularidade**: Cada provedor tem seu próprio módulo
2. **Extensibilidade**: Fácil adicionar novos provedores
3. **Flexibilidade**: Usar módulos específicos ou interface unificada
4. **Manutenibilidade**: Mudanças isoladas por módulo
5. **Testabilidade**: Cada componente pode ser testado independentemente
6. **Reutilização**: Módulos originais são reutilizados sem modificação

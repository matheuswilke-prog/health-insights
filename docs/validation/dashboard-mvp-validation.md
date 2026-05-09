# Dashboard MVP Validation

Data: 2026-05-08

## Resultado

Dashboard MVP implementado e validado por checks locais.

## Checks automatizados

- `.\gradlew.bat ktlintCheck` - passou.
- `.\gradlew.bat testDebugUnitTest` - passou.
- `.\gradlew.bat :app:assembleDebug` - passou.

Checks adicionais executados durante a implementacao:

- `.\gradlew.bat :feature:dashboard:compileDebugKotlin` - passou.
- `.\gradlew.bat :feature:dashboard:testDebugUnitTest :core:domain:test` - passou.
- `.\gradlew.bat :app:compileDebugKotlin` - passou.
- `.\gradlew.bat :feature:health-connect:testDebugUnitTest :feature:onboarding:testDebugUnitTest :app:assembleDebug` - passou apos correcao do fluxo de permissoes Health Connect.

## Cobertura implementada

- Regra de balanco usa `BMR + ActiveCaloriesBurned`.
- `TotalCaloriesBurned` nao participa do calculo do Dashboard.
- Limiar de manutencao ajustado para `-250 kcal` a `+250 kcal`, inclusivo.
- Ingestao `<= 0` vira estado sem ingestao, sem saldo calculado.
- Saldo nao e calculado sem permissao/consentimento de calorias ou sem leitura confiavel.
- Peso mostra valor mais recente e data da medicao quando disponivel, sem tendencia.
- Health Connect SDK permanece isolado fora da UI.
- Manifesto declara o rationale obrigatorio do Health Connect para Android 14+ via `android.intent.action.VIEW_PERMISSION_USAGE` e `android.intent.category.HEALTH_PERMISSIONS`.
- Contrato de calorias solicita apenas `ActiveCaloriesBurned`; `TotalCaloriesBurned` nao e solicitado nem usado pelo Dashboard.
- Dashboard real substitui o placeholder no NavHost.
- Foreground chama reload sem polling; ViewModel aplica cache curto e single-flight.
- Retry tem estado ocupado.
- Estado local invalido emite evento para refazer configuracao inicial.

## Testes adicionados/atualizados

- Dominio:
  - deficit abaixo de `-250`;
  - manutencao em `-250` e `+250`;
  - superavit acima de `+250`;
  - ingestao zero e negativa como `NoIntakeData`;
  - Health Connect indisponivel.
- Dashboard:
  - content completo;
  - sem ingestao sem saldo falso;
  - permissao parcial sem leitura de calorias;
  - Health Connect indisponivel preservando meta local;
  - estado local invalido;
  - foreground usando cache fresco;
  - formatacao de kcal, peso, data e labels.

## Validacao manual

APK debug instalado no emulador em 2026-05-08 apos correcao do fluxo de permissoes.

Validacoes via `adb dumpsys package com.healthinsights.app`:

- alias `ViewHealthPermissionUsageActivity` resolvido para `android.intent.action.VIEW_PERMISSION_USAGE`;
- categoria `android.intent.category.HEALTH_PERMISSIONS` presente;
- `READ_TOTAL_CALORIES_BURNED` ausente das permissoes solicitadas pelo pacote instalado;
- `READ_ACTIVE_CALORIES_BURNED`, `READ_NUTRITION`, `READ_WEIGHT` e `READ_EXERCISE` presentes.

Fluxo interativo de concessao deve ser reexecutado pelo usuario no emulador apos a instalacao corrigida.

Cenarios recomendados para validacao manual:

- Onboarding concluido abre Dashboard real.
- Reabrir app com `onboarding_complete = true` abre Dashboard.
- Revogar permissao fora do app e voltar ao Dashboard.
- Dispositivo/emulador sem Health Connect.
- Cenarios com apenas peso, apenas calorias e nenhum dado.
- Conferir visualmente os 8 estados do handoff em viewport Android comum.

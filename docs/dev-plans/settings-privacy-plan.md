# Settings Privacy Dev Plan

Data: 2026-05-08

## Ordem

1. Criar modelos de UI de Settings.
2. Criar mapper de consentimentos.
3. Criar `SettingsViewModel` lendo `ConsentRepository`.
4. Implementar `SettingsScreen` Compose.
5. Integrar rota `settings` no NavHost.
6. Conectar botao de configuracoes do Dashboard.
7. Adicionar testes de mapper e UI Compose.
8. Rodar validacao.

## Arquivos Principais

- `feature/settings/src/main/kotlin/com/healthinsights/feature/settings/SettingsUiModel.kt`
- `feature/settings/src/main/kotlin/com/healthinsights/feature/settings/SettingsViewModel.kt`
- `feature/settings/src/main/kotlin/com/healthinsights/feature/settings/SettingsScreen.kt`
- `app/src/main/java/com/healthinsights/app/AppNavHost.kt`
- `app/src/main/java/com/healthinsights/app/MainViewModel.kt`
- `app/build.gradle.kts`

## Validacao

- `.\gradlew.bat :feature:settings:testDebugUnitTest :app:assembleDebug`
- `.\gradlew.bat ktlintCheck`
- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat :app:assembleDebug`

## Riscos

- Atalho do Health Connect pode abrir o app do Health Connect, nao necessariamente a subpagina exata de permissoes, dependendo da versao do Android.
- Exportacao e exclusao ainda ficam para os proximos epicos.

# Settings Privacy Validation

Data: 2026-05-08

## Resultado

Tela minima de privacidade implementada e validada para alpha local.

## Checks

- `.\gradlew.bat :feature:settings:testDebugUnitTest :app:assembleDebug` - passou.
- `.\gradlew.bat ktlintCheck` - passou.
- `.\gradlew.bat testDebugUnitTest` - passou.
- `.\gradlew.bat :app:assembleDebug` - passou.

## Cobertura Implementada

- Tela de privacidade acessivel pelo Dashboard.
- Lista de consentimentos atuais do MVP.
- Resumo de politica de privacidade.
- Atalho para abrir Health Connect.
- Sem exportar/apagar dados nesta etapa.

## Limitacoes Conhecidas

- O atalho abre o Health Connect via intent do Android; a subpagina exata pode variar conforme versao do sistema.
- Exportar dados e apagar dados locais permanecem nos epicos `EP-04-02` e `EP-04-03`.

# Settings Export/Delete Validation

Data: 2026-05-09

Escopo:

- EP-04-02 - Exportar Dados.
- EP-04-03 - Apagar Dados Locais.

## Implementacao Validada

- Exportacao gera JSON local com perfil, objetivo, meta calorica e consentimentos.
- Exportacao usa o seletor nativo do Android para salvar `application/json`.
- Exportacao nao chama Health Connect e nao usa rede.
- Exclusao limpa perfil e consentimentos pelos repositorios locais.
- Exclusao reseta `onboarding_complete` no fluxo do app e retorna para Welcome.
- UI exige confirmacao antes de apagar dados locais.

## Checks Executados

- `gradlew.bat ktlintCheck`: passou.
- `gradlew.bat testDebugUnitTest`: passou.
- `gradlew.bat :app:assembleDebug`: passou.

Observacao: uma tentativa inicial de `:app:assembleDebug` em paralelo com `testDebugUnitTest` falhou em cache KSP de `:feature:onboarding`. Reexecutado isoladamente, passou.

## Cobertura de Testes

- `ExportLocalDataUseCaseTest` cobre formato JSON, perfil ausente e ausencia de campos fora do escopo coletado.
- `DeleteLocalDataUseCaseTest` cobre limpeza de perfil e consentimentos.
- `SettingsScreenTest` cobre CTA de exportacao, dialog de exclusao, cancelar e confirmar.

## Riscos Residuais

- Validacao manual ainda deve confirmar o seletor de arquivo Android em emulador/aparelho.
- Validacao manual ainda deve confirmar a navegacao para Welcome depois de apagar dados locais.

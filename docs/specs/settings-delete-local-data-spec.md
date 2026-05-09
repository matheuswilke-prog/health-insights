# Settings Delete Local Data Spec

Data: 2026-05-09

## Objetivo

Permitir que o usuario apague os dados locais armazenados pelo Health Insights e reinicie o app para o fluxo de onboarding.

## Escopo

- Apagar dados locais sensiveis mantidos pelo app:
  - perfil corporal;
  - objetivo;
  - meta calorica diaria;
  - registros de consentimento;
  - caches locais do app quando existirem.
- Resetar a flag nao sensivel `onboarding_complete`.
- Navegar para o inicio do onboarding apos a exclusao.
- Exibir confirmacao clara antes da exclusao.

## Fora de Escopo

- Apagar dados originais do Health Connect.
- Revogar permissoes no Health Connect automaticamente.
- Apagar dados de outros apps.
- Apagar backups de usuario, se o usuario tiver exportado JSON manualmente.

## Fluxo

1. Usuario abre Settings > Privacidade.
2. Tela mostra microcopy explicando o que sera apagado e o que nao sera apagado.
3. Usuario toca em "Apagar dados locais".
4. App exibe confirmacao.
5. Se o usuario cancela, nada muda.
6. Se o usuario confirma, app limpa repositorios locais sensiveis.
7. App reseta `onboarding_complete`.
8. App navega para Welcome.

## Estados

- Loading: enquanto consentimentos carregam.
- Content: tela normal com a acao de apagar.
- Confirmation: dialog de confirmacao.
- Delete in progress: CTA pode ficar indisponivel durante a limpeza.
- Delete complete: app retorna ao onboarding.
- Delete error: erro generico sem logar dados sensiveis.

## Dados Gravados ou Apagados

- `UserProfileRepository.clear()`.
- `ConsentRepository.clearAll()`.
- `onboarding_complete = false`.

## Regras de Privacidade

- Nao apagar dados originais do Health Connect.
- Nao tentar chamar APIs destrutivas do Health Connect.
- Nao logar perfil, peso, idade, altura, meta, calorias ou consentimentos.
- A confirmacao deve deixar claro que permissoes Android continuam gerenciadas no Health Connect.

## Acessibilidade

- Dialog deve ter titulo, descricao, acao destrutiva e cancelar.
- O estado destrutivo nao deve depender apenas de cor.

## Testes Esperados

- Use case limpa perfil e consentimentos.
- UI mostra microcopy e CTA de exclusao.
- Tocar no CTA abre confirmacao.
- Cancelar nao executa exclusao.
- Confirmar executa exclusao.

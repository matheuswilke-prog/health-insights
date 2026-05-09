# Settings Privacy Spec

Data: 2026-05-08

## Objetivo

Implementar a primeira tela minima de privacidade do MVP, acessivel a partir do Dashboard.

## Escopo

- Exibir resumo claro de privacidade on-device.
- Exibir consentimentos atuais para dados do MVP:
  - calorias;
  - peso;
  - treinos.
- Indicar se cada consentimento esta ativo ou desativado.
- Exibir versao da politica/consentimento quando houver registro local.
- Exibir resumo da politica de privacidade vigente.
- Permitir abrir o Health Connect para revisar permissoes Android.

## Fora de Escopo

- Exportar dados.
- Apagar dados locais.
- Revogar consentimentos dentro do app.
- Ler ou gravar Health Connect.
- Backend, analytics, telemetria ou envio externo.

## Regras

- UI nao acessa tipos do SDK Health Connect.
- Tela le apenas `ConsentRepository`.
- Atalho de permissoes abre app/area do Health Connect via intent do Android.
- Estados vazios de consentimento devem ser tratados como desativados.
- Nao mostrar dados sensiveis de perfil, peso, idade, altura ou calorias nesta tela.

## Estados

- Loading: enquanto consentimentos carregam.
- Content: lista de consentimentos e politica.

## Testes Esperados

- Mapper retorna sempre os tres tipos de dado do MVP.
- Consentimentos ausentes aparecem como desativados.
- UI mostra resumo de privacidade, consentimentos e politica.
- Botao de Health Connect dispara callback.
- Botao voltar dispara callback.

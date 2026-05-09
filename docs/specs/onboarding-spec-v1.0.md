# Onboarding Spec - Health Insights MVP

Fonte de produto: `docs/ROADMAP.md`. Este spec detalha apenas o fluxo de onboarding do MVP calorico.

## Fluxo

`Welcome -> Profile -> Goal -> Consent -> Dashboard`

Nao existe tela separada obrigatoria de First Insight no MVP atual. O Dashboard e a tela de payoff.

## Estado e Persistencia

Durante o fluxo:
- Valores de formulario podem viver em estado de UI/session.
- Nao persistir peso, altura, idade, sexo, objetivo ou meta em DataStore plain.
- Se o app morrer antes da conclusao, o usuario reinicia o onboarding.

Na conclusao:
- Salvar perfil, objetivo e meta diaria em Room + SQLCipher.
- Salvar consentimentos em Room + SQLCipher.
- Salvar `onboarding_complete = true` em DataStore plain.
- Navegar para Dashboard limpando back stack do onboarding.

## T1 - Welcome

Objetivo: explicar a promessa antes de pedir dados.

Conteudo:
- Nome/identidade do app.
- Headline sobre deficit, manutencao ou superavit calorico.
- Subheadline explicando Health Connect/Samsung Health de forma simples.
- Microcopy: dados ficam no aparelho.
- CTA: `Comecar`.

Aceite:
- Nenhuma leitura Health Connect.
- Nenhum dado persistido.
- CTA navega para Profile.
- Back do sistema fecha o app.
- Contraste e touch targets adequados.

## T2 - Profile

Objetivo: coletar dados para TMB e meta.

Campos obrigatorios:
- Peso atual em kg: 30 a 300.
- Altura em cm: 100 a 250.
- Idade: 13 a 100.
- Sexo biologico: masculino/feminino, usado somente para formula Mifflin-St Jeor.

Copy:
- Titulo: `Vamos calcular seu metabolismo basal.`
- Subtitulo: `Esses dados ficam so no seu aparelho.`
- Link: `Como tratamos esses dados` apontando para a politica de privacidade.

Aceite:
- CTA habilita apenas com campos validos.
- Teclado numerico nos campos numericos.
- Rotacao preserva valores digitados via estado de UI.
- Nenhuma leitura Health Connect.
- Nenhuma persistencia sensivel em DataStore plain.

## T3 - Goal

Objetivo: capturar objetivo calorico.

Opcoes:
- Emagrecer: deficit de 500 kcal/dia.
- Manter peso: meta alinhada ao gasto estimado.
- Ganhar massa: superavit de 300 kcal/dia.

Aceite:
- Apenas uma opcao selecionada.
- Estado selecionado nao depende apenas de cor.
- CTA habilita depois da selecao.
- Mostra disclaimer: acompanhamento calorico nao substitui nutricionista/medico.
- Nenhuma leitura Health Connect.

## T4 - Consent

Objetivo: obter consentimento explicito e granular.

Fonte de copy:
- `docs/legal/consent-copy-v1.1.md`.

Tipos de dado:
- Calorias: gasto total, gasto ativo e ingestao calorica quando disponivel.
- Peso.
- Treinos: tipo e duracao.

Regras:
- Toggles default OFF.
- Sem `Aceitar tudo` no MVP.
- Scroll pode ser usado para garantir leitura, mas consentimento exige acao afirmativa nos toggles e CTA.
- O app aceita 0, 1, 2 ou 3 categorias consentidas.
- Nao ha modo de entrada manual no MVP; permissao negada resulta em Dashboard com estados indisponiveis/vazios.

Aceite:
- Registrar todos os tipos de consentimento em Room + SQLCipher com `granted`, `grantedAt` e `policyVersion = consent-copy-v1.1`.
- Solicitar Health Connect apenas para categorias consentidas.
- Permissoes negadas pelo sistema atualizam o estado efetivo.
- Health Connect indisponivel leva ao Dashboard com erro/empty state, nao crash.
- Apos concluir, navegar direto para Dashboard.

## Dashboard Inicial Apos Onboarding

O Dashboard deve:
- Exibir meta diaria.
- Exibir balanco calorico quando dados existem.
- Mostrar estado `NoIntakeData` quando Health Connect nao fornece ingestao calorica.
- Mostrar peso mais recente quando disponivel.
- Explicar claramente permissoes ausentes ou Health Connect indisponivel.

Back stack:
- Back no Dashboard fecha o app.
- Usuario nao retorna ao onboarding concluido.

## Fora do Escopo do Onboarding MVP

- Tela separada de First Insight.
- Entrada manual de refeicao.
- Entrada manual de peso.
- Passos, sono e frequencia cardiaca.
- Tutorial guiado do Dashboard.
- Paywall.
- Login ou conta.
- Retomar exatamente a ultima etapa apos process death.

## Testes Esperados

Unitarios:
- Validacao de Profile.
- Selecao de Goal.
- Persistencia final do fluxo.
- Mapeamento de consentimentos para permissoes.

UI:
- Welcome renderiza e navega.
- Profile habilita CTA apenas com dados validos.
- Goal seleciona uma opcao.
- Consent inicia toggles OFF.
- Fluxo concluido abre Dashboard.

Estados:
- Health Connect indisponivel.
- Permissao parcial.
- Nenhuma permissao consentida.
- Sem ingestao calorica disponivel.

# Dashboard MVP Spec - Health Insights

Fonte de produto: `docs/product/prd/dashboard-mvp.md`.
Fonte de verdade geral: `docs/ROADMAP.md`.

Esta SPEC transforma o PRD do `dashboard-mvp` em comportamento implementavel. O Dashboard e a tela de payoff apos o onboarding e deve funcionar mesmo com Health Connect indisponivel, permissoes parciais ou dados incompletos.

## Decisoes da SPEC

1. Gasto canonico do Dashboard: `BMR + ActiveCaloriesBurned`.
2. `TotalCaloriesBurned` nao deve ser somado ao BMR no primeiro corte para evitar dupla contagem.
3. Peso no primeiro corte: apenas peso mais recente e data da medicao, quando disponivel.
4. Tendencia, grafico e diferenca vs medicao anterior ficam fora do primeiro corte.
5. Treinos podem continuar no escopo de consentimento/dados do MVP, mas nao aparecem no primeiro Dashboard.
6. O Dashboard deve recarregar ao entrar na tela e ao app retornar ao foreground, respeitando cache curto e trava contra leituras simultaneas.
7. O Dashboard nao oferece entrada manual de refeicao ou peso.
8. O peso vindo do Health Connect nao recalcula BMR/meta silenciosamente no primeiro corte.
9. Se `onboarding_complete = true`, mas dados locais essenciais estiverem ausentes, a recuperacao real e reiniciar o fluxo de onboarding.

## Fluxos

### Primeira entrada apos onboarding

`Consent concluido -> persiste perfil/meta/consentimentos -> onboarding_complete = true -> Dashboard`

Regras:
- Back no Dashboard fecha o app.
- Usuario nao retorna para telas concluidas do onboarding pelo back stack.
- Dashboard inicia carregamento de dados locais e Health Connect.

### Aberturas futuras

`App aberto -> verifica onboarding_complete -> Dashboard`

Regras:
- Se `onboarding_complete = true`, ir direto ao Dashboard.
- Se `onboarding_complete = false` ou ausente, ir ao onboarding.
- Dashboard recarrega dados ao abrir.

### Retorno ao foreground

`App em background -> usuario volta -> Dashboard visivel -> recarrega dados`

Regras:
- Recarregar sem polling continuo.
- Usar cache em memoria no ViewModel/repository com validade curta de 1 a 3 minutos.
- Usar comportamento single-flight: se uma leitura ja estiver em andamento, novas chamadas devem aguardar/reutilizar a mesma operacao em vez de iniciar leituras paralelas.
- Evitar flicker se dados anteriores ja estavam renderizados.
- Se a leitura nova falhar, preservar dados antigos somente se a UI indicar que houve erro de atualizacao. Caso contrario, exibir estado de erro/vazio apropriado.
- Ignorar retornos ao foreground muito proximos quando o cache ainda estiver fresco, exceto quando o retorno vier logo apos fluxo de permissao/configuracao Health Connect.
- Ignorar cache e recarregar quando a data local atual for diferente da data associada ao ultimo carregamento.

### Tentar novamente

`Erro inesperado -> CTA Tentar novamente -> nova leitura local + Health Connect`

Regras:
- CTA deve repetir a leitura.
- CTA e/ou bloco afetado deve entrar em loading transitorio imediatamente.
- Enquanto a tentativa estiver em andamento, evitar multiplos cliques acidentais.
- Erros nao devem gerar logs com dados de saude.

### Resolver permissao

`Permissao ausente/negada -> CTA -> fluxo/configuracao Health Connect quando disponivel -> volta ao app -> recarrega`

Regras:
- Quando o sistema permitir solicitar permissao novamente, usar o fluxo de permissao Health Connect.
- Quando o sistema exigir configuracao manual, abrir configuracoes do Health Connect quando tecnicamente possivel.
- Se nao houver destino confiavel, exibir instrucao curta e nao bloquear o app.

## Dados Lidos

### Dados locais criptografados

Origem esperada: Room + SQLCipher.

- Perfil usado para calculo de BMR.
- Objetivo calorico.
- Meta calorica diaria.
- Consentimentos.

Regras:
- Dashboard nao recalcula meta livremente na UI.
- Se a meta ja estiver persistida, a UI consome o valor persistido ou um use case de dominio.
- Se dados locais essenciais estiverem ausentes apesar de `onboarding_complete = true`, invalidar `onboarding_complete` e redirecionar para onboarding/configuracao inicial com copy curta de recuperacao.
- Peso mais recente do Health Connect pode ser exibido no Dashboard, mas nao altera BMR/meta do onboarding no primeiro corte.
- Recalculo de BMR/meta a partir de novo peso do Health Connect fica fora do primeiro corte e deve ser tratado em uma feature futura de revisao de perfil/meta.

### DataStore plain

- `onboarding_complete`.

Regras:
- Apenas flag operacional.
- Nao armazenar peso, altura, idade, sexo, objetivo, meta ou health data em DataStore plain.

### Health Connect

Dados para primeiro corte:
- `ActiveCaloriesBurnedRecord` do dia atual.
- `NutritionRecord` do dia atual para energia ingerida, quando disponivel.
- `WeightRecord` mais recente, quando disponivel.

Dados nao usados diretamente no Dashboard primeiro corte:
- `TotalCaloriesBurnedRecord`.
- `ExerciseSessionRecord`.
- Passos.
- Sono.
- Frequencia cardiaca.

Janela temporal:
- Calorias ativas: inicio do dia local ate o momento atual.
- Ingestao calorica: inicio do dia local ate o momento atual.
- Peso: registro mais recente disponivel dentro da retencao permitida.

Tempo e fuso:
- Usar `ZoneId.systemDefault()` no momento da leitura para calcular o inicio/fim do dia local.
- Usar `TimeRangeFilter.between(startOfDayLocal, nowLocal)` para agregacoes do dia.
- Em viagens ou mudancas de fuso, aceitar possiveis distorcoes do dia local no MVP em vez de tentar reconstruir o fuso original de cada registro.
- Nao duplicar registros manualmente para compensar fuso; a agregacao deve depender da janela temporal enviada ao Health Connect.

## Dados Gravados

O Dashboard nao deve gravar novos dados de saude no primeiro corte.

Permitido:
- Atualizar estados efemeros de UI.
- Registrar internamente status efetivo de permissao se ja existir padrao no projeto.

Nao permitido:
- Persistir leituras brutas do Health Connect sem decisao explicita posterior.
- Persistir dados sensiveis em DataStore plain.
- Enviar dados para backend, analytics ou telemetria.

## Permissoes e Consentimentos

O Dashboard deve respeitar consentimento local e permissao Health Connect efetiva.

Categorias:
- Calorias: gasto ativo e ingestao calorica.
- Peso.
- Treinos: consentimento pode existir, mas nao afeta blocos do primeiro Dashboard.

Regras:
- Consentimento local concedido sem permissao Health Connect efetiva nao autoriza leitura.
- Permissao Health Connect concedida sem consentimento local nao deve ser usada.
- Cada bloco deve se comportar de forma independente.
- Negar uma permissao nao deve bloquear o Dashboard inteiro quando outros dados puderem ser exibidos.

## Regra de Balanco Calorico

### Formula canonica

`gasto_estimado_do_dia = BMR_do_usuario + calorias_ativas_do_dia`

`saldo_calorico = ingestao_calorica_do_dia - gasto_estimado_do_dia`

Interpretacao:
- Saldo negativo: deficit.
- Saldo positivo: superavit.
- Saldo proximo de zero: manutencao.

Limiar inicial:
- Manutencao: saldo entre `-250 kcal` e `+250 kcal`, inclusive.
- Deficit: saldo menor que `-250 kcal`.
- Superavit: saldo maior que `+250 kcal`.

Regras:
- A UI nao deve implementar a formula diretamente se houver use case/modelo de dominio.
- Se ingestao estiver ausente, nao calcular saldo como se fosse zero.
- Se calorias ativas estiverem ausentes, nao calcular saldo.
- Se o design mostrar BMR nesse caso, rotular como `metabolismo basal estimado`, nao como gasto total/estimado do dia.
- `TotalCaloriesBurned` nao entra na formula do primeiro corte.
- O BMR usado no primeiro corte vem do perfil/meta persistidos no onboarding, nao do peso mais recente lido no Health Connect.

## Blocos de UI

A SPEC define comportamento, nao layout visual final. O layout sera feito em ferramenta externa de design.

### Meta Diaria

Mostra:
- Meta calorica diaria em kcal.
- Objetivo associado quando disponivel: emagrecer, manter ou ganhar massa.

Estados:
- Content: meta disponivel.
- LocalStateInvalid: onboarding marcado como completo, mas meta/perfil/objetivo/consentimentos essenciais ausentes ou corrompidos.

Aceite:
- Meta aparece mesmo quando Health Connect estiver indisponivel.
- Meta nao depende de permissao Health Connect.

### Balanco do Dia

Mostra quando completo:
- Ingestao do dia.
- Gasto estimado do dia.
- Saldo calorico.
- Status: deficit, manutencao ou superavit.

Estados:
- Content completo: ingestao e gasto estimado disponiveis.
- Sem ingestao: nao mostra saldo calculado.
- Dados parciais: mostra o que e confiavel e explica o que falta.
- Error: falha inesperada na leitura/processamento.

Aceite:
- Ingestao ausente nao aparece como `0 kcal consumidas`.
- Saldo so aparece quando a interpretacao for confiavel.
- Status visual nao depende apenas de cor.

### Ingestao Calorica

Mostra:
- Total ingerido no dia quando Health Connect trouxer energia ingerida.

Estados:
- Content: ingestao disponivel.
- Empty: Health Connect disponivel, consentimento/permissao ok, mas sem registros.
- PermissionMissing: consentimento/permissao ausente.
- Unavailable: Health Connect indisponivel.

Copy base para empty:

```text
Ainda nao ha ingestao calorica registrada hoje no Health Connect.
Quando esse dado aparecer, calculamos o balanco completo do dia.
```

Aceite:
- Nao sugerir entrada manual no MVP.
- Nao tratar ausencia como bug.
- A copy deve funcionar tanto de manha cedo, antes do usuario comer, quanto em casos de app de nutricao nao sincronizado.
- Somar apenas energia positiva como ingestao calorica confiavel.
- Se `NutritionRecord` retornar energia ausente, zero ou negativa, tratar como `Sem ingestao` no primeiro corte.

### Gasto Calorico

Mostra:
- BMR usado no calculo, se o design pedir detalhamento.
- Calorias ativas do dia, quando disponiveis.
- Gasto estimado do dia: `BMR + calorias ativas`.

Estados:
- Content: BMR e calorias ativas disponiveis.
- Partial: BMR disponivel, mas calorias ativas ausentes; nao calcular saldo.
- PermissionMissing: permissao/consentimento de calorias ausente.
- Unavailable: Health Connect indisponivel para calorias ativas.

Aceite:
- Nao somar `TotalCaloriesBurned` com BMR.
- Nao apresentar gasto estimado como medicao exata.
- Sem calorias ativas, BMR deve ser apresentado apenas como metabolismo basal estimado, se for apresentado.

### Peso

Mostra:
- Peso mais recente.
- Data da medicao se disponivel.

Estados:
- Content: peso mais recente disponivel.
- Empty: permissao ok, mas nenhum peso encontrado.
- PermissionMissing: consentimento/permissao de peso ausente.
- Unavailable: Health Connect indisponivel.

Aceite:
- Nao mostrar tendencia no primeiro corte.
- Nao inferir perda/ganho com uma unica medicao.

### Estado de Dados

Mostra mensagens de contexto quando houver:
- Health Connect indisponivel.
- Permissoes ausentes.
- Dados parciais.
- Sem dados suficientes.
- Erro inesperado.

Aceite:
- Mensagens devem ser curtas e acionaveis.
- Estados parciais nao devem esconder blocos que funcionam.

## Estados Globais

### Loading

Condicao:
- Dashboard esta lendo dados locais e Health Connect.

Regras:
- Loading inicial deve ser simples.
- Se passar do tempo definido pelo time sem retorno, exibir estado de erro ou vazio recuperavel.
- Tempo alvo inicial: conteudo principal em ate 2 segundos em aparelho comum; se exceder 5 segundos, mostrar feedback de demora ou opcao de tentar novamente.
- Releituras disparadas por CTA devem mostrar loading local no bloco/botao afetado.
- Durante loading de retry, manter CTA desabilitado ou em estado ocupado ate a operacao terminar.

### Content Completo

Condicao:
- Meta existe.
- Consentimento e permissao de calorias existem.
- Ingestao do dia disponivel.
- Calorias ativas disponiveis.

Resultado:
- Exibir meta, ingestao, gasto estimado, saldo e status.
- Exibir peso se disponivel.

### Content Parcial

Condicao:
- Pelo menos um bloco tem dado valido, mas outro bloco esta ausente/indisponivel.

Resultado:
- Renderizar dados validos.
- Explicar lacunas.
- Nao produzir conclusao geral enganosa.

### Empty

Condicao:
- Permissoes/consentimentos existem, Health Connect esta disponivel, mas nao ha registros uteis.

Resultado:
- Exibir estado vazio normal.
- Nao sugerir que houve falha tecnica.

### PermissionMissing

Condicao:
- Consentimento local ausente ou permissao Health Connect ausente/negada para uma categoria.

Resultado:
- Exibir qual categoria esta indisponivel.
- Oferecer CTA quando houver acao possivel.
- Nao bloquear blocos de outras categorias.

### HealthConnectUnavailable

Condicao:
- Health Connect nao instalado, desatualizado, desativado ou API indisponivel.

Resultado:
- Meta local ainda pode aparecer.
- Blocos dependentes de Health Connect mostram indisponibilidade.
- CTA para instalar/atualizar/abrir configuracao quando possivel.
- App nao deve crashar.

### Error

Condicao:
- Falha inesperada de leitura, mapeamento ou processamento.

Resultado:
- Mensagem amigavel.
- CTA de tentar novamente.
- Sem stack trace.
- Sem health data em log.

### LocalStateInvalid

Condicao:
- `onboarding_complete = true`, mas perfil, objetivo, meta ou consentimentos essenciais estao ausentes/corrompidos.

Resultado:
- Nao manter o usuario preso em erro irrecuperavel.
- Invalidar `onboarding_complete`.
- Redirecionar para onboarding/configuracao inicial.
- Exibir copy curta explicando que foi necessario refazer a configuracao local.

## Edge Cases

- `onboarding_complete = true`, mas perfil/meta nao existem: invalidar flag e redirecionar para onboarding.
- Consentimento local existe, mas permissao Health Connect foi revogada fora do app.
- Health Connect instalado, mas sem provider/dados sincronizados.
- Nutrition existe, mas sem campo energetico util.
- Nutrition retorna energia ausente, zero ou negativa: tratar como sem ingestao no primeiro corte.
- Calorias ativas ausentes, mas BMR local disponivel.
- Peso existe sem timestamp exibivel.
- Multiplas medicoes de peso no mesmo dia: usar a mais recente.
- Mudanca de dia enquanto app esta aberto: recarregar no retorno ao foreground; atualizacao em tempo real fica fora do MVP.
- Fuso horario local muda: usar `ZoneId.systemDefault()` no momento da leitura e aceitar distorcoes de viagem no MVP.
- Usuario alterna rapidamente entre app e configuracoes: cache curto e single-flight evitam leituras paralelas.
- App volta ao foreground depois de virar o dia local: ignorar cache e recarregar dados do novo dia.
- Permissao parcial: peso permitido, calorias negadas.
- Health Connect indisponivel durante carregamento.
- Erro de banco local ao ler meta/consentimentos.
- Valores negativos ou absurdos vindos de fonte externa: nao renderizar como numero normal; exibir erro/ausencia para o bloco.

## Privacidade e LGPD

- Nenhum dado sai do aparelho.
- Nenhum analytics ou telemetria.
- Nenhum health data em logs.
- Dados sensiveis e derivados permanecem em Room + SQLCipher.
- DataStore plain deve conter apenas flags operacionais nao sensiveis.
- Dashboard deve respeitar consentimentos granulares.
- Dados ausentes por permissao negada devem ser comunicados sem julgamento.
- Retencao padrao do MVP: 12 meses.

## Acessibilidade

- Estados de deficit/manutencao/superavit nao podem depender apenas de cor.
- Textos devem ter contraste adequado.
- Cards/blocos acionaveis devem ter touch target minimo adequado.
- Loading e erro devem ser legiveis por leitor de tela.
- Numeros devem ter labels compreensiveis, por exemplo `Meta diaria: 2200 kcal`.
- CTAs devem indicar acao real: tentar novamente, abrir permissoes ou abrir configuracoes.

## Requisitos de Design Externo

O prompt de design deve pedir telas/estados para:
- Dashboard completo.
- Sem ingestao calorica.
- Permissao parcial.
- Health Connect indisponivel.
- Sem peso registrado.
- Erro inesperado.
- Loading.

O design nao deve:
- Criar entrada manual de refeicao ou peso.
- Criar tela separada de First Insight.
- Incluir passos, sono, frequencia cardiaca, graficos avancados ou coach.
- Prometer recomendacao medica/nutricional.

## Reviews Obrigatorias

Produto/UX:
- Obrigatoria por tocar Dashboard, estados vazios e copy.

Seguranca/privacidade:
- Obrigatoria por tocar Health Connect, permissoes, consentimento e dados derivados de saude.

Design:
- Obrigatoria antes da implementacao visual final.

Infra/build:
- Somente se a implementacao alterar Gradle, modulos, dependencias, R8/ProGuard ou CI.

## Testes Esperados

Unitarios:
- Formula de saldo usando `BMR + ActiveCaloriesBurned`.
- `TotalCaloriesBurned` nao usado na formula do primeiro corte.
- Ingestao ausente nao vira zero.
- Classificacao de deficit/manutencao/superavit com manutencao inclusiva entre `-250 kcal` e `+250 kcal`.
- Mapeamento de permissoes/consentimentos para estados de UI.
- Peso mais recente escolhido corretamente.
- Peso mais recente do Health Connect nao altera BMR/meta no primeiro corte.
- Health Connect indisponivel mapeado para estado apropriado.
- Dados locais ausentes com onboarding completo invalidam flag e redirecionam para onboarding.
- Releituras em foreground respeitam cache curto e single-flight.
- Releitura ignora cache quando a data local muda.
- Nutrition com energia ausente, zero ou negativa vira estado sem ingestao.

UI/Compose:
- Loading renderiza.
- Retry mostra loading local e evita multiplos cliques.
- Content completo renderiza meta, ingestao, gasto, saldo e status.
- Sem ingestao mostra copy correta e nao mostra saldo.
- Permissao parcial mostra blocos disponiveis e lacunas.
- Health Connect indisponivel nao crasha e mantem meta local.
- Peso ausente mostra estado vazio do bloco.
- CTA de tentar novamente dispara nova leitura.

Instrumentados, quando aplicavel:
- Fluxo de permissao Health Connect.
- Retorno de configuracoes/permissoes para Dashboard.
- Leitura com Health Connect indisponivel/desatualizado, se simulavel.

Validacao manual:
- Onboarding concluido abre Dashboard.
- Reabrir app vai direto ao Dashboard.
- Revogar permissao fora do app e voltar ao Dashboard.
- Dispositivo/emulador sem Health Connect.
- Cenarios com apenas peso, apenas calorias, e nenhum dado.

## Criterios de Aceite da SPEC

- Dashboard funciona sem backend.
- Meta aparece sem depender de Health Connect.
- Balanco so aparece com ingestao e gasto confiaveis.
- Ingestao ausente nao e tratada como zero.
- Peso mostra apenas valor mais recente no primeiro corte.
- Peso do Health Connect nao recalcula BMR/meta silenciosamente.
- Permissoes parciais nao bloqueiam toda a tela.
- Health Connect indisponivel nao causa crash.
- Retorno ao foreground nao dispara leituras paralelas.
- Estado local invalido tem recuperacao real via retorno ao onboarding.
- Estados de erro/vazio sao claros e recuperaveis.
- Implementacao futura tem testes para formula, estados e permissoes.

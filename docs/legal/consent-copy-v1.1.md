# Texto de Consentimento — Health Insights v1.1
<!-- Documento: consent-copy-v1.1.md -->
<!-- Versão: 1.1 | Data de vigência: 2026-05-05 -->
<!-- Base legal: LGPD Art. 11, II, a — consentimento explícito para dados de saúde -->
<!-- Mudanças em relação à v1.0: pivot de produto. Removidos Passos, Sono, FC. Adicionados Calorias (gasto + ingestão), Peso, Treinos (mantido). -->
<!-- Status: pronto para uso como copy final na Tela 4 SEM revisão jurídica externa adicional, dentro do escopo de produto definido (app on-device, sem transmissão, sem terceiros). Caso o produto passe a transmitir dados ou integrar SDKs externos antes do lançamento, este copy DEVE passar por revisão jurídica antes do uso. -->

---

## Título da tela

```
Antes de começar — seus dados de saúde
```

<!-- LGPD Art. 9 — o controlador deve informar o titular antes do início do tratamento -->

---

## Parágrafo de introdução

```
O Health Insights é operado por Matheus Wilke (matheus.wilke@gmail.com),
responsável pelo tratamento dos seus dados de saúde neste aplicativo.

Para mostrar seu balanço calórico diário e a evolução do seu peso,
o app precisa ler dados do Android Health Connect — a plataforma do
Android que reúne informações do seu relógio e do seu celular. Nesta
tela explicamos exatamente o que será lido, para que serve cada
informação e quais são os seus direitos.

Você decide cada permissão separadamente. O app funciona mesmo se
você recusar — algumas funções ficam manuais.
```

<!-- LGPD Art. 9, I — identificação do controlador e finalidade do tratamento -->
<!-- LGPD Art. 8, § 6 — linguagem clara e acessível é obrigatória -->
<!-- LGPD Art. 8, § 5 — recusa não pode causar prejuízo, app continua funcionando -->

---

## Seção de tipos de dados

### Calorias gastas

```
O que o app lê: seu gasto calórico diário registrado pelo Android
Health Connect — gasto total (TotalCaloriesBurned) e gasto ativo
em atividades (ActiveCaloriesBurned).

Para que serve: calcular seu balanço calórico diário (gasto menos
ingestão) e mostrar se você está em déficit, manutenção ou superávit
em relação ao seu objetivo.

O que o app NÃO lê: frequência cardíaca, passos, sono, GPS,
intensidade de batimento durante a atividade, ou qualquer dado
biométrico além do número de calorias.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 11 — dado de saúde de categoria especial, exige consentimento explícito -->
<!-- LGPD Art. 6, III — princípio da necessidade: somente o mínimo necessário -->

### Ingestão calórica

```
O que o app lê: as calorias dos alimentos que você (ou outro app)
registrou no Health Connect (NutritionRecord) — apenas o total
de calorias por refeição ou registro.

Para que serve: somar sua ingestão diária e completar o cálculo do
seu balanço calórico (gasto menos ingestão).

Quando esse dado não está disponível: nem todo aparelho ou app
de saúde grava nutrição no Health Connect. Se o app não encontrar
esse registro, você poderá lançar suas refeições manualmente
dentro do Health Insights — esse lançamento manual também fica
só no seu aparelho.

O que o app NÃO lê: nomes de alimentos específicos, fotos de
refeições, marcas de produto, restrições alimentares, macros
detalhados (proteína, carboidrato, gordura) — somente o total
de calorias.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 11 — dado de saúde de categoria especial -->
<!-- LGPD Art. 6, III — minimização: apenas calorias totais, não macros nem itens identificáveis -->

### Peso

```
O que o app lê: seus registros de peso corporal armazenados no
Android Health Connect (WeightRecord) — o valor em quilogramas
e a data de cada medição.

Para que serve: mostrar a evolução do seu peso ao longo do tempo
e relacionar essa evolução com o balanço calórico que o app calcula.

O que o app NÃO lê: composição corporal (percentual de gordura,
massa magra, água), índice de massa corporal calculado por outros
apps, medições de circunferência ou qualquer outro dado antropométrico
além do peso em si.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 11 — dado de saúde de categoria especial -->
<!-- LGPD Art. 6, III — minimização: somente peso, não composição corporal -->

### Treinos

```
O que o app lê: o tipo de atividade física (caminhada, corrida,
musculação, pedalada etc.) e a duração de cada sessão registrada
no Health Connect (ExerciseSessionRecord).

Para que serve: validar e contextualizar seu gasto calórico ativo
do dia — saber, por exemplo, se as calorias gastas vieram de uma
corrida ou de uma caminhada — e exibir sua frequência de treinos
ao longo do tempo.

O que o app NÃO lê: GPS, rota percorrida, localização do treino,
velocidade, ritmo, frequência cardíaca durante a atividade,
elevação, ou qualquer outro dado além de tipo e duração.
```

<!-- LGPD Art. 9, I — finalidade específica por tipo de dado -->
<!-- LGPD Art. 11 — dado de saúde de categoria especial -->
<!-- LGPD Art. 6, III — dado mínimo necessário: sem GPS, sem rota, sem FC -->

---

## Garantia on-device

```
Seus dados nunca saem do seu aparelho.

O Health Insights não tem servidores. Não faz upload de nenhum dado.
Não compartilha informações com terceiros. Não usa seus dados para
publicidade. Tudo é processado e guardado só aqui no seu celular,
com criptografia.
```

<!-- LGPD Art. 9, V — informação sobre compartilhamento com terceiros (resposta: nenhum) -->
<!-- LGPD Art. 6, VII — princípio da segurança -->

---

## Período de retenção

```
Seus dados de saúde ficam guardados no seu aparelho por até 12 meses.
Você pode mudar esse prazo a qualquer momento em
Configurações → Privacidade → Meus Dados.

Quando você desinstalar o app, todos os dados são apagados
automaticamente.
```

<!-- LGPD Art. 9, III — forma e duração do tratamento devem ser informadas ao titular -->
<!-- LGPD Art. 15 — encerramento do tratamento quando o prazo determinado se encerra -->

---

## Resumo dos direitos do titular

```
Você tem os seguintes direitos sobre seus dados, exercíveis a qualquer
momento em Configurações → Privacidade → Meus Dados:

• Ver todos os dados que o app armazena sobre você
• Exportar seus dados em formato aberto
• Apagar todos os seus dados permanentemente

Esses direitos estão garantidos pela Lei Geral de Proteção de Dados
(LGPD), Art. 18.
```

<!-- LGPD Art. 9, VI — informação sobre o direito de não fornecer consentimento e as consequências -->
<!-- LGPD Art. 18 — direitos do titular: confirmação, acesso, eliminação, portabilidade -->

---

## Aviso de revogação

```
Você pode cancelar esta autorização a qualquer momento em
Configurações → Privacidade → Revogar Acesso à Saúde.

Se você revogar o acesso, o app para de ler novos dados de saúde
e apaga os dados que já estavam guardados. Você não perde nenhuma
função do app — apenas as telas que dependem dos dados revogados
ficarão indisponíveis ou passarão a depender de lançamento manual.
```

<!-- LGPD Art. 8, § 5 — o consentimento pode ser revogado a qualquer momento -->
<!-- LGPD Art. 18, IX — direito de revogação do consentimento -->

---

## Link para a política de privacidade

```
Leia nossa Política de Privacidade completa para todos os detalhes
sobre como tratamos seus dados.
```

[LINK_POLITICA_PRIVACIDADE]

<!-- LGPD Art. 9 — o controlador deve informar o titular sobre todos os aspectos do tratamento -->

---

## Label do botão de consentimento afirmativo

```
Concordo — permitir acesso aos dados de saúde
```

<!-- LGPD Art. 8 — consentimento deve ser manifestado por ação afirmativa e inequívoca -->
<!-- LGPD Art. 11, II, a — consentimento explícito para dados de saúde -->

---

## Label da opção de recusa

```
Agora não — usar com funções limitadas
```

<!-- LGPD Art. 8, § 5 — a recusa não pode causar prejuízo ao titular -->
<!-- LGPD Art. 9, VI — o titular deve ser informado das consequências de não consentir -->

---

## Notas de implementação (não exibidas ao usuário) — atualizadas para v1.1

As seguintes diretrizes são obrigatórias para que este copy constitua
consentimento válido nos termos da LGPD:

1. **Toggles default OFF.** Cada tipo de dado é um toggle independente
   com estado padrão "desligado". Consentimento por omissão ou
   pré-marcação é nulo (Art. 8, § 3).

2. **Modelo aprovado de UI: 1 tela com toggles granulares.** A v1.1 do
   produto (calorias / peso / treinos — 3 toggles) pode ser apresentada
   em uma única tela com scroll, desde que cada toggle tenha sua
   descrição completa de finalidade visível inline (não em link, não em
   modal, não em "saiba mais"). Esta autorização substitui a regra "uma
   tela por tipo de dado" da v1.0, que foi escrita quando o escopo
   incluía 5 tipos de dado e o agrupamento poderia ofuscar a
   granularidade.

3. **Consentimento exige ação afirmativa, não scroll.** Scroll-to-bottom
   é prova de oportunidade de leitura, não é consentimento em si. O
   consentimento se dá pelo toque no toggle de cada tipo de dado e pelo
   toque no CTA "Concordo" (Art. 8, § 3). Liberar o CTA "Concordo"
   somente após o usuário rolar a tela inteira é um reforço aceitável
   da informação prévia exigida pelo Art. 9 — não substitui o ato de
   consentimento.

4. **Recusa sem punição.** Tocar "Agora não" leva ao app em modo manual.
   Tocar "Concordo" com 0 toggles ON é tratado como modo manual após
   dialog de confirmação. O app não pode bloquear o acesso por negativa
   de consentimento (Art. 8, § 5).

5. **Granularidade real.** Cada toggle dispara o request da permissão
   correspondente do Health Connect de forma independente. Não há
   "Aceitar tudo" mascarado: o usuário pode aceitar 1, 2 ou os 3 toggles
   em qualquer combinação, e o app deve operar com a combinação escolhida.

6. **Registro do consentimento.** Para cada toggle, ao tocar "Concordo",
   o app deve registrar (criptografado em Room): `data_type`,
   `granted_at` (ISO-8601), `policy_version` ("consent-copy-v1.1"),
   `granted` (boolean). Esse registro é o comprovante da base legal e
   só pode ser apagado mediante solicitação de exclusão pelo titular.

7. **Versão deste documento:** `consent-copy-v1.1` — qualquer alteração
   material (novo tipo de dado, nova finalidade, mudança em
   "o que NÃO lê") exige nova versão e novo consentimento explícito do
   usuário antes do início do tratamento alterado.

8. **Privacy Policy desatualizada.** A `privacy-policy-v1.md` (vigente
   2026-05-04) ainda lista os tipos de dado da v1.0 (passos, sono, FC).
   Não é compliant publicar v1.1 do consent-copy sem antes atualizar a
   policy para refletir o pivot. Bloqueador adicional fora do escopo
   deste documento — encaminhar para revisão pelo controlador.

---

*Health Insights — Texto de Consentimento v1.1 | Vigente a partir de 2026-05-05*
*Controlador: Matheus Wilke — matheus.wilke@gmail.com*
*Próxima revisão obrigatória: antes de qualquer mudança no escopo de dados ou finalidades*

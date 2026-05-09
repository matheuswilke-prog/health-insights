# Settings Export/Delete Security Review

Data: 2026-05-09

Escopo revisado:

- EP-04-02 - Exportar Dados.
- EP-04-03 - Apagar Dados Locais.

## Decisao

Aprovado para implementacao com as restricoes abaixo.

## Riscos e Controles

### Exportacao de dados sensiveis

Risco: o JSON contem perfil corporal, objetivo, meta calorica e consentimentos. Ao sair do banco SQLCipher, o arquivo passa a depender do destino escolhido pelo usuario.

Controles:

- Microcopy deve aparecer antes do CTA avisando que o arquivo contem dados sensiveis.
- App deve abrir `CreateDocument` para o usuario escolher um arquivo local.
- App nao deve usar share sheet, email, rede, backend ou analytics.
- App nao deve gravar conteudo exportado em logs.

### Escopo excessivo de dados

Risco: exportar dados que o app nao coleta ou dados originais do Health Connect.

Controles:

- JSON deve ser gerado apenas a partir de `UserProfileRepository` e `ConsentRepository`.
- Exportacao nao deve chamar `HealthConnectRepository`.
- Teste deve garantir ausencia de campos fora de escopo, como passos, sono, frequencia cardiaca, GPS/localizacao, email, deviceId, foodName e macros.

### Exclusao incompleta

Risco: usuario espera apagar dados locais, mas a flag de onboarding ou registros sensiveis permanecem.

Controles:

- Use case deve limpar perfil e consentimentos.
- App deve resetar `onboarding_complete`.
- Apos sucesso, app deve navegar para Welcome.

### Exclusao alem do escopo

Risco: app tenta apagar dados originais do Health Connect.

Controles:

- Delete local nao deve chamar Health Connect.
- UI deve explicar que dados originais do Health Connect permanecem no Android/Health Connect.

## Itens Nao Exigidos

- Revisao de negocios: dispensada pelo founder para estas duas funcoes LGPD.
- Design externo: dispensado por ser extensao pequena da tela Settings existente, sem tela nova.
- Nova dependencia: nao necessaria.

## Checklist de Implementacao

- Sem novas dependencias.
- Sem rede.
- Sem logs de dados de saude.
- JSON deterministico o suficiente para teste de formato.
- Confirmacao obrigatoria antes de apagar dados.

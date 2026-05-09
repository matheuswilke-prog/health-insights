# Settings Data Export Spec

Data: 2026-05-09

## Objetivo

Permitir que o usuario exporte uma copia local, em JSON, dos dados que o Health Insights armazena no aparelho.

## Escopo

- Gerar JSON com os dados locais persistidos pelo app:
  - perfil corporal usado para calculo de TMB;
  - objetivo;
  - meta calorica diaria;
  - registros de consentimento;
  - metadados minimos da exportacao.
- Usar o seletor nativo do Android para o usuario salvar o arquivo localmente.
- Nao transmitir dados para backend, nuvem, analytics, email, share sheet ou terceiros.
- Nao ler dados novos do Health Connect durante a exportacao.

## Fora de Escopo

- Exportar dados brutos originais do Health Connect.
- Exportar passos, sono, frequencia cardiaca, GPS, rota, macros detalhados, nomes de alimentos, fotos, conta, email ou identificadores de dispositivo.
- Compartilhamento automatico do arquivo.
- Criptografar o arquivo exportado depois que o usuario escolhe salvar fora do banco local.

## Fluxo

1. Usuario abre Settings > Privacidade.
2. Tela mostra microcopy explicando que o arquivo contem dados locais sensiveis e fica sob controle do usuario.
3. Usuario toca em "Exportar dados".
4. App gera o JSON em memoria a partir dos repositorios locais.
5. App abre o seletor nativo `CreateDocument` com MIME type `application/json`.
6. Usuario escolhe o destino e nome do arquivo.
7. App grava o conteudo JSON no `Uri` retornado pelo Android.

## Estados

- Loading: enquanto consentimentos carregam.
- Content: tela normal com a acao de exportar.
- Export in progress: botao pode ficar indisponivel enquanto o JSON e gerado.
- Export ready: app entrega o JSON ao fluxo nativo de criacao de arquivo.
- Export error: erro generico sem logar dados sensiveis.

## Dados Lidos

- `UserProfileRepository.get()`.
- `ConsentRepository.getAll()`.

## Formato JSON

Campos raiz:

- `schemaVersion`: inteiro.
- `exportedAt`: timestamp ISO-8601 em UTC.
- `source`: `Health Insights local export`.
- `userProfile`: objeto ou `null`.
- `consents`: lista de registros.

`userProfile` deve conter apenas:

- `weightKg`;
- `heightCm`;
- `ageYears`;
- `sex`;
- `goal`;
- `dailyCalorieTarget`.

Cada consentimento deve conter:

- `dataType`;
- `granted`;
- `grantedAt`;
- `policyVersion`.

## Regras de Privacidade

- Exportacao nao deve iniciar leitura do Health Connect.
- Exportacao nao deve incluir dados fora do escopo MVP.
- Exportacao nao deve usar rede.
- Nenhum dado exportado deve ser escrito em logs.
- O arquivo salvo pelo usuario deixa de estar protegido pelo SQLCipher; a UI deve avisar isso antes do CTA.

## Acessibilidade

- O CTA deve ter texto claro.
- A microcopy de risco deve aparecer antes do CTA.
- Estados de erro devem ser legiveis por texto, sem depender apenas de cor.

## Testes Esperados

- Use case gera JSON com perfil e consentimentos.
- Use case gera `userProfile: null` quando nao ha perfil local.
- JSON nao contem campos nao coletados pelo app, como `steps`, `sleep`, `heartRate`, `email`, `deviceId`, `location`, `foodName` ou `macros`.
- UI mostra microcopy e CTA de exportacao.
- CTA dispara callback/acao de exportacao.

# Tabela de Registro de Tratamento de Dados — Health Insights

**Versão:** v1.0
**Data:** [DATA DE VIGÊNCIA]
**Finalidade deste documento:** Registro de Atividades de Tratamento (base para RIPD e Data Safety Form do Google Play)
**Responsável pelo documento:** [NOME DO DESENVOLVEDOR] — [EMAIL DE CONTATO]

> **Nota sobre o RIPD:** Este documento constitui a base para o Relatório de Impacto à Proteção de Dados Pessoais (RIPD) exigido pelo **Art. 38 da LGPD** quando o tratamento envolver dados pessoais sensíveis. Dado que o Health Insights trata exclusivamente dados de saúde (categoria especial, Art. 11), a elaboração do RIPD completo é recomendada antes do lançamento público, especialmente se o número de titulares vier a superar escala significativa ou se houver expansão de funcionalidades.

---

## Tabela de Tratamento de Dados

| Tipo de dado | Classificação LGPD | Fonte | Finalidade | Base legal | Retenção | Compartilhamento | Criptografia | Direito de exclusão |
|---|---|---|---|---|---|---|---|---|
| **Passos** (`StepsRecord`) — contagem de passos por período de tempo | Dado pessoal de categoria especial — dado de saúde (Art. 5º, II; Art. 11) | Android Health Connect (leitura somente; o app não grava no Health Connect) | Exibição de contagem diária/semanal/histórica; cálculo de médias e tendências de atividade física; visualização de metas de movimento | Consentimento explícito do titular — Art. 11, II, a da LGPD | 90 dias (padrão); configurável pelo usuário para 30, 180 ou 365 dias; exclusão automática de registros mais antigos que o período configurado | Nenhum — dados não saem do dispositivo | Banco de dados local criptografado (Room + SQLCipher); chave gerenciada pelo Android Keystore vinculada ao hardware do dispositivo | Exclusão imediata por: (1) solicitação do titular via app (Configurações > Privacidade > Excluir Dados); (2) revogação de consentimento; (3) desinstalação do app |
| **Sono** (`SleepSessionRecord`) — duração total e estágios do sono por sessão noturna | Dado pessoal de categoria especial — dado de saúde (Art. 5º, II; Art. 11) | Android Health Connect (leitura somente) | Exibição de duração e estágios do sono por noite; cálculo de médias e variações de padrões de sono; identificação de tendências de regularidade de horários | Consentimento explícito do titular — Art. 11, II, a da LGPD | 90 dias (padrão); configurável para 30, 180 ou 365 dias | Nenhum — dados não saem do dispositivo | Banco de dados local criptografado (Room + SQLCipher); chave gerenciada pelo Android Keystore | Exclusão imediata por solicitação, revogação de consentimento ou desinstalação |
| **Frequência cardíaca** (`HeartRateRecord`) — batimentos por minuto por sessão/período | Dado pessoal de categoria especial — dado de saúde (Art. 5º, II; Art. 11) — **nível de sensibilidade elevado** | Android Health Connect (leitura somente) | Exibição de leituras históricas de frequência cardíaca; visualização de variações durante repouso e atividade; acompanhamento pessoal pelo titular | Consentimento explícito do titular — Art. 11, II, a da LGPD | 90 dias (padrão); configurável para 30, 180 ou 365 dias | Nenhum — dados não saem do dispositivo | Banco de dados local criptografado (Room + SQLCipher); chave gerenciada pelo Android Keystore | Exclusão imediata por solicitação, revogação de consentimento ou desinstalação |
| **Exercício** (`ExerciseSessionRecord`) — tipo de atividade, duração e métricas de sessões físicas | Dado pessoal de categoria especial — dado de saúde (Art. 5º, II; Art. 11) | Android Health Connect (leitura somente) | Exibição de histórico de sessões de atividade física; cálculo de frequência, duração e tipo de exercícios; visualização de tendências de regularidade de prática esportiva | Consentimento explícito do titular — Art. 11, II, a da LGPD | 90 dias (padrão); configurável para 30, 180 ou 365 dias | Nenhum — dados não saem do dispositivo | Banco de dados local criptografado (Room + SQLCipher); chave gerenciada pelo Android Keystore | Exclusão imediata por solicitação, revogação de consentimento ou desinstalação |
| **Registro de consentimento** — data/hora ISO-8601 e versão da política aceita para cada tipo de dado | Dado pessoal (Art. 5º, I) — não é dado de saúde, mas é derivado do tratamento de dados de saúde | Gerado internamente pelo app no momento do aceite ou recusa em cada tela de consentimento do onboarding | Comprovação da validade da base legal de tratamento; auditabilidade do consentimento conforme Art. 7º, § 5º e Art. 11, II, a da LGPD | Obrigação legal — Art. 7º, § 5º da LGPD (dever de comprovação do consentimento pelo controlador) | Mesmo período configurado para o tipo de dado correspondente; excluído conjuntamente com os dados de saúde relacionados | Nenhum — armazenado exclusivamente no dispositivo | Banco de dados local criptografado (Room + SQLCipher); chave gerenciada pelo Android Keystore | Exclusão conjunta com os dados de saúde correspondentes por solicitação ou desinstalação |

---

## Notas Complementares à Tabela

### Infraestrutura de armazenamento
- **Tecnologia:** Room (ORM Android) + SQLCipher (criptografia AES-256 do arquivo de banco de dados)
- **Gerenciamento de chaves:** Android Keystore System — a chave não é acessível ao código da aplicação em texto simples; vinculada ao hardware do dispositivo (TEE ou Strongbox quando disponível)
- **Localização:** Armazenamento interno privado do app (`/data/data/<package>/databases/`) — inacessível a outros apps sem root
- **Backup:** O app deve declarar `android:allowBackup="false"` e `android:fullBackupContent="false"` no AndroidManifest para impedir que dados de saúde sejam incluídos em backups automáticos do Google (recomendação de segurança)

### Relação com o Android Health Connect
- O Health Insights opera como **leitura somente** (`READ` permissions) no Health Connect — nunca escreve dados no Health Connect
- O Health Connect em si é controlado pelo Google e possui política de privacidade própria
- O Health Insights não controla os dados que estão no Health Connect, apenas lê um subconjunto deles com permissão explícita do usuário

### Transferência internacional
- Não ocorre transferência internacional de dados pessoais — Art. 33 e seguintes da LGPD não se aplicam ao tratamento atual
- A distribuição via Google Play Store pode envolver coleta de metadados pelo Google (dados de instalação, crash reports do SO), que são regidos pela política do Google e estão fora do escopo desta tabela

---

## Dados NÃO Coletados

Esta seção é relevante para o **Data Safety Form do Google Play** e para demonstração de minimização de dados (Art. 6º, III da LGPD).

O Health Insights **não coleta, não acessa e não armazena** nenhum dos seguintes dados:

### Dados de identidade
- Nome completo
- Endereço de e-mail
- Número de telefone
- CPF, RG ou qualquer documento de identificação
- Foto ou imagem do usuário
- Nome de usuário ou apelido

### Dados de localização
- Localização precisa (GPS)
- Localização aproximada (rede/IP)
- Histórico de localização
- Localização de rotas de exercício (mesmo que presente no `ExerciseSessionRecord`, não é lida)

### Dados de dispositivo e rede
- Endereço IP
- Identificador de publicidade (GAID — Google Advertising ID)
- Identificador único do dispositivo (ANDROID_ID, IMEI, serial)
- Endereço MAC
- Informações de rede Wi-Fi ou operadora

### Dados de saúde não utilizados
- Peso corporal ou IMC
- Altura
- Temperatura corporal
- Saturação de oxigênio (SpO2)
- Pressão arterial
- Glicemia
- Dados de nutrição ou hidratação
- Dados de ciclo menstrual
- Dados de gravidez
- Dados de medicamentos
- Dados genéticos

### Dados comportamentais e de uso do app
- Telas visitadas (analytics de navegação)
- Tempo de uso do app
- Funcionalidades mais usadas
- Relatórios de falhas (crash reports) — **não há SDK de crash reporting no MVP**
- Logs de uso enviados a terceiros

### Dados financeiros
- Dados de pagamento
- Histórico de compras

### Dados de comunicação
- Conteúdo de mensagens
- Contatos do dispositivo
- Calendário

### Dados de mídia
- Fotos ou vídeos da câmera
- Arquivos de áudio
- Arquivos armazenados no dispositivo

---

## Mapeamento para o Data Safety Form do Google Play

Para facilitar o preenchimento do formulário Data Safety na Google Play Console:

| Pergunta do formulário | Resposta | Observação |
|---|---|---|
| O app coleta dados do usuário? | Sim | Coleta dados de saúde via Health Connect |
| Os dados são compartilhados com terceiros? | Não | Zero compartilhamento |
| Todos os dados são criptografados em trânsito? | N/A — sem trânsito | App é 100% on-device |
| O usuário pode solicitar exclusão dos dados? | Sim | Via app (Configurações > Privacidade) ou e-mail |
| Tipos de dados coletados | Saúde e condicionamento físico | Subcategorias: Atividade física, Sono, Frequência cardíaca |
| Dados coletados são necessários para o app funcionar? | Sim (condicionalmente) | Cada tipo de dado é opcional; recusa não bloqueia o app |
| O app é independentemente verificado em relação a padrões de segurança? | [A PREENCHER] | Recomenda-se avaliação externa antes do lançamento |

> **Alerta:** O Data Safety Form do Google Play é uma declaração pública. Inconsistências entre o formulário e o comportamento real do app podem resultar em remoção do app da loja e penalidades. Recomenda-se revisão técnica e jurídica conjunta antes do preenchimento final.

---

## Histórico de Versões

| Versão | Data | Alterações |
|---|---|---|
| v1.0 | [DATA DE VIGÊNCIA] | Versão inicial — 4 tipos de dados + registro de consentimento |

---

*Health Insights — Tabela de Registro de Tratamento de Dados LGPD v1.0*
*Controlador: [NOME DO DESENVOLVEDOR] — [EMAIL DE CONTATO]*

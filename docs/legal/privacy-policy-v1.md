# Política de Privacidade — Health Insights
<!-- Documento: privacy-policy-v1.md -->

**Versão:** v1.0
**Data de vigência:** 2026-05-04
**Última atualização:** 2026-05-04

---

## 1. Identificação do Controlador

<!-- LGPD Art. 5, VI — o controlador é a pessoa que decide as finalidades e os meios do tratamento -->
<!-- LGPD Art. 9, I — o titular deve ser informado sobre a identidade do controlador -->

O responsável pelo tratamento dos seus dados pessoais neste aplicativo é:

**Nome:** Matheus Wilke
**E-mail:** matheus.wilke@gmail.com
**Atuação:** Desenvolvedor independente (pessoa física)

Para os fins da Lei nº 13.709/2018 (Lei Geral de Proteção de Dados Pessoais — LGPD), Matheus Wilke é o controlador dos dados pessoais tratados pelo aplicativo Health Insights. É ele quem decide para que os dados são usados e de que forma são processados.

**Encarregado de dados (DPO):** Não há encarregado formalmente designado neste momento. O volume de tratamento e o perfil do aplicativo não exigem designação obrigatória de DPO na fase atual. O canal de contato para questões de privacidade é o e-mail acima. Caso o tratamento evolua a um escopo que exija designação nos termos do Art. 41 da LGPD, esta Política será atualizada e um DPO será nomeado.

---

## 2. Sobre o Aplicativo

O Health Insights é um aplicativo Android que lê dados de saúde armazenados no **Android Health Connect** e os transforma em gráficos e tendências para seu acompanhamento pessoal.

**Características fundamentais do tratamento:**

- Todo o processamento acontece exclusivamente no seu dispositivo ("on-device"). O app não tem servidores próprios.
- Nenhum dado é transmitido pela internet — nem para servidores do desenvolvedor, nem para terceiros.
- O app não cria contas de usuário e não coleta identificadores pessoais (nome, e-mail, CPF, localização).
- Não há SDKs de terceiros, ferramentas de análise de comportamento (analytics) ou rastreamento de publicidade.

O Android Health Connect é uma plataforma do Google que centraliza dados de saúde no dispositivo. A forma como o Health Connect armazena e gerencia esses dados é regida pela política de privacidade do próprio Google Health Connect, que é independente desta Política.

---

## 3. Dados Coletados

<!-- LGPD Art. 5, I — dado pessoal: informação relacionada a pessoa identificada ou identificável -->
<!-- LGPD Art. 5, II — dado pessoal sensível: dado de saúde -->
<!-- LGPD Art. 11 — dados de saúde são categoria especial, sujeitos a proteção reforçada -->

O Health Insights acessa as seguintes categorias de dados, todas provenientes do Android Health Connect e classificadas como **dados pessoais sensíveis de categoria especial (dados de saúde)** nos termos do Art. 5, II, e Art. 11 da LGPD:

| # | Tipo de dado | Registro Health Connect | Classificação LGPD |
|---|---|---|---|
| 1 | **Passos** — contagem de passos por período (diário e histórico) | `StepsRecord` | Dado de saúde — Art. 11 |
| 2 | **Sono** — duração e horários de cada sessão de sono | `SleepSessionRecord` | Dado de saúde — Art. 11 |
| 3 | **Frequência cardíaca em repouso** — medições de BPM em repouso | `RestingHeartRateRecord` | Dado de saúde — Art. 11 |
| 4 | **Frequência cardíaca** — medições de BPM ao longo do dia | `HeartRateRecord` | Dado de saúde — Art. 11 |
| 5 | **Exercício** — tipo de atividade e duração de cada sessão (sem GPS, sem rota) | `ExerciseSessionRecord` | Dado de saúde — Art. 11 |

Adicionalmente, o aplicativo armazena localmente:

| # | Tipo de dado | Finalidade |
|---|---|---|
| 6 | **Registro de consentimento** — data, hora, versão da política aceita e tipo de dado autorizado | Comprovação da base legal de tratamento (Art. 7, § 5 e Art. 11, II, a da LGPD) |

**O aplicativo NÃO coleta, em nenhuma circunstância:** nome, e-mail, CPF, telefone, localização geográfica, endereço IP, identificadores de dispositivo (IMEI, Android ID, GAID), dados de câmera ou microfone, dados de nutrição, dados clínicos (glicose, pressão arterial, saturação de oxigênio), dados financeiros, histórico de navegação, cookies, ou qualquer outro dado não listado acima.

---

## 4. Finalidade do Tratamento

<!-- LGPD Art. 6, I — princípio da finalidade: tratamento para propósitos legítimos, específicos e explícitos -->
<!-- LGPD Art. 9, I — o titular deve ser informado sobre a finalidade do tratamento -->
<!-- LGPD Art. 11, II, a — tratamento de dados de saúde requer consentimento para finalidade específica -->

O tratamento de cada tipo de dado serve às seguintes finalidades específicas:

### 4.1 Passos (`StepsRecord`)

Exibição da contagem diária de passos; cálculo de médias, tendências semanais e comparações entre períodos; visualização do progresso de atividade física ao longo do tempo na tela inicial do app.

### 4.2 Sono (`SleepSessionRecord`)

Exibição da duração de cada noite de sono; cálculo de médias e variações de padrões de sono; identificação de tendências de regularidade de horários de dormir e acordar ao longo das semanas.

### 4.3 Frequência cardíaca em repouso e frequência cardíaca (`RestingHeartRateRecord` + `HeartRateRecord`)

Exibição do histórico de frequência cardíaca em repouso ao longo do tempo; visualização de variações para acompanhamento pessoal do usuário. O app não realiza interpretações clínicas, diagnósticos ou avaliações médicas.

### 4.4 Exercício (`ExerciseSessionRecord`)

Exibição do histórico de sessões de atividade física com tipo e duração; inclusão dos dias de exercício no resumo semanal de atividades; visualização da frequência de prática ao longo do tempo. Apenas o tipo e a duração são lidos — GPS e dados de rota não são acessados.

### 4.5 Registro de consentimento

Comprovação de que o consentimento foi obtido de forma válida, com registro do tipo de dado autorizado, da data e da versão da política aceita, conforme exigência dos Art. 7, § 5, e Art. 11, II, a da LGPD.

**Vedação de uso secundário:** Os dados não serão utilizados para nenhuma finalidade além das descritas acima. Especificamente, o app não realiza inferências diagnósticas, não fornece avaliações médicas, não utiliza os dados para publicidade ou perfilamento comportamental e não os vende ou licencia a terceiros.

---

## 5. Base Legal para o Tratamento

<!-- LGPD Art. 11, II, a — única base legal aplicável para app de bem-estar pessoal com dados de saúde -->
<!-- LGPD Art. 8 — requisitos para que o consentimento seja válido -->

O tratamento de dados de saúde pelo Health Insights fundamenta-se exclusivamente no **consentimento explícito do titular**, conforme o **Art. 11, II, alínea "a" da LGPD**.

O consentimento obtido pelo app tem as seguintes características, todas exigidas pela LGPD:

- **Explícito:** solicitado por tela dedicada, com texto descritivo, antes de qualquer leitura de dados.
- **Informado:** cada tela descreve o dado específico, a finalidade e a ausência de transmissão externa.
- **Granular:** o usuário pode consentir ou recusar para cada tipo de dado individualmente — não há aceite único para todos os dados.
- **Livre:** a recusa não bloqueia o uso do app. As funcionalidades que não dependem do dado recusado continuam disponíveis (Art. 8, § 5).
- **Por ação afirmativa:** o consentimento exige o toque em botão de aceite com label explícito. Não há consentimento por omissão, checkbox pré-marcado ou scroll (Art. 8, § 3).
- **Registrado:** a data, a hora e a versão da política aceita ficam armazenadas localmente como comprovante da validade do consentimento.
- **Revogável:** pode ser retirado a qualquer momento em Configurações → Privacidade → Revogar Acesso à Saúde, sem custo e sem necessidade de justificativa (Art. 8, § 5).

---

## 6. Compartilhamento de Dados

<!-- LGPD Art. 9, V — o titular deve ser informado sobre o compartilhamento com terceiros -->
<!-- LGPD Art. 11, § 4 — vedação de comunicação de dados de saúde a terceiros sem consentimento -->

**Nenhum terceiro tem acesso aos seus dados de saúde.**

Os dados são processados exclusivamente no seu dispositivo. O Health Insights não transmite, vende, aluga, cede, compartilha nem disponibiliza seus dados a quaisquer terceiros, incluindo:

- Empresas de publicidade ou marketing
- Plataformas de análise de comportamento (analytics)
- Seguradoras ou operadoras de plano de saúde
- Empregadores ou outras pessoas físicas
- O próprio desenvolvedor do aplicativo (os dados nunca saem do dispositivo)
- Autoridades governamentais — salvo obrigação legal superveniente devidamente comprovada, caso em que o usuário será informado na medida do juridicamente permitido

O aplicativo não integra SDKs de terceiros, bibliotecas de rastreamento, coletores de relatórios de falha (crash reporters) ou qualquer componente de software que transmita dados para fora do dispositivo. O inventário atual de dependências é zero SDKs com acesso a dados do usuário.

**Relação com o Android Health Connect:** O Health Insights lê dados já armazenados no Health Connect, mas não grava dados nessa plataforma. O gerenciamento dos dados dentro do Health Connect é responsabilidade do Google, sob a política de privacidade do Android Health Connect.

---

## 7. Retenção de Dados

<!-- LGPD Art. 9, III — o titular deve ser informado sobre a forma e a duração do tratamento -->
<!-- LGPD Art. 15 — o tratamento encerra quando o prazo determinado se cumpre ou a finalidade se esgota -->
<!-- LGPD Art. 16 — após o encerramento, os dados devem ser eliminados -->

Os dados de saúde são armazenados localmente no dispositivo pelo período configurado pelo usuário:

| Período de retenção | Descrição |
|---|---|
| **12 meses** | **Padrão — aplicado se o usuário não alterar a configuração** |
| Configurável | O usuário pode ajustar o período em Configurações → Privacidade → Meus Dados |

Dados mais antigos que o período configurado são excluídos automaticamente pelo aplicativo.

**Exclusão completa ocorre nas seguintes situações:**

1. **Desinstalação do app** — o sistema operacional Android apaga todos os dados armazenados pelo app.
2. **Solicitação do titular** — mediante ação em Configurações → Privacidade → Meus Dados → Apagar, com execução imediata e irreversível.
3. **Revogação do consentimento** — a revogação do consentimento para um tipo de dado implica a exclusão imediata de todos os dados daquela categoria armazenados pelo app.

**Registro de consentimento:** Armazenado pelo mesmo período que os dados a que se refere, ou até que o titular solicite exclusão. A exclusão do registro de consentimento é realizada conjuntamente com a exclusão dos dados correspondentes e significa que o histórico de autorização é apagado.

---

## 8. Direitos do Titular

<!-- LGPD Art. 18 — rol completo dos direitos do titular de dados pessoais -->
<!-- LGPD Art. 18, § 3 — o controlador deve responder em prazo razoável -->

Nos termos do **Art. 18 da LGPD**, você tem os seguintes direitos em relação aos seus dados, exercíveis a qualquer momento:

### 8.1 Confirmação da existência de tratamento (Art. 18, I)

**O que é:** saber se o app está tratando seus dados e quais.
**Como exercer:** acesse Configurações → Privacidade → Meus Dados. O app exibe quais tipos de dado estão sendo processados e o período de retenção ativo.

### 8.2 Acesso aos dados (Art. 18, II)

**O que é:** visualizar os dados que o app armazena sobre você.
**Como exercer:** acesse Configurações → Privacidade → Meus Dados → Exportar. Você pode gerar um arquivo com todos os dados armazenados localmente pelo app.

### 8.3 Correção de dados (Art. 18, III)

**O que é:** corrigir dados incompletos, inexatos ou desatualizados.
**Como exercer:** o Health Insights lê dados do Health Connect, mas não os edita. Para corrigir um dado, utilize o aplicativo de saúde que o registrou originalmente ou o próprio Android Health Connect. O Health Insights atualizará a leitura automaticamente na próxima sincronização.

### 8.4 Anonimização, bloqueio ou eliminação de dados desnecessários (Art. 18, IV)

**O que é:** solicitar que dados tratados em desconformidade sejam eliminados ou bloqueados.
**Como exercer:** acesse Configurações → Privacidade → Meus Dados → Apagar para excluir dados por categoria ou todos de uma vez. Para bloqueio temporário, revogar o consentimento em Configurações → Privacidade → Revogar Acesso à Saúde impede imediatamente novos acessos pelo app.

### 8.5 Portabilidade dos dados (Art. 18, V)

**O que é:** receber seus dados em formato estruturado para uso em outro serviço.
**Como exercer:** acesse Configurações → Privacidade → Meus Dados → Exportar. Os dados são exportados em formato JSON. Por ser um app 100% on-device sem servidor, não há portabilidade automática para outros sistemas — o arquivo exportado fica disponível para o usuário usar como preferir.

### 8.6 Informação sobre compartilhamento (Art. 18, V)

**O que é:** saber com quais entidades seus dados foram compartilhados.
**Como exercer:** conforme declarado na Seção 6, nenhum dado é compartilhado com terceiros. Esta informação está disponível em Configurações → Privacidade → Compartilhamento de Dados.

### 8.7 Eliminação dos dados tratados com consentimento (Art. 18, VI)

**O que é:** solicitar a exclusão dos dados quando o tratamento é baseado em consentimento.
**Como exercer:** acesse Configurações → Privacidade → Meus Dados → Apagar, ou envie solicitação para matheus.wilke@gmail.com. A exclusão é imediata e irreversível. O prazo máximo de resposta para solicitações por e-mail é de 15 dias corridos (Art. 18, § 3).

### 8.8 Revogação do consentimento (Art. 18, IX c/c Art. 8, § 5)

**O que é:** retirar o consentimento dado anteriormente, sem custo e sem precisar explicar o motivo.
**Como exercer:** acesse Configurações → Privacidade → Revogar Acesso à Saúde. A revogação encerra imediatamente a leitura de novos dados e apaga os dados armazenados da categoria revogada. O app continua funcionando para as categorias cujo consentimento foi mantido.

### 8.9 Oposição ao tratamento (Art. 18, § 2)

**O que é:** manifestar oposição ao tratamento quando este não está em conformidade com a LGPD.
**Como exercer:** envie solicitação para matheus.wilke@gmail.com descrevendo a irregularidade identificada. O prazo de resposta é de 15 dias corridos.

---

## 9. Transferência Internacional de Dados

<!-- LGPD Art. 33 — transferência internacional somente nas hipóteses permitidas -->
<!-- LGPD Art. 9, VII — o titular deve ser informado sobre transferência internacional -->

**Não ocorre transferência internacional de dados.**

Os dados de saúde tratados pelo Health Insights permanecem no dispositivo do usuário. Não há transmissão a servidores, nuvens, parceiros ou qualquer sistema fora do dispositivo — seja no Brasil ou no exterior.

Nota: o Google Play Store processa dados relacionados à distribuição do aplicativo (instalação e atualizações) sob sua própria política de privacidade. Esses dados são de responsabilidade do Google e não incluem os dados de saúde tratados pelo Health Insights.

---

## 10. Segurança dos Dados

<!-- LGPD Art. 6, VII — princípio da segurança: medidas técnicas e administrativas adequadas -->
<!-- LGPD Art. 46 — o controlador deve adotar medidas de segurança para proteger os dados pessoais -->
<!-- LGPD Art. 48 — obrigação de notificação em caso de incidente de segurança -->

Os dados de saúde são protegidos pelas seguintes medidas técnicas, todas aplicadas localmente no dispositivo:

**Criptografia do banco de dados (SQLCipher — AES-256):** Todos os dados armazenados pelo app são criptografados com o padrão AES-256 via SQLCipher. Isso significa que os dados ficam "embaralhados" e são ilegíveis sem a chave correta — mesmo que alguém tenha acesso físico ao dispositivo e tente extrair os arquivos do banco de dados.

**Chave protegida pelo Android Keystore:** A chave que decodifica os dados é gerada e armazenada no sistema Android Keystore, uma área de segurança isolada do sistema operacional vinculada ao hardware do aparelho. A chave nunca é exposta em texto legível e nunca sai do dispositivo.

**Sem transmissão de dados:** Por não haver conexão com servidores externos, não há risco de interceptação de dados em trânsito — simplesmente não há trânsito.

**Limitações reconhecidas:** Nenhum sistema de segurança é absolutamente invulnerável. Em dispositivos com root ativado, as proteções do Android Keystore podem ser comprometidas. Recomendamos o uso de bloqueio de tela e a não utilização do app em dispositivos com root para proteger seus dados de saúde.

**Em caso de incidente de segurança:** Se identificarmos qualquer incidente que resulte em risco real ao titular, notificaremos a Autoridade Nacional de Proteção de Dados (ANPD) e os titulares afetados nos termos do **Art. 48 da LGPD**, no prazo de 2 (dois) dias úteis a partir da ciência do incidente, conforme regulamentação da ANPD.

---

## 11. Alterações nesta Política

<!-- LGPD Art. 8, § 6 — alterações materiais exigem novo consentimento -->
<!-- LGPD Art. 9 — o titular deve ser informado sobre mudanças nas condições de tratamento -->

Esta Política pode ser atualizada quando o app muda ou quando a legislação exige revisão. As alterações são classificadas em dois tipos:

**Mudanças materiais** — qualquer uma das seguintes situações exige nova versão da política e novo consentimento explícito do usuário antes que o tratamento alterado se inicie:
- Adição de um novo tipo de dado coletado
- Nova finalidade para um dado já coletado
- Início de compartilhamento com terceiros (atualmente inexistente)
- Alteração do período de retenção padrão

Quando houver mudança material, o app solicitará reconsentimento na próxima abertura após a atualização. Usuários que não reconsentam terão os dados da categoria alterada excluídos.

**Mudanças não materiais** — correções de texto, reorganização de seções, atualização de informações de contato — serão comunicadas por notificação no app, sem exigência de novo consentimento.

O histórico de versões desta Política é mantido no repositório do aplicativo e acessível em Configurações → Privacidade → Histórico de Política.

| Versão | Data | Tipo de alteração |
|---|---|---|
| v1.0 | 2026-05-04 | Versão inicial — Health Insights MVP |

---

## 12. Como Entrar em Contato

<!-- LGPD Art. 18, § 3 — o controlador deve responder às solicitações do titular em prazo razoável -->
<!-- LGPD Art. 9, I — o titular deve ser informado sobre como contatar o controlador -->

Para exercer seus direitos descritos na Seção 8, tirar dúvidas sobre esta Política ou reportar qualquer questão relacionada à privacidade dos seus dados:

**E-mail:** matheus.wilke@gmail.com
**Assunto sugerido:** [Health Insights — Privacidade] + descrição do pedido

Comprometo-me a responder a todas as solicitações no prazo de **15 (quinze) dias corridos**, contados do recebimento, conforme o **Art. 18, § 3 da LGPD**. Em casos de maior complexidade, o prazo poderá ser prorrogado por igual período, com comunicação ao solicitante.

Para reclamações não resolvidas diretamente com o controlador, o titular pode acionar a **Autoridade Nacional de Proteção de Dados (ANPD)** em: [https://www.gov.br/anpd](https://www.gov.br/anpd).

---

## 13. Vigência

<!-- LGPD Art. 9, III — o titular deve ser informado sobre a duração do tratamento -->

Esta Política entra em vigor em **2026-05-04** e permanece válida até que seja substituída por versão posterior devidamente notificada conforme a Seção 11.

A versão da Política aceita pelo usuário no momento do consentimento fica registrada localmente no dispositivo e determina as condições de tratamento aplicáveis àquele usuário até que uma mudança material exija novo consentimento.

---

## 14. Lei Aplicável

Esta Política é regida pela **Lei nº 13.709/2018 (Lei Geral de Proteção de Dados Pessoais — LGPD)** e pelas demais normas brasileiras aplicáveis à proteção de dados pessoais, incluindo regulamentos e orientações da Autoridade Nacional de Proteção de Dados (ANPD).

Para resolução de conflitos, fica eleito o foro da comarca de domicílio do controlador, ressalvada a competência da ANPD para apreciar reclamações administrativas.

---

*Health Insights — Política de Privacidade v1.0*
*Vigente a partir de 2026-05-04*
*Controlador: Matheus Wilke — matheus.wilke@gmail.com*

# CI/CD Setup Guide — Health Insights

Guia completo para o founder configurar o repositório GitHub e o pipeline de CI/CD do Health Insights.

---

## Índice

1. [Criar repositório no GitHub](#1-criar-repositório-no-github)
2. [Configurar branch protection em main](#2-configurar-branch-protection-em-main)
3. [Secrets do GitHub Actions](#3-secrets-do-github-actions)
4. [Gerar o keystore de release](#4-gerar-o-keystore-de-release)
5. [Converter keystore para base64 (Windows PowerShell)](#5-converter-keystore-para-base64-windows-powershell)
6. [Configurar os secrets no GitHub](#6-configurar-os-secrets-no-github)
7. [Ativar upload para Play Store (quando disponível)](#7-ativar-upload-para-play-store-quando-disponível)
8. [Verificar se o pipeline está funcionando](#8-verificar-se-o-pipeline-está-funcionando)

---

## 1. Criar repositório no GitHub

1. Acesse [github.com/new](https://github.com/new).
2. Preencha:
   - **Repository name:** `health-insights`
   - **Visibility:** Private (o código é proprietário; não torna público até definir licença)
   - **Initialize repository:** desmarque tudo — o projeto local já tem os arquivos
3. Clique em **Create repository**.
4. No terminal local, adicione o remote e faça o push inicial:

```powershell
cd C:\Dev\Claude-Code\Health-insights

git init
git add .
git commit -m "chore: initial project setup + CI/CD pipeline (PRE-09)"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/health-insights.git
git push -u origin main
```

> Substitua `SEU_USUARIO` pelo seu username do GitHub.

---

## 2. Configurar branch protection em main

A branch protection garante que **nenhum código vai para main sem passar por todos os checks do CI**.

1. No repositório GitHub, acesse **Settings → Branches**.
2. Clique em **Add branch ruleset** (ou "Add rule" na interface antiga).
3. Configure:

| Campo | Valor |
|---|---|
| Branch name pattern | `main` |
| Require a pull request before merging | Habilitado |
| Required approvals | 1 (ou 0 se você for o único desenvolvedor por ora) |
| Require status checks to pass before merging | Habilitado |
| Require branches to be up to date before merging | Habilitado |
| Do not allow bypassing the above settings | Habilitado |

4. Em **Status checks that are required**, adicione exatamente estes checks (são os `name:` dos jobs no `ci-pr.yml`):

```
Lint (ktlint + detekt)
Unit Tests + Kover Coverage
Android Lint
Build Debug APK
UI Tests (Emulador API 34)
```

   > Os checks só aparecem para seleção depois que o workflow rodar ao menos uma vez. Se ainda não apareceram, salve a regra sem os checks, faça um PR de teste, aguarde o workflow rodar e depois edite a regra para adicioná-los.

5. Clique em **Save changes** (ou **Create**).

---

## 3. Secrets do GitHub Actions

Os workflows precisam de **5 secrets** para funcionar completamente. Os primeiros 4 são necessários para o `ci-main.yml` gerar o AAB assinado. O quinto é necessário para publicação na Play Store (configuração futura).

| Secret | Descrição | Quando é necessário |
|---|---|---|
| `KEYSTORE_BASE64` | Conteúdo do arquivo `.jks` do keystore de release codificado em base64. É o arquivo que assina os builds enviados para a Play Store. | Antes do primeiro `ci-main.yml` rodar com `bundleRelease` |
| `KEYSTORE_PASSWORD` | Senha que protege o arquivo keystore (definida na criação com `keytool`). | Junto com `KEYSTORE_BASE64` |
| `KEY_ALIAS` | Alias da chave dentro do keystore (definido na criação com `keytool`, ex: `health-insights-key`). | Junto com `KEYSTORE_BASE64` |
| `KEY_PASSWORD` | Senha da chave individual dentro do keystore (pode ser igual à `KEYSTORE_PASSWORD`). | Junto com `KEYSTORE_BASE64` |
| `PLAY_SERVICE_ACCOUNT_JSON` | JSON completo da service account do Google Cloud com permissão na Play Console. Usado para publicação automática. | Somente quando ativar o stub de upload (ver seção 7) |

**Regra de segurança:** Nunca coloque esses valores em arquivos do repositório, em `.env`, nem os imprima em logs. Os workflows já estão configurados para usá-los de forma segura.

---

## 4. Gerar o keystore de release

O keystore é gerado uma única vez e guardado em local seguro (ex: cofre de senhas). **Perder o keystore significa não conseguir atualizar o app na Play Store** — o Google não aceita builds com keystore diferente do original.

### Pré-requisito

O `keytool` faz parte do JDK. Verifique se está disponível:

```powershell
keytool -version
```

Se não estiver no PATH, adicione o diretório `bin` do JDK ao PATH, ou use o caminho completo:

```powershell
# Exemplo com JDK 17 em localização padrão
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot\bin\keytool.exe" -version
```

### Gerar o keystore

```powershell
keytool -genkey -v `
  -keystore health-insights-release.jks `
  -alias health-insights-key `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000
```

O comando vai solicitar:

| Prompt | O que responder |
|---|---|
| Enter keystore password | Defina uma senha forte (ex: gerada por gerenciador de senhas). Salve como `KEYSTORE_PASSWORD`. |
| Re-enter new password | Repita a mesma senha. |
| What is your first and last name? | Seu nome completo. |
| What is your organizational unit? | Pode deixar em branco ou colocar "Development". |
| What is your organization? | Nome do seu negócio ou nome pessoal. |
| What is your City or Locality? | Sua cidade. |
| What is your State or Province? | Seu estado. |
| What is the two-letter country code? | `BR` |
| Enter key password for health-insights-key | Pode ser a mesma senha do keystore ou uma diferente. Salve como `KEY_PASSWORD`. |

Ao final, o arquivo `health-insights-release.jks` será criado no diretório atual.

**Guarde este arquivo em local seguro.** Sugestões:
- Cofre de senhas (1Password, Bitwarden) com anexo de arquivo
- Repositório privado separado de backup (nunca no repositório do app)
- Backup criptografado offline

---

## 5. Converter keystore para base64 (Windows PowerShell)

O GitHub Actions não aceita arquivos binários como secrets — apenas strings. A solução é codificar o `.jks` em base64.

```powershell
# Converte o keystore para base64 e copia para a área de transferência
$keystorePath = ".\health-insights-release.jks"
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keystorePath))

# Opção 1: Copiar para área de transferência (para colar direto no GitHub)
$base64 | Set-Clipboard
Write-Host "Base64 copiado para a area de transferencia. Cole no secret KEYSTORE_BASE64."

# Opção 2: Salvar em arquivo de texto (útil se a string for muito longa para o clipboard)
$base64 | Out-File -FilePath "keystore-base64.txt" -Encoding ASCII -NoNewline
Write-Host "Base64 salvo em keystore-base64.txt"
```

> **Atenção:** O arquivo `keystore-base64.txt` também é sensível. Delete-o após configurar o secret no GitHub.

---

## 6. Configurar os secrets no GitHub

1. No repositório GitHub, acesse **Settings → Secrets and variables → Actions**.
2. Clique em **New repository secret** para cada secret:

### `KEYSTORE_BASE64`
- Name: `KEYSTORE_BASE64`
- Secret: cole o conteúdo gerado no passo 5 (string base64 longa, sem quebras de linha)

### `KEYSTORE_PASSWORD`
- Name: `KEYSTORE_PASSWORD`
- Secret: a senha do keystore definida na criação com `keytool`

### `KEY_ALIAS`
- Name: `KEY_ALIAS`
- Secret: `health-insights-key` (ou o alias que você definiu na criação)

### `KEY_PASSWORD`
- Name: `KEY_PASSWORD`
- Secret: a senha da chave individual (pode ser igual à `KEYSTORE_PASSWORD`)

### `PLAY_SERVICE_ACCOUNT_JSON`
- Name: `PLAY_SERVICE_ACCOUNT_JSON`
- Secret: **deixe para configurar quando o app existir na Play Console** (ver seção 7)
- Se criar agora, coloque um valor placeholder: `{}` — o step de upload está comentado e não será executado

### Verificar

Após criar os secrets, a tela deve listar:

```
KEY_ALIAS              Updated X minutes ago
KEY_PASSWORD           Updated X minutes ago
KEYSTORE_BASE64        Updated X minutes ago
KEYSTORE_PASSWORD      Updated X minutes ago
PLAY_SERVICE_ACCOUNT_JSON  Updated X minutes ago
```

---

## 7. Ativar upload para Play Store (quando disponível)

O step de upload para o internal track está **comentado** em `ci-main.yml`. Para ativá-lo no futuro:

### Pré-requisitos

1. O app já deve existir na Play Console (o primeiro upload tem que ser manual via interface web).
2. A Play Console deve estar configurada com acesso à Google Play Android Developer API.

### Criar a service account

1. Acesse o [Google Cloud Console](https://console.cloud.google.com).
2. Crie ou selecione o projeto associado à Play Console.
3. Habilite a **Google Play Android Developer API**.
4. Acesse **IAM & Admin → Service Accounts → Create Service Account**.
5. Defina um nome (ex: `github-actions-deploy`) e clique em **Create and Continue**.
6. Não atribua papéis no GCP — as permissões são gerenciadas na Play Console.
7. Clique em **Done**, abra a service account criada e gere uma chave JSON (**Keys → Add Key → Create new key → JSON**).
8. Salve o arquivo JSON gerado.

### Vincular à Play Console

1. Na Play Console, acesse **Configuração → Acesso à API**.
2. Vincule ao projeto Google Cloud criado acima.
3. Na lista de service accounts, clique em **Conceder acesso** para a service account criada.
4. Papel mínimo necessário: **Gerente de versão** (permite upload para internal/alpha/beta).

### Configurar o secret e ativar o stub

1. Abra o arquivo JSON da service account, copie o conteúdo inteiro.
2. Salve como secret `PLAY_SERVICE_ACCOUNT_JSON` no GitHub (substitua o placeholder `{}`).
3. No arquivo `.github/workflows/ci-main.yml`, localize o bloco comentado:

```yaml
      # - name: Upload para Play Store (internal track)
      #   uses: r0adkll/upload-google-play@v1
      #   ...
```

4. Remova os `#` de todos os campos do bloco.
5. Atualize `packageName` com o package name real do app (ex: `com.healthinsights.app`).
6. Faça commit e push — o próximo merge em main fará o upload automaticamente.

---

## 8. Verificar se o pipeline está funcionando

Após configurar tudo, crie um PR de teste:

```powershell
git checkout -b chore/test-ci-pipeline
# Faça qualquer alteração trivial (ex: adicionar um comentário em qualquer arquivo)
git add .
git commit -m "chore: test CI pipeline"
git push -u origin chore/test-ci-pipeline
```

Abra o PR no GitHub e observe a aba **Checks**. Os 5 jobs devem aparecer e rodar sequencialmente:

1. `Lint (ktlint + detekt)` — alguns minutos
2. `Unit Tests + Kover Coverage` — alguns minutos
3. `Android Lint` — alguns minutos
4. `Build Debug APK` — alguns minutos
5. `UI Tests (Emulador API 34)` — 10-20 minutos (emulador é lento)

Quando todos os checks passarem (marcas verdes), o PR pode ser mergeado. Após o merge, o `ci-main.yml` rodará automaticamente e adicionará o job `Build Release AAB + Sign`.

### Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| Step "Rodar ktlint" falha com "Task not found" | Plugin ktlint não configurado nos módulos | Configurar `id("org.jlleitschuh.gradle.ktlint")` nos `build.gradle.kts` dos módulos |
| Step "Verificar threshold Kover" falha | Plugin Kover não configurado | Configurar `id("org.jetbrains.kotlinx.kover")` no build raiz |
| Testes instrumentados falham com timeout | Emulador demorou para inicializar | Aumentar o timeout do `android-emulator-runner` ou verificar os logs de boot |
| "KEYSTORE_BASE64: invalid base64" | String com quebra de linha no secret | Regenerar o base64 com `-NoNewline` e recriar o secret |
| AAB não assina (invalid keystore format) | Keystore corrompido na decodificação | Verificar se o base64 foi gerado sem quebras de linha e sem BOM |

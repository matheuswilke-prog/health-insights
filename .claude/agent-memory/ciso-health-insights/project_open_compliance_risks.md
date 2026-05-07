---
name: Riscos de compliance abertos — 2026-05-05
description: Lista de riscos LGPD/compliance identificados pelo CISO mas ainda não resolvidos. Devem ser revisitados antes do release.
type: project
---

**Risco aberto 1 — privacy-policy-v1.md desatualizada (alta prioridade).** A Política de Privacidade vigente (v1.0, 2026-05-04) lista passos, sono, FC de repouso e FC contínua como tipos de dado tratados. O produto pivotou para calorias/peso/treinos. Publicar consent-copy v1.1 sem antes atualizar a Policy para v1.1 cria divergência entre o que o usuário consente e o que a Política declara — risco de invalidação do consentimento (Art. 9). Bloqueador antes do release MVP.

**Risco aberto 2 — chave SQLCipher derivada do Keystore.** Privacy Policy item 10 declara "chave protegida pelo Android Keystore". Verificar na implementação do CTO que a chave do SQLCipher é (a) gerada no Keystore com `setIsStrongBoxBacked` quando disponível, (b) nunca exportada em texto, (c) protegida por `setUserAuthenticationRequired(false)` apenas se houver justificativa documentada (caso contrário, exigir auth). Sem isso, a declaração na Policy vira false claim.

**Risco aberto 3 — comprometimento da chave em devices com root.** Policy item 10 reconhece a limitação. Implementação deve detectar root em runtime (RootBeer ou similar) e exibir aviso ao usuário antes da primeira escrita de dado de saúde. Decisão pendente sobre se app deve recusar funcionar em root ou apenas avisar.

**Risco aberto 4 — registro de consentimento criptografado.** Spec onboarding item 4 da T4 cita "registro criptografado em Room" — confirmar com CTO que essa Room table usa o mesmo SQLCipher do resto do app (não DataStore plain). Caso contrário, comprovante da base legal vira evidência fraca em auditoria ANPD.

**How to apply:** antes de aprovar release MVP, todos os 4 itens devem estar fechados. Em qualquer review futuro de PR que toque consent flow, encryption ou Room schema, conferir contra esta lista. Atualizar essa memória conforme cada item for fechado.

---
name: Modelo de consentimento Tela 4 — aprovado em 2026-05-05
description: Decisão CISO-1 de 2026-05-05 sobre modelo de consentimento da Tela 4 do onboarding (1 tela vs. wizard) com condições mínimas obrigatórias para conformidade LGPD Art. 11.
type: project
---

Modelo aprovado para a Tela 4 do onboarding: **1 tela com 3 toggles granulares** (Calorias, Peso, Treinos), todos com default OFF, descrição de finalidade inline (não em modal/link/expansor), CTA "Concordo" habilitado após scroll-to-bottom. Decisão de 2026-05-05.

**Why:** com apenas 3 tipos de dado no produto pivotado (vs. 5 da v1.0), wizard de telas separadas vira fricção sem ganho de granularidade. A regra "uma tela por tipo de dado" do consent-copy-v1.0 (item 2 das Notas de implementação) foi escrita para impedir agrupamento ofuscado, não para exigir wizard quando o número de tipos é pequeno e cada tipo é tratado de forma independente. Toggles independentes + descrição inline + default OFF + ação afirmativa por toggle satisfazem Art. 11 II "a" e Art. 8 §3.

**How to apply:** sempre que alguém propuser bundling de permissões, agrupamento por categoria genérica ("dados de atividade"), ou pre-checked toggles, **rejeitar**. Sempre que alguém propuser scroll como substituto do toque afirmativo no CTA, **rejeitar** — scroll é prova de oportunidade de leitura, não é consentimento. Se o número de tipos de dado subir para 5+ no futuro, reavaliar voltando ao modelo de wizard. Para todo novo tipo de dado adicionado, exigir descrição inline com 3 blocos: "O que o app lê", "Para que serve", "O que o app NÃO lê".

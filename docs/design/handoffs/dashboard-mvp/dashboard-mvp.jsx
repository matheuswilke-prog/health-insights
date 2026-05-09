/* eslint-disable no-undef */
// Health Insights — Dashboard MVP (8 estados obrigatorios)
// Fonte de produto: docs/product/prd/dashboard-mvp.md
// Fonte de comportamento: docs/specs/dashboard-mvp-spec.md
// Fonte visual: design-system.md / tokens.css
//
// Estados:
//   01 complete       Dashboard completo (saldo, status, ingestao, gasto, peso)
//   02 no-intake      Sem ingestao calorica registrada hoje
//   03 partial-perm   Permissao parcial (peso ok, calorias negadas)
//   04 hc-unavailable Health Connect indisponivel
//   05 no-weight      Sem peso registrado
//   06 error          Erro inesperado (CTA Tentar novamente)
//   07 loading        Skeleton leve
//   08 local-invalid  Estado local invalido (CTA Refazer configuracao)

const HI_BR = (n, opts = {}) => {
  const { sign = false } = opts;
  const abs = Math.abs(n).toLocaleString('pt-BR');
  if (n < 0) return `−${abs}`;
  if (sign && n > 0) return `+${abs}`;
  return abs;
};

const TODAY = 'ter, 6 mai';

// ─────────────────────────────────────────────────────────────
// atoms
// ─────────────────────────────────────────────────────────────
const ShieldGlyph = ({ size = 13, color = 'currentColor' }) => (
  <svg width={size} height={size} viewBox="0 0 14 14" fill="none" aria-hidden="true">
    <path d="M7 1.5L2.5 3.5v3.2c0 2.8 1.9 5.4 4.5 5.8 2.6-.4 4.5-3 4.5-5.8V3.5L7 1.5z"
      stroke={color} strokeWidth="1.2" fill="none"/>
  </svg>
);

const Chevron = ({ size = 12, color = 'currentColor' }) => (
  <svg width={size} height={size} viewBox="0 0 12 12" fill="none" aria-hidden="true">
    <path d="M4.5 3l3 3-3 3" stroke={color} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

// Header: data, titulo, link Configuracoes
const TopBar = () => (
  <div style={{
    padding: '20px 24px 0',
    display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12,
  }}>
    <div>
      <div style={{
        fontFamily: 'var(--font-mono)', fontSize: 11, fontWeight: 500,
        letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--ink-3)',
      }}>{TODAY}</div>
      <h1 className="tight" style={{
        margin: '4px 0 0', fontSize: 28, fontWeight: 600, letterSpacing: '-0.02em',
        color: 'var(--ink-1)', lineHeight: 1.05,
      }}>Hoje</h1>
    </div>
    <button aria-label="Configuracoes" style={{
      width: 40, height: 40, borderRadius: 999, border: '1px solid var(--hairline)',
      background: 'var(--bg-elev)', color: 'var(--ink-2)', cursor: 'pointer',
      display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
    }}>
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="3.5" r="1.2" fill="currentColor"/>
        <circle cx="8" cy="8" r="1.2" fill="currentColor"/>
        <circle cx="8" cy="12.5" r="1.2" fill="currentColor"/>
      </svg>
    </button>
  </div>
);

// Privacy footer (always present, calmo)
const PrivacyFoot = () => (
  <div style={{
    padding: '14px 24px 28px',
    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
    fontSize: 11, color: 'var(--ink-3)',
  }}>
    <ShieldGlyph color="var(--ink-3)"/>
    <span>Dados ficam no aparelho</span>
  </div>
);

// Section eyebrow label
const Eyebrow = ({ children, tone = 'normal' }) => (
  <div style={{
    fontFamily: 'var(--font-mono)', fontSize: 10, fontWeight: 500,
    letterSpacing: '0.1em', textTransform: 'uppercase',
    color: tone === 'muted' ? 'var(--ink-4)' : 'var(--ink-3)',
  }}>{children}</div>
);

// Generic card
const Card = ({ children, style }) => (
  <div className="card" style={{
    background: 'var(--bg-elev)', borderRadius: 'var(--r-lg)',
    border: '1px solid var(--hairline)', padding: 20, ...style,
  }}>{children}</div>
);

// Status pill — sempre texto + cor (a11y)
const StatusPill = ({ kind }) => {
  const map = {
    deficit:    { bg: 'oklch(0.95 0.04 155)', fg: 'var(--brand-ink)',  dot: 'var(--deficit)',  label: 'Em deficit' },
    maintain:   { bg: 'oklch(0.95 0.03 250)', fg: 'oklch(0.40 0.06 250)', dot: 'var(--maintain)', label: 'Em manutencao' },
    surplus:    { bg: 'oklch(0.95 0.04 35)',  fg: 'oklch(0.40 0.10 35)',  dot: 'var(--surplus)',  label: 'Em superavit' },
  }[kind];
  return (
    <div role="status" style={{
      display: 'inline-flex', alignItems: 'center', gap: 8,
      padding: '8px 14px', borderRadius: 999,
      background: map.bg, color: map.fg,
      fontSize: 13, fontWeight: 600,
    }}>
      <span style={{ width: 8, height: 8, borderRadius: 999, background: map.dot }}/>
      {map.label}
    </div>
  );
};

// Empty/permission inline note (within a card)
const InlineNote = ({ tone = 'neutral', text, action, onAction }) => {
  const tones = {
    neutral: { bg: 'var(--bg-sunken)',                  fg: 'var(--ink-2)' },
    info:    { bg: 'oklch(0.96 0.02 250)',              fg: 'oklch(0.36 0.06 250)' },
    warn:    { bg: 'oklch(0.97 0.03 80)',               fg: 'oklch(0.40 0.10 80)' },
  }[tone];
  return (
    <div style={{
      marginTop: 10, padding: '12px 14px', background: tones.bg, color: tones.fg,
      borderRadius: 'var(--r-md)', fontSize: 13, lineHeight: 1.45,
      display: 'flex', alignItems: 'flex-start', gap: 12,
    }}>
      <div style={{ flex: 1 }}>{text}</div>
      {action && (
        <button onClick={onAction} style={{
          flexShrink: 0, height: 32, padding: '0 12px', borderRadius: 999,
          border: '1px solid currentColor', background: 'transparent', color: 'inherit',
          fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: 'inherit',
          display: 'inline-flex', alignItems: 'center', gap: 4,
        }}>{action}<Chevron size={10}/></button>
      )}
    </div>
  );
};

// ─────────────────────────────────────────────────────────────
// blocks
// ─────────────────────────────────────────────────────────────

// Hero: saldo do dia + status
function HeroBalance({ saldo, status }) {
  const color = status === 'deficit' ? 'var(--deficit)'
    : status === 'surplus' ? 'var(--surplus)' : 'var(--maintain)';
  const verbo = status === 'deficit' ? 'Voce esta em deficit'
    : status === 'surplus' ? 'Voce esta acima da meta'
    : 'Dentro da faixa de manutencao';
  return (
    <Card style={{ padding: '22px 22px 20px' }}>
      <Eyebrow>Saldo do dia</Eyebrow>
      <div className="tight num" style={{
        marginTop: 8, fontSize: 64, fontWeight: 600, letterSpacing: '-0.04em',
        lineHeight: 1, color: 'var(--ink-1)',
      }}>
        {HI_BR(saldo)}
        <span style={{ fontSize: 20, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 6 }}>kcal</span>
      </div>
      <div style={{ marginTop: 14, display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
        <StatusPill kind={status}/>
        <span style={{ fontSize: 13, color: 'var(--ink-3)' }}>{verbo}.</span>
      </div>
    </Card>
  );
}

// Hero unavailable: saldo nao calculavel — sem chart, sem dado falso
function HeroUnavailable({ reason }) {
  return (
    <Card style={{ padding: '22px 22px 20px' }}>
      <Eyebrow>Saldo do dia</Eyebrow>
      <div className="tight num" style={{
        marginTop: 8, fontSize: 64, fontWeight: 600, letterSpacing: '-0.04em',
        lineHeight: 1, color: 'var(--ink-4)',
      }}>—<span style={{ fontSize: 20, color: 'var(--ink-4)', fontWeight: 500, marginLeft: 6 }}>kcal</span></div>
      <div style={{ marginTop: 12, fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.5 }}>
        {reason}
      </div>
    </Card>
  );
}

// Meta diaria — sempre visivel (independe de Health Connect)
function MetaCard({ value = 2200, objetivo = 'Emagrecer' }) {
  return (
    <Card>
      <Eyebrow>Meta diaria</Eyebrow>
      <div className="tight num" style={{
        marginTop: 8, fontSize: 30, fontWeight: 600, letterSpacing: '-0.025em',
        color: 'var(--ink-1)', lineHeight: 1,
      }}>
        {value.toLocaleString('pt-BR')}
        <span style={{ fontSize: 13, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 4 }}>kcal</span>
      </div>
      <div style={{ marginTop: 8, fontSize: 12, color: 'var(--ink-3)' }}>
        Objetivo: {objetivo.toLowerCase()}
      </div>
    </Card>
  );
}

// Peso — apenas valor mais recente + data
function PesoCard({ value, when, state = 'content' }) {
  if (state === 'empty') {
    return (
      <Card>
        <Eyebrow>Peso mais recente</Eyebrow>
        <div className="tight num" style={{
          marginTop: 8, fontSize: 30, fontWeight: 600, letterSpacing: '-0.025em',
          color: 'var(--ink-4)', lineHeight: 1,
        }}>—<span style={{ fontSize: 13, color: 'var(--ink-4)', fontWeight: 500, marginLeft: 4 }}>kg</span></div>
        <div style={{ marginTop: 8, fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.4 }}>
          Sem registro de peso<br/>no Health Connect.
        </div>
      </Card>
    );
  }
  if (state === 'permission') {
    return (
      <Card>
        <Eyebrow>Peso mais recente</Eyebrow>
        <div className="tight" style={{
          marginTop: 8, fontSize: 18, fontWeight: 600, color: 'var(--ink-2)', lineHeight: 1.2,
        }}>Permissao necessaria</div>
        <div style={{ marginTop: 6, fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.4 }}>
          Habilitar peso no Health Connect.
        </div>
      </Card>
    );
  }
  if (state === 'unavailable') {
    return (
      <Card>
        <Eyebrow>Peso mais recente</Eyebrow>
        <div className="tight" style={{
          marginTop: 8, fontSize: 18, fontWeight: 600, color: 'var(--ink-2)', lineHeight: 1.2,
        }}>Indisponivel</div>
        <div style={{ marginTop: 6, fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.4 }}>
          Health Connect off-line.
        </div>
      </Card>
    );
  }
  return (
    <Card>
      <Eyebrow>Peso mais recente</Eyebrow>
      <div className="tight num" style={{
        marginTop: 8, fontSize: 30, fontWeight: 600, letterSpacing: '-0.025em',
        color: 'var(--ink-1)', lineHeight: 1,
      }}>
        {value.toString().replace('.', ',')}
        <span style={{ fontSize: 13, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 4 }}>kg</span>
      </div>
      <div style={{ marginTop: 8, fontSize: 12, color: 'var(--ink-3)' }}>
        Medido {when}
      </div>
    </Card>
  );
}

// Ingestao — content / empty / permission / unavailable
function IngestaoCard({ value, state = 'content', onCta }) {
  if (state === 'empty') {
    return (
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <Eyebrow>Ingestao registrada</Eyebrow>
          <span style={{ fontSize: 11, color: 'var(--ink-4)' }}>Health Connect</span>
        </div>
        <div className="tight num" style={{
          marginTop: 8, fontSize: 30, fontWeight: 600, letterSpacing: '-0.025em',
          color: 'var(--ink-4)', lineHeight: 1,
        }}>—<span style={{ fontSize: 13, color: 'var(--ink-4)', fontWeight: 500, marginLeft: 4 }}>kcal</span></div>
        <div style={{ marginTop: 12, fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.5 }}>
          Ainda nao ha ingestao calorica registrada hoje no Health Connect.
        </div>
        <div style={{ marginTop: 6, fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.5 }}>
          Quando esse dado aparecer, calculamos o balanco completo do dia.
        </div>
      </Card>
    );
  }
  if (state === 'permission') {
    return (
      <Card>
        <Eyebrow>Ingestao registrada</Eyebrow>
        <div className="tight" style={{
          marginTop: 10, fontSize: 18, fontWeight: 600, color: 'var(--ink-1)', lineHeight: 1.3,
        }}>Calorias dependem de permissao do Health Connect.</div>
        <InlineNote tone="info" text="Permissao de calorias esta negada ou ausente." action="Ajustar permissoes" onAction={onCta}/>
      </Card>
    );
  }
  if (state === 'unavailable') {
    return (
      <Card>
        <Eyebrow>Ingestao registrada</Eyebrow>
        <div className="tight" style={{
          marginTop: 10, fontSize: 18, fontWeight: 600, color: 'var(--ink-1)', lineHeight: 1.3,
        }}>Indisponivel sem Health Connect.</div>
      </Card>
    );
  }
  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <Eyebrow>Ingestao registrada</Eyebrow>
        <span style={{ fontSize: 11, color: 'var(--ink-4)' }}>Health Connect · hoje</span>
      </div>
      <div className="tight num" style={{
        marginTop: 8, fontSize: 36, fontWeight: 600, letterSpacing: '-0.03em',
        color: 'var(--ink-1)', lineHeight: 1,
      }}>
        {value.toLocaleString('pt-BR')}
        <span style={{ fontSize: 14, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 4 }}>kcal</span>
      </div>
    </Card>
  );
}

// Gasto estimado — BMR + ativas. Estados especiais conforme spec.
function GastoCard({ bmr, ativas, state = 'content', onCta }) {
  // Linha "atomo"
  const Line = ({ label, value, bold, accent }) => (
    <div style={{
      display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
      padding: '10px 0', borderTop: '1px solid var(--hairline)',
    }}>
      <span style={{ fontSize: 13, color: bold ? 'var(--ink-1)' : 'var(--ink-2)', fontWeight: bold ? 600 : 500 }}>{label}</span>
      <span className="num tight" style={{
        fontSize: bold ? 18 : 14, fontWeight: 600, letterSpacing: '-0.01em',
        color: accent || 'var(--ink-1)',
      }}>{value}</span>
    </div>
  );

  if (state === 'permission') {
    return (
      <Card>
        <Eyebrow>Gasto estimado</Eyebrow>
        <div className="tight" style={{
          marginTop: 10, fontSize: 18, fontWeight: 600, color: 'var(--ink-1)', lineHeight: 1.3,
        }}>Calorias ativas dependem de permissao do Health Connect.</div>
        <InlineNote tone="info" text="Sem calorias ativas, nao calculamos o gasto total do dia." action="Ajustar permissoes" onAction={onCta}/>
      </Card>
    );
  }

  if (state === 'unavailable') {
    return (
      <Card>
        <Eyebrow>Gasto estimado</Eyebrow>
        <div className="tight" style={{
          marginTop: 10, fontSize: 18, fontWeight: 600, color: 'var(--ink-1)', lineHeight: 1.3,
        }}>Indisponivel sem Health Connect.</div>
      </Card>
    );
  }

  if (state === 'partial') {
    // BMR sozinho — nunca rotular como gasto total.
    return (
      <Card>
        <Eyebrow>Gasto do dia</Eyebrow>
        <div className="tight num" style={{
          marginTop: 8, fontSize: 36, fontWeight: 600, letterSpacing: '-0.03em',
          color: 'var(--ink-2)', lineHeight: 1,
        }}>
          {bmr.toLocaleString('pt-BR')}
          <span style={{ fontSize: 14, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 4 }}>kcal</span>
        </div>
        <div style={{ marginTop: 6, fontSize: 12, color: 'var(--ink-3)' }}>
          Metabolismo basal estimado. Calorias ativas indisponiveis hoje.
        </div>
      </Card>
    );
  }

  const total = bmr + ativas;
  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <Eyebrow>Gasto estimado</Eyebrow>
        <span style={{ fontSize: 11, color: 'var(--ink-4)' }}>BMR + ativas</span>
      </div>
      <div className="tight num" style={{
        marginTop: 8, fontSize: 36, fontWeight: 600, letterSpacing: '-0.03em',
        color: 'var(--ink-1)', lineHeight: 1,
      }}>
        {total.toLocaleString('pt-BR')}
        <span style={{ fontSize: 14, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 4 }}>kcal</span>
      </div>
      <div style={{ marginTop: 14 }}>
        <Line label="Metabolismo basal estimado" value={bmr.toLocaleString('pt-BR')}/>
        <Line label="Calorias ativas" value={`+${ativas.toLocaleString('pt-BR')}`}/>
      </div>
    </Card>
  );
}

// Banner global — usado em estados que afetam toda a tela (HC indisponivel, erro de leitura)
function GlobalBanner({ tone = 'info', title, body, action, onAction }) {
  const tones = {
    info: { bg: 'oklch(0.97 0.02 250)', fg: 'oklch(0.30 0.06 250)', accent: 'var(--maintain)' },
    warn: { bg: 'oklch(0.97 0.03 80)',  fg: 'oklch(0.36 0.10 80)',  accent: 'oklch(0.62 0.14 80)' },
    err:  { bg: 'oklch(0.97 0.03 35)',  fg: 'oklch(0.36 0.10 35)',  accent: 'var(--surplus)' },
  }[tone];
  return (
    <div style={{
      background: tones.bg, color: tones.fg, borderRadius: 'var(--r-lg)',
      padding: '16px 18px', display: 'flex', alignItems: 'flex-start', gap: 14,
    }}>
      <div style={{
        width: 8, alignSelf: 'stretch', borderRadius: 999, background: tones.accent, flexShrink: 0,
      }}/>
      <div style={{ flex: 1 }}>
        <div className="tight" style={{ fontSize: 15, fontWeight: 600, letterSpacing: '-0.01em' }}>{title}</div>
        {body && <div style={{ marginTop: 4, fontSize: 13, lineHeight: 1.5, opacity: 0.9 }}>{body}</div>}
        {action && (
          <button onClick={onAction} style={{
            marginTop: 12, height: 36, padding: '0 14px', borderRadius: 999,
            border: '1px solid currentColor', background: 'transparent', color: 'inherit',
            fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: 'inherit',
            display: 'inline-flex', alignItems: 'center', gap: 6,
          }}>{action}<Chevron size={11}/></button>
        )}
      </div>
    </div>
  );
}

// Skeleton primitive
const Skel = ({ h, w = '100%', mt = 0, r = 10 }) => (
  <div style={{
    height: h, width: w, marginTop: mt, borderRadius: r,
    background: 'linear-gradient(90deg, var(--bg-sunken) 0%, #ECEBE5 50%, var(--bg-sunken) 100%)',
    backgroundSize: '200% 100%', animation: 'hi-shimmer 1.6s linear infinite',
  }}/>
);

// ─────────────────────────────────────────────────────────────
// state shells
// ─────────────────────────────────────────────────────────────
function Shell({ children }) {
  return (
    <div style={{
      height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column',
    }}>
      <TopBar/>
      <div style={{
        flex: 1, padding: '20px 20px 4px', overflow: 'auto',
        display: 'flex', flexDirection: 'column', gap: 14,
      }}>{children}</div>
      <PrivacyFoot/>
    </div>
  );
}

// 01 · Dashboard completo
function StateComplete() {
  return (
    <Shell>
      <HeroBalance saldo={-470} status="deficit"/>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <MetaCard value={2200} objetivo="Emagrecer"/>
        <PesoCard value="82,4" when="hoje"/>
      </div>
      <IngestaoCard value={1850}/>
      <GastoCard bmr={1700} ativas={620}/>
    </Shell>
  );
}

// 02 · Sem ingestao
function StateNoIntake() {
  return (
    <Shell>
      <HeroUnavailable reason="Sem ingestao registrada hoje, nao calculamos o saldo. Meta e gasto seguem abaixo."/>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <MetaCard value={2200} objetivo="Emagrecer"/>
        <PesoCard value="82,4" when="hoje"/>
      </div>
      <IngestaoCard state="empty"/>
      <GastoCard bmr={1700} ativas={620}/>
    </Shell>
  );
}

// 03 · Permissao parcial (peso ok, calorias negadas)
function StatePartialPerm() {
  return (
    <Shell>
      <HeroUnavailable reason="Calorias dependem de permissao no Health Connect. O peso ja esta acessivel."/>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <MetaCard value={2200} objetivo="Emagrecer"/>
        <PesoCard value="82,4" when="hoje"/>
      </div>
      <IngestaoCard state="permission"/>
      <GastoCard state="permission"/>
    </Shell>
  );
}

// 04 · Health Connect indisponivel
function StateHCUnavailable() {
  return (
    <Shell>
      <GlobalBanner
        tone="warn"
        title="Health Connect indisponivel"
        body="Nao conseguimos ler calorias, ingestao e peso agora. Sua meta local segue ativa."
        action="Abrir Health Connect"/>
      <HeroUnavailable reason="Saldo depende dos dados do Health Connect."/>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <MetaCard value={2200} objetivo="Emagrecer"/>
        <PesoCard state="unavailable"/>
      </div>
      <IngestaoCard state="unavailable"/>
      <GastoCard state="unavailable"/>
    </Shell>
  );
}

// 05 · Sem peso registrado
function StateNoWeight() {
  return (
    <Shell>
      <HeroBalance saldo={-470} status="deficit"/>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <MetaCard value={2200} objetivo="Emagrecer"/>
        <PesoCard state="empty"/>
      </div>
      <IngestaoCard value={1850}/>
      <GastoCard bmr={1700} ativas={620}/>
    </Shell>
  );
}

// 06 · Erro inesperado
function StateError() {
  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <TopBar/>
      <div style={{
        flex: 1, padding: '20px 24px', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', textAlign: 'center', gap: 16,
      }}>
        <div style={{
          width: 56, height: 56, borderRadius: 999,
          background: 'oklch(0.95 0.04 35)', color: 'var(--surplus)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <circle cx="11" cy="11" r="9" stroke="currentColor" strokeWidth="1.6"/>
            <path d="M11 7v5M11 14.5v.6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
          </svg>
        </div>
        <div className="tight" style={{
          fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em', color: 'var(--ink-1)',
          maxWidth: 300, lineHeight: 1.2,
        }}>Algo nao saiu como esperado.</div>
        <div style={{ fontSize: 14, color: 'var(--ink-2)', maxWidth: 280, lineHeight: 1.5 }}>
          Nao conseguimos carregar seus dados de hoje. Tente novamente em instantes.
        </div>
        <button className="btn-primary" style={{ marginTop: 8, maxWidth: 280 }}>
          Tentar novamente
        </button>
      </div>
      <PrivacyFoot/>
    </div>
  );
}

// 07 · Loading (skeleton)
function StateLoading() {
  return (
    <Shell>
      <Card>
        <Skel h={11} w="34%" r={6}/>
        <Skel h={48} w="58%" mt={14} r={8}/>
        <Skel h={28} w="44%" mt={16} r={999}/>
      </Card>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <Card><Skel h={11} w="60%" r={6}/><Skel h={26} w="80%" mt={12}/><Skel h={11} w="60%" mt={10}/></Card>
        <Card><Skel h={11} w="60%" r={6}/><Skel h={26} w="80%" mt={12}/><Skel h={11} w="60%" mt={10}/></Card>
      </div>
      <Card><Skel h={11} w="40%" r={6}/><Skel h={32} w="48%" mt={12}/></Card>
      <Card>
        <Skel h={11} w="40%" r={6}/>
        <Skel h={32} w="48%" mt={12}/>
        <Skel h={1} mt={16} r={0}/>
        <Skel h={14} w="80%" mt={12}/>
        <Skel h={14} w="60%" mt={8}/>
      </Card>
    </Shell>
  );
}

// 08 · Estado local invalido — recuperacao real e refazer onboarding
function StateLocalInvalid() {
  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <TopBar/>
      <div style={{
        flex: 1, padding: '20px 24px', display: 'flex', flexDirection: 'column',
        justifyContent: 'center', gap: 18,
      }}>
        <div style={{ width: 40, height: 40, borderRadius: 12,
          background: 'var(--bg-sunken)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M3 7.5h10M3 12.5h7M14 12.5l3 3M14 12.5l3-3" stroke="var(--ink-2)" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </div>
        <h2 className="tight" style={{
          margin: 0, fontSize: 24, fontWeight: 600, letterSpacing: '-0.02em',
          lineHeight: 1.2, color: 'var(--ink-1)', maxWidth: 320,
        }}>Precisamos refazer sua configuracao local para continuar.</h2>
        <p style={{
          margin: 0, fontSize: 14, lineHeight: 1.55, color: 'var(--ink-2)', maxWidth: 320,
        }}>Sua meta, perfil e consentimentos serao criados de novo. Nenhum dado sai do aparelho.</p>
        <div style={{
          padding: '12px 14px', background: 'var(--bg-sunken)', borderRadius: 'var(--r-md)',
          fontSize: 12, color: 'var(--ink-2)', display: 'flex', alignItems: 'center', gap: 8,
        }}>
          <ShieldGlyph color="var(--ink-2)"/>
          <span>Dados ficam no aparelho. Sem conta, sem nuvem.</span>
        </div>
      </div>
      <div style={{ padding: '12px 24px 28px' }}>
        <button className="btn-primary">Refazer configuracao</button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// dispatcher + global keyframes injection
// ─────────────────────────────────────────────────────────────
function DashboardMVP({ state = 'complete' }) {
  React.useEffect(() => {
    if (document.getElementById('hi-keyframes')) return;
    const s = document.createElement('style');
    s.id = 'hi-keyframes';
    s.textContent = `@keyframes hi-shimmer { 0% { background-position: -100% 0; } 100% { background-position: 200% 0; } }`;
    document.head.appendChild(s);
  }, []);
  switch (state) {
    case 'complete':       return <StateComplete/>;
    case 'no-intake':      return <StateNoIntake/>;
    case 'partial-perm':   return <StatePartialPerm/>;
    case 'hc-unavailable': return <StateHCUnavailable/>;
    case 'no-weight':      return <StateNoWeight/>;
    case 'error':          return <StateError/>;
    case 'loading':        return <StateLoading/>;
    case 'local-invalid':  return <StateLocalInvalid/>;
    default:               return <StateComplete/>;
  }
}

window.DashboardMVP = DashboardMVP;

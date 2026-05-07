/* eslint-disable no-undef */
// Health Insights — Onboarding completo + Dashboard + Settings
// Variante A (Apple Health minimal). Token system: tokens.css.

const fmtKcal = n => {
  const s = Math.abs(n).toLocaleString('pt-BR');
  return n < 0 ? `−${s}` : n > 0 ? `+${s}` : s;
};

// ─────────────────────────────────────────────────────────────
// Reusable bits (local to this file)
// ─────────────────────────────────────────────────────────────
const Header = ({ back, step, total, right }) => (
  <div style={{ padding: '20px 16px 12px', display: 'flex', alignItems: 'center', gap: 8 }}>
    {back ? (
      <button className="btn-ghost" style={{ padding: 0, height: 36, width: 36, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <svg width="18" height="18" viewBox="0 0 18 18"><path d="M11 4l-5 5 5 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" fill="none"/></svg>
      </button>
    ) : <div style={{ width: 36 }}/>}
    <div style={{ flex: 1, display: 'flex', justifyContent: 'center', gap: 6 }}>
      {step && Array.from({ length: total }).map((_, i) => (
        <span key={i} style={{
          width: i + 1 === step ? 18 : 6, height: 6, borderRadius: 999,
          background: i < step ? 'var(--ink-1)' : 'var(--hairline-strong)',
          transition: 'width .2s',
        }}/>
      ))}
    </div>
    <div style={{ width: 36, display: 'flex', justifyContent: 'flex-end' }}>{right}</div>
  </div>
);

const Field = ({ label, value, suffix, hint, error, focused }) => (
  <div style={{ marginBottom: 18 }}>
    <label style={{ display: 'block', fontSize: 12, fontWeight: 500, color: 'var(--ink-3)',
      letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: 8 }}>{label}</label>
    <div style={{
      display: 'flex', alignItems: 'baseline', gap: 6,
      background: 'var(--bg-elev)',
      border: `1px solid ${error ? 'var(--surplus)' : focused ? 'var(--ink-1)' : 'var(--hairline)'}`,
      borderRadius: 'var(--r-md)', padding: '14px 16px',
    }}>
      <span className="tight num" style={{
        fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em',
        color: value ? 'var(--ink-1)' : 'var(--ink-4)', flex: 1,
      }}>{value || '—'}</span>
      {suffix && <span style={{ fontSize: 14, color: 'var(--ink-3)', fontWeight: 500 }}>{suffix}</span>}
    </div>
    {(hint || error) && (
      <div style={{ marginTop: 6, fontSize: 12, color: error ? 'var(--surplus)' : 'var(--ink-3)' }}>
        {error || hint}
      </div>
    )}
  </div>
);

const Segmented = ({ options, value, onChange }) => (
  <div style={{
    display: 'flex', background: 'var(--bg-sunken)', padding: 4,
    borderRadius: 'var(--r-md)', gap: 2,
  }}>
    {options.map(opt => (
      <button key={opt.value} onClick={() => onChange?.(opt.value)} style={{
        flex: 1, height: 44, border: 0, cursor: 'pointer',
        borderRadius: 12, fontSize: 14, fontWeight: 500,
        background: value === opt.value ? 'var(--bg-elev)' : 'transparent',
        color: value === opt.value ? 'var(--ink-1)' : 'var(--ink-3)',
        boxShadow: value === opt.value ? '0 1px 3px rgba(0,0,0,0.08)' : 'none',
        fontFamily: 'inherit',
      }}>{opt.label}</button>
    ))}
  </div>
);

// ─────────────────────────────────────────────────────────────
// Tela 1 — PROFILE (T2)
// ─────────────────────────────────────────────────────────────
function ProfileScreen() {
  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <Header back step={2} total={5} />
      <div style={{ flex: 1, padding: '8px 24px 24px', overflow: 'auto' }}>
        <h1 className="tight" style={{
          margin: 0, fontSize: 26, fontWeight: 600, letterSpacing: '-0.02em', lineHeight: 1.15,
        }}>Vamos calcular seu metabolismo basal.</h1>
        <p style={{ margin: '8px 0 24px', fontSize: 14, color: 'var(--ink-2)', lineHeight: 1.5 }}>
          Esses dados ficam no seu aparelho e podem ser editados depois.
        </p>

        <label style={{ display: 'block', fontSize: 12, fontWeight: 500, color: 'var(--ink-3)',
          letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: 8 }}>Sexo biológico</label>
        <Segmented
          value="m"
          options={[{ value: 'm', label: 'Masculino' }, { value: 'f', label: 'Feminino' }]}
        />
        <div style={{ marginTop: 6, marginBottom: 22, fontSize: 12, color: 'var(--ink-3)' }}>
          Usado apenas na fórmula de Mifflin-St Jeor.
        </div>

        <Field label="Peso atual" value="78,4" suffix="kg" focused />
        <Field label="Altura" value="178" suffix="cm" />
        <Field label="Idade" value="32" suffix="anos" />

        <a href="#" style={{ display: 'inline-block', marginTop: 8,
          fontSize: 13, color: 'var(--brand-ink)', fontWeight: 500 }}>
          Como tratamos esses dados →
        </a>
      </div>
      <div style={{ padding: '12px 24px 28px', display: 'flex', flexDirection: 'column', gap: 12,
        borderTop: '1px solid var(--hairline)', background: 'var(--bg)' }}>
        <button className="btn-primary">Continuar</button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Tela 2 — GOAL (T3) — com preview reativo
// ─────────────────────────────────────────────────────────────
function GoalScreen() {
  const [goal, setGoal] = React.useState('lose');
  const BASE = 2680; // exemplo: gasto médio
  const targets = { lose: BASE - 500, maintain: BASE, gain: BASE + 300 };
  const target = targets[goal];

  const Card = ({ id, title, desc, delta }) => {
    const sel = goal === id;
    return (
      <button onClick={() => setGoal(id)} style={{
        width: '100%', padding: '16px 18px', textAlign: 'left',
        background: 'var(--bg-elev)',
        border: `1.5px solid ${sel ? 'var(--ink-1)' : 'var(--hairline)'}`,
        borderRadius: 'var(--r-lg)', cursor: 'pointer',
        display: 'flex', alignItems: 'center', gap: 14, fontFamily: 'inherit',
      }}>
        <div style={{
          width: 22, height: 22, borderRadius: '50%', flexShrink: 0,
          border: `2px solid ${sel ? 'var(--ink-1)' : 'var(--hairline-strong)'}`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          {sel && <div style={{ width: 10, height: 10, borderRadius: '50%', background: 'var(--ink-1)' }}/>}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span style={{ fontSize: 15, fontWeight: 600, color: 'var(--ink-1)' }}>{title}</span>
            <span className="num tight" style={{ fontSize: 13, fontWeight: 600,
              color: delta < 0 ? 'var(--deficit)' : delta > 0 ? 'var(--surplus)' : 'var(--ink-3)' }}>
              {delta === 0 ? '±0' : (delta > 0 ? '+' : '−') + Math.abs(delta)} kcal/dia
            </span>
          </div>
          <div style={{ fontSize: 13, color: 'var(--ink-2)', marginTop: 4, lineHeight: 1.4 }}>{desc}</div>
        </div>
      </button>
    );
  };

  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <Header back step={3} total={5} />
      <div style={{ flex: 1, padding: '8px 24px 24px', overflow: 'auto' }}>
        <h1 className="tight" style={{
          margin: 0, fontSize: 26, fontWeight: 600, letterSpacing: '-0.02em', lineHeight: 1.15,
        }}>Qual é o seu objetivo?</h1>
        <p style={{ margin: '8px 0 20px', fontSize: 14, color: 'var(--ink-2)' }}>
          Você pode mudar quando quiser.
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Card id="lose" title="Perder peso" desc="≈ 0,5 kg por semana" delta={-500}/>
          <Card id="maintain" title="Manter peso" desc="Ingestão alinhada ao gasto" delta={0}/>
          <Card id="gain" title="Ganhar peso" desc="≈ 0,3 kg por semana" delta={+300}/>
        </div>

        {/* Preview */}
        <div style={{ marginTop: 24, padding: '18px 20px', background: 'var(--bg-elev)',
          border: '1px solid var(--hairline)', borderRadius: 'var(--r-lg)' }}>
          <div style={{ fontSize: 12, color: 'var(--ink-3)', fontWeight: 500,
            textTransform: 'uppercase', letterSpacing: '0.06em' }}>Sua meta diária</div>
          <div className="tight num" style={{ fontSize: 36, fontWeight: 600,
            letterSpacing: '-0.025em', marginTop: 6 }}>
            {target.toLocaleString('pt-BR')} <span style={{ fontSize: 16, color: 'var(--ink-3)', fontWeight: 500 }}>kcal/dia</span>
          </div>
          <div style={{ fontSize: 12, color: 'var(--ink-3)', marginTop: 4 }}>
            Baseado em gasto médio de {BASE.toLocaleString('pt-BR')} kcal.
          </div>
        </div>

        <div style={{ marginTop: 16, padding: '10px 14px', background: 'var(--bg-sunken)',
          borderRadius: 'var(--r-md)', fontSize: 12, color: 'var(--ink-2)', lineHeight: 1.5 }}>
          Acompanhamento calórico. Não substitui orientação de nutricionista.
        </div>
      </div>
      <div style={{ padding: '12px 24px 28px', borderTop: '1px solid var(--hairline)' }}>
        <button className="btn-primary">Continuar</button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Tela 3 — CONNECTING (T5) — 4 estados
// ─────────────────────────────────────────────────────────────
function ConnectingScreen({ state = 'loading' }) {
  const StatusBlock = () => {
    if (state === 'loading') {
      return (
        <>
          <div style={{ width: 56, height: 56, borderRadius: '50%',
            border: '3px solid var(--hairline)', borderTopColor: 'var(--ink-1)',
            animation: 'spin 1s linear infinite' }}/>
          <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
          <div className="tight" style={{ fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em', marginTop: 24 }}>
            Buscando seus dados…
          </div>
          <div style={{ fontSize: 14, color: 'var(--ink-2)', marginTop: 8, textAlign: 'center' }}>
            Lendo gasto calórico dos últimos 15 dias.
          </div>
        </>
      );
    }
    if (state === 'not-installed') {
      return (
        <>
          <div style={{ width: 64, height: 64, borderRadius: 16,
            background: 'var(--bg-sunken)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 2v4m0 12v4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83M2 12h4m12 0h4M4.93 19.07l2.83-2.83m8.48-8.48l2.83-2.83"
                stroke="var(--ink-3)" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </div>
          <div className="tight" style={{ fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em', marginTop: 24, textAlign: 'center' }}>
            Health Connect não está instalado.
          </div>
          <div style={{ fontSize: 14, color: 'var(--ink-2)', marginTop: 8, textAlign: 'center', maxWidth: 280 }}>
            O Health Connect é a ponte oficial Android para os dados do Samsung Health.
          </div>
        </>
      );
    }
    if (state === 'partial') {
      const items = [
        { label: 'Calorias', ok: true },
        { label: 'Peso', ok: true },
        { label: 'Treinos', ok: false },
      ];
      return (
        <>
          <div className="tight" style={{ fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em', textAlign: 'center' }}>
            Acesso parcial concedido.
          </div>
          <div style={{ fontSize: 14, color: 'var(--ink-2)', marginTop: 8, textAlign: 'center', maxWidth: 280, marginBottom: 24 }}>
            Você pode continuar — alguns insights ficam limitados.
          </div>
          <div style={{ width: '100%', background: 'var(--bg-elev)',
            border: '1px solid var(--hairline)', borderRadius: 'var(--r-lg)', overflow: 'hidden' }}>
            {items.map((it, i) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12,
                padding: '14px 18px',
                borderBottom: i < items.length - 1 ? '1px solid var(--hairline)' : 'none' }}>
                <div style={{
                  width: 24, height: 24, borderRadius: '50%',
                  background: it.ok ? 'var(--brand-tint)' : 'var(--bg-sunken)',
                  color: it.ok ? 'var(--brand-ink)' : 'var(--ink-3)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 600,
                }}>{it.ok ? '✓' : '—'}</div>
                <span style={{ flex: 1, fontSize: 14, fontWeight: 500 }}>{it.label}</span>
                <span style={{ fontSize: 12, color: 'var(--ink-3)' }}>{it.ok ? 'Concedido' : 'Negado'}</span>
              </div>
            ))}
          </div>
        </>
      );
    }
  };

  const ctaCopy = {
    loading: null,
    'not-installed': 'Abrir Play Store',
    partial: 'Continuar mesmo assim',
  }[state];

  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <Header step={4} total={5} />
      <div style={{ flex: 1, padding: '24px 24px', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center' }}>
        <StatusBlock />
      </div>
      {ctaCopy && (
        <div style={{ padding: '12px 24px 28px', display: 'flex', flexDirection: 'column', gap: 10 }}>
          <button className="btn-primary">{ctaCopy}</button>
          <button className="btn-ghost" style={{ height: 44 }}>Pular por agora</button>
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Tela 4 — DASHBOARD HOJE (T6) — 4 estados
// ─────────────────────────────────────────────────────────────
function DashboardHero({ value, label }) {
  const color = value < 0 ? 'var(--deficit)' : value > 0 ? 'var(--surplus)' : 'var(--maintain)';
  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--ink-3)', fontSize: 13 }}>
        <span className="dot" style={{ color }}/>
        <span>Hoje · ter, 6 mai</span>
      </div>
      <div className="tight num" style={{
        fontSize: 64, fontWeight: 600, lineHeight: 1, letterSpacing: '-0.04em', marginTop: 10,
      }}>
        {fmtKcal(value)}
        <span style={{ fontSize: 20, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 6 }}>kcal</span>
      </div>
      <div style={{ marginTop: 6, fontSize: 14, color: 'var(--ink-2)' }}>
        Você está em <b style={{ color }}>{label}</b> hoje.
      </div>
    </>
  );
}

function DashboardChart({ data, todayIdx = 6 }) {
  const max = Math.max(...data.map(d => Math.abs(d)));
  return (
    <div className="card" style={{ padding: 18, marginTop: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <span style={{ fontSize: 13, fontWeight: 600 }}>Saldo · 7 dias</span>
        <span style={{ fontSize: 12, color: 'var(--ink-3)' }} className="num">
          média −312 kcal
        </span>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, height: 90, marginTop: 14, position: 'relative' }}>
        {/* zero line */}
        <div style={{ position: 'absolute', left: 0, right: 0, top: '50%',
          borderTop: '1px dashed var(--hairline-strong)' }}/>
        {data.map((v, i) => {
          const h = (Math.abs(v) / max) * 38;
          const isToday = i === todayIdx;
          const isDeficit = v < 0;
          return (
            <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column',
              alignItems: 'center', height: '100%', justifyContent: 'center', gap: 0 }}>
              <div style={{ height: 45, display: 'flex', alignItems: 'flex-end', width: '100%' }}>
                {!isDeficit && <div style={{ width: '100%', height: `${h}px`,
                  background: isToday ? 'var(--surplus)' : 'oklch(0.88 0.06 35)',
                  borderRadius: '4px 4px 0 0' }}/>}
              </div>
              <div style={{ height: 45, display: 'flex', alignItems: 'flex-start', width: '100%' }}>
                {isDeficit && <div style={{ width: '100%', height: `${h}px`,
                  background: isToday ? 'var(--deficit)' : 'var(--brand-tint-2)',
                  borderRadius: '0 0 4px 4px' }}/>}
              </div>
            </div>
          );
        })}
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 6,
        fontSize: 11, color: 'var(--ink-4)', fontFamily: 'var(--font-mono)' }}>
        <span>qua</span><span>qui</span><span>sex</span><span>sáb</span><span>dom</span><span>seg</span><span>ter</span>
      </div>
    </div>
  );
}

function DashboardBreakdown() {
  const Row = ({ label, value, sub, accent }) => (
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
      padding: '14px 0', borderBottom: '1px solid var(--hairline)' }}>
      <div>
        <div style={{ fontSize: 14, fontWeight: 500 }}>{label}</div>
        {sub && <div style={{ fontSize: 12, color: 'var(--ink-3)', marginTop: 2 }}>{sub}</div>}
      </div>
      <div className="num tight" style={{ fontSize: 18, fontWeight: 600,
        color: accent || 'var(--ink-1)', letterSpacing: '-0.01em' }}>{value}</div>
    </div>
  );
  return (
    <div className="card" style={{ padding: '4px 18px 16px', marginTop: 12 }}>
      <div style={{ padding: '14px 0 4px', fontSize: 13, fontWeight: 600 }}>Como chegamos lá</div>
      <Row label="Metabolismo basal" value="1 480" sub="Mifflin-St Jeor"/>
      <Row label="Gasto ativo" value="+592" sub="Health Connect · hoje"/>
      <Row label="Ingestão" value="−1 590" sub="MyFitnessPal · hoje"/>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline',
        padding: '14px 0 4px' }}>
        <div style={{ fontSize: 14, fontWeight: 600 }}>Saldo</div>
        <div className="num tight" style={{ fontSize: 22, fontWeight: 600,
          color: 'var(--deficit)', letterSpacing: '-0.02em' }}>−482</div>
      </div>
    </div>
  );
}

function DashboardScreen({ state = 'content' }) {
  const HeaderBar = () => (
    <div style={{ padding: '16px 20px 8px', display: 'flex', alignItems: 'center' }}>
      <Wordmark/>
      <div style={{ flex: 1 }}/>
      <button className="btn-ghost" style={{ height: 36, width: 36, padding: 0,
        display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <circle cx="10" cy="4.5" r="1.4" fill="currentColor"/>
          <circle cx="10" cy="10" r="1.4" fill="currentColor"/>
          <circle cx="10" cy="15.5" r="1.4" fill="currentColor"/>
        </svg>
      </button>
    </div>
  );

  if (state === 'loading') {
    const Sk = ({ h, w = '100%', mt = 0, r = 8 }) => (
      <div style={{ height: h, width: w, marginTop: mt, borderRadius: r,
        background: 'linear-gradient(90deg, var(--bg-sunken) 0%, #ECEBE5 50%, var(--bg-sunken) 100%)',
        backgroundSize: '200% 100%', animation: 'shimmer 1.6s infinite' }}/>
    );
    return (
      <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
        <style>{`@keyframes shimmer { 0% { background-position: -100% 0; } 100% { background-position: 200% 0; } }`}</style>
        <HeaderBar/>
        <div style={{ padding: '16px 24px', flex: 1 }}>
          <Sk h={14} w="40%"/>
          <Sk h={56} w="60%" mt={14}/>
          <Sk h={16} w="50%" mt={10}/>
          <Sk h={140} mt={20} r={22}/>
          <Sk h={220} mt={12} r={22}/>
        </div>
      </div>
    );
  }

  if (state === 'empty') {
    return (
      <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
        <HeaderBar/>
        <div style={{ padding: '16px 24px', flex: 1, overflow: 'auto' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--ink-3)', fontSize: 13 }}>
            <span className="dot"/>
            <span>Hoje · ter, 6 mai</span>
          </div>
          <div className="tight num" style={{
            fontSize: 56, fontWeight: 600, lineHeight: 1, letterSpacing: '-0.04em', marginTop: 10,
            color: 'var(--ink-3)',
          }}>−– <span style={{ fontSize: 20, fontWeight: 500 }}>kcal</span></div>
          <div style={{ marginTop: 8, fontSize: 14, color: 'var(--ink-2)', maxWidth: 300 }}>
            Falta ligar um app de food log para fechar a conta do dia.
          </div>

          <div style={{ marginTop: 20, padding: 18, background: 'var(--bg-elev)',
            border: '1px solid var(--hairline)', borderRadius: 'var(--r-lg)' }}>
            <div style={{ fontSize: 13, fontWeight: 600 }}>Conecte sua ingestão</div>
            <div style={{ fontSize: 13, color: 'var(--ink-2)', marginTop: 4, lineHeight: 1.5 }}>
              O Health Insights lê de qualquer app que sincroniza com o Samsung Health.
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', marginTop: 14, gap: 0 }}>
              {['MyFitnessPal', 'FatSecret', 'Outro app de nutrição'].map((n, i) => (
                <div key={n} style={{ display: 'flex', alignItems: 'center', gap: 12,
                  padding: '12px 0',
                  borderTop: i === 0 ? '1px solid var(--hairline)' : 'none',
                  borderBottom: '1px solid var(--hairline)' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 8, background: 'var(--bg-sunken)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 11, fontFamily: 'var(--font-mono)', color: 'var(--ink-3)' }}>{n[0]}</div>
                  <span style={{ flex: 1, fontSize: 14, fontWeight: 500 }}>{n}</span>
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M5 3l4 4-4 4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </div>
              ))}
            </div>
          </div>

          {/* Today's spending — ainda mostra o que tem */}
          <div style={{ marginTop: 16, padding: 18, background: 'var(--bg-elev)',
            border: '1px solid var(--hairline)', borderRadius: 'var(--r-lg)' }}>
            <div style={{ fontSize: 12, color: 'var(--ink-3)', textTransform: 'uppercase',
              letterSpacing: '0.06em', fontWeight: 500 }}>Já temos hoje</div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8 }}>
              <span style={{ fontSize: 14, color: 'var(--ink-2)' }}>Gasto total</span>
              <span className="num tight" style={{ fontSize: 16, fontWeight: 600 }}>2 072 kcal</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 6 }}>
              <span style={{ fontSize: 14, color: 'var(--ink-2)' }}>Meta diária</span>
              <span className="num tight" style={{ fontSize: 16, fontWeight: 600 }}>2 180 kcal</span>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (state === 'error') {
    return (
      <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
        <HeaderBar/>
        <div style={{ padding: '16px 24px', flex: 1, display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
          <div style={{ width: 56, height: 56, borderRadius: '50%',
            background: 'oklch(0.96 0.04 35)', color: 'var(--surplus)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 24, fontWeight: 700 }}>!</div>
          <div className="tight" style={{ fontSize: 22, fontWeight: 600, marginTop: 20,
            letterSpacing: '-0.02em' }}>Permissão revogada.</div>
          <div style={{ fontSize: 14, color: 'var(--ink-2)', marginTop: 8, maxWidth: 280, lineHeight: 1.5 }}>
            O Health Insights perdeu acesso ao Health Connect. Reconecte em Configurações para ver seus dados.
          </div>
          <button className="btn-primary" style={{ marginTop: 24, maxWidth: 260 }}>
            Reconectar agora
          </button>
        </div>
      </div>
    );
  }

  // CONTENT
  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <HeaderBar/>
      <div style={{ padding: '8px 24px 24px', flex: 1, overflow: 'auto' }}>
        <DashboardHero value={-482} label="déficit"/>
        <DashboardChart data={[-280, -410, -120, +180, -560, -390, -482]} todayIdx={6}/>
        <DashboardBreakdown/>
        <div style={{ marginTop: 12, padding: '10px 14px', background: 'var(--bg-sunken)',
          borderRadius: 'var(--r-md)', fontSize: 12, color: 'var(--ink-2)', lineHeight: 1.5 }}>
          Acompanhamento calórico. Não substitui orientação de nutricionista.
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Tela 5 — SETTINGS
// ─────────────────────────────────────────────────────────────
function SettingsScreen() {
  const Section = ({ title, children }) => (
    <div style={{ marginTop: 24 }}>
      <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--ink-3)',
        letterSpacing: '0.08em', textTransform: 'uppercase',
        padding: '0 24px 8px' }}>{title}</div>
      <div style={{ background: 'var(--bg-elev)', borderTop: '1px solid var(--hairline)',
        borderBottom: '1px solid var(--hairline)' }}>
        {children}
      </div>
    </div>
  );
  const Row = ({ label, value, danger, last }) => (
    <div style={{ padding: '14px 24px', display: 'flex', alignItems: 'center', gap: 12,
      borderBottom: last ? 'none' : '1px solid var(--hairline)' }}>
      <span style={{ flex: 1, fontSize: 15, fontWeight: 500,
        color: danger ? 'var(--surplus)' : 'var(--ink-1)' }}>{label}</span>
      {value && <span className="num" style={{ fontSize: 13, color: 'var(--ink-3)' }}>{value}</span>}
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
        <path d="M5 3l4 4-4 4" stroke={danger ? 'var(--surplus)' : 'var(--ink-3)'}
          strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    </div>
  );

  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px 24px 12px', display: 'flex', alignItems: 'center', gap: 12 }}>
        <button className="btn-ghost" style={{ padding: 0, height: 36, width: 36, display: 'flex',
          alignItems: 'center', justifyContent: 'center' }}>
          <svg width="18" height="18" viewBox="0 0 18 18"><path d="M11 4l-5 5 5 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" fill="none"/></svg>
        </button>
        <h1 className="tight" style={{ margin: 0, fontSize: 22, fontWeight: 600,
          letterSpacing: '-0.02em' }}>Configurações</h1>
      </div>

      <div style={{ flex: 1, overflow: 'auto', paddingBottom: 32 }}>
        <Section title="Perfil">
          <Row label="Peso, altura, idade" value="78,4 kg · 178 cm · 32 a"/>
          <Row label="Objetivo" value="Perder peso" last/>
        </Section>

        <Section title="Conexões">
          <Row label="Permissões Health Connect" value="2 de 3"/>
          <Row label="Gerenciar no Health Connect" last/>
        </Section>

        <Section title="Privacidade">
          <Row label="Exportar meus dados" value="JSON"/>
          <Row label="Política de privacidade"/>
          <Row label="Apagar todos os meus dados" danger last/>
        </Section>

        <Section title="Sobre">
          <Row label="Versão" value="1.0.0 (build 142)"/>
          <Row label="Open source · licenças" last/>
        </Section>

        <div style={{ padding: '24px 24px 0', fontSize: 12, color: 'var(--ink-3)',
          textAlign: 'center', lineHeight: 1.6 }}>
          Health Insights · Made in Brazil 🇧🇷<br/>
          Os dados que aparecem aqui nunca saem do seu aparelho.
        </div>
      </div>
    </div>
  );
}

window.ProfileScreen = ProfileScreen;
window.GoalScreen = GoalScreen;
window.ConnectingScreen = ConnectingScreen;
window.DashboardScreen = DashboardScreen;
window.SettingsScreen = SettingsScreen;

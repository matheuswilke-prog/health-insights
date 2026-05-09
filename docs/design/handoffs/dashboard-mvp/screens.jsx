/* eslint-disable no-undef */
// Health Insights — Welcome + Health Connect screens
// All variants render inside <AndroidDevice> at 412×892.

const SCREEN_W = 412;
const SCREEN_H = 892;

// ─────────────────────────────────────────────────────────────
// Shared atoms
// ─────────────────────────────────────────────────────────────
const Logo = ({ size = 28, color = 'var(--ink-1)' }) => (
  <svg width={size} height={size} viewBox="0 0 28 28" fill="none">
    <path d="M3 18 L9 12 L13 16 L19 8 L25 14" stroke={color} strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"/>
    <circle cx="19" cy="8" r="2" fill={color}/>
  </svg>
);

const Wordmark = ({ tone = 'dark' }) => (
  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
    <Logo color={tone === 'dark' ? 'var(--ink-1)' : '#fff'} />
    <span className="tight" style={{
      fontWeight: 600, fontSize: 16, letterSpacing: '-0.01em',
      color: tone === 'dark' ? 'var(--ink-1)' : '#fff',
    }}>Health Insights</span>
  </div>
);

const PrivacyNote = ({ children = 'Seus dados ficam no aparelho. Sem nuvem, sem conta.' }) => (
  <div style={{
    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
    fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.4,
  }}>
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
      <path d="M7 1.5L2.5 3.5v3.2c0 2.8 1.9 5.4 4.5 5.8 2.6-.4 4.5-3 4.5-5.8V3.5L7 1.5z" stroke="currentColor" strokeWidth="1.2" fill="none"/>
    </svg>
    <span>{children}</span>
  </div>
);

// Dotted-stripe placeholder (used when an illustration would be slop)
const Stripes = ({ children, h = 120, label }) => (
  <div style={{
    height: h, borderRadius: 'var(--r-md)',
    background: 'repeating-linear-gradient(135deg, var(--bg-sunken) 0 8px, transparent 8px 16px)',
    border: '1px dashed var(--hairline-strong)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--ink-4)',
  }}>{label || children}</div>
);

// ─────────────────────────────────────────────────────────────
// VARIANT A — Welcome: Apple Health minimal, hero metric preview
// ─────────────────────────────────────────────────────────────
function WelcomeA({ onContinue }) {
  return (
    <div style={{
      height: '100%', background: 'var(--bg)',
      display: 'flex', flexDirection: 'column', padding: '24px 24px 32px',
    }}>
      <div style={{ paddingTop: 12 }}><Wordmark /></div>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 28 }}>
        {/* Hero data preview card */}
        <div className="card" style={{ padding: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--ink-3)', fontSize: 13 }}>
            <span className="dot" style={{ color: 'var(--deficit)' }} />
            <span>Hoje · prévia</span>
          </div>
          <div className="tight num" style={{
            fontSize: 64, fontWeight: 600, lineHeight: 1, marginTop: 12,
            letterSpacing: '-0.04em', color: 'var(--ink-1)',
          }}>−482<span style={{ fontSize: 20, color: 'var(--ink-3)', fontWeight: 500, marginLeft: 6 }}>kcal</span></div>
          <div style={{ marginTop: 8, fontSize: 14, color: 'var(--ink-2)' }}>
            Você está em <b style={{ color: 'var(--deficit)' }}>déficit</b> hoje.
          </div>

          {/* mini bar chart 7 days */}
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 6, height: 56, marginTop: 20 }}>
            {[0.4, 0.7, 0.55, 0.85, 0.5, 0.9, 0.65].map((h, i) => (
              <div key={i} style={{
                flex: 1, height: `${h * 100}%`,
                background: i === 6 ? 'var(--deficit)' : 'var(--brand-tint-2)',
                borderRadius: 4,
              }} />
            ))}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8, fontSize: 11, color: 'var(--ink-4)' }}>
            <span>S</span><span>T</span><span>Q</span><span>Q</span><span>S</span><span>S</span><span>D</span>
          </div>
        </div>

        <div>
          <h1 className="tight" style={{
            margin: 0, fontSize: 34, fontWeight: 600, letterSpacing: '-0.025em',
            lineHeight: 1.08, color: 'var(--ink-1)',
          }}>Seu balanço calórico,<br />sem planilha.</h1>
          <p style={{
            margin: '12px 0 0', fontSize: 15, lineHeight: 1.5,
            color: 'var(--ink-2)', maxWidth: 320,
          }}>Lemos o Samsung Health e mostramos, em números, se você está perdendo, mantendo ou ganhando peso.</p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <button className="btn-primary" onClick={onContinue}>Começar</button>
        <PrivacyNote />
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// VARIANT B — Welcome: Editorial. Big number + serif-feel display
// ─────────────────────────────────────────────────────────────
function WelcomeB({ onContinue }) {
  return (
    <div style={{
      height: '100%', background: 'var(--bg-elev)',
      display: 'flex', flexDirection: 'column',
    }}>
      <div style={{ padding: '20px 24px 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Wordmark />
        <span className="chip">v1 · MVP</span>
      </div>

      <div style={{ flex: 1, padding: '40px 24px 0', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div style={{ fontSize: 12, fontWeight: 500, letterSpacing: '0.12em',
          textTransform: 'uppercase', color: 'var(--brand-ink)' }}>O ponto</div>

        <h1 className="tight" style={{
          margin: '16px 0 0', fontSize: 44, fontWeight: 700, lineHeight: 0.98,
          letterSpacing: '-0.035em', color: 'var(--ink-1)',
        }}>Você está,<br />de verdade,<br />em <span style={{ color: 'var(--deficit)' }}>déficit</span>?</h1>

        <p style={{
          margin: '24px 0 0', fontSize: 16, lineHeight: 1.55, color: 'var(--ink-2)', maxWidth: 320,
        }}>O Samsung Health já sabe quanto você gastou. Nós cruzamos com sua ingestão e respondemos a pergunta — todo dia.</p>

        <div style={{ marginTop: 36, display: 'flex', flexDirection: 'column', gap: 0,
          borderTop: '1px solid var(--hairline)' }}>
          {[
            ['Gasto vs ingestão', 'cruzados em um número'],
            ['Tendência semanal', 'em vez de fotos do dia'],
            ['100% no aparelho', 'sem conta, sem nuvem'],
          ].map(([t, s], i) => (
            <div key={i} style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'baseline',
              padding: '14px 0', borderBottom: '1px solid var(--hairline)',
            }}>
              <span style={{ fontSize: 14, fontWeight: 500, color: 'var(--ink-1)' }}>{t}</span>
              <span style={{ fontSize: 13, color: 'var(--ink-3)' }}>{s}</span>
            </div>
          ))}
        </div>
      </div>

      <div style={{ padding: '16px 24px 32px', display: 'flex', flexDirection: 'column', gap: 14 }}>
        <button className="btn-primary brand" onClick={onContinue}>Começar agora</button>
        <PrivacyNote />
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// VARIANT C — Welcome: Warm/card-stack metaphor — three sources merge
// ─────────────────────────────────────────────────────────────
function WelcomeC({ onContinue }) {
  const card = (label, value, color, offset, scale = 1, rot = 0) => (
    <div style={{
      position: 'absolute', left: '50%',
      transform: `translate(-50%, ${offset}px) rotate(${rot}deg) scale(${scale})`,
      width: 240, padding: '14px 18px',
      background: '#fff', border: '1px solid var(--hairline)',
      borderRadius: 'var(--r-md)', boxShadow: 'var(--shadow-md)',
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    }}>
      <span style={{ fontSize: 13, color: 'var(--ink-3)' }}>{label}</span>
      <span className="num tight" style={{ fontSize: 18, fontWeight: 600, color }}>{value}</span>
    </div>
  );

  return (
    <div style={{
      height: '100%', background: 'var(--bg)',
      display: 'flex', flexDirection: 'column',
    }}>
      <div style={{ padding: '20px 24px 0' }}><Wordmark /></div>

      {/* Stage with stacked cards */}
      <div style={{ position: 'relative', height: 280, marginTop: 16 }}>
        {/* halo */}
        <div style={{
          position: 'absolute', left: '50%', top: 130, transform: 'translateX(-50%)',
          width: 320, height: 320, borderRadius: '50%',
          background: 'radial-gradient(circle, var(--brand-tint) 0%, transparent 70%)',
        }} />
        {card('Gasto', '2 410 kcal', 'var(--ink-1)', 30, 0.92, -4)}
        {card('Ingestão', '1 928 kcal', 'var(--ink-1)', 90, 0.96, 2)}
        {/* hero balance card on top */}
        <div style={{
          position: 'absolute', left: '50%', top: 150, transform: 'translateX(-50%)',
          width: 280, padding: '20px 22px',
          background: 'var(--ink-1)', color: '#fff',
          borderRadius: 'var(--r-lg)', boxShadow: 'var(--shadow-lg)',
        }}>
          <div style={{ fontSize: 12, opacity: 0.6, fontWeight: 500 }}>BALANÇO HOJE</div>
          <div className="tight num" style={{
            fontSize: 44, fontWeight: 600, letterSpacing: '-0.03em', lineHeight: 1, marginTop: 4,
          }}>−482 <span style={{ fontSize: 16, opacity: 0.6 }}>kcal</span></div>
          <div style={{
            marginTop: 12, display: 'inline-flex', alignItems: 'center', gap: 6,
            padding: '4px 10px', background: 'rgba(255,255,255,0.12)', borderRadius: 999,
            fontSize: 11, fontWeight: 500,
          }}>
            <span className="dot" style={{ color: 'var(--deficit)', filter: 'brightness(1.3)' }} />
            Em déficit
          </div>
        </div>
      </div>

      <div style={{ flex: 1, padding: '28px 28px 0' }}>
        <h1 className="tight" style={{
          margin: 0, fontSize: 30, fontWeight: 600, lineHeight: 1.1,
          letterSpacing: '-0.025em', textAlign: 'center',
        }}>Os dados que você já gera,<br />finalmente respondendo.</h1>
        <p style={{
          margin: '14px auto 0', fontSize: 14, lineHeight: 1.5,
          color: 'var(--ink-2)', textAlign: 'center', maxWidth: 300,
        }}>Conectamos seu Samsung Health e calculamos seu saldo calórico real — todo dia, semana, mês.</p>
      </div>

      <div style={{ padding: '16px 24px 32px', display: 'flex', flexDirection: 'column', gap: 14 }}>
        <button className="btn-primary" onClick={onContinue}>Conectar Samsung Health</button>
        <PrivacyNote>On-device · LGPD · sem conta</PrivacyNote>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// VARIANT A — Health Connect: granular toggles, scroll-to-bottom
// ─────────────────────────────────────────────────────────────
function ConnectA({ onContinue }) {
  const [toggles, setToggles] = React.useState({ cal: false, weight: false, ex: false });
  const [scrolled, setScrolled] = React.useState(false);
  const anyOn = Object.values(toggles).some(Boolean);

  const Toggle = ({ on, onChange }) => (
    <button onClick={onChange} style={{
      width: 44, height: 26, borderRadius: 999, border: 0, cursor: 'pointer',
      background: on ? 'var(--brand)' : 'var(--bg-sunken)',
      position: 'relative', flexShrink: 0, transition: 'background .15s',
    }}>
      <span style={{
        position: 'absolute', top: 3, left: on ? 21 : 3,
        width: 20, height: 20, borderRadius: '50%', background: '#fff',
        boxShadow: '0 1px 3px rgba(0,0,0,0.2)', transition: 'left .15s',
      }} />
    </button>
  );

  const Row = ({ k, title, desc, perms }) => (
    <div style={{
      padding: '18px 20px', display: 'flex', gap: 14,
      borderBottom: '1px solid var(--hairline)',
    }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--ink-1)' }}>{title}</div>
        <div style={{ fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.45, marginTop: 4 }}>{desc}</div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 10 }}>
          {perms.map(p => <span key={p} className="chip">{p}</span>)}
        </div>
      </div>
      <Toggle on={toggles[k]} onChange={() => setToggles(s => ({ ...s, [k]: !s[k] }))} />
    </div>
  );

  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px 24px 16px', display: 'flex', alignItems: 'center', gap: 10 }}>
        <button className="btn-ghost" style={{ padding: 8, height: 36, width: 36 }}>
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M11 4l-5 5 5 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>
        <div style={{ flex: 1, display: 'flex', justifyContent: 'center', gap: 6 }}>
          {[0,1,2,3,4].map(i => (
            <span key={i} style={{
              width: 6, height: 6, borderRadius: '50%',
              background: i <= 2 ? 'var(--ink-1)' : 'var(--hairline-strong)',
            }}/>
          ))}
        </div>
        <div style={{ width: 36 }}/>
      </div>

      <div style={{ padding: '0 24px 8px' }}>
        <h1 className="tight" style={{
          margin: 0, fontSize: 26, fontWeight: 600, letterSpacing: '-0.02em', lineHeight: 1.15,
        }}>Conectar ao Samsung Health</h1>
        <p style={{ margin: '8px 0 0', fontSize: 14, color: 'var(--ink-2)', lineHeight: 1.5 }}>
          Escolha quais dados o app pode ler. Nada é compartilhado fora do seu aparelho.
        </p>
      </div>

      <div
        onScroll={(e) => {
          const el = e.currentTarget;
          if (el.scrollTop + el.clientHeight >= el.scrollHeight - 8) setScrolled(true);
        }}
        style={{ flex: 1, overflow: 'auto', margin: '16px 16px 0',
          background: 'var(--bg-elev)', borderRadius: 'var(--r-lg)',
          border: '1px solid var(--hairline)' }}
      >
        <Row k="cal" title="Calorias"
          desc="Gasto calórico diário e, se disponível, ingestão. Usado para calcular seu balanço diário."
          perms={['Gasto total', 'Gasto ativo', 'Nutrição']} />
        <Row k="weight" title="Peso"
          desc="Histórico de peso para acompanhar variação ao longo do tempo."
          perms={['Massa corporal']} />
        <Row k="ex" title="Treinos"
          desc="Tipo de atividade e duração. O app não lê GPS, rota ou localização."
          perms={['Sessão de exercício']} />
        <div style={{ padding: '18px 20px', fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.6 }}>
          <b style={{ color: 'var(--ink-2)' }}>O que o Health Insights nunca faz:</b><br/>
          envia dados para servidores · cria conta · exibe anúncios · grava localização · escreve no Health Connect. Você pode revogar permissões a qualquer momento em Configurações.
          <a href="#" style={{ display: 'block', marginTop: 12, color: 'var(--brand-ink)', fontWeight: 500 }}>
            Política de privacidade →
          </a>
        </div>
      </div>

      <div style={{ padding: '16px 24px 28px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <button className="btn-primary"
          disabled={!scrolled}
          style={{ background: scrolled ? 'var(--ink-1)' : 'var(--ink-4)', cursor: scrolled ? 'pointer' : 'default' }}
          onClick={onContinue}>
          {anyOn ? 'Concordo — permitir acesso' : 'Continuar em modo manual'}
        </button>
        {!scrolled && (
          <div style={{ fontSize: 12, color: 'var(--ink-3)', textAlign: 'center' }}>
            Role até o final para continuar
          </div>
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// VARIANT B — Health Connect: explainer-first, single big CTA
// ─────────────────────────────────────────────────────────────
function ConnectB({ onContinue }) {
  const Item = ({ icon, title, desc }) => (
    <div style={{ display: 'flex', gap: 14, padding: '14px 0', borderBottom: '1px solid var(--hairline)' }}>
      <div style={{
        width: 40, height: 40, borderRadius: 12, flexShrink: 0,
        background: 'var(--brand-tint)', color: 'var(--brand-ink)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontFamily: 'var(--font-display)', fontWeight: 600, fontSize: 16,
      }}>{icon}</div>
      <div>
        <div style={{ fontSize: 14, fontWeight: 600 }}>{title}</div>
        <div style={{ fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.45, marginTop: 2 }}>{desc}</div>
      </div>
    </div>
  );

  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px 24px 0', display: 'flex', alignItems: 'center', gap: 10 }}>
        <Wordmark />
        <div style={{ flex: 1 }}/>
        <span className="chip">3 de 5</span>
      </div>

      {/* connector visual */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 12, padding: '36px 24px 24px' }}>
        <div style={{
          width: 64, height: 64, borderRadius: 18,
          background: 'var(--bg-elev)', border: '1px solid var(--hairline)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: 'var(--shadow-sm)',
        }}>
          <span className="tight" style={{ fontWeight: 700, fontSize: 22, color: '#1428A0' }}>S</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          {[0,1,2].map(i => (
            <span key={i} style={{
              width: 5, height: 5, borderRadius: '50%',
              background: 'var(--brand)', opacity: 0.3 + i * 0.25,
            }}/>
          ))}
        </div>
        <div style={{
          width: 64, height: 64, borderRadius: 18, background: 'var(--ink-1)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Logo size={28} color="#fff"/>
        </div>
      </div>

      <div style={{ padding: '0 28px' }}>
        <h1 className="tight" style={{
          margin: 0, fontSize: 28, fontWeight: 600, letterSpacing: '-0.02em',
          lineHeight: 1.15, textAlign: 'center',
        }}>Conecte o Samsung Health</h1>
        <p style={{
          margin: '10px auto 0', fontSize: 14, lineHeight: 1.5,
          color: 'var(--ink-2)', textAlign: 'center', maxWidth: 300,
        }}>Vamos pedir 3 permissões via Health Connect. Você pode revogar a qualquer momento.</p>
      </div>

      <div style={{ padding: '20px 28px 0', flex: 1 }}>
        <Item icon="kc" title="Calorias" desc="Gasto e, se disponível, ingestão. Para calcular seu balanço diário." />
        <Item icon="kg" title="Peso" desc="Histórico para acompanhar variação semana a semana." />
        <Item icon="ex" title="Treinos" desc="Tipo e duração. Sem GPS, sem localização, sem rota." />
      </div>

      <div style={{ padding: '8px 24px 28px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        <button className="btn-primary brand" onClick={onContinue}>Permitir acesso</button>
        <button className="btn-ghost" style={{ height: 44, color: 'var(--ink-2)' }}>
          Continuar em modo manual
        </button>
        <PrivacyNote>Os dados nunca saem do seu aparelho.</PrivacyNote>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// VARIANT C — Health Connect: data sample preview, 'see what we'll read'
// ─────────────────────────────────────────────────────────────
function ConnectC({ onContinue }) {
  const Section = ({ on, setOn, title, sample, perms }) => (
    <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
      <div style={{
        padding: '14px 16px', display: 'flex', alignItems: 'center', gap: 12,
        borderBottom: on ? '1px solid var(--hairline)' : 'none',
      }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 14, fontWeight: 600 }}>{title}</div>
          <div style={{ fontSize: 12, color: 'var(--ink-3)', marginTop: 2 }}>{perms}</div>
        </div>
        <button onClick={() => setOn(!on)} style={{
          width: 40, height: 24, borderRadius: 999, border: 0, cursor: 'pointer',
          background: on ? 'var(--brand)' : 'var(--bg-sunken)', position: 'relative', flexShrink: 0,
        }}>
          <span style={{
            position: 'absolute', top: 3, left: on ? 19 : 3,
            width: 18, height: 18, borderRadius: '50%', background: '#fff',
            boxShadow: '0 1px 3px rgba(0,0,0,0.2)', transition: 'left .15s',
          }} />
        </button>
      </div>
      {on && (
        <div style={{ padding: '12px 16px 14px', background: 'var(--bg-sunken)' }}>
          <div style={{ fontSize: 11, color: 'var(--ink-3)', fontWeight: 500,
            letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: 8 }}>
            Amostra do que será lido
          </div>
          {sample}
        </div>
      )}
    </div>
  );

  const [a, setA] = React.useState(true);
  const [b, setB] = React.useState(true);
  const [c, setC] = React.useState(false);

  return (
    <div style={{ height: '100%', background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px 24px 8px' }}>
        <Wordmark />
        <h1 className="tight" style={{
          margin: '20px 0 0', fontSize: 26, fontWeight: 600,
          letterSpacing: '-0.02em', lineHeight: 1.15,
        }}>Veja o que vamos ler.</h1>
        <p style={{ margin: '8px 0 0', fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.5 }}>
          Sem caixas-pretas. Toque em cada categoria para ver uma amostra dos dados.
        </p>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '16px 16px 0',
        display: 'flex', flexDirection: 'column', gap: 12 }}>
        <Section on={a} setOn={setA} title="Calorias"
          perms="Gasto total · Gasto ativo · Nutrição"
          sample={
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6, fontFamily: 'var(--font-mono)', fontSize: 11 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--ink-2)' }}>
                <span>Hoje · gasto</span><span className="num">2 410 kcal</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--ink-2)' }}>
                <span>Hoje · ingestão</span><span className="num">1 928 kcal</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--ink-1)', fontWeight: 600 }}>
                <span>Saldo</span><span className="num" style={{ color: 'var(--deficit)' }}>−482 kcal</span>
              </div>
            </div>
          } />
        <Section on={b} setOn={setB} title="Peso"
          perms="Massa corporal"
          sample={
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: 4, height: 40 }}>
              {[78.4, 78.2, 78.3, 77.9, 77.8, 77.6, 77.5].map((w, i) => (
                <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column',
                  alignItems: 'center', gap: 4 }}>
                  <div style={{
                    width: '100%', height: `${(w - 77) * 30}px`,
                    background: 'var(--brand)', borderRadius: 2, opacity: 0.3 + i * 0.1,
                  }}/>
                </div>
              ))}
            </div>
          } />
        <Section on={c} setOn={setC} title="Treinos"
          perms="Sessão de exercício · sem GPS"
          sample={null} />

        <div style={{ padding: '8px 4px 16px', fontSize: 12, color: 'var(--ink-3)', lineHeight: 1.6 }}>
          A leitura é feita via Health Connect. Você pode revogar acesso a qualquer momento — o app continua funcionando em modo manual.
        </div>
      </div>

      <div style={{ padding: '12px 24px 28px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <button className="btn-primary brand" onClick={onContinue}>Permitir acesso</button>
        <PrivacyNote>On-device · LGPD Art. 11</PrivacyNote>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Tokens / handoff card content
// ─────────────────────────────────────────────────────────────
function HandoffCard() {
  const Sw = ({ name, value, ink = 'var(--ink-1)' }) => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      <div style={{ height: 56, background: value, borderRadius: 8, border: '1px solid var(--hairline)' }} />
      <div style={{ fontSize: 11, fontFamily: 'var(--font-mono)', color: ink }}>{name}</div>
    </div>
  );
  return (
    <div style={{ padding: 32, fontSize: 13, lineHeight: 1.55, color: 'var(--ink-1)',
      background: 'var(--bg-elev)', height: '100%', overflow: 'auto' }}>
      <div style={{ fontSize: 11, fontWeight: 500, letterSpacing: '0.1em',
        textTransform: 'uppercase', color: 'var(--ink-3)' }}>Handoff · Compose tokens</div>
      <h2 className="tight" style={{ margin: '6px 0 24px', fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em' }}>
        Direção de design
      </h2>

      <h3 style={{ fontSize: 13, fontWeight: 600, margin: '0 0 10px', color: 'var(--ink-2)' }}>Cores</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10 }}>
        <Sw name="bg #FAFAF7" value="#FAFAF7" />
        <Sw name="ink-1 #0E1116" value="#0E1116" ink="var(--ink-3)" />
        <Sw name="brand sage" value="oklch(0.62 0.12 155)" />
        <Sw name="surplus" value="oklch(0.68 0.14 35)" />
        <Sw name="bg-elev #FFF" value="#FFFFFF" />
        <Sw name="bg-sunken" value="#F2F1EC" />
        <Sw name="brand-tint" value="oklch(0.96 0.04 155)" />
        <Sw name="maintain" value="oklch(0.62 0.06 250)" />
      </div>

      <h3 style={{ fontSize: 13, fontWeight: 600, margin: '24px 0 10px', color: 'var(--ink-2)' }}>Tipografia</h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
        <div className="tight num" style={{ fontSize: 44, fontWeight: 600, letterSpacing: '-0.035em', lineHeight: 1 }}>−482</div>
        <div className="tight" style={{ fontSize: 26, fontWeight: 600, letterSpacing: '-0.02em' }}>Display 26 / 600</div>
        <div style={{ fontSize: 16, fontWeight: 500 }}>Body 16 / 500</div>
        <div style={{ fontSize: 13, color: 'var(--ink-2)' }}>Body 13 / 400 — secondary</div>
        <div style={{ fontSize: 11, fontFamily: 'var(--font-mono)', color: 'var(--ink-3)' }}>Mono 11 — labels</div>
      </div>

      <h3 style={{ fontSize: 13, fontWeight: 600, margin: '24px 0 10px', color: 'var(--ink-2)' }}>Princípios</h3>
      <ol style={{ paddingLeft: 18, margin: 0, color: 'var(--ink-2)' }}>
        <li><b style={{ color: 'var(--ink-1)' }}>O número primeiro.</b> Headlines liderados por dados, não por adjetivos.</li>
        <li><b style={{ color: 'var(--ink-1)' }}>Sinal explícito.</b> Sempre mostrar +/− e cor (déficit/superávit/manter).</li>
        <li><b style={{ color: 'var(--ink-1)' }}>Branco respira.</b> Fundo claro neutro; nunca gradientes saturados.</li>
        <li><b style={{ color: 'var(--ink-1)' }}>Privacidade visível.</b> Linha "no aparelho" em todo CTA crítico.</li>
        <li><b style={{ color: 'var(--ink-1)' }}>Sem placeholder slop.</b> Sem ícones decorativos; só dado real ou stripe.</li>
      </ol>

      <h3 style={{ fontSize: 13, fontWeight: 600, margin: '24px 0 10px', color: 'var(--ink-2)' }}>Mapeamento → Compose</h3>
      <pre style={{
        background: 'var(--bg-sunken)', padding: 14, borderRadius: 10,
        fontFamily: 'var(--font-mono)', fontSize: 11, lineHeight: 1.55, margin: 0,
        whiteSpace: 'pre-wrap',
      }}>{`MaterialTheme.colorScheme:
  background     = #FAFAF7
  surface        = #FFFFFF
  onSurface      = #0E1116
  primary        = oklch(0.62 0.12 155)  → #6FA47A approx
  onPrimary      = #FFFFFF

Typography:
  displayLarge   = Inter Tight 44/-3.5%/600
  headlineMedium = Inter Tight 26/-2%/600
  bodyLarge      = Inter 16/500
  bodyMedium     = Inter 14/400
  labelSmall     = JetBrains Mono 11

Shapes:
  small  = 10dp · medium = 16dp · large = 22dp`}</pre>

      <h3 style={{ fontSize: 13, fontWeight: 600, margin: '24px 0 10px', color: 'var(--ink-2)' }}>Ajustes vs tela atual</h3>
      <ul style={{ paddingLeft: 18, margin: 0, color: 'var(--ink-2)' }}>
        <li>Remover gradiente roxo + círculos decorativos (não-brand, não-funcional).</li>
        <li>Trocar logo "HI" por glyph de gráfico ou wordmark — é a marca real.</li>
        <li>Headline conduz com promessa de dado, não com pergunta retórica.</li>
        <li>Mostrar prévia do produto na tela 1 (mini-card de saldo).</li>
        <li>Microcopy de privacidade ganha ícone + posição fixa acima do CTA.</li>
        <li>Botão primário: cantos 16dp, ink-1 sólido (não brand) para força máxima.</li>
      </ul>
    </div>
  );
}

window.WelcomeA = WelcomeA;
window.WelcomeB = WelcomeB;
window.WelcomeC = WelcomeC;
window.ConnectA = ConnectA;
window.ConnectB = ConnectB;
window.ConnectC = ConnectC;
window.HandoffCard = HandoffCard;
window.SCREEN_W = SCREEN_W;
window.SCREEN_H = SCREEN_H;

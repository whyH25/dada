// 원본 mypage.js의 리포트 렌더 함수들을 그대로 옮긴 모듈입니다.
// 패널은 정적 HTML 문자열을 반환하므로 Vue에서 v-html로 렌더합니다.
/* eslint-disable */

export const compLabels = ['직무 전문성', '논리적 사고', '커뮤니케이션', '조직 적합성', '압박 대응']

export function scoreColor(v) {
  if (v >= 80) return 'var(--green-500)'
  if (v >= 70) return 'var(--accent-amber)'
  return 'var(--accent-red)'
}
export function statusBadge(label, kind) {
  return `<span class="badge badge-${kind}">${label}</span>`
}

export function radarSVG(axes, series, size) {
  size = size || 270
  const c = size / 2, r = size * 0.32, n = axes.length
  const ang = (i) => (-90 + i * 360 / n) * Math.PI / 180
  const pt = (i, val) => [c + r * (val / 100) * Math.cos(ang(i)), c + r * (val / 100) * Math.sin(ang(i))]
  let g = ''
  ;[20, 40, 60, 80, 100].forEach((rr) => {
    const pts = axes.map((_, i) => { const p = pt(i, rr); return p[0].toFixed(1) + ',' + p[1].toFixed(1) }).join(' ')
    g += `<polygon points="${pts}" fill="${rr === 100 ? 'var(--ink-50)' : 'none'}" stroke="var(--ink-200)" stroke-width="1"/>`
  })
  axes.forEach((lab, i) => {
    const p = pt(i, 100)
    g += `<line x1="${c}" y1="${c}" x2="${p[0].toFixed(1)}" y2="${p[1].toFixed(1)}" stroke="var(--ink-200)" stroke-width="1"/>`
    const lp = pt(i, 124)
    let anchor = 'middle'
    if (lp[0] < c - 4) anchor = 'end'; else if (lp[0] > c + 4) anchor = 'start'
    g += `<text x="${lp[0].toFixed(1)}" y="${(lp[1] + 4).toFixed(1)}" text-anchor="${anchor}" font-size="11.5" fill="var(--ink-600)" font-weight="600">${lab}</text>`
  })
  series.forEach((s) => {
    const pts = s.values.map((v, i) => { const p = pt(i, v); return p[0].toFixed(1) + ',' + p[1].toFixed(1) }).join(' ')
    g += `<polygon points="${pts}" fill="${s.fill}" stroke="${s.stroke}" stroke-width="2" stroke-linejoin="round"/>`
    s.values.forEach((v, i) => { const p = pt(i, v); g += `<circle cx="${p[0].toFixed(1)}" cy="${p[1].toFixed(1)}" r="3" fill="${s.stroke}"/>` })
  })
  return `<svg viewBox="0 0 ${size} ${size}" class="radar-svg">${g}</svg>`
}

// 총점 추이 꺾은선 그래프 SVG
function lineSVG(history) {
  if (!history || history.length === 0) {
    return `<p style="color:var(--ink-400);font-size:13px;text-align:center;padding:24px 0;margin:0;">면접 기록이 없습니다.</p>`
  }
  if (history.length === 1) {
    return `<div style="text-align:center;padding:20px 0;">
      <div style="font-size:48px;font-weight:900;color:var(--green-500);line-height:1;">${history[0].score}</div>
      <div style="font-size:12px;color:var(--ink-400);margin-top:6px;">첫 번째 면접 기록</div>
    </div>`
  }

  const W = 260, H = 148
  const pL = 30, pR = 10, pT = 18, pB = 30
  const cW = W - pL - pR, cH = H - pT - pB

  const scores = history.map(h => h.score)
  const minS = Math.max(0, Math.min(...scores) - 8)
  const maxS = Math.min(100, Math.max(...scores) + 8)
  const rangeS = maxS - minS || 20

  const xPos = i => pL + (i / (history.length - 1)) * cW
  const yPos = s => pT + cH - ((s - minS) / rangeS) * cH

  // Y축 눈금 (3개)
  const yTicks = [minS, Math.round((minS + maxS) / 2), maxS]
  let grid = yTicks.map(v => {
    const y = yPos(v).toFixed(1)
    return `<line x1="${pL}" y1="${y}" x2="${W - pR}" y2="${y}" stroke="var(--ink-150)" stroke-width="1" stroke-dasharray="3 3"/>
            <text x="${pL - 4}" y="${(parseFloat(y) + 3.5).toFixed(1)}" text-anchor="end" font-size="9" fill="var(--ink-400)">${v}</text>`
  }).join('')

  const pts = history.map((h, i) => `${xPos(i).toFixed(1)},${yPos(h.score).toFixed(1)}`).join(' ')
  const areaPts = `${xPos(0).toFixed(1)},${(pT + cH).toFixed(1)} ${pts} ${xPos(history.length - 1).toFixed(1)},${(pT + cH).toFixed(1)}`

  // 점 + 점수 레이블
  const step = Math.ceil(history.length / 5)
  let dots = '', xLabels = ''
  history.forEach((h, i) => {
    const x = xPos(i).toFixed(1), y = yPos(h.score).toFixed(1)
    const cur = h.current
    dots += `<circle cx="${x}" cy="${y}" r="${cur ? 5.5 : 3}" fill="${cur ? 'var(--green-500)' : '#fff'}" stroke="var(--green-500)" stroke-width="${cur ? 2.5 : 1.5}"/>`
    if (cur) {
      dots += `<text x="${x}" y="${(parseFloat(y) - 9).toFixed(1)}" text-anchor="middle" font-size="10.5" font-weight="700" fill="var(--green-500)">${h.score}</text>`
    }
    if (i === 0 || i === history.length - 1 || cur || i % step === 0) {
      xLabels += `<text x="${x}" y="${(pT + cH + 14).toFixed(1)}" text-anchor="middle" font-size="9" fill="${cur ? 'var(--green-500)' : 'var(--ink-400)'}" font-weight="${cur ? '700' : '400'}">${h.date}</text>`
    }
  })

  return `<svg viewBox="0 0 ${W} ${H}" width="100%" style="overflow:visible;display:block;">
    <defs>
      <linearGradient id="lgOvChart" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="var(--green-500)" stop-opacity="0.18"/>
        <stop offset="100%" stop-color="var(--green-500)" stop-opacity="0.01"/>
      </linearGradient>
    </defs>
    ${grid}
    <polygon points="${areaPts}" fill="url(#lgOvChart)"/>
    <polyline points="${pts}" fill="none" stroke="var(--green-500)" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"/>
    ${dots}
    ${xLabels}
  </svg>`
}

export function panelOverview(r) {
  const sc = r.score
  const heroClass = sc >= 80 ? 'good' : sc >= 60 ? 'warn' : 'bad'
  const scLabel = sc >= 90 ? '매우 우수' : sc >= 80 ? '합격권' : sc >= 70 ? '평균 이상' : sc >= 60 ? '보완 필요' : '개선 필요'

  // 히어로 한 줄 총평 — 첫 문장
  const firstLine = r.aiComment ? r.aiComment.split(/(?<=[.。])\s+/)[0].trim() : ''
  const heroQuote = firstLine.length > 10 ? firstLine : (r.aiComment || '')

  // 사용자 강약점 (지원자 비교 패널의 me 항목 재사용)
  const meAppl = r.applicants ? r.applicants.find(a => a.me) : null

  // 면접 정보 아이콘 목록
  const infoItems = r.info ? [
    { ico: '🏢', label: '회사', val: r.info.company },
    { ico: '💼', label: '직무', val: r.info.job },
    { ico: '📋', label: '지원 유형', val: r.info.type },
    { ico: '⚡', label: '난이도', val: r.info.difficulty },
    { ico: '🎤', label: 'AI 면접관', val: r.info.interviewerCnt + '명' },
    { ico: '👤', label: 'AI 지원자', val: r.info.applicantCnt + '명' },
  ] : []

  // 통계 미니 카드
  const fillerBad = r.speechFiller != null && r.speechFiller > 5
  const stats = [
    { ico: '⏱', label: '평균 답변 시간', val: r.speech.avgLen + '초', mod: '' },
    { ico: '💬', label: '분당 어절 수 (WPM)', val: r.speechWpm != null ? r.speechWpm + ' wpm' : '-', mod: '' },
    { ico: '🔔', label: '추임새 횟수', val: r.speechFiller != null ? r.speechFiller + '회' : '-', mod: fillerBad ? 'warn' : '' },
  ]

  // 히어로 체크리스트 일러스트 SVG — bad는 다홍 계열, 나머지는 흰색 반투명
  const isBad = heroClass === 'bad'
  const ic = isBad
    ? { bg: 'rgba(201,41,74,0.08)', border: 'rgba(201,41,74,0.22)', line: 'rgba(201,41,74,0.35)', line2: 'rgba(201,41,74,0.25)', dot: 'rgba(201,41,74,0.25)', ck: 'rgba(201,41,74,0.75)' }
    : { bg: 'rgba(255,255,255,0.14)', border: 'rgba(255,255,255,0.28)', line: 'rgba(255,255,255,0.55)', line2: 'rgba(255,255,255,0.4)', dot: 'rgba(255,255,255,0.35)', ck: 'rgba(255,255,255,0.9)' }
  const illustSvg = `<svg viewBox="0 0 90 90" width="86" height="86" fill="none" aria-hidden="true">
    <rect x="16" y="8" width="58" height="74" rx="7" fill="${ic.bg}" stroke="${ic.border}" stroke-width="1.5"/>
    <rect x="28" y="26" width="34" height="4" rx="2" fill="${ic.line}"/>
    <rect x="28" y="37" width="26" height="4" rx="2" fill="${ic.line2}"/>
    <rect x="28" y="48" width="30" height="4" rx="2" fill="${ic.line2}"/>
    <rect x="28" y="59" width="18" height="4" rx="2" fill="${ic.dot}"/>
    <circle cx="21" cy="28" r="3.5" fill="${ic.dot}"/>
    <polyline points="19.5,28 21,29.8 23,26" stroke="${ic.ck}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
    <circle cx="21" cy="39" r="3.5" fill="${ic.dot}"/>
    <circle cx="21" cy="50" r="3.5" fill="${ic.dot}"/>
    <circle cx="21" cy="61" r="3.5" fill="${ic.dot}" opacity="0.7"/>
  </svg>`

  return `
    <div class="ov-hero ov-hero--${heroClass}">
      <div class="ov-hero-score">
        <div class="ov-hero-num">${sc}</div>
        <div class="ov-hero-badge">${scLabel}</div>
        <div class="ov-hero-max">/ 100점</div>
      </div>
      <div class="ov-hero-body">
        <div class="ov-hero-eyebrow">AI 종합 평가</div>
        <p class="ov-hero-quote">"${heroQuote}"</p>
        ${r.info ? `<div class="ov-hero-meta">${r.info.company} · ${r.info.job} · ${r.info.endedAt}</div>` : ''}
      </div>
      <div class="ov-hero-illus">${illustSvg}</div>
    </div>

    ${r.info ? `
    <div class="card" style="margin-bottom:16px;">
      <div class="card-header">
        <h3 class="card-title">면접 정보</h3>
        <span class="text-sm text-muted">${r.info.endedAt}</span>
      </div>
      <div class="ov-info-grid">
        ${infoItems.map(item => `
          <div class="ov-info-item">
            <span class="ov-info-ico">${item.ico}</span>
            <div>
              <div class="ov-info-label">${item.label}</div>
              <div class="ov-info-val">${item.val}</div>
            </div>
          </div>`).join('')}
      </div>
    </div>` : ''}

    <div class="rep-grid-2" style="margin-bottom:16px;">
      <div class="card">
        <div class="card-header"><h3 class="card-title">면접 통계</h3></div>
        <div class="ov-stats">
          ${stats.map(s => `
            <div class="ov-stat-card${s.mod ? ' ov-stat-card--' + s.mod : ''}">
              <span class="ov-stat-ico">${s.ico}</span>
              <div class="ov-stat-body">
                <div class="ov-stat-label">${s.label}</div>
                <div class="ov-stat-val">${s.val}</div>
              </div>
            </div>`).join('')}
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3 class="card-title">이번 면접 한눈에 보기</h3></div>
        ${meAppl ? `
        <div class="ov-sw-stack">
          <div class="ov-sw-card ov-sw-card--good">
            <div class="ov-sw-head"><span>✅</span><span>잘한 점</span></div>
            <p class="ov-sw-body">${meAppl.strength || '-'}</p>
          </div>
          <div class="ov-sw-card ov-sw-card--bad">
            <div class="ov-sw-head"><span>📌</span><span>개선할 점</span></div>
            <p class="ov-sw-body">${meAppl.weak || '-'}</p>
          </div>
        </div>` : `<p style="color:var(--ink-400);font-size:13px;margin:0;">분석 데이터가 없습니다.</p>`}
      </div>
    </div>

    <div class="card" style="margin-bottom:16px;">
      <div class="card-header">
        <h3 class="card-title">AI 종합 요약</h3>
        <span class="ai-badge">AI</span>
      </div>
      <p class="rep-summary">${r.aiComment}</p>
    </div>

    <div class="rep-grid-2">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">총점 추이</h3>
          <span class="text-sm text-muted">${r.scoreHistory ? r.scoreHistory.length + '회' : '-'}</span>
        </div>
        ${lineSVG(r.scoreHistory)}
      </div>
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">다음 면접 체크리스트</h3>
          <span class="text-sm text-muted">꼭 준비하세요</span>
        </div>
        ${r.checklist && r.checklist.length ? `
        <ul class="ov-cl-list">
          ${r.checklist.map((item, i) => `
            <li class="ov-cl-item">
              <span class="ov-cl-num">${i + 1}</span>
              <span class="ov-cl-text">${item}</span>
            </li>`).join('')}
        </ul>` : `<p style="color:var(--ink-400);font-size:13px;margin:0;">체크리스트가 없습니다.</p>`}
      </div>
    </div>
  `
}

export function panelCompetency(r) {
  const bars = compLabels.map((l, i) => `
    <div class="score-item">
      <div class="score-num ${r.me[i] < 75 ? 'warn' : ''}">${r.me[i]}</div>
      <div class="score-label">${l}</div>
      <div class="score-bar"><div class="score-bar-fill ${r.me[i] < 75 ? 'warn' : ''}" style="width:${r.me[i]}%"></div></div>
    </div>`).join('')
  return `
    <div class="rep-grid-2" style="margin-bottom:16px;">
      <div class="card">
        <div class="card-header"><h3 class="card-title">역량 레이더</h3><span class="text-sm text-muted">5개 영역</span></div>
        <div class="radar-wrap">${radarSVG(compLabels, [{ values: r.me, stroke: 'var(--green-500)', fill: 'rgba(48,136,96,0.16)' }])}</div>
      </div>
      <div class="card">
        <div class="card-header"><h3 class="card-title">역량별 점수</h3></div>
        <div class="score-grid">${bars}</div>
      </div>
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">역량별 비교</h3><span class="text-sm text-muted">나 · AI 지원자 평균</span></div>
      <div class="cmp-list">
        ${compLabels.map((l, i) => `
          <div class="cmp-row">
            <div class="cmp-label">${l}</div>
            <div class="cmp-track">
              <div class="cmp-tick ai" style="left:${r.ai[i]}%" title="AI ${r.ai[i]}"></div>
              <div class="cmp-fill" style="width:${r.me[i]}%"></div>
            </div>
            <div class="cmp-val">${r.me[i]}</div>
          </div>`).join('')}
      </div>
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">역량별 상세 분석</h3><span class="ai-badge">AI</span></div>
      ${compLabels.map((l, i) => {
        const detail = r.compDetails && r.compDetails[i]
        if (!detail) return ''
        return `<div class="comp-detail-item">
          <div class="comp-detail-head">
            <span class="comp-detail-label">${l}</span>
            <span class="badge ${r.me[i] >= 80 ? 'badge-green' : r.me[i] >= 60 ? 'badge-amber' : 'badge-red'}">${r.me[i]}점</span>
          </div>
          <p class="comp-detail-body">${detail}</p>
        </div>`
      }).filter(Boolean).join('<div class="comp-detail-divider"></div>')}
    </div>`
}

export function panelApplicants(r) {
  const max = Math.max(...r.applicants.map((a) => a.score))
  return `
    <div class="card">
      <div class="card-header"><h3 class="card-title">지원자 비교</h3><span class="text-sm text-muted">같은 면접방의 AI 지원자 대비 나</span></div>
      <div class="appl-list">
        ${r.applicants.map((a) => `
          <div class="appl-row ${a.me ? 'me' : ''}">
            <div class="appl-name">${a.me ? '<span class="appl-you">YOU</span>' : '<span class="appl-ai">AI</span>'} ${a.name}</div>
            <div class="appl-track"><div class="appl-fill ${a.me ? 'me' : ''}" style="width:${a.score}%"></div></div>
            <div class="appl-score">${a.score}${a.score === max ? ' <span class="appl-top">최고</span>' : ''}</div>
          </div>`).join('')}
      </div>
      <div class="divider"></div>
      <div class="appl-tags">
        ${r.applicants.map((a) => `<div class="appl-tag-row"><span class="appl-tag-name">${a.me ? '나' : a.name}</span><span class="badge badge-green">강점 · ${a.strength}</span><span class="badge badge-amber">약점 · ${a.weak}</span></div>`).join('')}
      </div>
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">종합 인사이트</h3><span class="ai-badge">AI</span></div>
      ${r.insight
        ? `<p class="rep-summary">${r.insight}</p>`
        : `<p style="color:var(--ink-400);font-size:13px;margin:0;">인사이트 데이터가 없습니다.</p>`}
    </div>`
}

function participantColor(turnRole) {
  return turnRole === 'USER' ? 'var(--green-500,#309860)' : 'var(--accent-blue,#1f6fe5)'
}

export function panelQuestions(r) {
  const userScores = r.questions.flatMap(q => q.participants.filter(p => p.turnRole === 'USER').map(p => p.score))
  const avg = userScores.length ? Math.round(userScores.reduce((a, v) => a + v, 0) / userScores.length) : 0

  return `
    <div class="card">
      <div class="card-header"><h3 class="card-title">질문별 상세 피드백 (${r.questions.length}문항)</h3><span class="text-sm text-muted">평균 ${avg}점</span></div>
      <div class="q-list">
        ${r.questions.map((q) => `
          <div class="q-item open">
            <div class="q-head">
              <div><div class="q-num">${q.q}</div><div class="q-text">${q.text}</div></div>
            </div>
            <div class="q-body">
              ${q.participants.length === 0 ? '<p style="color:var(--ink-400);font-size:13px;">분석 데이터 없음</p>' :
                q.participants.map(p => `
                  <div style="margin-bottom:16px;padding:12px;border-radius:8px;background:var(--ink-50);border:1px solid var(--ink-150);">
                    <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px;">
                      <span style="padding:2px 8px;border-radius:99px;background:${participantColor(p.turnRole)};color:#fff;font-size:11px;font-weight:700;">${p.name}</span>
                      ${p.label ? `<span class="badge badge-${p.label[1]}">${p.label[0]}</span>` : ''}
                      <span style="margin-left:auto;font-weight:700;${p.score < 70 ? 'color:var(--accent-red)' : ''}">${p.score}점</span>
                    </div>
                    ${p.answerText ? `<p style="margin:0 0 10px;font-size:13px;line-height:1.75;color:var(--ink-800);white-space:pre-wrap;">${p.answerText}</p>` : '<p style="margin:0 0 10px;font-size:13px;color:var(--ink-400);">(답변 없음)</p>'}
                    <div style="height:1px;background:var(--ink-200);margin:8px 0;"></div>
                    <div style="font-size:12px;font-weight:600;color:var(--ink-400);margin-bottom:4px;letter-spacing:.04em;">AI 피드백</div>
                    <div style="font-size:13px;line-height:1.7;color:var(--ink-700);">${p.body || '-'}</div>
                    ${p.tags.length ? `<div class="q-tag-row" style="margin-top:8px;">${p.tags.map(t => `<span class="badge badge-outline">${t}</span>`).join('')}</div>` : ''}
                  </div>`).join('')}
            </div>
          </div>`).join('')}
      </div>
    </div>`
}

export function reportPanel(r, tab) {
  if (tab === 'overview') return panelOverview(r)
  if (tab === 'competency') return panelCompetency(r)
  if (tab === 'applicants') return panelApplicants(r)
  return panelQuestions(r)
}

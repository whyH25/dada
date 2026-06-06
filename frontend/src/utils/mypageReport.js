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

export function speedGauge(wpm, status) {
  const pct = Math.max(4, Math.min(100, ((wpm - 120) / (320 - 120)) * 100))
  return `
    <div class="speed-gauge">
      <div class="speed-bar">
        <div class="speed-zone slow">느림</div>
        <div class="speed-zone ok">적정</div>
        <div class="speed-zone fast">빠름</div>
        <div class="speed-marker" style="left:${pct}%"><span>${wpm} WPM</span></div>
      </div>
      <div class="speed-foot"><span>120</span><span class="badge badge-${status[1]}">${status[0]}</span><span>320</span></div>
    </div>`
}
function toneBar(label, v, lo, hi) {
  return `<div class="tone-item">
    <div class="tone-head"><span>${label}</span><strong>${v}</strong></div>
    <div class="tone-track"><div class="tone-fill" style="width:${v}%"></div></div>
    <div class="tone-ends"><span>${lo}</span><span>${hi}</span></div>
  </div>`
}
function fillerRow(word, n) {
  const w = Math.min(100, n * 9)
  return `<div class="filler-row"><span class="filler-word">${word}</span><div class="filler-track"><div class="filler-bar" style="width:${w}%"></div></div><span class="filler-n">${n}회</span></div>`
}

export function panelOverview(r) {
  const bars = compLabels.map((l, i) => `
    <div class="score-item">
      <div class="score-num ${r.me[i] < 75 ? 'warn' : ''}">${r.me[i]}</div>
      <div class="score-label">${l}</div>
      <div class="score-bar"><div class="score-bar-fill ${r.me[i] < 75 ? 'warn' : ''}" style="width:${r.me[i]}%"></div></div>
    </div>`).join('')
  return `
    <div class="rep-grid-2">
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
      <div class="card-header"><h3 class="card-title">발화 속도</h3><span class="text-sm text-muted">분당 단어 수(WPM) 기준</span></div>
      ${speedGauge(r.speech.wpm, r.speech.wpmStatus)}
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">총평 요약</h3></div>
      <p class="rep-summary">${r.aiComment}</p>
    </div>`
}

export function panelCompetency(r) {
  const logicIdx = 1
  return `
    <div class="card">
      <div class="card-header"><h3 class="card-title">역량 레이더 비교</h3></div>
      <div class="radar-compare">
        <div class="radar-wrap">${radarSVG(compLabels, [
          { values: r.ai, stroke: 'var(--accent-amber)', fill: 'rgba(201,138,16,0.10)' },
          { values: r.pass, stroke: 'var(--accent-blue)', fill: 'rgba(31,111,229,0.10)' },
          { values: r.me, stroke: 'var(--green-500)', fill: 'rgba(48,136,96,0.18)' },
        ])}</div>
        <div class="radar-legend">
          <div class="rl-item"><span class="rl-dot" style="background:var(--green-500)"></span>나 (${r.score}점)</div>
          <div class="rl-item"><span class="rl-dot" style="background:var(--accent-blue)"></span>합격자 평균</div>
          <div class="rl-item"><span class="rl-dot" style="background:var(--accent-amber)"></span>AI 지원자 평균</div>
          <div class="rl-note">합격자 평균 대비 <strong style="color:${r.me[0] >= r.pass[0] ? 'var(--green-500)' : 'var(--accent-red)'}">직무 전문성 ${r.me[0] >= r.pass[0] ? '+' : ''}${r.me[0] - r.pass[0]}</strong>, 압박 대응 <strong style="color:${r.me[4] >= r.pass[4] ? 'var(--green-500)' : 'var(--accent-red)'}">${r.me[4] - r.pass[4]}</strong></div>
        </div>
      </div>
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">역량별 비교</h3><span class="text-sm text-muted">나 · 합격자 평균 · AI 지원자 평균</span></div>
      <div class="cmp-list">
        ${compLabels.map((l, i) => `
          <div class="cmp-row">
            <div class="cmp-label">${l}</div>
            <div class="cmp-track">
              <div class="cmp-tick pass" style="left:${r.pass[i]}%" title="합격자 ${r.pass[i]}"></div>
              <div class="cmp-tick ai" style="left:${r.ai[i]}%" title="AI ${r.ai[i]}"></div>
              <div class="cmp-fill" style="width:${r.me[i]}%"></div>
            </div>
            <div class="cmp-val">${r.me[i]}</div>
          </div>`).join('')}
      </div>
    </div>
    <div class="card logic-card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">논리력 상세 분석</h3><span class="badge ${r.me[logicIdx] >= 80 ? 'badge-green' : 'badge-amber'}">${r.me[logicIdx]}점</span></div>
      <p class="rep-summary">${r.logic}</p>
      <div class="ai-comment"><div class="ai-comment-head"><span class="ai-badge">AI 코멘트</span></div><p>${r.aiComment}</p></div>
    </div>`
}

export function panelSpeech(r) {
  const s = r.speech, tone = s.tone
  return `
    <div class="rep-grid-2">
      <div class="card">
        <div class="card-header"><h3 class="card-title">발화 속도</h3></div>
        ${speedGauge(s.wpm, s.wpmStatus)}
        <div class="divider"></div>
        <div class="kv-row"><span>평균 답변 길이</span><strong>${s.avgLen}초</strong></div>
        <div class="kv-row"><span>답변 대기 시간</span><strong>${s.wait}초</strong></div>
      </div>
      <div class="card">
        <div class="card-header"><h3 class="card-title">목소리 톤 분석</h3></div>
        <div class="tone-list">
          ${toneBar('음높이 변화', tone.pitch, '단조로움', '풍부함')}
          ${toneBar('목소리 안정성', tone.stability, '떨림', '안정적')}
          ${toneBar('에너지 / 자신감', tone.energy, '낮음', '높음')}
        </div>
      </div>
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">필러(군더더기) 사용 분석</h3><span class="badge ${s.filler > 20 ? 'badge-red' : s.filler > 12 ? 'badge-amber' : 'badge-green'}">${s.filler > 20 ? '개선 필요' : s.filler > 12 ? '보통' : '양호'}</span></div>
      <div class="filler-total">총 <strong>${s.filler}회</strong> · 분당 ${(s.filler / 35 * 60 / 60).toFixed(1)}회</div>
      <div class="filler-list">
        ${fillerRow('음…', Math.round(s.filler * 0.4))}
        ${fillerRow('어…', Math.round(s.filler * 0.3))}
        ${fillerRow('그…', Math.round(s.filler * 0.18))}
        ${fillerRow('약간', Math.round(s.filler * 0.12))}
      </div>
      <p class="rep-hint">압박형 질문(Q3) 구간에서 필러 사용이 집중적으로 증가했습니다. 답변 전 1초 정지 후 시작하는 습관을 들이면 필러를 줄일 수 있습니다.</p>
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
      <div class="card-header"><h3 class="card-title">종합 인사이트</h3></div>
      <p class="rep-summary">이번 면접방에서 당신의 총점은 <strong>${r.score}점</strong>으로 ${r.applicants.filter((a) => !a.me && a.score < r.score).length}명의 AI 지원자보다 높았습니다. 특히 <strong>${r.applicants[0].strength}</strong>에서 두드러졌으나, <strong>${r.applicants[0].weak}</strong> 영역은 다른 지원자 대비 개선 여지가 있습니다.</p>
    </div>`
}

export function panelQuestions(r) {
  return `
    <div class="card">
      <div class="card-header"><h3 class="card-title">질문별 상세 피드백 (${r.questions.length}문항)</h3><span class="text-sm text-muted">평균 ${Math.round(r.questions.reduce((a, q) => a + q.score, 0) / r.questions.length)}점</span></div>
      <div class="q-list">
        ${r.questions.map((q) => `
          <div class="q-item open">
            <div class="q-head">
              <div><div class="q-num">${q.q}</div><div class="q-text">${q.text}</div></div>
              <div class="q-meta">${q.label ? `<span class="badge badge-${q.label[1]}">${q.label[0]}</span>` : ''}<span class="q-score" ${q.score < 70 ? 'style="color:var(--accent-red)"' : ''}>${q.score}</span></div>
            </div>
            <div class="q-body">${q.body}<div class="q-tag-row">${q.tags.map((t) => `<span class="badge badge-outline">${t}</span>`).join('')}</div></div>
          </div>`).join('')}
      </div>
    </div>`
}

export function reportPanel(r, tab) {
  if (tab === 'overview') return panelOverview(r)
  if (tab === 'competency') return panelCompetency(r)
  if (tab === 'speech') return panelSpeech(r)
  if (tab === 'applicants') return panelApplicants(r)
  return panelQuestions(r)
}

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

export function panelOverview(r) {
  const sc = r.score
  const scColor = sc >= 80 ? 'var(--green-500)' : sc >= 60 ? 'var(--accent-amber)' : 'var(--accent-red)'
  const scLabel = sc >= 90 ? '매우 우수' : sc >= 80 ? '합격권' : sc >= 70 ? '평균 이상' : sc >= 60 ? '보완 필요' : '개선 필요'
  const infoCard = r.info ? `
    <div class="card" style="margin-bottom:16px;">
      <div class="card-header"><h3 class="card-title">면접 정보</h3><span class="text-sm text-muted">${r.info.endedAt}</span></div>
      <div class="rep-grid-2" style="gap:0;">
        <div>
          <div class="kv-row"><span>회사</span><strong>${r.info.company}</strong></div>
          <div class="kv-row"><span>직무</span><strong>${r.info.job}</strong></div>
          <div class="kv-row"><span>지원 유형</span><strong>${r.info.type}</strong></div>
        </div>
        <div>
          <div class="kv-row"><span>난이도</span><strong>${r.info.difficulty}</strong></div>
          <div class="kv-row"><span>AI 면접관</span><strong>${r.info.interviewerCnt}명</strong></div>
          <div class="kv-row"><span>AI 지원자</span><strong>${r.info.applicantCnt}명</strong></div>
        </div>
      </div>
    </div>` : ''
  return `
    ${infoCard}
    <div class="rep-grid-2" style="margin-bottom:16px;">
      <div class="card" style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;padding:32px 16px;">
        <div style="font-size:13px;font-weight:600;color:var(--ink-500);">종합 점수</div>
        <div style="font-size:60px;font-weight:800;color:${scColor};line-height:1.1;">${sc}</div>
        <div style="font-size:13px;font-weight:700;color:${scColor};">${scLabel}</div>
      </div>
      <div class="card">
        <div class="card-header"><h3 class="card-title">면접 통계</h3></div>
        <div class="kv-row"><span>평균 답변 시간</span><strong>${r.speech.avgLen}초</strong></div>
        <div class="kv-row"><span>분당 어절 수 (WPM)</span><strong>${r.speechWpm != null ? r.speechWpm : '-'}</strong></div>
        <div class="kv-row"><span>추임새 횟수</span><strong>${r.speechFiller != null ? r.speechFiller + '회' : '-'}</strong></div>
      </div>
    </div>
    <div class="card">
      <div class="card-header"><h3 class="card-title">총평 요약</h3></div>
      <p class="rep-summary">${r.aiComment}</p>
    </div>
    <div class="card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">다음 면접 체크리스트</h3><span class="text-sm text-muted">다음 면접 전 꼭 준비하세요</span></div>
      ${r.checklist && r.checklist.length ? `
      <ul style="list-style:none;margin:0;padding:0;display:flex;flex-direction:column;gap:10px;">
        ${r.checklist.map(item => `
          <li style="display:flex;align-items:flex-start;gap:10px;font-size:14px;line-height:1.6;color:var(--ink-700);">
            <span style="flex-shrink:0;margin-top:1px;color:var(--green-500);font-size:16px;">☑</span>
            <span>${item}</span>
          </li>`).join('')}
      </ul>` : `<p style="color:var(--ink-400);font-size:13px;margin:0;">없음</p>`}
    </div>
  `
}

export function panelCompetency(r) {
  const logicIdx = 1
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
    <div class="card logic-card" style="margin-top:16px;">
      <div class="card-header"><h3 class="card-title">논리력 상세 분석</h3><span class="badge ${r.me[logicIdx] >= 80 ? 'badge-green' : 'badge-amber'}">${r.me[logicIdx]}점</span></div>
      <p class="rep-summary">${r.logic}</p>
      <div class="ai-comment"><div class="ai-comment-head"><span class="ai-badge">AI 코멘트</span></div><p>${r.aiComment}</p></div>
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

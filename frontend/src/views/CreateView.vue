<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { useFlowStore } from '../stores/flow.js'

const router = useRouter()
const data = useDataStore()
const flow = useFlowStore()

const step = ref(1)
const company = ref('카카오')
const role = ref('백엔드 개발')
const sel = reactive({ type: '신입', diff: '중 · 실제 면접 수준', interviewers: '2명', applicants: '2명' })

const typeOpts = ['신입', '경력', '인턴']
const diffOpts = ['하 · 편안한 분위기', '중 · 실제 면접 수준', '상 · 압박 면접']
const countOpts = ['1명', '2명', '3명']

function gotoStep(n) { step.value = n; window.scrollTo(0, 0) }

// 생성 → 폼 값으로 임시 방을 만들고 면접 진행
function createAndStart() {
  const diffShort = sel.diff.charAt(0)
  const logoMap = { 카카오: 'kakao', 삼성전자: 'samsung', 네이버: 'naver', 토스: 'toss' }
  data.rooms.push({
    co: company.value, logo: logoMap[company.value] || 'lounge', short: company.value.charAt(0),
    role: `${role.value} · ${sel.type}`, title: `${company.value} ${role.value} 맞춤형 다대다 면접`,
    diff: diffShort, date: '26.05.22', count: 0, sessions: 0, mine: false,
  })
  flow.currentRoom = data.rooms.length - 1
  router.push('/interview')
}
</script>

<template>
  <main class="page active" id="page-create">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 면접 <span class="sep">›</span> 맞춤형 면접방 생성</div>
        <h1 class="page-title">맞춤형 면접방 생성</h1>
        <p class="page-subtitle">2단계로 빠르게 나만의 다대다 모의 면접방을 만들어보세요.</p>
      </div>

      <!-- Stepper -->
      <div class="stepper">
        <div class="step" :class="{ active: step === 1, done: step === 2 }">
          <div class="step-num">1</div>
          <div><div class="step-label">STEP 1</div><div class="step-title">면접방 설정</div></div>
        </div>
        <div class="step" :class="{ active: step === 2 }">
          <div class="step-num">2</div>
          <div><div class="step-label">STEP 2</div><div class="step-title">응시환경 체크 &amp; 생성</div></div>
        </div>
      </div>

      <!-- STEP 1 -->
      <div v-show="step === 1">
        <div class="create-grid">
          <div>
            <div class="form-section">
              <div class="form-section-head"><h3 class="form-section-title">기본 정보</h3><p class="form-section-sub">어떤 회사와 직무로 면접을 진행할까요?</p></div>
              <div class="form-section-body">
                <div class="form-row">
                  <div class="field">
                    <label class="field-label">회사명 <span class="req">*</span></label>
                    <input class="input" v-model="company" placeholder="예) 카카오, 삼성전자 DS부문" />
                    <div class="field-hint">100대기업 리스트에서 검색해 자동완성 가능</div>
                  </div>
                  <div class="field">
                    <label class="field-label">직무 선택 <span class="req">*</span></label>
                    <select class="select" v-model="role">
                      <option>백엔드 개발</option><option>프론트엔드 개발</option><option>데이터 사이언티스트</option>
                      <option>iOS / Android 개발</option><option>인프라 · DevOps</option><option>기획 / PO</option>
                    </select>
                  </div>
                </div>
                <div class="field">
                  <label class="field-label">유형 선택 <span class="req">*</span></label>
                  <div class="chip-group">
                    <div v-for="o in typeOpts" :key="o" class="chip" :class="{ active: sel.type === o }" @click="sel.type = o">{{ o }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-section">
              <div class="form-section-head"><h3 class="form-section-title">면접 환경 설정</h3><p class="form-section-sub">난이도와 참여 인원수에 따라 분위기와 질문 강도가 바뀝니다.</p></div>
              <div class="form-section-body">
                <div class="field">
                  <label class="field-label">난이도</label>
                  <div class="chip-group">
                    <div v-for="o in diffOpts" :key="o" class="chip" :class="{ active: sel.diff === o }" @click="sel.diff = o">{{ o }}</div>
                  </div>
                </div>
                <div class="form-row">
                  <div class="field">
                    <label class="field-label">면접관 수</label>
                    <div class="chip-group">
                      <div v-for="o in countOpts" :key="o" class="chip" :class="{ active: sel.interviewers === o }" @click="sel.interviewers = o">{{ o }}</div>
                    </div>
                  </div>
                  <div class="field">
                    <label class="field-label">AI 경쟁 지원자 수</label>
                    <div class="chip-group">
                      <div v-for="o in countOpts" :key="o" class="chip" :class="{ active: sel.applicants === o }" @click="sel.applicants = o">{{ o }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-section">
              <div class="form-section-head"><h3 class="form-section-title">제출 서류</h3><p class="form-section-sub">업로드한 서류 기반으로 면접관이 맞춤형 질문을 생성합니다.</p></div>
              <div class="form-section-body">
                <div class="upload-grid">
                  <div class="upload uploaded">
                    <div class="upload-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg></div>
                    <div><div class="upload-title">이력서_김지원_v4.pdf</div><div class="upload-sub">324 KB · 마이페이지에서 자동 연동됨</div></div>
                  </div>
                  <div class="upload">
                    <div class="upload-icon" style="margin: 0 auto 8px;"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg></div>
                    <div class="upload-title">자기소개서</div><div class="upload-sub">PDF · DOC · 최대 10MB</div>
                  </div>
                  <div class="upload">
                    <div class="upload-icon" style="margin: 0 auto 8px;"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg></div>
                    <div class="upload-title">포트폴리오</div><div class="upload-sub">PDF · ZIP · URL · 최대 50MB</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <aside class="preview-panel">
            <div class="preview-head"><h3 class="preview-title">설정 미리보기</h3><p class="preview-sub">변경할 때마다 실시간으로 반영됩니다.</p></div>
            <div class="preview-body">
              <div class="preview-row"><div class="preview-row-label">회사</div><div class="preview-row-val">{{ company || '-' }}</div></div>
              <div class="preview-row"><div class="preview-row-label">직무</div><div class="preview-row-val">{{ role }}</div></div>
              <div class="preview-row"><div class="preview-row-label">유형</div><div class="preview-row-val">{{ sel.type }}</div></div>
              <div class="preview-row"><div class="preview-row-label">난이도</div><div class="preview-row-val">{{ sel.diff }}</div></div>
              <div class="preview-row"><div class="preview-row-label">면접관 수</div><div class="preview-row-val">{{ sel.interviewers }}</div></div>
              <div class="preview-row"><div class="preview-row-label">AI 경쟁 지원자</div><div class="preview-row-val">{{ sel.applicants }}</div></div>
              <div class="preview-row"><div class="preview-row-label">제출 서류</div><div class="preview-row-val">이력서 1건</div></div>
              <div class="preview-row"><div class="preview-row-label">예상 소요 시간</div><div class="preview-row-val">25 ~ 35분</div></div>
            </div>
            <div class="preview-foot">
              <div class="text-sm text-muted" style="margin-bottom: 10px;">총 6명 · 면접관 2 + 본인 + AI 지원자 2</div>
              <button class="btn btn-secondary btn-block" disabled style="opacity:0.6">다음 단계에서 생성</button>
            </div>
          </aside>
        </div>

        <div class="create-actions">
          <button class="btn btn-ghost" @click="router.push('/')">취소</button>
          <button class="btn btn-primary btn-lg" @click="gotoStep(2)">
            다음 단계
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
        </div>
      </div>

      <!-- STEP 2 -->
      <div v-show="step === 2">
        <div class="check-layout">
          <div>
            <div class="form-section">
              <div class="form-section-head"><h3 class="form-section-title">응시 환경 체크</h3><p class="form-section-sub">카메라와 마이크가 정상적으로 작동하는지 확인하세요.</p></div>
              <div class="form-section-body" style="padding-bottom: 22px;">
                <div class="check-screen">
                  <div class="check-cam">
                    <div class="you-circle">김</div>
                    <div class="check-overlay"><span class="live-dot"></span>카메라 미리보기</div>
                  </div>
                  <div class="check-controls">
                    <div class="check-control" title="마이크 음소거"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" /><path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v3M8 22h8" /></svg></div>
                    <div class="check-control" title="카메라 끄기"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m23 7-7 5 7 5z" /><rect x="1" y="5" width="15" height="14" rx="2" /></svg></div>
                    <div class="check-control" title="설정"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h0a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v0a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" /></svg></div>
                  </div>
                </div>

                <div class="check-list">
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body"><div class="check-item-title">카메라 — FaceTime HD Camera</div><div class="check-item-sub">1280 × 720 · 30 fps</div></div>
                    <span class="check-item-status"><span class="badge-dot" style="background: var(--green-500)"></span>정상</span>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body"><div class="check-item-title">마이크 — MacBook Pro 마이크</div><div class="check-item-sub">입력 레벨: <span style="color: var(--green-500)">●●●●●●●●</span><span style="color: var(--ink-300)">○○</span></div></div>
                    <span class="check-item-status"><span class="badge-dot" style="background: var(--green-500)"></span>정상</span>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body"><div class="check-item-title">스피커 — MacBook Pro 스피커</div><div class="check-item-sub">테스트 사운드를 재생해보세요</div></div>
                    <button class="btn btn-sm btn-secondary">테스트</button>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body"><div class="check-item-title">네트워크 — 안정적</div><div class="check-item-sub">상향 38 Mbps · 하향 124 Mbps · Ping 12ms</div></div>
                    <span class="check-item-status"><span class="badge-dot" style="background: var(--green-500)"></span>정상</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <aside class="preview-panel">
            <div class="preview-head"><h3 class="preview-title">면접방 설정 미리보기</h3><p class="preview-sub">생성 후에는 일부 항목만 수정 가능합니다.</p></div>
            <div class="preview-body">
              <div class="preview-row"><div class="preview-row-label">회사</div><div class="preview-row-val">{{ company || '-' }}</div></div>
              <div class="preview-row"><div class="preview-row-label">직무</div><div class="preview-row-val">{{ role }}</div></div>
              <div class="preview-row"><div class="preview-row-label">유형</div><div class="preview-row-val">{{ sel.type }}</div></div>
              <div class="preview-row"><div class="preview-row-label">난이도</div><div class="preview-row-val">{{ sel.diff }}</div></div>
              <div class="preview-row"><div class="preview-row-label">면접관 수</div><div class="preview-row-val">{{ sel.interviewers }}</div></div>
              <div class="preview-row"><div class="preview-row-label">AI 경쟁 지원자</div><div class="preview-row-val">{{ sel.applicants }}</div></div>
              <div class="preview-row"><div class="preview-row-label">제출 서류</div><div class="preview-row-val">이력서 1건</div></div>
              <div class="preview-row"><div class="preview-row-label">예상 소요 시간</div><div class="preview-row-val">25 ~ 35분</div></div>
            </div>
            <div class="preview-foot">
              <button class="btn btn-primary btn-block btn-lg" @click="createAndStart">면접방 생성하고 시작하기</button>
              <button class="btn btn-ghost btn-block" style="margin-top: 6px;" @click="gotoStep(1)">이전 단계로</button>
            </div>
          </aside>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useFlowStore } from '../stores/flow.js'
import { useDataStore } from '../stores/data.js'

const router = useRouter()
const flow = useFlowStore()
const data = useDataStore()

const r = computed(() => data.rooms[flow.currentRoom])
const roomTitle = computed(() => r.value ? `${r.value.co} · ${r.value.role} 다대다 면접` : '카카오 · 백엔드 개발 신입 다대다 면접')
const roomSub = computed(() => r.value ? `난이도 ${r.value.diff} · 면접관 2명 · 지원자 3명 (AI 2 + 본인)` : '난이도 중 · 면접관 2명 · 지원자 3명 (AI 2 + 본인)')

// 원본: 마이크 음소거 상태 (a1, a2 초기 muted)
const muted = reactive({ i1: false, i2: false, a1: true, me: false, a2: true })
const logHidden = ref(false)

// 원본 speakingOrder 사이클
const speakingOrder = ['i2', 'me', 'i1', 'a1', 'me', 'i2', 'a2']
const speakingIdx = ref(0)
const speakingId = computed(() => speakingOrder[speakingIdx.value])

function isSpeaking(id) { return speakingId.value === id }
function micActive(id) { return speakingId.value === id && !muted[id] }

// 타이머
const elapsed = ref(18 * 60 + 42)
const timeText = computed(() => {
  const h = String(Math.floor(elapsed.value / 3600)).padStart(2, '0')
  const m = String(Math.floor((elapsed.value % 3600) / 60)).padStart(2, '0')
  const s = String(elapsed.value % 60).padStart(2, '0')
  return `${h}:${m}:${s}`
})

let speakTimer = null
let clockTimer = null
onMounted(() => {
  speakTimer = setInterval(() => { speakingIdx.value = (speakingIdx.value + 1) % speakingOrder.length }, 3200)
  clockTimer = setInterval(() => { elapsed.value++ }, 1000)
})
onUnmounted(() => { clearInterval(speakTimer); clearInterval(clockTimer) })

function toggleLog() { logHidden.value = !logHidden.value }
function toggleMute() { muted.me = !muted.me }
function endInterview() { router.push('/saving') }

const micPath = 'M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z'
const micBase = 'M19 10v2a7 7 0 0 1-14 0v-2M12 19v3'
</script>

<template>
  <main class="page active interview-page" id="page-interview">
    <div class="iv-topbar">
      <div class="iv-room-name">
        <span class="live">LIVE</span>
        <div>
          <div class="iv-room-title">{{ roomTitle }}</div>
          <div class="iv-room-sub">{{ roomSub }}</div>
        </div>
      </div>
      <div class="iv-timer">
        <div><div class="iv-timer-label">진행 시간</div><div class="iv-timer-time">{{ timeText }}</div></div>
      </div>
      <div class="iv-topbar-actions">
        <button class="iv-icon-btn" title="화면 공유"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" /><path d="M8 21h8M12 17v4" /></svg></button>
        <button class="iv-icon-btn" title="참여자 목록"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></svg></button>
        <button class="iv-icon-btn" title="설정"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h0a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v0a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" /></svg></button>
      </div>
    </div>

    <div class="iv-body">
      <aside class="iv-log" :class="{ hidden: logHidden }">
        <div class="iv-log-head">
          <div class="iv-log-title">면접 진행 로그</div>
          <div class="iv-log-toggle" @click="toggleLog">{{ logHidden ? '› 펼치기' : '숨기기 ›' }}</div>
        </div>
        <div class="iv-log-list">
          <div class="iv-log-item"><div class="iv-log-time">00:00</div><div class="iv-log-body"><div class="iv-log-who interviewer">면접관 박</div><div class="iv-log-action">면접 시작 · 자기소개 요청</div><span class="iv-log-tag">PHASE · OPENING</span></div></div>
          <div class="iv-log-item"><div class="iv-log-time">01:24</div><div class="iv-log-body"><div class="iv-log-who">AI 지원자 1</div><div class="iv-log-action">자기소개 답변 (52초)</div></div></div>
          <div class="iv-log-item"><div class="iv-log-time">02:18</div><div class="iv-log-body"><div class="iv-log-who you">나 (김지원)</div><div class="iv-log-action">자기소개 답변 (1분 04초)</div><span class="iv-log-tag">VOICE · CLEAR</span></div></div>
          <div class="iv-log-item"><div class="iv-log-time">03:34</div><div class="iv-log-body"><div class="iv-log-who">AI 지원자 2</div><div class="iv-log-action">자기소개 답변 (48초)</div></div></div>
          <div class="iv-log-item"><div class="iv-log-time">04:30</div><div class="iv-log-body"><div class="iv-log-who interviewer">면접관 이</div><div class="iv-log-action">Q2 · 기술 경험에 대한 공통 질문</div><span class="iv-log-tag">PHASE · TECH</span></div></div>
          <div class="iv-log-item"><div class="iv-log-time">07:12</div><div class="iv-log-body"><div class="iv-log-who you">나 (김지원)</div><div class="iv-log-action">Spring Batch 운영 경험 답변</div><span class="iv-log-tag">KEYWORD · BATCH, KAFKA</span></div></div>
          <div class="iv-log-item"><div class="iv-log-time">11:45</div><div class="iv-log-body"><div class="iv-log-who interviewer">면접관 박</div><div class="iv-log-action">Q3 · 압박 질문 (장애 대응 경험)</div><span class="iv-log-tag">PHASE · PRESSURE</span></div></div>
          <div class="iv-log-item"><div class="iv-log-time">15:20</div><div class="iv-log-body"><div class="iv-log-who interviewer">면접관 이</div><div class="iv-log-action">현재 발화 중 · Q4 시작</div><span class="iv-log-tag">SPEAKING NOW</span></div></div>
        </div>
      </aside>

      <div class="iv-stage">
        <div class="iv-room-floor">
          <div class="iv-zone-label iv-zone-top"><span class="l"></span>INTERVIEWERS · 면접관<span class="l"></span></div>
          <div class="iv-zone">
            <div class="iv-tile" :class="{ speaking: isSpeaking('i1') }" data-id="i1">
              <div class="ripple"></div>
              <div class="iv-tile-avatar i1">박</div>
              <span class="iv-tile-tag ai">AI · INTERVIEWER</span>
              <div class="iv-tile-name"><span class="role-dot interviewer"></span>면접관 박</div>
              <div class="iv-tile-mic" :class="{ active: micActive('i1'), muted: muted.i1 }"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path :d="micPath" /><path :d="micBase" /></svg></div>
              <div class="iv-mic-wave"><span></span><span></span><span></span><span></span></div>
            </div>
            <div class="iv-tile" :class="{ speaking: isSpeaking('i2') }" data-id="i2">
              <div class="ripple"></div>
              <div class="iv-tile-avatar i2">이</div>
              <span class="iv-tile-tag ai">AI · INTERVIEWER</span>
              <div class="iv-tile-name"><span class="role-dot interviewer"></span>면접관 이</div>
              <div class="iv-tile-mic" :class="{ active: micActive('i2'), muted: muted.i2 }"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path :d="micPath" /><path :d="micBase" /></svg></div>
              <div class="iv-mic-wave"><span></span><span></span><span></span><span></span></div>
            </div>
          </div>

          <div class="iv-table"></div>

          <div class="iv-zone">
            <div class="iv-tile" :class="{ speaking: isSpeaking('a1') }" data-id="a1">
              <div class="ripple"></div>
              <div class="iv-tile-avatar a1">A</div>
              <span class="iv-tile-tag ai">AI · APPLICANT</span>
              <div class="iv-tile-name"><span class="role-dot applicant"></span>AI 지원자 1</div>
              <div class="iv-tile-mic" :class="{ active: micActive('a1'), muted: muted.a1 }"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 1l22 22M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V5a3 3 0 0 0-5.94-.6" /><path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23" /></svg></div>
              <div class="iv-mic-wave"><span></span><span></span><span></span><span></span></div>
            </div>
            <div class="iv-tile you" :class="{ speaking: isSpeaking('me') }" data-id="me">
              <div class="ripple"></div>
              <div class="iv-tile-avatar">김</div>
              <span class="iv-tile-tag you">YOU · 본인</span>
              <div class="iv-tile-name"><span class="role-dot you"></span>김지원 (나)</div>
              <div class="iv-tile-mic" :class="{ active: micActive('me'), muted: muted.me }"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path :d="micPath" /><path :d="micBase" /></svg></div>
              <div class="iv-mic-wave"><span></span><span></span><span></span><span></span></div>
            </div>
            <div class="iv-tile" :class="{ speaking: isSpeaking('a2') }" data-id="a2">
              <div class="ripple"></div>
              <div class="iv-tile-avatar a2">B</div>
              <span class="iv-tile-tag ai">AI · APPLICANT</span>
              <div class="iv-tile-name"><span class="role-dot applicant"></span>AI 지원자 2</div>
              <div class="iv-tile-mic" :class="{ active: micActive('a2'), muted: muted.a2 }"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 1l22 22M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V5a3 3 0 0 0-5.94-.6" /><path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23" /></svg></div>
              <div class="iv-mic-wave"><span></span><span></span><span></span><span></span></div>
            </div>
          </div>

          <div class="iv-zone-label iv-zone-bottom"><span class="l"></span>APPLICANTS · 지원자<span class="l"></span></div>
        </div>
      </div>
    </div>

    <div class="iv-controls">
      <div class="iv-controls-left">
        <div class="iv-q-indicator"><span class="q-label">Question</span><span class="q-num">04 / 08</span></div>
        <span>· 기술 라운드</span>
      </div>
      <div class="iv-controls-center">
        <button class="iv-ctl" :class="{ muted: muted.me }" title="마이크" @click="toggleMute">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" /><path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v3M8 22h8" /></svg>
        </button>
        <button class="iv-ctl" title="카메라"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m23 7-7 5 7 5z" /><rect x="1" y="5" width="15" height="14" rx="2" /></svg></button>
        <button class="iv-ctl" title="화면 공유"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" /><path d="M8 21h8M12 17v4" /></svg></button>
        <button class="iv-ctl" title="손들기"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 11V6a2 2 0 0 0-2-2v0a2 2 0 0 0-2 2v0M14 10V4a2 2 0 0 0-2-2v0a2 2 0 0 0-2 2v2M10 10.5V6a2 2 0 0 0-2-2v0a2 2 0 0 0-2 2v8" /><path d="M18 8a2 2 0 1 1 4 0v6a8 8 0 0 1-8 8h-2c-2.8 0-4.5-.86-5.99-2.34l-3.6-3.6a2 2 0 0 1 2.83-2.82L7 15" /></svg></button>
        <button class="iv-ctl leave" @click="endInterview">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" /></svg>
          면접 종료
        </button>
      </div>
      <div class="iv-controls-right">
        <span style="font-size:12px; color:#7a8a82;">자동 녹화 중</span>
        <span style="width:6px; height:6px; border-radius:50%; background:#ff4444; animation:blink 1s infinite;"></span>
      </div>
    </div>
  </main>
</template>

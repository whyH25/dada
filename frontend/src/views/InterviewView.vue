<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useFlowStore } from '../stores/flow.js'
import { useDataStore } from '../stores/data.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const flow = useFlowStore()
const data = useDataStore()
const auth = useAuthStore()

const r = computed(() => data.rooms[flow.currentRoom])
const roomTitle = computed(() =>
  r.value ? `${r.value.co} | ${r.value.role.replace(/·/g, '|')}` : '면접 진행 중'
)

// ── 참가자 (면접방 설정 기반 동적 생성) ──────────────
const interviewerCount = computed(() => r.value?.interviewerCount || 2)
const aiApplicantCount = computed(() => r.value?.aiApplicantCount ?? 1)

const interviewerVideos = ref([])
const applicantImages = ref([])

function pickRandom(count, total, pathFn) {
  const indices = Array.from({ length: total }, (_, i) => i + 1)
  for (let i = indices.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [indices[i], indices[j]] = [indices[j], indices[i]]
  }
  return indices.slice(0, Math.min(count, total)).map(pathFn)
}

function pickRandomVideos(count) {
  return pickRandom(count, 10, n => `/interviewers/interviewer_${String(n).padStart(2, '0')}.mp4`)
}

function pickRandomApplicants(count) {
  return pickRandom(count, 10, n => `/applicants/applicant_${String(n).padStart(2, '0')}.png`)
}

const interviewers = computed(() =>
  Array.from({ length: interviewerCount.value }, (_, i) => ({
    id: `i${i + 1}`,
    name: interviewerCount.value === 1 ? '면접관' : `면접관 ${i + 1}`,
    letter: ['박', '이', '김', '최'][i] ?? String(i + 1),
    video: interviewerVideos.value[i] ?? null,
  }))
)

const aiApplicants = computed(() =>
  Array.from({ length: aiApplicantCount.value }, (_, i) => ({
    id: `a${i + 1}`,
    name: aiApplicantCount.value === 1 ? 'AI 지원자' : `AI 지원자 ${i + 1}`,
    letter: String.fromCharCode(65 + i),
    image: applicantImages.value[i] ?? null,
  }))
)

const userName = computed(() => auth.user?.userName || '나')
const userInitial = computed(() => userName.value.charAt(0))

// ── 발화 감지 ─────────────────────────────────────────
const isSpeakingMe = ref(false)
function isSpeaking(id) {
  if (id === 'me') return isSpeakingMe.value
  return false  // 나중에 실제 오디오로 대체
}

let audioCtx = null
let audioAnalyser = null
let audioInterval = null

function startAudioDetection(stream) {
  try {
    audioCtx = new AudioContext()
    audioAnalyser = audioCtx.createAnalyser()
    audioAnalyser.fftSize = 256
    audioCtx.createMediaStreamSource(stream).connect(audioAnalyser)
    const buf = new Uint8Array(audioAnalyser.frequencyBinCount)
    audioInterval = setInterval(() => {
      if (muted.value) { isSpeakingMe.value = false; return }
      audioAnalyser.getByteFrequencyData(buf)
      const avg = buf.reduce((s, v) => s + v, 0) / buf.length
      isSpeakingMe.value = avg > 12
    }, 80)
  } catch { /* AudioContext 없는 환경 */ }
}

function stopAudioDetection() {
  clearInterval(audioInterval)
  audioCtx?.close().catch(() => {})
  audioCtx = null; audioAnalyser = null; audioInterval = null
}

// ── 타이머 ──────────────────────────────────────────
const elapsed = ref(0)
const timeText = computed(() => {
  const h = String(Math.floor(elapsed.value / 3600)).padStart(2, '0')
  const m = String(Math.floor((elapsed.value % 3600) / 60)).padStart(2, '0')
  const s = String(elapsed.value % 60).padStart(2, '0')
  return `${h}:${m}:${s}`
})

// ── 미디어 상태 ──────────────────────────────────────
const muted = ref(false)
const camOff = ref(false)
const screenSharing = ref(false)

const myStream = ref(null)
const screenStream = ref(null)

// 비디오 엘리먼트 ref (v-if/v-else로 교체됨 → watch로 srcObject 할당)
const myVideoEl = ref(null)     // 일반 모드 "나" 타일
const thumbVideoEl = ref(null)  // 화면공유 모드 "나" 썸네일
const screenVideoEl = ref(null) // 화면공유 영상

watch(myVideoEl, (el) => { if (el && myStream.value) el.srcObject = myStream.value })
watch(thumbVideoEl, (el) => { if (el && myStream.value) el.srcObject = myStream.value })
watch(screenVideoEl, (el) => { if (el && screenStream.value) el.srcObject = screenStream.value })

// 카메라 + 마이크 실제 시작
async function startMyMedia() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
    myStream.value = stream
    if (myVideoEl.value) myVideoEl.value.srcObject = stream
    startAudioDetection(stream)
  } catch {
    // 권한 없거나 장치 없음 → 아바타로 표시
  }
}

// 음소거 토글 (실제 오디오 트랙 제어)
function toggleMute() {
  muted.value = !muted.value
  myStream.value?.getAudioTracks().forEach(t => { t.enabled = !muted.value })
}

// 카메라 토글 (실제 비디오 트랙 제어)
function toggleCam() {
  camOff.value = !camOff.value
  myStream.value?.getVideoTracks().forEach(t => { t.enabled = !camOff.value })
}

// 화면 공유 (브라우저 기본 선택 다이얼로그 사용)
async function toggleScreenShare() {
  if (screenSharing.value) {
    screenStream.value?.getTracks().forEach(t => t.stop())
    screenStream.value = null
    screenSharing.value = false
    return
  }
  try {
    const stream = await navigator.mediaDevices.getDisplayMedia({ video: { cursor: 'always' } })
    screenStream.value = stream
    screenSharing.value = true
    // 브라우저 "공유 중지" 버튼으로 종료 시
    stream.getVideoTracks()[0].addEventListener('ended', () => {
      screenSharing.value = false
      screenStream.value = null
    })
  } catch {
    // 사용자 취소
  }
}

// ── 가상 배경 ──────────────────────────────────────────
const bgPickerOpen = ref(false)
const activeBgId = ref('none')
const bgCanvasEl = ref(null)

const BG_OPTIONS = [
  { id: 'none',     label: '없음',   style: null },
  { id: 'blur',     label: '흐림',   style: 'background:linear-gradient(135deg,#cbd5e1,#94a3b8)' },
  { id: 'white',    label: '흰색',   style: 'background:#f8f9fa' },
  { id: 'sky',      label: '하늘',   style: 'background:#bfdbfe' },
  { id: 'mint',     label: '민트',   style: 'background:#bbf7d0' },
  { id: 'pink',     label: '핑크',   style: 'background:#fbcfe8' },
  { id: 'lavender', label: '라벤더', style: 'background:#ddd6fe' },
  { id: 'custom',   label: '이미지', style: null },
]
const BG_FILLS = { white: '#f8f9fa', sky: '#bfdbfe', mint: '#bbf7d0', pink: '#fbcfe8', lavender: '#ddd6fe' }

const customBgImage = ref(null)   // HTMLImageElement
const customBgUrl = ref(null)     // object URL for preview + cleanup
const customBgInputEl = ref(null) // hidden <input type="file">

function handleCustomBgFile(e) {
  const file = e.target.files[0]
  if (!file) return
  if (customBgUrl.value) URL.revokeObjectURL(customBgUrl.value)
  const url = URL.createObjectURL(file)
  customBgUrl.value = url
  const img = new Image()
  img.onload = async () => {
    customBgImage.value = img
    activeBgId.value = 'custom'
    if (!myStream.value) return
    try {
      await initSegmentation()
      await nextTick()
      startBgLoop()
    } catch { activeBgId.value = 'none' }
  }
  img.src = url
  e.target.value = ''
}

let selfieSegmentation = null
let bgAnimFrame = null
let offCanvas = null
let offCtx = null
let bgCtx = null

function loadSegScript() {
  return new Promise((resolve, reject) => {
    if (window.SelfieSegmentation) { resolve(); return }
    const s = document.createElement('script')
    s.src = 'https://cdn.jsdelivr.net/npm/@mediapipe/selfie_segmentation/selfie_segmentation.js'
    s.crossOrigin = 'anonymous'
    s.onload = resolve; s.onerror = reject
    document.head.appendChild(s)
  })
}

async function initSegmentation() {
  if (selfieSegmentation) return
  await loadSegScript()
  selfieSegmentation = new window.SelfieSegmentation({
    locateFile: f => `https://cdn.jsdelivr.net/npm/@mediapipe/selfie_segmentation/${f}`
  })
  selfieSegmentation.setOptions({ modelSelection: 1, selfieMode: false })
  selfieSegmentation.onResults(({ segmentationMask, image }) => {
    const canvas = bgCanvasEl.value
    if (!canvas || !bgCtx) return
    const w = canvas.width, h = canvas.height

    if (!offCanvas) { offCanvas = document.createElement('canvas'); offCtx = offCanvas.getContext('2d') }
    offCanvas.width = w; offCanvas.height = h
    offCtx.clearRect(0, 0, w, h)
    offCtx.drawImage(image, 0, 0, w, h)
    offCtx.globalCompositeOperation = 'destination-in'
    offCtx.drawImage(segmentationMask, 0, 0, w, h)
    offCtx.globalCompositeOperation = 'source-over'

    bgCtx.save()
    bgCtx.clearRect(0, 0, w, h)
    if (activeBgId.value === 'blur') {
      bgCtx.filter = 'blur(18px)'
      bgCtx.drawImage(image, -20, -20, w + 40, h + 40)
      bgCtx.filter = 'none'
    } else if (activeBgId.value === 'custom' && customBgImage.value) {
      const img = customBgImage.value
      const scale = Math.max(w / img.naturalWidth, h / img.naturalHeight)
      const sw = img.naturalWidth * scale, sh = img.naturalHeight * scale
      bgCtx.drawImage(img, (w - sw) / 2, (h - sh) / 2, sw, sh)
    } else {
      bgCtx.fillStyle = BG_FILLS[activeBgId.value] ?? '#f8f9fa'
      bgCtx.fillRect(0, 0, w, h)
    }
    bgCtx.drawImage(offCanvas, 0, 0)
    bgCtx.restore()
  })
  await selfieSegmentation.initialize()
}

function startBgLoop() {
  const videoEl = myVideoEl.value
  const canvas = bgCanvasEl.value
  if (!videoEl || !canvas) return
  canvas.width = videoEl.videoWidth || 640
  canvas.height = videoEl.videoHeight || 480
  bgCtx = canvas.getContext('2d')
  const loop = async () => {
    if (activeBgId.value === 'none') return
    if (videoEl.readyState >= 2) {
      try { await selfieSegmentation.send({ image: videoEl }) } catch {}
    }
    bgAnimFrame = requestAnimationFrame(loop)
  }
  bgAnimFrame = requestAnimationFrame(loop)
}

function stopBgLoop() {
  cancelAnimationFrame(bgAnimFrame)
  bgAnimFrame = null; bgCtx = null
}

async function applyBg(id) {
  bgPickerOpen.value = false
  if (id === 'custom') {
    // 이미 이미지가 없으면 파일 탐색기 열기
    if (!customBgImage.value) { customBgInputEl.value?.click(); return }
    // 이미지 있으면 그냥 활성화
  }
  activeBgId.value = id
  if (id === 'none') { stopBgLoop(); return }
  if (!myStream.value) return
  try {
    await initSegmentation()
    await nextTick()
    startBgLoop()
  } catch { activeBgId.value = 'none' }
}

function openCustomBgPicker() {
  customBgInputEl.value?.click()
}

function endInterview() {
  stopAudioDetection()
  stopBgLoop()
  if (customBgUrl.value) { URL.revokeObjectURL(customBgUrl.value); customBgUrl.value = null }
  myStream.value?.getTracks().forEach(t => t.stop())
  screenStream.value?.getTracks().forEach(t => t.stop())
  router.push('/saving')
}

let clockTimer = null
onMounted(async () => {
  interviewerVideos.value = pickRandomVideos(interviewerCount.value)
  applicantImages.value = pickRandomApplicants(aiApplicantCount.value)
  clockTimer = setInterval(() => { elapsed.value++ }, 1000)
  await startMyMedia()
})
onUnmounted(() => {
  clearInterval(clockTimer)
  stopAudioDetection()
  stopBgLoop()
  if (customBgUrl.value) URL.revokeObjectURL(customBgUrl.value)
  myStream.value?.getTracks().forEach(t => t.stop())
  screenStream.value?.getTracks().forEach(t => t.stop())
})
</script>

<template>
  <div class="iv-page">

    <!-- 상단 바: 진행시간 중앙 -->
    <div class="iv-top">
      <div class="iv-top-left">
        <span class="live-badge">LIVE</span>
        <span class="iv-top-title">{{ roomTitle }}</span>
      </div>
      <div class="iv-top-timer">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
        </svg>
        {{ timeText }}
      </div>
      <div class="iv-top-right">
        <span class="rec-indicator"><span class="rec-dot"></span>REC</span>
      </div>
    </div>

    <!-- 메인 영역 -->
    <div class="iv-main">

      <!-- ── 화면 공유 모드 ── -->
      <template v-if="screenSharing">
        <div class="iv-screen-area">
          <video ref="screenVideoEl" autoplay muted playsinline class="iv-screen-video"></video>
          <button class="iv-screen-stop" @click="toggleScreenShare">공유 중지 ✕</button>
        </div>
        <div class="iv-thumb-strip">
          <div v-for="(iv, idx) in interviewers" :key="iv.id"
               class="iv-thumb" :class="{ speaking: isSpeaking(iv.id) }">
            <video v-if="iv.video" :src="iv.video" autoplay loop muted playsinline
                   class="iv-thumb-video iv-thumb-interviewer"></video>
            <div v-else class="iv-thumb-av" :class="`av-i${idx + 1}`">{{ iv.letter }}</div>
            <div class="iv-thumb-name">{{ iv.name }}</div>
          </div>
          <div v-for="ap in aiApplicants" :key="ap.id"
               class="iv-thumb" :class="{ speaking: isSpeaking(ap.id) }">
            <img v-if="ap.image" :src="ap.image" class="iv-thumb-video" />
            <div v-else class="iv-thumb-av av-appli">{{ ap.letter }}</div>
            <div class="iv-thumb-name">{{ ap.name }}</div>
          </div>
          <div class="iv-thumb" :class="{ speaking: isSpeaking('me') }">
            <video ref="thumbVideoEl" v-if="!camOff && myStream"
                   autoplay muted playsinline class="iv-thumb-video"></video>
            <div v-else class="iv-thumb-av av-me">{{ userInitial }}</div>
            <div class="iv-thumb-name">
              {{ userName }}
              <svg v-if="muted" width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.5">
                <line x1="1" y1="1" x2="23" y2="23"/>
                <path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V5a3 3 0 0 0-5.94-.6"/>
              </svg>
            </div>
          </div>
        </div>
      </template>

      <!-- ── 일반 모드 ── -->
      <template v-else>
        <!-- 면접관 행 -->
        <div class="iv-row-label">면접관</div>
        <div class="iv-row">
          <div v-for="(iv, idx) in interviewers" :key="iv.id"
               class="iv-tile" :class="{ speaking: isSpeaking(iv.id), 'has-video': iv.video }">
            <video v-if="iv.video" :src="iv.video" autoplay loop muted playsinline
                   class="iv-interviewer-video"></video>
            <div v-else class="iv-avatar" :class="`av-i${idx + 1}`">{{ iv.letter }}</div>
            <div class="iv-tile-name" :class="{ 'name-overlay': iv.video }">{{ iv.name }}</div>
          </div>
        </div>

        <!-- 지원자 행 -->
        <div class="iv-row-label" style="margin-top: 36px;">지원자</div>
        <div class="iv-row">
          <div v-for="ap in aiApplicants" :key="ap.id"
               class="iv-tile" :class="{ speaking: isSpeaking(ap.id), 'has-video': ap.image }">
            <img v-if="ap.image" :src="ap.image" class="iv-applicant-img" />
            <div v-else class="iv-avatar av-appli">{{ ap.letter }}</div>
            <div class="iv-tile-name" :class="{ 'name-overlay': ap.image }">{{ ap.name }}</div>
          </div>

          <!-- 나 (실제 카메라 - 타일 전체 채움) -->
          <div class="iv-tile me-tile" :class="{ speaking: isSpeaking('me') }">
            <video ref="myVideoEl"
                   v-show="!camOff && myStream && activeBgId === 'none'"
                   autoplay muted playsinline class="iv-my-video"></video>
            <canvas ref="bgCanvasEl"
                    v-show="!camOff && myStream && activeBgId !== 'none'"
                    class="iv-my-video"></canvas>
            <div class="iv-avatar av-me" v-show="camOff || !myStream">{{ userInitial }}</div>
            <div class="iv-me-overlay">
              <div class="iv-tile-name">
                {{ userName }}
                <svg v-if="muted" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.5" class="muted-icon">
                  <line x1="1" y1="1" x2="23" y2="23"/>
                  <path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V5a3 3 0 0 0-5.94-.6"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 하단 컨트롤: 아이콘만, 반응 없음 -->
    <div class="iv-controls">

      <!-- 가상 배경 피커 패널 -->
      <div class="iv-bg-picker" v-show="bgPickerOpen">
        <input ref="customBgInputEl" type="file" accept="image/*"
               style="display:none" @change="handleCustomBgFile">

        <template v-for="opt in BG_OPTIONS" :key="opt.id">
          <!-- 커스텀 이미지가 로드된 경우: 미리보기 + 변경 버튼 -->
          <template v-if="opt.id === 'custom' && customBgUrl">
            <button class="iv-bg-opt" :class="{ active: activeBgId === 'custom' }"
                    @click="applyBg('custom')">
              <div class="iv-bg-preview"
                   :style="`background:url('${customBgUrl}') center/cover no-repeat`"></div>
              <span class="iv-bg-label">이미지</span>
            </button>
            <button class="iv-bg-opt" @click="openCustomBgPicker()">
              <div class="iv-bg-preview" style="background:rgba(255,255,255,0.08)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                     stroke="rgba(255,255,255,0.5)" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
              </div>
              <span class="iv-bg-label">업로드</span>
            </button>
          </template>

          <!-- 일반 옵션 + 이미지 미로드 시 커스텀 -->
          <button v-else class="iv-bg-opt" :class="{ active: activeBgId === opt.id }"
                  @click="applyBg(opt.id)">
            <div class="iv-bg-preview" :style="opt.style || 'background:rgba(255,255,255,0.08)'">
              <svg v-if="opt.id === 'none'" width="16" height="16" viewBox="0 0 24 24"
                   fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
              <svg v-else-if="opt.id === 'blur'" width="16" height="16" viewBox="0 0 24 24"
                   fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="2">
                <circle cx="12" cy="12" r="3"/><path d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
              </svg>
              <svg v-else-if="opt.id === 'custom'" width="16" height="16" viewBox="0 0 24 24"
                   fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <span class="iv-bg-label">{{ opt.label }}</span>
          </button>
        </template>
      </div>

      <div class="iv-ctrl-group">
        <!-- 음소거 -->
        <button class="iv-ctrl-btn" :class="{ off: muted }" @click="toggleMute"
                :title="muted ? '음소거 해제' : '음소거'">
          <svg v-if="!muted" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/>
            <path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v3M8 22h8"/>
          </svg>
          <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="1" y1="1" x2="23" y2="23"/>
            <path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V5a3 3 0 0 0-5.94-.6"/>
            <path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23M12 19v3M8 22h8"/>
          </svg>
        </button>

        <!-- 비디오 -->
        <button class="iv-ctrl-btn" :class="{ off: camOff }" @click="toggleCam"
                :title="camOff ? '비디오 시작' : '비디오 중지'">
          <svg v-if="!camOff" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m23 7-7 5 7 5z"/><rect x="1" y="5" width="15" height="14" rx="2"/>
          </svg>
          <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"/>
            <line x1="1" y1="1" x2="23" y2="23"/>
          </svg>
        </button>

        <!-- 화면 공유 -->
        <button class="iv-ctrl-btn" :class="{ on: screenSharing }" @click="toggleScreenShare"
                :title="screenSharing ? '공유 중지' : '화면 공유'">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>
          </svg>
        </button>

        <!-- 가상 배경 -->
        <button class="iv-ctrl-btn" :class="{ on: activeBgId !== 'none' }"
                @click="bgPickerOpen = !bgPickerOpen"
                :title="activeBgId !== 'none' ? '가상 배경 켜짐' : '가상 배경'">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <path d="m3 16 4-4 4 4 4-6 6 6"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
          </svg>
        </button>
      </div>

      <button class="iv-end-btn" @click="endInterview">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>
        </svg>
        면접 종료
      </button>
    </div>
  </div>
</template>

<style scoped>
/* ── 전체 화면 ── */
.iv-page {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: #0d1117;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: inherit;
}

/* ── 상단 바 ── */
.iv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  background: rgba(0, 0, 0, 0.5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.iv-top-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.live-badge {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1.5px;
  color: #fff;
  background: #dc2626;
  padding: 3px 7px;
  border-radius: 4px;
  flex-shrink: 0;
}
.iv-top-title {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.iv-top-timer {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 3px;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}
.iv-top-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
.rec-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #fca5a5;
}
.rec-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ef4444;
  animation: blink 1.2s ease-in-out infinite;
}

/* ── 메인 영역 ── */
.iv-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 12px 28px 8px;
  overflow: hidden;
  gap: 0;
}
.iv-row-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.28);
  text-transform: uppercase;
  margin-bottom: 8px;
  align-self: flex-start;
}
.iv-row {
  display: flex;
  gap: 14px;
  justify-content: center;
  margin-bottom: 14px;
  width: 100%;
}

/* ── 타일 ── */
.iv-tile {
  background: #1a2130;
  border-radius: 16px;
  border: 3px solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 22px 20px 16px;
  flex: 1;
  max-width: 375px;
  min-height: 240px;
  min-width: 175px;
  gap: 10px;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.iv-tile.speaking {
  border-color: #ccff00;
  box-shadow: 0 0 0 1px rgba(204, 255, 0, 0.2), 0 0 24px rgba(204, 255, 0, 0.12);
}
.me-tile {
  background: #161f16;
}

/* ── 아바타 ── */
.iv-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 700;
  color: #fff;
}
.av-i1 { background: #1e3a5f; }
.av-i2 { background: #1b3052; }
.av-i3 { background: #162648; }
.av-i4 { background: #111e38; }
.av-appli { background: #3b1f5e; }
.av-me { background: #1a4430; }

/* ── 면접관 비디오 타일 ── */
.iv-tile.has-video {
  padding: 0;
  overflow: hidden;
  position: relative;
}
.iv-interviewer-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.iv-tile-name.name-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 2;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.65));
  padding: 28px 12px 12px;
  justify-content: center;
}
.iv-thumb-interviewer {
  transform: none;
}
.iv-applicant-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ── 내 카메라: 타일 전체 채움 ── */
.iv-tile.me-tile {
  padding: 0;
  overflow: hidden;
}
.iv-my-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
  z-index: 0;
}
.me-tile .iv-avatar {
  position: relative;
  z-index: 1;
}
.iv-me-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 2;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.65));
  padding: 28px 12px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

/* ── 타일 하단 이름 ── */
.iv-tile-name {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  gap: 4px;
}
.muted-icon {
  flex-shrink: 0;
}


/* ── 화면 공유 ── */
.iv-screen-area {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  width: 100%;
  margin-bottom: 12px;
  min-height: 0;
}
.iv-screen-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.iv-screen-stop {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(220, 38, 38, 0.9);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s;
}
.iv-screen-stop:hover { background: rgba(185, 28, 28, 0.95); }

.iv-thumb-strip {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-shrink: 0;
  flex-wrap: wrap;
}
.iv-thumb {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  background: #1a2130;
  border-radius: 10px;
  border: 2px solid transparent;
  min-width: 76px;
  transition: border-color 0.25s;
}
.iv-thumb.speaking {
  border-color: #ccff00;
  box-shadow: 0 0 10px rgba(204, 255, 0, 0.15);
}
.iv-thumb-av {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
}
.iv-thumb-video {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  transform: scaleX(-1);
}
.iv-thumb-name {
  font-size: 10px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.75);
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
}

/* ── 하단 컨트롤 바 ── */
.iv-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 32px 16px;
  background: rgba(0, 0, 0, 0.6);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.iv-ctrl-group {
  display: flex;
  gap: 10px;
  align-items: center;
}
.iv-ctrl-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  width: 56px;
  height: 56px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  flex-shrink: 0;
}
.iv-ctrl-btn:hover { background: rgba(255, 255, 255, 0.13); }
.iv-ctrl-btn.off {
  background: rgba(239, 68, 68, 0.14);
  border-color: rgba(239, 68, 68, 0.4);
  color: #fca5a5;
}
.iv-ctrl-btn.on {
  background: rgba(204, 255, 0, 0.1);
  border-color: rgba(204, 255, 0, 0.4);
  color: #ccff00;
}

/* ── 가상 배경 피커 ── */
.iv-controls { position: relative; }
.iv-bg-picker {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
  background: #1a1f2e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  gap: 10px;
  z-index: 10;
}
.iv-bg-opt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}
.iv-bg-preview {
  width: 56px;
  height: 38px;
  border-radius: 8px;
  border: 2px solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.15s;
  overflow: hidden;
}
.iv-bg-opt:hover .iv-bg-preview { border-color: rgba(255, 255, 255, 0.4); }
.iv-bg-opt.active .iv-bg-preview { border-color: #ccff00; }
.iv-bg-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  font-weight: 500;
  white-space: nowrap;
}
.iv-bg-opt.active .iv-bg-label { color: #ccff00; }

.iv-end-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #dc2626;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  padding: 14px 28px;
  cursor: pointer;
  transition: background 0.15s;
}
.iv-end-btn:hover { background: #b91c1c; }

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.2; }
}
</style>

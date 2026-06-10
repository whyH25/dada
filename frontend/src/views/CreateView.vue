<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { useFlowStore } from '../stores/flow.js'
import { useAuthStore } from '../stores/auth.js'
import { fetchJobCategories, createInterviewRoom } from '../api/interviewRoomApi.js'
import { startSession } from '../api/sessionApi.js'
import { useDeviceCheck } from '../composables/useDeviceCheck.js'

const router = useRouter()
const data = useDataStore()
const flow = useFlowStore()
const auth = useAuthStore()

const step = ref(1)

// ── 응시 환경 체크 ────────────────────────────────────────────
const {
  videoEl, camStatus, micStatus, speakerStatus,
  camOn, micOn, volumeLevel, camLabel, micLabel,
  startDeviceCheck, stopDeviceCheck, toggleCam, toggleMic, testSpeaker,
} = useDeviceCheck()

watch(step, val => {
  if (val === 2) startDeviceCheck()
  else stopDeviceCheck()
})

onUnmounted(() => stopDeviceCheck())
const company = ref('')
const selectedJobId = ref(null)
const selectedJobName = ref('')
const jobCategories = ref([])

const sel = reactive({
  type: '신입',
  diff: '중 | 실제 면접 수준',
  interviewers: '1명',
  applicants: '1명',
})

// 파일 상태
const resumeFile = ref(null)
const portfolioFile = ref(null)

// 로딩 / 에러
const submitting = ref(false)
const errorMsg = ref('')

const typeOpts = ['신입', '경력', '인턴']
const diffOpts = ['하 | 편안한 분위기', '중 | 실제 면접 수준', '상 | 압박 면접']
const countOpts = ['1명', '2명', '3명']
const applicantOpts = ['0명', '1명', '2명']

const difficultyMap = {
  '하 | 편안한 분위기': 'EASY',
  '중 | 실제 면접 수준': 'MEDIUM',
  '상 | 압박 면접': 'HARD',
}

const interviewerCount = computed(() => parseInt(sel.interviewers))
const applicantCount = computed(() => parseInt(sel.applicants))

// 예상 소요 시간: 면접관 수 * 약 10분
const estimatedTime = computed(() => {
  const base = interviewerCount.value * 10
  return `${base} ~ ${base + 10}분`
})

const submittedDocCount = computed(() => {
  let c = 0
  if (resumeFile.value) c++
  if (portfolioFile.value) c++
  return c
})

onMounted(async () => {
  try {
    jobCategories.value = await fetchJobCategories()
    // 첫 번째 직무를 기본 선택
    if (jobCategories.value.length > 0) {
      selectedJobId.value = jobCategories.value[0].jobId
      selectedJobName.value = jobCategories.value[0].jobName
    }
  } catch {
    // API 연결 전이거나 오류 시 빈 목록 유지
  }
})

function onJobChange(e) {
  const found = jobCategories.value.find(j => j.jobId === parseInt(e.target.value))
  if (found) {
    selectedJobId.value = found.jobId
    selectedJobName.value = found.jobName
  }
}

function onResumeChange(e) {
  resumeFile.value = e.target.files[0] || null
}

function onPortfolioChange(e) {
  portfolioFile.value = e.target.files[0] || null
}

function removeResume() {
  resumeFile.value = null
}

function removePortfolio() {
  portfolioFile.value = null
}

function gotoStep2() {
  if (!company.value.trim()) {
    errorMsg.value = '회사명을 입력해주세요.'
    return
  }
  errorMsg.value = ''
  step.value = 2
  window.scrollTo(0, 0)
}

async function startInterview() {
  try {
    submitting.value = true

    // 1. 면접방 DB 저장
    const roomResult = await createInterviewRoom({
      userId: auth.user?.userId ?? 1,
      companyName: company.value.trim(),
      jobId: selectedJobId.value,
      difficulty: difficultyMap[sel.diff],
      employmentType: sel.type,
      interviewerCount: interviewerCount.value,
      aiApplicantCount: applicantCount.value,
      resumeFile: resumeFile.value,
      portfolioFile: portfolioFile.value,
    })
    flow.roomId = roomResult.roomId

    // 2. 세션 시작 + AI 질문 생성
    const sessionResult = await startSession({
      roomId: flow.roomId,
      userId: auth.user?.userId ?? 1,
    })
    flow.sessionId = sessionResult.data.sessionId
    flow.scenarios = sessionResult.data.scenarios

    // 기존 mock 데이터에도 추가 (PrepView 등 호환용)
    const logoMap = { 카카오: 'kakao', 삼성전자: 'samsung', 네이버: 'naver', 토스: 'toss' }
    data.rooms.push({
      co: company.value,
      logo: logoMap[company.value] || 'lounge',
      short: company.value.charAt(0),
      role: `${selectedJobName.value} | ${sel.type}`,
      title: `${company.value} ${selectedJobName.value} 맞춤형 다대다 면접`,
      diff: difficultyMap[sel.diff].charAt(0),
      date: new Date().toLocaleDateString('ko-KR', { year: '2-digit', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').replace('.', ''),
      count: 0,
      sessions: 0,
      mine: true,
      interviewerCount: interviewerCount.value,
      aiApplicantCount: applicantCount.value,
    })
    flow.currentRoom = data.rooms.length - 1
    stopDeviceCheck()
    router.push('/prep')
  } catch (e) {
    errorMsg.value = e.message || '면접 시작 중 오류가 발생했습니다.'
  } finally {
    submitting.value = false
  }
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

      <!-- ── STEP 1 ── -->
      <div v-show="step === 1">
        <div class="create-grid">
          <div>
            <!-- 기본 정보 -->
            <div class="form-section">
              <div class="form-section-head">
                <h3 class="form-section-title">기본 정보</h3>
                <p class="form-section-sub">어떤 회사와 직무로 면접을 진행할까요?</p>
              </div>
              <div class="form-section-body">
                <div class="form-row">
                  <div class="field">
                    <label class="field-label">회사명 <span class="req">*</span></label>
                    <input class="input" v-model="company" placeholder="예) 카카오, 삼성전자 DS부문" />
                  </div>
                  <div class="field">
                    <label class="field-label">직무 선택 <span class="req">*</span></label>
                    <select class="select" :value="selectedJobId" @change="onJobChange">
                      <option v-if="jobCategories.length === 0" disabled value="">직무 목록 로딩 중...</option>
                      <option v-for="job in jobCategories" :key="job.jobId" :value="job.jobId">
                        {{ job.jobName }}
                      </option>
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

            <!-- 면접 환경 설정 -->
            <div class="form-section">
              <div class="form-section-head">
                <h3 class="form-section-title">면접 환경 설정</h3>
                <p class="form-section-sub">난이도와 참여 인원수에 따라 분위기와 질문 강도가 바뀝니다.</p>
              </div>
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
                      <div v-for="o in applicantOpts" :key="o" class="chip" :class="{ active: sel.applicants === o }" @click="sel.applicants = o">{{ o }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 제출 서류 -->
            <div class="form-section">
              <div class="form-section-head">
                <h3 class="form-section-title">제출 서류 <span style="font-size:13px;font-weight:400;color:var(--ink-400)">(선택)</span></h3>
                <p class="form-section-sub">업로드한 서류 기반으로 면접관이 맞춤형 질문을 생성합니다.</p>
              </div>
              <div class="form-section-body">
                <div class="upload-grid">

                  <!-- 이력서 및 자기소개서 -->
                  <label class="upload" :class="{ uploaded: resumeFile }" style="cursor:pointer">
                    <input type="file" accept=".pdf,.doc,.docx" style="display:none" @change="onResumeChange" />
                    <template v-if="resumeFile">
                      <div class="upload-icon">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg>
                      </div>
                      <div>
                        <div class="upload-title">{{ resumeFile.name }}</div>
                        <div class="upload-sub">{{ (resumeFile.size / 1024).toFixed(0) }} KB</div>
                      </div>
                      <button class="btn btn-sm btn-ghost" style="margin-left:auto" @click.prevent="removeResume">삭제</button>
                    </template>
                    <template v-else>
                      <div class="upload-icon" style="margin:0 auto 8px">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg>
                      </div>
                      <div class="upload-title">이력서 및 자기소개서</div>
                      <div class="upload-sub">PDF | DOC | 최대 10MB</div>
                    </template>
                  </label>

                  <!-- 포트폴리오 -->
                  <label class="upload" :class="{ uploaded: portfolioFile }" style="cursor:pointer">
                    <input type="file" accept=".pdf,.doc,.docx" style="display:none" @change="onPortfolioChange" />
                    <template v-if="portfolioFile">
                      <div class="upload-icon">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg>
                      </div>
                      <div>
                        <div class="upload-title">{{ portfolioFile.name }}</div>
                        <div class="upload-sub">{{ (portfolioFile.size / 1024).toFixed(0) }} KB</div>
                      </div>
                      <button class="btn btn-sm btn-ghost" style="margin-left:auto" @click.prevent="removePortfolio">삭제</button>
                    </template>
                    <template v-else>
                      <div class="upload-icon" style="margin:0 auto 8px">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg>
                      </div>
                      <div class="upload-title">포트폴리오</div>
                      <div class="upload-sub">PDF | DOC | 최대 50MB</div>
                    </template>
                  </label>

                </div>
              </div>
            </div>
          </div>

          <!-- 미리보기 -->
          <aside class="preview-panel">
            <div class="preview-head"><h3 class="preview-title">설정 미리보기</h3><p class="preview-sub">변경할 때마다 실시간으로 반영됩니다.</p></div>
            <div class="preview-body">
              <div class="preview-row"><div class="preview-row-label">회사</div><div class="preview-row-val">{{ company || '-' }}</div></div>
              <div class="preview-row"><div class="preview-row-label">직무</div><div class="preview-row-val">{{ selectedJobName || '-' }}</div></div>
              <div class="preview-row"><div class="preview-row-label">유형</div><div class="preview-row-val">{{ sel.type }}</div></div>
              <div class="preview-row"><div class="preview-row-label">난이도</div><div class="preview-row-val">{{ sel.diff }}</div></div>
              <div class="preview-row"><div class="preview-row-label">면접관 수</div><div class="preview-row-val">{{ sel.interviewers }}</div></div>
              <div class="preview-row"><div class="preview-row-label">AI 경쟁 지원자</div><div class="preview-row-val">{{ sel.applicants }}</div></div>
              <div class="preview-row">
                <div class="preview-row-label">제출 서류</div>
                <div class="preview-row-val">
                  <span v-if="submittedDocCount === 0">없음</span>
                  <span v-else>{{ submittedDocCount }}건</span>
                </div>
              </div>
              <div class="preview-row"><div class="preview-row-label">예상 소요 시간</div><div class="preview-row-val">{{ estimatedTime }}</div></div>
            </div>
            <div class="preview-foot">
              <div class="text-sm text-muted" style="margin-bottom: 10px;">
                총 {{ 1 + interviewerCount + applicantCount }}명 | 면접관 {{ interviewerCount }} + 본인 + AI 지원자 {{ applicantCount }}
              </div>
              <button class="btn btn-secondary btn-block" disabled style="opacity:0.6">다음 단계에서 생성</button>
            </div>
          </aside>
        </div>

        <!-- 에러 메시지 -->
        <div v-if="errorMsg" style="color:var(--red-500);margin-bottom:12px;font-size:14px;">{{ errorMsg }}</div>

        <div class="create-actions">
          <button class="btn btn-ghost" @click="router.push('/')">취소</button>
          <button class="btn btn-primary btn-lg" :disabled="submitting" @click="gotoStep2">
            <span v-if="submitting">저장 중...</span>
            <template v-else>
              다음 단계
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
            </template>
          </button>
        </div>
      </div>

      <!-- ── STEP 2 ── -->
      <div v-show="step === 2">
        <div class="check-layout">
          <div>
            <div class="form-section">
              <div class="form-section-head">
                <h3 class="form-section-title">응시 환경 체크</h3>
                <p class="form-section-sub">카메라와 마이크가 정상적으로 작동하는지 확인하세요.</p>
              </div>
              <div class="form-section-body" style="padding-bottom: 22px;">

                <div class="check-screen">
                  <div class="check-cam" style="position:relative;overflow:hidden;">
                    <video
                      v-show="camStatus === 'ok' && camOn"
                      ref="videoEl"
                      autoplay
                      playsinline
                      muted
                      style="width:100%;height:100%;object-fit:cover;border-radius:12px;transform:scaleX(-1)"
                    />
                    <div v-show="camStatus !== 'ok' || !camOn" class="you-circle">
                      {{ camStatus === 'error' ? '✕' : '나' }}
                    </div>
                    <div class="check-overlay">
                      <span class="live-dot" :style="{ background: camStatus === 'ok' ? 'var(--green-500)' : 'var(--ink-300)' }"></span>
                      {{ camStatus === 'pending' ? '카메라 연결 중...' : camStatus === 'error' ? '카메라 오류' : camOn ? '카메라 미리보기' : '카메라 꺼짐' }}
                    </div>
                  </div>
                  <div class="check-controls">
                    <div class="check-control" :title="micOn ? '마이크 음소거' : '마이크 켜기'" :style="{ opacity: micOn ? 1 : 0.4 }" @click="toggleMic">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" />
                        <path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v3M8 22h8" />
                      </svg>
                    </div>
                    <div class="check-control" :title="camOn ? '카메라 끄기' : '카메라 켜기'" :style="{ opacity: camOn ? 1 : 0.4 }" @click="toggleCam">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="m23 7-7 5 7 5z" /><rect x="1" y="5" width="15" height="14" rx="2" />
                      </svg>
                    </div>
                  </div>
                </div>

                <div class="check-list">
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">카메라 — {{ camLabel }}</div>
                      <div class="check-item-sub">{{ camStatus === 'pending' ? '권한 요청 중...' : camStatus === 'error' ? '카메라에 접근할 수 없습니다.' : '영상 출력 확인됨' }}</div>
                    </div>
                    <span class="check-item-status">
                      <span class="badge-dot" :style="{ background: camStatus === 'ok' ? 'var(--green-500)' : camStatus === 'error' ? 'var(--red-500)' : 'var(--ink-300)' }"></span>
                      {{ camStatus === 'ok' ? '정상' : camStatus === 'error' ? '오류' : '대기 중' }}
                    </span>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">마이크 — {{ micLabel }}</div>
                      <div class="check-item-sub">
                        <span v-if="micStatus === 'pending'">말을 해보세요...</span>
                        <span v-else-if="micStatus === 'error'">마이크에 접근할 수 없습니다.</span>
                        <template v-else>
                          입력 레벨:
                          <span v-for="i in 10" :key="i" :style="{ color: volumeLevel >= i * 10 ? 'var(--green-500)' : 'var(--ink-300)' }">●</span>
                        </template>
                      </div>
                    </div>
                    <span class="check-item-status">
                      <span class="badge-dot" :style="{ background: micStatus === 'ok' ? 'var(--green-500)' : micStatus === 'error' ? 'var(--red-500)' : 'var(--ink-300)' }"></span>
                      {{ micStatus === 'ok' ? '정상' : micStatus === 'error' ? '오류' : '대기 중' }}
                    </span>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">스피커</div>
                      <div class="check-item-sub">{{ speakerStatus === 'ok' ? '소리가 들리셨나요?' : '테스트 버튼을 눌러 소리를 확인하세요.' }}</div>
                    </div>
                    <span v-if="speakerStatus === 'ok'" class="check-item-status"><span class="badge-dot" style="background: var(--green-500)"></span>정상</span>
                    <button v-else class="btn btn-sm btn-secondary" @click="testSpeaker">테스트</button>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">네트워크</div>
                      <div class="check-item-sub">인터넷 연결 상태 양호</div>
                    </div>
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
              <div class="preview-row"><div class="preview-row-label">직무</div><div class="preview-row-val">{{ selectedJobName || '-' }}</div></div>
              <div class="preview-row"><div class="preview-row-label">유형</div><div class="preview-row-val">{{ sel.type }}</div></div>
              <div class="preview-row"><div class="preview-row-label">난이도</div><div class="preview-row-val">{{ sel.diff }}</div></div>
              <div class="preview-row"><div class="preview-row-label">면접관 수</div><div class="preview-row-val">{{ sel.interviewers }}</div></div>
              <div class="preview-row"><div class="preview-row-label">AI 경쟁 지원자</div><div class="preview-row-val">{{ sel.applicants }}</div></div>
              <div class="preview-row"><div class="preview-row-label">제출 서류</div><div class="preview-row-val">{{ submittedDocCount === 0 ? '없음' : submittedDocCount + '건' }}</div></div>
              <div class="preview-row"><div class="preview-row-label">예상 소요 시간</div><div class="preview-row-val">{{ estimatedTime }}</div></div>
            </div>
            <div class="preview-foot">
              <p v-if="camStatus !== 'ok' || micStatus !== 'ok'" style="font-size:13px;color:var(--ink-400);margin-bottom:10px;text-align:center;">
                카메라와 마이크가 모두 정상이어야 시작할 수 있습니다.
              </p>
              <button
                class="btn btn-primary btn-block btn-lg"
                :disabled="camStatus !== 'ok' || micStatus !== 'ok'"
                :style="{ opacity: camStatus === 'ok' && micStatus === 'ok' ? 1 : 0.45, cursor: camStatus === 'ok' && micStatus === 'ok' ? 'pointer' : 'not-allowed' }"
                @click="startInterview"
              >
                면접방 생성하고 시작하기
              </button>
              <button class="btn btn-ghost btn-block" style="margin-top: 6px;" @click="step = 1">이전 단계로</button>
            </div>
          </aside>
        </div>
      </div>

    </div>
  </main>
</template>

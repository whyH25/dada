<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { useRouter, useRoute } from 'vue-router'
import { toast } from '../utils/toast.js'
import { useAuthStore } from '../stores/auth.js'
import { updateUserApi, deleteUserApi, verifyPasswordApi } from '../api/authApi.js'
import { fetchMyInterviewRooms, fetchRoomScenarios, fetchRoomReport } from '../api/mypageApi.js'
import { deleteInterviewRoomApi } from '../api/interviewRoomApi.js'
import { fetchMyFiles, uploadFile, deleteFile } from '../api/userFileApi.js'
import { fetchMyPosts } from '../api/postsApi.js'
import { reportPanel } from '../utils/mypageReport.js'
import { preparePayment, fetchPaymentHistory } from '../api/paymentApi.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

// section: reports | report-detail | resume | portfolio | account | my-posts | billing
const section = ref('reports')
const reportTab = ref('overview')

const reportTabs = [
  ['overview', '종합 평가'], ['competency', '역량 분석'],
  ['applicants', '지원자 분석'], ['questions', '질문별 상세'],
]

function gotoMy(s) { section.value = s; window.scrollTo(0, 0) }
function setTab(k) { reportTab.value = k; window.scrollTo(0, 0) }

// ---- 면접 기록 목록 ----
const myRooms = ref([])
const roomsLoading = ref(false)

async function loadMyRooms() {
  roomsLoading.value = true
  try {
    myRooms.value = await fetchMyInterviewRooms()
  } catch (e) {
    toast(e.message)
  } finally {
    roomsLoading.value = false
  }
}

// DB는 deleted_at으로 비활성화 처리 (실삭제 아님) - 목록에서만 즉시 제거
async function deleteRoom(room) {
  if (!window.confirm('이 면접 기록과 리포트를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.')) return
  try {
    await deleteInterviewRoomApi(room.roomId)
    myRooms.value = myRooms.value.filter(r => r.roomId !== room.roomId)
    toast('면접 기록이 삭제되었습니다.')
  } catch (e) {
    toast(e.message)
  }
}

// ---- 면접 기록 상세 ----
const currentRoom = ref(null)
const roomScenarios = ref([])
const scenariosLoading = ref(false)
// AI 분석 리포트 데이터 (없으면 null)
const roomReport = ref(null)

async function openRoom(room) {
  currentRoom.value = room
  reportTab.value = 'overview'
  roomScenarios.value = []
  roomReport.value = null
  section.value = 'report-detail'
  window.scrollTo(0, 0)
  scenariosLoading.value = true
  try {
    // 시나리오 + 리포트 병렬 조회 (리포트 없으면 null)
    const [scenarios, report] = await Promise.all([
      fetchRoomScenarios(room.roomId),
      fetchRoomReport(room.roomId).catch(() => null),
    ])
    roomScenarios.value = scenarios
    roomReport.value = report
  } catch (e) {
    toast(e.message)
  } finally {
    scenariosLoading.value = false
  }
}

// API 응답 데이터 → mypageReport.js panelXxx 함수 입력 형식으로 변환
// scenarios: roomScenarios — AI 지원자 답변을 question_seq 기준으로 매핑
function transformReportForPanel(data, scenarios = []) {
  const rep = data.report
  const me = [rep.compExpertise ?? 0, rep.compLogic ?? 0, rep.compCommu ?? 0, rep.compCulture ?? 0, rep.compPressure ?? 0]

  // AI 경쟁 지원자 전체 평균 점수 (5개 역량 대리값으로 사용)
  const aiApps = data.applicants.filter(a => !a.isUser)
  const aiAvg = aiApps.length ? Math.round(aiApps.reduce((s, a) => s + (a.score ?? 70), 0) / aiApps.length) : 70

  // applicantId → name 맵 (지원자 분석 데이터에서 추출)
  const appNameMap = {}
  for (const a of data.applicants) {
    if (!a.isUser && a.applicantId) appNameMap[a.applicantId] = a.name ?? 'AI 지원자'
  }

  // question_seq → APPLICANT 답변 목록 (시나리오에서 추출)
  const appAnswersBySeq = {}
  for (const s of scenarios) {
    if (s.turnRole === 'APPLICANT' && s.questionSeq > 0 && s.speechText) {
      const name = appNameMap[s.turnRefId] ?? 'AI 지원자'
      ;(appAnswersBySeq[s.questionSeq] ??= []).push({ name, text: s.speechText })
    }
  }

  // 평균 답변 시간: USER 턴의 answer_sec(답변 완료까지 걸린 시간)만 모아 평균
  const userAnswerSecs = scenarios
    .filter(s => s.turnRole === 'USER' && s.answerSec != null)
    .map(s => s.answerSec)
  const avgAnswerSec = userAnswerSecs.length
    ? Math.round(userAnswerSecs.reduce((sum, v) => sum + v, 0) / userAnswerSecs.length)
    : 0

  return {
    score: rep.overallScore,
    aiComment: rep.aiComment,
    logic: rep.compLogicDetail,
    me,
    ai: me.map(() => aiAvg),
    pass: [75, 78, 80, 72, 70],
    speech: {
      avgLen: avgAnswerSec,
    },
    applicants: data.applicants.map(a => ({
      name: a.name ?? (a.isUser ? '나' : 'AI 지원자'),
      score: a.score ?? 0,
      me: !!a.isUser,
      strength: a.strength ?? '',
      weak: a.weakness ?? '',
    })),
    questions: data.questions.map(q => ({
      q: `Q${q.questionSeq}`,
      text: q.questionText ?? '',
      answerText: q.answerText ?? '',
      applicantAnswers: appAnswersBySeq[q.questionSeq] ?? [],
      score: q.score ?? 0,
      label: q.label
        ? [q.label, q.label === '우수' ? 'green' : q.label === '미흡' ? 'red' : 'amber']
        : null,
      body: q.feedback ?? '',
      tags: q.tags ? q.tags.split(',').map(t => t.trim()).filter(Boolean) : [],
    })),
  }
}

// 현재 탭에 맞는 패널 HTML (리포트 있을 때만)
const currentPanelHtml = computed(() => {
  if (!roomReport.value) return ''
  return reportPanel(transformReportForPanel(roomReport.value, roomScenarios.value), reportTab.value)
})

// ---- 리포트 4탭을 하나의 PDF로 다운로드 ----
const reportPanelRef = ref(null)
const pdfGenerating = ref(false)

async function downloadReportPdf() {
  if (!roomReport.value || pdfGenerating.value) return
  pdfGenerating.value = true
  const originalTab = reportTab.value

  try {
    const pdf = new jsPDF('p', 'mm', 'a4')
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const margin = 10
    const contentWidth = pageWidth - margin * 2

    for (let i = 0; i < reportTabs.length; i++) {
      const [key, label] = reportTabs[i]
      reportTab.value = key
      await nextTick()
      // v-html 렌더링과 레이아웃(레이더 SVG 등) 안정화 대기
      await new Promise((resolve) => setTimeout(resolve, 200))

      const el = reportPanelRef.value
      if (!el) continue

      // jsPDF 기본 폰트는 한글을 못 그리므로, 제목도 캡처 대상 DOM에 임시로 끼워 이미지로 같이 캡처
      const titleEl = document.createElement('h2')
      titleEl.textContent = label
      titleEl.style.cssText = 'font-size:22px;font-weight:800;color:#111827;margin:0 0 16px;'
      el.prepend(titleEl)

      const canvas = await html2canvas(el, { scale: 2, useCORS: true, backgroundColor: '#ffffff' })
      el.removeChild(titleEl)

      const pxPerMm = canvas.width / contentWidth

      if (i > 0) pdf.addPage()

      // 캔버스가 한 페이지보다 길면 잘라서 여러 페이지에 나눠 붙임
      let srcY = 0
      while (srcY < canvas.height) {
        const availableHeightMm = pageHeight - margin * 2
        const sliceHeightPx = Math.min(canvas.height - srcY, Math.floor(availableHeightMm * pxPerMm))

        const sliceCanvas = document.createElement('canvas')
        sliceCanvas.width = canvas.width
        sliceCanvas.height = sliceHeightPx
        sliceCanvas.getContext('2d').drawImage(canvas, 0, srcY, canvas.width, sliceHeightPx, 0, 0, canvas.width, sliceHeightPx)

        const sliceImgHeightMm = sliceHeightPx / pxPerMm
        pdf.addImage(sliceCanvas.toDataURL('image/png'), 'PNG', margin, margin, contentWidth, sliceImgHeightMm)

        srcY += sliceHeightPx
        if (srcY < canvas.height) pdf.addPage()
      }
    }

    const companyName = currentRoom.value?.companyName ?? '면접'
    pdf.save(`${companyName}_면접리포트.pdf`)
  } catch (e) {
    toast('PDF 생성에 실패했습니다.')
  } finally {
    reportTab.value = originalTab
    pdfGenerating.value = false
  }
}

// ---- 헬퍼 ----
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}.${m}.${day}`
}
function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${formatDate(dateStr)} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
// 리포트는 생성되어도 면접 종료 시점(ended_at) + 1일이 지나야 열람 가능
const REPORT_DELAY_MS = 24 * 60 * 60 * 1000
function reportOpenAt(room) {
  if (!room.endedAt) return null
  return new Date(new Date(room.endedAt).getTime() + REPORT_DELAY_MS)
}
function isReportOpen(room) {
  const openAt = reportOpenAt(room)
  return !!openAt && openAt <= new Date()
}
function reportOpenAtLabel(room) {
  const openAt = reportOpenAt(room)
  return openAt ? formatDateTime(openAt) : ''
}

function diffLabel(d) { return d === 'HARD' ? '상' : d === 'MEDIUM' ? '중' : '하' }
function statusLabel(s) { return s === 'COMPLETED' ? '완료' : s === 'IN_PROGRESS' ? '진행 중' : s === 'CANCELLED' ? '취소' : s || '-' }
function statusBadgeClass(s) { return s === 'COMPLETED' ? 'badge-green' : s === 'IN_PROGRESS' ? 'badge-blue' : '' }
function applicantTypeLabel(t) { return t === 'NEW' ? '신입' : t === 'INTERN' ? '인턴' : t === 'EXPERIENCED' ? '경력' : t || '-' }

// ---- 서류 관리 (마이페이지에 등록 → 면접방 생성 시 선택해서 재사용) ----
const docMeta = {
  resume: { title: '이력서/자기소개서', accept: '.pdf,.doc,.docx', hint: 'PDF | DOC | 최대 10MB' },
  portfolio: { title: '포트폴리오', accept: '.pdf,.doc,.docx', hint: 'PDF | DOC | 최대 50MB' },
}
const docs = reactive({ resume: [], portfolio: [] })
const docsLoading = reactive({ resume: false, portfolio: false })

async function loadDocs(type) {
  docsLoading[type] = true
  try {
    docs[type] = await fetchMyFiles(type)
  } catch (e) {
    toast(e.message)
  } finally {
    docsLoading[type] = false
  }
}

async function uploadDoc(type, e) {
  const f = e.target.files && e.target.files[0]
  e.target.value = ''
  if (!f) return
  try {
    const saved = await uploadFile(type, f)
    docs[type].unshift(saved)
    toast(`${docMeta[type].title}가 업로드되었어요.`)
  } catch (err) {
    toast(err.message)
  }
}

async function deleteDoc(type, i) {
  const item = docs[type][i]
  try {
    await deleteFile(type, item.id)
    docs[type].splice(i, 1)
  } catch (err) {
    toast(err.message)
  }
}

// ---- 회원정보 수정 ----
const isEditing = ref(false)
const editName = ref('')
const editPhone = ref('')
const editPwd = ref('')
const editPwdConfirm = ref('')
const editError = ref('')
const editLoading = ref(false)

// 수정 전 본인 확인 (현재 비밀번호 일치 확인)
const isVerifying = ref(false)
const verifyPwd = ref('')
const verifyError = ref('')
const verifyLoading = ref(false)

function startEdit() {
  // 소셜 가입만으로 만들어진 계정은 비밀번호가 없어 본인확인 절차를 건너뜀
  if (!user.value?.hasPassword) {
    editName.value = user.value?.userName || ''
    editPhone.value = user.value?.userPhone || ''
    editPwd.value = ''
    editPwdConfirm.value = ''
    editError.value = ''
    isEditing.value = true
    return
  }
  verifyPwd.value = ''
  verifyError.value = ''
  isVerifying.value = true
}

function cancelVerify() {
  isVerifying.value = false
  verifyPwd.value = ''
  verifyError.value = ''
}

async function confirmPassword() {
  verifyError.value = ''
  if (!verifyPwd.value) { verifyError.value = '비밀번호를 입력해주세요.'; return }
  verifyLoading.value = true
  try {
    await verifyPasswordApi(verifyPwd.value)
    isVerifying.value = false
    editName.value = user.value?.userName || ''
    editPhone.value = user.value?.userPhone || ''
    editPwd.value = ''
    editPwdConfirm.value = ''
    editError.value = ''
    isEditing.value = true
  } catch (e) {
    verifyError.value = e.message
  } finally {
    verifyLoading.value = false
  }
}

async function submitEdit() {
  editError.value = ''
  if (!editName.value.trim()) { editError.value = '이름을 입력해주세요.'; return }
  if (editPwd.value && editPwd.value.length < 8) { editError.value = '비밀번호는 8자 이상이어야 합니다.'; return }
  if (editPwd.value && editPwd.value !== editPwdConfirm.value) { editError.value = '비밀번호가 일치하지 않습니다.'; return }
  editLoading.value = true
  try {
    const payload = { userName: editName.value.trim(), userPhone: editPhone.value.trim() }
    if (editPwd.value) payload.userPwd = editPwd.value
    const res = await updateUserApi(payload)
    authStore.user = res.data
    isEditing.value = false
    toast('회원정보가 수정되었습니다.')
  } catch (e) {
    editError.value = e.message
  } finally {
    editLoading.value = false
  }
}

// ---- 회원탈퇴 ----
const showWithdrawConfirm = ref(false)
const withdrawLoading = ref(false)

async function withdrawUser() {
  withdrawLoading.value = true
  try {
    await deleteUserApi()
    authStore.user = null
    authStore.isLoggedIn = false
    showWithdrawConfirm.value = false
    router.push('/')
  } catch (e) {
    toast(e.message)
  } finally {
    withdrawLoading.value = false
  }
}

// ---- 내가 쓴 글 ----
const myPosts = ref([])
const myPostsLoading = ref(false)

async function loadMyPosts() {
  myPostsLoading.value = true
  try {
    myPosts.value = await fetchMyPosts()
  } catch (e) {
    toast(e.message)
  } finally {
    myPostsLoading.value = false
  }
}

watch(section, (s) => {
  if (s === 'my-posts' && !myPosts.value.length && !myPostsLoading.value) loadMyPosts()
})

// ---- 결제 ----
const TICKET_PLANS = [
  { name: '10회권',  tickets: 10, bonus: 0,  amount: 5_000,  label: '10회',     priceLabel: '5,000원' },
  { name: '20회권',  tickets: 20, bonus: 1,  amount: 10_000, label: '20 + 1회', priceLabel: '10,000원' },
  { name: '30회권',  tickets: 30, bonus: 2,  amount: 15_000, label: '30 + 2회', priceLabel: '15,000원' },
  { name: '50회권',  tickets: 50, bonus: 5,  amount: 25_000, label: '50 + 5회', priceLabel: '25,000원' },
]

const paymentHistory = ref([])
const paymentHistoryLoading = ref(false)
const paymentLoading = ref(false)

async function loadPaymentHistory() {
  paymentHistoryLoading.value = true
  try { paymentHistory.value = await fetchPaymentHistory() }
  catch (e) { toast(e.message) }
  finally { paymentHistoryLoading.value = false }
}

watch(section, (s) => {
  if (s === 'billing' && !paymentHistory.value.length && !paymentHistoryLoading.value) loadPaymentHistory()
})

async function buyTickets(plan) {
  if (paymentLoading.value) return
  paymentLoading.value = true
  try {
    const { orderId, amount } = await preparePayment(plan.name)

    // Toss SDK가 없으면 CDN에서 동적 로드
    if (!window.TossPayments) {
      await new Promise((resolve, reject) => {
        const s = document.createElement('script')
        s.src = 'https://js.tosspayments.com/v1/payment'
        s.onload = resolve
        s.onerror = reject
        document.head.appendChild(s)
      })
    }

    const toss = window.TossPayments(import.meta.env.VITE_TOSS_CLIENT_KEY || 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq')
    await toss.requestPayment('카드', {
      amount,
      orderId,
      orderName: plan.name + ' 구매',
      customerName: user.value?.userName || '사용자',
      successUrl: `${window.location.origin}${window.location.pathname}#/payment/success`,
      failUrl:    `${window.location.origin}${window.location.pathname}#/payment/fail`,
    })
  } catch (e) {
    // 사용자가 결제창 닫은 경우 포함 — 조용히 무시
    if (e?.code !== 'USER_CANCEL') toast(e.message || '결제 중 오류가 발생했습니다.')
  } finally {
    paymentLoading.value = false
  }
}

const sideDocCount = (t) => docs[t].length

onMounted(() => {
  if (route.query.section) section.value = route.query.section
  loadMyRooms()
  loadDocs('resume')
  loadDocs('portfolio')
})
</script>

<template>
  <main class="page active" id="page-mypage">
    <div class="container">
      <div class="mypage">
        <!-- Side menu -->
        <aside class="side-menu">
          <div class="side-profile">
            <div class="side-profile-avatar">{{ user?.userName?.charAt(0) }}</div>
            <div class="side-profile-name">{{ user?.userName }}</div>
            <div class="side-profile-email">{{ user?.userEmail }}</div>
          </div>
          <div class="side-section">
            <div class="side-section-title">면접 관리</div>
            <div class="side-item" :class="{ active: section === 'reports' || section === 'report-detail' }" @click="gotoMy('reports')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6M9 13h6M9 17h4" /></svg>
              리포트 <span class="count">{{ myRooms.length }}</span>
            </div>
          </div>
          <div class="side-section">
            <div class="side-section-title">서류 관리</div>
            <div class="side-item" :class="{ active: section === 'resume' }" @click="gotoMy('resume')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg>
              이력서/자기소개서 <span class="count">{{ sideDocCount('resume') }}</span>
            </div>
            <div class="side-item" :class="{ active: section === 'portfolio' }" @click="gotoMy('portfolio')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" /><circle cx="8.5" cy="8.5" r="1.5" /><path d="m21 15-5-5L5 21" /></svg>
              포트폴리오 <span class="count">{{ sideDocCount('portfolio') }}</span>
            </div>
          </div>
          <div class="side-section" style="border-bottom: none;">
            <div class="side-section-title">계정</div>
            <div class="side-item" :class="{ active: section === 'account' }" @click="gotoMy('account')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
              회원정보
            </div>
            <div class="side-item" :class="{ active: section === 'my-posts' }" @click="gotoMy('my-posts')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M8 12h8M8 16h5"/></svg>
              내가 쓴 글 <span class="count">{{ myPosts.length }}</span>
            </div>
            <div class="side-item" :class="{ active: section === 'billing' }" @click="gotoMy('billing')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2" /><path d="M2 10h20" /></svg>
              구독 및 결제
            </div>
          </div>
        </aside>

        <!-- Dynamic content -->
        <div id="mypage-content">

          <!-- ===== 면접 기록 목록 ===== -->
          <template v-if="section === 'reports'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 리포트</div>
            <div class="flex-between" style="margin:4px 0 20px;">
              <div><h2 class="mp-h1">리포트</h2><p class="mp-sub">완료한 모의 면접의 기록을 모아봤어요. 총 {{ myRooms.length }}건</p></div>
            </div>
            <div class="mp-stat-row">
              <div class="mp-stat"><div class="mp-stat-label">총 면접</div><div class="mp-stat-val">{{ myRooms.length }}<span>회</span></div></div>
              <div class="mp-stat"><div class="mp-stat-label">완료</div><div class="mp-stat-val" style="color:var(--green-500);">{{ myRooms.filter(r => r.status === 'COMPLETED').length }}<span>회</span></div></div>
              <div class="mp-stat"><div class="mp-stat-label">취소</div><div class="mp-stat-val" style="color:var(--ink-400);">{{ myRooms.filter(r => r.status === 'CANCELLED').length }}<span>회</span></div></div>
            </div>
            <div class="card" style="padding:0; overflow:hidden;">
              <div class="card-header" style="padding:18px 20px 14px; border-bottom:1px solid var(--ink-150); margin-bottom:0;">
                <h3 class="card-title">전체 리포트</h3><span class="text-sm text-muted">최신순</span>
              </div>
              <div v-if="roomsLoading" style="padding:48px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
              <div v-else-if="!myRooms.length" style="padding:48px; text-align:center; color:var(--ink-400);">면접 기록이 없습니다.</div>
              <table v-else class="history-table report-table">
                <thead><tr><th>기업 / 직무</th><th>지원유형</th><th>일시</th><th>난이도</th><th>상태</th><th></th><th></th></tr></thead>
                <tbody>
                  <tr v-for="room in myRooms" :key="room.roomId" :style="{ cursor: isReportOpen(room) ? 'pointer' : 'default' }" @click="isReportOpen(room) && openRoom(room)">
                    <td>
                      <div style="display:flex; align-items:center; gap:10px;">
                        <div class="company-logo" style="width:30px;height:30px;font-size:12px;">{{ room.companyName?.charAt(0) }}</div>
                        <div><strong>{{ room.companyName }}</strong><span v-if="room.jobName" class="text-muted"> | {{ room.jobName }}</span></div>
                      </div>
                    </td>
                    <td><span class="text-sm text-muted">{{ applicantTypeLabel(room.applicantType) }}</span></td>
                    <td><span class="text-sm text-muted">{{ formatDate(room.startedAt) }}</span></td>
                    <td><span class="badge">{{ diffLabel(room.difficulty) }}</span></td>
                    <td><span class="badge" :class="statusBadgeClass(room.status)">{{ statusLabel(room.status) }}</span></td>
                    <td>
                      <span v-if="isReportOpen(room)" class="report-go">리포트 보기 ›</span>
                      <span v-else class="text-sm text-muted" style="line-height:1.5;">
                        리포트 생성중
                        <template v-if="reportOpenAtLabel(room)"><br />{{ reportOpenAtLabel(room) }} 공개</template>
                      </span>
                    </td>
                    <td><button class="btn btn-sm btn-ghost doc-del" @click.stop="deleteRoom(room)">삭제</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- ===== 면접 기록 상세 ===== -->
          <template v-else-if="section === 'report-detail' && currentRoom">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> <a @click="gotoMy('reports')" style="cursor:pointer;">리포트</a> <span class="sep">›</span> {{ currentRoom.companyName }}</div>
            <div class="feedback-head" style="margin-top:8px;">
              <div class="feedback-co">
                <div class="company-logo" style="width:48px;height:48px;font-size:18px;border-radius:8px;">{{ currentRoom.companyName?.charAt(0) }}</div>
                <div>
                  <h2 class="feedback-title">{{ currentRoom.companyName }}<span v-if="currentRoom.jobName"> | {{ currentRoom.jobName }}</span></h2>
                  <div class="feedback-sub">{{ formatDateTime(currentRoom.startedAt) }} | 난이도 {{ diffLabel(currentRoom.difficulty) }} | {{ applicantTypeLabel(currentRoom.applicantType) }}</div>
                </div>
              </div>
              <div class="feedback-meta">
                <div>
                  <div class="feedback-meta-label">상태</div>
                  <div class="feedback-meta-val"><span class="badge" :class="statusBadgeClass(currentRoom.status)">{{ statusLabel(currentRoom.status) }}</span></div>
                </div>
              </div>
            </div>

            <div class="flex-between" style="margin-bottom:22px;">
              <div class="tabs" style="margin-bottom:0;">
                <div v-for="[k, label] in reportTabs" :key="k" class="tab" :class="{ active: reportTab === k }" @click="setTab(k)">{{ label }}</div>
              </div>
              <button v-if="roomReport" class="btn btn-sm btn-secondary" :disabled="pdfGenerating" @click="downloadReportPdf">
                {{ pdfGenerating ? 'PDF 생성 중...' : 'PDF 다운로드' }}
              </button>
            </div>

            <!-- 불러오는 중 -->
            <div v-if="scenariosLoading" style="padding:48px; text-align:center; color:var(--ink-400);">불러오는 중...</div>

            <!-- 리포트 있을 때: 기존 panelXxx UI 재사용 -->
            <div v-else-if="roomReport" ref="reportPanelRef" v-html="currentPanelHtml"></div>

            <!-- 리포트 없을 때: 모든 탭 공통 안내 -->
            <div v-else class="card" style="padding:56px 20px; text-align:center; color:var(--ink-400);">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin:0 auto 14px; display:block; opacity:.35;"><path d="M3 3v18h18"/><path d="M18 17V9M13 17V5M8 17v-3"/></svg>
              <div style="font-weight:600; margin-bottom:6px; color:var(--ink-700);">분석 데이터가 없습니다</div>
              <div class="text-sm text-muted">면접 종료 후 AI 분석이 완료되면 리포트가 제공됩니다.</div>
            </div>
          </template>

          <!-- ===== 서류 관리 ===== -->
          <template v-else-if="['resume', 'portfolio'].includes(section)">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 서류 관리 <span class="sep">›</span> {{ docMeta[section].title }}</div>
            <div class="flex-between" style="margin:4px 0 20px;">
              <div><h2 class="mp-h1">{{ docMeta[section].title }}</h2><p class="mp-sub">면접관이 {{ docMeta[section].title }} 기반으로 맞춤형 질문을 생성합니다. 총 {{ docs[section].length }}건</p></div>
            </div>
            <label class="doc-drop">
              <input type="file" :accept="docMeta[section].accept" style="display:none;" @change="uploadDoc(section, $event)" />
              <div class="doc-drop-icon"><svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg></div>
              <div class="doc-drop-title">{{ docMeta[section].title }} 업로드</div>
              <div class="doc-drop-hint">파일을 선택하거나 드래그하세요 | {{ docMeta[section].hint }}</div>
            </label>
            <div class="doc-list">
              <div v-if="docsLoading[section]" class="doc-empty">불러오는 중...</div>
              <div v-else-if="!docs[section].length" class="doc-empty">아직 업로드한 {{ docMeta[section].title }}가 없어요.</div>
              <div v-for="(d, i) in docs[section]" :key="d.id" class="doc-item">
                <div class="doc-ico"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg></div>
                <div class="doc-info"><div class="doc-name">{{ d.fileName }}</div><div class="doc-meta">업로드 {{ formatDateTime(d.createdAt) }}</div></div>
                <div class="doc-actions">
                  <a class="btn btn-sm btn-ghost" :href="d.downloadUrl" target="_blank" rel="noopener">다운로드</a>
                  <button class="btn btn-sm btn-ghost doc-del" @click="deleteDoc(section, i)">삭제</button>
                </div>
              </div>
            </div>
          </template>

          <!-- ===== 회원정보 ===== -->
          <template v-else-if="section === 'account'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 회원정보</div>
            <h2 class="mp-h1" style="margin:4px 0 20px;">회원정보</h2>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">기본 정보</h3>
                <button v-if="!isEditing && !isVerifying" class="btn btn-sm btn-secondary" @click="startEdit">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z" /></svg>
                  정보 수정
                </button>
                <button v-else-if="isEditing" class="btn btn-sm btn-primary" :disabled="editLoading" @click="submitEdit">
                  {{ editLoading ? '저장 중...' : '완료' }}
                </button>
              </div>

              <!-- 본인 확인: 정보 수정 전 현재 비밀번호 확인 -->
              <div v-if="isVerifying" style="margin:0 20px 16px; padding:14px 16px; background:var(--ink-50); border-radius:8px;">
                <p style="font-size:13px; color:var(--ink-600); margin-bottom:8px;">정보를 수정하려면 현재 비밀번호를 입력해주세요.</p>
                <div style="display:flex; align-items:center; gap:8px;">
                  <input class="input input-inline" type="password" v-model="verifyPwd" placeholder="현재 비밀번호" style="flex:1;" @keyup.enter="confirmPassword" autocomplete="current-password" />
                  <button class="btn btn-sm btn-primary" :disabled="verifyLoading" @click="confirmPassword">{{ verifyLoading ? '확인 중...' : '확인' }}</button>
                  <button class="btn btn-sm btn-ghost" @click="cancelVerify">취소</button>
                </div>
                <p v-if="verifyError" class="auth-error" style="margin-top:8px;">{{ verifyError }}</p>
              </div>

              <div class="info-grid">
                <div class="info-row"><div class="info-label">아이디</div><div class="info-val">{{ user?.userEmail }}</div></div>
                <div class="info-row">
                  <div class="info-label">이름</div>
                  <div class="info-val">
                    <input v-if="isEditing" class="input input-inline" v-model="editName" placeholder="이름" />
                    <span v-else>{{ user?.userName }}</span>
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">휴대폰 번호</div>
                  <div class="info-val">
                    <input v-if="isEditing" class="input input-inline" v-model="editPhone" placeholder="휴대폰 번호 (예: 010-1234-5678)" />
                    <span v-else>{{ user?.userPhone || '-' }}</span>
                  </div>
                </div>
                <div v-if="user?.hasPassword" class="info-row">
                  <div class="info-label">비밀번호</div>
                  <div class="info-val">
                    <template v-if="isEditing">
                      <input class="input input-inline" type="password" v-model="editPwd" placeholder="새 비밀번호 (변경 시 입력)" style="margin-bottom:6px;" autocomplete="new-password" />
                      <div style="display:flex; align-items:center; gap:8px;">
                        <input class="input input-inline" type="password" v-model="editPwdConfirm" placeholder="비밀번호 확인" autocomplete="new-password" />
                        <span v-if="editPwd && editPwdConfirm && editPwd !== editPwdConfirm" style="color:#e53e3e; font-size:12px; white-space:nowrap;">비밀번호가 일치하지 않습니다.</span>
                      </div>
                    </template>
                    <span v-else>••••••••</span>
                  </div>
                </div>
                <div v-else class="info-row">
                  <div class="info-label">로그인 방식</div>
                  <div class="info-val"><span class="text-muted">소셜 로그인 계정 (비밀번호 없음)</span></div>
                </div>
                <div class="info-row"><div class="info-label">가입일</div><div class="info-val">{{ user?.createdAt || '-' }}</div></div>
              </div>
              <p v-if="editError" class="auth-error" style="margin-top:8px;">{{ editError }}</p>
              <div style="margin-top:16px; padding-top:14px; border-top:1px solid var(--ink-150); text-align:right;">
                <button class="btn-withdraw" @click="showWithdrawConfirm = true">회원탈퇴</button>
              </div>
            </div>

            <!-- 회원탈퇴 확인 모달 -->
            <div v-if="showWithdrawConfirm" class="withdraw-overlay" @click.self="showWithdrawConfirm = false">
              <div class="withdraw-modal">
                <h3 class="withdraw-title">정말 탈퇴하시겠습니까?</h3>
                <p class="withdraw-desc">탈퇴 시 모든 면접 기록과 리포트가 삭제되며<br>복구할 수 없습니다.</p>
                <div class="withdraw-actions">
                  <button class="btn btn-secondary" @click="showWithdrawConfirm = false">취소</button>
                  <button class="btn btn-danger" :disabled="withdrawLoading" @click="withdrawUser">
                    {{ withdrawLoading ? '처리 중...' : '탈퇴하기' }}
                  </button>
                </div>
              </div>
            </div>
            <!--
            <div class="card" style="margin-top:16px;">
              <div class="card-header"><h3 class="card-title">알림 설정</h3><span class="text-sm text-muted">원하는 알림만 받아보세요</span></div>
              <div v-for="[key, label, desc] in notifMeta" :key="key" class="setting-row">
                <div><div class="setting-label">{{ label }}</div><div class="setting-desc">{{ desc }}</div></div>
                <label class="toggle"><input type="checkbox" v-model="notifPrefs[key]" @change="onNotifChange" /><span class="slider"></span></label>
              </div>
            </div>
            -->
          </template>

          <!-- ===== 내가 쓴 글 ===== -->
          <template v-else-if="section === 'my-posts'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 내가 쓴 글</div>
            <div class="flex-between" style="margin:4px 0 20px;">
              <div><h2 class="mp-h1">내가 쓴 글</h2><p class="mp-sub">커뮤니티에 작성한 게시글입니다. 총 {{ myPosts.length }}건</p></div>
            </div>
            <div class="card" style="padding:0; overflow:hidden;">
              <div v-if="myPostsLoading" style="padding:48px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
              <div v-else-if="!myPosts.length" style="padding:48px; text-align:center; color:var(--ink-400);">작성한 게시글이 없습니다.</div>
              <table v-else class="history-table">
                <thead><tr><th>분류</th><th>제목</th><th>날짜</th><th>댓글</th></tr></thead>
                <tbody>
                  <tr v-for="p in myPosts" :key="p.postId" style="cursor:pointer;" @click="router.push('/community/board/' + p.postId)">
                    <td><span class="badge">{{ p.category }}</span></td>
                    <td><strong>{{ p.title }}</strong></td>
                    <td><span class="text-sm text-muted">{{ formatDate(p.createdAt) }}</span></td>
                    <td><span class="text-sm text-muted">{{ p.commentCount }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- ===== 구독 및 결제 ===== -->
          <template v-else-if="section === 'billing'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 구독 및 결제</div>
            <h2 class="mp-h1" style="margin:4px 0 20px;">구독 및 결제</h2>

            <!-- 현재 이용권 현황 -->
            <div class="card plan-now" style="margin-bottom:24px;">
              <div>
                <div class="text-sm text-muted">보유 이용권</div>
                <div class="plan-now-name" style="font-size:28px; font-weight:700; color:var(--ink-900);">
                  {{ user?.ticketCount ?? 0 }}<span style="font-size:16px; font-weight:400; color:var(--ink-500); margin-left:4px;">회</span>
                </div>
                <div class="plan-now-meta">1회권 = 모의 면접 1회 이용</div>
              </div>
              <div style="text-align:right;">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--accent-blue,#1f6fe5)" stroke-width="1.5"><rect x="1" y="6" width="22" height="13" rx="2"/><path d="M1 10h22"/></svg>
              </div>
            </div>

            <!-- 요금제 선택 -->
            <h3 class="mp-section-title">이용권 충전</h3>
            <div class="plan-grid" style="grid-template-columns: repeat(4, 1fr);">
              <div v-for="p in TICKET_PLANS" :key="p.name" class="plan-card">
                <div class="plan-name">{{ p.name }}</div>
                <div class="plan-price">{{ p.priceLabel }}</div>
                <ul class="plan-feats">
                  <li>총 {{ p.tickets + p.bonus }}회 이용</li>
                  <li v-if="p.bonus > 0" style="color:var(--green-500); font-weight:600;">+ {{ p.bonus }}회 보너스 증정</li>
                  <li>회당 {{ Math.round(p.amount / (p.tickets + p.bonus)).toLocaleString() }}원</li>
                </ul>
                <button
                  class="btn btn-primary btn-block"
                  :disabled="paymentLoading"
                  @click="buyTickets(p)"
                >
                  {{ paymentLoading ? '처리 중...' : '구매하기' }}
                </button>
              </div>
            </div>

            <!-- 결제 내역 -->
            <h3 class="mp-section-title">결제 내역</h3>
            <div class="card" style="padding:0; overflow:hidden;">
              <div v-if="paymentHistoryLoading" style="padding:40px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
              <div v-else-if="!paymentHistory.length" style="padding:40px; text-align:center; color:var(--ink-400);">결제 내역이 없습니다.</div>
              <table v-else class="history-table">
                <thead><tr><th>날짜</th><th>내용</th><th>이용권</th><th>금액</th><th>상태</th></tr></thead>
                <tbody>
                  <tr v-for="h in paymentHistory" :key="h.paymentId">
                    <td><span class="text-sm text-muted">{{ formatDate(h.approvedAt || h.createdAt) }}</span></td>
                    <td>{{ h.planName }}</td>
                    <td><span class="text-sm">+{{ h.tickets }}회</span></td>
                    <td>{{ h.amount.toLocaleString() }}원</td>
                    <td>
                      <span class="badge" :class="h.status === 'DONE' ? 'badge-green' : h.status === 'FAILED' ? 'badge-red' : ''">
                        {{ h.status === 'DONE' ? '완료' : h.status === 'FAILED' ? '실패' : '처리 중' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { toast } from '../../../utils/toast.js'
import { fetchRoomScenarios, fetchRoomReport } from '../../../api/mypageApi.js'
import { reportPanel } from '../../../utils/mypageReport.js'

const props = defineProps({
  currentRoom: { type: Object, required: true },
  myRooms: { type: Array, default: () => [] },
})
const emit = defineEmits(['back'])

const reportTab = ref('overview')
const reportTabs = [
  ['overview', '종합 평가'], ['competency', '역량 분석'],
  ['applicants', '지원자 분석'], ['questions', '질문별 상세'],
]
function setTab(k) { reportTab.value = k; window.scrollTo(0, 0) }

const roomScenarios = ref([])
const scenariosLoading = ref(false)
const roomReport = ref(null)
const reportPanelRef = ref(null)
const pdfGenerating = ref(false)

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}
function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${formatDate(dateStr)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
function diffLabel(d) { return d === 'HARD' ? '상' : d === 'MEDIUM' ? '중' : '하' }
function statusLabel(s) { return s === 'COMPLETED' ? '완료' : s === 'IN_PROGRESS' ? '진행 중' : s === 'CANCELLED' ? '취소' : s || '-' }
function statusBadgeClass(s) { return s === 'COMPLETED' ? 'badge-green' : s === 'IN_PROGRESS' ? 'badge-blue' : '' }
function applicantTypeLabel(t) { return t === 'NEW' ? '신입' : t === 'INTERN' ? '인턴' : t === 'EXPERIENCED' ? '경력' : t || '-' }

function transformReportForPanel(data, scenarios = [], room = null, allRooms = []) {
  const rep = data.report
  const me = [rep.compExpertise ?? 0, rep.compLogic ?? 0, rep.compCommu ?? 0, rep.compCulture ?? 0, rep.compPressure ?? 0]

  const aiApps = data.applicants.filter(a => !a.isUser)
  const aiAvg = aiApps.length ? Math.round(aiApps.reduce((s, a) => s + (a.score ?? 70), 0) / aiApps.length) : 70

  const appNameMap = {}
  for (const a of data.applicants) {
    if (!a.isUser && a.applicantId) appNameMap[a.applicantId] = a.name ?? 'AI 지원자'
  }

  const questionMap = new Map()
  for (const q of data.questions) {
    if (!questionMap.has(q.questionSeq)) {
      questionMap.set(q.questionSeq, { q: `Q${q.questionSeq}`, text: q.questionText ?? '', participants: [] })
    }
    if (q.turnRole) {
      const labelColor = q.label === '우수' ? 'green' : q.label === '미흡' ? 'red' : 'amber'
      questionMap.get(q.questionSeq).participants.push({
        turnRole: q.turnRole,
        name: q.turnRole === 'USER' ? '나' : (appNameMap[q.turnRefId] ?? 'AI 지원자'),
        answerText: q.answerText ?? '',
        score: q.score ?? 0,
        label: q.label ? [q.label, labelColor] : null,
        body: q.feedback ?? '',
        tags: q.tags ? q.tags.split(',').map(t => t.trim()).filter(Boolean) : [],
      })
    }
  }

  const userAnswerSecs = scenarios
    .filter(s => s.turnRole === 'USER' && s.answerSec != null)
    .map(s => s.answerSec)
  const avgAnswerSec = userAnswerSecs.length
    ? Math.round(userAnswerSecs.reduce((sum, v) => sum + v, 0) / userAnswerSecs.length)
    : 0

  const typeMap = { NEW: '신입', INTERN: '인턴', EXPERIENCED: '경력' }
  const diffMap = { EASY: '쉬움', NORMAL: '보통', HARD: '어려움' }
  const info = room ? {
    company: room.companyName ?? '-',
    job: room.jobName ?? '-',
    type: typeMap[room.applicantType] ?? room.applicantType ?? '-',
    difficulty: diffMap[room.difficulty] ?? room.difficulty ?? '-',
    interviewerCnt: room.aiInterviewerCnt ?? 0,
    applicantCnt: room.aiApplicantCnt ?? 0,
    endedAt: room.endedAt ? new Date(room.endedAt).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }) : '-',
  } : null

  const scoreHistory = allRooms
    .filter(r => r.overallScore != null && r.endedAt)
    .sort((a, b) => new Date(a.endedAt) - new Date(b.endedAt))
    .map(r => ({
      date: new Date(r.endedAt).toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' }),
      score: r.overallScore,
      current: r.roomId === room?.roomId,
    }))

  return {
    score: rep.overallScore,
    aiComment: rep.aiComment,
    compDetails: [rep.compExpertiseDetail, rep.compLogicDetail, rep.compCommuDetail, rep.compCultureDetail, rep.compPressureDetail],
    insight: rep.insight ?? '',
    me,
    ai: me.map(() => aiAvg),
    speechWpm: rep.speechWpm,
    speechFiller: rep.speechFiller,
    checklist: rep.checklist ? JSON.parse(rep.checklist) : [],
    scoreHistory,
    info,
    speech: { avgLen: avgAnswerSec },
    applicants: data.applicants.map(a => ({
      name: a.name ?? (a.isUser ? '나' : 'AI 지원자'),
      score: a.score ?? 0,
      me: !!a.isUser,
      strength: a.strength ?? '',
      weak: a.weakness ?? '',
    })),
    questions: [...questionMap.values()],
  }
}

const currentPanelHtml = computed(() => {
  if (!roomReport.value) return ''
  return reportPanel(transformReportForPanel(roomReport.value, roomScenarios.value, props.currentRoom, props.myRooms), reportTab.value)
})

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
      await new Promise((resolve) => setTimeout(resolve, 200))

      const el = reportPanelRef.value
      if (!el) continue

      const titleEl = document.createElement('h2')
      titleEl.textContent = label
      titleEl.style.cssText = 'font-size:22px;font-weight:800;color:#111827;margin:0 0 16px;'
      el.prepend(titleEl)

      const canvas = await html2canvas(el, { scale: 2, useCORS: true, backgroundColor: '#ffffff' })
      el.removeChild(titleEl)

      const pxPerMm = canvas.width / contentWidth
      if (i > 0) pdf.addPage()

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
    pdf.save(`${props.currentRoom.companyName ?? '면접'}_면접리포트.pdf`)
  } catch {
    toast('PDF 생성에 실패했습니다.')
  } finally {
    reportTab.value = originalTab
    pdfGenerating.value = false
  }
}

onMounted(async () => {
  scenariosLoading.value = true
  try {
    const [scenarios, report] = await Promise.all([
      fetchRoomScenarios(props.currentRoom.roomId),
      fetchRoomReport(props.currentRoom.roomId).catch(() => null),
    ])
    roomScenarios.value = scenarios
    roomReport.value = report
  } catch (e) {
    toast(e.message)
  } finally {
    scenariosLoading.value = false
  }
})
</script>

<template>
  <div>
    <div class="breadcrumb">
      마이페이지 <span class="sep">›</span>
      <a @click="emit('back')" style="cursor:pointer;">리포트</a>
      <span class="sep">›</span> {{ currentRoom.companyName }}
    </div>
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

    <div v-if="scenariosLoading" style="padding:48px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
    <div v-else-if="roomReport" ref="reportPanelRef" v-html="currentPanelHtml"></div>
    <div v-else class="card" style="padding:56px 20px; text-align:center; color:var(--ink-400);">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin:0 auto 14px; display:block; opacity:.35;"><path d="M3 3v18h18"/><path d="M18 17V9M13 17V5M8 17v-3"/></svg>
      <div style="font-weight:600; margin-bottom:6px; color:var(--ink-700);">분석 데이터가 없습니다</div>
      <div class="text-sm text-muted">면접 종료 후 AI 분석이 완료되면 리포트가 제공됩니다.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useFlowStore } from '../../stores/flow.js'
import { useDataStore } from '../../stores/data.js'
import { startInterview } from '../../api/interviewRoomApi.js'

const router = useRouter()
const flow = useFlowStore()
const data = useDataStore()

const r = computed(() => data.rooms[flow.currentRoom])
const sub = computed(() =>
  r.value ? `회사명 : ${r.value.co} | 직무 : ${r.value.role}` : 'AI 면접관과 경쟁 지원자를 면접장에 입장시키는 중입니다.'
)

const interviewerCount = computed(() => r.value?.interviewerCount ?? 2)
const applicantCount = computed(() => r.value?.aiApplicantCount ?? 0)
const labels = computed(() => [
  '면접방 준비 중',
  `AI 면접관 ${interviewerCount.value}명 | AI 지원자 ${applicantCount.value}명 준비 중`,
  'AI 대본 생성 중',
])

const active = ref(1)
const errorMsg = ref('')
let timer = null

onMounted(() => {
  const apiPromise = startInterview(flow.roomId)
    .then(result => {
      flow.scenarios             = result.scenario              || []
      flow.interviewerPersonaIds = result.interviewerPersonaIds || []
      flow.applicantPersonaIds   = result.applicantPersonaIds   || []
      flow.personaNames            = result.personaNames            || {}
      flow.interviewerStopVideos   = result.interviewerStopVideos   || {}
      flow.interviewerMoveVideos   = result.interviewerMoveVideos   || {}
      flow.applicantStopVideos     = result.applicantStopVideos     || {}
      flow.applicantMoveVideos     = result.applicantMoveVideos     || {}
    })
    .catch(err => {
      errorMsg.value = err.message || 'AI 대본 생성에 실패했습니다.'
      clearInterval(timer)
    })

  let idx = 1
  timer = setInterval(() => {
    idx++
    active.value = idx
    if (idx >= labels.value.length) {
      clearInterval(timer)
      // 에러 없을 때만 면접 화면으로 진입
      apiPromise.then(() => setTimeout(() => router.push(`/interview/${flow.roomId}`), 3000))
    }
  }, 850)
})
onUnmounted(() => clearInterval(timer))

function stepClass(i) {
  if (i < active.value) return 'done'
  if (i === active.value) return 'active'
  return ''
}
</script>

<template>
  <main class="page active wait-page" id="page-prep">
    <div class="wait-screen">
      <div class="wait-spinner"><span></span><span></span><span></span></div>
      <h1 class="wait-title">면접을 준비하고 있어요</h1>
      <p class="wait-sub">{{ sub }}</p>
      <div class="wait-steps">
        <div v-for="(l, i) in labels" :key="i" class="wait-step" :class="stepClass(i)">{{ l }}</div>
      </div>
      <p style="margin-top:18px;color:#e53e3e;font-size:13px;font-weight:600;">
        면접방을 생성하는 중입니다. 창을 닫거나 다른 페이지로 이동하지 마세요.
      </p>
    </div>

    <!-- 이용권 부족 모달 -->
    <div v-if="errorMsg" style="position:fixed;inset:0;background:rgba(0,0,0,0.45);display:flex;align-items:center;justify-content:center;z-index:9999;">
      <div style="background:#fff;border-radius:16px;padding:40px 36px;max-width:360px;width:90%;text-align:center;box-shadow:0 8px 32px rgba(0,0,0,0.15);">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#e53e3e" stroke-width="1.8" stroke-linecap="round" style="margin:0 auto 14px;display:block;"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="13"/><circle cx="12" cy="16.5" r="0.8" fill="#e53e3e" stroke="none"/></svg>
        <h3 style="font-size:18px;font-weight:700;color:#111;margin-bottom:10px;">이용권이 부족해요</h3>
        <p style="font-size:14px;color:#6b7280;margin-bottom:24px;">{{ errorMsg }}<br/>이용권을 충전하고 면접을 시작해보세요.</p>
        <button
          style="width:100%;padding:12px 0;background:#2c7a4b;color:#fff;border:none;border-radius:10px;font-size:15px;font-weight:600;cursor:pointer;"
          @click="router.push('/mypage?section=billing')"
        >이용권 충전하러 가기</button>
      </div>
    </div>
  </main>
</template>


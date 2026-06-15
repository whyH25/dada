<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useFlowStore } from '../stores/flow.js'
import { useDataStore } from '../stores/data.js'
import { startInterview } from '../api/interviewRoomApi.js'

const router = useRouter()
const flow = useFlowStore()
const data = useDataStore()

const r = computed(() => data.rooms[flow.currentRoom])
const sub = computed(() =>
  r.value ? `${r.value.co} ${r.value.role} 면접장에 입장하고 있어요.` : 'AI 면접관과 경쟁 지원자를 면접장에 입장시키는 중입니다.'
)

const interviewerCount = computed(() => r.value?.interviewerCount ?? 2)
const labels = computed(() => [
  '면접방 입장 확인',
  `AI 면접관 ${interviewerCount.value}명 준비`,
  'AI 대본 생성 중',
  '카메라 | 마이크 연결',
])

const active = ref(1)
const errorMsg = ref('')
let timer = null

onMounted(() => {
  // AI 대본 생성 API 호출과 애니메이션 동시 진행
  const apiPromise = startInterview(flow.roomId)
    .then(result => {
      flow.scenarios             = result.scenario              || []
      flow.interviewerPersonaIds = result.interviewerPersonaIds || []
      flow.applicantPersonaIds   = result.applicantPersonaIds   || []
      flow.personaNames          = result.personaNames          || {}
    })
    .catch(err => { errorMsg.value = err.message || 'AI 대본 생성에 실패했습니다.' })

  let idx = 1
  timer = setInterval(() => {
    idx++
    active.value = idx
    if (idx >= labels.value.length) {
      clearInterval(timer)
      // 애니메이션 완료 후 API 응답 대기, 실패해도 진입
      apiPromise.finally(() => setTimeout(() => router.push('/interview'), 700))
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
    </div>
  </main>
</template>

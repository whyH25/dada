<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useFlowStore } from '../stores/flow.js'

const router = useRouter()
const flow = useFlowStore()
const labels = [  '면접 기록 저장 중',
                  '사용자 답변 분석 중',
                  'AI 리포트 생성 중',]
const active = ref(1)
let timer = null

onMounted(() => {
  // 리포트 생성 API fire-and-forget (결과를 기다리지 않고 화면 전환)
  if (flow.roomId) {
    fetch(`http://localhost:8080/api/interview-rooms/${flow.roomId}/report`, {
      method: 'POST',
      credentials: 'include',
    }).catch(err => console.warn('리포트 생성 요청 실패:', err))
  }

  let idx = 1
  timer = setInterval(() => {
    idx++
    active.value = idx
    if (idx >= labels.length) {
      clearInterval(timer)
      setTimeout(() => router.push('/done'), 2500)
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
  <main class="page active wait-page" id="page-saving">
    <div class="wait-screen">
      <div class="wait-spinner"><span></span><span></span><span></span></div>
      <h1 class="wait-title">면접 기록을 안전하게 저장하고 있습니다.</h1>
      <p class="wait-sub">발화 내용을 분석하고 리포트를 생성하는 중입니다. <br />잠시만 기다려 주세요.</p>
      <div class="wait-steps">
        <div v-for="(l, i) in labels" :key="i" class="wait-step" :class="stepClass(i)">{{ l }}</div>
      </div>
    </div>
  </main>
</template>

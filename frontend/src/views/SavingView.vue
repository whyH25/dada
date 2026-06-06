<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const labels = ['음성 · 영상 업로드', '답변 텍스트 변환', '역량별 점수 산출', '리포트 발행']
const active = ref(1)
let timer = null

onMounted(() => {
  let idx = 1
  timer = setInterval(() => {
    idx++
    active.value = idx
    if (idx >= labels.length) {
      clearInterval(timer)
      setTimeout(() => router.push('/done'), 700)
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
      <h1 class="wait-title">면접 기록을 저장하고 있어요</h1>
      <p class="wait-sub">발화 내용을 분석하고 리포트를 생성하는 중입니다. 잠시만 기다려 주세요.</p>
      <div class="wait-steps">
        <div v-for="(l, i) in labels" :key="i" class="wait-step" :class="stepClass(i)">{{ l }}</div>
      </div>
    </div>
  </main>
</template>

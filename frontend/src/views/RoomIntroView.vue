<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { useFlowStore } from '../stores/flow.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()
const flow = useFlowStore()

const idx = computed(() => Number(route.params.id) || 0)
const r = computed(() => data.rooms[idx.value])
const info = computed(() => data.infoFor(r.value.co))
const di = computed(() => data.diffInfo[r.value.diff] || data.diffInfo['중'])

function start() {
  flow.currentRoom = idx.value
  router.push('/prep')
}
</script>

<template>
  <main class="page active" id="page-room-intro">
    <div class="container" style="max-width:840px;">
      <div class="page-header" style="border-bottom:none; padding-bottom:8px; margin-bottom:8px;">
        <div class="breadcrumb">홈 <span class="sep">›</span> <a @click="router.push('/rooms')" style="cursor:pointer;">100대기업 면접방</a> <span class="sep">›</span> {{ r.co }}</div>
      </div>
      <div class="ri-hero">
        <div class="company-logo" :class="r.logo" style="width:56px;height:56px;font-size:22px;border-radius:12px;">{{ r.short }}</div>
        <div class="ri-hero-text">
          <div class="ri-co">{{ r.co }} <span class="ri-industry">{{ info.industry }}</span></div>
          <h1 class="ri-title">{{ r.title }}</h1>
          <div class="ri-role">{{ r.role }}</div>
        </div>
        <span v-if="r.mine" class="badge badge-green" style="align-self:flex-start;">참여완료 · {{ r.sessions }}회</span>
        <span v-else class="badge badge-outline" style="align-self:flex-start;">진행 전</span>
      </div>

      <div class="ri-info-grid">
        <div class="ri-info"><div class="ri-info-label">면접 유형</div><div class="ri-info-val">다대다 (면접관 2 : 지원자 3)</div></div>
        <div class="ri-info"><div class="ri-info-label">난이도</div><div class="ri-info-val">{{ di.label }}</div></div>
        <div class="ri-info"><div class="ri-info-label">면접관</div><div class="ri-info-val">AI 면접관 2명</div></div>
        <div class="ri-info"><div class="ri-info-label">경쟁 지원자</div><div class="ri-info-val">AI 지원자 2명 + 본인</div></div>
        <div class="ri-info"><div class="ri-info-label">질문 구성</div><div class="ri-info-val">기출 기반 8문항</div></div>
        <div class="ri-info"><div class="ri-info-label">예상 소요</div><div class="ri-info-val">30 ~ 40분</div></div>
      </div>

      <div class="ri-section">
        <h3 class="ri-section-title">이런 면접이에요</h3>
        <p class="ri-section-desc">{{ di.desc }}</p>
        <ul class="ri-list"><li v-for="(s, i) in info.style" :key="i">{{ s }}</li></ul>
      </div>

      <div class="ri-section">
        <h3 class="ri-section-title">평가 항목</h3>
        <div class="chip-group" style="pointer-events:none;">
          <div class="chip">직무 전문성</div><div class="chip">논리적 사고</div><div class="chip">커뮤니케이션</div><div class="chip">조직 적합성</div><div class="chip">압박 대응</div>
        </div>
      </div>

      <div class="ri-actions">
        <button class="btn btn-ghost" @click="router.push('/rooms')">목록으로</button>
        <button class="btn btn-primary btn-lg" @click="start">
          면접 시작하기
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
        </button>
      </div>
    </div>
  </main>
</template>

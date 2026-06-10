<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()
const auth = useAuthStore()

const query = ref('')
// 상단 검색에서 ?q= 로 들어온 경우 반영 (원본 globalSearch)
onMounted(() => { if (route.query.q) query.value = String(route.query.q) })

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  return data.rooms
    .map((r, i) => ({ r, i }))
    .filter(({ r }) => !q || (r.co + ' ' + r.role + ' ' + r.title).toLowerCase().includes(q))
})
const resultLabel = computed(() =>
  query.value.trim() ? `'${query.value.trim()}' 검색 결과 ${filtered.value.length}개` : ''
)

// 원본 enterRoom(): 로그인 게이트 → 면접방 소개
function enterRoom(i) {
  if (auth.isLoggedIn) router.push('/room-intro/' + i)
  else auth.openLogin('room-intro/' + i)
}
</script>

<template>
  <main class="page active" id="page-rooms">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 면접 <span class="sep">›</span> 100대기업 면접방</div>
        <div class="flex-between">
          <div>
            <h1 class="page-title">100대기업 면접방</h1>
            <p class="page-subtitle">실제 채용 트렌드를 반영한 기업|직무별 모의 면접방. 총 <strong>312개</strong></p>
            <div class="rooms-result-count">{{ resultLabel }}</div>
          </div>
        </div>
      </div>

      <!-- Filter bar -->
      <div class="filter-bar">
        <div style="flex:1; max-width:360px; position:relative;">
          <svg style="position:absolute; left:12px; top:50%; transform:translateY(-50%); color:var(--ink-400);" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" /></svg>
          <input class="input" v-model="query" placeholder="기업명, 직무, 키워드로 검색" style="padding-left:34px;" />
        </div>
        <select class="select" style="width:140px;"><option>전체 산업군</option><option>IT | 인터넷</option><option>제조 | 반도체</option><option>금융</option><option>유통 | 커머스</option></select>
        <select class="select" style="width:140px;"><option>전체 직무</option><option>개발</option><option>데이터</option><option>기획</option><option>디자인</option></select>
        <select class="select" style="width:120px;"><option>최신순</option><option>참여 많은순</option><option>난이도순</option></select>
        <button class="btn btn-ghost" @click="query = ''">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M7 12h10M11 18h2" /></svg>
          필터 초기화
        </button>
      </div>

      <div class="filter-chips">
        <div class="filter-chip active">전체 | 312</div>
        <div class="filter-chip">IT | 인터넷 | 142</div>
        <div class="filter-chip">제조 | 반도체 | 68</div>
        <div class="filter-chip">금융 | 41</div>
        <div class="filter-chip">유통 | 커머스 | 33</div>
        <div class="filter-chip">컨설팅 | 18</div>
        <div class="filter-chip">기타 | 10</div>
      </div>

      <div class="rooms-grid">
        <div v-if="!filtered.length" class="rooms-empty">
          '{{ query.trim() }}' 에 해당하는 면접방이 없어요.<br />다른 기업명이나 직무로 검색해 보세요.
        </div>
        <div v-for="{ r, i } in filtered" :key="i" class="room-card" @click="enterRoom(i)">
          <div class="room-top">
            <div class="room-co">
              <div class="company-logo" :class="r.logo">{{ r.short }}</div>
              <div><div class="room-co-name">{{ r.co }}</div><div class="room-co-role">{{ r.role }}</div></div>
            </div>
            <span v-if="r.mine" class="badge badge-green">참여완료 | {{ r.sessions }}회</span>
            <span v-else class="badge badge-outline">진행 전</span>
          </div>
          <h3 class="room-title">{{ r.title }}</h3>
          <div class="room-tags">
            <span class="badge">난이도 {{ r.diff }}</span>
            <span class="badge badge-blue">기출 기반</span>
            <span class="badge badge-outline">자동 피드백</span>
          </div>
          <div class="room-stats">
            <div><div class="room-stat-label">생성일</div><div class="room-stat-val">{{ r.date }}</div></div>
            <div><div class="room-stat-label">참여 인원</div><div class="room-stat-val">{{ r.count.toLocaleString() }}명</div></div>
            <div><div class="room-stat-label">평균 점수</div><div class="room-stat-val">{{ 72 + (r.count % 13) }}점</div></div>
          </div>
          <div class="room-foot">
            <span class="text-sm text-muted">예상 소요 30~40분</span>
            <button class="btn btn-sm btn-primary" @click.stop="enterRoom(i)">입장하기 →</button>
          </div>
        </div>
      </div>

      <div style="display:flex; justify-content:center; gap:6px; margin-top:32px;">
        <button class="cal-nav-btn"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6" /></svg></button>
        <button class="filter-chip active" style="border-radius:4px; min-width:32px; text-align:center;">1</button>
        <button class="filter-chip" style="border-radius:4px; min-width:32px; text-align:center;">2</button>
        <button class="filter-chip" style="border-radius:4px; min-width:32px; text-align:center;">3</button>
        <button class="filter-chip" style="border-radius:4px; min-width:32px; text-align:center;">4</button>
        <button class="filter-chip" style="border-radius:4px; min-width:32px; text-align:center;">5</button>
        <button class="cal-nav-btn"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6" /></svg></button>
      </div>
    </div>
  </main>
</template>

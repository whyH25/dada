<script setup>
import { ref, computed } from 'vue'

const cat = ref('all')
const notices = [
  { cat: 'notice', badge: 'badge-red', badgeText: '중요', title: '26기 상반기 채용 일정 일괄 업데이트 안내', sub: '100대기업 채용 일정을 갱신했습니다. 캘린더에서 서류 마감일을 확인하세요.', date: '26.05.18', pinned: true },
  { cat: 'update', badge: 'badge-green', badgeText: 'UPDATE', title: 'v1.4 — AI 면접관 ‘압박형’ 페르소나 추가', sub: '난이도 ‘상’ 선택 시 자동 적용됩니다.', date: '26.05.20' },
  { cat: 'maint', badge: 'badge-amber', badgeText: '점검', title: '5월 25일(일) 새벽 02:00 ~ 04:00 정기 점검', sub: '해당 시간 동안 면접방 입장이 제한됩니다.', date: '26.05.15' },
  { cat: 'event', badge: 'badge-blue', badgeText: '이벤트', title: '합격 수기 작성하고 프리미엄 한 달 무료', sub: '~ 26.06.30 까지 진행됩니다.', date: '26.05.10' },
  { cat: 'update', badge: 'badge-green', badgeText: 'UPDATE', title: '발화 분석 리포트에 ‘필러 단어’ 지표 추가', sub: '‘음/어’ 등 군더더기 표현을 자동으로 집계합니다.', date: '26.05.06' },
  { cat: 'notice', badge: '', badgeText: '공지', title: '개인정보처리방침 개정 안내 (시행 26.05.01)', sub: '서류 보관 기간 관련 내용이 변경되었습니다.', date: '26.04.24' },
  { cat: 'event', badge: 'badge-blue', badgeText: '이벤트', title: '친구 초대하고 모의 면접 이용권 받기', sub: '초대 1명당 1회씩, 친구도 첫 면접 무료. ~ 26.06.15', date: '26.04.20' },
  { cat: 'maint', badge: 'badge-amber', badgeText: '점검', title: '5월 8일(목) 03:00 ~ 03:40 DB 점검 안내', sub: '리포트 발행이 일시적으로 지연될 수 있습니다.', date: '26.05.04' },
]
const tabs = [['all', '전체'], ['update', '업데이트'], ['maint', '점검'], ['event', '이벤트']]
const shown = computed(() => notices.filter((n) => cat.value === 'all' || n.cat === cat.value))
</script>

<template>
  <main class="page active" id="page-notices">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 공지사항</div>
        <h1 class="page-title">공지사항</h1>
        <p class="page-subtitle">서비스 업데이트와 점검, 이벤트 소식을 확인하세요.</p>
      </div>

      <div class="seg-tabs">
        <div v-for="[c, label] in tabs" :key="c" class="seg-tab" :class="{ active: cat === c }" @click="cat = c">{{ label }}</div>
      </div>

      <div class="card list-card">
        <div v-for="(n, i) in shown" :key="i" class="notice-item" :class="{ pinned: n.pinned }">
          <span class="badge" :class="n.badge">{{ n.badgeText }}</span>
          <div class="list-item-text"><div class="list-item-title">{{ n.title }}</div><div class="list-item-sub">{{ n.sub }}</div></div>
          <span class="list-item-meta">{{ n.date }}</span>
        </div>
        <div v-if="!shown.length" class="notice-empty">해당 분류의 공지사항이 없어요.</div>
      </div>
    </div>
  </main>
</template>

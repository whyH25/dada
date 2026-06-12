<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchNotices } from '../api/noticesApi.js'

const router = useRouter()
const notices = ref([])
const cat = ref('all')

const CATEGORY_META = {
  '업데이트': { badge: 'badge-green',  label: 'UPDATE' },
  '공지':     { badge: '',             label: '공지' },
  '이벤트':   { badge: 'badge-blue',   label: '이벤트' },
}

const tabs = [
  ['all',   '전체'],
  ['업데이트', '업데이트'],
  ['공지',    '공지'],
  ['이벤트',  '이벤트'],
]

const shown = computed(() =>
  cat.value === 'all' ? notices.value : notices.value.filter(n => n.category === cat.value)
)

function badgeMeta(category) {
  return CATEGORY_META[category] || { badge: '', label: category }
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(2, 10).replace(/-/g, '.')
}

onMounted(async () => {
  notices.value = await fetchNotices()
})
</script>

<template>
  <main class="page active" id="page-notices">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 공지사항</div>
        <h1 class="page-title">공지사항</h1>
        <p class="page-subtitle">서비스 업데이트와 이벤트 소식을 확인하세요.</p>
      </div>

      <div class="seg-tabs">
        <div v-for="[c, label] in tabs" :key="c" class="seg-tab" :class="{ active: cat === c }" @click="cat = c">
          {{ label }}
        </div>
      </div>

      <div class="card list-card">
        <div
          v-for="n in shown"
          :key="n.noticeId"
          class="notice-item"
          @click="router.push('/notices/' + n.noticeId)"
          style="cursor:pointer;"
        >
          <span class="badge" :class="badgeMeta(n.category).badge">{{ badgeMeta(n.category).label }}</span>
          <div class="list-item-text">
            <div class="list-item-title">{{ n.title }}</div>
          </div>
          <span class="list-item-meta">{{ formatDate(n.createdAt) }}</span>
        </div>
        <div v-if="shown.length === 0" class="notice-empty">해당 분류의 공지사항이 없어요.</div>
      </div>
    </div>
  </main>
</template>

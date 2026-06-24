<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchNotices } from '../api/noticesApi.js'

const router = useRouter()
const notices = ref([])
const cat = ref('all')

const CATEGORY_META = {
  '공지':   { badge: 'badge-green', label: '공지' },
  '이벤트': { badge: 'badge-blue',  label: '이벤트' },
}

const tabs = [
  ['all',   '전체'],
  ['공지',    '공지'],
  ['이벤트',  '이벤트'],
]

const page = ref(1)
const PAGE_SIZE = 20

const filtered = computed(() =>
  cat.value === 'all' ? notices.value : notices.value.filter(n => n.category === cat.value)
)
const totalPages = computed(() => Math.ceil(filtered.value.length / PAGE_SIZE))
const shown = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return filtered.value.slice(start, start + PAGE_SIZE)
})

function setCat(val) { cat.value = val; page.value = 1 }

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
        <div v-for="[c, label] in tabs" :key="c" class="seg-tab" :class="{ active: cat === c }" @click="setCat(c)">
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

      <div v-if="totalPages > 1" class="pagination">
        <button class="pg-btn" :disabled="page === 1" @click="page--">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <button
          v-for="p in totalPages" :key="p"
          class="pg-num" :class="{ active: page === p }"
          @click="page = p"
        >{{ p }}</button>
        <button class="pg-btn" :disabled="page === totalPages" @click="page++">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>
    </div>
  </main>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 24px;
  padding-bottom: 48px;
}
.pg-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--border, #e5e7eb); border-radius: 8px;
  background: #fff; color: var(--ink-500); cursor: pointer; transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) { border-color: var(--green-500); color: var(--green-500); }
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-num {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--border, #e5e7eb); border-radius: 8px;
  background: #fff; font-size: 14px; font-weight: 500;
  color: var(--ink-600); cursor: pointer; transition: all 0.15s;
}
.pg-num:hover { border-color: var(--green-500); color: var(--green-500); }
.pg-num.active { background: var(--green-500, #308860); border-color: var(--green-500, #308860); color: #fff; font-weight: 700; }
</style>

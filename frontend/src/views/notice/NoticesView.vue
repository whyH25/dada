<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchNotices } from '../../api/noticesApi.js'

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
const query = ref('')
const inputVal = ref('')

function doSearch() { query.value = inputVal.value.trim(); page.value = 1 }
function onKeydown(e) { if (e.key === 'Enter') doSearch() }

const filtered = computed(() => {
  let list = cat.value === 'all' ? notices.value : notices.value.filter(n => n.category === cat.value)
  if (query.value) {
    const q = query.value.toLowerCase()
    list = list.filter(n => n.title.toLowerCase().includes(q))
  }
  return list
})
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

      <div class="notice-search">
        <input
          v-model="inputVal"
          class="notice-search-input"
          placeholder="키워드를 입력해보세요."
          @keydown="onKeydown"
        />
        <button class="notice-search-btn" @click="doSearch">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          검색
        </button>
      </div>

      <div class="seg-tabs">
        <div v-for="[c, label] in tabs" :key="c" class="seg-tab" :class="{ active: cat === c }" @click="setCat(c)">
          {{ label }}
        </div>
      </div>

      <div v-if="shown.length === 0" class="notice-empty">해당 분류의 공지사항이 없어요.</div>
      <div v-else class="notice-list">
        <div
          v-for="n in shown"
          :key="n.noticeId"
          class="ni-item"
          @click="router.push('/notices/' + n.noticeId)"
        >
          <div class="ni-meta-top">
            <span class="badge badge-sm" :class="badgeMeta(n.category).badge">{{ badgeMeta(n.category).label }}</span>
          </div>
          <div class="ni-title">{{ n.title }}</div>
          <div class="ni-meta">
            <span>{{ formatDate(n.createdAt) }}</span>
            <span class="dot-sep"></span>
            <span>조회 {{ n.views }}</span>
          </div>
        </div>
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
.notice-search {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1.5px solid #d8e6dc;
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 20px;
  transition: border-color 0.15s;
}
.notice-search:focus-within { border-color: #308860; }
.notice-search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 11px 18px;
  font-size: 14px;
  color: var(--ink-900);
  background: transparent;
}
.notice-search-input::placeholder { color: #aab5ae; }
.notice-search-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: var(--green-500);
  color: #ffffff;
  border: none;
  padding: 9px 18px;
  margin: 4px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
}
.notice-search-btn:hover { background: var(--green-550); }

/* 커뮤니티 cat-tab과 동일한 크기로 seg-tab 오버라이드 */
.seg-tabs { margin-bottom: 16px; }
.seg-tab {
  padding: 5px 14px;
  font-size: 13px;
}

/* 공지 목록 — 커뮤니티 스타일 */
.notice-list { display: flex; flex-direction: column; }
.ni-item {
  display: flex;
  flex-direction: column;
  padding: 16px 4px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  cursor: pointer;
  transition: background 0.12s;
}
.ni-item:hover { background: var(--ink-50, #f8f9fa); }
.ni-meta-top { margin-bottom: 6px; }
.ni-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-900, #111827);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}
.ni-meta {
  font-size: 12px;
  color: var(--ink-400, #9ca3af);
  display: flex;
  align-items: center;
  gap: 4px;
}
.badge-sm { font-size: 11px; padding: 2px 8px; }
.badge-blue   { background: #eff6ff; color: #1d4ed8; border-radius: 99px; }
.badge-green  { background: #f0fdf4; color: #15803d; border-radius: 99px; }
.notice-empty {
  text-align: center;
  padding: 60px 0;
  color: var(--ink-400, #9ca3af);
}

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

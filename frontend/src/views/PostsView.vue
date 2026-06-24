<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchPosts } from '../api/postsApi.js'

const router = useRouter()
const auth = useAuthStore()

const CATS = ['전체', '면접 후기', '질문', '스터디 모집', '기타']
const CAT_CLS = { '면접 후기': 'badge-blue', '질문': 'badge-green', '스터디 모집': 'badge-purple', '기타': '' }
const SORTS = [
  { key: 'latest', label: '최신순' },
  { key: 'views',  label: '조회순' },
  { key: 'likes',  label: '좋아요순' },
]

const posts    = ref([])
const loading  = ref(false)
const activeCat = ref('전체')
const keyword  = ref('')
const sort     = ref('latest')
const page     = ref(1)
const total    = ref(0)
const size     = ref(20)

const totalPages = () => Math.max(1, Math.ceil(total.value / size.value))

async function load() {
  loading.value = true
  const res = await fetchPosts(
    activeCat.value === '전체' ? '' : activeCat.value,
    keyword.value,
    sort.value,
    page.value
  )
  posts.value  = res.posts
  total.value  = res.total
  loading.value = false
}

function search() {
  page.value = 1
  load()
}

function goWrite() {
  const cat = activeCat.value === '전체' ? '기타' : activeCat.value
  if (!auth.isLoggedIn) {
    auth.openLogin(() => router.push({ path: '/community/board/new', query: { category: cat } }))
    return
  }
  router.push({ path: '/community/board/new', query: { category: cat } })
}

function formatDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  const now = new Date()
  const diff = now - dt
  if (diff < 60000) return '방금 전'
  if (diff < 3600000) return Math.floor(diff / 60000) + '분 전'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '시간 전'
  return String(d).slice(0, 10).replace(/-/g, '.')
}

watch(activeCat, () => { page.value = 1; load() })
watch(sort, () => { page.value = 1; load() })
onMounted(load)
</script>

<template>
  <div class="comm-panel">
    <!-- 툴바: 검색 + 정렬 + 글쓰기 -->
    <div class="board-toolbar">
      <div class="cat-tabs">
        <button
          v-for="c in CATS" :key="c"
          class="cat-tab" :class="{ active: activeCat === c }"
          @click="activeCat = c"
        >{{ c }}</button>
      </div>
      <button class="btn btn-primary btn-sm" @click="goWrite">글쓰기</button>
    </div>

    <div class="board-filters">
      <div class="search-box">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/>
        </svg>
        <input
          v-model="keyword"
          placeholder="제목 또는 내용 검색"
          @keydown.enter="search"
        />
      </div>
      <div class="sort-tabs">
        <button
          v-for="s in SORTS" :key="s.key"
          class="sort-tab" :class="{ active: sort === s.key }"
          @click="sort = s.key"
        >{{ s.label }}</button>
      </div>
    </div>

    <div v-if="loading" class="board-empty">불러오는 중...</div>
    <div v-else-if="posts.length === 0" class="board-empty">
      {{ keyword ? `"${keyword}" 검색 결과가 없습니다.` : '게시글이 없습니다.' }}
    </div>

    <div v-else class="board-list">
      <div
        v-for="p in posts" :key="p.postId"
        class="board-item"
        @click="router.push('/community/board/' + p.postId)"
      >
        <div class="bi-main">
          <div class="bi-meta-top">
            <span class="badge badge-sm" :class="CAT_CLS[p.category] || ''">{{ p.category }}</span>
          </div>
          <div class="bi-title">{{ p.title }}</div>
          <div class="bi-meta">
            <span>{{ p.authorName }}</span>
            <span class="dot-sep"></span>
            <span>{{ formatDate(p.createdAt) }}</span>
            <span class="dot-sep"></span>
            <span>댓글 {{ p.commentCount }}</span>
            <span class="dot-sep"></span>
            <span>조회 {{ p.views }}</span>
            <span class="dot-sep"></span>
            <span>좋아요 {{ p.likes }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages() > 1" class="pagination">
      <button class="pg-btn" :disabled="page <= 1" @click="page--; load()">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M15 18l-6-6 6-6"/></svg>
      </button>
      <button
        v-for="n in totalPages()" :key="n"
        class="pg-num" :class="{ active: page === n }"
        @click="page = n; load()"
      >{{ n }}</button>
      <button class="pg-btn" :disabled="page >= totalPages()" @click="page++; load()">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M9 18l6-6-6-6"/></svg>
      </button>
    </div>

    <div class="board-total">총 {{ total }}개</div>
  </div>
</template>

<style scoped>
.board-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.cat-tabs { display: flex; gap: 6px; flex-wrap: wrap; }
.cat-tab {
  padding: 5px 14px;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--border, #e5e7eb);
  background: #fff;
  color: var(--ink-600, #4b5563);
  cursor: pointer;
  transition: all 0.15s;
}
.cat-tab.active {
  background: var(--green-500, #308860);
  color: #fff;
  border-color: var(--green-500, #308860);
}

.board-filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 8px;
  padding: 7px 12px;
  background: #fff;
  flex: 1;
  color: var(--ink-400);
}
.search-box input {
  border: none;
  outline: none;
  font-size: 13px;
  color: var(--ink-800);
  width: 100%;
  background: transparent;
}

.sort-tabs { display: flex; gap: 12px; flex-shrink: 0; }
.sort-tab {
  padding: 0;
  font-size: 13px;
  font-weight: 500;
  border: none;
  background: none;
  color: var(--ink-400);
  cursor: pointer;
  transition: color 0.15s;
}
.sort-tab:hover { color: var(--ink-700); }
.sort-tab.active {
  color: var(--ink-900, #111827);
  font-weight: 700;
}

.board-empty {
  text-align: center;
  padding: 60px 0;
  color: var(--ink-400, #9ca3af);
}

.board-list {
  display: flex;
  flex-direction: column;
  border-top: 1px solid var(--border, #e5e7eb);
}

.board-item {
  display: flex;
  align-items: center;
  padding: 16px 4px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  cursor: pointer;
  transition: background 0.12s;
}
.board-item:hover { background: var(--ink-50, #f8f9fa); }

.bi-main { flex: 1; min-width: 0; }
.bi-meta-top { margin-bottom: 6px; }
.bi-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-900, #111827);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}
.bi-meta {
  font-size: 12px;
  color: var(--ink-400, #9ca3af);
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.badge-sm { font-size: 11px; padding: 2px 8px; }
.badge-blue   { background: #eff6ff; color: #1d4ed8; border-radius: 99px; }
.badge-green  { background: #f0fdf4; color: #15803d; border-radius: 99px; }
.badge-purple { background: #faf5ff; color: #7c3aed; border-radius: 99px; }

/* 페이지네이션 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 20px 0 8px;
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

.board-total {
  text-align: right;
  font-size: 12px;
  color: var(--ink-400);
  padding: 4px 0 8px;
}
</style>

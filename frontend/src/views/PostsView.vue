<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchPosts } from '../api/postsApi.js'

const router = useRouter()
const auth = useAuthStore()

const CATS = ['전체', '면접 후기', '질문', '스터디 모집', '자유']
const CAT_CLS = { '면접 후기': 'badge-blue', '질문': 'badge-green', '스터디 모집': 'badge-purple', '자유': '' }

const posts = ref([])
const loading = ref(false)
const activeCat = ref('전체')

async function load() {
  loading.value = true
  posts.value = await fetchPosts(activeCat.value === '전체' ? '' : activeCat.value)
  loading.value = false
}

function goWrite() {
  if (!auth.isLoggedIn) { auth.openLogin('/community/board/new'); return }
  router.push('/community/board/new')
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

watch(activeCat, load)
onMounted(load)
</script>

<template>
  <div class="comm-panel">
    <div class="board-toolbar">
      <div class="cat-tabs">
        <button
          v-for="c in CATS" :key="c"
          class="cat-tab"
          :class="{ active: activeCat === c }"
          @click="activeCat = c"
        >{{ c }}</button>
      </div>
      <button class="btn btn-primary btn-sm" @click="goWrite">글쓰기</button>
    </div>

    <div v-if="loading" class="board-empty">불러오는 중...</div>
    <div v-else-if="posts.length === 0" class="board-empty">게시글이 없습니다.</div>

    <div v-else class="board-list">
      <div
        v-for="p in posts"
        :key="p.postId"
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
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.board-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.cat-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

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
  justify-content: space-between;
  padding: 16px 4px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  cursor: pointer;
  transition: background 0.12s;
  gap: 12px;
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
</style>

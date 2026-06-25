<script setup>
import { ref, onMounted } from 'vue'

const BASE = 'http://localhost:8080/api/admin'
const OPTS = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

const CAT_CLS = {
  '면접 후기': 'tbl-badge-blue',
  '스터디 모집': 'tbl-badge-purple',
  '자유':      'tbl-badge-gray',
}

const posts = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await fetch(`${BASE}/posts`, OPTS)
    const data = await res.json()
    posts.value = data.data || []
  } finally { loading.value = false }
}

async function remove(p) {
  if (!confirm(`'${p.title}' 게시글을 삭제하시겠습니까?`)) return
  await fetch(`${BASE}/posts/${p.postId}`, { ...OPTS, method: 'DELETE' })
  load()
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
}

onMounted(load)
</script>

<template>
  <div>
    <div class="admin-page-header">
      <h2 class="admin-page-title">게시글 관리</h2>
    </div>

    <div v-if="loading" class="admin-empty">불러오는 중...</div>
    <div v-else-if="posts.length === 0" class="admin-empty">등록된 게시글이 없습니다.</div>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>분류</th>
          <th>제목</th>
          <th>작성자</th>
          <th>조회</th>
          <th>좋아요</th>
          <th>댓글</th>
          <th>등록일</th>
          <th style="width:70px;"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in posts" :key="p.postId">
          <td>
            <span class="tbl-badge" :class="CAT_CLS[p.category] || 'tbl-badge-gray'">{{ p.category }}</span>
          </td>
          <td class="post-title-cell">{{ p.title }}</td>
          <td>{{ p.authorName }}</td>
          <td>{{ p.views }}</td>
          <td>{{ p.likes }}</td>
          <td>{{ p.commentCount }}</td>
          <td>{{ formatDate(p.createdAt) }}</td>
          <td>
            <button class="btn btn-danger btn-xs" @click="remove(p)">삭제</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.admin-page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.admin-page-title  { font-size: 20px; font-weight: 700; color: var(--ink-900, #111827); }
.admin-empty       { color: var(--ink-400, #9ca3af); padding: 40px 0; text-align: center; }

.admin-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.admin-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid var(--border, #e5e7eb); font-weight: 600; color: var(--ink-600); }
.admin-table td { padding: 10px 12px; border-bottom: 1px solid var(--border, #e5e7eb); }
.admin-table tr:hover td { background: var(--ink-50, #f8f9fa); }

.post-title-cell { max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.tbl-badge { display: inline-block; padding: 2px 8px; border-radius: 99px; font-size: 12px; font-weight: 500; }
.tbl-badge-blue   { background: #eff6ff; color: #1d4ed8; }
.tbl-badge-green  { background: #f0fdf4; color: #15803d; }
.tbl-badge-purple { background: #faf5ff; color: #7c3aed; }
.tbl-badge-gray   { background: #f3f4f6; color: #374151; }

.btn-xs     { padding: 4px 10px; font-size: 12px; }
.btn-danger { background: #ef4444; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-danger:hover { background: #dc2626; }
</style>

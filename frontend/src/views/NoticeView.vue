<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchNotice } from '../api/noticesApi.js'

const route = useRoute()
const router = useRouter()

const notice = ref(null)
const loading = ref(true)

const BADGE = {
  '업데이트': 'badge-green',
  '공지':     '',
  '이벤트':   'badge-blue',
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
}

onMounted(async () => {
  notice.value = await fetchNotice(route.params.id)
  loading.value = false
})
</script>

<template>
  <main class="page active" id="page-notice">
    <div class="container container-narrow">

      <div class="page-header" style="border-bottom:none;padding-bottom:8px;margin-bottom:8px;">
        <div class="breadcrumb">
          홈 <span class="sep">›</span>
          <a @click="router.push('/notices')" style="cursor:pointer;">공지사항</a>
          <span class="sep">›</span> {{ notice?.category ?? '상세' }}
        </div>
      </div>

      <div v-if="loading" style="text-align:center;padding:80px 0;color:var(--ink-400);">불러오는 중...</div>
      <div v-else-if="!notice" style="text-align:center;padding:80px 0;color:var(--ink-400);">공지사항을 찾을 수 없습니다.</div>

      <article v-else class="notice-detail">
        <div class="nd-meta-row">
          <span class="badge" :class="BADGE[notice.category] || ''">{{ notice.category }}</span>
          <span class="nd-date">{{ formatDate(notice.createdAt) }}</span>
          <span class="nd-views">조회 {{ notice.views }}</span>
        </div>
        <h1 class="nd-title">{{ notice.title }}</h1>
        <hr class="nd-divider" />
        <div class="nd-content ql-content" v-html="notice.content"></div>
        <div class="nd-actions">
          <button class="btn btn-ghost" @click="router.push('/notices')">목록으로</button>
        </div>
      </article>

    </div>
  </main>
</template>

<style scoped>
.notice-detail { padding: 8px 0 40px; }
.nd-meta-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.nd-date, .nd-views { font-size: 13px; color: var(--ink-400, #9ca3af); }
.nd-title { font-size: 22px; font-weight: 800; color: var(--ink-900, #111827); line-height: 1.4; margin-bottom: 20px; }
.nd-divider { border: none; border-top: 1px solid var(--ink-150, #e5e7eb); margin: 0 0 24px; }
.nd-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-800, #1f2937);
  min-height: 200px;
}

.nd-content :deep(h1), .nd-content :deep(h2), .nd-content :deep(h3) {
  font-weight: 700; margin: 20px 0 8px; color: var(--ink-900, #111827);
}
.nd-content :deep(h1) { font-size: 22px; }
.nd-content :deep(h2) { font-size: 19px; }
.nd-content :deep(h3) { font-size: 16px; }
.nd-content :deep(p) { margin: 0 0 12px; }
.nd-content :deep(ul), .nd-content :deep(ol) { padding-left: 24px; margin-bottom: 12px; }
.nd-content :deep(blockquote) {
  border-left: 4px solid var(--green-400, #4ade80);
  padding-left: 16px; margin: 16px 0;
  color: var(--ink-600, #4b5563); font-style: italic;
}
.nd-content :deep(pre) {
  background: var(--ink-50, #f8f9fa); border-radius: 6px;
  padding: 14px 16px; overflow-x: auto; margin-bottom: 16px;
  font-size: 13px;
}
.nd-content :deep(a) { color: var(--green-600, #16a34a); text-decoration: underline; }
.nd-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 8px 0; }
.nd-actions { margin-top: 32px; }
</style>

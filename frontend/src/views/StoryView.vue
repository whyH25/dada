<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchStory, toggleLike } from '../api/storiesApi.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const story = ref(null)
const loading = ref(true)

async function load() {
  loading.value = true
  story.value = await fetchStory(route.params.id)
  loading.value = false
}

async function handleLike() {
  if (!auth.isLoggedIn) { auth.openLogin(); return }
  try {
    const res = await toggleLike(story.value.storyId)
    story.value.liked = res.liked
    story.value.likes += res.liked ? 1 : -1
  } catch { /* 무시 */ }
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
}

onMounted(load)
</script>

<template>
  <main class="page active" id="page-story">
    <div class="container container-narrow">

      <div class="page-header" style="border-bottom:none;padding-bottom:8px;margin-bottom:8px;">
        <div class="breadcrumb">
          홈 <span class="sep">›</span>
          <a @click="router.push('/stories')" style="cursor:pointer;">합격 스토리</a>
          <span class="sep">›</span> 자세히 보기
        </div>
      </div>

      <div v-if="loading" style="text-align:center;padding:80px 0;color:var(--ink-400);">불러오는 중...</div>
      <div v-else-if="!story" style="text-align:center;padding:80px 0;color:var(--ink-400);">게시물을 찾을 수 없습니다.</div>

      <article v-else class="story-detail">

        <div class="sd-kicker">
          <span class="sd-kicker-tag">합격 스토리</span>
        </div>

        <h1 class="sd-title">{{ story.title }}</h1>

        <div class="sd-meta">
          <span>{{ formatDate(story.createdAt) }}</span>
          <span class="dot-sep"></span>
          <span>조회 {{ story.views.toLocaleString() }}</span>
          <span class="dot-sep"></span>
          <span>좋아요 {{ story.likes }}</span>
        </div>

        <!-- 본문 (Quill HTML) -->
        <div class="sd-body sd-content ql-content" v-html="story.content"></div>

        <div class="sd-actions">
          <button
            class="btn"
            :class="story.liked ? 'btn-primary' : 'btn-secondary'"
            @click="handleLike"
          >
            <svg width="14" height="14" viewBox="0 0 24 24"
              :fill="story.liked ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            좋아요 {{ story.likes }}
          </button>
          <button class="btn btn-ghost" @click="router.push('/stories')">목록으로</button>
        </div>
      </article>

    </div>
  </main>
</template>

<style scoped>
.sd-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-800, #1f2937);
}

/* Quill HTML 렌더링 기본 스타일 */
.sd-content :deep(h1), .sd-content :deep(h2), .sd-content :deep(h3) {
  font-weight: 700; margin: 20px 0 8px; color: var(--ink-900, #111827);
}
.sd-content :deep(h1) { font-size: 22px; }
.sd-content :deep(h2) { font-size: 19px; }
.sd-content :deep(h3) { font-size: 16px; }
.sd-content :deep(p) { margin: 0 0 12px; }
.sd-content :deep(ul), .sd-content :deep(ol) { padding-left: 24px; margin-bottom: 12px; }
.sd-content :deep(blockquote) {
  border-left: 4px solid var(--green-400, #4ade80);
  padding-left: 16px; margin: 16px 0;
  color: var(--ink-600, #4b5563); font-style: italic;
}
.sd-content :deep(pre) {
  background: var(--ink-50, #f8f9fa); border-radius: 6px;
  padding: 14px 16px; overflow-x: auto; margin-bottom: 16px;
  font-size: 13px;
}
.sd-content :deep(a) { color: var(--green-600, #16a34a); text-decoration: underline; }
.sd-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 8px 0; }
</style>

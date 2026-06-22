<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchStory, fetchStories, toggleLike } from '../api/storiesApi.js'

const ACCENTS = ['#308860', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444', '#06b6d4']
const accent = (id) => ACCENTS[id % ACCENTS.length]

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const story = ref(null)
const moreStories = ref([])
const loading = ref(true)
const notFound = ref(false)

async function load() {
  loading.value = true
  try {
    const [s, all] = await Promise.all([fetchStory(route.params.id), fetchStories()])
    if (!s) { notFound.value = true; loading.value = false; return }
    story.value = s
    moreStories.value = all.filter(x => x.storyId !== s.storyId).slice(0, 4)
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
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
watch(() => route.params.id, load)
</script>

<template>
  <main class="page active" id="page-story">
    <div class="container container-narrow">

      <div class="page-header sv-page-header">
        <div class="breadcrumb">
          홈 <span class="sep">›</span>
          <a @click="router.push('/stories')" style="cursor:pointer;">합격 스토리</a>
        </div>
      </div>

      <div v-if="loading" class="sv-loading">불러오는 중...</div>

      <!-- 404 -->
      <div v-else-if="notFound" class="sv-not-found">
        <div class="sv-not-found-icon">🔍</div>
        <h2>스토리를 찾을 수 없습니다</h2>
        <p>삭제되었거나 존재하지 않는 스토리입니다.</p>
        <button class="btn btn-ghost" @click="router.push('/stories')">목록으로</button>
      </div>

      <template v-else>
        <article class="story-detail">
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
          </div>
        </article>

        <!-- 더보기 갤러리 -->
        <section v-if="moreStories.length > 0" class="sv-more">
          <div class="sv-more-header">
            <span class="sv-more-title">더보기</span>
            <a class="sv-more-all" @click="router.push('/stories')">전체 보기 →</a>
          </div>
          <div class="sv-more-grid">
            <div
              v-for="s in moreStories" :key="s.storyId"
              class="sv-more-card"
              @click="router.push('/story/' + s.storyId)"
            >
              <div class="sv-card-thumb">
                <img :src="s.thumbnail || '/thumbnail.png'" :alt="s.title" class="sv-card-thumb-img" />
              </div>
              <div class="sv-card-body">
                <div class="sv-card-cat">합격 스토리</div>
                <div class="sv-card-title">{{ s.title }}</div>
                <div class="sv-card-meta">{{ formatDate(s.createdAt) }} · 조회 {{ s.views }}</div>
              </div>
            </div>
          </div>
        </section>
      </template>
    </div>
  </main>
</template>

<style scoped>
.sv-page-header { border-bottom: none !important; padding-bottom: 8px; margin-bottom: 8px; }
.sv-loading { text-align: center; padding: 80px 0; color: var(--ink-400); }

.sv-not-found {
  text-align: center;
  padding: 80px 0;
  color: var(--ink-500);
}
.sv-not-found-icon { font-size: 40px; margin-bottom: 16px; }
.sv-not-found h2 { font-size: 20px; font-weight: 700; margin-bottom: 8px; color: var(--ink-800); }
.sv-not-found p { font-size: 14px; margin-bottom: 24px; }

.sd-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-800, #1f2937);
}
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
  padding: 14px 16px; overflow-x: auto; margin-bottom: 16px; font-size: 13px;
}
.sd-content :deep(a) { color: var(--green-600, #16a34a); text-decoration: underline; }
.sd-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 8px 0; }

.sd-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 32px;
}

/* 더보기 */
.sv-more {
  margin-top: 12px;
  padding-top: 48px;
  border-top: 1px solid var(--border, #e5e7eb);
}
.sv-more-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.sv-more-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900);
}
.sv-more-all {
  font-size: 13px;
  color: var(--ink-400);
  cursor: pointer;
  transition: color 0.15s;
}
.sv-more-all:hover { color: var(--green-600, #16a34a); }

.sv-more-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.sv-more-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border, #e5e7eb);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  background: #fff;
}
.sv-more-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.sv-card-thumb {
  width: 100%;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: flex-end;
  padding: 10px 12px;
  position: relative;
  overflow: hidden;
}
.sv-card-thumb-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.sv-more-card:hover .sv-card-thumb-img { transform: scale(1.04); }
.sv-card-thumb-label {
  font-size: 11px;
  font-weight: 700;
  color: rgba(255,255,255,0.75);
  letter-spacing: 0.04em;
}

.sv-card-body {
  padding: 12px;
}
.sv-card-cat {
  font-size: 11px;
  font-weight: 700;
  color: var(--green-600, #16a34a);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.sv-card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-900);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}
.sv-card-meta {
  font-size: 11px;
  color: var(--ink-400);
}

@media (max-width: 640px) {
  .sv-more-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

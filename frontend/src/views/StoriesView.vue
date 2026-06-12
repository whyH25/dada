<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchStories, toggleLike } from '../api/storiesApi.js'
import { fetchEventBanner } from '../api/noticesApi.js'

const router = useRouter()
const auth = useAuthStore()

const ACCENTS = ['#308860', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444', '#06b6d4']

const stories = ref([])
const banner = ref(null)
const sort = ref('latest')

async function load() {
  const [s, b] = await Promise.all([fetchStories(), fetchEventBanner()])
  stories.value = s
  banner.value = b
}

const sorted = computed(() => {
  return [...stories.value].sort((a, b) => {
    if (sort.value === 'views') return b.views - a.views
    if (sort.value === 'likes') return b.likes - a.likes
    return new Date(b.createdAt) - new Date(a.createdAt)
  })
})

async function handleLike(e, story) {
  e.stopPropagation()
  if (!auth.isLoggedIn) { auth.openLogin(); return }
  try {
    const res = await toggleLike(story.storyId)
    story.liked = res.liked
    story.likes += res.liked ? 1 : -1
  } catch { /* 무시 */ }
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
}

onMounted(load)
</script>

<template>
  <main class="page active" id="page-stories">
    <div class="container">

      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 합격 스토리</div>
        <h1 class="page-title">합격 스토리</h1>
        <p class="page-subtitle">
          다대다 편집팀이 직접 만난 합격자 인터뷰. 합격까지의 진짜 이야기를 들어보세요.
          총 <strong>{{ stories.length.toLocaleString() }}</strong>건
        </p>
      </div>

      <!-- 이벤트 배너 -->
      <div v-if="banner" class="story-banner" @click="router.push('/notices/' + banner.noticeId)">
        <div class="story-banner-inner">
          <span class="story-banner-tag">이벤트</span>
          <span class="story-banner-title">합격 후기 공유이벤트!</span>
          <span class="story-banner-sub">{{ banner.title }}</span>
        </div>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
      </div>

      <div class="seg-tabs">
        <div class="seg-tab" :class="{ active: sort === 'latest' }" @click="sort = 'latest'">최신순</div>
        <div class="seg-tab" :class="{ active: sort === 'views' }" @click="sort = 'views'">조회순</div>
        <div class="seg-tab" :class="{ active: sort === 'likes' }" @click="sort = 'likes'">인기순</div>
      </div>

      <div v-if="stories.length === 0" class="stories-empty">등록된 합격 스토리가 없습니다.</div>

      <div class="story-grid" v-else>
        <article
          v-for="(s, i) in sorted"
          :key="s.storyId"
          class="story-card"
          :style="{ '--accent': ACCENTS[i % ACCENTS.length] }"
          @click="router.push('/story/' + s.storyId)"
        >
          <div class="sc-accent-bar" />
          <div class="sc-body">
            <div class="sc-top">
              <span class="sc-tag">합격 스토리</span>
              <span class="sc-date">{{ formatDate(s.createdAt) }}</span>
            </div>
            <h3 class="sc-title">{{ s.title }}</h3>
          </div>
          <div class="sc-footer">
            <span class="sc-views">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              {{ s.views.toLocaleString() }}
            </span>
            <button class="sc-like" :class="{ on: s.liked }" @click.stop="handleLike($event, s)">
              <svg width="13" height="13" viewBox="0 0 24 24"
                :fill="s.liked ? 'currentColor' : 'none'"
                stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              {{ s.likes }}
            </button>
          </div>
        </article>
      </div>

    </div>
  </main>
</template>

<style scoped>
/* 이벤트 배너 */
.story-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(120deg, #d1fae5 0%, #a7f3d0 50%, #6ee7b7 100%);
  color: #065f46;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: filter 0.15s;
}
.story-banner:hover { filter: brightness(0.96); }
.story-banner-inner { display: flex; align-items: center; gap: 12px; }
.story-banner-tag {
  background: #059669;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 99px;
  letter-spacing: 0.05em;
}
.story-banner-title { font-size: 15px; font-weight: 700; color: #064e3b; }
.story-banner-sub   { font-size: 13px; color: #047857; }

/* 갤러리 그리드 */
.story-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1100px) {
  .story-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 740px) {
  .story-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .story-grid { grid-template-columns: 1fr; }
}

.story-card {
  background: #fff;
  border: 1px solid var(--ink-150, #e5e7eb);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.18s, transform 0.18s;
}
.story-card:hover {
  box-shadow: 0 6px 24px rgba(0,0,0,0.09);
  transform: translateY(-3px);
}

/* 상단 컬러 바 */
.sc-accent-bar {
  height: 5px;
  background: var(--accent);
  flex-shrink: 0;
}

.sc-body {
  padding: 20px 20px 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sc-tag {
  font-size: 11px;
  font-weight: 700;
  color: var(--accent);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.sc-date {
  font-size: 12px;
  color: var(--ink-400, #9ca3af);
}

.sc-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900, #111827);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: keep-all;
  min-height: 74px;
}

/* 하단 메타 */
.sc-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--ink-100, #f3f4f6);
}

.sc-views {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--ink-400, #9ca3af);
}

.sc-like {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--ink-400, #9ca3af);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s;
}
.sc-like.on  { color: #ef4444; }
.sc-like:hover { color: #ef4444; }

.stories-empty {
  color: var(--ink-400, #9ca3af);
  text-align: center;
  padding: 60px 0;
}
</style>

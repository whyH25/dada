<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'
import { fetchStories, toggleLike } from '../../api/storiesApi.js'
import { fetchEventBanner } from '../../api/noticesApi.js'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const ACCENTS = ['#308860', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444', '#06b6d4']

const stories = ref([])
const banner = ref(null)
const sort = ref('latest')
const page = ref(Number(route.query.page) || 1)
const PAGE_SIZE = 6

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

const totalPages = computed(() => Math.ceil(sorted.value.length / PAGE_SIZE))

const paged = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return sorted.value.slice(start, start + PAGE_SIZE)
})

function setSort(val) {
  sort.value = val
  page.value = 1
}

watch(page, (p) => {
  router.replace({ query: { ...route.query, page: p === 1 ? undefined : p } })
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

function parseMeta(content) {
  try {
    const p = JSON.parse(content)
    if (p.__template__) return { company: p.company || '', jobRole: p.jobRole || '' }
  } catch {}
  return { company: '', jobRole: '' }
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
        <div class="story-banner-blob b1"></div>
        <div class="story-banner-blob b2"></div>
        <div class="story-banner-blob b3"></div>
        <div class="story-banner-inner">
          <span class="story-banner-tag">이벤트</span>
          <div class="story-banner-texts">
            <span class="story-banner-title">합격 후기 공유이벤트!</span>
            <span class="story-banner-sub">{{ banner.title }}</span>
          </div>
        </div>
        <svg class="story-banner-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg>
      </div>

      <div class="sort-row">
        <button class="sort-btn" :class="{ active: sort === 'latest' }" @click="setSort('latest')">최신순</button>
        <button class="sort-btn" :class="{ active: sort === 'views' }" @click="setSort('views')">조회순</button>
        <button class="sort-btn" :class="{ active: sort === 'likes' }" @click="setSort('likes')">인기순</button>
      </div>

      <div v-if="stories.length === 0" class="stories-empty">등록된 합격 스토리가 없습니다.</div>

      <div class="story-grid" v-else>
        <article
          v-for="(s, i) in paged"
          :key="s.storyId"
          class="story-card"
          :style="{ '--accent': ACCENTS[i % ACCENTS.length] }"
          @click="router.push('/stories/' + s.storyId)"
        >
          <div class="sc-thumb">
            <img :src="s.thumbnail || '/thumbnail.png'" :alt="s.title" />
          </div>
          <div class="sc-body">
            <div class="sc-top">
              <span class="sc-tag">합격 스토리</span>
              <span class="sc-date">{{ formatDate(s.createdAt) }}</span>
            </div>
            <h3 class="sc-title">{{ s.title }}</h3>
            <div v-if="parseMeta(s.content).company || parseMeta(s.content).jobRole" class="sc-meta-tags">
              <span v-if="parseMeta(s.content).company" class="sc-meta-tag">{{ parseMeta(s.content).company }}</span>
              <span v-if="parseMeta(s.content).jobRole" class="sc-meta-tag sc-meta-tag-role">{{ parseMeta(s.content).jobRole }}</span>
            </div>
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

      <!-- 페이지네이션 -->
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
/* 이벤트 배너 */
.story-banner {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(160deg, #e8f5ed 0%, #d0edda 60%, #c0e6cc 100%);
  border-radius: 16px;
  padding: 18px 24px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: filter 0.15s;
}
.story-banner:hover { filter: brightness(0.97); }

/* 배경 원형 블롭 */
.story-banner-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.story-banner-blob.b1 {
  width: 200px; height: 200px;
  background: radial-gradient(circle, #a8dfc0 0%, transparent 70%);
  bottom: -80px; left: -40px;
  opacity: 0.65;
}
.story-banner-blob.b2 {
  width: 180px; height: 180px;
  background: radial-gradient(circle, #9dd8b8 0%, transparent 70%);
  top: -60px; left: 35%;
  opacity: 0.5;
}
.story-banner-blob.b3 {
  width: 220px; height: 220px;
  background: radial-gradient(circle, #b2e5c8 0%, transparent 70%);
  top: -80px; right: -30px;
  opacity: 0.6;
}

.story-banner-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}
.story-banner-texts { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.story-banner-tag {
  flex-shrink: 0;
  background: #286c4a;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 99px;
  letter-spacing: 0.05em;
}
.story-banner-title { font-size: 15px; font-weight: 800; color: #1a3d28; }
.story-banner-sub   { font-size: 13px; color: #2d6a47; }
.story-banner-arrow { position: relative; z-index: 1; color: #2d6a47; flex-shrink: 0; }

/* 정렬 탭 — 오른쪽 끝 정렬 */
.sort-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  margin-bottom: 16px;
}
.sort-btn {
  background: none;
  border: none;
  padding: 4px 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-400, #9ca3af);
  cursor: pointer;
  border-radius: 6px;
  transition: color 0.15s;
}
.sort-btn:hover { color: var(--ink-700); }
.sort-btn.active {
  color: var(--ink-900, #111827);
  font-weight: 700;
}

/* 갤러리 그리드 */
.story-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
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

/* 썸네일 이미지 */
.sc-thumb {
  height: 180px;
  overflow: hidden;
  border-radius: 10px;
  flex-shrink: 0;
  margin: 10px 10px 0;
}
.sc-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}
.story-card:hover .sc-thumb img { transform: scale(1.04); }

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
  color: var(--green-500, #308860);
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

.sc-meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 8px;
}
.sc-meta-tag {
  font-size: 11.5px;
  font-weight: 600;
  padding: 2px 9px;
  border-radius: 20px;
  background: var(--ink-100, #f3f4f6);
  color: var(--ink-600, #4b5563);
}
.sc-meta-tag-role {
  background: var(--green-50, #f0fdf4);
  color: var(--green-700, #15803d);
}

/* 페이지네이션 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 40px;
  padding-bottom: 48px;
}
.pg-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 8px;
  background: #fff;
  color: var(--ink-500);
  cursor: pointer;
  transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) { border-color: var(--green-500); color: var(--green-500); }
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-num {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  font-weight: 500;
  color: var(--ink-600);
  cursor: pointer;
  transition: all 0.15s;
}
.pg-num:hover { border-color: var(--green-500); color: var(--green-500); }
.pg-num.active {
  background: var(--green-500, #308860);
  border-color: var(--green-500, #308860);
  color: #fff;
  font-weight: 700;
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

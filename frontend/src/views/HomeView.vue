<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchStories } from '../api/storiesApi.js'
import { fetchPosts } from '../api/postsApi.js'
import { fetchJobSchedules } from '../api/scheduleApi.js'

const router = useRouter()
const auth = useAuthStore()


const stories = ref([])
const posts = ref([])
const schedules = ref([])

function go(p) { router.push(p) }
function requireAuth(action) {
  if (auth.isLoggedIn) { typeof action === 'function' ? action() : router.push('/' + action) }
  else auth.openLogin(action)
}

function formatDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  const diff = Date.now() - dt.getTime()
  if (diff < 60000) return '방금 전'
  if (diff < 3600000) return Math.floor(diff / 60000) + '분 전'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '시간 전'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '일 전'
  return String(d).slice(0, 10).replace(/-/g, '.')
}

function excerpt(html) {
  if (!html) return ''
  return html.replace(/<[^>]*>/g, '').replace(/&[a-z#\d]+;/gi, ' ').trim().slice(0, 90)
}

function dday(endDate) {
  if (!endDate) return ''
  const end = new Date(endDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  const diff = Math.ceil((end - today) / 86400000)
  if (diff < 0) return '마감'
  if (diff === 0) return 'D-Day'
  return `D-${diff}`
}

function ddayClass(endDate) {
  if (!endDate) return ''
  const end = new Date(endDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  const diff = Math.ceil((end - today) / 86400000)
  if (diff < 0) return 'dday-closed'
  if (diff <= 3) return 'dday-urgent'
  if (diff <= 7) return 'dday-soon'
  return 'dday-ok'
}

function openSchedule(s) {
  if (s.jobUrl) window.open(s.jobUrl, '_blank', 'noopener')
  else go('/schedule')
}

onMounted(async () => {
  const [s, p, sc] = await Promise.all([
    fetchStories().catch(() => []),
    fetchPosts('', '', 'latest', 1).catch(() => ({ posts: [] })),
    fetchJobSchedules().catch(() => [])
  ])
  stories.value = s.slice(0, 3)
  posts.value = p.posts.slice(0, 4)

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  schedules.value = sc
    .filter(s => !s.endDate || new Date(s.endDate) >= today)
    .sort((a, b) => new Date(a.endDate) - new Date(b.endDate))
    .slice(0, 4)
})
</script>

<template>
  <main class="page active" id="page-home">
    <div class="container">
      <!-- Hero -->
      <section class="hero">
        <div class="hero-inner">
          <div class="hero-eyebrow"><span class="pulse"></span>NEW | 실전형 다대다 면접</div>
          <h1 class="hero-title">AI 기반 <span>다대다</span><br />면접 시뮬레이션</h1>
          <p class="hero-desc">
            혼자 준비하기 어려운 면접, 이제 AI와 함께하세요. AI 면접관과 AI 지원자가 함께하는 실전형 시뮬레이션으로 실제 면접 분위기와 맞춤형 피드백을 경험해보세요.
          </p>
          <div class="hero-actions">
            <button class="hero-btn-primary" @click="go('/interview-intro')">
              빠른 시작
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
            </button>
          </div>
        </div>
      </section>
      <!-- Feature cards -->
      <section class="feature-cards">
        <div class="feature-card fc-a" @click="go('/schedule')">
          <h3 class="feature-card-title">채용 일정을<br />한눈에 확인하고<br />즐겨찾기로<br />관리하세요.</h3>
          <button class="feature-pill">채용 일정 보기
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
          <div class="feature-art art-a" aria-hidden="true"><span class="c1"></span><span class="c2"></span><span class="c3"></span></div>
        </div>
        <div class="feature-card fc-b" @click="go('/interview-intro')">
          <h3 class="feature-card-title">직무도 난이도도<br />내 마음대로!<br />나만의 다대다<br />면접방을 만들어요.</h3>
          <button class="feature-pill">면접방 생성
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
          <div class="feature-art art-b" aria-hidden="true"><span class="c1"></span><span class="c2"></span><span class="c3"></span></div>
        </div>
        <div class="feature-card fc-c" @click="requireAuth(() => go('/mypage?section=resume'))">
          <h3 class="feature-card-title">이력서 등록만으로<br />모든 면접방에<br />나에게 맞는<br />질문이 도착해요.</h3>
          <button class="feature-pill">이력서 등록
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
          <div class="feature-art art-c" aria-hidden="true"><span class="c1"></span><span class="c2"></span><span class="c3"></span></div>
        </div>
      </section>

      <!-- 합격 스토리 -->
      <section class="section">
        <div class="section-head">
          <div>
            <h2 class="section-title">합격 스토리</h2>
            <p class="section-desc">다대다 편집팀이 만난 합격자 인터뷰. 합격까지의 진짜 이야기를 들어보세요.</p>
          </div>
          <a class="section-link" @click="go('/stories')">전체 보기
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg>
          </a>
        </div>
        <div class="story-grid">
          <div
            v-for="s in stories"
            :key="s.storyId"
            class="story-card"
            @click="go('/story/' + s.storyId)"
          >
            <div class="story-thumb">
              <img :src="s.thumbnail || '/thumbnail.png'" :alt="s.title" />
            </div>
            <div class="story-kicker">
              <span class="story-kicker-tag">합격자 인터뷰</span>
              <span class="story-kicker-result">최종 합격</span>
            </div>
            <h3 class="story-headline">{{ s.title }}</h3>
            <p class="story-quote">{{ excerpt(s.content) }}</p>
            <div class="story-footer">
              <span class="story-read">인터뷰 읽기 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg></span>
              <span class="story-date">{{ formatDate(s.createdAt) }}</span>
            </div>
          </div>
          <div v-if="stories.length === 0" class="story-empty">등록된 합격 스토리가 없습니다.</div>
        </div>
      </section>

      <!-- 채용일정 + 자유게시판 -->
      <section class="split-2">
        <div class="card list-card">
          <div class="card-header"><h3 class="card-title">채용일정</h3><a class="card-link" @click="go('/schedule')">더보기 ›</a></div>
          <template v-if="schedules.length > 0">
            <div
              v-for="s in schedules"
              :key="s.scheduleId"
              class="list-item"
              @click="openSchedule(s)"
            >
              <div class="list-item-text">
                <div class="list-item-title">{{ s.companyName }} · {{ s.jobTitle }}</div>
                <div class="list-item-sub">{{ [s.employmentType, s.companyType].filter(Boolean).join(' · ') }}</div>
              </div>
              <span class="sched-dday" :class="ddayClass(s.endDate)">{{ dday(s.endDate) }}</span>
            </div>
          </template>
          <div v-else class="list-empty">등록된 채용일정이 없습니다.</div>
        </div>

        <div class="card list-card">
          <div class="card-header"><h3 class="card-title">커뮤니티</h3><a class="card-link" @click="go('/community/board')">더보기 ›</a></div>
          <template v-if="posts.length > 0">
            <div
              v-for="p in posts"
              :key="p.postId"
              class="list-item"
              @click="go('/community/board/' + p.postId)"
            >
              <div class="list-item-text">
                <div class="list-item-title">{{ p.title }}</div>
                <div class="list-item-sub">{{ p.category || '기타' }} · 댓글 {{ p.commentCount ?? 0 }}</div>
              </div>
              <span class="list-item-meta">{{ formatDate(p.createdAt) }}</span>
            </div>
          </template>
          <div v-else class="list-empty">등록된 게시글이 없습니다.</div>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.story-empty, .list-empty {
  color: var(--ink-400, #9ca3af);
  padding: 24px 0;
  text-align: center;
  font-size: 14px;
}

.story-thumb {
  height: 180px;
  overflow: hidden;
  border-radius: var(--radius);
  flex-shrink: 0;
  margin: 8px 8px 0;
}
.story-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}
.story-card:hover .story-thumb img { transform: scale(1.04); }

/* D-day 뱃지 */
.sched-dday {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
  font-family: var(--font-mono, monospace);
}
.dday-ok     { background: #d1fae5; color: #065f46; }
.dday-soon   { background: #fef3c7; color: #92400e; }
.dday-urgent { background: #fee2e2; color: #991b1b; }
.dday-closed { background: #f3f4f6; color: #9ca3af; }
</style>

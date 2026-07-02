<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchStories } from '../api/storiesApi.js'
import { fetchPosts } from '../api/postsApi.js'
import { fetchNotices } from '../api/noticesApi.js'
import c1Img from '../assets/images/c1.png'
import c2Img from '../assets/images/c2.png'
import c3Img from '../assets/images/c3.png'

const router = useRouter()
const auth = useAuthStore()


const stories = ref([])
const posts = ref([])
const notices = ref([])

function go(p) { router.push(p) }
function requireAuth(action) {
  if (auth.isLoggedIn) { typeof action === 'function' ? action() : router.push('/' + action) }
  else auth.openLogin(action)
}


function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
}


const CAT_CLASS = {
  '면접 후기': 'cat-review',
  '스터디 모집': 'cat-study',
  '기타':      'cat-etc',
}

function catClass(cat) { return CAT_CLASS[cat] || 'cat-etc' }

function parseMeta(content) {
  try {
    const p = JSON.parse(content)
    if (p.__template__) return { company: p.company || '', jobRole: p.jobRole || '' }
  } catch {}
  return { company: '', jobRole: '' }
}


onMounted(async () => {
  const [s, p, n] = await Promise.all([
    fetchStories().catch(() => []),
    fetchPosts('', '', 'latest', 1).catch(() => ({ posts: [] })),
    fetchNotices().catch(() => [])
  ])
  stories.value = s.slice(0, 3)
  posts.value = p.posts.slice(0, 3)
  notices.value = n.slice(0, 3)
})
</script>

<template>
  <main class="page active" id="page-home">
    <!-- Hero - full width -->
    <section class="hero">
      <div class="container">
        <div class="hero-inner">
          <div class="hero-eyebrow"><span class="pulse"></span>NEW | 실전형 다대다 면접</div>
          <h1 class="hero-title">AI 기반 <span>다대다</span> 면접 시뮬레이션</h1>
          <p class="hero-desc">
            혼자 준비하기 어려운 면접, 이제 AI와 함께하세요. AI 면접관과 AI 지원자가 함께하는 실전형 시뮬레이션으로 실제 면접 분위기와 맞춤형 피드백을 경험해보세요.
          </p>
          <div class="hero-actions">
            <button class="hero-btn-primary" @click="go('/interview/intro')">
              빠른 시작
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
            </button>
          </div>
        </div>
      </div>
    </section>
    <!-- Feature cards -->
    <div class="container">
      <section class="feature-cards">
        <div class="feature-card fc-a" @click="go('/schedule')">
          <img :src="c1Img" class="feature-icon" alt="" />
          <h3 class="feature-card-title">채용 일정 관리</h3>
          <p class="feature-card-desc">채용 일정을 한눈에 확인하고<br />즐겨찾기로 관리하세요.</p>
          <button class="feature-pill">채용 일정 보기
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
        </div>
        <div class="feature-card fc-b" @click="go('/interview/intro')">
          <img :src="c2Img" class="feature-icon" alt="" />
          <h3 class="feature-card-title">나만의 면접방</h3>
          <p class="feature-card-desc">직무와 난이도를 선택하고<br />나만의 면접방을 만들어요.</p>
          <button class="feature-pill">면접방 생성
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
        </div>
        <div class="feature-card fc-c" @click="requireAuth(() => go('/mypage?section=resume'))">
          <img :src="c3Img" class="feature-icon" alt="" />
          <h3 class="feature-card-title">이력서 등록</h3>
          <p class="feature-card-desc">이력서를 등록하면<br />나에게 딱 맞는 질문이 도착해요.</p>
          <button class="feature-pill">이력서 등록
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
        </div>
      </section>
    </div>

    <!-- 합격 스토리 -->
    <div class="stories-bg">
      <div class="container">
      <section class="section section-stories">
        <div class="section-head">
          <div>
            <h2 class="section-title">합격 스토리</h2>
            <p class="section-desc">다대다 편집팀이 직접 만난 합격자 인터뷰. 합격까지의 진짜 이야기를 들어보세요.</p>
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
            @click="go('/stories/' + s.storyId)"
          >
            <div class="story-thumb">
              <img :src="s.thumbnail || '/thumbnail.png'" :alt="s.title" />
            </div>
            <h3 class="story-headline">{{ s.title }}</h3>
            <div class="story-meta-tags">
              <span v-if="parseMeta(s.content).company" class="story-meta-tag">{{ parseMeta(s.content).company }}</span>
              <span v-if="parseMeta(s.content).jobRole" class="story-meta-tag story-meta-tag-role">{{ parseMeta(s.content).jobRole }}</span>
            </div>
            <div class="story-footer">
              <span class="story-read">인터뷰 읽기 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg></span>
            </div>
          </div>
          <div v-if="stories.length === 0" class="story-empty">등록된 합격 스토리가 없습니다.</div>
        </div>
      </section>
      </div>
    </div>

    <!-- 커뮤니티 + 공지사항 -->
    <div class="container">
      <section class="board-section">
        <!-- 커뮤니티 -->
        <div class="board-card">
          <div class="board-card-header">
            <h3 class="card-title">커뮤니티</h3>
            <a class="card-link" @click="go('/community/posts')">더보기 ›</a>
          </div>
          <div class="board-list">
            <div
              v-for="p in posts"
              :key="p.postId"
              class="board-item"
              @click="go('/community/posts/' + p.postId)"
            >
              <span class="board-cat-tag" :class="catClass(p.category)">{{ p.category || '기타' }}</span>
              <span class="board-item-title">{{ p.title }}</span>
              <span class="board-side">💬 {{ p.commentCount ?? 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 공지사항 -->
        <div class="board-card">
          <div class="board-card-header">
            <h3 class="card-title">공지사항</h3>
            <a class="card-link" @click="go('/notices')">더보기 ›</a>
          </div>
          <div class="board-list">
            <div
              v-for="n in notices"
              :key="n.noticeId"
              class="board-item"
              @click="go('/notices/' + n.noticeId)"
            >
              <span class="board-cat-tag" :class="n.category === '이벤트' ? 'ncat-event' : 'ncat-notice'">{{ n.category }}</span>
              <span class="board-item-title">{{ n.title }}</span>
              <span class="board-side">{{ formatDate(n.createdAt) }}</span>
            </div>
          </div>
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
  height: 190px;
  overflow: hidden;
  flex-shrink: 0;
}
.story-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}
.story-card:hover .story-thumb img { transform: scale(1.04); }

.story-meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 28px 16px;
}
.story-meta-tag {
  background: #edf7f0;
  color: #286c4a;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
}
.story-meta-tag-role {
  background: #e7f4df;
  color: #4f8f33;
}

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

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchStory, fetchStories, toggleLike } from '../api/storiesApi.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const story = ref(null)
const moreStories = ref([])
const loading = ref(true)
const notFound = ref(false)

const storyTemplate = computed(() => {
  if (!story.value?.content) return null
  try {
    const p = JSON.parse(story.value.content)
    if (p.__template__) return p
  } catch {}
  return null
})

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

const thumbRef = ref(null)
const thumbStyle = ref({})

function onScroll() {
  if (!thumbRef.value) return
  const rect = thumbRef.value.getBoundingClientRect()
  const progress = Math.max(0, Math.min(1, -rect.top / rect.height))
  thumbStyle.value = {
    opacity: 1 - progress * 0.75,
    transform: `scale(${1 - progress * 0.05}) translateY(${-progress * 20}px)`,
  }
}

onMounted(() => {
  load()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))
watch(() => route.params.id, () => { thumbStyle.value = {}; load() })
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
          <!-- 썸네일 -->
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

          <!-- 썸네일 -->
          <div v-if="story.thumbnail" ref="thumbRef" class="sd-thumbnail" :style="thumbStyle">
            <img :src="story.thumbnail" :alt="story.title" class="sd-thumbnail-img" />
          </div>

          <!-- 요약 -->
          <p v-if="storyTemplate?.summary" class="sd-summary">{{ storyTemplate.summary }}</p>

          <!-- 템플릿 Q&A 렌더링 -->
          <template v-if="storyTemplate">
            <!-- 기업·직무·경력 정보 박스 -->
            <div v-if="storyTemplate.company || storyTemplate.jobRole || storyTemplate.careerType" class="sd-info-box">
              <div v-if="storyTemplate.company" class="sd-info-item">
                <div class="sd-info-icon">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                </div>
                <span class="sd-info-label">합격 기업</span>
                <span class="sd-info-value">{{ storyTemplate.company }}</span>
              </div>
              <div v-if="storyTemplate.jobRole" class="sd-info-item">
                <div class="sd-info-icon">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
                  </svg>
                </div>
                <span class="sd-info-label">합격 직무</span>
                <span class="sd-info-value">{{ storyTemplate.jobRole }}</span>
              </div>
              <div v-if="storyTemplate.careerType" class="sd-info-item">
                <div class="sd-info-icon">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/>
                  </svg>
                </div>
                <span class="sd-info-label">경력 구분</span>
                <span class="sd-info-value">{{ storyTemplate.careerType }}</span>
              </div>
            </div>

            <div class="sd-body sd-qa-body">
              <div
                v-for="(section, si) in storyTemplate.sections"
                :key="si"
                class="sd-qa-section"
              >
                <div class="sd-qa-q">
                  <span class="sd-qa-mark">Q.</span>
                  <span>{{ section.question }}</span>
                </div>
                <p class="sd-qa-a">{{ section.answer }}</p>
                <img v-if="section.image" :src="section.image" class="sd-qa-img" />

                <template v-if="section.extra?.length">
                  <div v-for="(ex, ei) in section.extra" :key="ei" class="sd-qa-extra">
                    <div class="sd-qa-q sd-qa-q-extra">
                      <span class="sd-qa-mark sd-qa-mark-extra">Q.</span>
                      <span>{{ ex.question }}</span>
                    </div>
                    <p class="sd-qa-a">{{ ex.answer }}</p>
                    <img v-if="ex.image" :src="ex.image" class="sd-qa-img" />
                  </div>
                </template>
              </div>
            </div>
          </template>

          <!-- 기존 HTML 콘텐츠 렌더링 (레거시) -->
          <div v-else class="sd-body sd-content ql-content" v-html="story.content"></div>

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

          <!-- CTA -->
          <div class="sd-cta" @click="router.push('/interview-intro')">
            <p class="sd-cta-text">나도 합격의 주인공이 될 수 있어요</p>
            <p class="sd-cta-sub">DADA AI 모의면접으로 실전처럼 준비해보세요</p>
            <span class="sd-cta-btn">
              지금 면접 체험하기
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </span>
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
#page-story .container { padding-bottom: 80px; }
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

/* CTA */
.sd-cta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: 40px;
  padding: 28px 24px;
  background: var(--green-500, #308860);
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.15s;
  text-align: center;
}
.sd-cta:hover { filter: brightness(1.12); }
.sd-cta-text {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
  margin: 0;
}
.sd-cta-sub {
  font-size: 13.5px;
  color: rgba(255,255,255,0.75);
  margin: 0;
}
.sd-cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 10px 22px;
  background: #fff;
  color: var(--green-600, #308860);
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
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

/* 썸네일 히어로 */
.sd-thumbnail {
  margin: 20px 0 0;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
  transform-origin: top center;
  will-change: transform, opacity;
}
.sd-thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 요약 */
.sd-summary {
  margin: 20px 0 0;
  padding: 18px 20px;
  background: var(--ink-50, #f8fafc);
  border-left: 3px solid var(--green-500, #308860);
  border-radius: 0 8px 8px 0;
  font-size: 15px;
  line-height: 1.75;
  color: var(--ink-700, #374151);
  white-space: pre-line;
}

/* 기업·직무·경력 정보 박스 */
.sd-info-box {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin: 40px 0 0;
  background: var(--green-50, #f0fdf4);
  border: 1px solid var(--green-100, #dcfce7);
  border-radius: 12px;
  overflow: hidden;
}
.sd-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 22px;
  border-right: 1px solid var(--green-100, #dcfce7);
}
.sd-info-item:last-child { border-right: none; }
.sd-info-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--green-100, #dcfce7);
  color: var(--green-600, #16a34a);
  margin-bottom: 8px;
  flex-shrink: 0;
}
.sd-info-icon svg { width: 20px; height: 20px; }
.sd-info-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--green-600, #16a34a);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.sd-info-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-900, #111827);
  letter-spacing: -0.01em;
}

/* Q&A 템플릿 렌더링 */
.sd-qa-body { margin-top: 52px; }
.sd-qa-section {
  margin-bottom: 52px;
  padding-bottom: 52px;
  border-bottom: 1px solid var(--ink-150, #f1f5f9);
}
.sd-qa-section:last-child { border-bottom: none; }
.sd-qa-q {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  font-size: 20px;
  font-weight: 700;
  color: var(--ink-900, #111827);
  line-height: 1.45;
  margin-bottom: 18px;
  letter-spacing: -0.025em;
}
.sd-qa-mark {
  flex-shrink: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--green-600, #308860);
  margin-top: 0;
}
.sd-qa-a {
  font-size: 15.5px;
  line-height: 1.85;
  color: var(--ink-700, #374151);
  padding-left: 28px;
  margin: 0;
  white-space: pre-line;
}
.sd-qa-img {
  display: block;
  max-width: 100%;
  border-radius: 10px;
  margin-top: 16px;
  border: 1px solid var(--ink-150, #e9ecef);
}
.sd-qa-extra {
  margin-top: 28px;
  padding-top: 28px;
  border-top: 1px solid var(--ink-150, #f1f5f9);
}
.sd-qa-q-extra { font-size: 20px; font-weight: 700; }
.sd-qa-mark-extra { font-size: 20px; color: var(--green-600, #308860); }

@media (max-width: 640px) {
  .sv-more-grid { grid-template-columns: repeat(2, 1fr); }
  .sd-thumbnail { aspect-ratio: 3 / 2; }
}
</style>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDataStore } from '../stores/data.js'
import { useAuthStore } from '../stores/auth.js'
import { getBookmarks, toggleBookmark } from '../api/scheduleApi.js'

const data = useDataStore()
const auth = useAuthStore()

// ── 캘린더 상태 ──────────────────────────────────────
const todayDate = new Date()
const currentYear = ref(todayDate.getFullYear())
const currentMonth = ref(todayDate.getMonth() + 1)
const calView = ref('all')

// ── API 데이터 ────────────────────────────────────────
const schedules = ref([])

async function loadSchedules() {
  try {
    const res = await fetch('http://localhost:8080/api/job-schedules', { credentials: 'include' })
    if (res.ok) {
      const json = await res.json()
      schedules.value = json.data || []
    }
  } catch { /* 서버 미연결 시 빈 캘린더 */ }
}

// ── 관심 일정 (서버 연동) ─────────────────────────────
const savedIds = ref(new Set())

async function loadBookmarks() {
  if (!auth.isLoggedIn) return
  try {
    const ids = await getBookmarks()
    savedIds.value = new Set(ids)
  } catch { /* 조용히 실패 */ }
}

async function toggleSave(scheduleId) {
  if (!auth.isLoggedIn) {
    auth.openLogin()
    return
  }
  try {
    const res = await toggleBookmark(scheduleId)
    const s = new Set(savedIds.value)
    res.saved ? s.add(scheduleId) : s.delete(scheduleId)
    savedIds.value = s
  } catch { /* 네트워크 오류 무시 */ }
}

const isSaved = (id) => savedIds.value.has(id)
const mineCount = computed(() => savedIds.value.size)

// ── 날짜별 이벤트 인덱싱 ─────────────────────────────
const eventsByDate = computed(() => {
  const map = {}
  schedules.value.forEach(s => {
    if (s.startDate) {
      if (!map[s.startDate]) map[s.startDate] = []
      map[s.startDate].push({ ...s, eventType: 'start' })
    }
    if (s.endDate) {
      if (!map[s.endDate]) map[s.endDate] = []
      map[s.endDate].push({ ...s, eventType: 'end' })
    }
  })
  return map
})

// ── 캘린더 셀 ────────────────────────────────────────
const DOWS = ['일', '월', '화', '수', '목', '금', '토']

const cells = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDow = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const prevDays = new Date(year, month - 1, 0).getDate()
  const out = []

  for (let i = firstDow - 1; i >= 0; i--)
    out.push({ muted: true, date: prevDays - i, events: [] })

  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const dow = (firstDow + d - 1) % 7
    const isToday =
      todayDate.getFullYear() === year &&
      todayDate.getMonth() + 1 === month &&
      todayDate.getDate() === d
    out.push({
      muted: false, date: d, dateStr,
      sun: dow === 0, sat: dow === 6, today: isToday,
      events: eventsByDate.value[dateStr] || [],
    })
  }

  const total = firstDow + daysInMonth
  const tail = (7 - (total % 7)) % 7
  for (let i = 1; i <= tail; i++)
    out.push({ muted: true, date: i, events: [] })

  return out
})

const visibleCells = computed(() => {
  if (calView.value === 'all') return cells.value
  return cells.value.map(c => ({
    ...c,
    events: c.events.filter(e => savedIds.value.has(e.scheduleId)),
  }))
})

const monthLabel = computed(() => `${currentYear.value}년 ${currentMonth.value}월`)

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else currentMonth.value--
}
function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else currentMonth.value++
}
function goToday() {
  currentYear.value = todayDate.getFullYear()
  currentMonth.value = todayDate.getMonth() + 1
}

// ── 상세 모달 ─────────────────────────────────────────
const selected = ref(null)
function openDetail(event) { selected.value = event }
function closeDetail() { selected.value = null }

function formatDate(d) {
  if (!d) return ''
  return d.replace(/-/g, '.')
}

// ── 핫잡 슬라이더 ─────────────────────────────────────
const hotTrack = ref(null)
function slideHot(dir) {
  hotTrack.value?.scrollBy({ left: dir * 320, behavior: 'smooth' })
}

onMounted(() => {
  loadSchedules()
  loadBookmarks()
})
</script>

<template>
  <main class="page active" id="page-schedule">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 채용 일정</div>
        <h1 class="page-title">채용 일정</h1>
        <p class="page-subtitle">놓치면 안되는 채용 일정을 캘린더 한 곳에서 관리하세요.</p>
      </div>

      <!-- Hot jobs slider -->
      <section class="hot-jobs">
        <div class="section-head" style="margin-bottom:14px;">
          <h2 class="section-title" style="font-size:16px;">🔥 지금 뜨는 채용공고</h2>
          <div class="hot-nav">
            <button class="hot-arrow" @click="slideHot(-1)"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M15 18l-6-6 6-6"/></svg></button>
            <button class="hot-arrow" @click="slideHot(1)"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M9 18l6-6-6-6"/></svg></button>
            <a class="section-link" @click="slideHot(1)">더보기 ›</a>
          </div>
        </div>
        <div class="hot-track" ref="hotTrack">
          <div class="hot-job" v-for="(j, i) in data.hotJobs" :key="i">
            <div class="hot-job-top">
              <div class="company-logo" :class="j.logo" style="width:32px;height:32px;font-size:13px;">{{ j.short }}</div>
              <span class="badge" :class="'badge-' + j.kind">{{ j.dday }}</span>
            </div>
            <div><div class="hot-job-name">{{ j.co }}</div><div class="hot-job-sub">{{ j.sub }}</div></div>
            <div class="hot-job-meta"><span>{{ j.label }}</span><span>{{ j.date }}</span></div>
          </div>
        </div>
      </section>

      <!-- Full calendar -->
      <div class="calendar cal-full">
        <div class="cal-head">
          <div class="cal-toggle">
            <button class="cal-toggle-btn" :class="{ active: calView === 'all' }" @click="calView = 'all'">전체 일정</button>
            <button class="cal-toggle-btn" :class="{ active: calView === 'mine' }" @click="calView = 'mine'">
              내 일정 <span class="cal-mine-count">{{ mineCount }}</span>
            </button>
          </div>
          <div class="flex gap-12">
            <div class="cal-legend">
              <div class="cal-legend-item"><span class="swatch" style="background:var(--green-500)"></span>서류 시작</div>
              <div class="cal-legend-item"><span class="swatch" style="background:#ef4444"></span>서류 마감</div>
            </div>
            <button class="cal-today-btn" @click="goToday">오늘</button>
            <div class="cal-nav">
              <button class="cal-nav-btn" @click="prevMonth"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg></button>
              <button class="cal-nav-btn" @click="nextMonth"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg></button>
            </div>
          </div>
        </div>

        <div class="cal-month-row"><div class="cal-month">{{ monthLabel }}</div></div>

        <div class="cal-grid cal-grid-lg">
          <div class="cal-dow" v-for="d in DOWS" :key="d">{{ d }}</div>
          <div
            v-for="(c, i) in visibleCells"
            :key="i"
            class="cal-cell"
            :class="{ muted: c.muted, sun: c.sun, sat: c.sat, today: c.today }"
          >
            <div class="cal-date">{{ c.date }}</div>
            <!-- 이벤트 리스트 아이템 -->
            <span
              v-for="(e, ei) in c.events.slice(0, 3)"
              :key="ei"
              class="cal-event"
              :class="e.eventType === 'start' ? 'green' : 'red'"
              @click.stop="openDetail(e)"
            >
              <span class="cal-event-dot"></span>
              <span class="cal-event-txt">{{ e.companyName }} 서류 {{ e.eventType === 'start' ? '시작' : '마감' }}</span>
            </span>
            <span v-if="c.events.length > 3" class="cal-event-more">+{{ c.events.length - 3 }}건</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 상세 모달 -->
    <div v-if="selected" class="auth-overlay open" @click.self="closeDetail">
      <div class="auth-modal jd-modal" role="dialog" aria-modal="true">
        <button class="auth-close" @click="closeDetail">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
        </button>

        <!-- 기업명 -->
        <div class="jd-head">
          <div class="jd-co-circle">{{ selected.companyName.charAt(0) }}</div>
          <div class="jd-head-text">
            <div class="jd-co">{{ selected.companyName }}</div>
            <div class="jd-role">{{ selected.jobTitle }}</div>
          </div>
        </div>

        <hr class="jd-divider" />

        <!-- 상세 소개 -->
        <p class="jd-intro" v-if="selected.description">{{ selected.description }}</p>
        <p class="jd-intro jd-empty" v-else>등록된 소개가 없습니다.</p>

        <hr class="jd-divider" />

        <!-- 기업형태 · 채용직무 · 채용형태 -->
        <div class="jd-meta-row">
          <div class="jd-meta-item" v-if="selected.companyType">
            <span class="jd-meta-label">기업 형태</span>
            <span class="jd-meta-val">{{ selected.companyType }}</span>
          </div>
          <div class="jd-meta-item">
            <span class="jd-meta-label">채용 직무</span>
            <span class="jd-meta-val">{{ selected.jobTitle }}</span>
          </div>
          <div class="jd-meta-item" v-if="selected.employmentType">
            <span class="jd-meta-label">채용 형태</span>
            <span class="jd-meta-val">{{ selected.employmentType }}</span>
          </div>
        </div>

        <!-- 서류 시작 / 서류 마감 -->
        <div class="jd-date-strip">
          <div class="jd-date-item">
            <span class="jd-date-dot dot-start"></span>
            <div>
              <div class="jd-date-label">서류 시작</div>
              <div class="jd-date-val">{{ formatDate(selected.startDate) }}</div>
            </div>
          </div>
          <div class="jd-date-sep"></div>
          <div class="jd-date-item">
            <span class="jd-date-dot dot-end"></span>
            <div>
              <div class="jd-date-label">서류 마감</div>
              <div class="jd-date-val">{{ formatDate(selected.endDate) }}</div>
            </div>
          </div>
        </div>

        <!-- 버튼 -->
        <div class="jd-actions">
          <button class="jd-save" :class="{ on: isSaved(selected.scheduleId) }" style="margin-right:auto;"
            @click="toggleSave(selected.scheduleId)">
            <svg width="14" height="14" viewBox="0 0 24 24"
              :fill="isSaved(selected.scheduleId) ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
            {{ isSaved(selected.scheduleId) ? '내 일정' : '관심 일정' }}
          </button>
          <a v-if="selected.jobUrl" :href="selected.jobUrl" target="_blank" class="btn btn-primary btn-sm">공고 원문 보기</a>
          <button v-else class="btn btn-primary btn-sm" disabled style="opacity:.5;cursor:default;">공고 원문 없음</button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* 캘린더 이벤트 행 */
.cal-event {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
  margin-top: 2px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  border: 1px solid transparent;
}
.cal-event.green {
  background: var(--green-50, #f0fdf4);
  color: var(--green-650, #166534);
  border-color: var(--green-200, #bbf7d0);
}
.cal-event.red {
  background: #fef2f2;
  color: #991b1b;
  border-color: #fecaca;
}
.cal-event:hover { filter: brightness(0.95); }
.cal-event-dot {
  width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0;
}
.cal-event.green .cal-event-dot { background: var(--green-500, #308860); }
.cal-event.red   .cal-event-dot { background: #ef4444; }
.cal-event-txt { overflow: hidden; text-overflow: ellipsis; }
.cal-event-more { font-size: 10px; color: var(--ink-400, #9ca3af); padding: 1px 4px; }

/* 모달 내부 */
.jd-co-circle {
  width: 44px; height: 44px; border-radius: 50%;
  background: var(--green-500, #308860); color: #fff;
  font-size: 18px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.jd-divider {
  border: none; border-top: 1px solid var(--ink-150, #e5e7eb);
  margin: 14px 0;
}
.jd-empty { color: var(--ink-400, #9ca3af); font-style: italic; }

/* 기업형태 · 채용직무 · 채용형태 행 */
.jd-meta-row {
  display: flex; flex-wrap: wrap; gap: 16px;
  margin-bottom: 14px;
}
.jd-meta-item { display: flex; flex-direction: column; gap: 2px; }
.jd-meta-label { font-size: 11px; color: var(--ink-400, #9ca3af); }
.jd-meta-val   { font-size: 13.5px; font-weight: 600; color: var(--ink-800, #1f2937); }

/* 서류시작·마감 strip */
.jd-date-strip {
  display: flex; align-items: center; gap: 0;
  background: var(--ink-50, #f8f9fa);
  border-radius: 10px; padding: 12px 16px;
  margin-bottom: 16px;
}
.jd-date-item { display: flex; align-items: center; gap: 10px; flex: 1; }
.jd-date-dot  { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; }
.dot-start { background: var(--green-500, #308860); }
.dot-end   { background: #ef4444; }
.jd-date-label { font-size: 11px; color: var(--ink-400, #9ca3af); margin-bottom: 2px; }
.jd-date-val   { font-size: 14px; font-weight: 700; color: var(--ink-900, #111827); }
.jd-date-sep {
  width: 1px; height: 36px;
  background: var(--ink-200, #e5e7eb);
  margin: 0 16px; flex-shrink: 0;
}
</style>

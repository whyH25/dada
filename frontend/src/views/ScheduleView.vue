<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { toast } from '../utils/toast.js'

const router = useRouter()
const data = useDataStore()

const calView = ref('all')   // 'all' | 'mine'
const hotTrack = ref(null)
const selected = ref(null)   // [day, idx] for job detail modal

// 원본 buildCalendar(): 2026년 5월
const DOWS = ['일', '월', '화', '수', '목', '금', '토']
const FIRST_DOW = 5  // 5월 1일은 금요일
const DAYS = 31
const TODAY = 22

const cells = computed(() => {
  const out = []
  for (let i = FIRST_DOW - 1; i >= 0; i--) out.push({ muted: true, date: 30 - i, events: [] })
  for (let d = 1; d <= DAYS; d++) {
    const dow = (FIRST_DOW + d - 1) % 7
    let list = (data.deadlines[d] || []).map((e, i) => ({ e, i }))
    if (calView.value === 'mine') list = list.filter((x) => x.e.mine)
    out.push({ muted: false, date: d, sun: dow === 0, sat: dow === 6, today: d === TODAY, day: d, events: list })
  }
  const total = FIRST_DOW + DAYS
  const tail = (7 - (total % 7)) % 7
  for (let i = 1; i <= tail; i++) out.push({ muted: true, date: i, events: [] })
  return out
})

function slideHot(dir) {
  if (hotTrack.value) hotTrack.value.scrollBy({ left: dir * 320, behavior: 'smooth' })
}
function toggleSave(day, idx) {
  const e = data.toggleSaveDeadline(day, idx)
  toast(e.mine ? `'${e.co}' 일정을 내 일정에 담았어요.` : `'${e.co}' 일정을 내 일정에서 뺐어요.`)
}
function openDetail(day, idx) { selected.value = [day, idx] }
function closeDetail() { selected.value = null }
const sel = computed(() => selected.value ? data.deadlines[selected.value[0]][selected.value[1]] : null)
function pad(n) { return String(n).padStart(2, '0') }
</script>

<template>
  <main class="page active" id="page-schedule">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 채용 일정</div>
        <h1 class="page-title">채용 일정</h1>
        <p class="page-subtitle">놓치면 안되는 100대기업 채용 일정을 캘린더 한 곳에서 관리하세요.</p>
      </div>

      <!-- Hot jobs slider -->
      <section class="hot-jobs">
        <div class="section-head" style="margin-bottom:14px;">
          <h2 class="section-title" style="font-size:16px;">🔥 지금 뜨는 채용공고</h2>
          <div class="hot-nav">
            <button class="hot-arrow" @click="slideHot(-1)" aria-label="이전"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M15 18l-6-6 6-6" /></svg></button>
            <button class="hot-arrow" @click="slideHot(1)" aria-label="다음"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M9 18l6-6-6-6" /></svg></button>
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
            <button class="cal-toggle-btn" :class="{ active: calView === 'mine' }" @click="calView = 'mine'">내 일정 <span class="cal-mine-count">{{ data.mineCount }}</span></button>
          </div>
          <div class="flex gap-12">
            <div class="cal-legend"><div class="cal-legend-item"><span class="swatch" style="background:var(--green-500)"></span>서류 마감일</div></div>
            <button class="cal-today-btn">오늘</button>
            <div class="cal-nav">
              <button class="cal-nav-btn"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6" /></svg></button>
              <button class="cal-nav-btn"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6" /></svg></button>
            </div>
          </div>
        </div>
        <div class="cal-month-row"><div class="cal-month">2026년 5월</div></div>
        <div class="cal-grid cal-grid-lg">
          <div class="cal-dow" v-for="d in DOWS" :key="d">{{ d }}</div>
          <div
            v-for="(c, i) in cells"
            :key="i"
            class="cal-cell"
            :class="{ muted: c.muted, sun: c.sun, sat: c.sat, today: c.today }"
          >
            <div class="cal-date">{{ c.date }}</div>
            <span
              v-for="{ e, i: ei } in c.events"
              :key="ei"
              class="cal-event green"
              :class="{ mine: e.mine }"
              @click.stop="openDetail(c.day, ei)"
            >
              <button class="cal-star" :class="{ on: e.mine }" @click.stop="toggleSave(c.day, ei)" title="내 일정에 담기">
                <svg width="11" height="11" viewBox="0 0 24 24" :fill="e.mine ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
              </button>
              <span class="cal-event-txt">{{ e.co }} 서류 마감</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Job detail modal -->
    <div v-if="sel" class="auth-overlay open" @click.self="closeDetail">
      <div class="auth-modal jd-modal" role="dialog" aria-modal="true">
        <button class="auth-close" @click="closeDetail" aria-label="닫기"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12" /></svg></button>
        <div class="jd-head">
          <div class="company-logo" :class="sel.logo" style="width:48px;height:48px;font-size:18px;border-radius:10px;">{{ sel.short }}</div>
          <div class="jd-head-text"><div class="jd-co">{{ sel.co }}</div><div class="jd-role">{{ sel.role }}</div></div>
        </div>
        <p class="jd-intro">{{ sel.intro }}</p>
        <div class="jd-grid">
          <div class="jd-info"><div class="jd-info-label">서류 마감</div><div class="jd-info-val">2026.05.{{ pad(selected[0]) }} (마감)</div></div>
          <div class="jd-info"><div class="jd-info-label">모집 직무</div><div class="jd-info-val">{{ sel.role }}</div></div>
          <div class="jd-info"><div class="jd-info-label">고용 형태</div><div class="jd-info-val">{{ sel.emp }}</div></div>
          <div class="jd-info"><div class="jd-info-label">전형 절차</div><div class="jd-info-val">{{ sel.steps }}</div></div>
          <div class="jd-info" style="grid-column:1/-1;"><div class="jd-info-label">주요 자격</div><div class="jd-info-val">{{ sel.qual }}</div></div>
        </div>
        <div class="jd-actions">
          <button class="jd-save" :class="{ on: sel.mine }" style="margin-right:auto;" @click="toggleSave(selected[0], selected[1])">
            <svg width="14" height="14" viewBox="0 0 24 24" :fill="sel.mine ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
            {{ sel.mine ? '내 일정' : '관심 일정' }}
          </button>
          <button class="btn btn-secondary btn-sm" @click="alert('채용 공고 페이지로 이동합니다. (데모)')">공고 원문 보기</button>
          <button class="btn btn-primary btn-sm" @click="closeDetail(); router.push('/rooms')">이 기업 면접 연습하기 →</button>
        </div>
      </div>
    </div>
  </main>
</template>

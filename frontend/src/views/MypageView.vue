<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { reports } from '../stores/seed.js'
import { scoreColor, reportPanel } from '../utils/mypageReport.js'
import { toast } from '../utils/toast.js'
import { useAuthStore } from '../stores/auth.js'
import { updateUserApi, deleteUserApi } from '../api/authApi.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

// section: reports | report-detail | favorites | resume | cover | portfolio | account | billing
const section = ref('reports')
const currentReport = ref(0)
const reportTab = ref('overview')

const reportTabs = [
  ['overview', '종합 평가'], ['competency', '역량 분석'], ['speech', '발화 분석'],
  ['applicants', '지원자 분석'], ['questions', '질문별 상세'],
]

function gotoMy(s) { section.value = s; window.scrollTo(0, 0) }
function openReport(i) { currentReport.value = i; reportTab.value = 'overview'; section.value = 'report-detail'; window.scrollTo(0, 0) }
function setTab(k) { reportTab.value = k; window.scrollTo(0, 0) }

const r = computed(() => reports[currentReport.value])
const panelHtml = computed(() => reportPanel(r.value, reportTab.value))
const best = computed(() => Math.max(...reports.map((x) => x.score)))
const avg = computed(() => Math.round(reports.reduce((a, x) => a + x.score, 0) / reports.length))

// ---- 서류 관리 ----
const docMeta = {
  resume: { title: '이력서/자기소개서', accept: '.pdf,.doc,.docx', hint: 'PDF | DOC | 최대 10MB' },
  portfolio: { title: '포트폴리오', accept: '.pdf,.zip', hint: 'PDF | ZIP | URL | 최대 50MB' },
}
const docs = reactive({
  resume: [
    { name: '이력서_김지원_v4.pdf', size: '324 KB', date: '26.05.20', main: true },
    { name: '자기소개서_카카오_백엔드.pdf', size: '210 KB', date: '26.05.19', main: false },
    { name: '자기소개서_공통_v2.docx', size: '188 KB', date: '26.05.10', main: false },
  ],
  portfolio: [{ name: '포트폴리오_2026.pdf', size: '6.2 MB', date: '26.05.12', main: false }],
})
function uploadDoc(type, e) {
  const f = e.target.files && e.target.files[0]
  if (!f) return
  const kb = f.size / 1024
  const size = kb > 1024 ? (kb / 1024).toFixed(1) + ' MB' : Math.round(kb) + ' KB'
  docs[type].push({ name: f.name, size, date: '26.06.01', main: docs[type].length === 0 })
  e.target.value = ''
  toast(`${docMeta[type].title}가 업로드되었어요.`)
}
function deleteDoc(type, i) {
  const wasMain = docs[type][i].main
  docs[type].splice(i, 1)
  if (wasMain && docs[type].length) docs[type][0].main = true
}
function setMainDoc(type, i) { docs[type].forEach((d, j) => (d.main = j === i)) }

// ---- 회원정보 수정 ----
const isEditing = ref(false)
const editName = ref('')
const editPwd = ref('')
const editPwdConfirm = ref('')
const editError = ref('')
const editLoading = ref(false)

function startEdit() {
  editName.value = user.value?.userName || ''
  editPwd.value = ''
  editPwdConfirm.value = ''
  editError.value = ''
  isEditing.value = true
}

async function submitEdit() {
  editError.value = ''
  if (!editName.value.trim()) {
    editError.value = '이름을 입력해주세요.'
    return
  }
  if (editPwd.value && editPwd.value.length < 8) {
    editError.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  if (editPwd.value && editPwd.value !== editPwdConfirm.value) {
    editError.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  editLoading.value = true
  try {
    const payload = { userName: editName.value.trim() }
    if (editPwd.value) payload.userPwd = editPwd.value
    const res = await updateUserApi(payload)
    authStore.user = res.data
    isEditing.value = false
    toast('회원정보가 수정되었습니다.')
  } catch (e) {
    editError.value = e.message
  } finally {
    editLoading.value = false
  }
}

// ---- 회원탈퇴 ----
const showWithdrawConfirm = ref(false)
const withdrawLoading = ref(false)

async function withdrawUser() {
  withdrawLoading.value = true
  try {
    await deleteUserApi()
    authStore.user = null
    showWithdrawConfirm.value = false
    router.push('/')
  } catch (e) {
    toast(e.message)
  } finally {
    withdrawLoading.value = false
  }
}

// ---- 계정 ----
const notifPrefs = reactive({ schedule: true, report: true, community: false, marketing: false })
const notifMeta = [
  ['schedule', '면접 일정 알림', 'D-7 | D-3 | D-1 시점에 마감 임박 공고를 알려드려요'],
  ['report', '리포트 생성 알림', '면접 종료 후 분석 리포트가 발행되면 알려드려요'],
  ['community', '커뮤니티 활동 알림', '내 글의 댓글|오픈톡 새 메시지를 알려드려요'],
  ['marketing', '마케팅 | 혜택 알림', '이벤트, 프로모션, 할인 소식을 받아볼게요'],
]
function onNotifChange() { toast('알림 설정이 저장되었어요.') }

// ---- 즐겨찾기 ----
const favs = [
  { co: '삼성전자', logo: 'samsung', short: 'S', title: 'DS부문 반도체 설계 다대다 면접', diff: '중' },
  { co: '카카오', logo: 'kakao', short: 'K', title: '카카오 백엔드 신입 공채 면접', diff: '상' },
  { co: '토스', logo: 'toss', short: 'T', title: '토스 서버 경력 다대다 면접', diff: '상' },
]

// ---- 결제 ----
const plans = [
  { name: 'Free', price: '0원', per: '월', feats: ['100대기업 면접방 둘러보기', '월 2회 모의 면접', '기본 리포트'], current: false, cta: '무료로 시작' },
  { name: 'PRO', price: '9,900원', per: '월', feats: ['무제한 모의 면접', '역량|발화 상세 리포트', '맞춤형 면접방 생성', '관심 기업 알림'], current: true, cta: '현재 이용 중' },
  { name: 'Team', price: '79,000원', per: '월', feats: ['PRO 기능 전체', '멤버 10명까지', '팀 리포트 대시보드', '전담 매니저'], current: false, cta: 'Team 시작' },
]
const sideDocCount = (t) => docs[t].length

onMounted(() => {
  if (route.query.section) section.value = route.query.section
})
</script>

<template>
  <main class="page active" id="page-mypage">
    <div class="container">
      <div class="mypage">
        <!-- Side menu -->
        <aside class="side-menu">
          <div class="side-profile">
            <div class="side-profile-avatar">{{ user?.userName?.charAt(0) }}</div>
            <div class="side-profile-name">{{ user?.userName }}</div>
            <div class="side-profile-email">{{ user?.userEmail }}</div>
            <div class="side-profile-meta">PRO 멤버 | 26.12.31 만료</div>
          </div>
          <div class="side-section">
            <div class="side-section-title">면접 관리</div>
            <div class="side-item" :class="{ active: section === 'favorites' }" @click="gotoMy('favorites')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
              즐겨찾기 <span class="count">14</span>
            </div>
            <div class="side-item" :class="{ active: section === 'reports' || section === 'report-detail' }" @click="gotoMy('reports')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6M9 13h6M9 17h4" /></svg>
              리포트 <span class="count">23</span>
            </div>
          </div>
          <div class="side-section">
            <div class="side-section-title">서류 관리</div>
            <div class="side-item" :class="{ active: section === 'resume' }" @click="gotoMy('resume')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg>
              이력서/자기소개서 <span class="count">{{ sideDocCount('resume') }}</span>
            </div>
            <div class="side-item" :class="{ active: section === 'portfolio' }" @click="gotoMy('portfolio')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" /><circle cx="8.5" cy="8.5" r="1.5" /><path d="m21 15-5-5L5 21" /></svg>
              포트폴리오 <span class="count">{{ sideDocCount('portfolio') }}</span>
            </div>
          </div>
          <div class="side-section" style="border-bottom: none;">
            <div class="side-section-title">계정</div>
            <div class="side-item" :class="{ active: section === 'account' }" @click="gotoMy('account')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
              회원정보
            </div>
            <div class="side-item" :class="{ active: section === 'billing' }" @click="gotoMy('billing')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2" /><path d="M2 10h20" /></svg>
              구독 및 결제
            </div>
          </div>
        </aside>

        <!-- Dynamic content -->
        <div id="mypage-content">
          <!-- 리포트 목록 -->
          <template v-if="section === 'reports'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 리포트</div>
            <div class="flex-between" style="margin:4px 0 20px;">
              <div><h2 class="mp-h1">리포트</h2><p class="mp-sub">완료한 모의 면접의 분석 리포트를 모아봤어요. 총 {{ reports.length }}건</p></div>
            </div>
            <div class="mp-stat-row">
              <div class="mp-stat"><div class="mp-stat-label">총 면접</div><div class="mp-stat-val">{{ reports.length }}<span>회</span></div></div>
              <div class="mp-stat"><div class="mp-stat-label">평균 점수</div><div class="mp-stat-val" :style="{ color: scoreColor(avg) }">{{ avg }}<span>점</span></div></div>
              <div class="mp-stat"><div class="mp-stat-label">최고 점수</div><div class="mp-stat-val" style="color:var(--green-500);">{{ best }}<span>점</span></div></div>
              <div class="mp-stat"><div class="mp-stat-label">동일 직무 평균</div><div class="mp-stat-val" style="color:var(--ink-500);">74<span>점</span></div></div>
            </div>
            <div class="card" style="padding:0; overflow:hidden;">
              <div class="card-header" style="padding:18px 20px 14px; border-bottom:1px solid var(--ink-150); margin-bottom:0;"><h3 class="card-title">전체 리포트</h3><span class="text-sm text-muted">최신순</span></div>
              <table class="history-table report-table">
                <thead><tr><th>기업 / 직무</th><th>일시</th><th>난이도</th><th>점수</th><th>상태</th><th></th></tr></thead>
                <tbody>
                  <tr v-for="(rp, i) in reports" :key="i" @click="openReport(i)">
                    <td><div style="display:flex; align-items:center; gap:10px;"><div class="company-logo" :class="rp.logo" style="width:30px;height:30px;font-size:12px;">{{ rp.short }}</div><div><strong>{{ rp.co }}</strong> | {{ rp.role }}</div></div></td>
                    <td><span class="text-sm text-muted">{{ rp.date }}</span></td>
                    <td><span class="badge">{{ rp.diff }}</span></td>
                    <td><strong :style="{ color: scoreColor(rp.score) }">{{ rp.score }}</strong></td>
                    <td><span class="badge badge-green">분석 완료</span></td>
                    <td><span class="report-go">리포트 보기 ›</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- 리포트 상세 -->
          <template v-else-if="section === 'report-detail'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> <a @click="gotoMy('reports')" style="cursor:pointer;">리포트</a> <span class="sep">›</span> {{ r.co }}</div>
            <div class="feedback-head" style="margin-top:8px;">
              <div class="feedback-co">
                <div class="company-logo" :class="r.logo" style="width:48px;height:48px;font-size:18px;border-radius:8px;">{{ r.short }}</div>
                <div><h2 class="feedback-title">{{ r.co }} | {{ r.role }} 다대다 면접</h2><div class="feedback-sub">{{ r.date }} ({{ r.day }}) {{ r.time }} | {{ r.dur }} | 난이도 {{ r.diff }}</div></div>
              </div>
              <div class="feedback-meta">
                <div><div class="feedback-meta-label">총평 점수</div><div class="feedback-meta-val" :style="{ color: scoreColor(r.score), fontSize: '24px' }">{{ r.score }} <span style="font-size:13px; color:var(--ink-500); font-weight:500;">/ 100</span></div></div>
                <div><div class="feedback-meta-label">동일 직무 평균</div><div class="feedback-meta-val" style="color:var(--ink-500);">{{ r.avg }}</div></div>
                <button class="btn btn-secondary" style="align-self:center;" @click="toast('PDF 리포트를 내보냅니다. (데모)')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" /></svg>PDF 리포트</button>
              </div>
            </div>
            <div class="tabs" style="margin-bottom:22px;">
              <div v-for="[k, label] in reportTabs" :key="k" class="tab" :class="{ active: reportTab === k }" @click="setTab(k)">{{ label }}</div>
            </div>
            <div v-html="panelHtml"></div>
          </template>

          <!-- 즐겨찾기 -->
          <template v-else-if="section === 'favorites'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 즐겨찾기</div>
            <h2 class="mp-h1" style="margin:4px 0 4px;">즐겨찾기</h2>
            <p class="mp-sub" style="margin-bottom:20px;">관심 있는 면접방을 모아뒀어요. 총 14개 중 3개 표시</p>
            <div class="rooms-grid">
              <div v-for="(f, i) in favs" :key="i" class="room-card">
                <div class="room-top"><div class="room-co"><div class="company-logo" :class="f.logo">{{ f.short }}</div><div><div class="room-co-name">{{ f.co }}</div><div class="room-co-role">모의 면접방</div></div></div><span class="badge badge-green">즐겨찾기 ★</span></div>
                <h3 class="room-title">{{ f.title }}</h3>
                <div class="room-tags"><span class="badge">난이도 {{ f.diff }}</span><span class="badge badge-blue">기출 기반</span></div>
                <div class="room-foot"><span class="text-sm text-muted">예상 소요 30~40분</span><button class="btn btn-sm btn-primary" @click.stop="router.push('/rooms')">입장하기 →</button></div>
              </div>
            </div>
          </template>

          <!-- 서류 관리 -->
          <template v-else-if="['resume', 'portfolio'].includes(section)">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 서류 관리 <span class="sep">›</span> {{ docMeta[section].title }}</div>
            <div class="flex-between" style="margin:4px 0 20px;">
              <div><h2 class="mp-h1">{{ docMeta[section].title }}</h2><p class="mp-sub">면접관이 {{ docMeta[section].title }} 기반으로 맞춤형 질문을 생성합니다. 총 {{ docs[section].length }}건</p></div>
            </div>
            <label class="doc-drop">
              <input type="file" :accept="docMeta[section].accept" style="display:none;" @change="uploadDoc(section, $event)" />
              <div class="doc-drop-icon"><svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg></div>
              <div class="doc-drop-title">{{ docMeta[section].title }} 업로드</div>
              <div class="doc-drop-hint">파일을 선택하거나 드래그하세요 | {{ docMeta[section].hint }}</div>
            </label>
            <div class="doc-list">
              <div v-if="!docs[section].length" class="doc-empty">아직 업로드한 {{ docMeta[section].title }}가 없어요.</div>
              <div v-for="(d, i) in docs[section]" :key="i" class="doc-item">
                <div class="doc-ico"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg></div>
                <div class="doc-info"><div class="doc-name">{{ d.name }} <span v-if="d.main" class="badge badge-green">대표</span></div><div class="doc-meta">{{ d.size }} | 업로드 {{ d.date }}</div></div>
                <div class="doc-actions">
                  <button v-if="!d.main" class="btn btn-sm btn-ghost" @click="setMainDoc(section, i)">대표 지정</button>
                  <button class="btn btn-sm btn-ghost doc-del" @click="deleteDoc(section, i)">삭제</button>
                </div>
              </div>
            </div>
          </template>

          <!-- 회원정보 -->
          <template v-else-if="section === 'account'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 회원정보</div>
            <h2 class="mp-h1" style="margin:4px 0 20px;">회원정보</h2>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">기본 정보</h3>
                <button v-if="!isEditing" class="btn btn-sm btn-secondary" @click="startEdit">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z" /></svg>
                  정보 수정
                </button>
                <button v-else class="btn btn-sm btn-primary" :disabled="editLoading" @click="submitEdit">
                  {{ editLoading ? '저장 중...' : '완료' }}
                </button>
              </div>
              <div class="info-grid">
                <div class="info-row"><div class="info-label">아이디</div><div class="info-val">{{ user?.userEmail }}</div></div>
                <div class="info-row">
                  <div class="info-label">이름</div>
                  <div class="info-val">
                    <input v-if="isEditing" class="input input-inline" v-model="editName" placeholder="이름" />
                    <span v-else>{{ user?.userName }}</span>
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">비밀번호</div>
                  <div class="info-val">
                    <template v-if="isEditing">
                      <input class="input input-inline" type="password" v-model="editPwd" placeholder="새 비밀번호 (변경 시 입력)" style="margin-bottom:6px;" />
                      <div style="display:flex; align-items:center; gap:8px;">
                        <input class="input input-inline" type="password" v-model="editPwdConfirm" placeholder="비밀번호 확인" />
                        <span v-if="editPwd && editPwdConfirm && editPwd !== editPwdConfirm" style="color:#e53e3e; font-size:12px; white-space:nowrap;">비밀번호가 일치하지 않습니다.</span>
                      </div>
                    </template>
                    <span v-else>••••••••</span>
                  </div>
                </div>
                <div class="info-row"><div class="info-label">가입일</div><div class="info-val">{{ user?.createdAt || '-' }}</div></div>
                <div class="info-row"><div class="info-label">요금제</div><div class="info-val"><span class="badge badge-green">PRO 멤버</span></div></div>
              </div>
              <p v-if="editError" class="auth-error" style="margin-top:8px;">{{ editError }}</p>
              <div style="margin-top:16px; padding-top:14px; border-top:1px solid var(--ink-150); text-align:right;">
                <button class="btn-withdraw" @click="showWithdrawConfirm = true">회원탈퇴</button>
              </div>
            </div>

            <!-- 회원탈퇴 확인 모달 -->
            <div v-if="showWithdrawConfirm" class="withdraw-overlay" @click.self="showWithdrawConfirm = false">
              <div class="withdraw-modal">
                <h3 class="withdraw-title">정말 탈퇴하시겠습니까?</h3>
                <p class="withdraw-desc">탈퇴 시 모든 면접 기록과 리포트가 삭제되며<br>복구할 수 없습니다.</p>
                <div class="withdraw-actions">
                  <button class="btn btn-secondary" @click="showWithdrawConfirm = false">취소</button>
                  <button class="btn btn-danger" :disabled="withdrawLoading" @click="withdrawUser">
                    {{ withdrawLoading ? '처리 중...' : '탈퇴하기' }}
                  </button>
                </div>
              </div>
            </div>
            <!--
            <div class="card" style="margin-top:16px;">
              <div class="card-header"><h3 class="card-title">알림 설정</h3><span class="text-sm text-muted">원하는 알림만 받아보세요</span></div>
              <div v-for="[key, label, desc] in notifMeta" :key="key" class="setting-row">
                <div><div class="setting-label">{{ label }}</div><div class="setting-desc">{{ desc }}</div></div>
                <label class="toggle"><input type="checkbox" v-model="notifPrefs[key]" @change="onNotifChange" /><span class="slider"></span></label>
              </div>
            </div>
            -->
          </template>

          <!-- 구독 및 결제 -->
          <template v-else-if="section === 'billing'">
            <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 구독 및 결제</div>
            <h2 class="mp-h1" style="margin:4px 0 20px;">구독 및 결제</h2>
            <div class="card plan-now">
              <div><div class="text-sm text-muted">현재 요금제</div><div class="plan-now-name">PRO 플랜 <span class="badge badge-green">이용 중</span></div><div class="plan-now-meta">9,900원 / 월 | 다음 결제일 2026.06.14 | 신용카드 (•••• 4921)</div></div>
              <button class="btn btn-secondary btn-sm" @click="toast('결제 수단 변경 화면을 엽니다. (데모)')">결제 수단 변경</button>
            </div>
            <h3 class="mp-section-title">요금제</h3>
            <div class="plan-grid">
              <div v-for="(p, i) in plans" :key="i" class="plan-card" :class="{ current: p.current }">
                <div v-if="p.current" class="plan-ribbon">이용 중</div>
                <div class="plan-name">{{ p.name }}</div>
                <div class="plan-price">{{ p.price }}<span>/{{ p.per }}</span></div>
                <ul class="plan-feats"><li v-for="(f, fi) in p.feats" :key="fi">{{ f }}</li></ul>
                <button class="btn btn-block" :class="p.current ? 'btn-secondary' : 'btn-primary'" :disabled="p.current" :style="p.current ? 'opacity:0.7' : ''" @click="!p.current && toast(p.name + ' 플랜으로 변경합니다. (데모)')">{{ p.cta }}</button>
              </div>
            </div>
            <h3 class="mp-section-title">결제 내역</h3>
            <div class="card" style="padding:0; overflow:hidden;">
              <table class="history-table">
                <thead><tr><th>날짜</th><th>내용</th><th>금액</th><th>상태</th></tr></thead>
                <tbody>
                  <tr><td>2026.05.14</td><td>PRO 플랜 정기결제</td><td>9,900원</td><td><span class="badge badge-green">결제 완료</span></td></tr>
                  <tr><td>2026.04.14</td><td>PRO 플랜 정기결제</td><td>9,900원</td><td><span class="badge badge-green">결제 완료</span></td></tr>
                  <tr><td>2026.03.14</td><td>PRO 플랜 정기결제</td><td>9,900원</td><td><span class="badge badge-green">결제 완료</span></td></tr>
                </tbody>
              </table>
            </div>
            <div class="danger-zone">
              <div><div class="danger-title">회원 탈퇴</div><div class="danger-desc">탈퇴 시 모든 면접 리포트와 서류가 영구 삭제되며 복구할 수 없습니다.</div></div>
              <button class="btn btn-danger" @click="toast('회원 탈퇴 절차를 안내합니다. (데모)')">회원 탈퇴</button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </main>
</template>

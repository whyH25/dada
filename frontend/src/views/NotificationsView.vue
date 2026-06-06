<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth.js'
import { useDataStore } from '../stores/data.js'

const auth = useAuthStore()
const data = useDataStore()

const interestNames = computed(() => [...data.interest])
function logoFor(name) {
  return data.allCompanies.find((x) => x.name === name) || { logo: 'lounge', short: name[0] }
}
</script>

<template>
  <main class="page active" id="page-notifications">
    <div class="container container-narrow">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 알림</div>
        <div class="flex-between">
          <div>
            <h1 class="page-title">알림</h1>
            <p class="page-subtitle">읽지 않은 알림 <strong>3</strong>건</p>
          </div>
          <div class="flex gap-8">
            <button class="btn btn-secondary btn-sm" @click="auth.openInterest()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
              관심 기업 설정
            </button>
            <button class="btn btn-secondary btn-sm">모두 읽음 표시</button>
          </div>
        </div>
      </div>

      <!-- 관심 기업 요약 (원본 renderInterestSummary) -->
      <div class="interest-summary">
        <template v-if="interestNames.length">
          <span class="interest-summary-label">관심 기업</span>
          <span v-for="n in interestNames" :key="n" class="interest-tag">
            <span class="company-logo" :class="logoFor(n).logo" style="width:20px;height:20px;font-size:9px;">{{ logoFor(n).short }}</span>{{ n }}
          </span>
          <a class="interest-edit" @click="auth.openInterest()">편집</a>
        </template>
        <div v-else class="interest-summary-empty">설정된 관심 기업이 없어요. <a @click="auth.openInterest()">지금 설정하기 ›</a></div>
      </div>

      <div class="card list-card">
        <div class="notif-item unread">
          <div class="notif-ico green"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg></div>
          <div class="notif-body"><div class="notif-title">카카오 면접 리포트가 발행되었어요</div><div class="notif-sub">총평 82점 · 발화 분석 결과를 확인해 보세요.</div></div>
          <span class="notif-time">10분 전</span>
        </div>
        <div class="notif-item unread">
          <div class="notif-ico red"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 8v4l3 3M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" /></svg></div>
          <div class="notif-body"><div class="notif-title">삼성전자 DS부문 서류 마감 D-3</div><div class="notif-sub">5월 25일(일) 23:59 까지 · 저장한 공고</div></div>
          <span class="notif-time">2시간 전</span>
        </div>
        <div class="notif-item unread">
          <div class="notif-ico blue"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg></div>
          <div class="notif-body"><div class="notif-title">관심 기업 ‘네이버’에 새 합격 스토리가 올라왔어요</div><div class="notif-sub">SQL 실무 케이스, 다대다에서 본 패턴이 그대로 나왔습니다</div></div>
          <span class="notif-time">5시간 전</span>
        </div>
        <div class="notif-item">
          <div class="notif-ico green"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5" /></svg></div>
          <div class="notif-body"><div class="notif-title">이력서_김지원_v4.pdf 업로드 완료</div><div class="notif-sub">이제 모든 면접방에서 맞춤형 질문을 받을 수 있어요.</div></div>
          <span class="notif-time">어제</span>
        </div>
        <div class="notif-item">
          <div class="notif-ico amber"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.7 21a2 2 0 0 1-3.4 0" /></svg></div>
          <div class="notif-body"><div class="notif-title">카카오 백엔드 오픈톡에 새 글 12개</div><div class="notif-sub">오늘 카카오 1차 다녀왔습니다 (질문 복원 + 분위기 공유)</div></div>
          <span class="notif-time">어제</span>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()

function go(p) { router.push(p) }
function requireAuth(action) {
  if (auth.isLoggedIn) { typeof action === 'function' ? action() : router.push('/' + action) }
  else auth.openLogin(action)
}
</script>

<template>
  <main class="page active" id="page-home">
    <div class="container">
      <!-- Hero -->
      <section class="hero">
        <div class="hero-inner">
          <div class="hero-eyebrow"><span class="pulse"></span>NEW | 실전형 다대다 면접 베타</div>
          <h1 class="hero-title">AI 기반 <span>다대다</span><br />면접 시뮬레이션</h1>
          <p class="hero-desc">
            혼자 보던 1:1 AI 면접은 잊어주세요. AI 면접관 2명 + AI 경쟁 지원자 2명과 함께,
            실제 면접장과 동일한 긴장감을 경험하고 정량 피드백을 받아보세요.
          </p>
          <div class="hero-actions">
            <button class="hero-btn-primary" @click="requireAuth('create')">
              빠른 시작
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
            </button>
            <button class="hero-btn-secondary" @click="go('/rooms')">100대기업 면접방 둘러보기</button>
          </div>
          <div class="hero-stats">
            <div><div class="hero-stat-num">128,492</div><div class="hero-stat-label">누적 모의 면접</div></div>
            <div><div class="hero-stat-num">320+</div><div class="hero-stat-label">등록 기업|직무</div></div>
            <div><div class="hero-stat-num">87%</div><div class="hero-stat-label">사용자 만족도</div></div>
          </div>
        </div>
      </section>

      <!-- Feature cards -->
      <section class="feature-cards">
        <div class="feature-card fc-a" @click="go('/rooms')">
          <h3 class="feature-card-title">100대 기업<br />기출로 채운<br />면접방에서<br />실전을 경험하세요.</h3>
          <button class="feature-pill">면접방 입장
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
          <div class="feature-art art-a" aria-hidden="true"><span class="c1"></span><span class="c2"></span><span class="c3"></span></div>
        </div>
        <div class="feature-card fc-b" @click="requireAuth('create')">
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
          <div class="story-card" @click="go('/story/0')">
            <div class="story-card-top">
              <div class="company-logo samsung">S</div>
              <div><div class="story-meta-name">삼성전자 | DS부문</div><div class="story-meta-sub">반도체 설계 | 신입 | 26기 상반기</div></div>
            </div>
            <div class="story-kicker"><span class="story-kicker-tag">합격자 인터뷰</span><span class="story-kicker-result">최종 합격</span></div>
            <h3 class="story-headline">“임원이 가장 본 건 화려함이 아니라 근거의 일관성이었어요”</h3>
            <p class="story-quote">다대다 모의 면접에서 받은 “수치적 근거가 빈약하다”는 피드백이 합격의 출발점이었습니다.</p>
            <div class="story-footer">
              <span class="story-read">인터뷰 읽기 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg></span>
              <span class="story-date">2일 전</span>
            </div>
          </div>
          <div class="story-card" @click="go('/story/1')">
            <div class="story-card-top">
              <div class="company-logo kakao">K</div>
              <div><div class="story-meta-name">카카오 | 백엔드 개발</div><div class="story-meta-sub">서버 개발 | 경력 3년 | 수시 채용</div></div>
            </div>
            <div class="story-kicker"><span class="story-kicker-tag">합격자 인터뷰</span><span class="story-kicker-result">최종 합격</span></div>
            <h3 class="story-headline">“옆자리 AI 지원자가 저보다 잘했어요. 그게 약이 됐죠”</h3>
            <p class="story-quote">더 좋은 답변을 내놓는 경쟁자를 반복해서 겪다 보니 실제 면접에선 전혀 흔들리지 않았습니다.</p>
            <div class="story-footer">
              <span class="story-read">인터뷰 읽기 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg></span>
              <span class="story-date">5일 전</span>
            </div>
          </div>
          <div class="story-card" @click="go('/story/2')">
            <div class="story-card-top">
              <div class="company-logo naver">N</div>
              <div><div class="story-meta-name">네이버 | 데이터 분석</div><div class="story-meta-sub">데이터 사이언티스트 | 신입 | 26기 상반기</div></div>
            </div>
            <div class="story-kicker"><span class="story-kicker-tag">합격자 인터뷰</span><span class="story-kicker-result">최종 합격</span></div>
            <h3 class="story-headline">“케이스 면접은 정답이 아니라 생각의 절차를 봅니다”</h3>
            <p class="story-quote">난이도 ‘상’으로 5회 반복했더니 실제 케이스 면접에서 침착하게 풀어낼 수 있었습니다.</p>
            <div class="story-footer">
              <span class="story-read">인터뷰 읽기 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg></span>
              <span class="story-date">1주 전</span>
            </div>
          </div>
        </div>
      </section>

      <!-- News + Community split -->
      <section class="split-2">
        <div class="card list-card">
          <div class="card-header"><h3 class="card-title">공지사항</h3><a class="card-link" @click="go('/notices')">더보기 ›</a></div>
          <div class="list-item">
            <span class="badge badge-green">UPDATE</span>
            <div class="list-item-text"><div class="list-item-title">v1.4 — AI 면접관 ‘압박형’ 페르소나 추가</div><div class="list-item-sub">난이도 ‘상’ 선택 시 자동 적용됩니다.</div></div>
            <span class="list-item-meta">26.05.20</span>
          </div>
          <div class="list-item">
            <span class="badge">공지</span>
            <div class="list-item-text"><div class="list-item-title">26기 상반기 채용 일정 일괄 업데이트 안내</div><div class="list-item-sub">100대기업 채용 일정을 갱신했습니다. 캘린더에서 확인해보세요.</div></div>
            <span class="list-item-meta">26.05.18</span>
          </div>
          <div class="list-item">
            <span class="badge badge-amber">점검</span>
            <div class="list-item-text"><div class="list-item-title">5월 25일(일) 새벽 02:00 ~ 04:00 정기 점검</div><div class="list-item-sub">해당 시간 동안 면접방 입장이 제한됩니다.</div></div>
            <span class="list-item-meta">26.05.15</span>
          </div>
          <div class="list-item">
            <span class="badge badge-blue">이벤트</span>
            <div class="list-item-text"><div class="list-item-title">합격 수기 작성하고 프리미엄 한 달 무료</div><div class="list-item-sub">~ 26.06.30 까지 진행됩니다.</div></div>
            <span class="list-item-meta">26.05.10</span>
          </div>
        </div>

        <div class="card list-card">
          <div class="card-header"><h3 class="card-title">커뮤니티</h3><a class="card-link" @click="go('/community')">더보기 ›</a></div>
          <div class="list-item chat-item" @click="go('/community')">
            <div class="chat-ico samsung">S</div>
            <div class="list-item-text"><div class="list-item-title">삼성전자 채용 준비방</div><div class="list-item-sub"><span class="online-dot"></span>428명 접속 중 | 반도체|DS</div></div>
            <span class="chat-enter">입장</span>
          </div>
          <div class="list-item chat-item" @click="go('/community')">
            <div class="chat-ico kakao">K</div>
            <div class="list-item-text"><div class="list-item-title">카카오 백엔드 오픈톡</div><div class="list-item-sub"><span class="online-dot"></span>312명 접속 중 | 서버|플랫폼</div></div>
            <span class="chat-enter">입장</span>
          </div>
          <div class="list-item chat-item" @click="go('/community')">
            <div class="chat-ico naver">N</div>
            <div class="list-item-text"><div class="list-item-title">네이버 데이터 직군방</div><div class="list-item-sub"><span class="online-dot"></span>198명 접속 중 | 데이터 분석</div></div>
            <span class="chat-enter">입장</span>
          </div>
          <div class="list-item chat-item" @click="go('/community')">
            <div class="chat-ico lounge">26</div>
            <div class="list-item-text"><div class="list-item-title">26기 상반기 공채 라운지</div><div class="list-item-sub"><span class="online-dot"></span>1,204명 접속 중 | 전체 직군</div></div>
            <span class="chat-enter">입장</span>
          </div>
        </div>
      </section>
    </div>

    <div class="footer">
      <div class="footer-inner">
        <div>© 2026 다대다 (InterviewAI). 모든 권리 보유.</div>
        <div class="footer-links"><a>이용약관</a><a>개인정보처리방침</a><a>고객센터</a><a>회사소개</a></div>
      </div>
    </div>
  </main>
</template>

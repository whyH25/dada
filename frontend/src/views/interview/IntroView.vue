<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()
const showTicketModal = ref(false)

function goCreate() {
  if (!auth.isLoggedIn) {
    auth.openLogin(() => goCreate())
    return
  }
  if ((auth.user?.ticketCount ?? 0) <= 0) {
    showTicketModal.value = true
    return
  }
  router.push('/interview/create')
}

const cardsRef = ref(null)
const cardsVisible = ref(false)
let cardObserver = null

onMounted(() => {
  cardObserver = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) {
      cardsVisible.value = true
      cardObserver.disconnect()
    }
  }, { threshold: 0.1 })
  if (cardsRef.value) cardObserver.observe(cardsRef.value)
})

onUnmounted(() => { cardObserver?.disconnect() })
</script>

<template>
  <main class="ii-page">

    <!-- Section 1: 걱정 말풍선 -->
    <section class="ii-worry">
      <div class="ii-blob bc1"></div>
      <div class="ii-blob bc2"></div>
      <div class="ii-blob bc3"></div>
      <div class="ii-blob bc4"></div>

      <div class="ii-worry-inner">
        <div class="ii-tag">
          <span class="ii-tag-dot"></span>
          면접을 앞둔 당신에게
        </div>
        <h1 class="ii-worry-title">면접 준비, 막막하신가요?</h1>
        <div class="ii-bubbles">
          <div class="ii-bub ii-bub-l b1">
            <span class="ii-bub-dot"></span>
            <span class="ii-bub-text">면접관 앞에서 너무 긴장돼요</span>
          </div>
          <div class="ii-bub ii-bub-r b2">
            <span class="ii-bub-dot"></span>
            <span class="ii-bub-text">내 답변이 괜찮은지 모르겠어요</span>
          </div>
          <div class="ii-bub ii-bub-l b3">
            <span class="ii-bub-dot"></span>
            <span class="ii-bub-text">다른 지원자가 있으면 위축돼요</span>
          </div>
          <div class="ii-bub ii-bub-r b4">
            <span class="ii-bub-dot"></span>
            <span class="ii-bub-text">면접에서 탈락하는 이유를 모르겠어요</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 2: 기능 카드 -->
    <section class="ii-features">
      <div class="ii-feat-inner">
        <div class="ii-tag ii-tag-dark">DADA가 도와드릴게요</div>
        <h2 class="ii-feat-title">막막한 면접 준비,<br>DADA와 함께 실전처럼</h2>
        <div ref="cardsRef" class="ii-cards" :class="{ 'cards-visible': cardsVisible }">
          <div class="ii-card">
            <div class="ii-card-badge">01</div>
            <h3 class="ii-card-title">AI 면접관이 맞춤 질문을 생성해요</h3>
            <p class="ii-card-desc">회사, 직무, 난이도를 기반으로 질문을 받을 수 있어요</p>
          </div>
          <div class="ii-card">
            <div class="ii-card-badge">02</div>
            <h3 class="ii-card-title">AI 경쟁 지원자와 함께 답변해요</h3>
            <p class="ii-card-desc">AI 지원자를 통해 실제 면접 분위기를 경험할 수 있어요</p>
          </div>
          <div class="ii-card">
            <div class="ii-card-badge">03</div>
            <h3 class="ii-card-title">실제 면접처럼 자연스럽게 연습해요</h3>
            <p class="ii-card-desc">카메라와 마이크를 켜고 실전처럼 연습할 수 있어요</p>
          </div>
          <div class="ii-card">
            <div class="ii-card-badge">04</div>
            <h3 class="ii-card-title">AI 리포트로 개선점을 확인해요</h3>
            <p class="ii-card-desc">피드백을 확인하며 실전 면접을 준비할 수 있어요</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 3: CTA -->
    <section class="ii-cta">
      <div class="ii-cta-glow"></div>
      <div class="ii-cta-ring r1"></div>
      <div class="ii-cta-ring r2"></div>
      <div class="ii-cta-inner">
        <h2 class="ii-cta-title">준비 됐나요?</h2>
        <p class="ii-cta-sub">회사·직무·난이도를 설정하면 바로 시작됩니다</p>
        <button class="ii-cta-btn" @click="goCreate">
          면접 시작하기
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M5 12h14M13 6l6 6-6 6"/>
          </svg>
        </button>
      </div>
    </section>

    <!-- 이용권 부족 모달 -->
    <div v-if="showTicketModal" class="ii-modal-bg" @click.self="showTicketModal = false">
      <div class="ii-modal">
        <svg width="46" height="46" viewBox="0 0 24 24" fill="none" stroke="#e0654a" stroke-width="1.8" stroke-linecap="round" style="margin:0 auto 16px;display:block;"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="13"/><circle cx="12" cy="16.5" r="0.8" fill="#e0654a" stroke="none"/></svg>
        <h3 class="ii-modal-title">이용권이 부족해요</h3>
        <p class="ii-modal-desc">이용권을 충전하고<br>면접을 시작해보세요.</p>
        <button class="ii-modal-btn-primary" @click="router.push('/mypage?section=billing')">이용권 충전하러 가기</button>
        <button class="ii-modal-btn-ghost" @click="showTicketModal = false">닫기</button>
      </div>
    </div>

  </main>
</template>

<style scoped>
.ii-page {
  width: 100%;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #14241b;
  -webkit-font-smoothing: antialiased;
}

/* ──────────────────────────────────
   Keyframes
────────────────────────────────── */
@keyframes ii-blob {
  0%,100% { transform: translate(0,0) scale(1); }
  50%      { transform: translate(14px,-18px) scale(1.06); }
}
@keyframes ii-fade-l {
  from { opacity: 0; transform: translateX(-22px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes ii-fade-r {
  from { opacity: 0; transform: translateX(22px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes ii-fade-up {
  from { opacity: 0; transform: translateY(26px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes ii-float {
  0%,100% { transform: translateY(0); }
  50%      { transform: translateY(-7px); }
}

/* ──────────────────────────────────
   공통 태그 칩
────────────────────────────────── */
.ii-tag {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: rgba(47, 143, 99, 0.10);
  color: #2f8f63;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.01em;
  padding: 8px 16px;
  border-radius: 999px;
  margin-bottom: 22px;
}
.ii-tag-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2f8f63;
  flex-shrink: 0;
}
.ii-tag-dark { color: #2f8f63; }

/* ──────────────────────────────────
   Section 1: 걱정 말풍선
────────────────────────────────── */
.ii-worry {
  position: relative;
  min-height: 680px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f4faf6 0%, #e7f3ec 100%);
  padding: 108px 24px 116px;
  overflow: hidden;
}

.ii-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.bc1 { width: 260px; height: 260px; top:  2%; left:  3%; background: radial-gradient(circle at 35% 35%, #ffffff, rgba(176,224,196,0.35)); filter: blur(8px); opacity: .70; animation: ii-blob 13s ease-in-out infinite; }
.bc2 { width: 190px; height: 190px; top:  9%; right: 5%; background: radial-gradient(circle at 40% 40%, #ffffff, rgba(176,224,196,0.30)); filter: blur(6px); opacity: .65; animation: ii-blob 16s ease-in-out infinite 1s; }
.bc3 { width: 150px; height: 150px; bottom: 8%; left:  8%; background: radial-gradient(circle at 40% 40%, #ffffff, rgba(176,224,196,0.28)); filter: blur(6px); opacity: .60; animation: ii-blob 15s ease-in-out infinite .6s; }
.bc4 { width: 210px; height: 210px; bottom: 4%; right: 6%; background: radial-gradient(circle at 40% 40%, #ffffff, rgba(176,224,196,0.30)); filter: blur(8px); opacity: .60; animation: ii-blob 17s ease-in-out infinite 1.4s; }

.ii-worry-inner {
  position: relative;
  width: 100%;
  max-width: 640px;
  text-align: center;
}

.ii-worry-title {
  font-size: clamp(28px, 4.4vw, 44px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #14241b;
  line-height: 1.32;
  margin: 0 0 56px;
}

.ii-bubbles {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ii-bub {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 24px 24px 24px 6px;
  padding: 18px 28px;
  box-shadow: 0 12px 34px rgba(31, 107, 72, 0.10);
  max-width: 88%;
}
.ii-bub-l {
  align-self: flex-start;
  border-radius: 24px 24px 24px 6px;
  animation: ii-fade-l 0.6s ease both;
}
.ii-bub-r {
  align-self: flex-end;
  border-radius: 24px 24px 6px 24px;
  animation: ii-fade-r 0.6s ease both;
}
.b1 { animation-delay: 0.1s; }
.b2 { animation-delay: 0.45s; }
.b3 { animation-delay: 0.8s; }
.b4 { animation-delay: 1.15s; }

.ii-bub-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #2f8f63;
}

.ii-bub-text {
  font-size: clamp(15px, 2.6vw, 17px);
  font-weight: 500;
  color: #2a3a31;
  line-height: 1.5;
  white-space: nowrap;
}

/* ──────────────────────────────────
   Section 2: 기능 카드
────────────────────────────────── */
.ii-features {
  padding: 100px 24px;
  background: #fff;
}

.ii-feat-inner {
  width: 100%;
  max-width: 940px;
  margin: 0 auto;
  text-align: center;
}

.ii-feat-title {
  font-size: clamp(24px, 3.6vw, 38px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #14241b;
  line-height: 1.4;
  margin: 0 0 56px;
}

.ii-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  text-align: left;
}

.ii-card {
  position: relative;
  background: #fff;
  border: 1px solid #e4efe8;
  border-radius: 24px;
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 2px rgba(16, 40, 28, 0.04);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  opacity: 0;
}
.ii-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(31, 107, 72, 0.14);
  border-color: #cce6d6;
}
.cards-visible .ii-card:nth-child(1) { animation: ii-fade-up 0.6s ease 0.05s both; }
.cards-visible .ii-card:nth-child(2) { animation: ii-fade-up 0.6s ease 0.18s both; }
.cards-visible .ii-card:nth-child(3) { animation: ii-fade-up 0.6s ease 0.31s both; }
.cards-visible .ii-card:nth-child(4) { animation: ii-fade-up 0.6s ease 0.44s both; }

.ii-card-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 15px;
  background: rgba(47, 143, 99, 0.10);
  color: #2f8f63;
  font-size: 19px;
  font-weight: 800;
  margin-bottom: 22px;
  flex-shrink: 0;
}

.ii-card-title {
  font-size: 19px;
  font-weight: 700;
  color: #16261d;
  line-height: 1.45;
  margin: 0 0 12px;
}

.ii-card-desc {
  font-size: 14.5px;
  color: #5d6f64;
  line-height: 1.7;
  margin: 0;
}

/* ──────────────────────────────────
   Section 3: CTA
────────────────────────────────── */
.ii-cta {
  position: relative;
  background: #2f8f63;
  padding: 110px 24px;
  text-align: center;
  overflow: hidden;
}

.ii-cta-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba(255,255,255,0.18), transparent 55%);
  pointer-events: none;
}
.ii-cta-ring {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.r1 { width: 320px; height: 320px; border: 1px solid rgba(255,255,255,0.14); top: -90px; left: -60px; }
.r2 { width: 220px; height: 220px; border: 1px solid rgba(255,255,255,0.12); bottom: -70px; right: -40px; }

.ii-cta-inner {
  position: relative;
  max-width: 620px;
  margin: 0 auto;
}

.ii-cta-title {
  font-size: clamp(30px, 4.6vw, 50px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #fff;
  line-height: 1.2;
  margin: 0 0 16px;
}

.ii-cta-sub {
  font-size: clamp(15px, 2.4vw, 17px);
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 44px;
}

.ii-cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  color: #2f8f63;
  font-family: inherit;
  font-size: 16px;
  font-weight: 700;
  padding: 17px 42px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
  animation: ii-float 2.8s ease-in-out infinite;
}
.ii-cta-btn:hover {
  animation: none;
  transform: scale(1.04);
  transition: transform 0.15s;
}

/* ──────────────────────────────────
   모달
────────────────────────────────── */
.ii-modal-bg {
  position: fixed;
  inset: 0;
  background: rgba(16, 30, 22, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}
.ii-modal {
  background: #fff;
  border-radius: 20px;
  padding: 40px 36px;
  max-width: 360px;
  width: 100%;
  text-align: center;
  box-shadow: 0 24px 60px rgba(0,0,0,0.22);
}
.ii-modal-title { font-size: 19px; font-weight: 700; color: #14241b; margin: 0 0 10px; }
.ii-modal-desc  { font-size: 14px; color: #6b7280; line-height: 1.6; margin: 0 0 24px; }
.ii-modal-btn-primary {
  width: 100%; padding: 13px 0;
  background: #2f8f63; color: #fff;
  font-family: inherit; border: none; border-radius: 12px;
  font-size: 15px; font-weight: 600; cursor: pointer; margin-bottom: 10px;
}
.ii-modal-btn-ghost {
  width: 100%; padding: 12px 0;
  background: #fff; color: #6b7280;
  border: 1px solid #e5e7eb; border-radius: 12px;
  font-size: 14px; font-family: inherit; cursor: pointer;
}

/* ──────────────────────────────────
   Responsive
────────────────────────────────── */
@media (max-width: 700px) {
  .ii-cards { grid-template-columns: 1fr; }
  .ii-bub-text { white-space: normal; }
}
</style>


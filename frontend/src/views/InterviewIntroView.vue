<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
function goCreate() { router.push('/create') }
</script>

<template>
  <main class="intro-page">
    <div class="blob blob-tl" aria-hidden="true"></div>
    <div class="blob blob-br" aria-hidden="true"></div>
    <div class="blob blob-mr" aria-hidden="true"></div>

    <div class="intro-inner">
      <div class="intro-heading">
        <p class="intro-eyebrow">AI 면접 시뮬레이션</p>
        <h1 class="intro-title">다대다 면접,이렇게 진행됩니다</h1>
      </div>

      <div class="intro-grid">
        <div class="intro-card">
          <h2 class="icard-title green">AI 면접관이<br />직접 질문합니다.</h2>
          <p class="icard-body">
            설정한 회사, 직무, 난이도를 기반으로<br />
            AI 면접관이 맞춤형 질문을 생성해요.
          </p>
          <p class="icard-note"> AI 면접관은 1명에서 3명까지 설정할 수 있어요. </p>
        </div>

        <div class="intro-card">
          <h2 class="icard-title blue">순서대로<br />답변합니다.</h2>
          <p class="icard-body">
            나와 AI 경쟁 지원자가 번갈아 가며 답변하며<br />
            실제 다대다 면접과 동일한 긴장감을 경험할 수 있어요.
          </p>
          <p class="icard-note">AI 지원자는 0명에서 2명까지 설정할 수 있어요. </p>
        </div>

        <div class="intro-card">
          <h2 class="icard-title teal">웹캠과 마이크를<br />켜주세요.</h2>
          <p class="icard-body">
            면접 중에는 카메라와 마이크가 항상 켜진 상태를 유지해주세요.
            
          </p>
          <p class="icard-note">무선이어폰 사용 시 연결이 끊기지 않도록 주의해 주세요.</p>
        </div>

        <div class="intro-card">
          <h2 class="icard-title purple">AI 맞춤 피드백을<br />제공합니다.</h2>
          <p class="icard-body">
            면접 종료 후 답변 분석과 개선 포인트를 한눈에 확인할 수 있어요.
          </p>
          <p class="icard-note">마이페이지에서 내 면접 기록을 한 눈에 볼 수 있어요.</p>
        </div>
      </div>

      <div class="intro-cta-card">
        <div class="cta-left">
          <h3 class="cta-title">준비가 됐나요?</h3>
          <p class="cta-sub">회사·직무·난이도를 설정하면 바로 시작됩니다.</p>
        </div>
        <button class="cta-btn" @click="goCreate">
          면접 시작하기
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M5 12h14M13 6l6 6-6 6" />
          </svg>
        </button>
      </div>
    </div>
  </main>
</template>

<style scoped>
.intro-page {
  /* nav(61px) + footer(76px, margin-top 제거됨)를 뺀 나머지 한 화면 안에 들어가도록 높이를 맞춰서 스크롤 없이 보이게 함 */
  min-height: calc(100vh - 61px - 76px);
  background: #f3f4f6;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 24px;
}

.blob {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
  pointer-events: none;
}
.blob-tl {
  width: 340px; height: 340px;
  background: radial-gradient(circle, #d1fae5 0%, #b6dec5 100%);
  top: -80px; left: -100px;
}
.blob-br {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #b6dec5 0%, #6cc492 100%);
  bottom: 30px; right: -90px;
}
.blob-mr {
  width: 300px; height: 300px;
  background: radial-gradient(circle, #d1fae5 0%, #b6dec5 100%);
  top: 50%; right: 150px;
  transform: translateY(-50%);
}

.intro-inner {
  position: relative;
  width: 100%;
  max-width: 1300px;
  z-index: 1;
}

.intro-heading {
  text-align: center;
  margin-bottom: 50px;
}
.intro-eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #1a6b45;
  margin-bottom: 6px;
}
.intro-title {
  font-size: clamp(20px, 2.6vw, 28px);
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
}

.intro-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

.intro-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 18px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.icard-title {
  font-size: 17px;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 10px;
}
.icard-title.green  { color: #1a6b45; }
.icard-title.blue   { color: #308860; }
.icard-title.teal   { color: #4aaf7a; }
.icard-title.purple { color: #6cc492; }

.icard-body {
  font-size: 13.5px;
  line-height: 1.6;
  color: #374151;
  margin-bottom: 10px;
}
.icard-note {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.5;
}

.intro-cta-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}
.cta-title {
  font-size: 20px;
  font-weight: 800;
  color: #1a6b45;
  margin-bottom: 6px;
}
.cta-sub {
  font-size: 13px;
  color: #6b7280;
}
.cta-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #1a6b45;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  padding: 14px 28px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s, transform 0.1s;
  flex-shrink: 0;
}
.cta-btn:hover { background: #155a38; transform: translateY(-1px); }
.cta-btn:active { transform: translateY(0); }

@media (max-width: 900px) {
  .intro-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 640px) {
  .intro-page { min-height: 0; align-items: flex-start; }
  .intro-grid { grid-template-columns: 1fr; }
  .intro-cta-card { flex-direction: column; align-items: flex-start; }
  .cta-btn { width: 100%; justify-content: center; }
}
</style>

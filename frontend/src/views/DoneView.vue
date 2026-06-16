<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
function review() { router.push('/community/board') }
</script>

<template>
  <main class="page active wait-page" id="page-done">
    <div class="wait-screen done-screen">
      <div class="done-check">
        <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg>
      </div>
      <h1 class="wait-title">면접하시느라 수고하셨어요</h1>
      <p class="wait-sub">방금 면접 기록을 안전하게 저장했어요.<br />AI가 발화를 분석해 상세 리포트를 만드는 데 보통 <strong>2~3분</strong>이 걸려요. 완료되면 알림으로 알려드릴게요.</p>
      <div class="done-actions">
        <button class="btn btn-primary btn-lg" @click="router.push('/')">메인으로 가기</button>
        <button class="btn btn-secondary btn-lg" @click="review">면접 후기 남기기</button>
      </div>
      <div class="done-note">후기를 남겨주시면 다음 모의 면접 이용권 1회를 드려요.</div>
    </div>
  </main>
</template>

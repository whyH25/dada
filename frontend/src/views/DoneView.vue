<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
function mypage() { router.push('/mypage') }
</script>

<template>
  <main class="page active wait-page" id="page-done">
    <div class="wait-screen done-screen">
      <div class="done-check">
        <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg>
      </div>
      <h1 class="wait-title">면접을 끝까지 완료하셨네요! <br />정말 수고하셨어요.</h1>
      <p class="wait-sub">소중한 면접 기록은 안전하게 저장되었으며,<br />AI 분석 리포트는 <strong>1일 후</strong> 열람할 수 있습니다. <br />리포트가 준비되면 메일로 안내해 드릴게요.</p>
      <div class="done-actions">
        <button class="btn btn-primary btn-lg" @click="router.push('/')">메인으로 이동</button>
        <button class="btn btn-secondary btn-lg" @click="mypage">마이페이지로 이동</button>
      </div>
      <div class="done-note">여러분의 합격을 진심으로 응원합니다. 🍀</div>
    </div>
  </main>
</template>






<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const params = new URLSearchParams(window.location.search)
const code    = params.get('code')    || route.query.code    || ''
const message = params.get('message') || route.query.message || '결제가 취소되었습니다.'
</script>

<template>
  <div style="min-height:100vh; display:flex; align-items:center; justify-content:center; background:var(--ink-50, #f8f9fa);">
    <div style="background:#fff; border-radius:16px; box-shadow:0 4px 24px rgba(0,0,0,.08); padding:48px 40px; max-width:400px; width:100%; text-align:center;">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#e53e3e" stroke-width="1.5" style="margin:0 auto 16px; display:block;"><circle cx="12" cy="12" r="10"/><path d="M15 9l-6 6M9 9l6 6"/></svg>
      <h2 style="font-size:20px; font-weight:700; margin-bottom:8px;">결제 실패</h2>
      <p style="color:var(--ink-500); font-size:14px; margin-bottom:6px;">{{ message }}</p>
      <p v-if="code" style="color:var(--ink-400); font-size:12px; margin-bottom:28px;">에러 코드: {{ code }}</p>
      <div v-else style="margin-bottom:28px;"></div>
      <button class="btn btn-primary" style="width:100%;" @click="router.push('/mypage?section=billing')">돌아가기</button>
    </div>
  </div>
</template>

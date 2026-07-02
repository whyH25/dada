<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { verifyPayment } from '../../api/paymentApi.js'
import { useAuthStore } from '../../stores/auth.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const error = ref('')
const result = ref(null)

onMounted(async () => {
  // Toss는 쿼리 파라미터를 # 앞에 붙여서 리다이렉트하므로 window.location.search로 읽어야 함
  const params = new URLSearchParams(window.location.search)
  const paymentKey = params.get('paymentKey') || route.query.paymentKey
  const orderId    = params.get('orderId')    || route.query.orderId
  const amount     = params.get('amount')     || route.query.amount
  if (!paymentKey || !orderId || !amount) {
    error.value = '결제 정보가 올바르지 않습니다.'
    loading.value = false
    return
  }
  try {
    const data = await verifyPayment(paymentKey, orderId, amount)
    result.value = data
    // Pinia auth store의 ticketCount도 업데이트
    if (auth.user) auth.user.ticketCount = data.ticketCount
  } catch (e) {
    error.value = e.message || '결제 검증에 실패했습니다.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div style="min-height:100vh; display:flex; align-items:center; justify-content:center; background:var(--ink-50, #f8f9fa);">
    <div style="background:#fff; border-radius:16px; box-shadow:0 4px 24px rgba(0,0,0,.08); padding:48px 40px; max-width:400px; width:100%; text-align:center;">

      <div v-if="loading" style="color:var(--ink-400);">결제 확인 중...</div>

      <template v-else-if="error">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#e53e3e" stroke-width="1.5" style="margin:0 auto 16px; display:block;"><circle cx="12" cy="12" r="10"/><path d="M15 9l-6 6M9 9l6 6"/></svg>
        <h2 style="font-size:20px; font-weight:700; margin-bottom:8px;">결제 실패</h2>
        <p style="color:var(--ink-500); font-size:14px; margin-bottom:28px;">{{ error }}</p>
        <button class="btn btn-primary" style="width:100%;" @click="router.push('/mypage?section=billing')">돌아가기</button>
      </template>

      <template v-else>
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="1.5" style="margin:0 auto 16px; display:block;"><circle cx="12" cy="12" r="10"/><path d="M8 12l3 3 5-5"/></svg>
        <h2 style="font-size:22px; font-weight:700; margin-bottom:8px;">결제 완료!</h2>
        <p style="color:var(--ink-500); font-size:15px; margin-bottom:6px;">
          <strong style="color:var(--ink-900);">{{ result?.tickets }}회</strong> 이용권이 충전되었습니다.
        </p>
        <p style="color:var(--ink-400); font-size:13px; margin-bottom:28px;">
          현재 보유 이용권: <strong style="color:var(--ink-700);">{{ result?.ticketCount }}회</strong>
        </p>
        <button class="btn btn-primary" style="width:100%;" @click="router.push('/mypage?section=billing')">결제 내역 확인</button>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { toast } from '../../../utils/toast.js'
import { useAuthStore } from '../../../stores/auth.js'
import { preparePayment, fetchPaymentHistory } from '../../../api/paymentApi.js'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const TICKET_PLANS = [
  { name: '10회권', tickets: 10, bonus: 0, amount: 5_000,  label: '10회',     priceLabel: '5,000원' },
  { name: '20회권', tickets: 20, bonus: 1, amount: 10_000, label: '20 + 1회', priceLabel: '10,000원' },
  { name: '30회권', tickets: 30, bonus: 2, amount: 15_000, label: '30 + 2회', priceLabel: '15,000원' },
  { name: '50회권', tickets: 50, bonus: 5, amount: 25_000, label: '50 + 5회', priceLabel: '25,000원' },
]

const paymentHistory = ref([])
const paymentHistoryLoading = ref(false)
const paymentLoading = ref(false)

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

async function buyTickets(plan) {
  if (paymentLoading.value) return
  paymentLoading.value = true
  try {
    const { orderId, amount } = await preparePayment(plan.name)
    if (!window.TossPayments) {
      await new Promise((resolve, reject) => {
        const s = document.createElement('script')
        s.src = 'https://js.tosspayments.com/v1/payment'
        s.onload = resolve
        s.onerror = reject
        document.head.appendChild(s)
      })
    }
    const toss = window.TossPayments(import.meta.env.VITE_TOSS_CLIENT_KEY || 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq')
    await toss.requestPayment('카드', {
      amount,
      orderId,
      orderName: plan.name + ' 구매',
      customerName: user.value?.userName || '사용자',
      successUrl: `${window.location.origin}${window.location.pathname}#/payment/success`,
      failUrl:    `${window.location.origin}${window.location.pathname}#/payment/fail`,
    })
  } catch (e) {
    if (e?.code !== 'USER_CANCEL') toast(e.message || '결제 중 오류가 발생했습니다.')
  } finally {
    paymentLoading.value = false
  }
}

onMounted(async () => {
  paymentHistoryLoading.value = true
  try { paymentHistory.value = await fetchPaymentHistory() }
  catch (e) { toast(e.message) }
  finally { paymentHistoryLoading.value = false }
})
</script>

<template>
  <div>
    <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 구독 및 결제</div>
    <h2 class="mp-h1" style="margin:4px 0 20px;">구독 및 결제</h2>

    <div class="card plan-now" style="margin-bottom:24px;">
      <div>
        <div class="text-sm text-muted">보유 이용권</div>
        <div class="plan-now-name" style="font-size:28px; font-weight:700; color:var(--ink-900);">
          {{ user?.ticketCount ?? 0 }}<span style="font-size:16px; font-weight:400; color:var(--ink-500); margin-left:4px;">회</span>
        </div>
        <div class="plan-now-meta">1회권 = 모의 면접 1회 이용</div>
      </div>
      <div style="text-align:right;">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--accent-blue,#1f6fe5)" stroke-width="1.5"><rect x="1" y="6" width="22" height="13" rx="2"/><path d="M1 10h22"/></svg>
      </div>
    </div>

    <h3 class="mp-section-title">이용권 충전</h3>
    <div class="plan-grid" style="grid-template-columns: repeat(4, 1fr);">
      <div v-for="p in TICKET_PLANS" :key="p.name" class="plan-card">
        <div class="plan-name">{{ p.name }}</div>
        <div class="plan-price">{{ p.priceLabel }}</div>
        <ul class="plan-feats">
          <li>총 {{ p.tickets + p.bonus }}회 이용</li>
          <li v-if="p.bonus > 0" style="color:var(--green-500); font-weight:600;">+ {{ p.bonus }}회 보너스 증정</li>
          <li>회당 {{ Math.round(p.amount / (p.tickets + p.bonus)).toLocaleString() }}원</li>
        </ul>
        <button class="btn btn-primary btn-block" :disabled="paymentLoading" @click="buyTickets(p)">
          {{ paymentLoading ? '처리 중...' : '구매하기' }}
        </button>
      </div>
    </div>

    <h3 class="mp-section-title">결제 내역</h3>
    <div class="card" style="padding:0; overflow:hidden;">
      <div v-if="paymentHistoryLoading" style="padding:40px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
      <div v-else-if="!paymentHistory.length" style="padding:40px; text-align:center; color:var(--ink-400);">결제 내역이 없습니다.</div>
      <table v-else class="history-table">
        <thead><tr><th>날짜</th><th>내용</th><th>이용권</th><th>금액</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="h in paymentHistory" :key="h.paymentId">
            <td><span class="text-sm text-muted">{{ formatDate(h.approvedAt || h.createdAt) }}</span></td>
            <td>{{ h.planName }}</td>
            <td><span class="text-sm">+{{ h.tickets }}회</span></td>
            <td>{{ h.amount.toLocaleString() }}원</td>
            <td>
              <span class="badge" :class="h.status === 'DONE' ? 'badge-green' : h.status === 'FAILED' ? 'badge-red' : ''">
                {{ h.status === 'DONE' ? '완료' : h.status === 'FAILED' ? '실패' : '처리 중' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

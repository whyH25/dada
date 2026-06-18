const BASE = 'http://localhost:8080/api/payments'
const OPTS = { credentials: 'include' }

async function json(res) {
  const data = await res.json()
  if (!data.success) throw new Error(data.message || '오류가 발생했습니다.')
  return data
}

export async function preparePayment(planName) {
  const data = await json(await fetch(`${BASE}/ready`, {
    ...OPTS,
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ planName }),
  }))
  return data // { orderId, amount, tickets }
}

export async function verifyPayment(paymentKey, orderId, amount) {
  const data = await json(await fetch(`${BASE}/verify`, {
    ...OPTS,
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ paymentKey, orderId, amount: Number(amount) }),
  }))
  return data // { tickets, ticketCount }
}

export async function fetchPaymentHistory() {
  const data = await json(await fetch(`${BASE}/history`, OPTS))
  return data.data ?? []
}

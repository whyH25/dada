import { API_BASE_URL } from '../config/api.js'
const BASE = `${API_BASE_URL}/notices`
const OPTS = { credentials: 'include' }

export async function fetchNotices() {
  const res = await fetch(BASE, OPTS)
  const data = await res.json()
  return data.data ?? []
}

export async function fetchNotice(id) {
  const res = await fetch(`${BASE}/${id}`, OPTS)
  if (!res.ok) return null
  const data = await res.json()
  return data.data
}

export async function fetchEventBanner() {
  const res = await fetch(`${BASE}/event-banner`, OPTS)
  if (!res.ok) return null
  const data = await res.json()
  return data.data   // null 이면 배너 없음
}

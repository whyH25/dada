import { API_BASE_URL } from '../config/api.js'
const BASE = `${API_BASE_URL}/job-schedules`
const OPTS = { credentials: 'include' }

export async function fetchJobSchedules() {
  const res = await fetch(BASE, OPTS)
  if (!res.ok) return []
  const json = await res.json()
  return json.data ?? []
}

export async function getBookmarks() {
  const res = await fetch(`${BASE}/bookmarks`, OPTS)
  if (!res.ok) throw new Error('bookmark fetch failed')
  const json = await res.json()
  return json.data ?? []
}

export async function toggleBookmark(scheduleId) {
  const res = await fetch(`${BASE}/${scheduleId}/bookmark`, {
    ...OPTS,
    method: 'POST',
  })
  if (!res.ok) throw new Error('bookmark toggle failed')
  return res.json() // { success, saved }
}

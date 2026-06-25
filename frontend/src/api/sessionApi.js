import { API_BASE_URL } from '../config/api.js'
const BASE = API_BASE_URL

export async function startSession({ roomId }) {
  const res = await fetch(`${BASE}/sessions/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ roomId }),
  })
  if (!res.ok) throw new Error('면접 세션 시작에 실패했습니다.')
  return res.json()
}

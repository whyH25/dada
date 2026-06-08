const BASE = 'http://localhost:8080/api'

export async function startSession({ roomId, userId }) {
  const res = await fetch(`${BASE}/sessions/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ roomId, userId }),
  })
  if (!res.ok) throw new Error('면접 세션 시작에 실패했습니다.')
  return res.json()
}

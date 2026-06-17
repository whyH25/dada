const BASE = 'http://localhost:8080/api'

export async function fetchMyInterviewRooms() {
  const res = await fetch(`${BASE}/interview-rooms`, { credentials: 'include' })
  if (!res.ok) throw new Error('면접 기록을 불러오는데 실패했습니다.')
  return res.json()
}

export async function fetchRoomScenarios(roomId) {
  const res = await fetch(`${BASE}/interview-rooms/${roomId}/scenarios`, { credentials: 'include' })
  if (!res.ok) throw new Error('시나리오를 불러오는데 실패했습니다.')
  return res.json()
}

// 면접 리포트 전체 조회 (report + applicants + questions)
// 리포트가 없으면 null 반환
export async function fetchRoomReport(roomId) {
  const res = await fetch(`${BASE}/interview-rooms/${roomId}/report`, { credentials: 'include' })
  if (res.status === 204) return null
  if (!res.ok) throw new Error('리포트를 불러오는데 실패했습니다.')
  return res.json()
}

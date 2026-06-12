const BASE = 'http://localhost:8080/api/stories'
const OPTS = { credentials: 'include' }

export async function fetchStories() {
  const res = await fetch(BASE, OPTS)
  const data = await res.json()
  return data.data ?? []
}

export async function fetchStory(id) {
  const res = await fetch(`${BASE}/${id}`, OPTS)
  if (!res.ok) return null
  const data = await res.json()
  return data.data
}

export async function toggleLike(storyId) {
  const res = await fetch(`${BASE}/${storyId}/like`, { ...OPTS, method: 'POST' })
  if (!res.ok) throw new Error('좋아요 요청 실패')
  return res.json() // { success, liked }
}

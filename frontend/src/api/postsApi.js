const BASE = 'http://localhost:8080/api'
const OPTS = { credentials: 'include' }
const JSON_OPTS = { ...OPTS, headers: { 'Content-Type': 'application/json' } }

async function json(res) {
  if (!res.ok) throw new Error(res.status)
  return res.json()
}

export async function fetchPosts(category = '') {
  const q = category ? `?category=${encodeURIComponent(category)}` : ''
  const data = await json(await fetch(`${BASE}/posts${q}`, OPTS))
  return data.data ?? []
}

export async function fetchPost(id) {
  const data = await json(await fetch(`${BASE}/posts/${id}`, OPTS))
  return data.data ?? null
}

export async function createPost(payload) {
  const data = await json(await fetch(`${BASE}/posts`, { ...JSON_OPTS, method: 'POST', body: JSON.stringify(payload) }))
  return data
}

export async function updatePost(id, payload) {
  await json(await fetch(`${BASE}/posts/${id}`, { ...JSON_OPTS, method: 'PUT', body: JSON.stringify(payload) }))
}

export async function deletePost(id) {
  await json(await fetch(`${BASE}/posts/${id}`, { ...JSON_OPTS, method: 'DELETE' }))
}

export async function togglePostLike(id) {
  const data = await json(await fetch(`${BASE}/posts/${id}/like`, { ...JSON_OPTS, method: 'POST' }))
  return data
}

export async function fetchComments(postId) {
  const data = await json(await fetch(`${BASE}/posts/${postId}/comments`, OPTS))
  return data.data ?? []
}

export async function addComment(postId, content, anonymous) {
  const data = await json(await fetch(`${BASE}/posts/${postId}/comments`, {
    ...JSON_OPTS, method: 'POST', body: JSON.stringify({ content, anonymous })
  }))
  return data
}

export async function deleteComment(postId, commentId) {
  await json(await fetch(`${BASE}/posts/${postId}/comments/${commentId}`, { ...JSON_OPTS, method: 'DELETE' }))
}

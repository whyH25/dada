const BASE = 'http://localhost:8080/api/admin'
const opts = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

async function request(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    ...opts,
    body: body ? JSON.stringify(body) : undefined,
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || '요청에 실패했습니다.')
  return data
}

// 사용자
export const getUsers = () => request('GET', '/users')
export const deleteUser = (id) => request('DELETE', `/users/${id}`)

// 채용일정
export const getJobSchedules = () => request('GET', '/job-schedules')
export const createJobSchedule = (body) => request('POST', '/job-schedules', body)
export const updateJobSchedule = (id, body) => request('PUT', `/job-schedules/${id}`, body)
export const deleteJobSchedule = (id) => request('DELETE', `/job-schedules/${id}`)

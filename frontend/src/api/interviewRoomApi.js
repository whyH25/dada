const BASE = 'http://localhost:8080/api'

const EMPLOYMENT_TYPE_MAP = {
  '신입': 'NEW',
  '인턴': 'INTERN',
  '경력': 'EXPERIENCED',
}

// 직무 대분류 + 소속 직무 목록 조회
export async function fetchJobGroups() {
  const res = await fetch(`${BASE}/categories/job-groups`)
  if (!res.ok) throw new Error('직무 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function createInterviewRoom({ companyName, jobId, difficulty, employmentType, interviewerCount, aiApplicantCount, resumeId, portfolioId, language }) {
  // 파일 업로드 대신 마이페이지에 등록된 이력서/포트폴리오의 id만 전달
  const params = new URLSearchParams()
  params.append('companyName', companyName)
  if (jobId != null) params.append('jobId', jobId)
  params.append('difficulty', difficulty)
  params.append('applicantType', EMPLOYMENT_TYPE_MAP[employmentType] ?? 'NEW')
  params.append('aiInterviewerCnt', interviewerCount)
  params.append('aiApplicantCnt', aiApplicantCount)
  if (resumeId != null) params.append('resumeId', resumeId)
  if (portfolioId != null) params.append('portfolioId', portfolioId)
  // 언어 미지정 시 백엔드가 기본값 "KO"로 처리
  if (language != null) params.append('language', language)

  const res = await fetch(`${BASE}/interview-rooms`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params,
    credentials: 'include',
  })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || '면접방 생성에 실패했습니다.')
  }
  return res.json()
}

export async function startInterview(roomId) {
  const res = await fetch(`${BASE}/interview-rooms/${roomId}/start`, {
    method: 'POST',
    credentials: 'include',
  })
  if (!res.ok) {
    let msg = 'AI 대본 생성에 실패했습니다.'
    try { const data = await res.json(); if (data.message) msg = data.message } catch { /* ignore */ }
    throw new Error(msg)
  }
  return res.json()
}

export function updateRoomStatusApi(roomId, status) {
  return fetch(`${BASE}/interview-rooms/${roomId}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ status }),
    keepalive: true,
  })
}

export async function deleteInterviewRoomApi(roomId) {
  const res = await fetch(`${BASE}/interview-rooms/${roomId}`, {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) {
    let msg = '면접 기록 삭제에 실패했습니다.'
    try { const data = await res.json(); if (data.message) msg = data.message } catch { /* ignore */ }
    throw new Error(msg)
  }
  return res.json()
}

const BASE = 'http://localhost:8080/api'

export async function fetchJobCategories() {
  const res = await fetch(`${BASE}/categories/jobs`)
  if (!res.ok) throw new Error('직무 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function createInterviewRoom({ userId, companyName, jobId, difficulty, interviewerCount, aiApplicantCount, resumeFile, portfolioFile }) {
  const form = new FormData()
  form.append('userId', userId)
  form.append('companyName', companyName)
  if (jobId != null) form.append('jobId', jobId)
  form.append('difficulty', difficulty)
  form.append('interviewerCount', interviewerCount)
  form.append('aiApplicantCount', aiApplicantCount)
  if (resumeFile) form.append('resumeFile', resumeFile)
  if (portfolioFile) form.append('portfolioFile', portfolioFile)

  const res = await fetch(`${BASE}/interview-rooms`, { method: 'POST', body: form })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || '면접방 생성에 실패했습니다.')
  }
  return res.json()
}

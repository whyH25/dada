const BASE = 'http://localhost:8080/api'

const EMPLOYMENT_TYPE_MAP = {
  '신입': 'NEW',
  '인턴': 'INTERN',
  '경력': 'EXPERIENCED',
}

const FALLBACK_JOB_CATEGORIES = [
  { jobId: 1,  jobCode: 'DEV_BACKEND',    jobName: '백엔드 개발자' },
  { jobId: 2,  jobCode: 'DEV_FRONTEND',   jobName: '프론트엔드 개발자' },
  { jobId: 3,  jobCode: 'DEV_FULLSTACK',  jobName: '풀스택 개발자' },
  { jobId: 4,  jobCode: 'DEV_MOBILE',     jobName: '모바일 앱 개발자' },
  { jobId: 5,  jobCode: 'DEV_AI',         jobName: 'AI/ML 엔지니어' },
  { jobId: 6,  jobCode: 'DEV_DATA',       jobName: '데이터 엔지니어' },
  { jobId: 7,  jobCode: 'DEV_DEVOPS',     jobName: 'DevOps/클라우드 엔지니어' },
  { jobId: 8,  jobCode: 'DEV_SECURITY',   jobName: '보안 엔지니어' },
  { jobId: 9,  jobCode: 'DEV_EMBEDDED',   jobName: '임베디드 개발자' },
  { jobId: 10, jobCode: 'DEV_GAME',       jobName: '게임 개발자' },
  { jobId: 11, jobCode: 'DATA_ANALYST',   jobName: '데이터 분석가' },
  { jobId: 12, jobCode: 'DATA_SCIENTIST', jobName: '데이터 사이언티스트' },
  { jobId: 13, jobCode: 'PM',             jobName: '프로덕트 매니저' },
  { jobId: 14, jobCode: 'PO',             jobName: '프로덕트 오너' },
  { jobId: 15, jobCode: 'DESIGN_UX',      jobName: 'UX 디자이너' },
  { jobId: 16, jobCode: 'DESIGN_UI',      jobName: 'UI 디자이너' },
  { jobId: 17, jobCode: 'DESIGN_BRAND',   jobName: '브랜드 디자이너' },
  { jobId: 18, jobCode: 'MKT_DIGITAL',    jobName: '디지털 마케터' },
  { jobId: 19, jobCode: 'MKT_CONTENT',    jobName: '콘텐츠 마케터' },
  { jobId: 20, jobCode: 'MKT_BRAND',      jobName: '브랜드 마케터' },
  { jobId: 21, jobCode: 'SALES',          jobName: '영업 담당자' },
  { jobId: 22, jobCode: 'CS',             jobName: '고객 서비스' },
  { jobId: 23, jobCode: 'HR',             jobName: '인사/채용 담당자' },
  { jobId: 24, jobCode: 'FINANCE',        jobName: '재무/회계' },
  { jobId: 25, jobCode: 'STRATEGY',       jobName: '전략기획' },
  { jobId: 26, jobCode: 'SUPPLY_CHAIN',   jobName: '공급망/물류' },
  { jobId: 27, jobCode: 'QA',             jobName: 'QA 엔지니어' },
  { jobId: 28, jobCode: 'RESEARCH',       jobName: 'R&D 연구원' },
  { jobId: 29, jobCode: 'LEGAL',          jobName: '법무' },
  { jobId: 30, jobCode: 'CONSULTING',     jobName: '컨설턴트' },
]

export async function fetchJobCategories() {
  try {
    const res = await fetch(`${BASE}/categories/jobs`)
    if (!res.ok) throw new Error()
    return res.json()
  } catch {
    return FALLBACK_JOB_CATEGORIES
  }
}

export async function createInterviewRoom({ companyName, jobId, difficulty, employmentType, interviewerCount, aiApplicantCount, resumeId, portfolioId }) {
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

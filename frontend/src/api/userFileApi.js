const BASE = 'http://localhost:8080/api/user-files'

// 마이페이지 등록 서류 목록 (type: 'resume' | 'portfolio')
export async function fetchMyFiles(type) {
  const res = await fetch(`${BASE}/${type}`, { credentials: 'include' })
  if (!res.ok) throw new Error('서류 목록을 불러오는데 실패했습니다.')
  return res.json()
}

// 서류 업로드 (파일 저장 + 텍스트 파싱은 서버에서 처리)
export async function uploadFile(type, file) {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(`${BASE}/${type}`, { method: 'POST', body: form, credentials: 'include' })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || '업로드에 실패했습니다.')
  }
  return res.json()
}

// 서류 삭제
export async function deleteFile(type, id) {
  const res = await fetch(`${BASE}/${type}/${id}`, { method: 'DELETE', credentials: 'include' })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || '삭제에 실패했습니다.')
  }
  return res.json()
}

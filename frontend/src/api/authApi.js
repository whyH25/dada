export async function loginApi(userEmail, userPwd) {
  const response = await fetch('http://localhost:8080/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ userEmail, userPwd }),
  })

  if (!response.ok) {
    const data = await response.json()
    throw new Error(data.message || '로그인에 실패했습니다.')
  }

  return response
}

export async function signupApi(userData) {
  const response = await fetch('http://localhost:8080/api/users/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(userData),
  })
  const data = await response.json()
  if (!response.ok) {
    throw new Error(data.message || '회원가입에 실패했습니다.')
  }
  return data
}

export async function getMeApi() {
  const response = await fetch('http://localhost:8080/api/users/me', {
    credentials: 'include',
  })
  if (!response.ok) return null
  const data = await response.json()
  return data.data
}

export async function logoutApi() {
  await fetch('http://localhost:8080/api/users/logout', {
    method: 'POST',
    credentials: 'include',
  })
}

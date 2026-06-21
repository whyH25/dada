import { defineStore } from 'pinia'

const BASE = 'http://localhost:8080/api/admin'
const OPTS = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

// restoreSession이 여러 곳(main.js, 라우터 가드)에서 호출돼도 실제 API 호출은 한 번만 일어나도록 캐싱
let restoreSessionPromise = null

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    admin: null,
    isAdminLoggedIn: false,
  }),
  actions: {
    async login(adminEmail, adminPwd) {
      const res = await fetch(`${BASE}/login`, {
        ...OPTS,
        method: 'POST',
        body: JSON.stringify({ adminEmail, adminPwd }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.message || '로그인에 실패했습니다.')
      this.admin = data.data
      this.isAdminLoggedIn = true
    },
    async logout(router) {
      await fetch(`${BASE}/logout`, { ...OPTS, method: 'POST' })
      this.admin = null
      this.isAdminLoggedIn = false
      if (router) router.push('/admin/login')
    },
    restoreSession() {
      if (!restoreSessionPromise) {
        restoreSessionPromise = fetch(`${BASE}/me`, OPTS)
          .then((res) => (res.ok ? res.json() : null))
          .then((data) => {
            if (data && data.success) {
              this.admin = data.data
              this.isAdminLoggedIn = true
            }
          })
          .catch(() => {
            // 세션 없음
          })
      }
      return restoreSessionPromise
    },
  },
})

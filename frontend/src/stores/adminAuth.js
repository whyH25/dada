import { defineStore } from 'pinia'

const BASE = 'http://localhost:8080/api/admin'
const OPTS = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

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
    async restoreSession() {
      try {
        const res = await fetch(`${BASE}/me`, OPTS)
        if (!res.ok) return
        const data = await res.json()
        if (data.success) {
          this.admin = data.data
          this.isAdminLoggedIn = true
        }
      } catch {
        // 세션 없음
      }
    },
  },
})

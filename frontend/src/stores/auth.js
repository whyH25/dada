import { defineStore } from 'pinia'
import { loginApi, logoutApi, getMeApi } from '../api/authApi.js'

export const GATED_ROUTES = ['create', 'mypage', 'interview', 'room-intro']

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    user: null,
    loginOpen: false,
    interestOpen: false,
    pendingAction: null,
  }),
  actions: {
    openLogin(pending = null) {
      this.pendingAction = pending
      this.loginOpen = true
      document.body.style.overflow = 'hidden'
    },
    closeLogin() {
      this.loginOpen = false
      this.pendingAction = null
      document.body.style.overflow = ''
    },
    async doLogin(userEmail, userPwd, router) {
      const res = await loginApi(userEmail, userPwd)
      const data = await res.json()
      this.user = data.data
      this.isLoggedIn = true
      this.loginOpen = false
      document.body.style.overflow = ''
      const action = this.pendingAction
      this.pendingAction = null
      if (action) {
        if (typeof action === 'function') action()
        else if (typeof action === 'string' && router) router.push('/' + action)
      }
    },
    async logout(router) {
      await logoutApi()
      this.isLoggedIn = false
      this.user = null
      if (router) router.push('/')
    },
    async restoreSession() {
      const user = await getMeApi()
      if (user) {
        this.user = user
        this.isLoggedIn = true
      }
    },
    openInterest() { this.interestOpen = true; document.body.style.overflow = 'hidden' },
    closeInterest() { this.interestOpen = false; document.body.style.overflow = '' },
  },
})

import { defineStore } from 'pinia'
import { loginApi, logoutApi, getMeApi } from '../api/authApi.js'

export const GATED_ROUTES = ['create', 'mypage', 'interview', 'room-intro']

// restoreSession이 여러 곳(main.js, 라우터 가드)에서 호출돼도 실제 API 호출은 한 번만 일어나도록 캐싱
// 라우터 가드가 이 프로미스를 같이 기다려서, 새로고침 시 세션 복원 전에 로그인 여부를 잘못 판단하는 걸 막음
let restoreSessionPromise = null

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
    restoreSession() {
      if (!restoreSessionPromise) {
        restoreSessionPromise = getMeApi().then((user) => {
          if (user) {
            this.user = user
            this.isLoggedIn = true
          }
        })
      }
      return restoreSessionPromise
    },
    openInterest() { this.interestOpen = true; document.body.style.overflow = 'hidden' },
    closeInterest() { this.interestOpen = false; document.body.style.overflow = '' },
  },
})

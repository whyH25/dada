import { defineStore } from 'pinia'

// 원본의 GATED_ROUTES: 로그인 후 이용 가능한 경로
export const GATED_ROUTES = ['create', 'mypage', 'interview', 'room-intro']

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    loginOpen: false,        // 로그인 모달 표시 여부
    interestOpen: false,     // 관심기업 모달 표시 여부
    pendingAction: null,     // 로그인 후 실행할 콜백/경로
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
    // 원본 doLogin(): 로그인 처리 후 대기중이던 액션 실행
    doLogin(router) {
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
    logout(router) {
      this.isLoggedIn = false
      if (router) router.push('/')
    },
    openInterest() { this.interestOpen = true; document.body.style.overflow = 'hidden' },
    closeInterest() { this.interestOpen = false; document.body.style.overflow = '' },
  },
})

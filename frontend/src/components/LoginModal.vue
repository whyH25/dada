<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { toast } from '../utils/toast.js'

const emit = defineEmits(['login-success'])

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const SOCIAL_BASE = 'http://localhost:8080'
function loginWithKakao() { window.location.href = `${SOCIAL_BASE}/oauth2/authorization/kakao` }
function loginWithGoogle() { window.location.href = `${SOCIAL_BASE}/oauth2/authorization/google` }

const REMEMBERED_EMAIL_KEY = 'rememberedEmail'

const userEmail = ref('')
const userPwd = ref('')
const rememberId = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 저장된 아이디가 있으면 입력란에 미리 채워둠
// 소셜 로그인 실패 후 돌아온 경우(?socialLoginError=1) 모달을 열고 에러 표시
onMounted(() => {
  const saved = localStorage.getItem(REMEMBERED_EMAIL_KEY)
  if (saved) {
    userEmail.value = saved
    rememberId.value = true
  }

  if (route.query.socialLoginError) {
    errorMsg.value = '소셜 로그인에 실패했습니다. 이메일 제공에 동의해주셨는지 확인해주세요.'
    toast(errorMsg.value)
    auth.openLogin()
    router.replace({ query: { ...route.query, socialLoginError: undefined } })
  }
})

// 모달이 닫힐 때(로그인 성공/수동 닫기 공통)마다 비밀번호는 항상 비우고,
// 아이디는 "아이디 저장"이 꺼져 있을 때만 비움
watch(() => auth.loginOpen, (open) => {
  if (open) return
  userPwd.value = ''
  errorMsg.value = ''
  successMsg.value = ''
  if (!rememberId.value) userEmail.value = ''
})

const emailRef = ref(null)
const pwdRef = ref(null)

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate() {
  if (!userEmail.value.trim()) {
    errorMsg.value = '이메일을 입력해주세요.'
    emailRef.value?.focus()
    return false
  }
  if (!EMAIL_REGEX.test(userEmail.value.trim())) {
    errorMsg.value = '올바른 이메일 형식이 아닙니다.'
    emailRef.value?.focus()
    return false
  }
  if (!userPwd.value.trim()) {
    errorMsg.value = '비밀번호를 입력해주세요.'
    pwdRef.value?.focus()
    return false
  }
  return true
}

async function handleLogin() {
  errorMsg.value = ''
  successMsg.value = ''
  if (!validate()) return
  loading.value = true
  try {
    await auth.doLogin(userEmail.value, userPwd.value, router)
    // 아이디 저장 체크 여부에 따라 이메일을 기기에 저장/삭제
    if (rememberId.value) {
      localStorage.setItem(REMEMBERED_EMAIL_KEY, userEmail.value.trim())
    } else {
      localStorage.removeItem(REMEMBERED_EMAIL_KEY)
    }
    successMsg.value = '로그인에 성공했습니다.'
    emit('login-success')
  } catch (e) {
    errorMsg.value = e.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-overlay" :class="{ open: auth.loginOpen }" @click.self="auth.closeLogin()">
    <div class="auth-modal" role="dialog" aria-modal="true">
      <button class="auth-close" @click="auth.closeLogin()" aria-label="닫기">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12" /></svg>
      </button>
      <div class="auth-brand">
        <div class="brand-logo"><span class="d"></span><span class="d"></span><span class="d"></span></div>
        <div class="auth-brand-name">다대다</div>
      </div>
      <h2 class="auth-title">로그인이 필요해요</h2>
      <p class="auth-sub">면접방 생성·이력서 등록은 로그인 후 이용할 수 있어요.</p>

      <div class="auth-field">
        <label class="auth-label">이메일</label>
        <input class="input" type="email" v-model="userEmail" placeholder="you@email.com" ref="emailRef" />
      </div>
      <div class="auth-field">
        <label class="auth-label">비밀번호</label>
        <input class="input" type="password" v-model="userPwd" placeholder="비밀번호" @keyup.enter="handleLogin" ref="pwdRef" />
      </div>
      <div class="auth-row">
        <label class="auth-remember"><input type="checkbox" v-model="rememberId" /> 아이디 저장</label>
        <a class="auth-forgot" style="cursor:pointer" @click="() => { auth.closeLogin(); router.push('/find-password') }">아이디/비밀번호 찾기</a>
      </div>

      <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>
      <p v-if="successMsg" class="auth-success">{{ successMsg }}</p>

      <button class="btn btn-primary btn-block btn-lg" :disabled="loading" @click="handleLogin">
        {{ loading ? '로그인 중...' : '로그인' }}
      </button>

      <div class="auth-divider"><span>또는</span></div>
      <div class="auth-social">
        <!-- <button class="auth-social-btn kakao" @click="loginWithKakao">카카오로 시작하기</button> -->
        <button class="auth-social-btn" @click="loginWithGoogle">Google 계정으로 계속</button>
      </div>
      <div class="auth-foot">
        아직 회원이 아니신가요? <a style="cursor:pointer" @click="() => { auth.closeLogin(); router.push('/signup') }">회원가입</a>
      </div>
    </div>
  </div>
</template>

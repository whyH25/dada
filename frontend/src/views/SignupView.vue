<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { signupApi } from '../api/authApi.js'

const router = useRouter()

const userEmail = ref('')
const userPwd = ref('')
const userPwdConfirm = ref('')
const userName = ref('')
const userPhone = ref('')
const loading = ref(false)
const errorMsg = ref('')

const emailRef = ref(null)
const pwdRef = ref(null)
const pwdConfirmRef = ref(null)
const nameRef = ref(null)

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
  if (!userPwd.value) {
    errorMsg.value = '비밀번호를 입력해주세요.'
    pwdRef.value?.focus()
    return false
  }
  if (userPwd.value.length < 8) {
    errorMsg.value = '비밀번호는 8자 이상이어야 합니다.'
    pwdRef.value?.focus()
    return false
  }
  if (userPwd.value !== userPwdConfirm.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.'
    pwdConfirmRef.value?.focus()
    return false
  }
  if (!userName.value.trim()) {
    errorMsg.value = '이름을 입력해주세요.'
    nameRef.value?.focus()
    return false
  }
  return true
}

async function handleSignup() {
  errorMsg.value = ''
  if (!validate()) return
  loading.value = true
  try {
    await signupApi({
      userEmail: userEmail.value.trim(),
      userPwd: userPwd.value,
      userName: userName.value.trim(),
      userPhone: userPhone.value.trim() || null,
    })
    alert('회원가입이 완료되었습니다.')
    router.push('/')
  } catch (e) {
    errorMsg.value = e.message || '회원가입 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="signup-page">
    <div class="signup-card">
      <div class="auth-brand" style="margin-bottom: 4px">
        <div class="brand-logo" style="transform: scale(0.8)">
          <span class="d"></span><span class="d"></span><span class="d"></span>
        </div>
        <div class="auth-brand-name">다대다</div>
      </div>

      <h2 class="signup-title">회원가입</h2>
      <p class="signup-sub">다대다와 함께 면접을 준비해보세요.</p>

      <div class="auth-field">
        <label class="auth-label">이메일 <span class="required">*</span></label>
        <input class="input" type="email" v-model="userEmail" placeholder="you@email.com" ref="emailRef" />
      </div>

      <div class="auth-field">
        <label class="auth-label">비밀번호 <span class="required">*</span></label>
        <input class="input" type="password" v-model="userPwd" placeholder="8자 이상" ref="pwdRef" />
      </div>

      <div class="auth-field">
        <label class="auth-label">비밀번호 확인 <span class="required">*</span></label>
        <input class="input" type="password" v-model="userPwdConfirm" placeholder="비밀번호 재입력" ref="pwdConfirmRef" @keyup.enter="handleSignup" />
      </div>

      <div class="auth-field">
        <label class="auth-label">이름 <span class="required">*</span></label>
        <input class="input" type="text" v-model="userName" placeholder="홍길동" ref="nameRef" />
      </div>

      <div class="auth-field">
        <label class="auth-label">전화번호 <span class="optional">(선택)</span></label>
        <input class="input" type="tel" v-model="userPhone" placeholder="010-0000-0000" />
      </div>

      <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

      <button class="btn btn-primary btn-block btn-lg" :disabled="loading" @click="handleSignup">
        {{ loading ? '가입 중...' : '회원가입' }}
      </button>

      <div class="signup-foot">
        이미 계정이 있으신가요?
        <a style="cursor:pointer" @click="router.push('/')">로그인</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.signup-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-base, #f5f6f8);
}
.signup-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 28px 36px;
  width: 100%;
  max-width: 440px;
}
.signup-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-900, #111);
  margin: 0 0 4px;
}
.signup-sub {
  font-size: 13.5px;
  color: var(--ink-400, #888);
  margin: 0 0 14px;
}
.auth-field {
  margin-bottom: 8px;
}
.required { color: #e53e3e; font-size: 12px; }
.optional { color: var(--ink-400, #aaa); font-size: 12px; font-weight: 400; }
.signup-foot {
  text-align: center;
  margin-top: 20px;
  font-size: 13.5px;
  color: var(--ink-500, #666);
}
.signup-foot a {
  color: var(--green-600, #2c7a4b);
  font-weight: 600;
  margin-left: 4px;
}
</style>

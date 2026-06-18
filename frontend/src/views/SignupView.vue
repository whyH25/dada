<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { signupApi, sendVerificationCodeApi, verifyCodeApi } from '../api/authApi.js'

const router = useRouter()

const userEmail = ref('')
const userPwd = ref('')
const userPwdConfirm = ref('')
const userName = ref('')
const userPhone = ref('')
const loading = ref(false)
const errorMsg = ref('')
const showSuccessModal = ref(false)

const verificationCode = ref('')
const codeSent = ref(false)
const codeVerified = ref(false)
const sendingCode = ref(false)
const verifyingCode = ref(false)
const codeError = ref('')
const codeSuccess = ref('')
const countdown = ref(0)

let timer = null

const emailRef = ref(null)
const pwdRef = ref(null)
const pwdConfirmRef = ref(null)
const nameRef = ref(null)

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const formattedCountdown = computed(() => {
  const m = Math.floor(countdown.value / 60)
  const s = countdown.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

function startCountdown() {
  countdown.value = 300
  clearInterval(timer)
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timer)
      if (!codeVerified.value) {
        codeError.value = '인증시간이 만료되었습니다. 다시 발송해주세요.'
      }
    }
  }, 1000)
}

onUnmounted(() => clearInterval(timer))

async function handleSendCode() {
  codeError.value = ''
  codeSuccess.value = ''
  if (!userEmail.value.trim()) {
    codeError.value = '이메일을 입력해주세요.'
    return
  }
  if (!EMAIL_REGEX.test(userEmail.value.trim())) {
    codeError.value = '올바른 이메일 형식이 아닙니다.'
    return
  }
  sendingCode.value = true
  try {
    await sendVerificationCodeApi(userEmail.value.trim())
    codeSent.value = true
    codeVerified.value = false
    verificationCode.value = ''
    codeSuccess.value = '인증코드가 발송되었습니다.'
    startCountdown()
  } catch (e) {
    codeError.value = e.message
  } finally {
    sendingCode.value = false
  }
}

async function handleVerifyCode() {
  codeError.value = ''
  codeSuccess.value = ''
  if (!verificationCode.value.trim()) {
    codeError.value = '인증코드를 입력해주세요.'
    return
  }
  verifyingCode.value = true
  try {
    await verifyCodeApi(userEmail.value.trim(), verificationCode.value.trim())
    codeVerified.value = true
    clearInterval(timer)
    codeSuccess.value = '이메일 인증이 완료되었습니다.'
  } catch (e) {
    codeError.value = e.message
  } finally {
    verifyingCode.value = false
  }
}

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
  if (!codeVerified.value) {
    errorMsg.value = '이메일 인증을 완료해주세요.'
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
    showSuccessModal.value = true
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

      <!-- 이메일 + 인증코드 발송 -->
      <div class="auth-field">
        <label class="auth-label">이메일 <span class="required">*</span></label>
        <div class="input-row">
          <input
            class="input"
            type="email"
            v-model="userEmail"
            placeholder="you@email.com"
            ref="emailRef"
            :disabled="codeVerified"
          />
          <button
            class="btn btn-outline btn-sm"
            :disabled="sendingCode || codeVerified"
            @click="handleSendCode"
          >
            {{ sendingCode ? '발송 중...' : codeSent ? '재발송' : '인증코드 발송' }}
          </button>
        </div>
      </div>

      <!-- 인증코드 입력 -->
      <div v-if="codeSent && !codeVerified" class="auth-field">
        <label class="auth-label">인증코드</label>
        <div class="input-row">
          <div class="input-with-timer">
            <input
              class="input"
              type="text"
              v-model="verificationCode"
              placeholder="6자리 숫자"
              maxlength="6"
              @keyup.enter="handleVerifyCode"
            />
            <span v-if="countdown > 0" class="timer">{{ formattedCountdown }}</span>
          </div>
          <button
            class="btn btn-outline btn-sm"
            :disabled="verifyingCode || countdown === 0"
            @click="handleVerifyCode"
          >
            {{ verifyingCode ? '확인 중...' : '인증 확인' }}
          </button>
        </div>
      </div>

      <!-- 인증 결과 메시지 -->
      <p v-if="codeError" class="auth-error" style="margin-top: 4px">{{ codeError }}</p>
      <p v-if="codeSuccess" class="auth-success" style="margin-top: 4px">{{ codeSuccess }}</p>

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

      <button class="btn btn-primary btn-block btn-lg" :disabled="loading || !codeVerified" @click="handleSignup">
        {{ loading ? '가입 중...' : '회원가입' }}
      </button>

      <div class="signup-foot">
        이미 계정이 있으신가요?
        <a style="cursor:pointer" @click="router.push('/')">로그인</a>
      </div>
    </div>
  </div>

  <!-- 회원가입 완료 모달 -->
  <div v-if="showSuccessModal" class="signup-modal-overlay">
    <div class="signup-modal">
      <div class="signup-modal-icon">🎉</div>
      <h3 class="signup-modal-title">회원가입 완료!</h3>
      <p class="signup-modal-desc">
        가입을 환영해요!<br>
        무료 이용권 <strong>1장</strong>이 지급되었어요.
      </p>
      <p class="signup-modal-sub">지금 바로 AI 모의 면접을 체험해보세요.</p>
      <button class="btn btn-primary btn-block" @click="router.push('/')">무료 면접 시작하기</button>
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
.input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.input-row .input {
  flex: 1;
}
.input-with-timer {
  flex: 1;
  position: relative;
}
.input-with-timer .input {
  width: 100%;
  padding-right: 56px;
}
.timer {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #e53e3e;
  font-weight: 600;
}
.btn-sm {
  padding: 8px 12px;
  font-size: 13px;
  white-space: nowrap;
  flex-shrink: 0;
}
.btn-outline {
  background: #fff;
  border: 1px solid var(--green-600, #2c7a4b);
  color: var(--green-600, #2c7a4b);
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}
.btn-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.required { color: #e53e3e; font-size: 12px; }
.optional { color: var(--ink-400, #aaa); font-size: 12px; font-weight: 400; }
.auth-success {
  color: var(--green-600, #2c7a4b);
  font-size: 13px;
}
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
.auth-error {
  color: #e53e3e;
  font-size: 13px;
  margin: 4px 0 10px;
}
.signup-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.signup-modal {
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  max-width: 360px;
  width: 90%;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.signup-modal-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.signup-modal-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-900, #111);
  margin: 0 0 12px;
}
.signup-modal-desc {
  font-size: 15px;
  color: var(--ink-600, #444);
  line-height: 1.6;
  margin: 0 0 6px;
}
.signup-modal-desc strong {
  color: var(--green-600, #2c7a4b);
  font-size: 18px;
}
.signup-modal-sub {
  font-size: 13px;
  color: var(--ink-400, #888);
  margin: 0 0 24px;
}
</style>

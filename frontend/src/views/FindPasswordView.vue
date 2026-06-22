<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { findPasswordApi } from '../api/authApi.js'

const router = useRouter()

const userEmail = ref('')
const loading = ref(false)
const errorMsg = ref('')
const showSuccessModal = ref(false)

const emailRef = ref(null)
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
  return true
}

async function handleFindPassword() {
  errorMsg.value = ''
  if (!validate()) return
  loading.value = true
  try {
    await findPasswordApi(userEmail.value.trim())
    showSuccessModal.value = true
  } catch (e) {
    errorMsg.value = e.message || '가입되지 않은 회원입니다.'
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

      <h2 class="signup-title">비밀번호 찾기</h2>
      <p class="signup-sub">가입하신 이메일로 임시 비밀번호를 보내드려요.</p>

      <div class="auth-field">
        <label class="auth-label">이메일 <span class="required">*</span></label>
        <input
          class="input"
          type="email"
          v-model="userEmail"
          placeholder="you@email.com"
          ref="emailRef"
          @keyup.enter="handleFindPassword"
        />
      </div>

      <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

      <button class="btn btn-primary btn-block btn-lg" :disabled="loading" @click="handleFindPassword">
        {{ loading ? '발송 중...' : '임시 비밀번호 받기' }}
      </button>

      <div class="signup-foot">
        비밀번호가 기억나셨나요?
        <a style="cursor:pointer" @click="router.push('/')">로그인</a>
      </div>
    </div>
  </div>

  <!-- 발송 완료 모달 -->
  <div v-if="showSuccessModal" class="signup-modal-overlay">
    <div class="signup-modal">
      <div class="signup-modal-icon">📧</div>
      <h3 class="signup-modal-title">메일을 보냈어요!</h3>
      <p class="signup-modal-desc">
        입력하신 이메일로<br>
        임시 비밀번호를 보내드렸어요.
      </p>
      <p class="signup-modal-sub">로그인 후 마이페이지에서 비밀번호를 변경해주세요.</p>
      <button class="btn btn-primary btn-block" @click="router.push('/')">로그인하러 가기</button>
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
.signup-modal-sub {
  font-size: 13px;
  color: var(--ink-400, #888);
  margin: 0 0 24px;
}
</style>

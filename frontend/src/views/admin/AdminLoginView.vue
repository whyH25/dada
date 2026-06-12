<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '../../stores/adminAuth.js'

const router = useRouter()
const adminAuth = useAdminAuthStore()

const adminEmail = ref('')
const adminPwd = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!adminEmail.value || !adminPwd.value) {
    errorMsg.value = '이메일과 비밀번호를 입력해주세요.'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await adminAuth.login(adminEmail.value, adminPwd.value)
    router.push('/admin/jobs')
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="admin-login-brand">
        <div class="brand-logo-mini">
          <span class="d"></span><span class="d"></span><span class="d"></span>
        </div>
        <span class="admin-login-title">관리자 로그인</span>
      </div>

      <form class="admin-login-form" @submit.prevent="handleLogin">
        <div class="admin-input-group">
          <label class="admin-label">이메일</label>
          <input
            v-model="adminEmail"
            type="email"
            class="admin-input"
            placeholder="admin@example.com"
            autocomplete="username"
          />
        </div>
        <div class="admin-input-group">
          <label class="admin-label">비밀번호</label>
          <input
            v-model="adminPwd"
            type="password"
            class="admin-input"
            placeholder="비밀번호 입력"
            autocomplete="current-password"
          />
        </div>

        <p v-if="errorMsg" class="admin-login-error">{{ errorMsg }}</p>

        <button type="submit" class="admin-login-btn" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--ink-50, #f8f9fa);
}

.admin-login-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  padding: 40px 36px;
  width: 100%;
  max-width: 380px;
}

.admin-login-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
}

.brand-logo-mini {
  display: flex;
  gap: 3px;
}

.brand-logo-mini .d {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--green-500, #308860);
}

.admin-login-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-900, #111827);
}

.admin-login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.admin-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-700, #374151);
}

.admin-input {
  border: 1px solid var(--ink-200, #e5e7eb);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s;
}

.admin-input:focus {
  border-color: var(--green-500, #308860);
}

.admin-login-error {
  font-size: 13px;
  color: #ef4444;
  margin: 0;
}

.admin-login-btn {
  margin-top: 4px;
  background: var(--green-500, #308860);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.admin-login-btn:hover:not(:disabled) {
  background: var(--green-600, #276b4c);
}

.admin-login-btn:disabled {
  opacity: 0.6;
  cursor: default;
}
</style>

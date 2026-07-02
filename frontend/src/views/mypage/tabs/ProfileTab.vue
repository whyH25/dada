<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from '../../../utils/toast.js'
import { useAuthStore } from '../../../stores/auth.js'
import { updateUserApi, deleteUserApi, verifyPasswordApi } from '../../../api/authApi.js'

const router = useRouter()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const isEditing = ref(false)
const editName = ref('')
const editPhone = ref('')
const editPwd = ref('')
const editPwdConfirm = ref('')
const editError = ref('')
const editLoading = ref(false)

const isVerifying = ref(false)
const verifyPwd = ref('')
const verifyError = ref('')
const verifyLoading = ref(false)

const showWithdrawConfirm = ref(false)
const withdrawLoading = ref(false)

function startEdit() {
  if (!user.value?.hasPassword) {
    editName.value = user.value?.userName || ''
    editPhone.value = user.value?.userPhone || ''
    editPwd.value = ''
    editPwdConfirm.value = ''
    editError.value = ''
    isEditing.value = true
    return
  }
  verifyPwd.value = ''
  verifyError.value = ''
  isVerifying.value = true
}

function cancelVerify() {
  isVerifying.value = false
  verifyPwd.value = ''
  verifyError.value = ''
}

async function confirmPassword() {
  verifyError.value = ''
  if (!verifyPwd.value) { verifyError.value = '비밀번호를 입력해주세요.'; return }
  verifyLoading.value = true
  try {
    await verifyPasswordApi(verifyPwd.value)
    isVerifying.value = false
    editName.value = user.value?.userName || ''
    editPhone.value = user.value?.userPhone || ''
    editPwd.value = ''
    editPwdConfirm.value = ''
    editError.value = ''
    isEditing.value = true
  } catch (e) {
    verifyError.value = e.message
  } finally {
    verifyLoading.value = false
  }
}

async function submitEdit() {
  editError.value = ''
  if (!editName.value.trim()) { editError.value = '이름을 입력해주세요.'; return }
  if (editPwd.value && editPwd.value.length < 8) { editError.value = '비밀번호는 8자 이상이어야 합니다.'; return }
  if (editPwd.value && editPwd.value !== editPwdConfirm.value) { editError.value = '비밀번호가 일치하지 않습니다.'; return }
  editLoading.value = true
  try {
    const payload = { userName: editName.value.trim(), userPhone: editPhone.value.trim() }
    if (editPwd.value) payload.userPwd = editPwd.value
    const res = await updateUserApi(payload)
    authStore.user = res.data
    isEditing.value = false
    toast('회원정보가 수정되었습니다.')
  } catch (e) {
    editError.value = e.message
  } finally {
    editLoading.value = false
  }
}

async function withdrawUser() {
  withdrawLoading.value = true
  try {
    await deleteUserApi()
    authStore.user = null
    authStore.isLoggedIn = false
    showWithdrawConfirm.value = false
    router.push('/')
  } catch (e) {
    toast(e.message)
  } finally {
    withdrawLoading.value = false
  }
}
</script>

<template>
  <div>
    <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 회원정보</div>
    <h2 class="mp-h1" style="margin:4px 0 20px;">회원정보</h2>
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">기본 정보</h3>
        <button v-if="!isEditing && !isVerifying" class="btn btn-sm btn-secondary" @click="startEdit">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z" /></svg>
          정보 수정
        </button>
        <button v-else-if="isEditing" class="btn btn-sm btn-primary" :disabled="editLoading" @click="submitEdit">
          {{ editLoading ? '저장 중...' : '완료' }}
        </button>
      </div>

      <div v-if="isVerifying" style="margin:0 20px 16px; padding:14px 16px; background:var(--ink-50); border-radius:8px;">
        <p style="font-size:13px; color:var(--ink-600); margin-bottom:8px;">정보를 수정하려면 현재 비밀번호를 입력해주세요.</p>
        <div style="display:flex; align-items:center; gap:8px;">
          <input class="input input-inline" type="password" v-model="verifyPwd" placeholder="현재 비밀번호" style="flex:1;" @keyup.enter="confirmPassword" autocomplete="current-password" />
          <button class="btn btn-sm btn-primary" :disabled="verifyLoading" @click="confirmPassword">{{ verifyLoading ? '확인 중...' : '확인' }}</button>
          <button class="btn btn-sm btn-ghost" @click="cancelVerify">취소</button>
        </div>
        <p v-if="verifyError" class="auth-error" style="margin-top:8px;">{{ verifyError }}</p>
      </div>

      <div class="info-grid">
        <div class="info-row"><div class="info-label">아이디</div><div class="info-val">{{ user?.userEmail }}</div></div>
        <div class="info-row">
          <div class="info-label">이름</div>
          <div class="info-val">
            <input v-if="isEditing" class="input input-inline" v-model="editName" placeholder="이름" />
            <span v-else>{{ user?.userName }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-label">휴대폰 번호</div>
          <div class="info-val">
            <input v-if="isEditing" class="input input-inline" v-model="editPhone" placeholder="휴대폰 번호 (예: 010-1234-5678)" />
            <span v-else>{{ user?.userPhone || '-' }}</span>
          </div>
        </div>
        <div v-if="user?.hasPassword" class="info-row">
          <div class="info-label">비밀번호</div>
          <div class="info-val">
            <template v-if="isEditing">
              <input class="input input-inline" type="password" v-model="editPwd" placeholder="새 비밀번호 (변경 시 입력)" style="margin-bottom:6px;" autocomplete="new-password" />
              <div style="display:flex; align-items:center; gap:8px;">
                <input class="input input-inline" type="password" v-model="editPwdConfirm" placeholder="비밀번호 확인" autocomplete="new-password" />
                <span v-if="editPwd && editPwdConfirm && editPwd !== editPwdConfirm" style="color:#e53e3e; font-size:12px; white-space:nowrap;">비밀번호가 일치하지 않습니다.</span>
              </div>
            </template>
            <span v-else>••••••••</span>
          </div>
        </div>
        <div v-else class="info-row">
          <div class="info-label">로그인 방식</div>
          <div class="info-val"><span class="text-muted">소셜 로그인 계정 (비밀번호 없음)</span></div>
        </div>
        <div class="info-row"><div class="info-label">가입일</div><div class="info-val">{{ user?.createdAt || '-' }}</div></div>
      </div>
      <p v-if="editError" class="auth-error" style="margin-top:8px;">{{ editError }}</p>
      <div style="margin-top:16px; padding-top:14px; border-top:1px solid var(--ink-150); text-align:right;">
        <button class="btn-withdraw" @click="showWithdrawConfirm = true">회원탈퇴</button>
      </div>
    </div>

    <div v-if="showWithdrawConfirm" class="withdraw-overlay" @click.self="showWithdrawConfirm = false">
      <div class="withdraw-modal">
        <h3 class="withdraw-title">정말 탈퇴하시겠습니까?</h3>
        <p class="withdraw-desc">탈퇴 시 모든 면접 기록과 리포트가 삭제되며<br>복구할 수 없습니다.</p>
        <div class="withdraw-actions">
          <button class="btn btn-secondary" @click="showWithdrawConfirm = false">취소</button>
          <button class="btn btn-danger" :disabled="withdrawLoading" @click="withdrawUser">
            {{ withdrawLoading ? '처리 중...' : '탈퇴하기' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

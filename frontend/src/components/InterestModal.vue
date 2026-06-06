<script setup>
import { ref, watch } from 'vue'
import { useAuthStore } from '../stores/auth.js'
import { useDataStore } from '../stores/data.js'

const auth = useAuthStore()
const data = useDataStore()
const draft = ref(new Set())

// 모달 열릴 때 현재 관심기업을 draft로 복사 (원본 openInterest)
watch(() => auth.interestOpen, (open) => {
  if (open) draft.value = new Set(data.interest)
})

function toggle(name) {
  const d = new Set(draft.value)
  if (d.has(name)) d.delete(name)
  else d.add(name)
  draft.value = d
}
function save() {
  data.saveInterest(draft.value)
  auth.closeInterest()
}
</script>

<template>
  <div class="auth-overlay" :class="{ open: auth.interestOpen }" @click.self="auth.closeInterest()">
    <div class="auth-modal interest-modal" role="dialog" aria-modal="true">
      <button class="auth-close" @click="auth.closeInterest()" aria-label="닫기">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12" /></svg>
      </button>
      <h2 class="auth-title">관심 기업 설정</h2>
      <p class="auth-sub">관심 기업을 선택하면 새 합격 스토리·채용 공고·서류 마감일 알림을 받아볼 수 있어요.</p>
      <div class="interest-grid">
        <button
          v-for="(c, i) in data.allCompanies"
          :key="i"
          type="button"
          class="interest-chip"
          :class="{ on: draft.has(c.name) }"
          @click="toggle(c.name)"
        >
          <span class="company-logo" :class="c.logo" style="width:26px;height:26px;font-size:11px;">{{ c.short }}</span>{{ c.name }}
          <span class="interest-check">{{ draft.has(c.name) ? '✓' : '+' }}</span>
        </button>
      </div>
      <button class="btn btn-primary btn-block btn-lg" style="margin-top:18px;" @click="save">저장하기</button>
    </div>
  </div>
</template>

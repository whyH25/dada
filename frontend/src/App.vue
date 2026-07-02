<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import LoginModal from './components/LoginModal.vue'
import InterestModal from './components/InterestModal.vue'
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'

const router = useRouter()
const route = useRoute()

const NO_CHROME_ROUTES = ['prep', 'room', 'saving', 'done']
watch(() => route.name, (name) => {
  document.body.style.overflow = NO_CHROME_ROUTES.includes(name) ? 'hidden' : ''
}, { immediate: true })

const showNewUserPopup = ref(false)

watch(() => route.query.newSocialUser, (val) => {
  if (val) {
    showNewUserPopup.value = true
    router.replace({ path: route.path, query: {} })
  }
}, { immediate: true })
</script>

<template>
  <AppHeader />

  <router-view v-slot="{ Component }">
    <component :is="Component" />
  </router-view>

  <AppFooter />

  <LoginModal />
  <InterestModal />

  <div v-if="showNewUserPopup" class="new-user-overlay" @click.self="showNewUserPopup = false">
    <div class="new-user-modal">
      <div class="new-user-icon">🎉</div>
      <h3 class="new-user-title">회원가입 완료!</h3>
      <p class="new-user-desc">가입을 환영해요!<br>무료 이용권 <strong>1장</strong>이 지급되었어요.</p>
      <p class="new-user-sub">지금 바로 AI 모의 면접을 체험해보세요.</p>
      <button class="btn btn-primary btn-block" @click="showNewUserPopup = false; router.push('/')">확인</button>
    </div>
  </div>
</template>

<style scoped>
.new-user-overlay {
  position: fixed; inset: 0; background: rgba(10,22,16,0.55);
  display: flex; align-items: center; justify-content: center; z-index: 9999;
}
.new-user-modal {
  background: #fff; border-radius: 16px; padding: 40px 32px 32px;
  width: 340px; max-width: 90vw; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}
.new-user-icon { font-size: 40px; margin-bottom: 12px; }
.new-user-title { font-size: 20px; font-weight: 800; color: var(--ink-900); margin: 0 0 12px; }
.new-user-desc { font-size: 14px; color: var(--ink-600); line-height: 1.7; margin: 0 0 8px; }
.new-user-desc strong { color: var(--green-500); }
.new-user-sub { font-size: 13px; color: var(--ink-400); margin: 0 0 24px; }
</style>

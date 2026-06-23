<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth.js'
import LoginModal from './components/LoginModal.vue'
import InterestModal from './components/InterestModal.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

// /prep, /saving: 처리 중 화면이라 네비게이션·푸터 숨김 + 스크롤 차단
const NO_CHROME_ROUTES = ['prep', 'saving']
watch(() => route.name, (name) => {
  document.body.style.overflow = NO_CHROME_ROUTES.includes(name) ? 'hidden' : ''
}, { immediate: true })

function go(path) { router.push(path) }

const showNewUserPopup = ref(false)

const profileOpen = ref(false)
const profileRef = ref(null)

function toggleProfile() {
  profileOpen.value = !profileOpen.value
}

function handleOutsideClick(e) {
  if (profileRef.value && !profileRef.value.contains(e.target)) {
    profileOpen.value = false
  }
}

watch(() => route.query.newSocialUser, (val) => {
  if (val) {
    showNewUserPopup.value = true
    router.replace({ path: route.path, query: {} })
  }
}, { immediate: true })

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})
onUnmounted(() => document.removeEventListener('click', handleOutsideClick))
</script>

<template>
  <!-- ============ TOP NAVIGATION ============ -->
  <nav class="nav" v-if="!$route.path.startsWith('/admin') && !NO_CHROME_ROUTES.includes($route.name)">
    <div class="nav-inner">
      <div class="brand" @click="go('/')">
        <div class="brand-logo"><span class="d"></span><span class="d"></span><span class="d"></span></div>
        <div class="brand-name">다다</div>
      </div>

      <div class="nav-menu">
        <div class="nav-item" :class="{ active: $route.name === 'schedule' }" @click="go('/schedule')">채용 일정</div>
        <div class="nav-item" :class="{ active: ['create', 'interview-intro'].includes($route.name) }" @click="go('/interview-intro')">면접</div>
        <div class="nav-item" :class="{ active: $route.name === 'stories' }" @click="go('/stories')">합격스토리</div>
        <div class="nav-item" :class="{ active: ['board', 'post-create', 'post-edit', 'post-detail'].includes($route.name) }" @click="go('/community/board')">커뮤니티</div>
        <!-- <div class="nav-item" :class="{ active: $route.name === 'openchat' }" @click="go('/openchat')">기업별 오픈톡</div> -->
        <div class="nav-item" :class="{ active: $route.name === 'notices' }" @click="go('/notices')">공지사항</div>
      </div>

      <div class="nav-right">
<div class="nav-auth" v-if="!auth.isLoggedIn">
          <button class="nav-login-btn" @click="auth.openLogin()">로그인 / 회원가입</button>
        </div>
        <div class="nav-profile has-dropdown" v-else ref="profileRef" @click.stop="toggleProfile">
          <div class="avatar">{{ auth.user?.userName?.charAt(0) }}</div>
          <div class="nav-profile-name">{{ auth.user?.userName }}</div>
          <div class="nav-dropdown" v-if="profileOpen">
            <div class="nav-dropdown-item" @click="go('/mypage')">마이페이지</div>
            <div class="nav-dropdown-item" @click="auth.logout(router)">로그아웃</div>
          </div>
        </div>
      </div>
    </div>
  </nav>

  <!-- 라우팅된 화면이 여기 렌더됩니다 (원본의 #page-* 들) -->
  <router-view v-slot="{ Component }">
    <component :is="Component" />
  </router-view>

  <!-- 푸터 -->
  <footer class="footer" v-if="!['signup', 'find-password'].includes($route.name) && !$route.path.startsWith('/admin') && !NO_CHROME_ROUTES.includes($route.name)">
    <div class="footer-inner">
      <div>© 2026 다대다. All rights reserved.</div>
      <div class="footer-links">
        <a style="cursor:pointer" @click="go('/faq')">자주 묻는 질문 (FAQ)</a>
      </div>
    </div>
  </footer>

  <!-- 모달들 -->
  <LoginModal />
  <InterestModal />

  <!-- 소셜 신규 가입 완료 팝업 -->
  <div v-if="showNewUserPopup" class="new-user-overlay" @click.self="showNewUserPopup = false">
    <div class="new-user-modal">
      <div class="new-user-icon">🎉</div>
      <h3 class="new-user-title">회원가입 완료!</h3>
      <p class="new-user-desc">가입을 환영해요!<br>무료 이용권 <strong>1장</strong>이 지급되었어요.</p>
      <p class="new-user-sub">지금 바로 AI 모의 면접을 체험해보세요.</p>
      <button class="btn btn-primary btn-block" @click="showNewUserPopup = false; go('/')">확인</button>
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

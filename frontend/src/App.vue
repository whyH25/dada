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

function requireAuth(action) {
  if (auth.isLoggedIn) {
    if (typeof action === 'function') action()
    else router.push('/' + action)
  } else {
    auth.openLogin(action)
  }
}
function go(path) { router.push(path) }

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
        <div class="brand-name">다대다</div>
      </div>

      <div class="nav-menu">
        <div class="nav-item" :class="{ active: $route.name === 'home' }" @click="go('/')">홈</div>
        <div class="nav-item" :class="{ active: $route.name === 'schedule' }" @click="go('/schedule')">채용 일정</div>
        <div class="nav-item" :class="{ active: ['create', 'interview-intro'].includes($route.name) }" @click="go('/interview-intro')">면접</div>
        <div class="nav-item" :class="{ active: $route.name === 'stories' }" @click="go('/stories')">합격스토리</div>
        <div class="nav-item has-dropdown" :class="{ active: ['openchat', 'board', 'post-create', 'post-edit', 'post-detail'].includes($route.name) }">
          커뮤니티
          <span class="caret"></span>
          <div class="nav-dropdown">
            <div class="nav-dropdown-item" @click="go('/community/openchat')">기업별 오픈톡</div>
            <div class="nav-dropdown-item" @click="go('/community/board')">자유게시판</div>
          </div>
        </div>
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
    </div>
  </footer>

  <!-- 모달들 -->
  <LoginModal />
  <InterestModal />
</template>

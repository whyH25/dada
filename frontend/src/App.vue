<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth.js'
import LoginModal from './components/LoginModal.vue'
import InterestModal from './components/InterestModal.vue'

const router = useRouter()
const auth = useAuthStore()

function requireAuth(action) {
  if (auth.isLoggedIn) {
    if (typeof action === 'function') action()
    else router.push('/' + action)
  } else {
    auth.openLogin(action)
  }
}
function go(path) { router.push(path) }
function globalSearch(q) {
  router.push({ path: '/rooms', query: { q: (q || '').trim() } })
}

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
  <nav class="nav" v-if="!$route.path.startsWith('/admin')">
    <div class="nav-inner">
      <div class="brand" @click="go('/')">
        <div class="brand-logo"><span class="d"></span><span class="d"></span><span class="d"></span></div>
        <div class="brand-name">다대다</div>
        <div class="brand-sub">BETA</div>
      </div>

      <div class="nav-menu">
        <div class="nav-item" :class="{ active: $route.name === 'home' }" @click="go('/')">홈</div>
        <div class="nav-item" :class="{ active: $route.name === 'schedule' }" @click="go('/schedule')">채용 일정</div>
        <div class="nav-item has-dropdown" :class="{ active: ['rooms', 'create'].includes($route.name) }">
          면접
          <span class="caret"></span>
          <div class="nav-dropdown">
            <div class="nav-dropdown-item" @click="go('/rooms')">100대기업 면접방</div>
            <div class="nav-dropdown-item" @click="requireAuth('create')">맞춤형 면접방 생성</div>
          </div>
        </div>
        <div class="nav-item" :class="{ active: $route.name === 'stories' }" @click="go('/stories')">합격스토리</div>
        <div class="nav-item" :class="{ active: $route.name === 'community' }" @click="go('/community')">커뮤니티</div>
        <div class="nav-item" :class="{ active: $route.name === 'notices' }" @click="go('/notices')">공지사항</div>
      </div>

      <div class="nav-right">
        <div class="nav-search">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" /></svg>
          <input placeholder="기업명, 직무 검색" @keydown.enter="globalSearch($event.target.value)" />
        </div>
        <div class="nav-icon-btn" title="알림" @click="go('/notifications')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" /><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" /></svg>
          <span class="dot"></span>
        </div>
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
  <footer class="footer" v-if="$route.name !== 'signup' && !$route.path.startsWith('/admin')">
    <div class="footer-inner">
      <div>© 2026 다대다. All rights reserved.</div>
      <div class="footer-links">
        <a>이용약관</a>
        <a>개인정보처리방침</a>
        <a>고객센터</a>
      </div>
    </div>
  </footer>

  <!-- 모달들 -->
  <LoginModal />
  <InterestModal />
</template>

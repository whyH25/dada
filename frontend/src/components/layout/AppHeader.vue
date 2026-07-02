<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'
import NavigationBar from './NavigationBar.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const NO_CHROME_ROUTES = ['prep', 'room', 'saving', 'done']

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

onMounted(() => document.addEventListener('click', handleOutsideClick))
onUnmounted(() => document.removeEventListener('click', handleOutsideClick))
</script>

<template>
  <nav class="nav" v-if="!route.path.startsWith('/admin') && !NO_CHROME_ROUTES.includes(route.name)">
    <div class="nav-inner">
      <div class="brand" @click="router.push('/')">
        <div class="brand-logo"><span class="d"></span><span class="d"></span><span class="d"></span></div>
        <div class="brand-name">다다</div>
      </div>

      <NavigationBar />

      <div class="nav-right">
        <div class="nav-auth" v-if="!auth.isLoggedIn">
          <button class="nav-login-btn" @click="auth.openLogin()">로그인 / 회원가입</button>
        </div>
        <div class="nav-profile has-dropdown" v-else ref="profileRef" @click.stop="toggleProfile">
          <div class="avatar">{{ auth.user?.userName?.charAt(0) }}</div>
          <div class="nav-profile-name">{{ auth.user?.userName }}</div>
          <div class="nav-dropdown" v-if="profileOpen">
            <div class="nav-dropdown-item" @click="router.push('/mypage')">마이페이지</div>
            <div class="nav-dropdown-item" @click="auth.logout(router)">로그아웃</div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

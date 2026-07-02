<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const NO_CHROME_ROUTES = ['prep', 'room', 'saving', 'done']
</script>

<template>
  <footer class="footer" v-if="!['signup', 'find-password'].includes(route.name) && !route.path.startsWith('/admin') && !NO_CHROME_ROUTES.includes(route.name)">
    <div class="footer-inner">
      <div>© 2026 다대다. All rights reserved.</div>
      <div class="footer-links">
        <a style="cursor:pointer" @click="router.push('/faq')">자주 묻는 질문 (FAQ)</a>
      </div>
    </div>
  </footer>
</template>

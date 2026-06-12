import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth.js'
import { useAdminAuthStore } from './stores/adminAuth.js'

// 원본 CSS 그대로 사용 (디자인 100% 유지)
import './assets/styles.css'
import './assets/home.css'
import './assets/screens.css'
import './assets/interview.css'
import './assets/extra.css'
import './assets/mypage.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const auth = useAuthStore()
const adminAuth = useAdminAuthStore()
await Promise.all([auth.restoreSession(), adminAuth.restoreSession()])

app.mount('#app')

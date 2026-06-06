import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 원본 CSS 그대로 사용 (디자인 100% 유지)
import './assets/styles.css'
import './assets/home.css'
import './assets/screens.css'
import './assets/interview.css'
import './assets/extra.css'
import './assets/mypage.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

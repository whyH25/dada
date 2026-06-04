// import { createRouter, createWebHistory } from 'vue-router'

// const router = createRouter({
//   history: createWebHistory(import.meta.env.BASE_URL),
//   routes: [],
// })

// export default router

// import { createRouter, createWebHistory } from 'vue-router'
// import TestView from '@/views/TestView.vue'

// const router = createRouter({
//   history: createWebHistory(import.meta.env.BASE_URL),
//   routes: [
//     {
//       path: '/test',
//       name: 'test',
//       component: TestView,
//     },
//   ],
// })

// export default router

import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

// 로그인 후 이용 가능한 경로 (원본 GATED_ROUTES)
const GATED = ['create', 'interview', 'room-intro', 'mypage']

const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/schedule', name: 'schedule', component: () => import('../views/ScheduleView.vue') },
  { path: '/create', name: 'create', component: () => import('../views/CreateView.vue') },
  { path: '/rooms', name: 'rooms', component: () => import('../views/RoomsView.vue') },
  { path: '/room-intro/:id', name: 'room-intro', component: () => import('../views/RoomIntroView.vue') },
  { path: '/interview', name: 'interview', component: () => import('../views/InterviewView.vue') },
  { path: '/prep', name: 'prep', component: () => import('../views/PrepView.vue') },
  { path: '/saving', name: 'saving', component: () => import('../views/SavingView.vue') },
  { path: '/done', name: 'done', component: () => import('../views/DoneView.vue') },
  { path: '/stories', name: 'stories', component: () => import('../views/StoriesView.vue') },
  { path: '/story/:id', name: 'story', component: () => import('../views/StoryView.vue') },
  { path: '/community', name: 'community', component: () => import('../views/CommunityView.vue') },
  { path: '/post/:id', name: 'post', component: () => import('../views/PostView.vue') },
  { path: '/study/:id', name: 'study-detail', component: () => import('../views/StudyDetailView.vue') },
  { path: '/chatroom/:id', name: 'chatroom', component: () => import('../views/ChatroomView.vue') },
  { path: '/notices', name: 'notices', component: () => import('../views/NoticesView.vue') },
  { path: '/notifications', name: 'notifications', component: () => import('../views/NotificationsView.vue') },
  { path: '/mypage', name: 'mypage', component: () => import('../views/MypageView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// 원본 goto()의 게이팅 로직: 비로그인 상태로 보호 경로 진입 시 로그인 모달
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn && GATED.includes(to.name)) {
    auth.openLogin(to.fullPath) // 로그인 후 이 경로로 이동
    next(from.name ? false : '/')
  } else {
    next()
  }
})

export default router


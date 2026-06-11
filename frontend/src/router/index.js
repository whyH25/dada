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

const GATED = ['create', 'interview', 'room-intro', 'mypage']
const ADMIN_ROUTES = ['admin', 'admin-jobs', 'admin-users']

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
  { path: '/signup', name: 'signup', component: () => import('../views/SignupView.vue') },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/admin/AdminView.vue'),
    children: [
      { path: '', redirect: '/admin/jobs' },
      { path: 'jobs', name: 'admin-jobs', component: () => import('../views/admin/AdminJobsView.vue') },
      { path: 'users', name: 'admin-users', component: () => import('../views/admin/AdminUsersView.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  if (ADMIN_ROUTES.includes(to.name)) {
    if (!auth.isLoggedIn) {
      auth.openLogin(to.fullPath)
      next(from.name ? false : '/')
    } else if (auth.user?.userRole !== 'ADMIN') {
      next('/')  // 로그인했지만 ADMIN 아님 → 홈
    } else {
      next()
    }
  } else if (!auth.isLoggedIn && GATED.includes(to.name)) {
    auth.openLogin(to.fullPath)
    next(from.name ? false : '/')
  } else {
    next()
  }
})

export default router


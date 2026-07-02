import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useAdminAuthStore } from '../stores/adminAuth.js'

const GATED = ['create', 'prep', 'room', 'saving', 'done', 'mypage', 'post-create', 'post-edit']
const ADMIN_GATED = ['admin', 'admin-jobs', 'admin-users', 'admin-stories', 'admin-notices', 'admin-posts']

const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/schedule', name: 'schedule', component: () => import('../views/jobschedule/ScheduleView.vue') },
  { path: '/interview/intro', name: 'interview-intro', component: () => import('../views/interview/IntroView.vue') },
  { path: '/interview/create', name: 'create', component: () => import('../views/interview/CreateView.vue') },
  { path: '/interview/:roomId/prep', name: 'prep', component: () => import('../views/interview/PrepView.vue') },
  { path: '/interview/:roomId', name: 'room', component: () => import('../views/interview/RoomView.vue') },
  { path: '/interview/:roomId/saving', name: 'saving', component: () => import('../views/interview/SavingView.vue') },
  { path: '/interview/:roomId/done', name: 'done', component: () => import('../views/interview/DoneView.vue') },
  { path: '/stories', name: 'stories', component: () => import('../views/story/StoriesView.vue') },
  { path: '/stories/:id', name: 'story', component: () => import('../views/story/StoryView.vue') },
  // 커뮤니티 (글쓰기/상세는 layout 바깥에 별도 배치)
  { path: '/community/posts/new',              name: 'post-create', component: () => import('../views/community/PostFormView.vue') },
  { path: '/community/posts/:postId/edit',     name: 'post-edit',   component: () => import('../views/community/PostFormView.vue') },
  { path: '/community/posts/:postId',          name: 'post-detail', component: () => import('../views/community/PostDetailView.vue') },
  { path: '/openchat', name: 'openchat', component: () => import('../views/chat/OpenChatView.vue') },
  {
    path: '/community',
    component: () => import('../views/community/CommunityView.vue'),
    children: [
      { path: '', redirect: '/community/posts' },
      { path: 'posts', name: 'posts', component: () => import('../views/community/PostListView.vue') },
    ],
  },
  { path: '/chatroom/:id', name: 'chatroom', component: () => import('../views/chat/ChatroomView.vue') },
  { path: '/notices', name: 'notices', component: () => import('../views/notice/NoticesView.vue') },
  { path: '/mypage', name: 'mypage', component: () => import('../views/mypage/MypageView.vue') },
  { path: '/payment/success', name: 'payment-success', component: () => import('../views/payment/PaymentSuccessView.vue') },
  { path: '/payment/fail',    name: 'payment-fail',    component: () => import('../views/payment/PaymentFailView.vue') },
  { path: '/faq', name: 'faq', component: () => import('../views/FaqView.vue') },
  { path: '/auth/signup', name: 'signup', component: () => import('../views/auth/SignupView.vue') },
  { path: '/auth/find-password', name: 'find-password', component: () => import('../views/auth/FindPasswordView.vue') },
  { path: '/notices/:id', name: 'notice', component: () => import('../views/notice/NoticeView.vue') },

  // 관리자 로그인 (독립 페이지 - 일반 nav 없음)
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/admin/AdminLoginView.vue') },

  // 관리자 대시보드 (사이드바 레이아웃)
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/admin/AdminView.vue'),
    children: [
      { path: '', redirect: '/admin/jobs' },
      { path: 'jobs', name: 'admin-jobs', component: () => import('../views/admin/AdminJobsView.vue') },
      { path: 'stories', name: 'admin-stories', component: () => import('../views/admin/AdminStoriesView.vue') },
      { path: 'notices', name: 'admin-notices', component: () => import('../views/admin/AdminNoticesView.vue') },
      { path: 'posts',   name: 'admin-posts',   component: () => import('../views/admin/AdminPostsView.vue') },
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

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()
  const adminAuth = useAdminAuthStore()

  // 새로고침 등 첫 진입 시 세션 복원이 끝나야 로그인 여부를 정확히 판단할 수 있음
  await Promise.all([auth.restoreSession(), adminAuth.restoreSession()])

  if (to.name === 'admin-login') {
    // 이미 관리자 로그인 상태면 대시보드로 redirect
    if (adminAuth.isAdminLoggedIn) return next('/admin/jobs')
    return next()
  }

  if (ADMIN_GATED.includes(to.name)) {
    if (!adminAuth.isAdminLoggedIn) return next('/admin/login')
    return next()
  }

  if (!auth.isLoggedIn && GATED.includes(to.name)) {
    auth.openLogin(to.fullPath)
    return next(from.name ? false : '/')
  }

  next()
})

export default router

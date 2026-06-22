import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useAdminAuthStore } from '../stores/adminAuth.js'

const GATED = ['create', 'interview', 'room-intro', 'mypage', 'post-create', 'post-edit']
const ADMIN_GATED = ['admin', 'admin-jobs', 'admin-users', 'admin-stories', 'admin-notices', 'admin-posts']

const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/schedule', name: 'schedule', component: () => import('../views/ScheduleView.vue') },
  { path: '/create', name: 'create', component: () => import('../views/CreateView.vue') },
  { path: '/interview', name: 'interview', component: () => import('../views/InterviewView.vue') },
  { path: '/prep', name: 'prep', component: () => import('../views/PrepView.vue') },
  { path: '/saving', name: 'saving', component: () => import('../views/SavingView.vue') },
  { path: '/done', name: 'done', component: () => import('../views/DoneView.vue') },
  { path: '/interview-intro', name: 'interview-intro', component: () => import('../views/InterviewIntroView.vue') },
  { path: '/stories', name: 'stories', component: () => import('../views/StoriesView.vue') },
  { path: '/story/:id', name: 'story', component: () => import('../views/StoryView.vue') },
  // 커뮤니티 (글쓰기/상세는 layout 바깥에 별도 배치)
  { path: '/community/board/new',       name: 'post-create', component: () => import('../views/PostCreateView.vue') },
  { path: '/community/board/:id/edit',  name: 'post-edit',   component: () => import('../views/PostCreateView.vue') },
  { path: '/community/board/:id',       name: 'post-detail', component: () => import('../views/PostDetailView.vue') },
  { path: '/openchat', name: 'openchat', component: () => import('../views/OpenChatView.vue') },
  {
    path: '/community',
    component: () => import('../views/CommunityView.vue'),
    children: [
      { path: '', redirect: '/community/board' },
      { path: 'board', name: 'board', component: () => import('../views/PostsView.vue') },
    ],
  },
  { path: '/study/:id', name: 'study-detail', component: () => import('../views/StudyDetailView.vue') },
  { path: '/chatroom/:id', name: 'chatroom', component: () => import('../views/ChatroomView.vue') },
  { path: '/notices', name: 'notices', component: () => import('../views/NoticesView.vue') },
  { path: '/notifications', name: 'notifications', component: () => import('../views/NotificationsView.vue') },
  { path: '/mypage', name: 'mypage', component: () => import('../views/MypageView.vue') },
  { path: '/payment/success', name: 'payment-success', component: () => import('../views/PaymentSuccessView.vue') },
  { path: '/payment/fail',    name: 'payment-fail',    component: () => import('../views/PaymentFailView.vue') },
  { path: '/signup', name: 'signup', component: () => import('../views/SignupView.vue') },
  { path: '/find-password', name: 'find-password', component: () => import('../views/FindPasswordView.vue') },
  { path: '/notices/:id', name: 'notice', component: () => import('../views/NoticeView.vue') },

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

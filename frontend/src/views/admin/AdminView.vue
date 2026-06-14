<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAdminAuthStore } from '../../stores/adminAuth.js'

const router = useRouter()
const route = useRoute()
const adminAuth = useAdminAuthStore()

const topMenus = [
  { name: 'admin-jobs',    label: '채용일정 관리', path: '/admin/jobs' },
  { name: 'admin-stories', label: '합격스토리 관리', path: '/admin/stories' },
  { name: 'admin-notices', label: '공지사항 관리', path: '/admin/notices' },
]

const communityMenus = [
  { name: 'admin-posts', label: '게시글 관리', path: '/admin/posts' },
]

const bottomMenus = [
  { name: 'admin-users', label: '사용자 관리', path: '/admin/users' },
]
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-sidebar-header">
        <div class="admin-sidebar-title">관리자</div>
        <div class="admin-sidebar-name">{{ adminAuth.admin?.adminName }}</div>
      </div>
      <nav class="admin-nav">
        <button
          v-for="m in topMenus" :key="m.name"
          class="admin-nav-item" :class="{ active: route.name === m.name }"
          @click="router.push(m.path)"
        >{{ m.label }}</button>

        <div class="admin-nav-section-label">커뮤니티</div>

        <button
          v-for="m in communityMenus" :key="m.name"
          class="admin-nav-item" :class="{ active: route.name === m.name }"
          @click="router.push(m.path)"
        >{{ m.label }}</button>

        <button
          v-for="m in bottomMenus" :key="m.name"
          class="admin-nav-item" :class="{ active: route.name === m.name }"
          @click="router.push(m.path)"
        >{{ m.label }}</button>
      </nav>
      <div class="admin-sidebar-footer">
        <button class="admin-logout-btn" @click="adminAuth.logout(router)">로그아웃</button>
      </div>
    </aside>
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: var(--surface-2, #f8f9fa);
  border-right: 1px solid var(--border, #e5e7eb);
  display: flex;
  flex-direction: column;
}

.admin-sidebar-header {
  padding: 24px 20px 16px;
  border-bottom: 1px solid var(--ink-150, #e5e7eb);
}

.admin-sidebar-title {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--text-3, #9ca3af);
  text-transform: uppercase;
  margin-bottom: 4px;
}

.admin-sidebar-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-800, #1f2937);
}

.admin-nav {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  flex: 1;
  overflow-y: auto;
}

.admin-nav-section-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-3, #9ca3af);
  padding: 14px 20px 4px;
}

.admin-nav-item {
  text-align: left;
  padding: 10px 20px;
  font-size: 14px;
  color: var(--text-2, #374151);
  background: none;
  border: none;
  border-left: 3px solid transparent;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.admin-nav-item:hover {
  background: var(--surface-3, #f1f3f5);
}

.admin-nav-item.active {
  border-left-color: var(--green-500, #308860);
  color: var(--green-500, #308860);
  font-weight: 600;
  background: var(--surface-3, #f1f3f5);
}

.admin-sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--ink-150, #e5e7eb);
}

.admin-logout-btn {
  width: 100%;
  padding: 8px 0;
  font-size: 13px;
  color: var(--ink-500, #6b7280);
  background: none;
  border: 1px solid var(--ink-200, #e5e7eb);
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.admin-logout-btn:hover {
  background: #fef2f2;
  color: #ef4444;
  border-color: #fecaca;
}

.admin-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}
</style>

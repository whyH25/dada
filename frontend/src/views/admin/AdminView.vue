<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const menus = [
  { name: 'admin-jobs', label: '채용일정 관리', path: '/admin/jobs' },
  { name: 'admin-users', label: '사용자 관리', path: '/admin/users' },
]
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-sidebar-title">관리자</div>
      <nav class="admin-nav">
        <button
          v-for="m in menus"
          :key="m.name"
          class="admin-nav-item"
          :class="{ active: route.name === m.name }"
          @click="router.push(m.path)"
        >
          {{ m.label }}
        </button>
      </nav>
    </aside>
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: calc(100vh - 60px);
}

.admin-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: var(--surface-2, #f8f9fa);
  border-right: 1px solid var(--border, #e5e7eb);
  padding: 24px 0;
}

.admin-sidebar-title {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--text-3, #9ca3af);
  text-transform: uppercase;
  padding: 0 20px 16px;
}

.admin-nav {
  display: flex;
  flex-direction: column;
}

.admin-nav-item {
  text-align: left;
  padding: 10px 20px;
  font-size: 14px;
  color: var(--text-2, #374151);
  background: none;
  border: none;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: background 0.15s, color 0.15s;
}

.admin-nav-item:hover {
  background: var(--surface-3, #f1f3f5);
}

.admin-nav-item.active {
  border-left-color: var(--primary, #6366f1);
  color: var(--primary, #6366f1);
  font-weight: 600;
  background: var(--surface-3, #f1f3f5);
}

.admin-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}
</style>

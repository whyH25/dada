<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, deleteUser } from '../../api/adminApi.js'
import { toast } from '../../utils/toast.js'

const users = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getUsers()
    users.value = res.data
  } catch (e) {
    toast('사용자 목록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

async function remove(u) {
  if (!confirm(`'${u.userName}(${u.userEmail})' 계정을 삭제하시겠습니까?\n이 작업은 소프트 삭제로 처리됩니다.`)) return
  try {
    await deleteUser(u.userId)
    toast('사용자가 삭제되었습니다.')
    load()
  } catch (e) {
    toast(e.message)
  }
}

const statusLabel = { ACTIVE: '활성', INACTIVE: '비활성', DELETED: '삭제됨' }
const statusClass = { ACTIVE: 'badge-active', INACTIVE: 'badge-inactive', DELETED: 'badge-deleted' }

onMounted(load)
</script>

<template>
  <div>
    <div class="admin-page-header">
      <h2 class="admin-page-title">사용자 관리</h2>
      <span class="admin-count">총 {{ users.length }}명</span>
    </div>

    <div v-if="loading" class="admin-empty">불러오는 중...</div>
    <div v-else-if="users.length === 0" class="admin-empty">등록된 사용자가 없습니다.</div>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>이름</th>
          <th>이메일</th>
          <th>전화번호</th>
          <th>상태</th>
          <th style="width:80px;"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.userId">
          <td class="admin-muted">{{ u.userId }}</td>
          <td>{{ u.userName }}</td>
          <td>{{ u.userEmail }}</td>
          <td>{{ u.userPhone || '-' }}</td>
          <td>
            <span class="user-badge" :class="statusClass[u.userStatus]">
              {{ statusLabel[u.userStatus] || u.userStatus }}
            </span>
          </td>
          <td>
            <button class="btn btn-danger btn-xs" @click="remove(u)">삭제</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.admin-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.admin-page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1, #111827);
}
.admin-count {
  font-size: 14px;
  color: var(--text-3, #9ca3af);
}
.admin-empty {
  color: var(--text-3, #9ca3af);
  padding: 40px 0;
  text-align: center;
}
.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.admin-table th {
  text-align: left;
  padding: 10px 12px;
  border-bottom: 2px solid var(--border, #e5e7eb);
  font-weight: 600;
  color: var(--text-2, #374151);
}
.admin-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  color: var(--text-1, #111827);
}
.admin-table tr:hover td {
  background: var(--surface-2, #f8f9fa);
}
.admin-muted {
  color: var(--text-3, #9ca3af);
}
.user-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 99px;
  font-size: 12px;
  font-weight: 500;
}
.badge-active {
  background: #dcfce7;
  color: #16a34a;
}
.badge-inactive {
  background: #fef9c3;
  color: #854d0e;
}
.badge-deleted {
  background: #fee2e2;
  color: #dc2626;
}
.btn-xs {
  padding: 4px 10px;
  font-size: 12px;
}
.btn-danger {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-danger:hover {
  background: #dc2626;
}
</style>

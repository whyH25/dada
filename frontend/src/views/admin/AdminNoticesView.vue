<script setup>
import { ref, onMounted } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const BASE = 'http://localhost:8080/api/admin'
const OPTS = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

const CATEGORIES = ['공지', '이벤트']

const notices = ref([])
const loading = ref(false)
const showModal = ref(false)
const editTarget = ref(null)
const form = ref(emptyForm())

function emptyForm() {
  return { category: '공지', title: '', content: '' }
}

async function load() {
  loading.value = true
  try {
    const res = await fetch(`${BASE}/notices`, OPTS)
    const data = await res.json()
    notices.value = data.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editTarget.value = null
  form.value = emptyForm()
  showModal.value = true
}

async function openEdit(n) {
  editTarget.value = n
  const res = await fetch(`${BASE}/notices/${n.noticeId}`, OPTS)
  const data = await res.json()
  const full = data.data || n
  form.value = { category: full.category, title: full.title, content: full.content || '' }
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function submit() {
  if (!form.value.title || !form.value.content || form.value.content === '<p><br></p>') {
    alert('제목과 내용은 필수입니다.'); return
  }
  const url = editTarget.value ? `${BASE}/notices/${editTarget.value.noticeId}` : `${BASE}/notices`
  const method = editTarget.value ? 'PUT' : 'POST'
  await fetch(url, { ...OPTS, method, body: JSON.stringify(form.value) })
  closeModal()
  load()
}

async function remove(n) {
  if (!confirm(`'${n.title}' 공지사항을 삭제하시겠습니까?`)) return
  await fetch(`${BASE}/notices/${n.noticeId}`, { ...OPTS, method: 'DELETE' })
  load()
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
}

onMounted(load)
</script>

<template>
  <div>
    <div class="admin-page-header">
      <h2 class="admin-page-title">공지사항 관리</h2>
      <button class="btn btn-primary btn-sm" @click="openCreate">+ 등록</button>
    </div>

    <div v-if="loading" class="admin-empty">불러오는 중...</div>
    <div v-else-if="notices.length === 0" class="admin-empty">등록된 공지사항이 없습니다.</div>

    <table v-else class="admin-table">
      <thead>
        <tr><th>카테고리</th><th>제목</th><th>조회</th><th>등록일</th><th style="width:110px;"></th></tr>
      </thead>
      <tbody>
        <tr v-for="n in notices" :key="n.noticeId">
          <td><span class="tbl-badge" :class="n.category === '이벤트' ? 'tbl-badge-blue' : 'tbl-badge-green'">{{ n.category }}</span></td>
          <td>{{ n.title }}</td>
          <td>{{ n.views }}</td>
          <td>{{ formatDate(n.createdAt) }}</td>
          <td>
            <div class="admin-actions">
              <button class="btn btn-secondary btn-xs" @click="openEdit(n)">수정</button>
              <button class="btn btn-danger btn-xs" @click="remove(n)">삭제</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 등록/수정 모달 -->
    <div v-if="showModal" class="auth-overlay open" @click.self="closeModal">
      <div class="auth-modal notice-modal" role="dialog" aria-modal="true">
        <div class="admin-modal-header">
          <button class="auth-close" @click="closeModal">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
          <h3 class="admin-modal-title">{{ editTarget ? '공지사항 수정' : '공지사항 등록' }}</h3>
        </div>

        <div class="admin-form">
          <label class="admin-label">카테고리</label>
          <div class="cat-btns">
            <button
              v-for="c in CATEGORIES" :key="c"
              class="cat-btn"
              :class="{ active: form.category === c }"
              @click="form.category = c"
            >{{ c }}</button>
          </div>

          <label class="admin-label">제목 <span class="req">*</span></label>
          <input v-model="form.title" class="admin-input" placeholder="공지사항 제목" />

          <label class="admin-label">내용 <span class="req">*</span></label>
          <div class="editor-wrap">
            <QuillEditor
              v-model:content="form.content"
              content-type="html"
              theme="snow"
              :toolbar="[
                [{ header: [1, 2, 3, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ color: [] }, { background: [] }],
                [{ list: 'ordered' }, { list: 'bullet' }],
                ['blockquote', 'code-block'],
                ['link', 'image'],
                ['clean']
              ]"
            />
          </div>
        </div>

        <div class="admin-modal-footer">
          <button class="btn btn-secondary btn-sm" @click="closeModal">취소</button>
          <button class="btn btn-primary btn-sm" @click="submit">{{ editTarget ? '수정' : '등록' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.admin-page-title  { font-size: 20px; font-weight: 700; color: var(--ink-900, #111827); }
.admin-empty       { color: var(--ink-400, #9ca3af); padding: 40px 0; text-align: center; }

.admin-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.admin-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid var(--border, #e5e7eb); font-weight: 600; color: var(--ink-600); }
.admin-table td { padding: 10px 12px; border-bottom: 1px solid var(--border, #e5e7eb); }
.admin-table tr:hover td { background: var(--ink-50, #f8f9fa); }
.admin-actions { display: flex; gap: 6px; }

.tbl-badge { display: inline-block; padding: 2px 8px; border-radius: 99px; font-size: 12px; font-weight: 500; }
.tbl-badge-green { background: #f0fdf4; color: #15803d; }
.tbl-badge-blue  { background: #eff6ff; color: #1d4ed8; }
.tbl-badge-gray  { background: #f3f4f6; color: #374151; }

.notice-modal {
  width: 800px; max-width: 95vw; padding: 0;
  max-height: 92vh; display: flex; flex-direction: column; overflow: hidden;
}
.admin-modal-header {
  padding: 20px 28px 0;
  flex-shrink: 0;
}
.admin-modal-title { font-size: 17px; font-weight: 700; margin-bottom: 16px; color: var(--ink-900); }
.admin-form {
  display: flex; flex-direction: column; gap: 4px;
  flex: 1; overflow: hidden; padding: 0 28px 4px; min-height: 0;
}
.admin-label { font-size: 13px; font-weight: 500; color: var(--ink-600); margin-top: 10px; }
.req { color: #ef4444; }
.admin-input {
  width: 100%; padding: 8px 10px;
  border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box; background: #fff;
}
.admin-input:focus { border-color: var(--green-500, #308860); }
.editor-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 4px;
  overflow: hidden;
}
.editor-wrap :deep(.ql-toolbar.ql-snow) {
  border: none !important;
  border-bottom: 1px solid var(--border, #e5e7eb) !important;
  flex-shrink: 0;
}
.editor-wrap :deep(.ql-container.ql-snow) {
  border: none !important;
  flex: 1;
  min-height: 0;
  height: auto !important;
  overflow-y: auto;
}
.editor-wrap :deep(.ql-editor) {
  min-height: 200px;
}
.admin-modal-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 16px 28px 20px;
  border-top: 1px solid var(--border, #e5e7eb);
  flex-shrink: 0;
}

.cat-btns { display: flex; gap: 8px; margin-top: 4px; }
.cat-btn {
  padding: 6px 14px; border-radius: 99px; font-size: 13px; font-weight: 500;
  border: 1px solid var(--border, #e5e7eb); background: #fff; cursor: pointer;
  color: var(--ink-600); transition: all 0.15s;
}
.cat-btn.active {
  background: var(--green-500, #308860); color: #fff; border-color: var(--green-500, #308860);
}

.btn-xs { padding: 4px 10px; font-size: 12px; }
.btn-danger { background: #ef4444; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-danger:hover { background: #dc2626; }
</style>

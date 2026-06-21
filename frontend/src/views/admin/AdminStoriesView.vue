<script setup>
import { ref, onMounted } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const BASE = 'http://localhost:8080/api/admin'
const OPTS = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

const stories = ref([])
const loading = ref(false)
const showModal = ref(false)
const editTarget = ref(null)
const form = ref(emptyForm())
const thumbnailUploading = ref(false)

function emptyForm() {
  return { title: '', content: '', thumbnail: '' }
}

async function load() {
  loading.value = true
  try {
    const res = await fetch(`${BASE}/stories`, OPTS)
    const data = await res.json()
    stories.value = data.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editTarget.value = null
  form.value = emptyForm()
  showModal.value = true
}

async function openEdit(s) {
  editTarget.value = s
  const res = await fetch(`${BASE}/stories/${s.storyId}`, OPTS)
  const data = await res.json()
  const full = data.data || s
  form.value = { title: full.title, content: full.content || '', thumbnail: full.thumbnail || '' }
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function handleThumbnail(e) {
  const file = e.target.files[0]
  if (!file) return
  thumbnailUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch(`${BASE}/stories/thumbnail`, { method: 'POST', credentials: 'include', body: fd })
    const data = await res.json()
    if (data.success) form.value.thumbnail = data.url
    else alert(data.message || '썸네일 업로드에 실패했습니다.')
  } finally {
    thumbnailUploading.value = false
  }
}

async function submit() {
  if (!form.value.title || !form.value.content || form.value.content === '<p><br></p>') {
    alert('제목과 본문은 필수입니다.'); return
  }
  const url = editTarget.value ? `${BASE}/stories/${editTarget.value.storyId}` : `${BASE}/stories`
  const method = editTarget.value ? 'PUT' : 'POST'
  await fetch(url, { ...OPTS, method, body: JSON.stringify(form.value) })
  closeModal()
  load()
}

async function remove(s) {
  if (!confirm(`'${s.title}' 스토리를 삭제하시겠습니까?`)) return
  await fetch(`${BASE}/stories/${s.storyId}`, { ...OPTS, method: 'DELETE' })
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
      <h2 class="admin-page-title">합격스토리 관리</h2>
      <button class="btn btn-primary btn-sm" @click="openCreate">+ 등록</button>
    </div>

    <div v-if="loading" class="admin-empty">불러오는 중...</div>
    <div v-else-if="stories.length === 0" class="admin-empty">등록된 합격스토리가 없습니다.</div>

    <table v-else class="admin-table">
      <thead>
        <tr><th>제목</th><th>썸네일</th><th>조회</th><th>좋아요</th><th>등록일</th><th style="width:110px;"></th></tr>
      </thead>
      <tbody>
        <tr v-for="s in stories" :key="s.storyId">
          <td>{{ s.title }}</td>
          <td>
            <img v-if="s.thumbnail" :src="s.thumbnail" style="width:48px;height:32px;object-fit:cover;border-radius:4px;" />
            <span v-else class="admin-muted">-</span>
          </td>
          <td>{{ s.views }}</td>
          <td>{{ s.likes }}</td>
          <td>{{ formatDate(s.createdAt) }}</td>
          <td>
            <div class="admin-actions">
              <button class="btn btn-secondary btn-xs" @click="openEdit(s)">수정</button>
              <button class="btn btn-danger btn-xs" @click="remove(s)">삭제</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 등록/수정 모달 -->
    <div v-if="showModal" class="auth-overlay open" @click.self="closeModal">
      <div class="auth-modal story-modal" role="dialog" aria-modal="true">
        <div class="admin-modal-header">
          <button class="auth-close" @click="closeModal">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
          <h3 class="admin-modal-title">{{ editTarget ? '스토리 수정' : '스토리 등록' }}</h3>
        </div>

        <div class="admin-form">
          <label class="admin-label">제목 <span class="req">*</span></label>
          <input v-model="form.title" class="admin-input" placeholder="합격 스토리 제목" />

          <label class="admin-label">썸네일</label>
          <div class="thumb-upload">
            <img v-if="form.thumbnail" :src="form.thumbnail" class="thumb-preview" />
            <label class="thumb-btn" :class="{ uploading: thumbnailUploading }">
              {{ thumbnailUploading ? '업로드 중...' : (form.thumbnail ? '이미지 변경' : '이미지 선택') }}
              <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" style="display:none" :disabled="thumbnailUploading" @change="handleThumbnail" />
            </label>
          </div>
          <p class="thumb-hint">JPEG · PNG · GIF · WEBP 형식, 최대 5MB</p>

          <label class="admin-label">본문 <span class="req">*</span></label>
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
.admin-table td { padding: 10px 12px; border-bottom: 1px solid var(--border, #e5e7eb); vertical-align: middle; }
.admin-table tr:hover td { background: var(--ink-50, #f8f9fa); }
.admin-actions { display: flex; gap: 6px; }
.admin-muted { color: var(--ink-400, #9ca3af); }

.story-modal {
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
.admin-modal-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 16px 28px 20px;
  border-top: 1px solid var(--border, #e5e7eb);
  flex-shrink: 0;
}

.btn-xs { padding: 4px 10px; font-size: 12px; }
.btn-danger { background: #ef4444; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-danger:hover { background: #dc2626; }

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
.thumb-hint { font-size: 12px; color: var(--ink-400, #9ca3af); margin-top: 4px; }
.thumb-upload { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.thumb-preview { width: 80px; height: 54px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border, #e5e7eb); flex-shrink: 0; }
.thumb-btn {
  display: inline-block; padding: 6px 14px;
  background: #f3f4f6; border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 13px; font-weight: 500; color: var(--ink-700, #374151);
  cursor: pointer; transition: background 0.15s;
}
.thumb-btn:hover { background: #e5e7eb; }
.thumb-btn.uploading { opacity: 0.6; cursor: not-allowed; }
</style>

<script setup>
import { ref, onMounted } from 'vue'

const BASE = 'http://localhost:8080/api/admin'
const OPTS = { credentials: 'include', headers: { 'Content-Type': 'application/json' } }

const FIXED_QUESTIONS = [
  '간단한 자기소개를 부탁드려요.',
  '지원자님의 취업 준비 과정이 궁금합니다.',
  'DADA AI 모의면접 서비스를 취업 준비 과정에서 어떻게 활용하셨나요?',
  '현재 취업을 준비하고 있는 후배들에게 해주고 싶은 조언이 있다면 말씀해주세요.',
]

const stories = ref([])
const loading = ref(false)
const showModal = ref(false)
const editTarget = ref(null)
const form = ref(emptyForm())
const thumbnailUploading = ref(false)

function emptyForm() {
  return {
    title: '',
    company: '',
    jobRole: '',
    careerType: '',
    thumbnail: '',
    summary: '',
    sections: FIXED_QUESTIONS.map(q => ({ question: q, answer: '', image: '', extra: [] }))
  }
}

function parseContent(content) {
  try {
    const parsed = JSON.parse(content)
    if (parsed.__template__) return parsed
  } catch {}
  return null
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
  const parsed = parseContent(full.content)
  if (parsed) {
    form.value = {
      title: full.title,
      company: parsed.company || '',
      jobRole: parsed.jobRole || '',
      careerType: parsed.careerType || '',
      thumbnail: full.thumbnail || '',
      summary: parsed.summary || '',
      sections: parsed.sections?.length
        ? parsed.sections.map(s => ({ ...s, extra: s.extra || [] }))
        : emptyForm().sections
    }
  } else {
    form.value = { ...emptyForm(), title: full.title, thumbnail: full.thumbnail || '' }
  }
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
  if (!form.value.title.trim())      { alert('제목은 필수입니다.'); return }
  if (!form.value.company.trim())    { alert('합격 기업은 필수입니다.'); return }
  if (!form.value.jobRole.trim())    { alert('합격 직무는 필수입니다.'); return }
  if (!form.value.careerType)        { alert('경력 구분을 선택해주세요.'); return }
  if (!form.value.thumbnail)         { alert('썸네일 이미지는 필수입니다.'); return }

  const content = JSON.stringify({
    __template__: true,
    company: form.value.company,
    jobRole: form.value.jobRole,
    careerType: form.value.careerType,
    summary: form.value.summary,
    sections: form.value.sections
  })

  const url = editTarget.value ? `${BASE}/stories/${editTarget.value.storyId}` : `${BASE}/stories`
  const method = editTarget.value ? 'PUT' : 'POST'
  await fetch(url, { ...OPTS, method, body: JSON.stringify({ title: form.value.title, content, thumbnail: form.value.thumbnail }) })
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

function addExtra(section) {
  section.extra.push({ question: '', answer: '', image: '' })
}

const imageUploading = ref(false)

async function uploadImage(target, e) {
  const file = e.target.files[0]
  if (!file) return
  imageUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch(`${BASE}/stories/thumbnail`, { method: 'POST', credentials: 'include', body: fd })
    const data = await res.json()
    if (data.success) target.image = data.url
    else alert(data.message || '이미지 업로드에 실패했습니다.')
  } finally {
    imageUploading.value = false
    e.target.value = ''
  }
}

function removeExtra(section, idx) {
  section.extra.splice(idx, 1)
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
        <tr><th>썸네일</th><th>제목</th><th>조회</th><th>좋아요</th><th>등록일</th><th style="width:110px;"></th></tr>
      </thead>
      <tbody>
        <tr v-for="s in stories" :key="s.storyId">
          <td>
            <img v-if="s.thumbnail" :src="s.thumbnail" style="width:64px;height:42px;object-fit:cover;border-radius:4px;" />
            <span v-else class="admin-muted">-</span>
          </td>
          <td>{{ s.title }}</td>
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
          <!-- 기본 정보 -->
          <div class="form-row">
            <div class="form-col">
              <label class="admin-label">제목 <span class="req">*</span></label>
              <input v-model="form.title" class="admin-input" placeholder="합격 스토리 제목" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-col">
              <label class="admin-label">합격 기업 <span class="req">*</span></label>
              <input v-model="form.company" class="admin-input" placeholder="예) 카카오" />
            </div>
            <div class="form-col">
              <label class="admin-label">합격 직무 <span class="req">*</span></label>
              <input v-model="form.jobRole" class="admin-input" placeholder="예) 백엔드 개발" />
            </div>
          </div>

          <label class="admin-label">경력 구분 <span class="req">*</span></label>
          <div class="career-options">
            <label v-for="opt in ['신입', '인턴', '경력']" :key="opt" class="career-radio">
              <input type="radio" :value="opt" v-model="form.careerType" />
              <span>{{ opt }}</span>
            </label>
          </div>

          <label class="admin-label">썸네일 <span class="req">*</span></label>
          <div class="thumb-upload">
            <img v-if="form.thumbnail" :src="form.thumbnail" class="thumb-preview" />
            <label class="thumb-btn" :class="{ uploading: thumbnailUploading }">
              {{ thumbnailUploading ? '업로드 중...' : (form.thumbnail ? '이미지 변경' : '이미지 선택') }}
              <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" style="display:none" :disabled="thumbnailUploading" @change="handleThumbnail" />
            </label>
          </div>
          <p class="thumb-hint">JPEG · PNG · GIF · WEBP 형식, 최대 5MB</p>

          <!-- 질문 섹션 -->
          <div class="sections-wrap">
            <template v-for="(section, si) in form.sections" :key="si">
              <!-- 고정 질문 -->
              <div class="qa-section">
                <div class="qa-q-label">
                  <span class="qa-q-mark">Q.</span>
                  <span class="qa-q-text">{{ section.question }}</span>
                </div>
                <textarea v-model="section.answer" class="admin-textarea" rows="4" placeholder="답변을 입력하세요." />
                <div class="qa-img-row">
                  <div v-if="section.image" class="qa-img-preview-wrap">
                    <img :src="section.image" class="qa-img-preview" />
                    <button class="qa-img-remove" @click="section.image = ''" title="이미지 삭제">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6 6 18M6 6l12 12"/></svg>
                    </button>
                  </div>
                  <label class="qa-img-btn" :class="{ uploading: imageUploading }">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                    {{ section.image ? '이미지 변경' : '이미지 첨부' }}
                    <input type="file" accept="image/*" style="display:none" :disabled="imageUploading" @change="uploadImage(section, $event)" />
                  </label>
                </div>
              </div>

              <!-- 사이에 추가된 질문들 -->
              <div v-for="(ex, ei) in section.extra" :key="ei" class="extra-qa-block">
                <div class="extra-q-row">
                  <span class="qa-q-mark extra-q-mark">Q.</span>
                  <input v-model="ex.question" class="admin-input extra-q-input" placeholder="추가 질문을 입력하세요." />
                  <button class="extra-remove-btn" @click="removeExtra(section, ei)" title="삭제">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
                  </button>
                </div>
                <textarea v-model="ex.answer" class="admin-textarea" rows="3" placeholder="답변을 입력하세요." />
                <div class="qa-img-row">
                  <div v-if="ex.image" class="qa-img-preview-wrap">
                    <img :src="ex.image" class="qa-img-preview" />
                    <button class="qa-img-remove" @click="ex.image = ''" title="이미지 삭제">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6 6 18M6 6l12 12"/></svg>
                    </button>
                  </div>
                  <label class="qa-img-btn" :class="{ uploading: imageUploading }">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                    {{ ex.image ? '이미지 변경' : '이미지 첨부' }}
                    <input type="file" accept="image/*" style="display:none" :disabled="imageUploading" @change="uploadImage(ex, $event)" />
                  </label>
                </div>
              </div>

              <!-- + 질문 추가 버튼 (섹션 사이) -->
              <div class="add-extra-wrap">
                <button class="add-extra-btn" @click="addExtra(section)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 5v14M5 12h14"/></svg>
                  질문 추가
                </button>
              </div>
            </template>
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
  width: 760px; max-width: 95vw; padding: 0;
  max-height: 92vh; display: flex; flex-direction: column; overflow: hidden;
}
.admin-modal-header { padding: 20px 28px 0; flex-shrink: 0; }
.admin-modal-title  { font-size: 17px; font-weight: 700; margin-bottom: 16px; color: var(--ink-900); }
.admin-form {
  display: flex; flex-direction: column; gap: 0;
  flex: 1; overflow-y: auto; padding: 0 28px 16px; min-height: 0;
}
.admin-label { font-size: 13px; font-weight: 500; color: var(--ink-600); margin-top: 14px; display: block; margin-bottom: 4px; }
.req { color: #ef4444; }
.admin-input {
  width: 100%; padding: 8px 10px;
  border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box; background: #fff;
}
.admin-input:focus { border-color: var(--green-500, #308860); }
.admin-textarea {
  width: 100%; padding: 8px 10px;
  border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box;
  resize: vertical; font-family: inherit; line-height: 1.6;
}
.admin-textarea:focus { border-color: var(--green-500, #308860); }
.admin-modal-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 16px 28px 20px;
  border-top: 1px solid var(--border, #e5e7eb);
  flex-shrink: 0;
}

.btn-xs     { padding: 4px 10px; font-size: 12px; }
.btn-danger { background: #ef4444; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-danger:hover { background: #dc2626; }

/* 가로 2열 */
.form-row { display: flex; gap: 12px; }
.form-col  { flex: 1; min-width: 0; }

/* 경력 구분 라디오 */
.career-options { display: flex; gap: 16px; margin-top: 6px; }
.career-radio {
  display: flex; align-items: center; gap: 6px;
  font-size: 14px; cursor: pointer; color: var(--ink-700);
}
.career-radio input { accent-color: var(--green-500, #308860); cursor: pointer; }

/* 썸네일 */
.thumb-hint   { font-size: 12px; color: var(--ink-400, #9ca3af); margin-top: 4px; }
.thumb-upload { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.thumb-preview { width: 80px; height: 54px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border, #e5e7eb); flex-shrink: 0; }
.thumb-btn {
  display: inline-block; padding: 6px 14px;
  background: #f3f4f6; border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 13px; font-weight: 500; color: var(--ink-700, #374151);
  cursor: pointer; transition: background 0.15s;
}
.thumb-btn:hover    { background: #e5e7eb; }
.thumb-btn.uploading { opacity: 0.6; cursor: not-allowed; }

/* Q&A 섹션 */
.sections-wrap { display: flex; flex-direction: column; gap: 0; margin-top: 16px; }
.qa-section {
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
  padding: 16px 18px;
  background: #fafafa;
}
.qa-q-label {
  display: flex; align-items: flex-start; gap: 8px; margin-bottom: 8px;
}
.qa-q-mark {
  flex-shrink: 0; font-size: 14px; font-weight: 800;
  color: var(--green-600, #308860); margin-top: 1px;
}
.qa-q-text { font-size: 14px; font-weight: 600; color: var(--ink-800, #1f2937); line-height: 1.5; }

/* 사이에 추가된 질문 블록 */
.extra-qa-block {
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
  padding: 14px 18px;
  background: #fff;
  margin-top: 6px;
}
.extra-q-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.extra-q-mark { font-size: 13px; margin-top: 0; color: var(--ink-500, #6b7280); }
.extra-q-input { flex: 1; font-size: 13px; }
.extra-remove-btn {
  flex-shrink: 0; width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  background: #fff; color: var(--ink-400); cursor: pointer;
}
.extra-remove-btn:hover { background: #fee2e2; color: #ef4444; border-color: #fca5a5; }

/* + 질문 추가 버튼 */
.add-extra-wrap { display: flex; justify-content: center; padding: 6px 0; }
.add-extra-btn {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 14px;
  background: #fff; border: 1px dashed var(--green-500, #308860);
  border-radius: 6px; font-size: 13px; color: var(--green-500, #308860);
  font-weight: 500; cursor: pointer; transition: background 0.15s;
}
.add-extra-btn:hover { background: var(--green-50, #f0faf5); }

/* Q&A 이미지 첨부 */
.qa-img-row { display: flex; align-items: center; gap: 10px; margin-top: 8px; }
.qa-img-preview-wrap { position: relative; flex-shrink: 0; }
.qa-img-preview { width: 80px; height: 54px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border, #e5e7eb); display: block; }
.qa-img-remove {
  position: absolute; top: -6px; right: -6px;
  width: 18px; height: 18px; border-radius: 50%;
  background: #ef4444; color: #fff; border: none;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; padding: 0;
}
.qa-img-btn {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 12px;
  background: #f3f4f6; border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 12px; font-weight: 500; color: var(--ink-600, #4b5563);
  cursor: pointer; transition: background 0.15s;
}
.qa-img-btn:hover { background: #e5e7eb; }
.qa-img-btn.uploading { opacity: 0.6; cursor: not-allowed; }
</style>

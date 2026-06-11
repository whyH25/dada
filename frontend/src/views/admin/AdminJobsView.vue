<script setup>
import { ref, computed, onMounted } from 'vue'
import { getJobSchedules, createJobSchedule, updateJobSchedule, deleteJobSchedule } from '../../api/adminApi.js'
import { toast } from '../../utils/toast.js'

const schedules = ref([])
const loading = ref(false)
const showModal = ref(false)
const editTarget = ref(null)

const COMPANY_TYPES = ['대기업', '중견기업', '중소기업', '스타트업', '공기업', '외국계']
const EMPLOYMENT_TYPES = ['정규직', '계약직', '인턴', '파견직', '프리랜서']

const form = ref(emptyForm())

function emptyForm() {
  return { companyName: '', jobTitle: '', companyType: '', employmentType: '', startDate: '', endDate: '', description: '', jobUrl: '' }
}

async function load() {
  loading.value = true
  try {
    const res = await getJobSchedules()
    schedules.value = res.data
  } catch { toast('일정을 불러오지 못했습니다.') }
  finally { loading.value = false }
}

function openCreate() { editTarget.value = null; form.value = emptyForm(); showModal.value = true }
function openEdit(s) {
  editTarget.value = s
  form.value = {
    companyName: s.companyName, jobTitle: s.jobTitle,
    companyType: s.companyType || '', employmentType: s.employmentType || '',
    startDate: s.startDate, endDate: s.endDate,
    description: s.description || '', jobUrl: s.jobUrl || '',
  }
  showModal.value = true
}
function closeModal() { showModal.value = false }

async function submit() {
  if (!form.value.companyName || !form.value.jobTitle || !form.value.startDate || !form.value.endDate) {
    toast('회사명, 직무명, 시작일, 마감일은 필수입니다.'); return
  }
  try {
    if (editTarget.value) { await updateJobSchedule(editTarget.value.scheduleId, form.value); toast('수정되었습니다.') }
    else { await createJobSchedule(form.value); toast('등록되었습니다.') }
    closeModal(); load()
  } catch (e) { toast(e.message) }
}

async function remove(s) {
  if (!confirm(`'${s.companyName} - ${s.jobTitle}' 일정을 삭제하시겠습니까?`)) return
  try { await deleteJobSchedule(s.scheduleId); toast('삭제되었습니다.'); load() }
  catch (e) { toast(e.message) }
}

// ── 미리보기 ──────────────────────────────────────────
function formatKorDate(d) {
  if (!d) return null
  const [, m, day] = d.split('-')
  return `${parseInt(m)}월 ${parseInt(day)}일`
}
function formatDotDate(d) {
  if (!d) return ''
  return d.replace(/-/g, '.')
}

const previewStartKor = computed(() => formatKorDate(form.value.startDate))
const previewEndKor   = computed(() => formatKorDate(form.value.endDate))

onMounted(load)
</script>

<template>
  <div>
    <div class="admin-page-header">
      <h2 class="admin-page-title">채용일정 관리</h2>
      <button class="btn btn-primary btn-sm" @click="openCreate">+ 등록</button>
    </div>

    <div v-if="loading" class="admin-empty">불러오는 중...</div>
    <div v-else-if="schedules.length === 0" class="admin-empty">등록된 채용일정이 없습니다.</div>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>회사명</th><th>직무명</th><th>기업형태</th><th>고용형태</th>
          <th>시작일</th><th>마감일</th><th>공고 URL</th><th style="width:120px;"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in schedules" :key="s.scheduleId">
          <td>{{ s.companyName }}</td>
          <td>{{ s.jobTitle }}</td>
          <td><span v-if="s.companyType" class="tbl-badge tbl-badge-blue">{{ s.companyType }}</span><span v-else class="admin-muted">-</span></td>
          <td><span v-if="s.employmentType" class="tbl-badge tbl-badge-green">{{ s.employmentType }}</span><span v-else class="admin-muted">-</span></td>
          <td>{{ s.startDate }}</td><td>{{ s.endDate }}</td>
          <td><a v-if="s.jobUrl" :href="s.jobUrl" target="_blank" class="admin-link">링크</a><span v-else class="admin-muted">-</span></td>
          <td class="admin-actions">
            <button class="btn btn-secondary btn-xs" @click="openEdit(s)">수정</button>
            <button class="btn btn-danger btn-xs" @click="remove(s)">삭제</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 등록/수정 모달 -->
    <div v-if="showModal" class="auth-overlay open" @click.self="closeModal">
      <div class="auth-modal admin-modal-wide" role="dialog" aria-modal="true">
        <button class="auth-close" @click="closeModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
        </button>

        <div class="admin-modal-cols">

          <!-- ── 좌: 입력 폼 ── -->
          <div class="admin-modal-form-col">
            <h3 class="admin-modal-title">{{ editTarget ? '채용일정 수정' : '채용일정 등록' }}</h3>
            <div class="admin-form">

              <label class="admin-label">회사명 <span class="req">*</span></label>
              <input v-model="form.companyName" class="admin-input" placeholder="예) 삼성전자" />

              <label class="admin-label">직무명 <span class="req">*</span></label>
              <input v-model="form.jobTitle" class="admin-input" placeholder="예) 소프트웨어 개발" />

              <div class="admin-form-row">
                <div>
                  <label class="admin-label">기업형태</label>
                  <select v-model="form.companyType" class="admin-input">
                    <option value="">선택</option>
                    <option v-for="t in COMPANY_TYPES" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div>
                  <label class="admin-label">고용형태</label>
                  <select v-model="form.employmentType" class="admin-input">
                    <option value="">선택</option>
                    <option v-for="t in EMPLOYMENT_TYPES" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
              </div>

              <div class="admin-form-row">
                <div>
                  <label class="admin-label">시작일 <span class="req">*</span></label>
                  <input v-model="form.startDate" type="date" class="admin-input" />
                </div>
                <div>
                  <label class="admin-label">마감일 <span class="req">*</span></label>
                  <input v-model="form.endDate" type="date" class="admin-input" />
                </div>
              </div>

              <label class="admin-label">설명</label>
              <textarea v-model="form.description" class="admin-input admin-textarea" placeholder="채용 관련 상세 내용"></textarea>

              <label class="admin-label">공고 URL</label>
              <input v-model="form.jobUrl" class="admin-input" placeholder="https://..." />
            </div>

            <div class="admin-modal-footer">
              <button class="btn btn-secondary btn-sm" @click="closeModal">취소</button>
              <button class="btn btn-primary btn-sm" @click="submit">{{ editTarget ? '수정' : '등록' }}</button>
            </div>
          </div>

          <!-- ── 우: 미리보기 ── -->
          <div class="admin-preview-col">

            <!-- 카드 1: 상세 카드 미리보기 -->
            <div class="preview-card">
              <div class="preview-card-head">
                <div class="preview-co-circle">{{ form.companyName ? form.companyName.charAt(0) : '?' }}</div>
                <div>
                  <div class="preview-co-name">{{ form.companyName || '회사명' }}</div>
                  <div class="preview-job-title">{{ form.jobTitle || '직무명' }}</div>
                </div>
              </div>

              <div class="preview-badges" v-if="form.companyType || form.employmentType">
                <span v-if="form.companyType" class="tbl-badge tbl-badge-blue">{{ form.companyType }}</span>
                <span v-if="form.employmentType" class="tbl-badge tbl-badge-green">{{ form.employmentType }}</span>
              </div>

              <hr class="preview-divider" />

              <p class="preview-desc">{{ form.description || '등록된 소개가 없습니다.' }}</p>

              <hr class="preview-divider" />

              <div class="preview-meta-row">
                <div v-if="form.companyType" class="preview-meta-item">
                  <span class="preview-meta-label">기업 형태</span>
                  <span class="preview-meta-val">{{ form.companyType }}</span>
                </div>
                <div v-if="form.jobTitle" class="preview-meta-item">
                  <span class="preview-meta-label">채용 직무</span>
                  <span class="preview-meta-val">{{ form.jobTitle }}</span>
                </div>
                <div v-if="form.employmentType" class="preview-meta-item">
                  <span class="preview-meta-label">채용 형태</span>
                  <span class="preview-meta-val">{{ form.employmentType }}</span>
                </div>
              </div>

              <div class="preview-date-strip">
                <div class="preview-date-item">
                  <span class="preview-dot dot-start"></span>
                  <div>
                    <div class="preview-date-label">서류 시작</div>
                    <div class="preview-date-val">{{ form.startDate ? formatDotDate(form.startDate) : '—' }}</div>
                  </div>
                </div>
                <div class="preview-date-sep"></div>
                <div class="preview-date-item">
                  <span class="preview-dot dot-end"></span>
                  <div>
                    <div class="preview-date-label">서류 마감</div>
                    <div class="preview-date-val">{{ form.endDate ? formatDotDate(form.endDate) : '—' }}</div>
                  </div>
                </div>
              </div>

              <div class="preview-btn-row">
                <span class="preview-btn-primary">공고 원문 보기</span>
              </div>
            </div>

            <!-- 카드 2: 캘린더 표시 미리보기 -->
            <div class="preview-card">
              <div class="preview-cal-item">
                <span class="preview-dot dot-start"></span>
                <span class="preview-cal-text">
                  서류 시작 : <strong>{{ previewStartKor || '날짜 미입력' }}</strong>
                </span>
              </div>
              <div class="preview-cal-item" style="margin-top:10px;">
                <span class="preview-dot dot-end"></span>
                <span class="preview-cal-text">
                  서류 마감 : <strong>{{ previewEndKor || '날짜 미입력' }}</strong>
                </span>
              </div>
            </div>

          </div>
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
.admin-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid var(--border, #e5e7eb); font-weight: 600; color: var(--ink-600, #374151); }
.admin-table td { padding: 10px 12px; border-bottom: 1px solid var(--border, #e5e7eb); }
.admin-table tr:hover td { background: var(--ink-50, #f8f9fa); }
.admin-actions { display: flex; gap: 6px; }
.admin-link   { color: var(--green-500, #308860); text-decoration: none; font-size: 13px; }
.admin-muted  { color: var(--ink-400, #9ca3af); }

.tbl-badge { display: inline-block; padding: 2px 8px; border-radius: 99px; font-size: 12px; font-weight: 500; }
.tbl-badge-blue  { background: #e0f2fe; color: #0369a1; }
.tbl-badge-green { background: #f0fdf4; color: #15803d; }

/* 모달 */
.admin-modal-wide { width: 860px; max-width: 95vw; padding: 28px; }
.admin-modal-cols {
  display: grid; grid-template-columns: 1fr 1fr; gap: 24px;
}
@media (max-width: 700px) { .admin-modal-cols { grid-template-columns: 1fr; } }
.admin-modal-form-col { display: flex; flex-direction: column; }
.admin-modal-title { font-size: 17px; font-weight: 700; margin-bottom: 16px; color: var(--ink-900, #111827); }

.admin-form { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.admin-form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.admin-label { font-size: 13px; font-weight: 500; color: var(--ink-600, #374151); margin-top: 10px; }
.req { color: #ef4444; }
.admin-input {
  width: 100%; padding: 8px 10px;
  border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box; background: #fff;
}
.admin-input:focus { border-color: var(--green-500, #308860); }
.admin-textarea { min-height: 72px; resize: vertical; }
.admin-modal-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }

/* 미리보기 패널 */
.admin-preview-col { display: flex; flex-direction: column; gap: 12px; }

.preview-card {
  background: #fff;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 12px;
  padding: 16px;
}
.preview-card-head { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.preview-co-circle {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--green-500, #308860); color: #fff;
  font-size: 15px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.preview-co-name   { font-size: 14px; font-weight: 700; color: var(--ink-900, #111827); }
.preview-job-title { font-size: 12px; color: var(--ink-500, #6b7280); margin-top: 1px; }
.preview-badges    { display: flex; gap: 5px; margin-bottom: 10px; flex-wrap: wrap; }
.preview-divider   { border: none; border-top: 1px solid var(--ink-150, #e5e7eb); margin: 10px 0; }
.preview-desc      { font-size: 12px; color: var(--ink-500, #6b7280); line-height: 1.5; margin: 0; }

.preview-meta-row  { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 10px; }
.preview-meta-item { display: flex; flex-direction: column; gap: 1px; }
.preview-meta-label { font-size: 10px; color: var(--ink-400, #9ca3af); }
.preview-meta-val   { font-size: 12px; font-weight: 600; color: var(--ink-800, #1f2937); }

.preview-date-strip {
  display: flex; align-items: center;
  background: var(--ink-50, #f8f9fa); border-radius: 8px; padding: 10px 12px;
  margin-bottom: 10px;
}
.preview-date-item  { display: flex; align-items: center; gap: 8px; flex: 1; }
.preview-date-label { font-size: 10px; color: var(--ink-400, #9ca3af); }
.preview-date-val   { font-size: 12px; font-weight: 600; color: var(--ink-900, #111827); }
.preview-date-sep   { width: 1px; height: 30px; background: var(--ink-200, #e5e7eb); margin: 0 12px; flex-shrink: 0; }

.preview-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
.dot-start { background: var(--green-500, #308860); }
.dot-end   { background: #ef4444; }

.preview-btn-row { display: flex; justify-content: flex-end; }
.preview-btn-primary {
  padding: 6px 14px; border-radius: 6px; font-size: 12px; font-weight: 600;
  background: var(--green-500, #308860); color: #fff;
}

/* 카드 2: 캘린더 표시 */
.preview-cal-item {
  display: flex; align-items: center; gap: 10px;
}
.preview-cal-text { font-size: 13px; color: var(--ink-700, #374151); }
.preview-cal-text strong { color: var(--ink-900, #111827); }

.btn-xs { padding: 4px 10px; font-size: 12px; }
.btn-danger { background: #ef4444; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-danger:hover { background: #dc2626; }
</style>

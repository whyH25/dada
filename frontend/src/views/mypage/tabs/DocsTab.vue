<script setup>
import { ref, onMounted } from 'vue'
import { toast } from '../../../utils/toast.js'
import { fetchMyFiles, uploadFile, deleteFile } from '../../../api/userFileApi.js'

const props = defineProps({
  type: { type: String, required: true }, // 'resume' | 'portfolio'
})
const emit = defineEmits(['update:count'])

const docMeta = {
  resume:    { title: '이력서/자기소개서', accept: '.pdf,.doc,.docx', hint: 'PDF | DOC | 최대 10MB' },
  portfolio: { title: '포트폴리오',        accept: '.pdf,.doc,.docx', hint: 'PDF | DOC | 최대 50MB' },
}

const docs = ref([])
const docsLoading = ref(false)

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const date = `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
  return `${date} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadDocs() {
  docsLoading.value = true
  try {
    docs.value = await fetchMyFiles(props.type)
    emit('update:count', docs.value.length)
  } catch (e) {
    toast(e.message)
  } finally {
    docsLoading.value = false
  }
}

async function uploadDoc(e) {
  const f = e.target.files && e.target.files[0]
  e.target.value = ''
  if (!f) return
  try {
    const saved = await uploadFile(props.type, f)
    docs.value.unshift(saved)
    emit('update:count', docs.value.length)
    toast(`${docMeta[props.type].title}가 업로드되었어요.`)
  } catch (err) {
    toast(err.message)
  }
}

async function deleteDoc(i) {
  const item = docs.value[i]
  try {
    await deleteFile(props.type, item.id)
    docs.value.splice(i, 1)
    emit('update:count', docs.value.length)
  } catch (err) {
    toast(err.message)
  }
}

onMounted(loadDocs)
</script>

<template>
  <div>
    <div class="breadcrumb">마이페이지 <span class="sep">›</span> 서류 관리 <span class="sep">›</span> {{ docMeta[type].title }}</div>
    <div class="flex-between" style="margin:4px 0 20px;">
      <div>
        <h2 class="mp-h1">{{ docMeta[type].title }}</h2>
        <p class="mp-sub">면접관이 {{ docMeta[type].title }} 기반으로 맞춤형 질문을 생성합니다. 총 {{ docs.length }}건</p>
      </div>
    </div>
    <label class="doc-drop">
      <input type="file" :accept="docMeta[type].accept" style="display:none;" @change="uploadDoc" />
      <div class="doc-drop-icon"><svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" /></svg></div>
      <div class="doc-drop-title">{{ docMeta[type].title }} 업로드</div>
      <div class="doc-drop-hint">파일을 선택하거나 드래그하세요 | {{ docMeta[type].hint }}</div>
    </label>
    <div class="doc-list">
      <div v-if="docsLoading" class="doc-empty">불러오는 중...</div>
      <div v-else-if="!docs.length" class="doc-empty">아직 업로드한 {{ docMeta[type].title }}가 없어요.</div>
      <div v-for="(d, i) in docs" :key="d.id" class="doc-item">
        <div class="doc-ico"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg></div>
        <div class="doc-info"><div class="doc-name">{{ d.fileName }}</div><div class="doc-meta">업로드 {{ formatDateTime(d.createdAt) }}</div></div>
        <div class="doc-actions">
          <a class="btn btn-sm btn-ghost" :href="d.downloadUrl" target="_blank" rel="noopener">보기</a>
          <button class="btn btn-sm btn-ghost doc-del" @click="deleteDoc(i)">삭제</button>
        </div>
      </div>
    </div>
  </div>
</template>

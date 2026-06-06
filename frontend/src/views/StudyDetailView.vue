<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { toast } from '../utils/toast.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()

const idx = computed(() => Number(route.params.id) || 0)
const s = computed(() => data.studies[idx.value])
const text = ref('')
const pct = computed(() => Math.round((s.value.members / s.value.max) * 100))

function add() {
  const v = text.value.trim()
  if (!v) return
  data.addStudyComment(idx.value, v)
  text.value = ''
  toast('신청 댓글을 남겼어요. 방장이 확인하면 알려드릴게요.')
}
</script>

<template>
  <main class="page active" id="page-study-detail">
    <div class="container container-narrow">
      <div class="page-header" style="border-bottom:none; padding-bottom:8px; margin-bottom:8px;">
        <div class="breadcrumb">홈 <span class="sep">›</span> <a @click="router.push('/community')" style="cursor:pointer;">커뮤니티</a> <span class="sep">›</span> 스터디 모집</div>
      </div>
      <div class="pd-head">
        <div class="study-tags"><span v-for="(t, ti) in s.tags" :key="ti" class="badge badge-outline">{{ t }}</span></div>
        <h1 class="pd-title">{{ s.title }}</h1>
        <div class="pd-meta"><span>방장 익명의 {{ s.leader }}</span><span class="dot-sep"></span><span>모집 {{ s.members }}/{{ s.max }}명</span><span class="dot-sep"></span><span>{{ s.schedule }}</span></div>
      </div>
      <div class="sd-recruit-bar"><div class="sd-recruit-fill" :style="{ width: pct + '%' }"></div></div>
      <div class="pd-body"><p v-for="(t, i) in s.body" :key="i">{{ t }}</p></div>
      <div class="pd-comments">
        <h3 class="pd-comments-title">신청 댓글 {{ s.cl.length }}</h3>
        <p class="sd-apply-hint">지원 직무와 가능한 시간을 적어 신청해 주세요. 댓글은 익명으로 등록돼요.</p>
        <div>
          <div v-if="!s.cl.length" class="pd-comment-empty">아직 댓글이 없어요. 첫 댓글을 남겨보세요.</div>
          <div v-for="(c, i) in s.cl" :key="i" class="pd-comment">
            <div class="cr-avatar">{{ c.who[0] }}</div>
            <div><div class="cr-name">익명의 {{ c.who }} <span class="pd-comment-time">{{ c.time }}</span></div><div class="pd-comment-text">{{ c.t }}</div></div>
          </div>
        </div>
        <div class="pd-comment-input">
          <input v-model="text" placeholder="예) 마케팅 직무 지원합니다. 월·목 가능해요!" @keydown.enter="add" />
          <button class="btn btn-primary btn-sm" @click="add">신청</button>
        </div>
      </div>
      <div class="ri-actions"><button class="btn btn-ghost" @click="router.push('/community')">목록으로</button></div>
    </div>
  </main>
</template>

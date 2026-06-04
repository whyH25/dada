<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()

const idx = computed(() => Number(route.params.id) || 0)
const p = computed(() => data.boardPosts[idx.value])
const text = ref('')

function add() {
  const v = text.value.trim()
  if (!v) return
  data.addComment(idx.value, v)
  text.value = ''
}
</script>

<template>
  <main class="page active" id="page-post">
    <div class="container container-narrow">
      <div class="page-header" style="border-bottom:none; padding-bottom:8px; margin-bottom:8px;">
        <div class="breadcrumb">홈 <span class="sep">›</span> <a @click="router.push('/community')" style="cursor:pointer;">커뮤니티</a> <span class="sep">›</span> 자유게시판</div>
      </div>
      <div class="pd-head">
        <div class="post-head"><span class="badge" :class="p.cls">{{ p.cat }}</span><span class="post-co">{{ p.co }}</span></div>
        <h1 class="pd-title">{{ p.title }}</h1>
        <div class="pd-meta"><span>{{ p.author }}</span><span class="dot-sep"></span><span>{{ p.time }}</span><span class="dot-sep"></span><span>댓글 {{ p.cl.length }}</span></div>
      </div>
      <div class="pd-body"><p v-for="(t, i) in p.body" :key="i">{{ t }}</p></div>
      <div class="pd-comments">
        <h3 class="pd-comments-title">댓글 {{ p.cl.length }}</h3>
        <div>
          <div v-if="!p.cl.length" class="pd-comment-empty">아직 댓글이 없어요. 첫 댓글을 남겨보세요.</div>
          <div v-for="(c, i) in p.cl" :key="i" class="pd-comment">
            <div class="cr-avatar">{{ c.who[0] }}</div>
            <div><div class="cr-name">익명의 {{ c.who }} <span class="pd-comment-time">{{ c.time }}</span></div><div class="pd-comment-text">{{ c.t }}</div></div>
          </div>
        </div>
        <div class="pd-comment-input">
          <input v-model="text" placeholder="댓글을 입력하세요 (익명)" @keydown.enter="add" />
          <button class="btn btn-primary btn-sm" @click="add">등록</button>
        </div>
      </div>
      <div class="ri-actions"><button class="btn btn-ghost" @click="router.push('/community')">목록으로</button></div>
    </div>
  </main>
</template>

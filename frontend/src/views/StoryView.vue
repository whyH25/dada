<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()

const idx = computed(() => Number(route.params.id) || 0)
const s = computed(() => data.stories[idx.value])

// 원본 openStory(): 조회수 +1
onMounted(() => data.viewStory(idx.value))
function like() { data.likeStory(idx.value) }
</script>

<template>
  <main class="page active" id="page-story">
    <div class="container container-narrow">
      <div class="page-header" style="border-bottom:none; padding-bottom:8px; margin-bottom:8px;">
        <div class="breadcrumb">홈 <span class="sep">›</span> <a @click="router.push('/stories')" style="cursor:pointer;">합격 스토리</a> <span class="sep">›</span> 자세히 보기</div>
      </div>
      <article class="story-detail">
        <div class="sd-kicker"><span class="sd-kicker-tag">합격자 인터뷰</span><span class="sd-kicker-by">다대다 편집팀 정리</span></div>
        <h1 class="sd-title">{{ s.headline }}</h1>
        <div class="sd-top">
          <div class="company-logo" :class="s.logo" style="width:48px;height:48px;font-size:18px;border-radius:10px;">{{ s.short }}</div>
          <div><div class="story-meta-name" style="font-size:15px;">{{ s.co }} <span class="badge badge-green">{{ s.result }}</span></div><div class="story-meta-sub">{{ s.role }}</div></div>
        </div>
        <div class="sd-profile"><span class="sd-profile-label">인터뷰이</span>{{ s.profile }}</div>
        <div class="sd-meta"><span>{{ s.date }}</span><span class="dot-sep"></span><span>조회 {{ s.views.toLocaleString() }}</span><span class="dot-sep"></span><span>좋아요 {{ s.likes }}</span></div>
        <div class="sd-body">
          <div v-for="(p, i) in s.qa" :key="i" class="sd-qa"><div class="sd-q"><span class="sd-q-mark">Q</span>{{ p.q }}</div><div class="sd-a">{{ p.a }}</div></div>
        </div>
        <div class="sd-tip"><div class="sd-tip-label">면접 팁</div><div>{{ s.tip }}</div></div>
        <div class="sd-actions">
          <button class="btn btn-secondary" @click="like">♥ 좋아요 <span>{{ s.likes }}</span></button>
          <button class="btn btn-ghost" @click="router.push('/stories')">목록으로</button>
        </div>
      </article>
    </div>
  </main>
</template>

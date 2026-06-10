<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'

const router = useRouter()
const data = useDataStore()
const sort = ref('latest')

const sorted = computed(() => {
  const idx = data.stories.map((s, i) => i)
  idx.sort((a, b) => {
    if (sort.value === 'views') return data.stories[b].views - data.stories[a].views
    if (sort.value === 'likes') return data.stories[b].likes - data.stories[a].likes
    return data.stories[b].order - data.stories[a].order
  })
  return idx
})
function open(i) { router.push('/story/' + i) }
</script>

<template>
  <main class="page active" id="page-stories">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 합격 스토리</div>
        <h1 class="page-title">합격 스토리</h1>
        <p class="page-subtitle">다대다 편집팀이 직접 만난 합격자 인터뷰. 합격까지의 진짜 이야기를 들어보세요. 총 <strong>1,284</strong>건</p>
      </div>

      <div class="seg-tabs">
        <div class="seg-tab" :class="{ active: sort === 'latest' }" @click="sort = 'latest'">최신순</div>
        <div class="seg-tab" :class="{ active: sort === 'views' }" @click="sort = 'views'">조회순</div>
        <div class="seg-tab" :class="{ active: sort === 'likes' }" @click="sort = 'likes'">인기순</div>
      </div>

      <div class="story-grid story-grid-lg">
        <div v-for="i in sorted" :key="i" class="story-card" @click="open(i)">
          <div class="story-card-top">
            <div class="company-logo" :class="data.stories[i].logo">{{ data.stories[i].short }}</div>
            <div><div class="story-meta-name">{{ data.stories[i].co }}</div><div class="story-meta-sub">{{ data.stories[i].role }}</div></div>
          </div>
          <div class="story-kicker"><span class="story-kicker-tag">합격자 인터뷰</span><span class="story-kicker-result">{{ data.stories[i].result }}</span></div>
          <h3 class="story-headline">{{ data.stories[i].headline }}</h3>
          <p class="story-quote">{{ data.stories[i].quote }}</p>
          <div class="story-footer">
            <span class="story-read">인터뷰 읽기 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" /></svg></span>
            <span class="story-stats">조회 {{ data.stories[i].views.toLocaleString() }} | ♥ {{ data.stories[i].likes }}</span>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

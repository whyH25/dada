<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
</script>

<template>
  <div class="nav-menu">
    <div class="nav-item" :class="{ active: route.name === 'schedule' }" @click="router.push('/schedule')">채용 일정</div>
    <div class="nav-item" :class="{ active: ['interview-intro', 'create', 'prep', 'room', 'saving', 'done'].includes(route.name) }" @click="router.push('/interview/intro')">면접</div>
    <div class="nav-item" :class="{ active: route.name === 'stories' }" @click="router.push('/stories')">합격스토리</div>
    <div class="nav-item" :class="{ active: ['posts', 'post-create', 'post-edit', 'post-detail'].includes(route.name) }" @click="router.push('/community/posts')">커뮤니티</div>
    <!-- <div class="nav-item" :class="{ active: route.name === 'openchat' }" @click="router.push('/openchat')">기업별 오픈톡</div> -->
    <div class="nav-item" :class="{ active: route.name === 'notices' }" @click="router.push('/notices')">공지사항</div>
  </div>
</template>

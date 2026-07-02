<script setup>
import { useRouter } from 'vue-router'
import { useDataStore } from '../../stores/data.js'

const router = useRouter()
const data = useDataStore()
</script>

<template>
  <div class="comm-panel">
    <div class="card list-card">
      <div class="card-header">
        <h3 class="card-title">실시간 오픈채팅</h3>
        <span class="text-sm text-muted">접속 많은순</span>
      </div>
      <div
        v-for="(c, i) in data.openChats"
        :key="i"
        class="list-item chat-item"
        @click="router.push('/chatroom/' + i)"
      >
        <div class="chat-ico" :class="c.logo">{{ c.short }}</div>
        <div class="list-item-text">
          <div class="list-item-title">{{ c.title }}</div>
          <div class="list-item-sub">
            <span class="online-dot"></span>{{ c.online.toLocaleString() }}명 접속 중 | {{ c.tag }}
          </div>
        </div>
        <span class="chat-enter">입장</span>
      </div>
    </div>
  </div>
</template>

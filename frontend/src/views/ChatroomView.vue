<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()

const idx = computed(() => Number(route.params.id) || 0)
const c = computed(() => data.openChats[idx.value])
const text = ref('')
const messagesBox = ref(null)

function seedFor() {
  return [
    { who: '두꺼비', me: false, t: '오늘 1차 보신 분 계세요? 분위기 어땠나요' },
    { who: '수달', me: false, t: '저 오전에 봤어요. 면접관 두 분이 번갈아 질문하시는데 꼬리질문이 깊었습니다' },
    { who: '나', me: true, t: '헉 긴장되네요… 압박 질문도 있었나요?' },
    { who: '수달', me: false, t: '네 “그 판단의 근거가 뭐냐” 이런 식으로 계속 파고들어요' },
    { who: '부엉이', me: false, t: '다대다에서 연습한 대로 결론부터 말하니까 덜 흔들리더라고요 👍' },
  ]
}
const messages = ref(seedFor())

// 방 전환 시 메시지 초기화
watch(idx, () => { messages.value = seedFor(); scrollBottom() })

async function scrollBottom() {
  await nextTick()
  if (messagesBox.value) messagesBox.value.scrollTop = messagesBox.value.scrollHeight
}
function send() {
  const v = text.value.trim()
  if (!v) return
  messages.value.push({ me: true, t: v })
  text.value = ''
  scrollBottom()
  setTimeout(() => {
    messages.value.push({ who: '부엉이', me: false, t: '화이팅입니다! 다대다 한 번 더 돌리고 가세요 🙌' })
    scrollBottom()
  }, 1100)
}
</script>

<template>
  <main class="page active" id="page-chatroom">
    <div class="container">
      <div class="cr-layout">
        <div class="chatroom">
          <div class="cr-head">
            <button class="cr-back" @click="router.push('/community')"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6" /></svg></button>
            <div class="chat-ico" :class="c.logo">{{ c.short }}</div>
            <div class="cr-head-text"><div class="cr-title">{{ c.title }}</div><div class="cr-sub"><span class="online-dot"></span>{{ c.online.toLocaleString() }}명 접속 중 · {{ c.tag }}</div></div>
            <button class="cr-leave" @click="router.push('/community')">나가기</button>
          </div>
          <div class="cr-messages" ref="messagesBox">
            <div class="cr-day">오늘</div>
            <template v-for="(m, i) in messages" :key="i">
              <div v-if="m.me" class="cr-msg me"><div class="cr-bubble">{{ m.t }}</div></div>
              <div v-else class="cr-msg"><div class="cr-avatar">{{ m.who[0] }}</div><div><div class="cr-name">익명의 {{ m.who }}</div><div class="cr-bubble">{{ m.t }}</div></div></div>
            </template>
          </div>
          <div class="cr-input">
            <input v-model="text" placeholder="메시지를 입력하세요" @keydown.enter="send" />
            <button class="btn btn-primary btn-sm" @click="send">전송</button>
          </div>
        </div>
        <aside class="cr-side">
          <div class="cr-side-head">다른 오픈채팅방</div>
          <div class="cr-side-list">
            <button v-for="(o, j) in data.openChats" :key="j" type="button" class="cr-side-item" :class="{ active: j === idx }" @click="router.push('/chatroom/' + j)">
              <span class="chat-ico" :class="o.logo">{{ o.short }}</span>
              <span class="cr-side-text">
                <span class="cr-side-name">{{ o.title }}</span>
                <span class="cr-side-sub"><span class="online-dot"></span>{{ o.online.toLocaleString() }}명 · {{ o.tag }}</span>
              </span>
            </button>
          </div>
        </aside>
      </div>
    </div>
  </main>
</template>

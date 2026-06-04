<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { toast } from '../utils/toast.js'

const router = useRouter()
const data = useDataStore()

const tab = ref('openchat')   // openchat | board | study
const writeOpen = ref(false)
const writeCat = ref('자유')
const writeTitle = ref('')
const writeBody = ref('')
const writeErr = ref(false)

function openWrite() { writeOpen.value = true; document.body.style.overflow = 'hidden' }
function closeWrite() { writeOpen.value = false; document.body.style.overflow = ''; writeErr.value = false }
function submitPost() {
  const title = writeTitle.value.trim()
  const body = writeBody.value.trim()
  if (!title || !body) { writeErr.value = true; return }
  data.addPost({ title, body, cat: writeCat.value })
  writeTitle.value = ''; writeBody.value = ''
  closeWrite()
  tab.value = 'board'
  toast('글이 등록되었어요.')
}
</script>

<template>
  <main class="page active" id="page-community">
    <div class="container">
      <div class="page-header">
        <div class="breadcrumb">홈 <span class="sep">›</span> 커뮤니티</div>
        <h1 class="page-title">커뮤니티</h1>
        <p class="page-subtitle">같은 기업·직군을 준비하는 사람들과 정보를 나누고, 실시간 오픈채팅에 참여하세요.</p>
      </div>

      <div class="seg-tabs">
        <div class="seg-tab" :class="{ active: tab === 'openchat' }" @click="tab = 'openchat'">기업별 오픈톡</div>
        <div class="seg-tab" :class="{ active: tab === 'board' }" @click="tab = 'board'">자유게시판</div>
        <div class="seg-tab" :class="{ active: tab === 'study' }" @click="tab = 'study'">스터디 모집</div>
      </div>

      <!-- 오픈톡 -->
      <div v-show="tab === 'openchat'" class="comm-panel">
        <div class="card list-card">
          <div class="card-header"><h3 class="card-title">실시간 오픈채팅</h3><span class="text-sm text-muted">접속 많은순</span></div>
          <div v-for="(c, i) in data.openChats" :key="i" class="list-item chat-item" @click="router.push('/chatroom/' + i)">
            <div class="chat-ico" :class="c.logo">{{ c.short }}</div>
            <div class="list-item-text"><div class="list-item-title">{{ c.title }}</div><div class="list-item-sub"><span class="online-dot"></span>{{ c.online.toLocaleString() }}명 접속 중 · {{ c.tag }}</div></div>
            <span class="chat-enter">입장</span>
          </div>
        </div>
      </div>

      <!-- 자유게시판 -->
      <div v-show="tab === 'board'" class="comm-panel">
        <div class="card list-card">
          <div class="card-header"><h3 class="card-title">자유게시판</h3><a class="card-link" @click="openWrite">글쓰기 ›</a></div>
          <div v-for="(p, i) in data.boardPosts" :key="i" class="post-item" @click="router.push('/post/' + i)">
            <div class="post-head"><span class="badge" :class="p.cls">{{ p.cat }}</span><span class="post-co">{{ p.co }}</span></div>
            <div class="post-title">{{ p.title }}</div>
            <div class="post-meta"><span>{{ p.author }}</span><span class="dot-sep"></span><span>댓글 {{ p.cl.length }}</span><span class="dot-sep"></span><span>{{ p.time }}</span></div>
          </div>
        </div>
      </div>

      <!-- 스터디 -->
      <div v-show="tab === 'study'" class="comm-panel">
        <div class="study-grid">
          <div v-for="(s, i) in data.studies" :key="i" class="study-card" @click="router.push('/study/' + i)">
            <div class="study-tags"><span v-for="(t, ti) in s.tags" :key="ti" class="badge badge-outline">{{ t }}</span></div>
            <h3 class="study-title">{{ s.title }}</h3>
            <p class="study-desc">{{ s.desc }}</p>
            <div class="study-meta">
              <div><span class="study-meta-label">모집</span> {{ s.members }}/{{ s.max }}명</div>
              <div><span class="study-meta-label">일정</span> {{ s.schedule }}</div>
              <div><span class="study-meta-label">방장</span> 익명의 {{ s.leader }}</div>
            </div>
            <button class="btn btn-primary btn-block btn-sm" @click.stop="router.push('/study/' + i)">신청하기</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 글쓰기 모달 -->
    <div v-if="writeOpen" class="auth-overlay open" @click.self="closeWrite">
      <div class="auth-modal write-modal">
        <button class="auth-close" @click="closeWrite" aria-label="닫기"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12" /></svg></button>
        <h2 class="auth-title">글쓰기</h2>
        <p class="auth-sub">자유게시판은 닉네임이 익명으로 표시돼요.</p>
        <div class="auth-field"><label class="auth-label">분류</label>
          <div class="chip-group">
            <div v-for="([c], i) in data.boardCats" :key="i" class="chip" :class="{ active: writeCat === c }" @click="writeCat = c">{{ c }}</div>
          </div>
        </div>
        <div class="auth-field"><label class="auth-label">제목</label><input class="input" v-model="writeTitle" placeholder="제목을 입력하세요" maxlength="60" /></div>
        <div class="auth-field"><label class="auth-label">내용</label><textarea class="input write-textarea" v-model="writeBody" placeholder="질문이나 후기를 자유롭게 작성해 주세요"></textarea></div>
        <div class="form-err" :style="{ display: writeErr ? 'block' : 'none' }">제목과 내용을 모두 입력해 주세요.</div>
        <button class="btn btn-primary btn-block btn-lg" style="margin-top:4px;" @click="submitPost">등록하기</button>
      </div>
    </div>
  </main>
</template>

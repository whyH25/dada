<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from '../../../utils/toast.js'
import { fetchMyPosts } from '../../../api/postsApi.js'

const emit = defineEmits(['update:count'])
const router = useRouter()

const myPosts = ref([])
const myPostsLoading = ref(false)

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  myPostsLoading.value = true
  try {
    myPosts.value = await fetchMyPosts()
    emit('update:count', myPosts.value.length)
  } catch (e) {
    toast(e.message)
  } finally {
    myPostsLoading.value = false
  }
})
</script>

<template>
  <div>
    <div class="breadcrumb">마이페이지 <span class="sep">›</span> 계정 <span class="sep">›</span> 내가 쓴 글</div>
    <div class="flex-between" style="margin:4px 0 20px;">
      <div><h2 class="mp-h1">내가 쓴 글</h2><p class="mp-sub">커뮤니티에 작성한 게시글입니다. 총 {{ myPosts.length }}건</p></div>
    </div>
    <div class="card" style="padding:0; overflow:hidden;">
      <div v-if="myPostsLoading" style="padding:48px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
      <div v-else-if="!myPosts.length" style="padding:48px; text-align:center; color:var(--ink-400);">작성한 게시글이 없습니다.</div>
      <table v-else class="history-table">
        <thead><tr><th>분류</th><th>제목</th><th>날짜</th><th>댓글</th></tr></thead>
        <tbody>
          <tr v-for="p in myPosts" :key="p.postId" style="cursor:pointer;" @click="router.push('/community/posts/' + p.postId)">
            <td><span class="badge">{{ p.category }}</span></td>
            <td><strong>{{ p.title }}</strong></td>
            <td><span class="text-sm text-muted">{{ formatDate(p.createdAt) }}</span></td>
            <td><span class="text-sm text-muted">{{ p.commentCount }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

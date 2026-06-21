<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { fetchPost, createPost, updatePost } from '../api/postsApi.js'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const CATS = ['면접 후기', '질문', '스터디 모집', '기타']

const isEdit = !!route.params.id
const postId = route.params.id ? Number(route.params.id) : null

const defaultCat = CATS.includes(route.query.category) ? route.query.category : '기타'
const category = ref(defaultCat)
const title = ref('')
const content = ref('')
const anonymous = ref(false)
const submitting = ref(false)
const err = ref('')

onMounted(async () => {
  if (isEdit && postId) {
    const { post } = await fetchPost(postId)
    if (!post) { router.replace('/community/board'); return }
    if (post.userId !== auth.user?.userId) { router.replace('/community/board'); return }
    category.value = post.category
    title.value = post.title
    content.value = post.content || ''
    anonymous.value = post.anonymous ?? false
  }
})

async function submit() {
  err.value = ''
  if (!title.value.trim()) { err.value = '제목을 입력하세요.'; return }
  if (!content.value || content.value === '<p><br></p>') { err.value = '내용을 입력하세요.'; return }
  submitting.value = true
  try {
    const payload = { category: category.value, title: title.value.trim(), content: content.value, anonymous: anonymous.value }
    if (isEdit) {
      await updatePost(postId, payload)
      router.push('/community/board/' + postId)
    } else {
      const res = await createPost(payload)
      router.push('/community/board/' + res.postId)
    }
  } catch (e) {
    if (String(e.message) === '401') {
      auth.openLogin(() => submit())
      return
    }
    err.value = '저장 중 오류가 발생했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="page active" id="page-post-create">
    <div class="container container-narrow">
      <div class="page-header" style="border-bottom:none;padding-bottom:8px;margin-bottom:8px;">
        <div class="breadcrumb">
          홈 <span class="sep">›</span>
          커뮤니티 <span class="sep">›</span>
          <a @click="router.push('/community/board')" style="cursor:pointer;">자유게시판</a>
          <span class="sep">›</span> {{ isEdit ? '글 수정' : '글쓰기' }}
        </div>
      </div>

      <div class="write-form">
        <h2 class="write-title">{{ isEdit ? '글 수정' : '글쓰기' }}</h2>

        <div class="write-field">
          <label class="write-label">분류</label>
          <div class="cat-btns">
            <button
              v-for="c in CATS" :key="c"
              class="cat-btn"
              :class="{ active: category === c }"
              @click="category = c"
            >{{ c }}</button>
          </div>
        </div>

        <div class="write-field">
          <label class="write-label">제목 <span class="req">*</span></label>
          <input v-model="title" class="write-input" placeholder="제목을 입력하세요" maxlength="200" />
        </div>

        <div class="write-field">
          <label class="write-label">내용 <span class="req">*</span></label>
          <QuillEditor
            v-model:content="content"
            content-type="html"
            theme="snow"
            :toolbar="[
              [{ header: [1, 2, 3, false] }],
              ['bold', 'italic', 'underline', 'strike'],
              [{ color: [] }],
              [{ list: 'ordered' }, { list: 'bullet' }],
              ['blockquote', 'code-block'],
              ['link'],
              ['clean']
            ]"
            class="post-editor"
          />
        </div>

        <div v-if="err" class="write-err">{{ err }}</div>

        <label class="write-anon">
          <input type="checkbox" v-model="anonymous" />
          익명으로 게시
        </label>

        <div class="write-actions">
          <button class="btn btn-ghost" @click="router.push('/community/board')">취소</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submit">
            {{ submitting ? '저장 중...' : (isEdit ? '수정 완료' : '등록하기') }}
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.write-form { padding: 8px 0 60px; }
.write-title { font-size: 22px; font-weight: 800; color: var(--ink-900); margin-bottom: 24px; }

.write-field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 20px; }
.write-label { font-size: 13px; font-weight: 600; color: var(--ink-700, #374151); }
.req { color: #ef4444; }

.write-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  box-sizing: border-box;
}
.write-input:focus { border-color: var(--green-500, #308860); }

.cat-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.cat-btn {
  padding: 6px 16px; border-radius: 99px; font-size: 13px; font-weight: 500;
  border: 1px solid var(--border, #e5e7eb); background: #fff;
  cursor: pointer; color: var(--ink-600); transition: all 0.15s;
}
.cat-btn.active {
  background: var(--green-500, #308860); color: #fff; border-color: var(--green-500, #308860);
}

.post-editor { min-height: 300px; }

.write-err {
  color: #ef4444;
  font-size: 13px;
  margin-bottom: 12px;
}

.write-anon {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--ink-600, #4b5563);
  cursor: pointer;
  margin-bottom: 4px;
}
.write-anon input { cursor: pointer; }

.write-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
</style>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchPost, deletePost, togglePostLike, fetchComments, addComment, updateComment, deleteComment } from '../api/postsApi.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const CAT_CLS = { '면접 후기': 'badge-blue', '스터디 모집': 'badge-purple', '기타': '' }

const post = ref(null)
const related = ref([])
const comments = ref([])
const loading = ref(true)
const notFound = ref(false)
const commentText = ref('')
const isAnonymous = ref(false)
const submittingComment = ref(false)

// 댓글 수정 상태
const editingCommentId = ref(null)
const editingText = ref('')
const editingAnonymous = ref(false)

const postId = Number(route.params.id)

async function load() {
  loading.value = true
  try {
    const [{ post: p, related: rel }, c] = await Promise.all([fetchPost(postId), fetchComments(postId)])
    if (!p) { notFound.value = true; loading.value = false; return }
    post.value = p
    related.value = rel
    comments.value = c
    loading.value = false
  } catch {
    notFound.value = true
    loading.value = false
  }
}

async function handleLike() {
  if (!auth.isLoggedIn) { auth.openLogin(); return }
  const res = await togglePostLike(postId)
  post.value.liked = res.liked
  post.value.likes += res.liked ? 1 : -1
}

async function handleDelete() {
  if (!confirm('게시글을 삭제하시겠습니까?')) return
  await deletePost(postId)
  router.push('/community/board')
}

const commentInputRef = ref(null)

function autoResizeComment(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

async function submitComment() {
  if (!auth.isLoggedIn) { auth.openLogin(); return }
  const text = commentText.value.trim()
  if (!text) return
  submittingComment.value = true
  try {
    await addComment(postId, text, isAnonymous.value)
    commentText.value = ''
    if (commentInputRef.value) commentInputRef.value.style.height = 'auto'
    comments.value = await fetchComments(postId)
    post.value.commentCount = comments.value.length
  } finally {
    submittingComment.value = false
  }
}

function startEditComment(c) {
  editingCommentId.value = c.commentId
  editingText.value = c.content
  editingAnonymous.value = c.anonymous
}

function cancelEditComment() {
  editingCommentId.value = null
  editingText.value = ''
  editingAnonymous.value = false
}

async function submitEditComment(cid) {
  const text = editingText.value.trim()
  if (!text) return
  await updateComment(postId, cid, text, editingAnonymous.value)
  comments.value = await fetchComments(postId)
  cancelEditComment()
}

async function handleDeleteComment(cid) {
  if (!confirm('댓글을 삭제하시겠습니까?')) return
  await deleteComment(postId, cid)
  comments.value = await fetchComments(postId)
  post.value.commentCount = comments.value.length
}

function formatDate(d) {
  if (!d) return ''
  return String(d).slice(0, 16).replace('T', ' ')
}


const isOwner = () => auth.user && post.value && auth.user.userId === post.value.userId
const isEdited = computed(() => {
  if (!post.value?.updatedAt) return false
  return post.value.updatedAt.slice(0, 16) !== post.value.createdAt.slice(0, 16)
})

onMounted(load)
</script>

<template>
  <main class="page active" id="page-post-detail">
    <div class="container container-narrow">

      <div class="page-header" style="border-bottom:none;padding-bottom:8px;margin-bottom:8px;">
        <div class="breadcrumb">
          홈 <span class="sep">›</span>
          <a @click="router.push('/community/board')" style="cursor:pointer;">커뮤니티</a>
          <span class="sep">›</span> {{ post?.category ?? '게시글' }}
        </div>
      </div>

      <div v-if="loading" class="pd-loading">불러오는 중...</div>
      <div v-else-if="notFound" class="pd-not-found">
        <div class="pd-not-found-icon">🔍</div>
        <h2>게시글을 찾을 수 없습니다</h2>
        <p>삭제되었거나 존재하지 않는 게시글입니다.</p>
        <button class="btn btn-ghost" @click="router.push('/community/board')">목록으로</button>
      </div>

      <template v-else>
        <!-- 게시글 헤더 -->
        <div class="pd-head">
          <div class="pd-badges">
            <span class="badge badge-sm" :class="CAT_CLS[post.category] || ''">{{ post.category }}</span>
          </div>
          <h1 class="pd-title">{{ post.title }}</h1>
          <div class="pd-meta-row">
            <div class="pd-meta">
              <span class="pd-meta-author">{{ post.authorName }}</span>
              <span class="pd-dot"></span>
              <span>{{ formatDate(post.createdAt) }}</span>
              <span v-if="isEdited" class="edited-mark">(수정됨)</span>
              <span class="pd-dot"></span>
              <span>조회 {{ post.views }}</span>
              <span class="pd-dot"></span>
              <span>댓글 {{ post.commentCount }}</span>
            </div>
            <div v-if="isOwner()" class="pd-owner-actions">
              <button class="pd-action-btn" @click="router.push('/community/board/' + postId + '/edit')">수정</button>
              <button class="pd-action-btn danger" @click="handleDelete">삭제</button>
            </div>
          </div>
        </div>

        <!-- 본문 -->
        <div class="pd-body ql-content" v-html="post.content"></div>

        <!-- 좋아요 -->
        <div class="pd-like-row">
          <button class="like-btn" :class="{ on: post.liked }" @click="handleLike">
            <svg width="16" height="16" viewBox="0 0 24 24"
              :fill="post.liked ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            좋아요 {{ post.likes }}
          </button>
        </div>

        <!-- 댓글 -->
        <div class="pd-comments">
          <h3 class="pd-comments-title">댓글 <span class="pd-comments-count">{{ comments.length }}</span></h3>

          <!-- 댓글 입력 (목록 위에) -->
          <div class="pd-comment-write">
            <textarea
              ref="commentInputRef"
              v-model="commentText"
              class="ci-input"
              placeholder="댓글을 작성해보세요."
              rows="1"
              @input="autoResizeComment"
              @keydown.enter.exact.prevent="submitComment"
            ></textarea>
            <div class="ci-bottom">
              <label class="ci-anon">
                <input type="checkbox" v-model="isAnonymous" />
                <span>익명</span>
              </label>
              <button class="ci-submit" :disabled="submittingComment" @click="submitComment">
                {{ submittingComment ? '...' : '등록' }}
              </button>
            </div>
          </div>

          <!-- 댓글 목록 -->
          <div v-if="comments.length === 0" class="pd-comment-empty">
            첫 댓글을 남겨보세요.
          </div>

          <div v-for="c in comments" :key="c.commentId" class="pd-comment">
            <div class="cr-avatar">{{ c.authorName[0] }}</div>
            <div class="cr-content">
              <div class="cr-name">
                {{ c.authorName }}
                <span class="cr-time">{{ formatDate(c.createdAt) }}</span>
                <span v-if="c.updatedAt && c.updatedAt.slice(0,16) !== c.createdAt.slice(0,16)" class="edited-mark">(수정됨)</span>
              </div>
              <!-- 수정 중인 댓글 -->
              <template v-if="editingCommentId === c.commentId">
                <textarea class="cr-edit-textarea" v-model="editingText" rows="2"></textarea>
                <div class="cr-edit-actions">
                  <label class="cr-edit-anon">
                    <input type="checkbox" v-model="editingAnonymous" />
                    <span>익명</span>
                  </label>
                  <button class="cr-edit-btn" @click="submitEditComment(c.commentId)">저장</button>
                  <button class="cr-edit-btn cancel" @click="cancelEditComment">취소</button>
                </div>
              </template>
              <div v-else class="cr-text">{{ c.content }}</div>
            </div>
            <div v-if="auth.user && auth.user.userId === c.userId" class="cr-owner-btns">
              <button class="cr-edit-action" @click="startEditComment(c)">수정</button>
              <button class="cr-del" @click="handleDeleteComment(c.commentId)">삭제</button>
            </div>
          </div>
        </div>

        <!-- 목록으로 -->
        <div class="pd-actions">
          <button class="btn btn-secondary" @click="router.push('/community/board')">목록으로 →</button>
        </div>

      </template>
    </div>
  </main>
</template>

<style scoped>
.pd-loading { text-align: center; padding: 80px 0; color: #9aa6a0; }

.pd-not-found { text-align: center; padding: 80px 0; color: #9aa6a0; }
.pd-not-found-icon { font-size: 40px; margin-bottom: 16px; }
.pd-not-found h2 { font-size: 20px; font-weight: 700; margin-bottom: 8px; color: #14241b; }
.pd-not-found p { font-size: 14px; margin-bottom: 24px; }

/* ── 헤더 ── */
.pd-head { padding: 8px 0 24px; }
.pd-badges { margin-bottom: 12px; }
.pd-title {
  font-size: 26px; font-weight: 800;
  color: #14241b; line-height: 1.4;
  margin: 0 0 16px; letter-spacing: -0.02em;
}
.pd-meta-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.pd-meta {
  font-size: 13px; color: #9aa6a0;
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
}
.pd-meta-author { font-weight: 600; color: #4b5563; }
.pd-dot { width: 3px; height: 3px; border-radius: 50%; background: #d0d6d3; flex-shrink: 0; }
.edited-mark { font-size: 11px; color: #9aa6a0; font-weight: 400; }

.pd-owner-actions { display: flex; gap: 6px; flex-shrink: 0; }
.pd-action-btn {
  padding: 5px 12px; font-size: 12px; border-radius: 7px;
  background: none; border: 1px solid #e5e9e7;
  color: #6b7280; cursor: pointer; font-family: inherit; transition: all 0.15s;
}
.pd-action-btn:hover { background: #f7f9f8; }
.pd-action-btn.danger { color: #e0667a; border-color: #f5cdd4; }
.pd-action-btn.danger:hover { background: #fdf1f3; }

/* ── 본문 ── */
.pd-body {
  font-size: 15.5px; line-height: 1.85;
  color: #2a3a31; min-height: 120px; margin-bottom: 8px;
}
.pd-body :deep(p) { margin: 0 0 16px; }
.pd-body :deep(h1), .pd-body :deep(h2), .pd-body :deep(h3) { font-weight: 700; margin: 20px 0 8px; color: #14241b; }
.pd-body :deep(ul), .pd-body :deep(ol) { padding-left: 24px; margin-bottom: 12px; }
.pd-body :deep(blockquote) { border-left: 4px solid #2f8f63; padding-left: 14px; margin: 14px 0; color: #5d7a66; font-style: italic; }
.pd-body :deep(pre) { background: #f7f9f8; border-radius: 8px; padding: 12px 14px; font-size: 13px; overflow-x: auto; margin-bottom: 12px; }
.pd-body :deep(a) { color: #2f8f63; text-decoration: underline; }

/* ── 좋아요 ── */
.pd-like-row { display: flex; justify-content: center; padding: 28px 0; }
.like-btn {
  display: flex; align-items: center; gap: 7px;
  padding: 10px 24px; border-radius: 999px;
  border: 1px solid #e5e9e7;
  background: #fff; cursor: pointer;
  font-size: 14px; font-weight: 600;
  color: #6b7280; font-family: inherit;
  transition: all 0.15s;
}
.like-btn.on { background: #fdf1f3; color: #e0667a; border-color: #f5cdd4; }
.like-btn:hover { border-color: #e0667a; color: #e0667a; }

/* ── 댓글 ── */
.pd-comments {
  background: #f7f9f8;
  border-radius: 16px;
  padding: 26px 24px;
  margin-top: 16px;
}
.pd-comments-title { font-size: 15px; font-weight: 700; color: #14241b; margin: 0 0 18px; }
.pd-comments-count { color: #2f8f63; }
.pd-comment-empty { color: #9aa6a0; font-size: 14px; padding: 16px 0; }

.pd-comment {
  display: flex; gap: 12px;
  padding: 14px 16px; margin-bottom: 8px;
  background: #fff; border-radius: 12px;
  align-items: flex-start;
}
.cr-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: #dcefe3; color: #15803d;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.cr-content { flex: 1; min-width: 0; }
.cr-name { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 4px; }
.cr-time { font-size: 11px; font-weight: 400; color: #9aa6a0; margin-left: 8px; }
.cr-text { font-size: 14px; color: #2a3a31; line-height: 1.6; }

.cr-edit-textarea {
  width: 100%; padding: 8px 10px;
  border: 1px solid #d8e6dc; border-radius: 8px;
  font-size: 14px; font-family: inherit;
  resize: vertical; outline: none; box-sizing: border-box;
}
.cr-edit-actions { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.cr-edit-anon { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #9aa6a0; cursor: pointer; margin-right: auto; }
.cr-edit-btn {
  padding: 4px 12px; font-size: 12px; border-radius: 7px;
  border: none; background: #2f8f63; color: #fff;
  cursor: pointer; font-weight: 600; font-family: inherit;
}
.cr-edit-btn.cancel { background: #fff; color: #6b7280; border: 1px solid #e5e9e7; }

.cr-owner-btns { display: flex; gap: 4px; flex-shrink: 0; align-items: center; }
.cr-edit-action { font-size: 12px; color: #9aa6a0; background: none; border: none; cursor: pointer; padding: 2px 6px; }
.cr-edit-action:hover { color: #374151; }
.cr-del { font-size: 12px; color: #9aa6a0; background: none; border: none; cursor: pointer; padding: 2px 6px; }
.cr-del:hover { color: #e0667a; }

/* ── 댓글 입력 ── */
.pd-comment-write {
  display: flex; flex-direction: column;
  background: #fff; border: 1.5px solid #d8e6dc;
  border-radius: 12px; padding: 14px 14px 10px 16px;
  margin-bottom: 22px; transition: border-color 0.15s;
}
.pd-comment-write:focus-within { border-color: #2f8f63; }
.ci-anon {
  display: flex; align-items: center; gap: 5px;
  font-size: 13px; color: #9aa6a0; cursor: pointer;
  user-select: none; flex-shrink: 0; white-space: nowrap;
}
.ci-anon input { cursor: pointer; accent-color: #2f8f63; }
.ci-input {
  width: 100%; border: none; outline: none; resize: none; overflow: hidden;
  font-size: 14px; font-family: inherit; line-height: 1.6;
  color: #14241b; background: transparent; padding: 0;
  min-height: 24px; max-height: 200px; box-sizing: border-box;
}
.ci-input::placeholder { color: #aab5ae; }
.ci-bottom {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 10px; margin-top: 6px; border-top: 1px solid #eef1ef;
}
.ci-submit {
  -webkit-appearance: none; appearance: none; box-sizing: border-box;
  display: flex; align-items: center; justify-content: center;
  height: 40px; padding: 0 20px; margin: 0;
  font-size: 13px; font-weight: 700; font-family: inherit;
  background: #2f8f63; color: #fff;
  border: none; border-radius: 9px; cursor: pointer;
  white-space: nowrap; flex-shrink: 0; line-height: 1;
  transition: background 0.15s;
}
.ci-submit:hover { background: #268054; }
.ci-submit:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── 목록으로 ── */
.pd-actions {
  margin-top: 32px; padding-top: 20px;
  border-top: 1px solid #eef1ef;
  text-align: right; padding-bottom: 36px;
}
.pd-actions .btn {
  padding: 10px 20px; border-radius: 10px;
  border: 1px solid #e5e9e7; background: #fff;
  color: #4b5563; font-size: 14px; font-weight: 600;
  cursor: pointer; font-family: inherit;
  transition: background 0.15s;
}
.pd-actions .btn:hover { background: #f7f9f8; }

/* ── 배지 ── */
.badge-sm { font-size: 12px; padding: 3px 10px; border-radius: 999px; }
.badge-blue   { background: #eff6ff; color: #1d4ed8; }
.badge-green  { background: #f0fdf4; color: #15803d; }
.badge-purple { background: #faf5ff; color: #7c3aed; }
</style>

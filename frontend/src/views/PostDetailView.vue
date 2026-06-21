<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { fetchPost, deletePost, togglePostLike, fetchComments, addComment, updateComment, deleteComment } from '../api/postsApi.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const CAT_CLS = { '면접 후기': 'badge-blue', '질문': 'badge-green', '스터디 모집': 'badge-purple', '기타': '' }

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

async function submitComment() {
  if (!auth.isLoggedIn) { auth.openLogin(); return }
  const text = commentText.value.trim()
  if (!text) return
  submittingComment.value = true
  try {
    await addComment(postId, text, isAnonymous.value)
    commentText.value = ''
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

function formatDateShort(d) {
  if (!d) return ''
  return String(d).slice(0, 10).replace(/-/g, '.')
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
          커뮤니티 <span class="sep">›</span>
          <a @click="router.push('/community/board')" style="cursor:pointer;">자유게시판</a>
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
              <span>{{ post.authorName }}</span>
              <span class="dot-sep"></span>
              <span>{{ formatDate(post.createdAt) }}</span>
              <span v-if="isEdited" class="edited-mark">(수정됨)</span>
              <span class="dot-sep"></span>
              <span>조회 {{ post.views }}</span>
              <span class="dot-sep"></span>
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
          <h3 class="pd-comments-title">댓글 {{ comments.length }}</h3>

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

          <!-- 댓글 입력 -->
          <div class="pd-comment-input">
            <textarea
              v-model="commentText"
              class="ci-textarea"
              placeholder="댓글을 입력하세요"
              rows="3"
              @keydown.ctrl.enter="submitComment"
            ></textarea>
            <div class="ci-footer">
              <label class="ci-anon">
                <input type="checkbox" v-model="isAnonymous" />
                <span>익명</span>
              </label>
              <button class="ci-submit" :disabled="submittingComment" @click="submitComment">
                {{ submittingComment ? '등록 중...' : '댓글 등록' }}
              </button>
            </div>
          </div>
        </div>

        <div class="pd-actions">
          <button class="btn btn-ghost" @click="router.push('/community/board')">목록으로</button>
        </div>

        <!-- 연관 게시글 -->
        <div v-if="related.length > 0" class="pd-related">
          <h3 class="pd-related-title">더보기</h3>
          <div class="related-list">
            <div
              v-for="r in related" :key="r.postId"
              class="related-item"
              @click="router.push('/community/board/' + r.postId)"
            >
              <span class="related-title">{{ r.title }}</span>
              <span class="related-meta">{{ formatDateShort(r.createdAt) }}</span>
            </div>
          </div>
        </div>

      </template>
    </div>
  </main>
</template>

<style scoped>
.pd-loading { text-align: center; padding: 80px 0; color: var(--ink-400); }

.pd-not-found {
  text-align: center;
  padding: 80px 0;
  color: var(--ink-500);
}
.pd-not-found-icon { font-size: 40px; margin-bottom: 16px; }
.pd-not-found h2 { font-size: 20px; font-weight: 700; margin-bottom: 8px; color: var(--ink-800); }
.pd-not-found p { font-size: 14px; margin-bottom: 24px; }

.pd-head { padding: 8px 0 16px; }
.pd-badges { margin-bottom: 10px; }
.pd-title { font-size: 22px; font-weight: 800; color: var(--ink-900); line-height: 1.4; margin-bottom: 12px; }
.pd-meta-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.pd-meta { font-size: 13px; color: var(--ink-400); display: flex; gap: 4px; align-items: center; flex-wrap: wrap; }
.edited-mark { font-size: 11px; color: var(--ink-400); font-weight: 400; }
.pd-owner-actions { display: flex; gap: 6px; flex-shrink: 0; }
.pd-action-btn {
  padding: 4px 10px; font-size: 12px; border-radius: 6px;
  background: none; border: 1px solid var(--border, #e5e7eb);
  color: var(--ink-500); cursor: pointer; transition: all 0.15s;
}
.pd-action-btn:hover { background: var(--ink-50); }
.pd-action-btn.danger { color: #ef4444; border-color: #fecaca; }
.pd-action-btn.danger:hover { background: #fef2f2; }
.pd-divider { border: none; border-top: 1px solid var(--ink-150, #e5e7eb); margin: 20px 0; }

.pd-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-800);
  min-height: 120px;
  margin-bottom: 8px;
}
.pd-body :deep(h1), .pd-body :deep(h2), .pd-body :deep(h3) { font-weight: 700; margin: 18px 0 8px; }
.pd-body :deep(p) { margin: 0 0 10px; }
.pd-body :deep(ul), .pd-body :deep(ol) { padding-left: 24px; margin-bottom: 10px; }
.pd-body :deep(blockquote) { border-left: 4px solid var(--green-400, #4ade80); padding-left: 14px; margin: 14px 0; color: var(--ink-600); font-style: italic; }
.pd-body :deep(pre) { background: #f8f9fa; border-radius: 6px; padding: 12px 14px; font-size: 13px; overflow-x: auto; margin-bottom: 12px; }
.pd-body :deep(a) { color: var(--green-600, #16a34a); text-decoration: underline; }

.pd-like-row { display: flex; justify-content: center; padding: 8px 0; }
.like-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 20px; border-radius: 99px;
  border: 1px solid var(--border, #e5e7eb);
  background: #fff; cursor: pointer; font-size: 14px;
  color: var(--ink-500); transition: all 0.15s;
}
.like-btn.on { background: #fef2f2; color: #ef4444; border-color: #fecaca; }
.like-btn:hover { border-color: #ef4444; color: #ef4444; }

/* 댓글 */
.pd-comments { margin: 8px 0 24px; }
.pd-comments-title { font-size: 16px; font-weight: 700; color: var(--ink-900); margin-bottom: 16px; }
.pd-comment-empty { color: var(--ink-400); font-size: 14px; padding: 16px 0; }

.pd-comment {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--ink-100, #f3f4f6);
  align-items: flex-start;
}
.cr-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: var(--green-100, #d1fae5);
  color: var(--green-700, #15803d);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.cr-content { flex: 1; }
.cr-name { font-size: 13px; font-weight: 600; color: var(--ink-700); margin-bottom: 4px; }
.cr-time { font-size: 11px; font-weight: 400; color: var(--ink-400); margin-left: 8px; }
.cr-text { font-size: 14px; color: var(--ink-800); line-height: 1.6; }

.cr-edit-textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
}
.cr-edit-actions { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.cr-edit-anon { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--ink-600); cursor: pointer; margin-right: auto; }
.cr-edit-btn {
  padding: 4px 12px; font-size: 12px; border-radius: 6px;
  border: 1px solid var(--border, #e5e7eb);
  background: var(--green-500, #308860); color: #fff;
  cursor: pointer; font-weight: 600;
}
.cr-edit-btn.cancel { background: #fff; color: var(--ink-500); }

.cr-owner-btns { display: flex; flex-direction: row; gap: 4px; flex-shrink: 0; align-items: center; }
.cr-edit-action {
  font-size: 12px; color: var(--ink-400); background: none;
  border: none; cursor: pointer; padding: 2px 6px;
}
.cr-edit-action:hover { color: var(--ink-700); }
.cr-del {
  font-size: 12px; color: var(--ink-400); background: none;
  border: none; cursor: pointer; padding: 2px 6px;
}
.cr-del:hover { color: #ef4444; }

/* 댓글 입력 */
.pd-comment-input {
  display: block;
  width: 100%;
  margin-top: 20px;
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
  overflow: hidden;
  box-sizing: border-box;
}
.ci-textarea {
  display: block;
  width: 100%;
  padding: 12px 14px;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.6;
  box-sizing: border-box;
}
.ci-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 12px;
  border-top: 1px solid var(--ink-100, #f3f4f6);
  background: var(--ink-50, #f8f9fa);
  box-sizing: border-box;
}
.ci-anon { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ink-600); cursor: pointer; user-select: none; }
.ci-anon input { cursor: pointer; }
.ci-submit {
  padding: 6px 16px; font-size: 13px; font-weight: 600;
  background: var(--green-500, #308860); color: #fff;
  border: none; border-radius: 6px; cursor: pointer;
  white-space: nowrap; flex-shrink: 0; transition: background 0.15s;
}
.ci-submit:hover { background: var(--green-600, #256b4a); }
.ci-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.pd-actions { margin-top: 8px; }

/* 연관 게시글 */
.pd-related {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border, #e5e7eb);
}
.pd-related-title { font-size: 14px; font-weight: 700; color: var(--ink-700); margin-bottom: 12px; }
.related-list { display: flex; flex-direction: column; }
.related-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--ink-100, #f3f4f6);
  cursor: pointer;
  transition: color 0.12s;
}
.related-item:hover .related-title { color: var(--green-600, #16a34a); }
.related-title {
  font-size: 14px;
  color: var(--ink-800);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.related-meta { font-size: 12px; color: var(--ink-400); flex-shrink: 0; margin-left: 12px; }

/* 배지 */
.badge-sm { font-size: 12px; padding: 3px 10px; border-radius: 99px; }
.badge-blue   { background: #eff6ff; color: #1d4ed8; }
.badge-green  { background: #f0fdf4; color: #15803d; }
.badge-purple { background: #faf5ff; color: #7c3aed; }
</style>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from '../../utils/toast.js'
import { useAuthStore } from '../../stores/auth.js'
import { fetchMyInterviewRooms } from '../../api/mypageApi.js'
import { deleteInterviewRoomApi } from '../../api/interviewRoomApi.js'
import ReportTab from './tabs/ReportTab.vue'
import ReportDetailTab from './tabs/ReportDetailTab.vue'
import DocsTab from './tabs/DocsTab.vue'
import ProfileTab from './tabs/ProfileTab.vue'
import MyPostsTab from './tabs/MyPostsTab.vue'
import BillingTab from './tabs/BillingTab.vue'

const route = useRoute()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const section = ref('reports')
const myRooms = ref([])
const roomsLoading = ref(false)
const currentRoom = ref(null)

const docCounts = reactive({ resume: 0, portfolio: 0 })
const myPostsCount = ref(0)

function gotoMy(s) { section.value = s; window.scrollTo(0, 0) }

async function loadMyRooms() {
  roomsLoading.value = true
  try {
    myRooms.value = await fetchMyInterviewRooms()
  } catch (e) {
    toast(e.message)
  } finally {
    roomsLoading.value = false
  }
}

async function deleteRoom(room) {
  if (!window.confirm('이 면접 기록과 리포트를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.')) return
  try {
    await deleteInterviewRoomApi(room.roomId)
    myRooms.value = myRooms.value.filter(r => r.roomId !== room.roomId)
    toast('면접 기록이 삭제되었습니다.')
  } catch (e) {
    toast(e.message)
  }
}

function openRoom(room) {
  currentRoom.value = room
  section.value = 'report-detail'
  window.scrollTo(0, 0)
}

onMounted(() => {
  if (route.query.section) section.value = route.query.section
  loadMyRooms()
})
</script>

<template>
  <main class="page active" id="page-mypage">
    <div class="container">
      <div class="mypage">
        <!-- Side menu -->
        <aside class="side-menu">
          <div class="side-profile">
            <div class="side-profile-avatar">{{ user?.userName?.charAt(0) }}</div>
            <div class="side-profile-name">{{ user?.userName }}</div>
            <div class="side-profile-email">{{ user?.userEmail }}</div>
          </div>
          <div class="side-section">
            <div class="side-section-title">면접 관리</div>
            <div class="side-item" :class="{ active: section === 'reports' || section === 'report-detail' }" @click="gotoMy('reports')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6M9 13h6M9 17h4" /></svg>
              리포트 <span class="count">{{ myRooms.length }}</span>
            </div>
          </div>
          <div class="side-section">
            <div class="side-section-title">서류 관리</div>
            <div class="side-item" :class="{ active: section === 'resume' }" @click="gotoMy('resume')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6" /></svg>
              이력서/자기소개서 <span class="count">{{ docCounts.resume }}</span>
            </div>
            <div class="side-item" :class="{ active: section === 'portfolio' }" @click="gotoMy('portfolio')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" /><circle cx="8.5" cy="8.5" r="1.5" /><path d="m21 15-5-5L5 21" /></svg>
              포트폴리오 <span class="count">{{ docCounts.portfolio }}</span>
            </div>
          </div>
          <div class="side-section" style="border-bottom: none;">
            <div class="side-section-title">계정</div>
            <div class="side-item" :class="{ active: section === 'account' }" @click="gotoMy('account')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
              회원정보
            </div>
            <div class="side-item" :class="{ active: section === 'my-posts' }" @click="gotoMy('my-posts')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M8 12h8M8 16h5"/></svg>
              내가 쓴 글 <span class="count">{{ myPostsCount }}</span>
            </div>
            <div class="side-item" :class="{ active: section === 'billing' }" @click="gotoMy('billing')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2" /><path d="M2 10h20" /></svg>
              구독 및 결제
            </div>
          </div>
        </aside>

        <!-- Dynamic content -->
        <div id="mypage-content">
          <ReportTab
            v-if="section === 'reports'"
            :my-rooms="myRooms"
            :rooms-loading="roomsLoading"
            @open-room="openRoom"
            @delete-room="deleteRoom"
          />
          <ReportDetailTab
            v-else-if="section === 'report-detail' && currentRoom"
            :current-room="currentRoom"
            :my-rooms="myRooms"
            @back="gotoMy('reports')"
          />
          <DocsTab
            v-else-if="section === 'resume' || section === 'portfolio'"
            :key="section"
            :type="section"
            @update:count="(n) => docCounts[section] = n"
          />
          <ProfileTab v-else-if="section === 'account'" />
          <MyPostsTab
            v-else-if="section === 'my-posts'"
            @update:count="(n) => myPostsCount = n"
          />
          <BillingTab v-else-if="section === 'billing'" />
        </div>
      </div>
    </div>
  </main>
</template>

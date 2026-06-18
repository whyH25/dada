<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataStore } from '../stores/data.js'
import { useFlowStore } from '../stores/flow.js'
import { useDeviceCheck } from '../composables/useDeviceCheck.js'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const data = useDataStore()
const flow = useFlowStore()
const auth = useAuthStore()

const showTicketModal = ref(false)

const idx = computed(() => Number(route.params.id) || 0)
const r = computed(() => data.rooms[idx.value])
const info = computed(() => data.infoFor(r.value.co))
const di = computed(() => data.diffInfo[r.value.diff] || data.diffInfo['중'])

// 환경 체크 단계 표시 여부
const showEnvCheck = ref(false)

const {
  videoEl, camStatus, micStatus, speakerStatus,
  camOn, micOn, volumeLevel, camLabel, micLabel,
  startDeviceCheck, stopDeviceCheck, toggleCam, toggleMic, testSpeaker,
} = useDeviceCheck()

function goToEnvCheck() {
  showEnvCheck.value = true
  startDeviceCheck()
}

function goBack() {
  showEnvCheck.value = false
  stopDeviceCheck()
}

function startInterview() {
  if ((auth.user?.ticketCount ?? 0) <= 0) {
    showTicketModal.value = true
    return
  }
  flow.currentRoom = idx.value
  stopDeviceCheck()
  router.push('/prep')
}

onUnmounted(() => stopDeviceCheck())
</script>

<template>
  <main class="page active" id="page-room-intro">
    <div class="container" style="max-width:840px;">

      <!-- ── 면접방 소개 ── -->
      <template v-if="!showEnvCheck">
        <div class="page-header" style="border-bottom:none; padding-bottom:8px; margin-bottom:8px;">
          <div class="breadcrumb">홈 <span class="sep">›</span> <a @click="router.push('/rooms')" style="cursor:pointer;">100대기업 면접방</a> <span class="sep">›</span> {{ r.co }}</div>
        </div>
        <div class="ri-hero">
          <div class="company-logo" :class="r.logo" style="width:56px;height:56px;font-size:22px;border-radius:12px;">{{ r.short }}</div>
          <div class="ri-hero-text">
            <div class="ri-co">{{ r.co }} <span class="ri-industry">{{ info.industry }}</span></div>
            <h1 class="ri-title">{{ r.title }}</h1>
            <div class="ri-role">{{ r.role }}</div>
          </div>
          <span v-if="r.mine" class="badge badge-green" style="align-self:flex-start;">참여완료 · {{ r.sessions }}회</span>
          <span v-else class="badge badge-outline" style="align-self:flex-start;">진행 전</span>
        </div>

        <div class="ri-info-grid">
          <div class="ri-info"><div class="ri-info-label">면접 유형</div><div class="ri-info-val">다대다 (면접관 2 : 지원자 3)</div></div>
          <div class="ri-info"><div class="ri-info-label">난이도</div><div class="ri-info-val">{{ di.label }}</div></div>
          <div class="ri-info"><div class="ri-info-label">면접관</div><div class="ri-info-val">AI 면접관 2명</div></div>
          <div class="ri-info"><div class="ri-info-label">경쟁 지원자</div><div class="ri-info-val">AI 지원자 2명 + 본인</div></div>
          <div class="ri-info"><div class="ri-info-label">질문 구성</div><div class="ri-info-val">기출 기반 8문항</div></div>
          <div class="ri-info"><div class="ri-info-label">예상 소요</div><div class="ri-info-val">30 ~ 40분</div></div>
        </div>

        <div class="ri-section">
          <h3 class="ri-section-title">이런 면접이에요</h3>
          <p class="ri-section-desc">{{ di.desc }}</p>
          <ul class="ri-list"><li v-for="(s, i) in info.style" :key="i">{{ s }}</li></ul>
        </div>

        <div class="ri-section">
          <h3 class="ri-section-title">평가 항목</h3>
          <div class="chip-group" style="pointer-events:none;">
            <div class="chip">직무 전문성</div><div class="chip">논리적 사고</div><div class="chip">커뮤니케이션</div><div class="chip">조직 적합성</div><div class="chip">압박 대응</div>
          </div>
        </div>

        <div class="ri-actions">
          <button class="btn btn-ghost" @click="router.push('/rooms')">목록으로</button>
          <button class="btn btn-primary btn-lg" @click="goToEnvCheck">
            면접 시작하기
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </button>
        </div>
      </template>

      <!-- ── 응시 환경 체크 ── -->
      <template v-else>
        <div class="page-header" style="border-bottom:none; padding-bottom:8px; margin-bottom:8px;">
          <div class="breadcrumb">홈 <span class="sep">›</span> <a @click="router.push('/rooms')" style="cursor:pointer;">100대기업 면접방</a> <span class="sep">›</span> {{ r.co }} <span class="sep">›</span> 응시 환경 체크</div>
        </div>

        <div class="check-layout">
          <div>
            <div class="form-section">
              <div class="form-section-head">
                <h3 class="form-section-title">응시 환경 체크</h3>
                <p class="form-section-sub">카메라와 마이크가 정상적으로 작동하는지 확인하세요.</p>
              </div>
              <div class="form-section-body" style="padding-bottom: 22px;">

                <div class="check-screen">
                  <div class="check-cam" style="position:relative;overflow:hidden;">
                    <video
                      v-show="camStatus === 'ok' && camOn"
                      ref="videoEl"
                      autoplay
                      playsinline
                      muted
                      style="width:100%;height:100%;object-fit:cover;border-radius:12px;transform:scaleX(-1)"
                    />
                    <div v-show="camStatus !== 'ok' || !camOn" class="you-circle">
                      {{ camStatus === 'error' ? '✕' : '나' }}
                    </div>
                    <div class="check-overlay">
                      <span class="live-dot" :style="{ background: camStatus === 'ok' ? 'var(--green-500)' : 'var(--ink-300)' }"></span>
                      {{ camStatus === 'pending' ? '카메라 연결 중...' : camStatus === 'error' ? '카메라 오류' : camOn ? '카메라 미리보기' : '카메라 꺼짐' }}
                    </div>
                  </div>
                  <div class="check-controls">
                    <div class="check-control" :title="micOn ? '마이크 음소거' : '마이크 켜기'" :style="{ opacity: micOn ? 1 : 0.4 }" @click="toggleMic">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" /><path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v3M8 22h8" /></svg>
                    </div>
                    <div class="check-control" :title="camOn ? '카메라 끄기' : '카메라 켜기'" :style="{ opacity: camOn ? 1 : 0.4 }" @click="toggleCam">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m23 7-7 5 7 5z" /><rect x="1" y="5" width="15" height="14" rx="2" /></svg>
                    </div>
                  </div>
                </div>

                <div class="check-list">
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">카메라 — {{ camLabel }}</div>
                      <div class="check-item-sub">{{ camStatus === 'pending' ? '권한 요청 중...' : camStatus === 'error' ? '카메라에 접근할 수 없습니다.' : '영상 출력 확인됨' }}</div>
                    </div>
                    <span class="check-item-status">
                      <span class="badge-dot" :style="{ background: camStatus === 'ok' ? 'var(--green-500)' : camStatus === 'error' ? 'var(--red-500)' : 'var(--ink-300)' }"></span>
                      {{ camStatus === 'ok' ? '정상' : camStatus === 'error' ? '오류' : '대기 중' }}
                    </span>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">마이크 — {{ micLabel }}</div>
                      <div class="check-item-sub">
                        <span v-if="micStatus === 'pending'">말을 해보세요...</span>
                        <span v-else-if="micStatus === 'error'">마이크에 접근할 수 없습니다.</span>
                        <template v-else>
                          입력 레벨:
                          <span v-for="i in 10" :key="i" :style="{ color: volumeLevel >= i * 10 ? 'var(--green-500)' : 'var(--ink-300)' }">●</span>
                        </template>
                      </div>
                    </div>
                    <span class="check-item-status">
                      <span class="badge-dot" :style="{ background: micStatus === 'ok' ? 'var(--green-500)' : micStatus === 'error' ? 'var(--red-500)' : 'var(--ink-300)' }"></span>
                      {{ micStatus === 'ok' ? '정상' : micStatus === 'error' ? '오류' : '대기 중' }}
                    </span>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">스피커</div>
                      <div class="check-item-sub">{{ speakerStatus === 'ok' ? '소리가 들리셨나요?' : '테스트 버튼을 눌러 소리를 확인하세요.' }}</div>
                    </div>
                    <span v-if="speakerStatus === 'ok'" class="check-item-status"><span class="badge-dot" style="background: var(--green-500)"></span>정상</span>
                    <button v-else class="btn btn-sm btn-secondary" @click="testSpeaker">테스트</button>
                  </div>
                  <div class="check-item">
                    <div class="check-item-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6 9 17l-5-5" /></svg></div>
                    <div class="check-item-body">
                      <div class="check-item-title">네트워크</div>
                      <div class="check-item-sub">인터넷 연결 상태 양호</div>
                    </div>
                    <span class="check-item-status"><span class="badge-dot" style="background: var(--green-500)"></span>정상</span>
                  </div>
                </div>

              </div>
            </div>
          </div>

          <!-- 사이드 패널 -->
          <aside class="preview-panel">
            <div class="preview-head"><h3 class="preview-title">면접방 정보</h3></div>
            <div class="preview-body">
              <div class="preview-row"><div class="preview-row-label">회사</div><div class="preview-row-val">{{ r.co }}</div></div>
              <div class="preview-row"><div class="preview-row-label">직무</div><div class="preview-row-val">{{ r.role }}</div></div>
              <div class="preview-row"><div class="preview-row-label">난이도</div><div class="preview-row-val">{{ di.label }}</div></div>
              <div class="preview-row"><div class="preview-row-label">면접관</div><div class="preview-row-val">AI 2명</div></div>
              <div class="preview-row"><div class="preview-row-label">경쟁 지원자</div><div class="preview-row-val">AI 2명</div></div>
              <div class="preview-row"><div class="preview-row-label">예상 소요</div><div class="preview-row-val">30 ~ 40분</div></div>
            </div>
            <div class="preview-foot">
              <p v-if="camStatus !== 'ok' || micStatus !== 'ok'" style="font-size:13px;color:var(--ink-400);margin-bottom:10px;text-align:center;">
                카메라와 마이크가 모두 정상이어야 시작할 수 있습니다.
              </p>
              <button
                class="btn btn-primary btn-block btn-lg"
                :disabled="camStatus !== 'ok' || micStatus !== 'ok'"
                :style="{ opacity: camStatus === 'ok' && micStatus === 'ok' ? 1 : 0.45, cursor: camStatus === 'ok' && micStatus === 'ok' ? 'pointer' : 'not-allowed' }"
                @click="startInterview"
              >
                면접 시작하기
              </button>
              <button class="btn btn-ghost btn-block" style="margin-top: 6px;" @click="goBack">이전으로</button>
            </div>
          </aside>
        </div>
      </template>

    </div>

    <!-- 이용권 부족 모달 -->
    <div v-if="showTicketModal" style="position:fixed;inset:0;background:rgba(0,0,0,0.45);display:flex;align-items:center;justify-content:center;z-index:9999;">
      <div style="background:#fff;border-radius:16px;padding:40px 36px;max-width:360px;width:90%;text-align:center;box-shadow:0 8px 32px rgba(0,0,0,0.15);">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#e53e3e" stroke-width="1.8" stroke-linecap="round" style="margin:0 auto 14px;display:block;"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="13"/><circle cx="12" cy="16.5" r="0.8" fill="#e53e3e" stroke="none"/></svg>
        <h3 style="font-size:18px;font-weight:700;color:#111;margin-bottom:10px;">이용권이 부족해요</h3>
        <p style="font-size:14px;color:#6b7280;margin-bottom:24px;">이용권을 충전하고<br/>면접을 시작해보세요.</p>
        <button
          style="width:100%;padding:12px 0;background:#2c7a4b;color:#fff;border:none;border-radius:10px;font-size:15px;font-weight:600;cursor:pointer;margin-bottom:10px;"
          @click="router.push('/mypage?section=billing')"
        >이용권 충전하러 가기</button>
        <button
          style="width:100%;padding:10px 0;background:#fff;color:#6b7280;border:1px solid #e5e7eb;border-radius:10px;font-size:14px;cursor:pointer;"
          @click="showTicketModal = false"
        >닫기</button>
      </div>
    </div>
  </main>
</template>

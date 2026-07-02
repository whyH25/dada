<script setup>
defineProps({
  myRooms: { type: Array, default: () => [] },
  roomsLoading: Boolean,
})
const emit = defineEmits(['open-room', 'delete-room'])

const REPORT_DELAY_MS = 60 * 1000

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}
function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${formatDate(dateStr)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
function diffLabel(d) { return d === 'HARD' ? '상' : d === 'MEDIUM' ? '중' : '하' }
function statusLabel(s) { return s === 'COMPLETED' ? '완료' : s === 'IN_PROGRESS' ? '진행 중' : s === 'CANCELLED' ? '취소' : s || '-' }
function statusBadgeClass(s) { return s === 'COMPLETED' ? 'badge-green' : s === 'IN_PROGRESS' ? 'badge-blue' : '' }
function applicantTypeLabel(t) { return t === 'NEW' ? '신입' : t === 'INTERN' ? '인턴' : t === 'EXPERIENCED' ? '경력' : t || '-' }

function reportOpenAt(room) {
  if (!room.endedAt) return null
  return new Date(new Date(room.endedAt).getTime() + REPORT_DELAY_MS)
}
function isReportOpen(room) {
  const openAt = reportOpenAt(room)
  return !!openAt && openAt <= new Date()
}
function reportOpenAtLabel(room) {
  const openAt = reportOpenAt(room)
  return openAt ? formatDateTime(openAt) : ''
}
</script>

<template>
  <div>
    <div class="breadcrumb">마이페이지 <span class="sep">›</span> 리포트</div>
    <div class="flex-between" style="margin:4px 0 20px;">
      <div><h2 class="mp-h1">리포트</h2><p class="mp-sub">완료한 모의 면접의 기록을 모아봤어요. 총 {{ myRooms.length }}건</p></div>
    </div>
    <div class="mp-stat-row">
      <div class="mp-stat"><div class="mp-stat-label">총 면접</div><div class="mp-stat-val">{{ myRooms.length }}<span>회</span></div></div>
      <div class="mp-stat"><div class="mp-stat-label">완료</div><div class="mp-stat-val" style="color:var(--green-500);">{{ myRooms.filter(r => r.status === 'COMPLETED').length }}<span>회</span></div></div>
      <div class="mp-stat"><div class="mp-stat-label">취소</div><div class="mp-stat-val" style="color:var(--ink-400);">{{ myRooms.filter(r => r.status === 'CANCELLED').length }}<span>회</span></div></div>
    </div>
    <div class="card" style="padding:0; overflow:hidden;">
      <div class="card-header" style="padding:18px 20px 14px; border-bottom:1px solid var(--ink-150); margin-bottom:0;">
        <h3 class="card-title">전체 리포트</h3><span class="text-sm text-muted">최신순</span>
      </div>
      <div v-if="roomsLoading" style="padding:48px; text-align:center; color:var(--ink-400);">불러오는 중...</div>
      <div v-else-if="!myRooms.length" style="padding:48px; text-align:center; color:var(--ink-400);">면접 기록이 없습니다.</div>
      <table v-else class="history-table report-table">
        <thead><tr><th>기업 / 직무</th><th>지원유형</th><th>일시</th><th>난이도</th><th>상태</th><th></th><th></th></tr></thead>
        <tbody>
          <tr
            v-for="room in myRooms"
            :key="room.roomId"
            :style="{ cursor: isReportOpen(room) ? 'pointer' : 'default' }"
            @click="isReportOpen(room) && emit('open-room', room)"
          >
            <td>
              <div style="display:flex; align-items:center; gap:10px;">
                <div class="company-logo" style="width:30px;height:30px;font-size:12px;">{{ room.companyName?.charAt(0) }}</div>
                <div><strong>{{ room.companyName }}</strong><span v-if="room.jobName" class="text-muted"> | {{ room.jobName }}</span></div>
              </div>
            </td>
            <td><span class="text-sm text-muted">{{ applicantTypeLabel(room.applicantType) }}</span></td>
            <td><span class="text-sm text-muted">{{ formatDate(room.startedAt) }}</span></td>
            <td><span class="badge">{{ diffLabel(room.difficulty) }}</span></td>
            <td><span class="badge" :class="statusBadgeClass(room.status)">{{ statusLabel(room.status) }}</span></td>
            <td>
              <span v-if="isReportOpen(room)" class="report-go">리포트 보기 ›</span>
              <span v-else class="text-sm text-muted" style="line-height:1.5;">
                리포트 생성중
                <template v-if="reportOpenAtLabel(room)"><br />{{ reportOpenAtLabel(room) }} 공개</template>
              </span>
            </td>
            <td><button class="btn btn-sm btn-ghost doc-del" @click.stop="emit('delete-room', room)">삭제</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

import { defineStore } from 'pinia'

export const useFlowStore = defineStore('flow', {
  state: () => ({
    currentRoom: 0,              // mock 데이터 인덱스 (RoomIntroView 등 기존 호환용)
    roomId: null,                // DB의 실제 interview_room.room_id
    language: 'KO',              // 면접 진행 언어 ("KO" | "EN") - STT 요청 시 같이 전달
    scenarios: [],               // AI 생성 시나리오 턴 목록
    interviewerPersonaIds: [],   // 선정된 면접관 ID 순서 (타일 순서와 일치)
    applicantPersonaIds: [],     // 선정된 지원자 ID 순서 (타일 순서와 일치)
    personaNames: {},              // { applicantId: 이름 }
    interviewerStopVideos: {},   // { interviewerId: stop 영상 URL }
    interviewerMoveVideos: {},   // { interviewerId: move 영상 URL }
    applicantStopVideos: {},     // { applicantId: stop 영상 URL }
    applicantMoveVideos: {},     // { applicantId: move 영상 URL }
  }),
})

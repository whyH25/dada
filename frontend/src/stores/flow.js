import { defineStore } from 'pinia'

export const useFlowStore = defineStore('flow', {
  state: () => ({
    currentRoom: 0,              // mock 데이터 인덱스 (RoomIntroView 등 기존 호환용)
    roomId: null,                // DB의 실제 interview_room.room_id
    scenarios: [],               // AI 생성 시나리오 턴 목록
    interviewerPersonaIds: [],   // 선정된 면접관 ID 순서 (타일 순서와 일치)
    applicantPersonaIds: [],     // 선정된 지원자 ID 순서 (타일 순서와 일치)
    personaNames: {},            // { personaId: 이름 }
  }),
})

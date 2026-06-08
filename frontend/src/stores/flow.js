import { defineStore } from 'pinia'

export const useFlowStore = defineStore('flow', {
  state: () => ({
    currentRoom: 0,   // mock 데이터 인덱스 (RoomIntroView 등 기존 호환용)
    roomId: null,     // DB의 실제 interview_room.room_id
    sessionId: null,  // DB의 실제 interview_session.session_id
    scenarios: [],    // AI 생성 질문 목록
  }),
})

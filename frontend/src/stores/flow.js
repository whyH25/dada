import { defineStore } from 'pinia'

// 면접 진행 흐름에서 선택된 방 인덱스를 공유 (원본 currentRoom 전역변수 대체)
export const useFlowStore = defineStore('flow', {
  state: () => ({ currentRoom: 0 }),
})

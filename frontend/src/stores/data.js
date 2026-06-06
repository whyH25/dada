import { defineStore } from 'pinia'
import {
  deadlines, hotJobs, rooms, diffInfo, companyInfo,
  stories, openChats, boardPosts, studies, boardCats, anonNames, allCompanies,
} from './seed.js'

export const useDataStore = defineStore('data', {
  state: () => ({
    // deep copy so mutations are reactive and don't touch the seed module
    deadlines: JSON.parse(JSON.stringify(deadlines)),
    hotJobs: [...hotJobs],
    rooms: JSON.parse(JSON.stringify(rooms)),
    diffInfo,
    companyInfo,
    stories: JSON.parse(JSON.stringify(stories)),
    openChats: [...openChats],
    boardPosts: JSON.parse(JSON.stringify(boardPosts)),
    studies: JSON.parse(JSON.stringify(studies)),
    boardCats,
    anonNames,
    allCompanies,
    interest: new Set(['삼성전자', '네이버', '카카오']),
    roomQuery: '',
  }),
  getters: {
    // 원본 updateMineCount()
    mineCount: (s) => {
      let n = 0
      Object.values(s.deadlines).forEach((list) => list.forEach((e) => { if (e.mine) n++ }))
      return n
    },
    // 원본 infoFor()
    infoFor: (s) => (co) =>
      s.companyInfo[co] || {
        industry: '대기업 공채',
        style: [
          '직무 경험 기반의 심화 질문이 이어집니다.',
          '논리적 근거와 구체적 사례를 중시합니다.',
          '인성·조직 적합성을 함께 평가합니다.',
        ],
      },
  },
  actions: {
    toggleSaveDeadline(day, idx) {
      const e = this.deadlines[day][idx]
      e.mine = !e.mine
      return e
    },
    // 관심 기업
    saveInterest(draftSet) { this.interest = new Set(draftSet) },
    // 합격 스토리
    viewStory(i) { this.stories[i].views += 1 },
    likeStory(i) { this.stories[i].likes += 1 },
    // 자유게시판
    addPost({ title, body, cat }) {
      const cls = (this.boardCats.find((c) => c[0] === cat) || ['', 'badge'])[1]
      this.boardPosts.unshift({
        cat, cls, co: '전체', title,
        author: '익명의 ' + this.anonNames[Math.floor(Math.random() * this.anonNames.length)],
        time: '방금 전', body: body.split('\n').filter(Boolean), cl: [],
      })
    },
    addComment(postIdx, text) {
      this.boardPosts[postIdx].cl.push({ who: '나', t: text, time: '방금 전' })
    },
    // 스터디 신청
    addStudyComment(studyIdx, text) {
      const s = this.studies[studyIdx]
      s.cl.push({ who: '나', t: text, time: '방금 전' })
      if (s.members < s.max) s.members++
    },
  },
})

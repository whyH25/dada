<script setup>
import { ref } from 'vue'

const CATEGORIES = ['회원가입 / 로그인', '면접 이용', '면접 리포트', '이력서 / 포트폴리오', '계정', '결제 / 이용권']

const FAQ = [
  {
    category: '회원가입 / 로그인',
    items: [
      { q: '비밀번호를 잊어버렸어요.', a: '로그인 화면의 "아이디/비밀번호 찾기"를 이용해 주세요.' },
      { q: '소셜 로그인(Google)으로도 가입할 수 있나요?', a: '네, Google 계정으로 간편하게 가입하고 로그인할 수 있습니다.' },
      { q: '회원 탈퇴는 어떻게 하나요?', a: '마이페이지 > 하단의 "회원탈퇴" 버튼을 통해 탈퇴하실 수 있습니다.' },
    ],
  },
  {
    category: '면접 이용',
    items: [
      { q: '면접 중간에 종료되면 이용권을 복구할 수 있나요?', a: '면접이 비정상적으로 종료된 경우, 오류 화면 캡처 등 증빙 자료를 첨부해 고객센터 메일로 문의해 주세요. 확인 후 이용권 복구를 도와드립니다.'},
      { q: '이력서와 포트폴리오는 꼭 등록해야 하나요?', a: '필수는 아닙니다. 다만 등록 시 직무에 맞는 보다 정확한 질문이 생성되므로 등록을 권장드립니다.' },
      { q: '면접 시간은 얼마나 걸리나요?', a: '일반적으로 10~30분 정도 소요됩니다.' },
    ],
  },
  {
    category: '면접 리포트',
    items: [
      { q: '면접 리포트는 언제 확인할 수 있나요?', a: '면접 종료 후 1일 뒤부터 확인할 수 있습니다.' },
      { q: '리포트는 어떤 내용을 제공하나요?', a: '종합 평가, 역량 분석, 지원자 분석, 질문별 상세 피드백을 제공합니다.' },
      { q: '이전 면접 리포트도 다시 볼 수 있나요?', a: '네, 마이페이지에서 이전 면접 리포트를 언제든지 다시 확인할 수 있습니다.' },
    ],
  },
  {
    category: '이력서 / 포트폴리오',
    items: [
      { q: '이력서와 포트폴리오는 여러 개 등록할 수 있나요?', a: '네. 여러 개 등록 후 면접방 생성 시 원하는 파일을 선택할 수 있습니다.' },
      { q: '등록한 이력서를 수정하면 이전 면접에도 반영되나요?', a: '아니요. 면접 생성 당시 사용된 정보는 별도로 저장되어 이전 면접 결과에는 영향을 주지 않습니다.' },
      { q: '이력서나 포트폴리오를 삭제하면 면접 기록도 삭제되나요?', a: '아니요. 면접 당시 사용된 내용은 면접 기록에 안전하게 보관됩니다.' },
    ],
  },
  {
    category: '계정',
    items: [
      { q: '회원 탈퇴 시 면접 기록도 삭제되나요?', a: '회원 탈퇴 시 등록하신 이력서, 포트폴리오 등 개인정보는 삭제됩니다. 다만, 서비스 운영 및 관련 법령에 따라 일부 이용 기록은 일정 기간 보관될 수 있습니다.' },
      { q: '개인정보는 안전하게 관리되나요?', a: '업로드된 파일과 면접 데이터는 보안 정책에 따라 안전하게 관리됩니다.' },
    ],
  },
  {
    category: '결제 / 이용권',
    items: [
      { q: '무료 체험은 몇 회까지 가능한가요?', a: '신규 회원에 한해 무료 체험 이용권 1회가 제공됩니다.' },
      { q: '이용권은 어떻게 사용되나요?', a: '면접 1회 진행 시 이용권 1회가 차감됩니다.' },
      { q: '이용권 구매는 어디서 하나요?', a: '마이페이지 > 구독 및 결제에서 이용권을 충전하실 수 있습니다.'},
    ],
  },
]

const activeCategory = ref(CATEGORIES[0])
const openItems = ref(new Set())

function filteredFaq() {
  return FAQ.filter(g => g.category === activeCategory.value)
}

function toggle(key) {
  if (openItems.value.has(key)) openItems.value.delete(key)
  else openItems.value.add(key)
}
</script>

<template>
  <main class="page active" id="page-faq">
    <div class="container">
      <!-- 페이지 헤더 -->
      <div class="page-header">
        <h1 class="page-title">자주 묻는 질문 (FAQ)</h1>
        <p class="page-desc">자주 묻는 질문과 답변을 확인하세요.</p>
      </div>

      <!-- 문의 안내 배너 -->
      <div class="faq-contact-banner">
        <div class="faq-contact-left">
          <div class="faq-contact-title">고객센터</div>
          <a href="mailto:admin.yh25@gmail.com" class="faq-contact-email">admin.yh25@gmail.com</a>
        </div>
        <div class="faq-contact-right">
          <div class="faq-contact-hours-label">문의 메일 운영 시간</div>
          <div class="faq-contact-hours">평일 09:00 – 18:00</div>
        </div>
      </div>

      <!-- 카테고리 탭 -->
      <div class="faq-tabs">
        <button
          v-for="cat in CATEGORIES"
          :key="cat"
          class="faq-tab"
          :class="{ active: activeCategory === cat }"
          @click="activeCategory = cat; openItems = new Set()"
        >{{ cat }}</button>
      </div>

      <!-- FAQ 목록 -->
      <div class="faq-sections">
        <div v-for="group in filteredFaq()" :key="group.category" class="faq-group">
          <h2 class="faq-group-title">{{ group.category }}</h2>
          <div class="faq-list">
            <div
              v-for="(item, idx) in group.items"
              :key="idx"
              class="faq-item"
              :class="{ open: openItems.has(group.category + idx) }"
            >
              <button class="faq-q" @click="toggle(group.category + idx)">
                <span class="faq-q-label">Q</span>
                <span class="faq-q-text">{{ item.q }}</span>
                <svg class="faq-chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </button>
              <div class="faq-a" v-show="openItems.has(group.category + idx)">
                <span class="faq-a-label">A</span>
                <span class="faq-a-text">{{ item.a }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* 문의 배너 */
.faq-contact-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--green-50);
  border: 1px solid var(--green-100);
  border-radius: 12px;
  padding: 20px 28px;
  margin-bottom: 32px;
  gap: 16px;
  flex-wrap: wrap;
}
.faq-contact-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-500);
  margin-bottom: 4px;
}
.faq-contact-email {
  font-size: 15px;
  font-weight: 700;
  color: var(--green-500);
  text-decoration: none;
}
.faq-contact-email:hover { text-decoration: underline; }
.faq-contact-right { text-align: right; }
.faq-contact-hours-label {
  font-size: 12px;
  color: var(--ink-400);
  margin-bottom: 2px;
}
.faq-contact-hours {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
}

/* 카테고리 탭 */
.faq-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 32px;
}
.faq-tab {
  padding: 7px 16px;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--ink-200);
  background: #fff;
  color: var(--ink-600);
  cursor: pointer;
  transition: all 0.15s;
}
.faq-tab:hover { border-color: var(--green-500); color: var(--green-500); }
.faq-tab.active {
  background: var(--green-500);
  border-color: var(--green-500);
  color: #fff;
}

/* FAQ 섹션 */
.faq-sections { display: flex; flex-direction: column; gap: 40px; padding-bottom: 80px; }
.faq-group-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--green-500);
  margin: 0 0 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--green-100);
}

/* FAQ 아이템 */
.faq-list { display: flex; flex-direction: column; }
.faq-item {
  border-bottom: 1px solid var(--ink-150);
}
.faq-q {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 4px;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
}
.faq-q:hover .faq-q-text { color: var(--green-500); }
.faq-q-label {
  flex-shrink: 0;
  width: 24px; height: 24px;
  border-radius: 50%;
  background: var(--green-500);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.faq-q-text {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900);
  transition: color 0.15s;
}
.faq-chevron {
  flex-shrink: 0;
  color: var(--ink-400);
  transition: transform 0.2s;
}
.faq-item.open .faq-chevron { transform: rotate(180deg); }

.faq-a {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 0 4px 18px;
  background: var(--ink-50);
  border-radius: 8px;
  margin-bottom: 2px;
  padding: 14px 16px;
}
.faq-a-label {
  flex-shrink: 0;
  width: 24px; height: 24px;
  border-radius: 50%;
  background: var(--ink-200);
  color: var(--ink-600);
  font-size: 12px;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.faq-a-text {
  font-size: 14px;
  color: var(--ink-600);
  line-height: 1.7;
  padding-top: 2px;
}

.page-desc {
  font-size: 14px;
  color: var(--ink-500);
  margin: 4px 0 0;
}
</style>

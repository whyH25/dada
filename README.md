<div align="center">

# 🎙️ 다대다(多:多)

### AI 기반 다대다(多:多) 면접 시뮬레이션 플랫폼

복수의 **AI 면접관** 페르소나와 **AI 경쟁 지원자**가 함께하는 실전형 면접 연습 플랫폼

<br>

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue-3-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o-412991?style=flat-square&logo=openai&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)

</div>

---

## 📌 프로젝트 개요

> 기존 AI 면접 서비스가 **1:1 구조**에 머물러 있는 반면,
> **InterviewAI**는 실제 기업 면접과 동일한 **다대다 면접**를 시뮬레이션합니다.

| 항목 | 내용 |
|:---|:---|
| **프로젝트명** | InterviewAI |
| **개발 기간** | 2026.06.01 ~ 2026.06.26 |
| **서비스 유형** | AI 기반 면접 연습 웹 플랫폼 |
| **핵심 차별점** | 1:1 AI 면접을 넘어선 **다대다(多:多)** 실전 면접 시뮬레이션 |

### ✨ 핵심 기능

- 🧑‍💼 **복수의 AI 면접관 페르소나** — 인사 / 현업 / 임원
- 🤝 **AI 경쟁 지원자와의 동시 면접** — 직무 30종 × 업종 21종
- 🔗 **꼬리 질문 생성 엔진** — 이전 답변을 분석해 후속 질문 자동 생성
- 📊 **종합 피드백 리포트** — 면접 종료 후 정성 · 정량 평가 제공

---

## 👥 팀원 소개

| 이름 | 역할 |
|:---:|:---:|
| 박상은 | 👑 팀장 |
| 김영현 | 🙋 팀원 |

---

## ⚙️ 기술 스택

### 🖥️ Backend

| 분류 | 기술 |
|:---|:---|
| Framework | Spring Boot 3.x |
| ORM | MyBatis |
| Database | MySQL 8.x |
| AI 연동 | OpenAI GPT API (GPT-4o) |
| 인증 |  |
| 빌드 | Maven |

### 🎨 Frontend

| 분류 | 기술 |
|:---|:---|
| Framework | Vue 3 (Composition API) |
| 상태 관리 | Pinia |
| HTTP | Axios |
| UI | Tailwind CSS |

### ☁️ 인프라 / 기타

| 분류 | 기술 |
|:---|:---|
| 음성 변환 | Web Speech API / Whisper API |
| 파일 저장 | AWS S3 (또는 로컬) |
| 형상 관리 | Git / GitHub |

---

## 📋 기능 목록

<details open>
<summary><b>👤 회원 관리</b></summary>

<br>

| 기능 ID | 기능명 | 설명 |
|:---:|:---|:---|
| F-01 | 회원가입 | 이메일 계정 생성 및 소셜 로그인 지원 |
| F-02 | 로그인 | 이메일/비밀번호 인증, Spring Security |
| F-03 | 사용자 프로필 관리 | 기본 정보 및 이력서 관리 |
| F-04 | 이력서 등록 | PDF/DOC 파일 업로드 |
| F-05 | 자기소개서 등록 | 텍스트 입력 또는 파일 업로드 |
| F-06 | 포트폴리오 등록 | 파일 또는 URL 등록 |

</details>

<details open>
<summary><b>🎯 면접 설정 및 시나리오</b></summary>

<br>

| 기능 ID | 기능명 | 설명 |
|:---:|:---|:---|
| F-08 | 면접 설정 | 회사/직무/난이도/면접관·지원자 인원 선택 → 초기 질문 생성 |
| F-09 | 시나리오 (초기) | 이력서 분석 기반 공통·개인화 질문 리스트 생성 |
| F-10 | AI 면접관 생성 | 페르소나 기반 면접관 목록 생성 (인사/현업/임원 등) |
| F-11 | AI 지원자 생성 | 페르소나 기반 경쟁 지원자 생성 |

</details>

<details open>
<summary><b>🗣️ 면접 진행</b></summary>

<br>

| 기능 ID | 기능명 | 설명 |
|:---:|:---|:---|
| F-14 | 사용자 답변 입력 | 음성/영상 입력 + STT 변환 |
| F-15 | 면접관 기억 시스템 | 이전 답변 컨텍스트 기반 질문 연속성 유지 |
| F-17 | 실시간 행동 분석 | 시선 추적, 말 속도 분석 (확장 기능) |
| F-18 | 면접 종료 처리 | 면접 로그 저장 및 세션 종료 |

</details>

<details open>
<summary><b>📈 피드백 & 리포트</b></summary>

<br>

| 기능 ID | 기능명 | 설명 |
|:---:|:---|:---|
| F-19 | 피드백 리포트 생성 | 면접 데이터 기반 종합 평가 리포트 |
| F-20 | 정성 분석 | 답변 품질·논리성·표현력 평가 코멘트 |
| F-21 | 시각화 리포트 | 레이더 차트, 타임라인 등 시각 자료 |
| F-22 | 면접 기록 조회 | 과거 면접 결과 목록 및 상세 조회 |

</details>

<details>
<summary><b>🛠️ 관리자 기능 (확장)</b></summary>

<br>

| 기능 ID | 기능명 | 설명 |
|:---:|:---|:---|
| F-28 | 관리자 로그인 | 관리자 전용 인증 |
| F-29 | 회원 관리 | 사용자 목록 조회/수정/탈퇴 처리 |
| F-30 | 면접 데이터 관리 | 면접 로그 관리 및 삭제 |
| F-31 | 시나리오 관리 | 질문·시나리오 등록/수정 |
| F-32 | 공지사항 관리 | 공지 및 FAQ CRUD |
| F-33 | 자유게시판 관리 | 면접후기, 질문, 기타 |

</details>

---

## 📂 프로젝트 구조

```
InterviewAI/
├── backend/                            # Spring Boot
│   ├── src/main/java/
│   │   └── com/interviewai/
│   │       ├── common/                 # 공통 유틸·상수·예외 처리
│   │       ├── config/                 # Spring 설정 (Security, CORS 등)
│   │       ├── controller/             # REST API 컨트롤러
│   │       ├── dao/                    # MyBatis Mapper 인터페이스
│   │       ├── dto/                    # VO/DTO 데이터 객체
│   │       └── service/                # 비즈니스 로직
│   ├── src/main/resources/
│   │   ├── mappers/                    # MyBatis XML 매퍼
│   │   ├── static/                     # 정적 리소스 (이미지, JS, CSS)
│   │   ├── application.properties      # 환경 설정 파일
│   │   └── application-local.properties# 환경 설정 파일
│   └── src/main/webapp/WEB-INF/views/  # JSP 뷰
│
├── frontend/                           # Vue 3
│   ├── src/
│   │   ├── api/                        # Axios API 호출 모듈
│   │   ├── assets/                     # 정적 자원 (이미지, 폰트, 스타일)
│   │   ├── components/                 # 공통 컴포넌트
│   │   ├── composables/                # 재사용 로직 (Composition 함수)
│   │   ├── layouts/                    # 페이지 공통 레이아웃
│   │   ├── router/                     # Vue Router 라우팅 설정
│   │   ├── stores/                     # Pinia 상태 관리
│   │   ├── utils/                      # 공통 유틸 함수
│   │   └── views/                      # 페이지 컴포넌트
│   └── public/                         # 정적 파일 (index.html 등)
│
├── sql/                                # DDL / 초기 데이터
└── docs/                               # 설계 문서
```

---

## 🗄️ 주요 DB 테이블

| 테이블명 | 설명 |
|:---|:---|
| `users` | 회원 정보 |
| `resumes` | 이력서 데이터 |
| `cover_letters` | 자기소개서 |
| `interview_rooms` | 면접 방 설정 |
| `interview_sessions` | 면접 진행 세션 |
| `ai_interviewers` | AI 면접관 페르소나 |
| `ai_applicants` | AI 경쟁 지원자 |
| `interview_logs` | 면접 대화 로그 |
| `feedback_reports` | 피드백 리포트 |
| `notices` | 공지사항 |


## 🗄️ 전체 DB 테이블

| 테이블명 | 설명 |
|:---|:---|
| `users` | 회원 정보 |
| `users_social` | 소셜 로그인 연동 |
| `user_resume` | 이력서 |
| `user_portfolio` | 포트폴리오 |
| `cover_letter` | 자기소개서 |
| `category_job_group` | 직무 분류 대분류 |
| `category_job` | 직무 분류 중분류 |
| `category_industry` | 업종 분류 21종 |
| `interview_room` | 사용자 생성 면접방 |
| `ai_interviewer` | AI 면접관 페르소나 |
| `ai_applicant` | AI 경쟁 지원자 |
| `interview_scenario` | 면접 시나리오 / 질문 목록 |
| `notice` | 공지사항 / FAQ |
| `category_community` | 커뮤니티 게시판 카테고리 |
| `community_post` | 커뮤니티 게시글 |
| `community_comment` | 댓글 / 대댓글 |
| `community_like` | 게시글 / 댓글 좋아요 |
---

<div align="center">

**InterviewAI** · 2026

실전 같은 연습, 합격에 한 걸음 더 가깝게 🚀

</div>

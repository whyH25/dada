<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()

function doLogin() { auth.doLogin(router) }
</script>

<template>
  <div class="auth-overlay" :class="{ open: auth.loginOpen }" @click.self="auth.closeLogin()">
    <div class="auth-modal" role="dialog" aria-modal="true">
      <button class="auth-close" @click="auth.closeLogin()" aria-label="닫기">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12" /></svg>
      </button>
      <div class="auth-brand">
        <div class="brand-logo"><span class="d"></span><span class="d"></span><span class="d"></span></div>
        <div class="auth-brand-name">다대다</div>
      </div>
      <h2 class="auth-title">로그인이 필요해요</h2>
      <p class="auth-sub">면접 참여·면접방 생성·이력서 등록은 로그인 후 이용할 수 있어요.</p>

      <div class="auth-field">
        <label class="auth-label">이메일</label>
        <input class="input" type="email" value="jiwon.kim@email.com" placeholder="you@email.com" />
      </div>
      <div class="auth-field">
        <label class="auth-label">비밀번호</label>
        <input class="input" type="password" value="········" placeholder="비밀번호" />
      </div>
      <div class="auth-row">
        <label class="auth-remember"><input type="checkbox" checked /> 로그인 상태 유지</label>
        <a class="auth-forgot">비밀번호 찾기</a>
      </div>

      <button class="btn btn-primary btn-block btn-lg" @click="doLogin">로그인</button>

      <div class="auth-divider"><span>또는</span></div>
      <div class="auth-social">
        <button class="auth-social-btn kakao" @click="doLogin">카카오로 시작하기</button>
        <button class="auth-social-btn" @click="doLogin">Google 계정으로 계속</button>
      </div>
      <div class="auth-foot">
        아직 회원이 아니신가요? <a @click="doLogin">회원가입</a>
      </div>
    </div>
  </div>
</template>

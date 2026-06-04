// 원본 mypage.js의 toast() 그대로
let toastTimer = null
export function toast(msg) {
  let t = document.getElementById('mp-toast')
  if (!t) { t = document.createElement('div'); t.id = 'mp-toast'; document.body.appendChild(t) }
  t.textContent = msg
  t.classList.add('show')
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => t.classList.remove('show'), 2200)
}

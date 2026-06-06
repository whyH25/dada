<script setup>
import { ref } from 'vue'
import { getTestMessage } from '@/api/testApi'

const message = ref('')

const callTestApi = async () => {
  try {
    message.value = await getTestMessage()
  } catch (error) {
    console.error(error)
    message.value = '통신 실패'
  }
}
</script>

<template>
  <main>
    <h1>API 통신 테스트</h1>

    <button @click="callTestApi">
      Spring API 호출
    </button>

    <p>결과: {{ message }}</p>
  </main>
</template>
<script setup>
import { ref } from 'vue'

const message = ref('')

const testApi = async () => {
  const response = await fetch('http://localhost:8080/api/test')
  message.value = await response.text()
}
</script>

<template>
  <h1>다대다 테스트</h1>

  <button @click="testApi">
    API 호출
  </button>

  <p>{{ message }}</p>
</template>
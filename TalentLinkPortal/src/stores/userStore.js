// src/stores/userStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const isAuthenticated = ref(false)
  const user = ref(null)

  function setUser(userData) {
    user.value = userData
    isAuthenticated.value = true
  }

  function logout() {
    user.value = null
    isAuthenticated.value = false
  }

  return {
    isAuthenticated,
    user,
    setUser,
    logout
  }
})

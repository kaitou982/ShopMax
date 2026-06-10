<script setup lang="ts">
import { NMessageProvider, NDialogProvider } from 'naive-ui'
</script>

<template>
  <n-message-provider>
    <n-dialog-provider>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </n-dialog-provider>
  </n-message-provider>
</template>

<style lang="scss">
@import '@/styles/variables.scss';

*, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
html { background: $bg-page; -webkit-font-smoothing: antialiased; }
body { font-family: $font-family; font-size: $font-size-base; color: $text-primary; line-height: 1.5; }
a { color: inherit; text-decoration: none; }
img { max-width: 100%; display: block; }
::-webkit-scrollbar { width: 6px; } ::-webkit-scrollbar-thumb { background: #D1D1D6; border-radius: 3px; }

// Route transition
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

// Cart badge animation
@keyframes badgePop { 0% { transform: scale(1); } 50% { transform: scale(1.4); } 100% { transform: scale(1); } }
.cart-badge-pop { animation: badgePop 0.3s ease; }

// Skeleton
@keyframes shimmer { 0% { background-position: -400px 0; } 100% { background-position: 400px 0; } }
</style>

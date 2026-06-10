<script setup lang="ts">
/**
 * LiveDanmaku 弹幕组件
 * 轨道式滚动弹幕
 */
import { ref, onMounted, onUnmounted, watch } from 'vue'

defineOptions({ name: 'LiveDanmaku' })

interface DanmakuItem {
  id: number
  nickname: string
  content: string
  color?: string
}

interface Props {
  messages: DanmakuItem[]
  trackCount?: number
  speed?: number
}

const props = withDefaults(defineProps<Props>(), {
  trackCount: 4,
  speed: 8,
})

const tracks = ref<{ items: { id: number; text: string; left: number; color: string }[] }[]>([])

// 初始化轨道
for (let i = 0; i < props.trackCount; i++) {
  tracks.value.push({ items: [] })
}

let animFrame: number | null = null
let lastMessageIndex = 0

const getRandomColor = () => {
  const colors = ['#fff', '#4fc3f7', '#66bb6a', '#ffa726', '#ef5350', '#ab47bc']
  return colors[Math.floor(Math.random() * colors.length)]
}

const addDanmaku = (msg: DanmakuItem) => {
  // 选择最空闲的轨道
  let minItems = Infinity
  let targetTrack = 0
  tracks.value.forEach((track, index) => {
    if (track.items.length < minItems) {
      minItems = track.items.length
      targetTrack = index
    }
  })

  const item = {
    id: msg.id || Date.now(),
    text: `${msg.nickname}: ${msg.content}`,
    left: 100, // 从右侧开始（百分比）
    color: msg.color || getRandomColor(),
  }

  tracks.value[targetTrack].items.push(item)

  // 8 秒后移除
  setTimeout(() => {
    const track = tracks.value[targetTrack]
    const index = track.items.findIndex(i => i.id === item.id)
    if (index !== -1) {
      track.items.splice(index, 1)
    }
  }, 8000)
}

const animate = () => {
  tracks.value.forEach(track => {
    track.items.forEach(item => {
      item.left -= 0.15 * props.speed / 60
    })
  })
  animFrame = requestAnimationFrame(animate)
}

watch(() => props.messages.length, () => {
  // 处理新消息
  while (lastMessageIndex < props.messages.length) {
    const msg = props.messages[lastMessageIndex]
    if (msg.type === 'danmaku') {
      addDanmaku(msg as unknown as DanmakuItem)
    }
    lastMessageIndex++
  }
})

onMounted(() => {
  animFrame = requestAnimationFrame(animate)
})

onUnmounted(() => {
  if (animFrame) {
    cancelAnimationFrame(animFrame)
  }
})
</script>

<template>
  <div class="live-danmaku">
    <div
      v-for="(track, trackIndex) in tracks"
      :key="trackIndex"
      class="live-danmaku__track"
    >
      <span
        v-for="item in track.items"
        :key="item.id"
        class="live-danmaku__item"
        :style="{
          right: item.left + '%',
          color: item.color,
        }"
      >
        {{ item.text }}
      </span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.live-danmaku {
  position: absolute;
  bottom: 60px;
  left: 0;
  right: 0;
  height: 120px;
  overflow: hidden;
  pointer-events: none;

  &__track {
    position: relative;
    height: 30px;
    width: 100%;
  }

  &__item {
    position: absolute;
    white-space: nowrap;
    font-size: 13px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
    background: rgba(0, 0, 0, 0.3);
    padding: 2px 10px;
    border-radius: 12px;
    transition: right 0.016s linear;
  }
}
</style>

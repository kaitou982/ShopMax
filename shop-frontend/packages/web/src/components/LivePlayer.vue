<script setup lang="ts">
/**
 * LivePlayer 直播播放器组件
 * 支持 HTTP-FLV (mpegts.js) 和 HLS (hls.js) 自动降级
 */
import { ref, onMounted, onUnmounted, watch } from 'vue'
import mpegts from 'mpegts.js'
import Hls from 'hls.js'

defineOptions({ name: 'LivePlayer' })

interface Props {
  roomId: number
  flvUrl?: string
  hlsUrl?: string
  autoplay?: boolean
  muted?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoplay: true,
  muted: true,
})

const videoRef = ref<HTMLVideoElement>()
const playerState = ref<'loading' | 'playing' | 'ended' | 'error' | 'reconnecting'>('loading')
const reconnectCount = ref(0)
const maxReconnect = 3

let player: mpegts.Player | Hls | null = null

const initPlayer = () => {
  if (!videoRef.value) return

  // 优先使用 HTTP-FLV (低延迟)
  if (props.flvUrl && mpegts.isSupported()) {
    initFlvPlayer()
  } else if (props.hlsUrl && Hls.isSupported()) {
    initHlsPlayer()
  } else if (props.hlsUrl && videoRef.value.canPlayType('application/vnd.apple.mpegurl')) {
    // Safari 原生支持 HLS
    initNativeHls()
  } else {
    playerState.value = 'error'
    console.error('浏览器不支持 FLV 或 HLS')
  }
}

const initFlvPlayer = () => {
  if (!videoRef.value || !props.flvUrl) return

  player = mpegts.createPlayer({
    type: 'flv',
    isLive: true,
    url: props.flvUrl,
    enableStashBuffer: false,  // 关闭缓冲累积
    stashInitialSize: 128,     // 最小缓冲（默认 384KB）
    enableWorker: true,        // 启用 Worker 多线程解码
  })

  player.attachMediaElement(videoRef.value)
  player.load()

  if (props.autoplay) {
    videoRef.value.play().catch(() => {
      console.log('自动播放被阻止，需要用户交互')
    })
  }

  player.on(mpegts.Events.ERROR, () => {
    handlePlayerError()
  })

  playerState.value = 'playing'
}

const initHlsPlayer = () => {
  if (!videoRef.value || !props.hlsUrl) return

  const hls = new Hls({
    liveDurationInfinity: true,
    lowLatencyMode: true,
  })
  player = hls

  hls.loadSource(props.hlsUrl)
  hls.attachMedia(videoRef.value)

  hls.on(Hls.Events.MANIFEST_PARSED, () => {
    if (props.autoplay) {
      videoRef.value?.play().catch(() => {
        console.log('自动播放被阻止')
      })
    }
    playerState.value = 'playing'
  })

  hls.on(Hls.Events.ERROR, () => {
    handlePlayerError()
  })
}

const initNativeHls = () => {
  if (!videoRef.value || !props.hlsUrl) return

  videoRef.value.src = props.hlsUrl
  if (props.autoplay) {
    videoRef.value.play().catch(() => {
      console.log('自动播放被阻止')
    })
  }
  playerState.value = 'playing'
}

const handlePlayerError = () => {
  if (reconnectCount.value >= maxReconnect) {
    playerState.value = 'ended'
    return
  }

  playerState.value = 'reconnecting'
  reconnectCount.value++

  setTimeout(() => {
    destroyPlayer()
    initPlayer()
  }, 3000)
}

const destroyPlayer = () => {
  if (player instanceof mpegts.Player) {
    player.detachMediaElement()
    player.destroy()
  } else if (player instanceof Hls) {
    player.destroy()
  }
  player = null
}

const toggleMute = () => {
  if (videoRef.value) {
    videoRef.value.muted = !videoRef.value.muted
  }
}

const toggleFullscreen = () => {
  if (!videoRef.value) return
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    videoRef.value.requestFullscreen()
  }
}

watch(() => props.flvUrl, () => {
  destroyPlayer()
  reconnectCount.value = 0
  initPlayer()
})

onMounted(() => {
  initPlayer()
})

onUnmounted(() => {
  destroyPlayer()
})

defineExpose({ toggleMute, toggleFullscreen })
</script>

<template>
  <div class="live-player">
    <video
      ref="videoRef"
      :muted="muted"
      playsinline
      webkit-playsinline
      class="live-player__video"
    />

    <!-- 状态覆盖层 -->
    <div v-if="playerState === 'loading'" class="live-player__overlay">
      <div class="live-player__loading">
        <div class="spinner" />
        <span>加载中...</span>
      </div>
    </div>

    <div v-else-if="playerState === 'reconnecting'" class="live-player__overlay">
      <div class="live-player__loading">
        <div class="spinner" />
        <span>连接中断，正在重连 ({{ reconnectCount }}/{{ maxReconnect }})...</span>
      </div>
    </div>

    <div v-else-if="playerState === 'ended'" class="live-player__overlay">
      <div class="live-player__ended">
        <span class="live-player__ended-icon">📺</span>
        <span>直播已结束</span>
      </div>
    </div>

    <!-- LIVE 标记 -->
    <div v-if="playerState === 'playing'" class="live-player__live-badge">
      <span class="live-player__live-dot" />
      LIVE
    </div>

    <!-- 控制栏 -->
    <div v-if="playerState === 'playing'" class="live-player__controls">
      <button class="live-player__btn" @click="toggleMute">
        {{ muted ? '🔇' : '🔊' }}
      </button>
      <button class="live-player__btn" @click="toggleFullscreen">
        ⛶
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.live-player {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  overflow: hidden;

  &__video {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  &__overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.6);
  }

  &__loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: #fff;
    font-size: 14px;
  }

  &__ended {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: #aaa;
    font-size: 16px;
  }

  &__ended-icon {
    font-size: 48px;
    opacity: 0.5;
  }

  &__live-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    background: #e74c3c;
    color: #fff;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
  }

  &__live-dot {
    width: 8px;
    height: 8px;
    background: #fff;
    border-radius: 50%;
    animation: pulse 1.5s infinite;
  }

  &__controls {
    position: absolute;
    bottom: 12px;
    right: 12px;
    display: flex;
    gap: 8px;
  }

  &__btn {
    width: 32px;
    height: 32px;
    background: rgba(0, 0, 0, 0.5);
    border: none;
    border-radius: 50%;
    color: #fff;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: rgba(0, 0, 0, 0.7);
    }
  }
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>

<script setup lang="ts">
defineOptions({ name: 'DefaultLayout' })

import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { categoryApi, searchApi, type Category, type SuggestResponse } from '@shop/shared'
import { useUserStore, useCartStore } from '@/stores'
import CsChatWindow from '@/components/CsChatWindow.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 搜索
const searchKeyword = ref('')
const showSearchSuggest = ref(false)
const suggestData = ref<SuggestResponse>({ products: [], hotWords: [] })
let suggestTimer: ReturnType<typeof setTimeout> | null = null

const onSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { q: searchKeyword.value.trim() } })
    showSearchSuggest.value = false
  }
}

const onSuggestSelect = (kw: string) => {
  searchKeyword.value = kw
  router.push({ path: '/search', query: { q: kw } })
  showSearchSuggest.value = false
}

watch(searchKeyword, (val) => {
  if (suggestTimer) clearTimeout(suggestTimer)
  if (!val.trim()) {
    suggestData.value = { products: [], hotWords: [] }
    return
  }
  suggestTimer = setTimeout(async () => {
    try {
      suggestData.value = await searchApi.getSuggest(val.trim())
    } catch { /* noop */ }
  }, 300)
})

// 分类
const categories = ref<Category[]>([])
const hoverCategory = ref<Category | null>(null)

onMounted(async () => {
  try { categories.value = await categoryApi.getTree() } catch { /* noop */ }
  userStore.fetchUserInfo().catch(() => {})
})

// 回到顶部
const showBackTop = ref(false)
const onScroll = () => { showBackTop.value = window.scrollY > 800 }
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
const backToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

// 用户菜单
const showUserMenu = ref(false)

// 购物车面板
const showCartPanel = ref(false)

const goCheckout = () => {
  showCartPanel.value = false
  router.push('/order/confirm')
}

// 客服聊天
const showChatWindow = ref(false)

</script>

<template>
  <div class="layout-default">
    <!-- 顶部导航 -->
    <header class="top-nav">
      <div class="top-nav-inner">
        <router-link to="/" class="logo">ShopMax</router-link>
        <div class="search-wrap" @mouseleave="showSearchSuggest = false">
          <input
            v-model="searchKeyword" class="search-input"
            placeholder="搜索商品、品牌、分类..." @keyup.enter="onSearch"
            @focus="showSearchSuggest = true"
          />
          <button class="search-btn" @click="onSearch">🔍</button>
          <!-- 搜索建议下拉 -->
          <div class="suggest-dropdown" v-if="showSearchSuggest && (suggestData.products.length || suggestData.hotWords.length)">
            <div v-for="p in suggestData.products" :key="p" class="suggest-item" @mousedown.prevent="onSuggestSelect(p)">
              <span class="suggest-icon">📱</span>
              <span class="suggest-text">{{ p }}</span>
            </div>
            <div v-for="h in suggestData.hotWords" :key="'hot-'+h" class="suggest-item hot" @mousedown.prevent="onSuggestSelect(h)">
              <span class="suggest-icon">🔥</span>
              <span class="suggest-text">热搜：{{ h }}</span>
            </div>
          </div>
        </div>
        <nav class="nav-links">
          <!-- User / Admin dropdown -->
          <span class="nav-link cart-link" @mouseenter="showCartPanel = cartStore.cartList.length > 0" style="margin-top: 10px">
            🛒 购物车
            <span class="cart-badge" v-if="cartStore.totalCount">{{ cartStore.totalCount }}</span>
          </span>
          <router-link to="/order/list" class="nav-link" style="margin-top: 10px">📋 订单</router-link>
          <span class="nav-link user-menu" v-if="userStore.isLoggedIn" @click="showUserMenu = !showUserMenu">
            <img :src="userStore.userInfo?.avatar || '/default-avatar.svg'" class="nav-avatar" @error="$event.target.src='/default-avatar.svg'" />
            <span class="nav-nickname">{{ userStore.userName || '我的' }}</span>
            <span class="nav-arrow" :class="{ rotated: showUserMenu }">▾</span>
          </span>
          <router-link to="/login" class="nav-link" v-else>登录</router-link>
        </nav>
      </div>
    </header>

    <!-- 购物车滑出面板 -->
    <div class="cart-panel-overlay" v-if="showCartPanel" @click="showCartPanel = false" />
    <div class="cart-panel" v-if="showCartPanel" @mouseleave="showCartPanel = false">
      <div class="cart-panel-header">购物车 ({{ cartStore.totalCount }})</div>
      <div class="cart-panel-list" v-if="cartStore.cartList.length">
        <div class="cart-panel-item" v-for="item in cartStore.cartList" :key="item.id">
          <img :src="item.image || '/api/v1/files/default/product'" class="cart-panel-img" />
          <div class="cart-panel-info">
            <div class="cart-panel-name">{{ item.name }}</div>
            <div class="cart-panel-price">¥{{ item.price }} × {{ item.quantity }}</div>
          </div>
          <span class="cart-panel-remove" @click="cartStore.removeFromCart(item.id)">✕</span>
        </div>
      </div>
      <div class="cart-panel-empty" v-else>购物车为空</div>
      <button class="cart-panel-btn" v-if="cartStore.cartList.length" @click="goCheckout">去结算</button>
    </div>

    <!-- 用户下拉菜单 -->
    <div class="user-drop" v-if="showUserMenu">
      <div class="user-drop-hd">
        <img :src="userStore.userInfo?.avatar || '/default-avatar.svg'" class="ud-avatar" @error="$event.target.src='/default-avatar.svg'" />
        <div class="ud-info">
          <strong>{{ userStore.userName }}</strong>
          <span class="ud-role" :class="{ admin: userStore.isAdmin, store: userStore.isStore }">{{ userStore.isAdmin ? '管理员' : userStore.isStore ? '店家' : '普通用户' }}</span>
        </div>
      </div>
      <div class="user-drop-menu">
        <router-link to="/user" class="udm" @click="showUserMenu=false"><span class="udm-icon">🏠</span>个人中心</router-link>
        <router-link to="/order/list" class="udm" @click="showUserMenu=false"><span class="udm-icon">📋</span>订单管理</router-link>
        <div class="ud-divider" />
        <router-link to="/user/profile" class="udm" @click="showUserMenu=false"><span class="udm-icon">✏️</span>编辑资料</router-link>
        <router-link to="/user/address" class="udm" @click="showUserMenu=false"><span class="udm-icon">📍</span>收货地址</router-link>
        <div class="ud-divider" />
        <a class="udm logout" @click="userStore.logout();showUserMenu=false;router.push('/login')"><span class="udm-icon">🚪</span>退出登录</a>
      </div>
    </div>
    <div class="user-drop-overlay" v-if="showUserMenu" @click="showUserMenu=false" />

    <!-- 主体区域 -->
    <div class="main-area">
      <!-- 左侧分类 -->
      <aside class="category-side">
        <nav class="category-nav">
          <router-link
            v-for="cat in categories" :key="cat.id"
            :to="`/category/${cat.id}`"
            class="category-item"
            :class="{ active: route.params.id === String(cat.id) }"
            @mouseenter="hoverCategory = cat"
          >
            {{ cat.name }}
          </router-link>
        </nav>
        <!-- 分类悬浮面板 -->
        <div class="category-panel" v-if="hoverCategory" @mouseleave="hoverCategory = null">
          <div class="category-panel-inner">
            <div class="cat-group" v-for="child in hoverCategory.children" :key="child.id">
              <router-link :to="`/category/${child.id}`" class="cat-group-title">{{ child.name }}</router-link>
              <div class="cat-group-children" v-if="child.children">
                <router-link v-for="sub in child.children" :key="sub.id" :to="`/category/${sub.id}`" class="cat-child">
                  {{ sub.name }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧内容 -->
      <main class="content-area">
        <router-view />
      </main>
    </div>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-col"><strong>ShopMax</strong><br>品质生活电商平台</div>
        <div class="footer-col"><strong>帮助</strong><br>购物指南<br>支付方式<br>配送说明</div>
        <div class="footer-col"><strong>服务</strong><br>售后服务<br>投诉建议<br>联系客服</div>
        <div class="footer-col"><strong>关于</strong><br>关于我们<br>隐私政策<br>用户协议</div>
      </div>
      <div class="footer-bottom">© 2026 ShopMax. All rights reserved. 浙ICP备XXXXXXXX号</div>
    </footer>

    <!-- 回到顶部 -->
    <button class="back-top-btn" v-if="showBackTop" @click="backToTop">↑</button>
    <!-- 客服悬浮 -->
    <button class="cs-float-btn" @click="showChatWindow = !showChatWindow">💬</button>
    <CsChatWindow v-if="showChatWindow" @close="showChatWindow = false" />
  </div>
</template>

<style scoped lang="scss">
.layout-default {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// ── 顶部导航 ────────────────────────────────────
.top-nav {
  position: sticky; top: 0; z-index: 100;
  height: $header-height;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid $border-color;
}
.top-nav-inner {
  max-width: $container-max; margin: 0 auto;
  height: 100%; display: flex; align-items: center; gap: $spacing-xl;
  padding: 0 $spacing-xl;
}
.logo {
  font-size: $font-size-xl; font-weight: 800; color: $brand-orange; flex-shrink: 0;
  letter-spacing: -0.5px;
}
.search-wrap {
  flex: 1; max-width: 520px; display: flex; position: relative;
}
.search-input {
  flex: 1; height: 38px; padding: 0 16px;
  border: 2px solid $brand-orange; border-radius: 19px 0 0 19px;
  outline: none; font-size: $font-size-sm;
  &:focus { box-shadow: 0 0 0 3px rgba($brand-orange, 0.1); }
}
.search-btn {
  width: 64px; height: 38px;
  background: $brand-gradient; color: #fff; border: none;
  border-radius: 0 19px 19px 0; cursor: pointer; font-size: 18px;
}
.suggest-dropdown {
  position: absolute; top: 100%; left: 0; right: 0; z-index: 300;
  background: #fff; border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  max-height: 360px; overflow-y: auto;
}
.suggest-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; cursor: pointer; font-size: 13px; color: #333;
  &:hover { background: #f5f5f5; }
  &.hot { color: #FF5000; }
  .suggest-icon { font-size: 14px; flex-shrink: 0; }
}
.nav-links { display: flex; margin-left: 400px; gap: $spacing-lg; flex-shrink: 0; }
.nav-link {
  font-size: $font-size-sm; color: $text-secondary; cursor: pointer;
  white-space: nowrap; transition: color $transition-fast;
  &:hover { color: $brand-orange; }
}
.cart-link { position: relative; }
.cart-badge {
  position: absolute; top: -6px; right: -10px;
  min-width: 16px; height: 16px; padding: 0 4px;
  background: $brand-orange; color: #fff; border-radius: 8px;
  font-size: 10px; display: flex; align-items: center; justify-content: center;
}

// ── 购物车滑出面板 ──────────────────────────────
.cart-panel-overlay { position: fixed; inset: 0; z-index: 200; }
.cart-panel {
  position: fixed; top: 0; right: 0; bottom: 0; width: 340px; z-index: 201;
  background: #fff; box-shadow: $shadow-lg; display: flex; flex-direction: column;
  animation: slideIn 0.2s ease-out;
}
@keyframes slideIn { from { transform: translateX(100%); } to { transform: translateX(0); } }
.cart-panel-header { padding: $spacing-base; font-weight: 700; border-bottom: 1px solid $border-color; }
.cart-panel-list { flex: 1; overflow-y: auto; padding: $spacing-md; }
.cart-panel-item { display: flex; gap: $spacing-md; padding: $spacing-sm 0; align-items: center; }
.cart-panel-img { width: 56px; height: 56px; border-radius: $radius-sm; object-fit: cover; background: $bg-input; }
.cart-panel-info { flex: 1; font-size: $font-size-xs; }
.cart-panel-name { color: $text-primary; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cart-panel-price { color: $brand-orange; font-weight: 600; }
.cart-panel-remove { cursor: pointer; color: $text-hint; &:hover { color: $color-danger; } }
.cart-panel-empty { text-align: center; padding: 40px; color: $text-hint; }
.cart-panel-btn {
  margin: $spacing-md; height: 44px;
  background: $brand-gradient; color: #fff; border: none; border-radius: 22px;
  font-size: $font-size-base; font-weight: 600; cursor: pointer;
}

// ── 主区域 ──────────────────────────────────────
.main-area {
  flex: 1; display: flex;
  max-width: $container-max; margin: 0 auto; width: 100%;
}
.category-side { width: $category-width; flex-shrink: 0; position: relative; background: $bg-card; border-right: 1px solid $border-light; }
.category-nav { padding: $spacing-sm 0; }
.category-item {
  display: block; padding: 10px $spacing-xl;
  font-size: $font-size-sm; color: $text-secondary; cursor: pointer;
  transition: all $transition-fast;
  &:hover, &.active { color: $brand-orange; background: $brand-light; font-weight: 600; }
}
.category-panel {
  position: absolute; left: 100%; top: 0; z-index: 50;
  width: 640px; min-height: 400px;
  background: #fff; box-shadow: $shadow-md; border-radius: 0 $radius-md $radius-md $radius-md;
  animation: panelIn 0.15s ease-out;
}
@keyframes panelIn { from { opacity: 0; transform: translateX(-8px); } to { opacity: 1; transform: none; } }
.category-panel-inner { padding: $spacing-xl; display: grid; grid-template-columns: repeat(3, 1fr); gap: $spacing-xl; }
.cat-group-title { font-weight: 600; font-size: $font-size-sm; color: $text-primary; display: block; margin-bottom: $spacing-sm; }
.cat-child { display: block; font-size: $font-size-xs; color: $text-secondary; padding: 3px 0; &:hover { color: $brand-orange; } }
.content-area { flex: 1; min-width: 0; padding: $spacing-xl; }

// ── 页脚 ────────────────────────────────────────
.site-footer { background: #fff; border-top: 1px solid $border-color; margin-top: auto; }
.footer-inner { max-width: $container-max; margin: 0 auto; display: flex; gap: $spacing-3xl; padding: $spacing-3xl $spacing-xl; }
.footer-col { font-size: $font-size-xs; color: $text-secondary; line-height: 2; flex: 1; }
.footer-bottom { text-align: center; padding: $spacing-base; font-size: $font-size-xs; color: $text-hint; border-top: 1px solid $border-light; }

// ── 悬浮按钮 ────────────────────────────────────
.back-top-btn, .cs-float-btn {
  position: fixed; right: $spacing-xl; z-index: 90;
  width: 44px; height: 44px; border-radius: 50%; border: 1px solid $border-color;
  background: #fff; box-shadow: $shadow-base; cursor: pointer;
  font-size: 20px; display: flex; align-items: center; justify-content: center;
  transition: all $transition-fast;
  &:hover { box-shadow: $shadow-md; transform: translateY(-2px); }
}
.back-top-btn { bottom: 100px; }
.cs-float-btn { bottom: 40px; }

// 用户下拉菜单
.user-menu {
  cursor: pointer; position: relative;
  display: flex; align-items: center; gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  background: $bg-hover;
  transition: all $transition-fast;
  &:hover { background: $brand-light; }
}
.nav-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  object-fit: cover; border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.nav-nickname {
  font-size: $font-size-sm; color: $text-primary; font-weight: 500;
  max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.nav-arrow {
  font-size: 10px; color: $text-hint; transition: transform 0.2s;
  &.rotated { transform: rotate(180deg); }
}
.user-drop-overlay { position: fixed; inset: 0; z-index: 250; }
.user-drop {
  position: fixed; top: $header-height + 8px; right: 40px; z-index: 251;
  width: 280px; background: #fff; border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden; animation: dropIn .18s ease-out;
}
@keyframes dropIn { from { opacity: 0; transform: translateY(-12px) scale(0.96); } to { opacity: 1; transform: none; } }
.user-drop-hd {
  display: flex; align-items: center; gap: 14px; padding: 20px;
  background: linear-gradient(135deg, #FFF3EC 0%, #FFF8F5 100%);
  border-bottom: 1px solid $border-light;
}
.ud-avatar {
  width: 48px; height: 48px; border-radius: 50%;
  object-fit: cover; border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); flex-shrink: 0;
}
.ud-info {
  flex: 1; min-width: 0;
  strong { font-size: 16px; font-weight: 600; display: block; color: $text-primary; }
  .ud-role {
    display: inline-block; font-size: 11px; padding: 2px 8px; border-radius: 10px;
    margin-top: 4px; font-weight: 500;
    background: #f0f0f0; color: $text-secondary;
    &.admin { background: #FFF3EC; color: $brand-orange; }
    &.store { background: #E8F5E9; color: #2E7D32; }
  }
}
.user-drop-menu { padding: 6px 0; }
.udm {
  display: flex; align-items: center; gap: 10px;
  padding: 11px 20px; font-size: 14px; color: $text-primary;
  text-decoration: none; cursor: pointer;
  transition: background 0.15s;
  &:hover { background: $bg-hover; }
  &.logout { color: $color-danger; &:hover { background: #FFF5F5; } }
}
.udm-icon { width: 20px; text-align: center; flex-shrink: 0; font-size: 14px; }
.ud-divider { height: 1px; background: $border-light; margin: 4px 16px; }
</style>

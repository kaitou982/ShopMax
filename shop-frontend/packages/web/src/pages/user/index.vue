<script setup lang="ts">
defineOptions({ name: 'UserPage' })

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { userApi, addressApi, couponApi, type AddressInfo } from '@shop/shared'

const router = useRouter()
const userStore = useUserStore()

// ── 编辑资料 ──
const showEdit = ref(false)
const pf = ref({ nickname: '', avatar: '', gender: 0, birthday: '' })
const pfSaving = ref(false)
const pfMsg = ref('')

const openEdit = () => {
  pf.value = {
    nickname: userStore.userInfo?.nickname || '',
    avatar: userStore.userInfo?.avatar || '',
    gender: userStore.userInfo?.gender || 0,
    birthday: userStore.userInfo?.birthday || '',
  }
  pfMsg.value = ''
  showEdit.value = true
}

const cancelEdit = () => { showEdit.value = false; pfMsg.value = '' }

const saveProfile = async () => {
  pfSaving.value = true; pfMsg.value = ''
  try {
    await userStore.updateUserInfo(pf.value)
    pfMsg.value = '保存成功'
    setTimeout(() => { showEdit.value = false; pfMsg.value = '' }, 800)
  } catch (e: any) {
    pfMsg.value = e?.message || '保存失败'
  } finally {
    pfSaving.value = false
  }
}

// ── 头像上传 ──
const avatarUploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const triggerUpload = () => { fileInput.value?.click() }

const onFileChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { pfMsg.value = '图片大小不能超过5MB'; return }
  avatarUploading.value = true
  try {
    const url = await userApi.uploadAvatar(file)
    pf.value.avatar = url
  } catch (e: any) {
    pfMsg.value = e?.message || '上传失败'
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

// ── 地址管理 ──
const addrList = ref<AddressInfo[]>([])
const addrShowForm = ref(false)
const addrEdit = ref<AddressInfo | null>(null)
const af = ref({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '' })
const addrSaving = ref(false)
const couponCount = ref(0)

const loadAddr = async () => { try { addrList.value = await addressApi.getList() } catch { /* noop */ } }

const openAddrAdd = () => {
  addrEdit.value = null
  af.value = { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '' }
  addrShowForm.value = true
}

const openAddrEdit = (a: AddressInfo) => {
  addrEdit.value = a
  af.value = { receiverName: a.receiverName, receiverPhone: a.receiverPhone, province: a.province, city: a.city, district: a.district, detailAddress: a.detailAddress }
  addrShowForm.value = true
}

const cancelAddr = () => { addrShowForm.value = false }

const saveAddr = async () => {
  addrSaving.value = true
  try {
    if (addrEdit.value) {
      await addressApi.update(addrEdit.value.addressId, { ...af.value, isDefault: false })
    } else {
      await addressApi.create({ ...af.value, isDefault: false })
    }
    addrShowForm.value = false
    await loadAddr()
  } catch { /* noop */ } finally { addrSaving.value = false }
}

const delAddr = async (id: number) => { await addressApi.delete(id); await loadAddr() }
const setDftAddr = async (id: number) => { await addressApi.setDefault(id); await loadAddr() }

onMounted(async () => {
  loadAddr()
  try { couponCount.value = (await couponApi.getMyCoupons())?.total || 0 } catch { /* noop */ }
})
</script>

<template>
  <div class="user-page">
    <h2>个人中心</h2>

    <!-- ── 用户概要 ── -->
    <div class="user-summary">
      <div class="us-left">
        <div class="avatar-wrap" @click="triggerUpload" title="点击更换头像">
          <img :src="userStore.userInfo?.avatar || '/default-avatar.svg'" class="us-avatar" @error="$event.target.src='/default-avatar.svg'" />
          <div class="avatar-overlay"><span>更换头像</span></div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
        <div class="us-info">
          <div class="us-name-row">
            <span class="us-name">{{ userStore.userName || '用户' }}</span>
            <span class="us-role" :class="{ admin: userStore.isAdmin, store: userStore.isStore }">
              {{ userStore.isAdmin ? '管理员' : userStore.isStore ? '店家' : '普通用户' }}
            </span>
            <span class="us-level" v-if="userStore.userInfo?.memberLevelName">{{ userStore.userInfo.memberLevelName }}</span>
          </div>
          <div class="us-meta" v-if="userStore.userInfo?.phone">手机号：{{ userStore.userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</div>
        </div>
      </div>
      <div class="us-stats">
        <div class="stat-item">
          <span class="stat-val">{{ (userStore.userInfo?.balance || 0).toFixed(2) }}</span>
          <span class="stat-label">余额</span>
        </div>
        <div class="stat-item">
          <span class="stat-val">{{ userStore.userInfo?.integral || 0 }}</span>
          <span class="stat-label">积分</span>
        </div>
        <div class="stat-item clickable" @click="router.push('/user/coupons')">
          <span class="stat-val">{{ couponCount }}</span>
          <span class="stat-label">优惠券</span>
        </div>
        <div class="stat-item">
          <span class="stat-val">{{ userStore.userInfo?.growthValue || 0 }}</span>
          <span class="stat-label">成长值</span>
        </div>
      </div>
      <button class="edit-btn" v-if="!showEdit" @click="openEdit">编辑资料</button>
    </div>

    <!-- ── 编辑资料（内联）── -->
    <div class="edit-section" v-if="showEdit">
      <div class="es-message" v-if="pfMsg" :class="{ error: pfMsg.includes('失败') || pfMsg.includes('超过') }">{{ pfMsg }}</div>
      <div class="es-row">
        <span class="es-label">头像</span>
        <div class="es-avatar-row">
          <img :src="pf.avatar || '/default-avatar.svg'" class="es-avatar" @error="$event.target.src='/default-avatar.svg'" />
          <button class="es-upload-btn" @click="triggerUpload" :disabled="avatarUploading">{{ avatarUploading ? '上传中...' : '更换头像' }}</button>
        </div>
      </div>
      <div class="es-row">
        <span class="es-label">昵称</span>
        <input v-model="pf.nickname" class="es-input" placeholder="请输入昵称" />
      </div>
      <div class="es-row">
        <span class="es-label">性别</span>
        <select v-model.number="pf.gender" class="es-input">
          <option :value="0">保密</option>
          <option :value="1">男</option>
          <option :value="2">女</option>
        </select>
      </div>
      <div class="es-row">
        <span class="es-label">生日</span>
        <input type="date" v-model="pf.birthday" class="es-input" />
      </div>
      <div class="es-actions">
        <button class="es-cancel" @click="cancelEdit">取消</button>
        <button class="es-save" :disabled="pfSaving" @click="saveProfile">{{ pfSaving ? '保存中...' : '保存' }}</button>
      </div>
    </div>

    <!-- ── 收货地址 ── -->
    <div class="addr-section">
      <div class="addr-header">
        <h3>收货地址</h3>
        <button class="addr-add-btn" @click="openAddrAdd">+ 新增</button>
      </div>

      <!-- 新增/编辑表单 (内联) -->
      <div class="addr-form" v-if="addrShowForm">
        <div class="af-row">
          <input v-model="af.receiverName" placeholder="收货人姓名" class="af-input" />
          <input v-model="af.receiverPhone" placeholder="手机号" class="af-input" />
        </div>
        <div class="af-row af-row-3">
          <input v-model="af.province" placeholder="省" class="af-input" />
          <input v-model="af.city" placeholder="市" class="af-input" />
          <input v-model="af.district" placeholder="区" class="af-input" />
        </div>
        <input v-model="af.detailAddress" placeholder="详细地址（街道、门牌号等）" class="af-input af-full" />
        <div class="af-actions">
          <button class="af-cancel" @click="cancelAddr">取消</button>
          <button class="af-save" :disabled="addrSaving" @click="saveAddr">{{ addrSaving ? '保存中...' : (addrEdit ? '更新' : '保存') }}</button>
        </div>
      </div>

      <div class="addr-empty" v-if="!addrList.length && !addrShowForm">暂无收货地址</div>

      <div class="addr-item" v-for="a in addrList" :key="a.addressId">
        <div class="addr-item-info">
          <div class="addr-item-hd">
            <strong>{{ a.receiverName }}</strong>
            <span class="addr-phone">{{ a.receiverPhone }}</span>
            <span class="addr-dft" v-if="a.isDefault">默认</span>
          </div>
          <p class="addr-detail">{{ a.fullAddress || [a.province, a.city, a.district, a.detailAddress].filter(Boolean).join('') }}</p>
        </div>
        <div class="addr-item-actions">
          <button @click="openAddrEdit(a)">编辑</button>
          <button @click="delAddr(a.addressId)">删除</button>
          <button v-if="!a.isDefault" @click="setDftAddr(a.addressId)">设为默认</button>
        </div>
      </div>
    </div>

    <!-- ── 退出登录 ── -->
    <div class="logout-section">
      <button class="logout-btn" @click="userStore.logout(); router.push('/')">退出登录</button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.user-page {
  max-width: 900px;
}

h2 { margin-bottom: $spacing-xl; }
h3 { margin: 0; }

// ── 概要 ────────────────────────────────────────
.user-summary {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $spacing-2xl;
  margin-bottom: $spacing-lg;
  position: relative;
}

.us-left {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.avatar-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  &:hover .avatar-overlay { opacity: 1; }
}

.us-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity $transition-fast;
  border-radius: 50%;
  span { color: #fff; font-size: $font-size-xs; }
}

.us-info { min-width: 0; }

.us-name-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.us-name {
  font-size: $font-size-xl;
  font-weight: 700;
  color: $text-primary;
}

.us-role {
  display: inline-block;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 500;
  background: #f0f0f0;
  color: $text-secondary;
  &.admin { background: $brand-light; color: $brand-orange; }
  &.store { background: #E8F5E9; color: #2E7D32; }
}

.us-level {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  font-weight: 500;
}

.us-meta {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.us-stats {
  display: flex;
  gap: $spacing-3xl;
  margin-bottom: $spacing-lg;
  padding-left: 88px; // align with text after avatar
}

.stat-item {
  text-align: center;
  &.clickable { cursor: pointer; &:hover { .stat-val { color: $brand-orange; } } }
}

.stat-val {
  display: block;
  font-size: $font-size-2xl;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 2px;
}

.stat-label {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.edit-btn {
  position: absolute;
  top: $spacing-2xl;
  right: $spacing-2xl;
  padding: 6px 20px;
  border: 1px solid $brand-orange;
  background: #fff;
  color: $brand-orange;
  border-radius: 20px;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover { background: $brand-orange; color: #fff; }
}

// ── 编辑资料 ────────────────────────────────────
.edit-section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl;
  margin-bottom: $spacing-lg;
}

.es-message {
  padding: 8px 14px;
  border-radius: 8px;
  font-size: $font-size-sm;
  margin-bottom: $spacing-base;
  background: #e8f5e9;
  color: #2e7d32;
  &.error { background: #ffebee; color: #c62828; }
}

.es-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid $border-light;
  &:last-of-type { border-bottom: none; }
}

.es-label {
  width: 80px;
  font-size: $font-size-base;
  color: $text-secondary;
  flex-shrink: 0;
}

.es-input {
  flex: 1;
  max-width: 320px;
  height: 36px;
  border: 1px solid $border-color;
  border-radius: $radius-base;
  padding: 0 12px;
  font-size: $font-size-base;
  outline: none;
  &:focus { border-color: $brand-orange; }
}

.es-avatar-row {
  display: flex;
  align-items: center;
  gap: $spacing-base;
}

.es-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.es-upload-btn {
  padding: 6px 16px;
  border: 1px solid $border-color;
  background: #fff;
  border-radius: 16px;
  font-size: $font-size-xs;
  cursor: pointer;
  color: $text-secondary;
  transition: all $transition-fast;
  &:hover { border-color: $brand-orange; color: $brand-orange; }
  &:disabled { opacity: 0.5; cursor: default; }
}

.es-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
  margin-top: $spacing-lg;
  padding-top: $spacing-base;
  border-top: 1px solid $border-light;
}

.es-cancel {
  padding: 8px 24px;
  border: 1px solid $border-color;
  background: #fff;
  border-radius: 20px;
  font-size: $font-size-sm;
  cursor: pointer;
  color: $text-secondary;
}

.es-save {
  padding: 8px 24px;
  border: none;
  background: $brand-gradient;
  color: #fff;
  border-radius: 20px;
  font-size: $font-size-sm;
  font-weight: 600;
  cursor: pointer;
  &:disabled { opacity: 0.5; }
}

// ── 地址 ────────────────────────────────────────
.addr-section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl;
}

.addr-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.addr-add-btn {
  padding: 6px 18px;
  border: 1px solid $brand-orange;
  background: #fff;
  color: $brand-orange;
  border-radius: 16px;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover { background: $brand-orange; color: #fff; }
}

.addr-empty {
  text-align: center;
  padding: 40px 0;
  color: $text-hint;
  font-size: $font-size-sm;
}

.addr-form {
  background: $bg-page;
  border-radius: $radius-base;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  border: 1px solid $border-light;
}

.af-row {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.af-row-3 .af-input {
  flex: 1;
}

.af-input {
  height: 38px;
  border: 1px solid $border-color;
  border-radius: $radius-base;
  padding: 0 12px;
  font-size: $font-size-sm;
  outline: none;
  box-sizing: border-box;
  &:focus { border-color: $brand-orange; }
}

.af-full {
  width: 100%;
  margin-bottom: $spacing-md;
}

.af-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
}

.af-cancel {
  padding: 6px 20px;
  border: 1px solid $border-color;
  background: #fff;
  border-radius: 16px;
  font-size: $font-size-sm;
  cursor: pointer;
  color: $text-secondary;
}

.af-save {
  padding: 6px 20px;
  border: none;
  background: $brand-gradient;
  color: #fff;
  border-radius: 16px;
  font-size: $font-size-sm;
  font-weight: 600;
  cursor: pointer;
  &:disabled { opacity: 0.5; }
}

// ── 地址列表项 ──────────────────────────────────
.addr-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid $border-light;

  &:last-child {
    border-bottom: none;
  }
}

.addr-item-info {
  flex: 1;
  min-width: 0;
}

.addr-item-hd {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: 6px;

  strong {
    font-size: $font-size-base;
    color: $text-primary;
  }
}

.addr-phone {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.addr-dft {
  font-size: 10px;
  background: $brand-light;
  color: $brand-orange;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.addr-detail {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0;
}

.addr-item-actions {
  display: flex;
  gap: $spacing-sm;
  flex-shrink: 0;
  margin-left: $spacing-base;

  button {
    padding: 4px 14px;
    border: 1px solid $border-color;
    background: #fff;
    border-radius: 12px;
    font-size: $font-size-xs;
    cursor: pointer;
    color: $text-secondary;
    transition: all $transition-fast;
    white-space: nowrap;

    &:hover {
      border-color: $brand-orange;
      color: $brand-orange;
    }
  }
}

// ── 退出登录 ────────────────────────────────────
.logout-section {
  text-align: center;
  padding: $spacing-2xl 0;
}

.logout-btn {
  padding: 10px 48px;
  border: 1px solid $color-danger;
  background: #fff;
  color: $color-danger;
  border-radius: 22px;
  font-size: $font-size-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover { background: $color-danger; color: #fff; }
}
</style>

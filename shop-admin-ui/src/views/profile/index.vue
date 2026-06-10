<script setup lang="ts">
defineOptions({ name: 'Profile' })

import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { useUserStore } from '@/stores/modules/user'
import { updateCurrentUser, changePassword, applyStore } from '@/api/modules/user'
import type { UserUpdateRequest, ChangePasswordRequest } from '@/types/api'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

// ── Avatar ─────────────────────────────────────
const avatarDialogVisible = ref(false)
const avatarPreview = ref('')
const avatarFile = ref<File | null>(null)
const avatarLoading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const openAvatarDialog = () => {
  avatarPreview.value = userStore.avatar || ''
  avatarFile.value = null
  avatarDialogVisible.value = true
}

const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

const resizeImage = (file: File, maxSize: number): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        let { width, height } = img
        if (width > maxSize || height > maxSize) {
          if (width > height) {
            height = Math.round(height * (maxSize / width))
            width = maxSize
          } else {
            width = Math.round(width * (maxSize / height))
            height = maxSize
          }
        }
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')!
        ctx.drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', 0.85))
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target?.result as string
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

const handleFileChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  avatarFile.value = file
  avatarPreview.value = URL.createObjectURL(file)
  input.value = ''
}

const handleAvatarDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  avatarFile.value = file
  avatarPreview.value = URL.createObjectURL(file)
}

const saveAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.warning('请先选择头像图片')
    return
  }
  avatarLoading.value = true
  try {
    const base64 = await resizeImage(avatarFile.value, 200)
    await updateCurrentUser({ avatar: base64 })
    await userStore.refreshUserInfo()
    ElMessage.success('头像更新成功')
    avatarDialogVisible.value = false
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarLoading.value = false
  }
}

const removeAvatarFile = () => {
  avatarFile.value = null
  avatarPreview.value = userStore.avatar || ''
}

// ── Profile form ───────────────────────────────
const profileFormRef = ref<FormInstance>()
const profileForm = reactive<UserUpdateRequest>({
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  birthday: ''
})
const profileLoading = ref(false)

const profileRules: FormRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const resetProfileForm = () => {
  const info = userStore.userInfo
  if (!info) return
  profileForm.nickname = info.nickname || ''
  profileForm.phone = info.phone || ''
  profileForm.email = info.email || ''
  profileForm.gender = info.gender ?? 0
  profileForm.birthday = info.birthday || ''
}

const handleProfileSubmit = async () => {
  if (!profileFormRef.value) return
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  profileLoading.value = true
  try {
    await updateCurrentUser({
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      email: profileForm.email,
      gender: profileForm.gender,
      birthday: profileForm.birthday
    })
    await userStore.refreshUserInfo()
    ElMessage.success('个人资料更新成功')
  } catch {
    // error handled by interceptor
  } finally {
    profileLoading.value = false
  }
}

// ── Password form ──────────────────────────────
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive<ChangePasswordRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordLoading = ref(false)

const validateConfirmPassword = (_rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (value && value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch {
    // error handled by interceptor
  } finally {
    passwordLoading.value = false
  }
}

// ── Store Apply ───────────────────────────────
const storeDialogVisible = ref(false)
const storeForm = reactive({
  storeName: '',
  storeLogo: '',
  storeDescription: ''
})
const storeApplyLoading = ref(false)

const openStoreApply = () => {
  storeForm.storeName = ''
  storeForm.storeLogo = ''
  storeForm.storeDescription = ''
  storeDialogVisible.value = true
}

const doApplyStore = async () => {
  if (!storeForm.storeName.trim()) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  storeApplyLoading.value = true
  try {
    await applyStore(storeForm)
    ElMessage.success('入驻申请已提交，请等待审核')
    storeDialogVisible.value = false
    userStore.refreshUserInfo()
  } finally {
    storeApplyLoading.value = false
  }
}

// ── Init ───────────────────────────────────────
watch(() => userStore.userInfo, () => {
  resetProfileForm()
}, { immediate: true })

</script>

<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <!-- Store Section -->
    <div v-if="userStore.userRole === 'STORE'" class="store-status-bar">
      <span v-if="userStore.storeStatus === 0" class="store-pending">⏳ 店铺审核中，请耐心等待</span>
      <span v-else-if="userStore.storeStatus === 1" class="store-approved">✅ 店铺已通过审核 — {{ userStore.userInfo?.storeName }}</span>
      <span v-else-if="userStore.storeStatus === 2" class="store-rejected">❌ 入驻申请已拒绝，请重新申请</span>
    </div>
    <div v-if="userStore.isAdmin" class="admin-banner">
      <el-tag type="danger" size="large">管理员账号</el-tag>
    </div>

    <!-- Store Apply Dialog -->
    <el-dialog v-model="storeDialogVisible" title="申请成为店家" width="480px">
      <el-form :model="storeForm" label-width="80px">
        <el-form-item label="店铺名称" required>
          <el-input v-model="storeForm.storeName" placeholder="请输入店铺名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="店铺Logo">
          <ImageUpload v-model="storeForm.storeLogo" upload-type="avatar" />
        </el-form-item>
        <el-form-item label="店铺简介">
          <el-input v-model="storeForm.storeDescription" type="textarea" :rows="3" placeholder="简要介绍您的店铺（可选）" maxlength="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="storeApplyLoading" @click="doApplyStore">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- User Info Card -->
    <div class="profile-card">
      <div class="profile-hero">
        <div class="avatar-section" @click="openAvatarDialog">
          <el-avatar :size="100" :src="userStore.avatar" class="hero-avatar">
            {{ userStore.userName?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="avatar-overlay">
            <el-icon><Edit /></el-icon>
            <span>修改头像</span>
          </div>
        </div>
        <div class="hero-info">
          <div class="hero-name">{{ userStore.userName || '--' }}</div>
          <div class="hero-meta">
            <el-tag :type="userStore.isAdmin ? 'danger' : 'primary'" size="small">
              {{ userStore.isAdmin ? '管理员' : '店家' }}
            </el-tag>
            <span class="hero-username">@{{ userInfo?.username || '--' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-two-col">
      <!-- Edit Profile -->
      <el-card class="profile-card">
        <template #header>
          <span class="card-title">编辑资料</span>
        </template>
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="80px"
          label-position="left"
        >
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="30" show-word-limit />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :value="0">保密</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="生日">
            <el-date-picker
              v-model="profileForm.birthday"
              type="date"
              placeholder="请选择生日"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileLoading" @click="handleProfileSubmit">
              保存修改
            </el-button>
            <el-button @click="resetProfileForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- Change Password -->
      <el-card class="profile-card">
        <template #header>
          <span class="card-title">修改密码</span>
        </template>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="80px"
          label-position="left"
        >
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="passwordLoading" @click="handlePasswordSubmit">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- Avatar Edit Dialog -->
    <el-dialog v-model="avatarDialogVisible" title="修改头像" width="460px" :close-on-click-modal="false">
      <div class="avatar-dialog-body">
        <el-avatar :size="120" :src="avatarPreview" class="avatar-preview">
          {{ userStore.userName?.charAt(0) || 'U' }}
        </el-avatar>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileChange"
        />
        <div
          class="upload-zone"
          @click="triggerFileSelect"
          @dragover.prevent
          @drop.prevent="handleAvatarDrop"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <p class="upload-text">点击或拖拽图片到此处</p>
          <p class="upload-hint">支持 JPG、PNG 格式，大小不超过 5MB</p>
        </div>
        <div v-if="avatarFile" class="file-selected">
          <el-icon><PictureFilled /></el-icon>
          <span class="file-name">{{ avatarFile.name }}</span>
          <span class="file-size">{{ (avatarFile.size / 1024).toFixed(1) }} KB</span>
          <el-button type="danger" link size="small" @click="removeAvatarFile">移除</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="avatarLoading" :disabled="!avatarFile" @click="saveAvatar">
          确定上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.profile-page {
  max-width: 960px;
  margin: 0 auto;
}

// ── Hero Section ───────────────────────────────
.profile-card {
  margin-bottom: 20px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 32px;
  background: linear-gradient(135deg, #FFFAF5 0%, #FFF2EC 100%);
  border-radius: $radius-lg;
  border: 1px solid $border-color-light;
}

.avatar-section {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.hero-avatar {
  border: 3px solid $brand-orange;
  flex-shrink: 0;

  :deep(img) {
    object-fit: cover;
  }
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: $font-size-sm;
  opacity: 0;
  transition: opacity $transition-base;

  .el-icon {
    font-size: 22px;
  }
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.hero-name {
  font-size: $font-size-2xl;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 6px;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hero-username {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 24px;
  background: $bg-white;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: 700;
  color: $brand-orange;
  line-height: 1.2;
}

.stat-label {
  font-size: $font-size-xs;
  color: $text-secondary;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: $border-color-light;
}

// ── Two Column Layout ──────────────────────────
.profile-two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.card-title {
  font-weight: 600;
  font-size: $font-size-md;
}

// ── Avatar Dialog ──────────────────────────────
.avatar-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 12px 0;
}

.avatar-preview {
  border: 3px solid $border-color-base;
  flex-shrink: 0;

  :deep(img) {
    object-fit: cover;
  }
}

.upload-zone {
  width: 100%;
  padding: 28px 16px;
  border: 2px dashed $border-color-base;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all $transition-fast;
  background: $bg-color-page;

  &:hover {
    border-color: $brand-orange;
    background: $primary-lighter;
  }
}

.upload-icon {
  font-size: 36px;
  color: $text-placeholder;
}

.upload-zone:hover .upload-icon {
  color: $brand-orange;
}

.upload-text {
  font-size: $font-size-base;
  color: $text-regular;
  margin: 0;
}

.upload-hint {
  font-size: $font-size-xs;
  color: $text-placeholder;
  margin: 0;
}

.file-selected {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: $success-light;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  color: $success-color;

  .file-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-primary;
  }

  .file-size {
    color: $text-secondary;
    flex-shrink: 0;
  }
}

// ── Responsive ─────────────────────────────────
@media (max-width: 768px) {
  .profile-hero {
    flex-direction: column;
    text-align: center;
  }

  .hero-stats {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .profile-two-col {
    grid-template-columns: 1fr;
  }
}

.store-apply-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #FF5000, #FF8C3A);
  border-radius: 12px;
  padding: 20px 28px;
  margin-bottom: 20px;
  color: #fff;
  h3 { margin: 0 0 4px; font-size: 16px; }
  p { margin: 0; opacity: 0.9; font-size: 13px; }
}

.store-status-bar {
  padding: 14px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  .store-pending { color: #E6A23C; background: #FEF0D5; padding: 8px 16px; border-radius: 6px; }
  .store-approved { color: #67C23A; background: #E1F3D8; padding: 8px 16px; border-radius: 6px; }
  .store-rejected { color: #F56C6C; background: #FDE2E2; padding: 8px 16px; border-radius: 6px; }
}

.admin-banner {
  margin-bottom: 20px;
}
</style>

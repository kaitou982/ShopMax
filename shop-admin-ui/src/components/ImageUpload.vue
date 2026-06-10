<script setup lang="ts">
/**
 * ImageUpload 图片上传组件
 * @description 支持拖拽上传、预览、进度提示
 * @author shop
 * @since 2026-05-29
 */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/modules/file'

defineOptions({ name: 'ImageUpload' })

interface Props {
  modelValue: string
  uploadType?: string
  maxSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  uploadType: 'product',
  maxSize: 10
})

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const emit = defineEmits<Emits>()

const uploading = ref(false)
const imageUrl = ref(props.modelValue || '')

watch(() => props.modelValue, (val) => {
  imageUrl.value = val || ''
})

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt = file.size / 1024 / 1024 < props.maxSize
  if (!isLt) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

const handleUpload = async (options: { file: File }) => {
  const { file } = options
  if (!beforeUpload(file)) return
  uploading.value = true
  try {
    const result = await uploadFile(file, props.uploadType)
    imageUrl.value = result.url
    emit('update:modelValue', result.url)
    ElMessage.success('上传成功')
  } catch {
    // error handled in interceptor
  } finally {
    uploading.value = false
  }
}

const handleRemove = () => {
  imageUrl.value = ''
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="image-upload">
    <el-upload
      class="uploader"
      :show-file-list="false"
      :http-request="handleUpload"
      accept="image/*"
      drag
    >
      <div v-if="imageUrl" class="preview-wrap">
        <img :src="imageUrl" class="preview-img" />
        <div class="preview-mask">
          <el-icon :size="20"><Loading v-if="uploading" /></el-icon>
          <span v-if="!uploading">点击更换</span>
        </div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon v-if="uploading" class="is-loading" :size="28"><Loading /></el-icon>
        <el-icon v-else :size="28"><Plus /></el-icon>
        <span>{{ uploading ? '上传中...' : '点击或拖拽上传' }}</span>
      </div>
    </el-upload>
    <el-button v-if="imageUrl" link type="danger" size="small" @click="handleRemove">
      移除图片
    </el-button>
  </div>
</template>

<style scoped lang="scss">
.image-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.uploader {
  :deep(.el-upload) {
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    cursor: pointer;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }

  :deep(.el-upload-dragger) {
    width: 160px;
    height: 160px;
    padding: 0;
  }
}

.preview-wrap {
  position: relative;
  width: 160px;
  height: 160px;

  .preview-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .preview-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
    font-size: 14px;
    gap: 4px;
  }

  &:hover .preview-mask {
    opacity: 1;
  }
}

.upload-placeholder {
  width: 160px;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #c0c4cc;
  font-size: 13px;
}
</style>

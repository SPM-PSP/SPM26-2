<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'
import type { UserInfo } from '@/types/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const info = ref<UserInfo | null>(null)
const err = ref('')
const msg = ref('')
const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)

// 编辑模式
const isEditing = ref(false)
const editForm = ref({
  nickname: '',
  phone: '',
})

// 文件选择
const fileInput = ref<HTMLInputElement | null>(null)

// 头像加载状态
const avatarLoadError = ref(false)

onMounted(async () => {
  await loadUserInfo()
})

async function loadUserInfo() {
  loading.value = true
  err.value = ''
  avatarLoadError.value = false
  try {
    const res = await fetchUserInfo()
    if (res.code !== 200) {
      err.value = res.message || '获取失败'
      return
    }
    info.value = res.data
    // 初始化编辑表单
    editForm.value.nickname = res.data?.nickname || ''
    editForm.value.phone = res.data?.phone || ''
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

function startEdit() {
  isEditing.value = true
  // 重新加载表单数据
  if (info.value) {
    editForm.value.nickname = info.value.nickname || ''
    editForm.value.phone = info.value.phone || ''
  }
}

function cancelEdit() {
  isEditing.value = false
  err.value = ''
  msg.value = ''
}

async function saveEdit() {
  saving.value = true
  err.value = ''
  msg.value = ''
  
  try {
    const res = await updateUserInfo({
      nickname: editForm.value.nickname.trim() || undefined,
      phone: editForm.value.phone.trim() || undefined,
    })
    
    if (res.code !== 200) {
      err.value = res.message || '保存失败'
      return
    }
    
    msg.value = '保存成功'
    isEditing.value = false
    
    // 重新加载用户信息
    await loadUserInfo()
    
    // 更新 auth store 中的用户信息
    if (info.value) {
      auth.updateUserPreview({
        ...auth.userPreview,
        nickname: info.value.nickname,
      } as any)
    }
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

function triggerFileSelect() {
  fileInput.value?.click()
}

async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    err.value = '只支持 JPG、PNG、WEBP 格式的图片'
    return
  }
  
  // 验证文件大小（10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    err.value = '图片大小不能超过 10MB'
    return
  }
  
  uploading.value = true
  err.value = ''
  msg.value = ''
  
  try {
    const res = await uploadAvatar(file)
    
    if (res.code !== 200) {
      err.value = res.message || '上传失败'
      return
    }
    
    msg.value = '头像上传成功'
    
    // 先更新 auth store（用于导航栏立即显示）
    if (res.data) {
      auth.updateUserPreview({
        ...auth.userPreview,
        avatar: res.data.avatarUrl,
      } as any)
    }
    
    // 再重新加载用户信息（用于页面显示）
    await loadUserInfo()
    
    // 强制刷新图片，清除浏览器缓存
    avatarLoadError.value = false
    await new Promise(resolve => setTimeout(resolve, 100))
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '上传失败'
  } finally {
    uploading.value = false
    // 清空文件输入
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  console.error('头像加载失败，URL:', img.src)
  avatarLoadError.value = true
}

function formatDateTime(dateTime: string | undefined): string {
  if (!dateTime) return '未知'
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return '未知'
    return date.toLocaleString('zh-CN')
  } catch {
    return '未知'
  }
}
</script>

<template>
  <div class="page">
    <div class="header">
      <h1>个人资料</h1>
      <button v-if="!isEditing" type="button" class="btn-edit" @click="startEdit">
        ✏️ 编辑资料
      </button>
    </div>
    
    <div v-if="loading" class="card muted">加载中…</div>
    <div v-else-if="err" class="card err">{{ err }}</div>
    <div v-else-if="info" class="card">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <img 
            v-if="info.avatar && !avatarLoadError" 
            :src="info.avatar" 
            alt="头像" 
            class="avatar"
            @error="handleImageError"
          />
          <div v-else class="avatar ph">{{ info.nickname?.slice(0, 1) || '?' }}</div>
          
          <!-- 上传按钮 -->
          <button 
            type="button" 
            class="btn-upload-avatar" 
            :disabled="uploading"
            @click="triggerFileSelect"
            title="更换头像"
          >
            <span v-if="uploading" class="spinner" />
            <span v-else>📷</span>
          </button>
        </div>
        
        <input 
          ref="fileInput"
          type="file" 
          accept="image/jpeg,image/jpg,image/png,image/webp"
          style="display: none"
          @change="handleFileChange"
        />
        
        <div class="user-info">
          <div class="name">{{ info.nickname }}</div>
          <div class="muted">@{{ info.username }}</div>
        </div>
      </div>
      
      <!-- 编辑模式 -->
      <div v-if="isEditing" class="edit-form">
        <div class="form-group">
          <label for="edit-nickname">昵称</label>
          <input 
            id="edit-nickname"
            v-model="editForm.nickname" 
            type="text" 
            maxlength="20"
            placeholder="请输入昵称"
          />
        </div>
        
        <div class="form-group">
          <label for="edit-phone">手机号（可选）</label>
          <input 
            id="edit-phone"
            v-model="editForm.phone" 
            type="tel"
            maxlength="11"
            placeholder="请输入手机号"
          />
        </div>
        
        <div class="form-actions">
          <button type="button" class="btn btn-cancel" :disabled="saving" @click="cancelEdit">
            取消
          </button>
          <button type="button" class="btn btn-save" :disabled="saving" @click="saveEdit">
            <span v-if="saving" class="spinner-inline" />
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
      
      <!-- 查看模式 -->
      <dl v-else class="info-list">
        <dt>邮箱</dt>
        <dd>{{ info.email }}</dd>
        
        <dt v-if="info.phone">手机</dt>
        <dd v-if="info.phone">{{ info.phone }}</dd>
        
        <dt>用户 ID</dt>
        <dd>{{ info.userId }}</dd>
        
        <dt>注册时间</dt>
        <dd>{{ formatDateTime(info.createTime) }}</dd>
      </dl>
      
      <!-- 消息提示 -->
      <div v-if="msg" class="msg-success">{{ msg }}</div>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 600px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header h1 {
  margin: 0;
}

.btn-edit {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-edit:hover {
  background: var(--lc-accent-dim);
  border-color: var(--lc-accent);
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 24px;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--lc-border);
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--lc-border);
}

.avatar.ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  color: #fff;
  font-size: 2rem;
  font-weight: 700;
}

.btn-upload-avatar {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--lc-surface);
  background: var(--lc-accent);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.btn-upload-avatar:hover:not(:disabled) {
  transform: scale(1.1);
  background: #ff6b4a;
}

.btn-upload-avatar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.user-info {
  flex: 1;
}

.name {
  font-weight: 700;
  font-size: 1.25rem;
  margin-bottom: 4px;
}

/* 编辑表单 */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.85rem;
  color: var(--lc-text-muted);
  font-weight: 600;
}

.form-group input {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  font-size: 0.95rem;
  transition: all 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: var(--lc-accent);
  box-shadow: 0 0 0 3px rgba(255, 161, 22, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 8px;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  border: 1px solid var(--lc-border);
}

.btn-cancel:hover:not(:disabled) {
  background: var(--lc-border);
  color: var(--lc-text);
}

.btn-save {
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  color: #0c0e14;
}

.btn-save:hover:not(:disabled) {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 161, 22, 0.3);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 信息列表 */
.info-list {
  margin: 0;
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 12px 16px;
  font-size: 0.95rem;
}

.info-list dt {
  color: var(--lc-text-muted);
  font-weight: 600;
}

.info-list dd {
  margin: 0;
  color: var(--lc-text);
}

/* 消息提示 */
.msg-success {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(0, 184, 163, 0.1);
  border: 1px solid rgba(0, 184, 163, 0.3);
  color: var(--lc-easy);
  font-size: 0.9rem;
}

.muted {
  color: var(--lc-text-muted);
}

.err {
  color: var(--lc-red);
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.spinner-inline {
  display: inline-block;
  width: 14px;
  height: 14px;
  margin-right: 6px;
  border: 2px solid rgba(12, 14, 20, 0.3);
  border-top-color: #0c0e14;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  vertical-align: middle;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

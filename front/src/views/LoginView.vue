<template>
  <div class="login-page-viewport">
    <!-- Ambient Background Glows -->
    <div class="ambient-glow glow-1"></div>
    <div class="ambient-glow glow-2"></div>

    <!-- Centered Clean Auth Card -->
    <div class="login-auth-card">
      <!-- 1. Brand Logo & Title -->
      <div class="auth-brand-header">
        <div class="brand-logo-badge">
          <svg viewBox="0 0 40 40" fill="none" class="cyber-svg-logo" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="lNeon" x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#6366f1" />
                <stop offset="50%" stop-color="#8b5cf6" />
                <stop offset="100%" stop-color="#06b6d4" />
              </linearGradient>
              <linearGradient id="lCheck" x1="10" y1="30" x2="32" y2="8" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#10b981" />
                <stop offset="45%" stop-color="#06b6d4" />
                <stop offset="100%" stop-color="#38bdf8" />
              </linearGradient>
            </defs>
            <rect width="40" height="40" rx="11" fill="#090d16" />
            <rect x="0.75" y="0.75" width="38.5" height="38.5" rx="10.25" stroke="url(#lNeon)" stroke-opacity="0.5" stroke-width="1.5" />
            <rect x="9.5" y="9.5" width="4.5" height="21" rx="2.25" fill="url(#lNeon)" />
            <path
              d="M10.5 27.5L29.5 9.5C30.6 8.4 32.4 8.4 33.5 9.5C34.6 10.6 34.6 12.4 33.5 13.5L14.5 31.5C13.4 32.6 11.6 32.6 10.5 31.5L8.5 29.5C7.4 28.4 7.4 26.6 8.5 25.5C9.6 24.4 11.4 24.4 12.5 25.5L10.5 27.5Z"
              fill="url(#lCheck)"
            />
            <rect x="26" y="16.5" width="4.5" height="14" rx="2.25" fill="url(#lNeon)" />
            <circle cx="31.5" cy="11.5" r="2.2" fill="#38bdf8" />
          </svg>
        </div>
        <div class="brand-title">N-Check</div>
        <div class="brand-desc">名企面经智能收纳站</div>
      </div>

      <!-- 2. Auth Mode Switch Tabs -->
      <div class="auth-tabs-wrap">
        <div
          class="auth-tab-item"
          :class="{ 'tab-item-active': mode === 'login' }"
          @click="mode = 'login'"
        >
          账号登录
        </div>
        <div
          class="auth-tab-item"
          :class="{ 'tab-item-active': mode === 'register' }"
          @click="mode = 'register'"
        >
          注册新账号
        </div>
      </div>

      <!-- 3. Form Body -->
      <!-- A. Login Form -->
      <div v-if="mode === 'login'" class="auth-form-content">
        <div class="form-inputs-group">
          <div class="form-field">
            <label class="field-label">账号 / 用户名</label>
            <a-input
              v-model:value="loginForm.username"
              size="large"
              placeholder="请输入用户名"
              @keyup.enter="handleLogin"
            >
              <template #prefix><UserOutlined class="field-icon" /></template>
            </a-input>
          </div>

          <div class="form-field">
            <label class="field-label">登录密码</label>
            <a-input-password
              v-model:value="loginForm.password"
              size="large"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            >
              <template #prefix><LockOutlined class="field-icon" /></template>
            </a-input-password>
          </div>
        </div>

        <button class="auth-submit-btn" :disabled="loading" @click="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>正在登录...</span>
        </button>
      </div>

      <!-- B. Register Form -->
      <div v-else class="auth-form-content">
        <div class="form-inputs-group">
          <div class="form-field">
            <label class="field-label">新用户名</label>
            <a-input
              v-model:value="registerForm.username"
              size="large"
              placeholder="请输入英文字母或数字账号"
            >
              <template #prefix><UserOutlined class="field-icon" /></template>
            </a-input>
          </div>

          <div class="form-field">
            <label class="field-label">姓名 / 显示昵称</label>
            <a-input
              v-model:value="registerForm.name"
              size="large"
              placeholder="例如: 张三"
            >
              <template #prefix><IdcardOutlined class="field-icon" /></template>
            </a-input>
          </div>

          <div class="form-field">
            <label class="field-label">设置密码</label>
            <a-input-password
              v-model:value="registerForm.password"
              size="large"
              placeholder="请输入密码 (至少 4 位)"
              @keyup.enter="handleRegister"
            >
              <template #prefix><LockOutlined class="field-icon" /></template>
            </a-input-password>
          </div>
        </div>

        <button class="auth-submit-btn register-color-btn" :disabled="loading" @click="handleRegister">
          <span v-if="!loading">注 册 并 进 入</span>
          <span v-else>正在注册...</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  IdcardOutlined,
} from '@ant-design/icons-vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const router = useRouter()
const store = useInterviewStore()

const mode = ref('login') // 'login' | 'register'
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
})

const registerForm = ref({
  username: '',
  name: '',
  password: '',
})

async function handleLogin() {
  if (!loginForm.value.username.trim() || !loginForm.value.password) {
    message.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await store.login(loginForm.value.username.trim(), loginForm.value.password)
    if (res.success) {
      message.success(`欢迎回来，${res.user.name}！🎉`)
      router.push('/')
    } else {
      message.error(res.message || '登录失败，请检查账号密码')
    }
  } catch (e) {
    // 错误已在 request 拦截器中提示
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.value.username.trim()) {
    message.warning('请输入用户名')
    return
  }
  if (!registerForm.value.password || registerForm.value.password.length < 4) {
    message.warning('密码长度至少为 4 位')
    return
  }

  loading.value = true
  try {
    const res = await store.register({
      username: registerForm.value.username.trim(),
      password: registerForm.value.password,
      name: registerForm.value.name.trim() || registerForm.value.username.trim(),
    })
    if (res.success) {
      message.success(`注册成功！欢迎加入，${res.user.name}！🎉`)
      router.push('/')
    } else {
      message.error(res.message)
    }
  } catch (e) {
    // 错误已在 request 拦截器中提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page-viewport {
  min-height: 100vh;
  background: #090d16;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  overflow: hidden;
  box-sizing: border-box;
}

/* Ambient Glows */
.ambient-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(140px);
  pointer-events: none;
  opacity: 0.35;
}
.glow-1 {
  width: 460px;
  height: 460px;
  background: #4f46e5;
  top: -80px;
  left: -80px;
}
.glow-2 {
  width: 420px;
  height: 420px;
  background: #06b6d4;
  bottom: -80px;
  right: -80px;
}

/* Centered Auth Card */
.login-auth-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 20px;
  padding: 38px 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.45);
  z-index: 10;
  box-sizing: border-box;
}

/* Brand Header */
.auth-brand-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.brand-logo-badge {
  width: 48px;
  height: 48px;
  margin-bottom: 10px;
}
.cyber-svg-logo {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 22px;
  font-weight: 900;
  color: var(--text-1, #0f172a);
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.brand-desc {
  font-size: 12px;
  color: var(--text-3, #94a3b8);
  font-weight: 500;
  margin-top: 2px;
}

/* Mode Tabs */
.auth-tabs-wrap {
  display: flex;
  background: var(--bg-subtle, #f1f5f9);
  padding: 4px;
  border-radius: 10px;
  margin-bottom: 22px;
}

.auth-tab-item {
  flex: 1;
  text-align: center;
  padding: 7px 0;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-2, #64748b);
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.tab-item-active {
  background: #ffffff;
  color: var(--accent, #4f46e5) !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

/* Form */
.auth-form-content {
  display: flex;
  flex-direction: column;
}

.form-inputs-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 24px;
}

.form-field {}

.field-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2, #334155);
  margin-bottom: 5px;
}

.field-icon {
  color: var(--text-3, #94a3b8);
}

.auth-submit-btn {
  width: 100%;
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: #ffffff;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}
.auth-submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4338ca 0%, #4f46e5 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}
.auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-color-btn {
  background: linear-gradient(135deg, #059669 0%, #10b981 100%) !important;
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.25) !important;
}
.register-color-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #047857 0%, #059669 100%) !important;
}
</style>

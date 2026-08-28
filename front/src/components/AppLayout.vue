<template>
  <div class="app-shell-layout">
    <!-- Left Fixed Navigation Sidebar -->
    <aside class="app-left-sidebar" :class="{ 'sidebar-collapsed': collapsed }">
      <!-- 1. High-Tech Cyber Brand Header -->
      <div class="sidebar-brand-header">
        <div class="cyber-logo-badge" title="N-Check 面经收纳平台">
          <svg viewBox="0 0 40 40" fill="none" class="cyber-svg-logo" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="neonGradient" x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#6366f1" />
                <stop offset="50%" stop-color="#8b5cf6" />
                <stop offset="100%" stop-color="#06b6d4" />
              </linearGradient>
              <linearGradient id="checkCyber" x1="10" y1="30" x2="32" y2="8" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#10b981" />
                <stop offset="45%" stop-color="#06b6d4" />
                <stop offset="100%" stop-color="#38bdf8" />
              </linearGradient>
              <filter id="logoGlow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur stdDeviation="1.2" result="glow" />
                <feComposite in="SourceGraphic" in2="glow" operator="over" />
              </filter>
            </defs>

            <!-- 1. Dark Crystal Base Shield -->
            <rect width="40" height="40" rx="11" fill="#090d16" />
            <rect x="0.75" y="0.75" width="38.5" height="38.5" rx="10.25" stroke="url(#neonGradient)" stroke-opacity="0.35" stroke-width="1.5" />

            <!-- 2. Integrated 'N' Geometry + 'Check' Energy Blade -->
            <rect x="9.5" y="9.5" width="4.5" height="21" rx="2.25" fill="url(#neonGradient)" />
            <path
              d="M10.5 27.5L29.5 9.5C30.6 8.4 32.4 8.4 33.5 9.5C34.6 10.6 34.6 12.4 33.5 13.5L14.5 31.5C13.4 32.6 11.6 32.6 10.5 31.5L8.5 29.5C7.4 28.4 7.4 26.6 8.5 25.5C9.6 24.4 11.4 24.4 12.5 25.5L10.5 27.5Z"
              fill="url(#checkCyber)"
              filter="url(#logoGlow)"
            />
            <rect x="26" y="16.5" width="4.5" height="14" rx="2.25" fill="url(#neonGradient)" />
            <circle cx="31.5" cy="11.5" r="2.2" fill="#38bdf8" />
          </svg>
        </div>

        <div class="brand-text-wrap" v-show="!collapsed">
          <div class="brand-title-row">
            <span class="brand-main-title">N-Check</span>
            <span class="brand-pro-tag">PRO</span>
          </div>
          <div class="brand-sub-title">名企面经智能收纳站</div>
        </div>
      </div>

      <!-- 2. Main Navigation Menu (Clean & Pure 4 Menus) -->
      <nav class="sidebar-menu-nav">
        <!-- 1. 总览工作台 -->
        <router-link
          to="/"
          class="nav-menu-item"
          :class="{ 'item-active': currentPath === '/' }"
        >
          <div class="nmi-icon-box box-indigo">
            <AppstoreOutlined />
          </div>
          <span class="nmi-label" v-show="!collapsed">总览工作台</span>
        </router-link>

        <!-- 2. 公司与题库 -->
        <router-link
          to="/companies"
          class="nav-menu-item"
          :class="{ 'item-active': currentPath.startsWith('/companies') }"
        >
          <div class="nmi-icon-box box-blue">
            <BankOutlined />
          </div>
          <span class="nmi-label" v-show="!collapsed">公司与题库</span>
        </router-link>

        <!-- 3. 高频必背 -->
        <router-link
          to="/favorites"
          class="nav-menu-item"
          :class="{ 'item-active': currentPath === '/favorites' }"
        >
          <div class="nmi-icon-box box-amber">
            <StarFilled />
          </div>
          <span class="nmi-label" v-show="!collapsed">高频必背</span>
        </router-link>

        <!-- 4. 每日复盘 -->
        <router-link
          to="/daily-review"
          class="nav-menu-item"
          :class="{ 'item-active': currentPath === '/daily-review' }"
        >
          <div class="nmi-icon-box box-rose">
            <CalendarOutlined />
          </div>
          <span class="nmi-label" v-show="!collapsed">每日复盘</span>
        </router-link>
      </nav>

      <!-- 3. Current Authenticated User Card (With Logout Option) -->
      <div class="sidebar-user-card-wrap" v-show="!collapsed && store.currentUser">
        <div class="sidebar-user-card">
          <div class="suc-avatar" :style="{ background: store.currentUser.color || '#4f46e5' }">
            {{ store.currentUser.name?.slice(0, 1) || 'U' }}
          </div>
          <div class="suc-info">
            <div class="suc-name">{{ store.currentUser.name }}</div>
            <div class="suc-role">@{{ store.currentUser.username || 'user' }} · 独立题库</div>
          </div>

          <!-- Logout Action -->
          <a-popconfirm
            title="确定要退出登录并返回登录页吗？"
            ok-text="退出登录"
            cancel-text="取消"
            @confirm="handleLogout"
          >
            <button class="suc-logout-btn" title="退出登录">
              <LogoutOutlined />
            </button>
          </a-popconfirm>
        </div>
      </div>

      <!-- 4. Bottom Knowledge Base Metric Widget -->
      <div class="sidebar-bottom-widget" v-show="!collapsed">
        <div class="sbw-card">
          <div class="sbw-title-row">
            <span class="sbw-dot"></span>
            <span class="sbw-title">当前账号题库进度</span>
          </div>

          <div class="sbw-stats-line">
            <span class="sbw-count"><strong>{{ store.entries.length }}</strong> 题在库</span>
            <span class="sbw-mastered">已掌握 {{ masteredCount }} 题</span>
          </div>

          <div class="sbw-progress-track">
            <div class="sbw-progress-bar" :style="{ width: `${masteryRate}%` }"></div>
          </div>
          <div class="sbw-pct-text">掌握率 {{ masteryRate }}%</div>
        </div>
      </div>

      <!-- 5. Collapse Trigger Button -->
      <div class="sidebar-collapse-bar" @click="collapsed = !collapsed">
        <MenuFoldOutlined v-if="!collapsed" />
        <MenuUnfoldOutlined v-else />
      </div>
    </aside>

    <!-- Main Workspace Content -->
    <main class="app-main-workspace" :class="{ 'workspace-collapsed-margin': collapsed }">
      <div class="workspace-scroll-container">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined,
  BankOutlined,
  StarFilled,
  CalendarOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons-vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const route = useRoute()
const router = useRouter()
const store = useInterviewStore()
const collapsed = ref(false)

const currentPath = computed(() => route.path)

const masteredCount = computed(() => {
  return store.entries.filter(e => e.status === '已掌握').length
})

const masteryRate = computed(() => {
  if (!store.entries.length) return 0
  return Math.round((masteredCount.value / store.entries.length) * 100)
})

function handleLogout() {
  store.logout()
  message.success('已安全退出登录')
  router.push('/login')
}
</script>

<style scoped>
.app-shell-layout {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-page);
}

/* Left Sidebar */
.app-left-sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  transition: width 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 1px 0 6px rgba(0, 0, 0, 0.02);
}

.sidebar-collapsed {
  width: 72px;
}

/* Brand Header */
.sidebar-brand-header {
  height: 72px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--border-light);
}

.cyber-logo-badge {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.25s ease, filter 0.25s ease;
  cursor: pointer;
}

.cyber-logo-badge:hover {
  transform: scale(1.06) rotate(2deg);
  filter: drop-shadow(0 4px 14px rgba(99, 102, 241, 0.35));
}

.cyber-svg-logo {
  width: 100%;
  height: 100%;
  display: block;
}

.brand-text-wrap {
  overflow: hidden;
  white-space: nowrap;
}

.brand-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.brand-main-title {
  font-size: 16.5px;
  font-weight: 900;
  color: var(--text-1);
  letter-spacing: -0.5px;
  line-height: 1.2;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-pro-tag {
  font-size: 9.5px;
  font-weight: 800;
  padding: 1px 5px;
  border-radius: 4px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: #ffffff;
  letter-spacing: 0.5px;
}

.brand-sub-title {
  font-size: 11px;
  color: var(--text-3);
  font-weight: 500;
  letter-spacing: 0.2px;
}

/* Menu Nav */
.sidebar-menu-nav {
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.nav-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 12px;
  border-radius: 12px;
  color: var(--text-2);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.18s ease;
  position: relative;
}

.nav-menu-item:hover {
  background: var(--bg-subtle);
  color: var(--text-1);
}

.item-active {
  background: var(--accent-subtle) !important;
  color: var(--accent) !important;
  font-weight: 600;
}

/* Colored Icon Boxes */
.nmi-icon-box {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.nav-menu-item:hover .nmi-icon-box {
  transform: scale(1.08);
}

.box-indigo {
  background: #eef2ff;
  color: #4f46e5;
  border: 1px solid #e0e7ff;
}

.box-blue {
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #dbeafe;
}

.box-amber {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fef3c7;
}

.box-rose {
  background: #fff1f2;
  color: #e11d48;
  border: 1px solid #ffe4e6;
}

.item-active .nmi-icon-box {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.nmi-label {
  white-space: nowrap;
  overflow: hidden;
  flex: 1;
}

/* User Card Widget */
.sidebar-user-card-wrap {
  padding: 0 12px;
  margin-bottom: 8px;
}

.sidebar-user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  transition: all 0.18s ease;
}

.sidebar-user-card:hover {
  background: #ffffff;
  border-color: var(--accent-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.suc-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.suc-info {
  flex: 1;
  overflow: hidden;
}

.suc-name {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suc-role {
  font-size: 10.5px;
  color: var(--text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suc-logout-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 14px;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.18s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.suc-logout-btn:hover {
  color: #f43f5e;
  background: #fff1f2;
}

/* Bottom Metric Widget */
.sidebar-bottom-widget {
  padding: 0 12px 12px 12px;
  margin-top: auto;
}

.sbw-card {
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
}

.sbw-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 8px;
}

.sbw-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.sbw-stats-line {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-2);
  margin-bottom: 6px;
}
.sbw-count strong {
  color: var(--accent);
}

.sbw-progress-track {
  height: 6px;
  background: #e2e8f0;
  border-radius: 100px;
  overflow: hidden;
  margin-bottom: 4px;
}

.sbw-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5 0%, #06b6d4 100%);
  border-radius: 100px;
  transition: width 0.3s ease;
}

.sbw-pct-text {
  font-size: 10.5px;
  color: var(--text-3);
  text-align: right;
  font-weight: 500;
}

/* Collapse Trigger */
.sidebar-collapse-bar {
  height: 42px;
  border-top: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.18s;
}

.sidebar-collapse-bar:hover {
  background: var(--bg-subtle);
  color: var(--text-1);
}

/* Main Workspace */
.app-main-workspace {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  transition: margin-left 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

.workspace-collapsed-margin {
  margin-left: 72px;
}

.workspace-scroll-container {
  flex: 1;
  padding: 24px 32px;
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}
</style>

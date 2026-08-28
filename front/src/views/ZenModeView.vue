<template>
  <div class="zen-mode-viewport">
    <!-- 1. Top Zen Control Bar -->
    <header class="zen-top-bar">
      <div class="ztb-left">
        <button class="zen-back-btn" @click="$router.back()" title="退出禅定背题 (Esc)">
          <ArrowLeftOutlined /> 退出背题
        </button>

        <!-- Company / Date Filter Selector -->
        <div class="zen-company-filter">
          <span class="zcf-label">🎯 背题范围：</span>
          <a-select
            v-model:value="selectedCompanyId"
            style="width: 190px"
            size="small"
            class="zen-select"
            @change="handleFilterChange"
          >
            <a-select-option :value="null">🌐 全部企业题库</a-select-option>
            <a-select-option value="starred">⭐ 仅高频必背题</a-select-option>
            <a-select-option v-if="dateFilter" :value="`date_${dateFilter}`">
              📅 {{ dateFilter }} 当日题
            </a-select-option>
            <a-select-option v-for="c in store.companies" :key="c.id" :value="c.id">
              {{ c.name }}
            </a-select-option>
          </a-select>
        </div>
      </div>

      <!-- Center Progress -->
      <div class="ztb-center">
        <span class="zen-progress-pill">
          进度：<strong>{{ currentList.length ? currentIndex + 1 : 0 }}</strong> / {{ currentList.length }}
        </span>
      </div>

      <!-- Right Shortcuts Help Tip -->
      <div class="ztb-right">
        <div class="zen-shortcuts-badge">
          <span><code>Space</code> 翻转</span>
          <span><code>←</code> <code>→</code> 翻页</span>
          <span><code>1</code><code>2</code><code>3</code> 快速打分</span>
        </div>
      </div>
    </header>

    <!-- 2. Main 3D Flashcard Stage Area -->
    <main class="zen-card-stage">
      <div v-if="!currentList.length" class="zen-empty-state">
        <div class="zes-icon">🧘</div>
        <h2 class="zes-title">当前筛选范围暂无面试题</h2>
        <p class="zes-desc">请尝试切换上方的企业筛选，或返回题库录入新题目</p>
        <a-button type="primary" @click="selectedCompanyId = null">查看全部题目</a-button>
      </div>

      <div v-else class="flashcard-3d-scene" @click="flipCard">
        <div class="flashcard-3d-flipper" :class="{ 'is-flipped': isFlipped }">
          <!-- ===== CARD FRONT: Question Side ===== -->
          <div class="card-face card-front">
            <div class="cf-header">
              <div class="cf-comp-brand">
                <CompanyLogo :logo="currentCompany?.logo" :name="currentCompany?.name" :size="36" :radius="8" />
                <span class="cf-comp-name">{{ currentCompany?.name || '综合题库' }}</span>
              </div>
              <div class="cf-badges">
                <span class="cf-type" :class="currentCard.type === '八股文' ? 'cft-blue' : 'cft-green'">
                  {{ currentCard.type }}
                </span>
                <span class="diff-pill" :class="`diff-${currentCard.difficulty}`">{{ currentCard.difficulty }}</span>
              </div>
            </div>

            <!-- Question Center Body -->
            <div class="cf-body-center">
              <div class="cf-card-label">QUESTION #{{ currentIndex + 1 }}</div>
              <h1 class="cf-question-title">{{ currentCard.title }}</h1>
              
              <div class="cf-tags-row" v-if="currentCard.tags?.length">
                <span v-for="t in currentCard.tags" :key="t" class="cf-tag-pill">{{ t }}</span>
              </div>
            </div>

            <!-- Card Bottom Hint -->
            <div class="cf-footer-hint">
              <span class="cf-space-key">SPACE</span>
              <span class="cf-hint-text">自我思考后，按空格键或点击卡片翻转揭晓解析</span>
            </div>
          </div>

          <!-- ===== CARD BACK: Answer & Code Side ===== -->
          <div class="card-face card-back" @click.stop>
            <div class="cb-header">
              <div class="cb-title-bar">
                <span class="cb-ans-badge">📖 官方核心解析与代码</span>
                <span class="cb-q-title">{{ currentCard.title }}</span>
              </div>
              <button class="cb-flip-back-btn" @click="flipCard" title="翻回正面 (Space)">
                <RedoOutlined /> 翻回题目
              </button>
            </div>

            <!-- Scrollable Markdown & Code Content -->
            <div class="cb-content-scroll">
              <div class="drawer-markdown-render" v-html="renderMd(currentCard.content)"></div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 3. Bottom Mastery Rating & Navigation Bar -->
    <footer class="zen-bottom-bar" v-if="currentList.length">
      <!-- Left Prev Button -->
      <button class="zen-nav-arrow-btn" :disabled="currentIndex === 0" @click="prevCard" title="上一题 (←)">
        <LeftOutlined /> 上一题
      </button>

      <!-- Center 1/2/3 Rating System -->
      <div class="zen-mastery-group">
        <button
          class="mastery-rate-btn rate-forgot"
          @click="rateAndNext('未掌握')"
          title="快捷键: 1"
        >
          <span class="rate-key-badge">1</span>
          <span>遗忘 / 需重背</span>
        </button>

        <button
          class="mastery-rate-btn rate-fuzzy"
          @click="rateAndNext('学习中')"
          title="快捷键: 2"
        >
          <span class="rate-key-badge">2</span>
          <span>模糊 / 基本掌握</span>
        </button>

        <button
          class="mastery-rate-btn rate-mastered"
          @click="rateAndNext('已掌握')"
          title="快捷键: 3"
        >
          <span class="rate-key-badge">3</span>
          <span>熟练掌握 / 秒杀</span>
        </button>
      </div>

      <!-- Right Next Button -->
      <button
        class="zen-nav-arrow-btn"
        :disabled="currentIndex === currentList.length - 1"
        @click="nextCard"
        title="下一题 (→)"
      >
        下一题 <RightOutlined />
      </button>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  LeftOutlined,
  RightOutlined,
  RedoOutlined,
} from '@ant-design/icons-vue'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import cpp from 'highlight.js/lib/languages/cpp'
import python from 'highlight.js/lib/languages/python'
import 'highlight.js/styles/atom-one-dark.css'
import CompanyLogo from '../components/CompanyLogo.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('python', python)

const route = useRoute()
const router = useRouter()
const store = useInterviewStore()

const selectedCompanyId = ref(null)
const dateFilter = ref(null)
const currentIndex = ref(0)
const isFlipped = ref(false)

// Init query params
onMounted(() => {
  if (route.query.date) {
    dateFilter.value = route.query.date
    selectedCompanyId.value = `date_${route.query.date}`
  } else if (route.query.starred === 'true') {
    selectedCompanyId.value = 'starred'
  } else if (route.query.companyId) {
    selectedCompanyId.value = route.query.companyId
  }
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

const currentList = computed(() => {
  let list = store.entries
  if (selectedCompanyId.value?.startsWith('date_')) {
    const dStr = selectedCompanyId.value.replace('date_', '')
    list = store.getEntriesByDate(dStr)
  } else if (selectedCompanyId.value === 'starred') {
    list = store.starredEntries
  } else if (selectedCompanyId.value) {
    list = list.filter(e => e.companyId === selectedCompanyId.value)
  }
  return list
})

const currentCard = computed(() => currentList.value[currentIndex.value] || null)

const currentCompany = computed(() => {
  if (!currentCard.value) return null
  return store.companies.find(c => c.id === currentCard.value.companyId)
})

function handleFilterChange() {
  currentIndex.value = 0
  isFlipped.value = false
}

function flipCard() {
  if (currentList.value.length) {
    isFlipped.value = !isFlipped.value
  }
}

function nextCard() {
  if (currentIndex.value < currentList.value.length - 1) {
    isFlipped.value = false
    currentIndex.value++
  } else {
    message.info('已经是本组最后一题了 🎉')
  }
}

function prevCard() {
  if (currentIndex.value > 0) {
    isFlipped.value = false
    currentIndex.value--
  }
}

function rateAndNext(status) {
  if (!currentCard.value) return
  store.updateMasteryStatus(currentCard.value.id, status)
  message.success(`已标记为「${status}」`)
  nextCard()
}

// Global Keyboard Shortcuts
function handleKeydown(e) {
  if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return

  if (e.code === 'Space') {
    e.preventDefault()
    flipCard()
  } else if (e.code === 'ArrowRight') {
    e.preventDefault()
    nextCard()
  } else if (e.code === 'ArrowLeft') {
    e.preventDefault()
    prevCard()
  } else if (e.key === '1') {
    e.preventDefault()
    rateAndNext('未掌握')
  } else if (e.key === '2') {
    e.preventDefault()
    rateAndNext('学习中')
  } else if (e.key === '3') {
    e.preventDefault()
    rateAndNext('已掌握')
  } else if (e.code === 'Escape') {
    router.back()
  }
}

function renderMd(content) {
  if (!content) return '<p style="color:var(--text-3); padding: 40px 0; text-align: center;">暂无解析内容</p>'
  return content
    .replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
      let hl = ''
      try {
        hl = hljs.highlight(code, { language: lang || 'java' }).value
      } catch {
        hl = hljs.highlightAuto(code).value
      }
      return `
        <div class="render-code-wrapper">
          <div class="render-code-header">
            <span class="rc-lang-tag">${(lang || 'CODE').toUpperCase()}</span>
          </div>
          <pre class="render-code-pre"><code class="hljs">${hl}</code></pre>
        </div>
      `
    })
    .replace(/^### (.+)$/gm, '<h3 class="render-h3">$1</h3>')
    .replace(/^## (.+)$/gm, '<h2 class="render-h2">$1</h2>')
    .replace(/^# (.+)$/gm, '<h1 class="render-h1">$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong class="render-strong">$1</strong>')
    .replace(/`(.+?)`/g, '<code class="render-inline-code">$1</code>')
    .replace(/^- (.+)$/gm, '<div class="render-li">• $1</div>')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.zen-mode-viewport {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #090d16;
  color: #f8fafc;
  display: flex;
  flex-direction: column;
  z-index: 9999;
  user-select: none;
}

/* 1. Top Bar */
.zen-top-bar {
  height: 60px;
  padding: 0 28px;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ztb-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.zen-back-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.18s;
}
.zen-back-btn:hover {
  background: rgba(255, 255, 255, 0.16);
  border-color: rgba(255, 255, 255, 0.24);
}

.zen-company-filter {
  display: flex;
  align-items: center;
  gap: 6px;
}
.zcf-label {
  font-size: 12px;
  color: #94a3b8;
}

:deep(.zen-select .ant-select-selector) {
  background: rgba(255, 255, 255, 0.08) !important;
  border-color: rgba(255, 255, 255, 0.15) !important;
  color: #f8fafc !important;
  border-radius: 6px !important;
}

.ztb-center {}
.zen-progress-pill {
  font-size: 13px;
  color: #94a3b8;
  background: rgba(255, 255, 255, 0.05);
  padding: 4px 14px;
  border-radius: 100px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.zen-progress-pill strong {
  color: #38bdf8;
  font-size: 15px;
}

.ztb-right {}
.zen-shortcuts-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 11.5px;
  color: #64748b;
}
.zen-shortcuts-badge code {
  background: rgba(255, 255, 255, 0.1);
  color: #cbd5e1;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  margin-right: 4px;
}

/* 2. Main Stage */
.zen-card-stage {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  perspective: 1200px;
}

.zen-empty-state {
  text-align: center;
  color: #94a3b8;
}
.zes-icon { font-size: 48px; margin-bottom: 12px; }
.zes-title { font-size: 20px; font-weight: 700; color: #f8fafc; margin-bottom: 6px; }
.zes-desc { font-size: 13px; margin-bottom: 20px; }

/* 3D Flip Card Container */
.flashcard-3d-scene {
  width: 100%;
  max-width: 860px;
  height: 480px;
  cursor: pointer;
}

.flashcard-3d-flipper {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.is-flipped {
  transform: rotateY(180deg);
}

.card-face {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 24px;
  padding: 32px 40px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* Front Face */
.card-front {
  background: linear-gradient(145deg, #1e293b 0%, #0f172a 100%);
  border: 1.5px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5), 0 0 40px rgba(79, 70, 229, 0.15);
  justify-content: space-between;
}

.cf-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cf-comp-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cf-comp-name {
  font-size: 15px;
  font-weight: 700;
  color: #f8fafc;
}

.cf-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cf-type {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 6px;
}
.cft-blue { background: rgba(37, 99, 235, 0.2); color: #60a5fa; border: 1px solid rgba(96, 165, 250, 0.3); }
.cft-green { background: rgba(5, 150, 105, 0.2); color: #34d399; border: 1px solid rgba(52, 211, 153, 0.3); }

.cf-body-center {
  text-align: center;
  padding: 20px 0;
}
.cf-card-label {
  font-size: 12px;
  font-weight: 800;
  color: #6366f1;
  letter-spacing: 2px;
  margin-bottom: 12px;
}
.cf-question-title {
  font-size: 26px;
  font-weight: 800;
  color: #ffffff;
  line-height: 1.4;
  margin-bottom: 18px;
  letter-spacing: -0.4px;
}

.cf-tags-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.cf-tag-pill {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.08);
  color: #94a3b8;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.cf-footer-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}
.cf-space-key {
  background: rgba(255, 255, 255, 0.15);
  color: #f8fafc;
  padding: 3px 10px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 11px;
  letter-spacing: 1px;
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.3);
}

/* Back Face */
.card-back {
  background: #1e222d;
  border: 1.5px solid rgba(99, 102, 241, 0.3);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6);
  transform: rotateY(180deg);
  cursor: default;
}

.cb-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 14px;
  margin-bottom: 14px;
}

.cb-title-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
}
.cb-ans-badge {
  font-size: 12px;
  font-weight: 700;
  color: #818cf8;
  flex-shrink: 0;
}
.cb-q-title {
  font-size: 14px;
  font-weight: 600;
  color: #cbd5e1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cb-flip-back-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #94a3b8;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.cb-flip-back-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

.cb-content-scroll {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  color: #e2e8f0;
  line-height: 1.8;
  font-size: 14.5px;
}

/* 3. Bottom Bar */
.zen-bottom-bar {
  height: 76px;
  padding: 0 40px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.zen-nav-arrow-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.18s;
}
.zen-nav-arrow-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.28);
}
.zen-nav-arrow-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.zen-mastery-group {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mastery-rate-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.18s ease;
  border: 1px solid transparent;
}
.mastery-rate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.4);
}

.rate-key-badge {
  background: rgba(0, 0, 0, 0.25);
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
}

.rate-forgot {
  background: #f43f5e;
  color: #ffffff;
}
.rate-forgot:hover { background: #e11d48; }

.rate-fuzzy {
  background: #f59e0b;
  color: #ffffff;
}
.rate-fuzzy:hover { background: #d97706; }

.rate-mastered {
  background: #10b981;
  color: #ffffff;
}
.rate-mastered:hover { background: #059669; }
</style>

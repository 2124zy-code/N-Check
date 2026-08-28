<template>
  <AppLayout>
    <div class="favorites-page-container">
      <!-- 1. Header Banner -->
      <div class="fav-header-card">
        <div class="fhc-left">
          <div class="fhc-icon-box">⭐</div>
          <div class="fhc-info">
            <h1 class="fhc-title">高频必背 · 考前压轴题库</h1>
            <p class="fhc-desc">集中收纳标记为星标的核心必考点与手撕真题，助你考前高效冲刺</p>
          </div>
        </div>

        <div class="fhc-right-actions">
          <a-button class="zen-action-btn" @click="$router.push('/zen-mode?starred=true')">
            <CompassOutlined />
            以必背题开启禅定背题
          </a-button>
        </div>
      </div>

      <!-- 2. Filter & Toolbar Bar -->
      <div class="fav-toolbar-bar">
        <div class="ftb-left">
          <!-- Company Filter -->
          <a-select
            v-model:value="filterCompany"
            placeholder="按目标企业筛选"
            style="width: 170px"
            allow-clear
          >
            <a-select-option v-for="c in store.companies" :key="c.id" :value="c.id">
              {{ c.name }}
            </a-select-option>
          </a-select>

          <!-- Type Filter -->
          <a-select
            v-model:value="filterType"
            placeholder="题目类型"
            style="width: 120px"
            allow-clear
          >
            <a-select-option value="八股文">八股理论</a-select-option>
            <a-select-option value="算法题">手撕算法</a-select-option>
          </a-select>

          <!-- Difficulty -->
          <a-select
            v-model:value="filterDiff"
            placeholder="难度"
            style="width: 100px"
            allow-clear
          >
            <a-select-option value="简单">简单</a-select-option>
            <a-select-option value="中等">中等</a-select-option>
            <a-select-option value="困难">困难</a-select-option>
          </a-select>

          <!-- Keyword -->
          <a-input
            v-model:value="keyword"
            placeholder="搜索必背题目标题/内容..."
            style="width: 220px"
            allow-clear
          >
            <template #prefix>
              <SearchOutlined style="color: var(--text-3);" />
            </template>
          </a-input>
        </div>

        <div class="ftb-right">
          <span class="count-badge">已收纳 <strong>{{ filteredList.length }}</strong> 道高频必背题</span>
        </div>
      </div>

      <!-- 3. Entries Stack -->
      <div v-if="!filteredList.length" class="empty-fav-box">
        <a-empty
          :description="store.starredEntries.length ? '未找到符合筛选条件的高频题目' : '当前暂无星标必背题目，在任意题目卡片上点击 ⭐ 即可收藏至此'"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
        >
          <a-button v-if="!store.starredEntries.length" type="primary" @click="$router.push('/companies')">
            去企业题库挑选压轴题
          </a-button>
        </a-empty>
      </div>

      <div v-else class="entries-cards-stack">
        <div
          v-for="e in filteredList"
          :key="e.id"
          class="entry-detail-card"
          @click="openDetail(e)"
        >
          <!-- Star Toggle -->
          <button
            class="star-btn active"
            @click.stop="toggleStar(e.id)"
            title="取消星标收藏"
          >
            <StarFilled />
          </button>

          <!-- Type Box -->
          <div class="edc-type-box" :class="e.type === '八股文' ? 'edc-type-blue' : 'edc-type-green'">
            <ReadOutlined v-if="e.type === '八股文'" />
            <CodeOutlined v-else />
          </div>

          <!-- Content Info -->
          <div class="edc-content-area">
            <div class="edc-title-text">{{ e.title }}</div>
            <div class="edc-tags-line">
              <span class="company-badge-item" @click.stop="$router.push(`/companies/${e.companyId}`)">
                {{ getCompanyName(e.companyId) }}
              </span>
              <span v-for="t in (e.tags || []).slice(0, 4)" :key="t" class="edc-tag-item">{{ t }}</span>
            </div>
          </div>

          <!-- Right Meta -->
          <div class="edc-right-meta">
            <span class="diff-pill" :class="`diff-${e.difficulty}`">{{ e.difficulty }}</span>
            <span class="status-pill" :class="`status-${e.status}`">{{ e.status }}</span>
            <span class="edc-time">{{ e.createdAt?.slice(0, 10) }}</span>
            <RightOutlined class="edc-arrow" />
          </div>
        </div>
      </div>

      <!-- Detail Drawer -->
      <a-drawer
        v-model:open="drawerOpen"
        :title="detailEntry?.title"
        width="760"
        placement="right"
      >
        <div v-if="detailEntry" class="drawer-detail-body">
          <div class="drawer-meta-header">
            <span class="drawer-company-badge">{{ getCompanyName(detailEntry.companyId) }}</span>
            <span class="drawer-type-badge" :class="detailEntry.type === '八股文' ? 'dtb-blue' : 'dtb-green'">
              <ReadOutlined v-if="detailEntry.type === '八股文'" />
              <CodeOutlined v-else />
              {{ detailEntry.type }}
            </span>
            <span class="diff-pill" :class="`diff-${detailEntry.difficulty}`">{{ detailEntry.difficulty }}</span>
            <span class="status-pill" :class="`status-${detailEntry.status}`">{{ detailEntry.status }}</span>
          </div>

          <div class="drawer-divider"></div>
          <div class="drawer-markdown-render" v-html="renderMd(detailEntry.content)"></div>
        </div>
      </a-drawer>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Empty, message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReadOutlined,
  CodeOutlined,
  StarFilled,
  RightOutlined,
  CompassOutlined,
} from '@ant-design/icons-vue'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import cpp from 'highlight.js/lib/languages/cpp'
import python from 'highlight.js/lib/languages/python'
import 'highlight.js/styles/atom-one-dark.css'
import AppLayout from '../components/AppLayout.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('python', python)

const store = useInterviewStore()

const filterCompany = ref(null)
const filterType = ref(null)
const filterDiff = ref(null)
const keyword = ref('')
const drawerOpen = ref(false)
const detailEntry = ref(null)

const filteredList = computed(() => {
  let list = store.starredEntries
  if (filterCompany.value) list = list.filter(e => e.companyId === filterCompany.value)
  if (filterType.value) list = list.filter(e => e.type === filterType.value)
  if (filterDiff.value) list = list.filter(e => e.difficulty === filterDiff.value)
  if (keyword.value) {
    const q = keyword.value.toLowerCase()
    list = list.filter(e => e.title?.toLowerCase().includes(q) || e.content?.toLowerCase().includes(q))
  }
  return list
})

function getCompanyName(companyId) {
  const c = store.companies.find(item => item.id === companyId)
  return c ? c.name : '未知企业'
}

function toggleStar(id) {
  store.toggleStar(id)
  message.info('已取消该题目的星标')
}

function openDetail(e) {
  detailEntry.value = e
  drawerOpen.value = true
}

function renderMd(content) {
  if (!content) return '<p style="color:var(--text-3); text-align:center; padding: 40px 0;">暂无解析内容</p>'
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
            <button class="copy-code-btn" onclick="navigator.clipboard.writeText(\`${code.replace(/`/g, '\\`').replace(/\\/g, '\\\\')}\`); alert('代码已复制到剪贴板！')">复制代码</button>
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
.favorites-page-container {
  max-width: 1280px;
  margin: 0 auto;
}

/* Header */
.fav-header-card {
  background: linear-gradient(135deg, #fffbeb 0%, #ffffff 100%);
  border: 1px solid #fef3c7;
  border-radius: 20px;
  padding: 24px 28px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(245, 158, 11, 0.05);
}

.fhc-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.fhc-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #fef3c7;
  border: 1px solid #fde68a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.fhc-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.4px;
  margin-bottom: 4px;
}
.fhc-desc {
  font-size: 13px;
  color: #92400e;
}

.zen-action-btn {
  height: 40px !important;
  background: #0f172a !important;
  color: #ffffff !important;
  border: none !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.18);
}

/* Toolbar */
.fav-toolbar-bar {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px 18px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.ftb-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.count-badge {
  font-size: 13px;
  color: var(--text-2);
}
.count-badge strong {
  color: #d97706;
}

/* Cards Stack */
.entries-cards-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.entry-detail-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.entry-detail-card:hover {
  border-color: #f59e0b;
  box-shadow: 0 3px 12px rgba(245, 158, 11, 0.08);
  transform: translateY(-2px);
}

.star-btn {
  background: transparent;
  border: none;
  font-size: 18px;
  color: #f59e0b;
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.18s;
}
.star-btn:hover {
  transform: scale(1.2);
}

.edc-type-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.edc-type-blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.edc-type-green { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }

.edc-content-area {
  flex: 1;
  overflow: hidden;
}
.edc-title-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.edc-tags-line {
  display: flex;
  align-items: center;
  gap: 6px;
}
.company-badge-item {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 4px;
  background: var(--accent-subtle);
  color: var(--accent);
  cursor: pointer;
}
.company-badge-item:hover {
  text-decoration: underline;
}

.edc-tag-item {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--bg-subtle);
  color: var(--text-3);
}

.edc-right-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.diff-pill {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 7px;
  border-radius: 4px;
}
.diff-简单 { background: #ecfdf5; color: #059669; }
.diff-中等 { background: #fffbeb; color: #d97706; }
.diff-困难 { background: #fff1f2; color: #e11d48; }

.status-pill {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 4px;
  background: var(--bg-subtle);
  color: var(--text-2);
}
.status-已掌握 { background: #ecfdf5; color: #059669; font-weight: 600; }

.edc-time {
  font-size: 12px;
  color: var(--text-3);
}
.edc-arrow {
  font-size: 12px;
  color: var(--text-3);
}

.empty-fav-box {
  padding: 60px 0;
  text-align: center;
}

/* Drawer */
.drawer-meta-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.drawer-company-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  background: var(--accent-subtle);
  color: var(--accent);
}
.drawer-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 6px;
}
.dtb-blue { background: #eff6ff; color: #2563eb; }
.dtb-green { background: #ecfdf5; color: #059669; }
.drawer-divider { height: 1px; background: var(--border-light); margin: 16px 0; }
.drawer-markdown-render { color: var(--text-1); line-height: 1.85; font-size: 14px; }
</style>

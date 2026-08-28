<template>
  <AppLayout>
    <!-- Not Found -->
    <div v-if="!company" class="company-not-found">
      <a-result status="404" title="未找到该目标企业" sub-title="该公司可能已被删除或访问链接不存在">
        <template #extra>
          <a-button type="primary" @click="$router.push('/companies')">返回企业列表</a-button>
        </template>
      </a-result>
    </div>

    <div v-else class="company-detail-page">
      <!-- ===== Company Profile Hero Banner ===== -->
      <div class="company-hero-card">
        <div class="hero-left-profile">
          <CompanyLogo :logo="company.logo" :name="company.name" :size="64" :radius="16" />
          
          <div class="hero-text-info">
            <div class="back-link-row">
              <span class="back-link" @click="$router.push('/companies')">
                <ArrowLeftOutlined /> 返回企业列表
              </span>
            </div>
            <h1 class="hero-company-name">{{ company.name }}</h1>
            <div class="hero-meta-badges">
              <span class="industry-badge">{{ company.industry || '互联网' }}</span>
              <span class="created-date">建档日期：{{ company.createdAt?.slice(0, 10) }}</span>
            </div>
          </div>
        </div>

        <div class="hero-right-actions-group">
          <!-- Stats Pills -->
          <div class="hero-stats-group">
            <div class="hero-stat-pill">
              <span class="hsp-num text-blue">{{ baguCount }}</span>
              <span class="hsp-label">八股考点</span>
            </div>
            <div class="hero-stat-pill">
              <span class="hsp-num text-green">{{ algoCount }}</span>
              <span class="hsp-label">算法手撕</span>
            </div>
            <div class="hero-stat-pill">
              <span class="hsp-num text-purple">{{ masteredCount }}</span>
              <span class="hsp-label">已熟练掌握</span>
            </div>
          </div>

          <!-- Quick Actions: Export Handbook & Zen Mode -->
          <div class="hero-btn-row">
            <a-dropdown :trigger="['click']">
              <a-button class="export-handbook-btn">
                <FileTextOutlined /> 导出八股备战手册 <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="exportMarkdownHandbook">
                    <DownloadOutlined /> 下载 Markdown 文档 (.md)
                  </a-menu-item>
                  <a-menu-item @click="printPdfHandbook">
                    <PrinterOutlined /> 打印 / 保存为 PDF 手册
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>

            <a-button class="hero-zen-btn" @click="$router.push(`/zen-mode?companyId=${company.id}`)">
              <CompassOutlined /> 专属禅定背题
            </a-button>
          </div>
        </div>
      </div>

      <!-- ===== Control & Filter Toolbar ===== -->
      <div class="detail-toolbar-card">
        <!-- Left: Category Pills -->
        <div class="tab-pills-row">
          <div
            v-for="t in tabs"
            :key="t.key"
            class="filter-pill-btn"
            :class="{ 'pill-active': activeTab === t.key }"
            @click="activeTab = t.key"
          >
            <component :is="t.icon" />
            <span>{{ t.label }}</span>
            <span class="pill-count-num">{{ t.count }}</span>
          </div>
        </div>

        <!-- Right: Search & Filters & Add Button -->
        <div class="toolbar-right-actions">
          <a-input
            v-model:value="keyword"
            placeholder="搜索题目标题/内容..."
            style="width: 200px"
            allow-clear
          >
            <template #prefix>
              <SearchOutlined style="color: var(--text-3);" />
            </template>
          </a-input>

          <a-select v-model:value="filterDiff" placeholder="难度" style="width: 96px" allow-clear>
            <a-select-option value="简单">简单</a-select-option>
            <a-select-option value="中等">中等</a-select-option>
            <a-select-option value="困难">困难</a-select-option>
          </a-select>

          <a-select v-model:value="filterStatus" placeholder="熟练度" style="width: 104px" allow-clear>
            <a-select-option value="未掌握">未掌握</a-select-option>
            <a-select-option value="学习中">学习中</a-select-option>
            <a-select-option value="已掌握">已掌握</a-select-option>
          </a-select>

          <a-button type="primary" class="add-entry-btn" @click="openAdd">
            <template #icon><PlusOutlined /></template>
            录入题目
          </a-button>
        </div>
      </div>

      <!-- ===== Entries List ===== -->
      <div v-if="!filtered.length" class="empty-entries-view">
        <a-empty
          :description="allEntries.length ? '未找到符合筛选条件的题目' : '当前企业尚未录入任何面试题目'"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
        >
          <a-button v-if="!allEntries.length" type="primary" @click="openAdd">立即录入第一题</a-button>
        </a-empty>
      </div>

      <div v-else class="entries-cards-stack">
        <div
          v-for="e in filtered"
          :key="e.id"
          class="entry-detail-card"
          @click="openDetail(e)"
        >
          <!-- Star Toggle Button -->
          <button
            class="entry-star-btn"
            :class="{ 'star-lit': e.isStarred }"
            @click.stop="toggleStar(e.id)"
            :title="e.isStarred ? '取消高频必背星标' : '标记为高频必背压轴题'"
          >
            <StarFilled v-if="e.isStarred" />
            <StarOutlined v-else />
          </button>

          <!-- Left Type Badge -->
          <div class="edc-type-box" :class="e.type === '八股文' ? 'edc-type-blue' : 'edc-type-green'">
            <ReadOutlined v-if="e.type === '八股文'" />
            <CodeOutlined v-else />
          </div>

          <!-- Main Info -->
          <div class="edc-content-area">
            <div class="edc-title-text">{{ e.title }}</div>
            <div class="edc-tags-line" v-if="e.tags?.length">
              <span v-for="t in e.tags.slice(0, 5)" :key="t" class="edc-tag-item">{{ t }}</span>
            </div>
          </div>

          <!-- Right Meta & Actions -->
          <div class="edc-right-meta">
            <span class="diff-pill" :class="`diff-${e.difficulty}`">{{ e.difficulty }}</span>
            <span class="status-pill" :class="`status-${e.status}`">{{ e.status }}</span>
            <span class="edc-time">{{ e.createdAt?.slice(0, 10) }}</span>

            <div class="edc-btn-group" @click.stop>
              <button class="edc-action-icon-btn" title="编辑题目" @click="openEdit(e)">
                <EditOutlined />
              </button>
              <a-popconfirm
                title="确认删除该条目？"
                ok-text="确认删除"
                cancel-text="取消"
                ok-type="danger"
                @confirm="handleDelete(e.id)"
              >
                <button class="edc-action-icon-btn btn-del" title="删除题目">
                  <DeleteOutlined />
                </button>
              </a-popconfirm>
            </div>
          </div>
        </div>
      </div>

      <!-- Modals & Drawers -->
      <EntryModal
        v-model:open="modalOpen"
        :company-id="route.params.id"
        :edit-data="editTarget"
        @saved="modalOpen = false"
      />

      <!-- Right Drawer for Detail Reading -->
      <a-drawer
        v-model:open="drawerOpen"
        :title="detailEntry?.title"
        width="760"
        placement="right"
      >
        <div v-if="detailEntry" class="drawer-detail-body">
          <div class="drawer-meta-header">
            <button
              class="drawer-star-btn"
              :class="{ 'star-lit': detailEntry.isStarred }"
              @click="toggleStar(detailEntry.id)"
            >
              <StarFilled v-if="detailEntry.isStarred" />
              <StarOutlined v-else />
              {{ detailEntry.isStarred ? '高频必背题' : '加入必背' }}
            </button>
            <span class="drawer-type-badge" :class="detailEntry.type === '八股文' ? 'dtb-blue' : 'dtb-green'">
              <ReadOutlined v-if="detailEntry.type === '八股文'" />
              <CodeOutlined v-else />
              {{ detailEntry.type }}
            </span>
            <span class="diff-pill" :class="`diff-${detailEntry.difficulty}`">{{ detailEntry.difficulty }}</span>
            <span class="status-pill" :class="`status-${detailEntry.status}`">{{ detailEntry.status }}</span>
            <div class="drawer-tags-wrap" v-if="detailEntry.tags?.length">
              <span v-for="t in detailEntry.tags" :key="t" class="drawer-tag">{{ t }}</span>
            </div>
          </div>

          <div class="drawer-divider"></div>

          <!-- Highlighted Render Content -->
          <div class="drawer-markdown-render" v-html="renderMd(detailEntry.content)"></div>
        </div>
      </a-drawer>

      <!-- Hidden Printable Container for PDF Generation -->
      <div id="printable-handbook-area" class="printable-handbook-area">
        <h1 class="print-title">{{ company.name }} · 八股文核心考点离线备战手册</h1>
        <p class="print-meta">生成时间：{{ new Date().toLocaleString() }} | 归纳题目：{{ baguEntries.length }} 篇</p>
        <hr class="print-hr" />

        <div v-for="(b, idx) in baguEntries" :key="b.id" class="print-entry-item">
          <h2 class="print-entry-title">{{ idx + 1 }}. {{ b.title }} 【难度：{{ b.difficulty }} | 掌握度：{{ b.status }}】</h2>
          <div class="print-entry-content" v-html="renderMd(b.content)"></div>
          <hr class="print-sub-hr" />
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Empty, message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReadOutlined,
  CodeOutlined,
  SearchOutlined,
  AppstoreOutlined,
  ArrowLeftOutlined,
  StarFilled,
  StarOutlined,
  FileTextOutlined,
  DownloadOutlined,
  PrinterOutlined,
  DownOutlined,
  CompassOutlined,
} from '@ant-design/icons-vue'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import cpp from 'highlight.js/lib/languages/cpp'
import python from 'highlight.js/lib/languages/python'
import 'highlight.js/styles/atom-one-dark.css'
import AppLayout from '../components/AppLayout.vue'
import EntryModal from '../components/EntryModal.vue'
import CompanyLogo from '../components/CompanyLogo.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('python', python)

const route = useRoute()
const router = useRouter()
const store = useInterviewStore()

const company = computed(() => store.getCompany(route.params.id))
const allEntries = computed(() => store.getEntriesByCompany(route.params.id))

const activeTab = ref('all')
const keyword = ref('')
const filterDiff = ref(null)
const filterStatus = ref(null)
const modalOpen = ref(false)
const editTarget = ref(null)
const drawerOpen = ref(false)
const detailEntry = ref(null)

const baguEntries = computed(() => allEntries.value.filter(e => e.type === '八股文'))
const baguCount = computed(() => baguEntries.value.length)
const algoCount = computed(() => allEntries.value.filter(e => e.type === '算法题').length)
const masteredCount = computed(() => allEntries.value.filter(e => e.status === '已掌握').length)

const tabs = computed(() => [
  { key: 'all', label: '全部题目', icon: AppstoreOutlined, count: allEntries.value.length },
  { key: 'bagu', label: '八股文', icon: ReadOutlined, count: baguCount.value },
  { key: 'algo', label: '手撕算法', icon: CodeOutlined, count: algoCount.value },
])

const filtered = computed(() => {
  let list = allEntries.value
  if (activeTab.value === 'bagu') list = list.filter(e => e.type === '八股文')
  if (activeTab.value === 'algo') list = list.filter(e => e.type === '算法题')
  if (keyword.value) {
    const q = keyword.value.toLowerCase()
    list = list.filter(e => e.title?.toLowerCase().includes(q) || e.content?.toLowerCase().includes(q))
  }
  if (filterDiff.value) list = list.filter(e => e.difficulty === filterDiff.value)
  if (filterStatus.value) list = list.filter(e => e.status === filterStatus.value)
  return list
})

function openAdd() {
  editTarget.value = null
  modalOpen.value = true
}

function openEdit(e) {
  editTarget.value = e
  modalOpen.value = true
}

function openDetail(e) {
  detailEntry.value = e
  drawerOpen.value = true
}

function toggleStar(id) {
  store.toggleStar(id)
  const item = store.entries.find(e => e.id === id)
  if (item?.isStarred) message.success('已标记为 ⭐ 高频必背题')
  else message.info('已取消星标')
}

function handleDelete(id) {
  store.deleteEntry(id)
  message.success('面试题目已删除')
}

// ===== Export Handbook (Bagu Only, No Algo) =====
function exportMarkdownHandbook() {
  if (!baguEntries.value.length) {
    message.warning('当前企业暂无八股文考点可导出')
    return
  }

  let md = `# ${company.value.name} · 八股理论考点离线备战手册\n\n`
  md += `> 导出时间：${new Date().toLocaleDateString()} | 八股考点：共 ${baguEntries.value.length} 篇\n\n---\n\n`

  baguEntries.value.forEach((b, idx) => {
    md += `## ${idx + 1}. ${b.title}\n`
    md += `- **难度**：${b.difficulty}\n`
    md += `- **掌握状态**：${b.status}\n`
    md += `- **技术标签**：${(b.tags || []).join(', ') || '无'}\n\n`
    md += `### 核心深度解析\n\n${b.content}\n\n---\n\n`
  })

  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${company.value.name}_八股文备战手册.md`
  link.click()
  URL.revokeObjectURL(url)
  message.success(`已成功导出 ${company.value.name} 八股备战手册 (.md) 🎉`)
}

function printPdfHandbook() {
  if (!baguEntries.value.length) {
    message.warning('当前企业暂无八股文考点可打印')
    return
  }
  window.print()
}

function renderMd(content) {
  if (!content) return '<p style="color:var(--text-3); padding: 40px 0; text-align: center;">暂无解析内容</p>'
  return content
    .replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
      let hl = ''
      const language = lang || 'java'
      try {
        hl = hljs.highlight(code, { language }).value
      } catch {
        hl = hljs.highlightAuto(code).value
      }
      return `
        <div class="render-code-wrapper">
          <div class="render-code-header">
            <div class="rch-left">
              <span class="rc-dot rd-red"></span>
              <span class="rc-dot rd-yellow"></span>
              <span class="rc-dot rd-green"></span>
              <span class="rc-lang-tag">${language.toUpperCase()}</span>
            </div>
            <button class="copy-code-btn" onclick="navigator.clipboard.writeText(\`${code.replace(/`/g, '\\`').replace(/\\/g, '\\\\')}\`); alert('代码已复制到剪贴板！')">
              复制代码
            </button>
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
.company-detail-page {
  max-width: 1280px;
  margin: 0 auto;
}

.company-not-found {
  padding: 80px 0;
  text-align: center;
}

/* ===== Company Profile Hero Banner ===== */
.company-hero-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 24px 30px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
}

.hero-left-profile {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-link-row {
  margin-bottom: 4px;
}
.back-link {
  font-size: 12px;
  color: var(--accent);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  transition: opacity 0.18s;
}
.back-link:hover {
  opacity: 0.8;
}

.hero-company-name {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.4px;
  margin-bottom: 6px;
}

.hero-meta-badges {
  display: flex;
  align-items: center;
  gap: 10px;
}
.industry-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--purple-subtle);
  color: var(--purple);
  border: 1px solid var(--purple-border);
  font-weight: 600;
}
.created-date {
  font-size: 12px;
  color: var(--text-3);
}

.hero-right-actions-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.hero-stats-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hero-stat-pill {
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 86px;
}
.hsp-num {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 2px;
}
.text-blue { color: #2563eb; }
.text-green { color: #059669; }
.text-purple { color: #7c3aed; }
.hsp-label {
  font-size: 10.5px;
  color: var(--text-3);
  font-weight: 500;
}

.hero-btn-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.export-handbook-btn {
  font-size: 12px !important;
  color: var(--text-1) !important;
  border-radius: 8px !important;
}

.hero-zen-btn {
  font-size: 12px !important;
  background: #0f172a !important;
  color: #ffffff !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
}

/* ===== Toolbar ===== */
.detail-toolbar-card {
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

.tab-pills-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-pill-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-2);
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.18s ease;
}
.filter-pill-btn:hover {
  background: var(--bg-subtle);
  color: var(--text-1);
}
.pill-active {
  background: var(--accent-subtle) !important;
  color: var(--accent) !important;
  border-color: var(--accent-border) !important;
  font-weight: 600;
}

.pill-count-num {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.05);
}
.pill-active .pill-count-num {
  background: var(--accent);
  color: #ffffff;
}

.toolbar-right-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.add-entry-btn {
  font-size: 13px !important;
}

/* ===== Entry Cards List ===== */
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
  gap: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.entry-detail-card:hover {
  border-color: var(--accent-border);
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.entry-star-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  color: #cbd5e1;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s;
}
.entry-star-btn:hover {
  color: #f59e0b;
  transform: scale(1.15);
}
.star-lit {
  color: #f59e0b !important;
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
  gap: 4px;
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

.edc-btn-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.edc-action-icon-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s;
}
.edc-action-icon-btn:hover {
  background: var(--bg-subtle);
  color: var(--text-1);
}
.btn-del:hover {
  color: #e11d48 !important;
  background: #fff1f2 !important;
}

.empty-entries-view {
  padding: 60px 0;
  text-align: center;
}

/* ===== Drawer Detail ===== */
.drawer-meta-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.drawer-star-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 6px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #d97706;
  cursor: pointer;
  font-weight: 600;
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
.dtb-blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.dtb-green { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }

.drawer-tags-wrap {
  display: flex;
  gap: 4px;
}
.drawer-tag {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 4px;
  background: var(--bg-subtle);
  color: var(--text-2);
}

.drawer-divider {
  height: 1px;
  background: var(--border-light);
  margin: 16px 0 20px;
}

.drawer-markdown-render {
  color: var(--text-1);
  line-height: 1.85;
  font-size: 14px;
}

:global(.render-h1) { font-size: 20px; font-weight: 700; color: var(--text-1); margin: 18px 0 8px; }
:global(.render-h2) { font-size: 16px; font-weight: 700; color: var(--text-1); margin: 16px 0 6px; }
:global(.render-h3) { font-size: 14px; font-weight: 600; color: var(--text-1); margin: 12px 0 4px; }

:global(.render-inline-code) {
  background: var(--accent-subtle);
  color: var(--accent);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', monospace;
  font-size: 12px;
}

:global(.render-li) {
  margin: 4px 0;
  color: var(--text-2);
}
:global(.render-strong) {
  color: var(--text-1);
  font-weight: 700;
}

/* ===== Real High-Contrast Code Block Window in Drawer ===== */
:global(.render-code-wrapper) {
  background: #282c34;
  border-radius: 12px;
  border: 1px solid #1e2227;
  overflow: hidden;
  margin: 14px 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

:global(.render-code-header) {
  height: 38px;
  background: #21252b;
  border-bottom: 1px solid #181a1f;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

:global(.rch-left) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:global(.rc-dot) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
:global(.rd-red) { background: #ff5f56; }
:global(.rd-yellow) { background: #ffbd2e; }
:global(.rd-green) { background: #27c93f; }

:global(.rc-lang-tag) {
  font-size: 11px;
  font-weight: 700;
  color: #abb2bf;
  margin-left: 6px;
  letter-spacing: 0.5px;
}

:global(.copy-code-btn) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #abb2bf;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.18s;
}
:global(.copy-code-btn:hover) {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

:global(.render-code-pre) {
  margin: 0 !important;
  padding: 14px 18px !important;
  background: transparent !important;
  overflow-x: auto;
}

:global(.render-code-pre code) {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13.5px !important;
  line-height: 1.65 !important;
  background: transparent !important;
}

/* Printable area */
.printable-handbook-area {
  display: none;
}

@media print {
  body * {
    visibility: hidden;
  }
  .printable-handbook-area, .printable-handbook-area * {
    visibility: visible;
  }
  .printable-handbook-area {
    display: block !important;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    padding: 20px;
    background: #ffffff;
    color: #000000;
  }
  .print-title {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 6px;
  }
  .print-meta {
    font-size: 12px;
    color: #666666;
    margin-bottom: 16px;
  }
  .print-hr {
    border: none;
    border-top: 2px solid #000000;
    margin-bottom: 24px;
  }
  .print-entry-item {
    page-break-inside: avoid;
    margin-bottom: 24px;
  }
  .print-entry-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
  }
  .print-sub-hr {
    border: none;
    border-top: 1px dashed #cccccc;
    margin-top: 16px;
  }
}
</style>
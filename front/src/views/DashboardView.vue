<template>
  <AppLayout>
    <div class="dashboard-page-container">
      <!-- 1. Top Welcome & Quick Actions (Zen Mode / Mock Interview / Global Search) -->
      <div class="dash-welcome-row">
        <div>
          <h1 class="welcome-heading">面试收纳与备战工作台</h1>
          <p class="welcome-sub-text">实时跟踪各大目标企业面经储备、技术栈雷达掌握度与算法手撕进度</p>
        </div>
        <div class="dash-top-actions">
          <!-- 1. 禅定背题模式 (Solid Violet 实心紫) -->
          <button class="solid-btn btn-solid-violet" @click="$router.push('/zen-mode')">
            <CompassOutlined />
            <span>禅定背题模式</span>
          </button>

          <!-- 2. 随机模拟抽题 (Solid Emerald 实心绿) -->
          <button class="solid-btn btn-solid-emerald" @click="mockModalOpen = true">
            <ThunderboltOutlined />
            <span>随机模拟抽题</span>
          </button>

          <!-- 3. 全局检索 (Solid Indigo 实心蓝) -->
          <button class="solid-btn btn-solid-indigo" @click="$router.push('/search')">
            <SearchOutlined />
            <span>全局检索</span>
          </button>
        </div>
      </div>

      <!-- 2. Core Metrics Cards (5 Cards) -->
      <div class="metrics-summary-grid">
        <div class="metric-card-item">
          <div class="mci-icon-box bg-blue-subtle text-blue">
            <BankOutlined />
          </div>
          <div class="mci-info">
            <div class="mci-value">{{ store.companies.length }}</div>
            <div class="mci-label">目标企业总数</div>
          </div>
        </div>

        <div class="metric-card-item">
          <div class="mci-icon-box bg-indigo-subtle text-indigo">
            <BookOutlined />
          </div>
          <div class="mci-info">
            <div class="mci-value">{{ store.entries.length }}</div>
            <div class="mci-label">累计收纳题目</div>
          </div>
        </div>

        <div class="metric-card-item">
          <div class="mci-icon-box bg-sky-subtle text-sky">
            <ReadOutlined />
          </div>
          <div class="mci-info">
            <div class="mci-value">{{ baguCount }}</div>
            <div class="mci-label">八股理论考点</div>
          </div>
        </div>

        <div class="metric-card-item">
          <div class="mci-icon-box bg-emerald-subtle text-emerald">
            <CodeOutlined />
          </div>
          <div class="mci-info">
            <div class="mci-value">{{ algoCount }}</div>
            <div class="mci-label">算法手撕题解</div>
          </div>
        </div>

        <div class="metric-card-item" @click="$router.push('/favorites')" style="cursor: pointer;" title="查看高频必背题库">
          <div class="mci-icon-box bg-amber-subtle text-amber">
            <StarFilled />
          </div>
          <div class="mci-info">
            <div class="mci-value">{{ store.starredEntries.length }}</div>
            <div class="mci-label">⭐ 高频必背压轴题</div>
          </div>
        </div>
      </div>

      <!-- 3. Advanced Visualizations Grid (Radar Chart & Donut Chart) -->
      <div class="visualizations-dual-grid">
        <!-- A. 6-Dimension Tech Stack Radar Chart -->
        <div class="viz-card-item">
          <div class="viz-card-header">
            <div class="vch-left">
              <span class="vch-dot bg-indigo"></span>
              <span class="vch-title">技术栈六维能力雷达图</span>
            </div>
            <span class="vch-hint">根据八股与算法掌握率动态计算</span>
          </div>

          <div class="radar-chart-container">
            <svg viewBox="0 0 400 320" class="radar-svg">
              <!-- Radar Web Grids (4 concentric polygons) -->
              <polygon
                v-for="lvl in [0.25, 0.5, 0.75, 1.0]"
                :key="lvl"
                :points="getWebPolygonPoints(lvl)"
                class="radar-web-polygon"
              />

              <!-- Axis Rays -->
              <line
                v-for="(axis, idx) in radarAxes"
                :key="idx"
                :x1="center.x"
                :y1="center.y"
                :x2="axis.x"
                :y2="axis.y"
                class="radar-axis-line"
              />

              <!-- Filled Data Polygon with Gradient -->
              <defs>
                <linearGradient id="radarGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4f46e5" stop-opacity="0.6" />
                  <stop offset="100%" stop-color="#06b6d4" stop-opacity="0.3" />
                </linearGradient>
              </defs>
              <polygon :points="radarDataPolygonPoints" fill="url(#radarGrad)" stroke="#4f46e5" stroke-width="2.5" />

              <!-- Vertex Dots & Scores -->
              <g v-for="(pt, idx) in radarVertexPoints" :key="idx">
                <circle :cx="pt.x" :cy="pt.y" r="5" fill="#4f46e5" stroke="#ffffff" stroke-width="2" class="radar-dot" />
              </g>

              <!-- Dimension Labels -->
              <text
                v-for="(axis, idx) in radarAxes"
                :key="`lbl-${idx}`"
                :x="axis.labelX"
                :y="axis.labelY"
                class="radar-label-text"
                :text-anchor="axis.anchor"
              >
                {{ store.radarSkillStats[idx]?.dimension }}
                <tspan class="radar-score-badge" fill="#4f46e5"> {{ store.radarSkillStats[idx]?.score }}分</tspan>
              </text>
            </svg>
          </div>
        </div>

        <!-- B. Company Question Distribution Donut Chart -->
        <div class="viz-card-item">
          <div class="viz-card-header">
            <div class="vch-left">
              <span class="vch-dot bg-emerald"></span>
              <span class="vch-title">目标企业题量分布占比</span>
            </div>
            <span class="vch-hint">共收录 {{ store.companies.length }} 家企业题库</span>
          </div>

          <div class="donut-chart-flex">
            <!-- Donut SVG Wrap with Perfect Centered HTML Overlay -->
            <div class="donut-svg-wrap">
              <svg viewBox="0 0 200 200" class="donut-svg">
                <!-- Base Circle -->
                <circle cx="100" cy="100" r="70" fill="transparent" stroke="#f1f5f9" stroke-width="22" />

                <!-- Segments -->
                <circle
                  v-for="(seg, idx) in donutSegments"
                  :key="idx"
                  cx="100"
                  cy="100"
                  r="70"
                  fill="transparent"
                  :stroke="seg.color"
                  stroke-width="22"
                  :stroke-dasharray="`${seg.strokeLength} ${440 - seg.strokeLength}`"
                  :stroke-dashoffset="-seg.offset"
                  class="donut-segment"
                />
              </svg>

              <!-- HTML Center Text (100% Never overlaps or misaligns) -->
              <div class="donut-center-overlay">
                <span class="dco-number">{{ store.entries.length }}</span>
                <span class="dco-label">总题目数</span>
              </div>
            </div>

            <!-- Legend List -->
            <div class="donut-legend-list">
              <div
                v-for="item in store.companyDistributionStats.slice(0, 5)"
                :key="item.id"
                class="legend-item"
                @click="$router.push(`/companies/${item.id}`)"
              >
                <span class="legend-color-dot" :style="{ background: item.color }"></span>
                <span class="legend-company-name">{{ item.name }}</span>
                <span class="legend-count-pill">{{ item.count }} 题</span>
                <span class="legend-pct">{{ item.percentage }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. Target Companies Quick Rail -->
      <div class="section-card-wrapper">
        <div class="section-title-row">
          <div class="str-title-group">
            <span class="str-icon-dot"></span>
            <h2 class="str-main-title">我的目标企业</h2>
            <span class="str-badge">{{ store.companies.length }} 家已建档</span>
          </div>
          <router-link to="/companies" class="str-link-more">
            查看企业列表 <RightOutlined />
          </router-link>
        </div>

        <div v-if="!store.companies.length" class="empty-state-box">
          <a-empty description="暂无目标企业，进入【公司与题库】即可添加" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        </div>

        <div v-else class="companies-rail-grid">
          <div
            v-for="c in store.companies.slice(0, 6)"
            :key="c.id"
            class="company-rail-card"
            @click="$router.push(`/companies/${c.id}`)"
          >
            <div class="crc-top">
              <CompanyLogo :logo="c.logo" :name="c.name" :size="44" :radius="12" />
              <span class="crc-count-badge">{{ getCompanyEntryCount(c.id) }} 题收纳</span>
            </div>
            <div class="crc-name">{{ c.name }}</div>
            <div class="crc-bottom-stats">
              <span class="crc-count-text">进入专属题库</span>
              <RightOutlined class="crc-arrow" />
            </div>
          </div>
        </div>
      </div>

      <!-- 5. Recent Entries List (With Star Toggle) -->
      <div class="section-card-wrapper">
        <div class="section-title-row">
          <div class="str-title-group">
            <span class="str-icon-dot dot-emerald"></span>
            <h2 class="str-main-title">最近录入的面试题目</h2>
            <span class="str-badge">{{ recentEntries.length }} 条最新动态</span>
          </div>
          <router-link to="/favorites" class="str-link-more" v-if="store.starredEntries.length">
            ⭐ 查看高频必背压轴题 ({{ store.starredEntries.length }}) <RightOutlined />
          </router-link>
        </div>

        <div v-if="!recentEntries.length" class="empty-state-box">
          <a-empty description="暂无题目记录，进入企业详情页即可开始录入" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        </div>

        <div v-else class="recent-entries-table-card">
          <div
            v-for="e in recentEntries"
            :key="e.id"
            class="entry-table-row"
            @click="$router.push(`/companies/${e.companyId}`)"
          >
            <!-- Star Button -->
            <button
              class="star-toggle-btn"
              :class="{ 'star-active': e.isStarred }"
              @click.stop="toggleStar(e.id)"
              :title="e.isStarred ? '取消高频必背星标' : '标记为高频必背压轴题'"
            >
              <StarFilled v-if="e.isStarred" />
              <StarOutlined v-else />
            </button>

            <div class="etr-type-badge" :class="e.type === '八股文' ? 'type-blue' : 'type-green'">
              <ReadOutlined v-if="e.type === '八股文'" />
              <CodeOutlined v-else />
              <span>{{ e.type }}</span>
            </div>

            <div class="etr-title-info">
              <div class="etr-title">{{ e.title }}</div>
              <div class="etr-company-hint">
                <span class="ech-badge">{{ getCompanyName(e.companyId) }}</span>
                <span v-for="t in (e.tags || []).slice(0, 3)" :key="t" class="ech-tag">{{ t }}</span>
              </div>
            </div>

            <div class="etr-meta-right">
              <span class="diff-chip" :class="`diff-${e.difficulty}`">{{ e.difficulty }}</span>
              <span class="status-chip" :class="`status-${e.status}`">{{ e.status }}</span>
              <span class="etr-time">{{ e.createdAt?.slice(0, 10) }}</span>
              <RightOutlined class="etr-arrow" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <MockInterviewModal v-model:open="mockModalOpen" />
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Empty, message } from 'ant-design-vue'
import {
  BankOutlined,
  BookOutlined,
  ReadOutlined,
  CodeOutlined,
  StarFilled,
  StarOutlined,
  ThunderboltOutlined,
  CompassOutlined,
  SearchOutlined,
  RightOutlined,
} from '@ant-design/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import CompanyLogo from '../components/CompanyLogo.vue'
import MockInterviewModal from '../components/MockInterviewModal.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const store = useInterviewStore()
const mockModalOpen = ref(false)

const baguCount = computed(() => store.entries.filter(e => e.type === '八股文').length)
const algoCount = computed(() => store.entries.filter(e => e.type === '算法题').length)

const recentEntries = computed(() => {
  return [...store.entries].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 8)
})

function getCompanyName(companyId) {
  const c = store.companies.find(item => item.id === companyId)
  return c ? c.name : '未知企业'
}

function getCompanyEntryCount(companyId) {
  return store.entries.filter(e => e.companyId === companyId).length
}

function toggleStar(id) {
  store.toggleStar(id)
  const isNow = store.entries.find(e => e.id === id)?.isStarred
  if (isNow) message.success('已标记为 ⭐ 高频必背压轴题')
  else message.info('已取消星标')
}

// ===== Radar Chart Math Geometry =====
const center = { x: 200, y: 160 }
const radarRadius = 100
const totalSides = 6

const radarAxes = computed(() => {
  const axes = []
  for (let i = 0; i < totalSides; i++) {
    const angle = (Math.PI * 2 / totalSides) * i - Math.PI / 2
    const x = center.x + Math.cos(angle) * radarRadius
    const y = center.y + Math.sin(angle) * radarRadius

    // Label Position slightly further out
    const labelX = center.x + Math.cos(angle) * (radarRadius + 22)
    const labelY = center.y + Math.sin(angle) * (radarRadius + 18)

    let anchor = 'middle'
    if (Math.cos(angle) > 0.3) anchor = 'start'
    else if (Math.cos(angle) < -0.3) anchor = 'end'

    axes.push({ x, y, labelX, labelY, anchor })
  }
  return axes
})

function getWebPolygonPoints(levelRatio) {
  const pts = []
  for (let i = 0; i < totalSides; i++) {
    const angle = (Math.PI * 2 / totalSides) * i - Math.PI / 2
    const x = center.x + Math.cos(angle) * (radarRadius * levelRatio)
    const y = center.y + Math.sin(angle) * (radarRadius * levelRatio)
    pts.push(`${x},${y}`)
  }
  return pts.join(' ')
}

const radarDataPolygonPoints = computed(() => {
  const stats = store.radarSkillStats
  const pts = []
  for (let i = 0; i < totalSides; i++) {
    const angle = (Math.PI * 2 / totalSides) * i - Math.PI / 2
    const scoreRatio = (stats[i]?.score || 30) / 100
    const x = center.x + Math.cos(angle) * (radarRadius * scoreRatio)
    const y = center.y + Math.sin(angle) * (radarRadius * scoreRatio)
    pts.push(`${x},${y}`)
  }
  return pts.join(' ')
})

const radarVertexPoints = computed(() => {
  const stats = store.radarSkillStats
  const pts = []
  for (let i = 0; i < totalSides; i++) {
    const angle = (Math.PI * 2 / totalSides) * i - Math.PI / 2
    const scoreRatio = (stats[i]?.score || 30) / 100
    const x = center.x + Math.cos(angle) * (radarRadius * scoreRatio)
    const y = center.y + Math.sin(angle) * (radarRadius * scoreRatio)
    pts.push({ x, y })
  }
  return pts
})

// ===== Donut Chart Geometry =====
const circumference = 2 * Math.PI * 70 // ~439.82

const donutSegments = computed(() => {
  const stats = store.companyDistributionStats
  const total = store.entries.length || 1
  let currentOffset = 0

  return stats.map(item => {
    const ratio = item.count / total
    const strokeLength = ratio * circumference
    const seg = {
      ...item,
      strokeLength,
      offset: currentOffset
    }
    currentOffset += strokeLength
    return seg
  })
})
</script>

<style scoped>
.dashboard-page-container {
  max-width: 1280px;
  margin: 0 auto;
}

/* Top Welcome */
.dash-welcome-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.welcome-heading {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.5px;
  margin-bottom: 4px;
}
.welcome-sub-text {
  font-size: 13px;
  color: var(--text-2);
}

/* Solid Color Action Buttons Group */
.dash-top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.solid-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.18s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}
.solid-btn:hover {
  transform: translateY(-1.5px);
  filter: brightness(1.08);
}
.solid-btn:active {
  transform: translateY(0);
}

/* 1. 禅定背题 (Solid Violet 实心紫) */
.btn-solid-violet {
  background: #7c3aed;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.25);
}
.btn-solid-violet:hover {
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.35);
}

/* 2. 随机模拟抽题 (Solid Emerald 实心绿) */
.btn-solid-emerald {
  background: #059669;
  box-shadow: 0 2px 8px rgba(5, 150, 105, 0.25);
}
.btn-solid-emerald:hover {
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.35);
}

/* 3. 全局检索 (Solid Indigo 实心蓝) */
.btn-solid-indigo {
  background: #4f46e5;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.25);
}
.btn-solid-indigo:hover {
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.35);
}

/* Metrics Summary */
.metrics-summary-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card-item {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  transition: all 0.2s ease;
}

.metric-card-item:hover {
  border-color: var(--accent-border);
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.mci-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.bg-blue-subtle { background: #eff6ff; }
.text-blue { color: #2563eb; }
.bg-indigo-subtle { background: #eef2ff; }
.text-indigo { color: #4f46e5; }
.bg-sky-subtle { background: #f0f9ff; }
.text-sky { color: #0284c7; }
.bg-emerald-subtle { background: #ecfdf5; }
.text-emerald { color: #059669; }
.bg-amber-subtle { background: #fffbeb; }
.text-amber { color: #d97706; }

.mci-value {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
  line-height: 1.1;
  margin-bottom: 2px;
}
.mci-label {
  font-size: 12px;
  color: var(--text-3);
  font-weight: 500;
}

/* ===== Visualizations Dual Grid ===== */
.visualizations-dual-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.viz-card-item {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.viz-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.vch-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.vch-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.bg-indigo { background: #4f46e5; }
.bg-emerald { background: #059669; }

.vch-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
}
.vch-hint {
  font-size: 11px;
  color: var(--text-3);
}

/* Radar SVG */
.radar-chart-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 250px;
}
.radar-svg {
  width: 100%;
  height: 100%;
}

.radar-web-polygon {
  fill: none;
  stroke: #e2e8f0;
  stroke-width: 1;
}
.radar-axis-line {
  stroke: #e2e8f0;
  stroke-width: 1;
  stroke-dasharray: 3, 3;
}
.radar-dot {
  transition: all 0.2s;
}
.radar-label-text {
  font-size: 10.5px;
  fill: var(--text-2);
  font-weight: 600;
  dominant-baseline: central;
}
.radar-score-badge {
  font-weight: 700;
}

/* Donut Chart */
.donut-chart-flex {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 250px;
}

.donut-svg-wrap {
  width: 180px;
  height: 180px;
  flex-shrink: 0;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.donut-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.donut-segment {
  transition: stroke-dasharray 0.5s ease;
}

/* Perfect Absolute Centered HTML Text */
.donut-center-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  text-align: center;
}

.dco-number {
  font-size: 28px;
  font-weight: 900;
  color: var(--text-1);
  line-height: 1.1;
  margin-bottom: 2px;
}

.dco-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-3);
  letter-spacing: 0.5px;
}

.donut-legend-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 220px;
  overflow-y: auto;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  background: var(--bg-subtle);
  cursor: pointer;
  transition: all 0.18s;
}
.legend-item:hover {
  background: #ffffff;
  border: 1px solid var(--border);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.legend-color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.legend-company-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  flex: 1;
}
.legend-count-pill {
  font-size: 11px;
  color: var(--text-3);
}
.legend-pct {
  font-size: 11px;
  font-weight: 700;
  color: var(--accent);
}

/* Sections */
.section-card-wrapper {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.str-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.str-icon-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
}
.dot-emerald {
  background: #059669 !important;
}

.str-main-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-1);
}

.str-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 100px;
  background: var(--bg-subtle);
  color: var(--text-2);
  border: 1px solid var(--border);
}

.str-link-more {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* Companies Rail Grid */
.companies-rail-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}

.company-rail-card {
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.company-rail-card:hover {
  background: #ffffff;
  border-color: var(--accent);
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.crc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.crc-count-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 100px;
  background: var(--accent-subtle);
  color: var(--accent);
}

.crc-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.crc-bottom-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-3);
}
.crc-count-text strong {
  color: var(--accent);
}
.crc-arrow {
  font-size: 10px;
  color: var(--text-3);
  transition: transform 0.18s;
}
.company-rail-card:hover .crc-arrow {
  transform: translateX(2px);
  color: var(--accent);
}

/* Recent Entries Table */
.recent-entries-table-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.entry-table-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  background: var(--bg-subtle);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.18s ease;
}

.entry-table-row:hover {
  background: #ffffff;
  border-color: var(--accent-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.star-toggle-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  color: #cbd5e1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s;
  padding: 4px;
}
.star-toggle-btn:hover {
  color: #f59e0b;
  transform: scale(1.15);
}
.star-active {
  color: #f59e0b !important;
}

.etr-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
  flex-shrink: 0;
}
.type-blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.type-green { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }

.etr-title-info {
  flex: 1;
  overflow: hidden;
}
.etr-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}
.etr-company-hint {
  display: flex;
  align-items: center;
  gap: 6px;
}
.ech-badge {
  font-size: 11px;
  color: var(--accent);
  background: var(--accent-subtle);
  padding: 0 6px;
  border-radius: 4px;
  font-weight: 500;
}
.ech-tag {
  font-size: 11px;
  color: var(--text-3);
}

.etr-meta-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.diff-chip {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
}
.diff-简单 { background: #ecfdf5; color: #059669; }
.diff-中等 { background: #fffbeb; color: #d97706; }
.diff-困难 { background: #fff1f2; color: #e11d48; }

.status-chip {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #ffffff;
  color: var(--text-2);
  border: 1px solid var(--border);
}
.status-已掌握 { background: #ecfdf5; color: #059669; border-color: #a7f3d0; font-weight: 600; }

.etr-time {
  font-size: 12px;
  color: var(--text-3);
}
.etr-arrow {
  font-size: 12px;
  color: var(--text-3);
}

.empty-state-box {
  padding: 40px 0;
  text-align: center;
}
</style>

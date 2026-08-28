<template>
  <AppLayout>
    <div class="daily-review-page-container">
      <!-- 1. Header Row -->
      <div class="dr-header-row">
        <div>
          <h1 class="dr-main-title">📅 每日复盘与学习打卡</h1>
          <p class="dr-sub-title">按日期记录每日备战轨迹，沉淀复盘心得，一键开启当日专项闪卡二刷</p>
        </div>

        <!-- Date Quick Navigation -->
        <div class="dr-date-picker-group">
          <button class="dr-nav-day-btn" @click="shiftDate(-1)" title="前一天">
            <LeftOutlined />
          </button>
          <div class="dr-current-date-badge">
            <CalendarOutlined class="dr-cal-icon" />
            <span class="dr-date-text">{{ formattedSelectedDate }}</span>
            <span class="dr-weekday-tag">{{ weekdayText }}</span>
          </div>
          <button class="dr-nav-day-btn" :disabled="isToday" @click="shiftDate(1)" title="后一天">
            <RightOutlined />
          </button>
          <button class="dr-today-btn" v-if="!isToday" @click="selectedDateStr = getTodayStr()">
            回到今天
          </button>
        </div>
      </div>

      <!-- 2. GitHub-Style Activity Heatmap Grid -->
      <div class="heatmap-card-wrapper">
        <div class="hcw-header">
          <div class="hcw-title-wrap">
            <span class="hcw-dot"></span>
            <span class="hcw-title">备战活跃度打卡热力图 (近 12 周)</span>
          </div>

          <div class="hcw-stats-pills">
            <span class="hcw-pill">🔥 连续打卡：<strong>{{ streakDays }}</strong> 天</span>
            <span class="hcw-pill">📚 近 30 天收纳：<strong>{{ last30DaysCount }}</strong> 题</span>
          </div>
        </div>

        <!-- Heatmap Grid Matrix -->
        <div class="heatmap-matrix-scroll">
          <div class="heatmap-matrix">
            <div
              v-for="col in heatmapWeeks"
              :key="col.weekIndex"
              class="heatmap-column"
            >
              <div
                v-for="day in col.days"
                :key="day.dateStr"
                class="heatmap-cell"
                :class="[
                  `level-${day.level}`,
                  { 'cell-selected': day.dateStr === selectedDateStr },
                  { 'cell-future': day.isFuture }
                ]"
                @click="!day.isFuture && (selectedDateStr = day.dateStr)"
              >
                <a-tooltip :title="`${day.dateStr}：收纳/复习 ${day.count} 题`" placement="top">
                  <div class="cell-inner"></div>
                </a-tooltip>
              </div>
            </div>
          </div>

          <!-- Heatmap Legend -->
          <div class="heatmap-legend-row">
            <span class="hlr-label">少</span>
            <span class="legend-box level-0"></span>
            <span class="legend-box level-1"></span>
            <span class="legend-box level-2"></span>
            <span class="legend-box level-3"></span>
            <span class="legend-box level-4"></span>
            <span class="hlr-label">多</span>
          </div>
        </div>
      </div>

      <!-- 3. Dual Columns: Daily Briefing & Reflection Journal Pad -->
      <div class="daily-dual-grid">
        <!-- A. Daily Briefing Card -->
        <div class="daily-card-box">
          <div class="dcb-header">
            <div class="dcb-title-left">
              <span class="dcb-icon">📊</span>
              <span class="dcb-title">{{ formattedSelectedDate }} 学习战报</span>
            </div>

            <!-- Start Sprint Flashcard Button for Selected Day -->
            <button
              v-if="dayEntries.length"
              class="day-sprint-btn"
              @click="startDayZenSprint"
            >
              <ThunderboltOutlined />
              <span>复习本日所学 ({{ dayEntries.length }} 题)</span>
            </button>
          </div>

          <!-- Daily Metrics Row -->
          <div class="dcb-metrics-strip">
            <div class="dms-item">
              <div class="dms-val text-indigo">{{ dayEntries.length }}</div>
              <div class="dms-lbl">当日涉及题目</div>
            </div>
            <div class="dms-item">
              <div class="dms-val text-blue">{{ dayBaguCount }}</div>
              <div class="dms-lbl">八股考点</div>
            </div>
            <div class="dms-item">
              <div class="dms-val text-emerald">{{ dayAlgoCount }}</div>
              <div class="dms-lbl">手撕算法</div>
            </div>
            <div class="dms-item">
              <div class="dms-val text-amber">{{ dayMasteredRate }}%</div>
              <div class="dms-lbl">当日掌握率</div>
            </div>
          </div>

          <!-- Target Companies Covered Today -->
          <div class="dcb-companies-covered" v-if="dayCompanies.length">
            <span class="dcc-label">涉及企业：</span>
            <div class="dcc-list">
              <span v-for="c in dayCompanies" :key="c.id" class="dcc-badge">
                <CompanyLogo :logo="c.logo" :name="c.name" :size="20" :radius="4" />
                <span>{{ c.name }}</span>
              </span>
            </div>
          </div>
        </div>

        <!-- B. Daily Reflection Journal Pad -->
        <div class="daily-card-box">
          <div class="dcb-header">
            <div class="dcb-title-left">
              <span class="dcb-icon">📝</span>
              <span class="dcb-title">每日复盘心得与备战笔记</span>
            </div>
            <span class="dcb-autosave-tag">
              <CheckCircleFilled v-if="noteSaved" class="text-emerald" />
              <span>{{ noteSaved ? '已自动保存' : '正在输入...' }}</span>
            </span>
          </div>

          <!-- Quick Templates Bar -->
          <div class="quick-template-bar">
            <button class="qtb-btn" @click="insertTemplate('breakthrough')">💡 核心突破</button>
            <button class="qtb-btn" @click="insertTemplate('pitfall')">⚠️ 易错坑点</button>
            <button class="qtb-btn" @click="insertTemplate('plan')">🎯 明日目标</button>
          </div>

          <!-- Note Textarea -->
          <a-textarea
            v-model:value="currentDayNote"
            :rows="6"
            placeholder="写下今天的复盘心得、容易被面试官深挖的追问逻辑，或明日攻克计划..."
            class="daily-note-textarea"
            @input="handleNoteInput"
          />
        </div>
      </div>

      <!-- 4. Daily Entries List -->
      <div class="section-card-wrapper">
        <div class="section-title-row">
          <div class="str-title-group">
            <span class="str-icon-dot dot-indigo"></span>
            <h2 class="str-main-title">{{ formattedSelectedDate }} 收纳与复习的题目清单</h2>
            <span class="str-badge">{{ dayEntries.length }} 篇题目</span>
          </div>
        </div>

        <div v-if="!dayEntries.length" class="empty-day-box">
          <a-empty description="该日期暂无题目记录，点击下方去企业题库添加或选择其他日期" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          <a-button type="primary" style="margin-top: 12px;" @click="$router.push('/companies')">
            去题库录入题目
          </a-button>
        </div>

        <div v-else class="daily-entries-list">
          <div
            v-for="e in dayEntries"
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
              <!-- Quick Mastery Status Toggle -->
              <a-select
                :value="e.status"
                size="small"
                style="width: 96px"
                @click.stop
                @change="(v) => store.updateMasteryStatus(e.id, v)"
              >
                <a-select-option value="未掌握">未掌握</a-select-option>
                <a-select-option value="学习中">学习中</a-select-option>
                <a-select-option value="已掌握">已掌握</a-select-option>
              </a-select>
              <RightOutlined class="etr-arrow" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Empty, message } from 'ant-design-vue'
import {
  LeftOutlined,
  RightOutlined,
  CalendarOutlined,
  ThunderboltOutlined,
  CheckCircleFilled,
  StarFilled,
  StarOutlined,
  ReadOutlined,
  CodeOutlined,
} from '@ant-design/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import CompanyLogo from '../components/CompanyLogo.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const router = useRouter()
const store = useInterviewStore()

function getTodayStr() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const selectedDateStr = ref(getTodayStr())
const isToday = computed(() => selectedDateStr.value === getTodayStr())

const formattedSelectedDate = computed(() => {
  const [y, m, d] = selectedDateStr.value.split('-')
  return `${y}年${m}月${d}日`
})

const weekdayText = computed(() => {
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const d = new Date(selectedDateStr.value)
  return isNaN(d) ? '' : days[d.getDay()]
})

function shiftDate(offsetDays) {
  const d = new Date(selectedDateStr.value)
  d.setDate(d.getDate() + offsetDays)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  selectedDateStr.value = `${y}-${m}-${day}`
}

// ===== Heatmap Logic (Past 12 Weeks) =====
const heatmapWeeks = computed(() => {
  const weeks = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // Align end to current week's Sunday
  const currentDayOfWeek = today.getDay() || 7 // 1 (Mon) - 7 (Sun)
  const totalDays = 12 * 7

  const startDate = new Date(today)
  startDate.setDate(today.getDate() - totalDays + (7 - currentDayOfWeek) + 1)

  let cur = new Date(startDate)
  for (let w = 0; w < 12; w++) {
    const days = []
    for (let d = 0; d < 7; d++) {
      const y = cur.getFullYear()
      const m = String(cur.getMonth() + 1).padStart(2, '0')
      const dayStr = String(cur.getDate()).padStart(2, '0')
      const fullStr = `${y}-${m}-${dayStr}`

      const dayEntriesCount = store.entries.filter(e => {
        const created = e.createdAt?.slice(0, 10)
        const updated = e.updatedAt?.slice(0, 10)
        return created === fullStr || updated === fullStr
      }).length

      let level = 0
      if (dayEntriesCount >= 5) level = 4
      else if (dayEntriesCount >= 3) level = 3
      else if (dayEntriesCount >= 2) level = 2
      else if (dayEntriesCount >= 1) level = 1

      days.push({
        dateStr: fullStr,
        count: dayEntriesCount,
        level,
        isFuture: cur > today
      })

      cur.setDate(cur.getDate() + 1)
    }
    weeks.push({ weekIndex: w, days })
  }
  return weeks
})

const streakDays = computed(() => {
  let count = 0
  const cur = new Date()
  while (true) {
    const y = cur.getFullYear()
    const m = String(cur.getMonth() + 1).padStart(2, '0')
    const d = String(cur.getDate()).padStart(2, '0')
    const str = `${y}-${m}-${d}`

    const has = store.entries.some(e => e.createdAt?.slice(0, 10) === str || e.updatedAt?.slice(0, 10) === str)
    if (has) {
      count++
      cur.setDate(cur.getDate() - 1)
    } else {
      break
    }
  }
  return count || 1
})

const last30DaysCount = computed(() => {
  const d30 = new Date()
  d30.setDate(d30.getDate() - 30)
  const d30Str = d30.toISOString().slice(0, 10)
  return store.entries.filter(e => (e.createdAt?.slice(0, 10) || '') >= d30Str).length
})

// ===== Day Entries & Stats =====
const dayEntries = computed(() => {
  return store.getEntriesByDate(selectedDateStr.value)
})

const dayBaguCount = computed(() => dayEntries.value.filter(e => e.type === '八股文').length)
const dayAlgoCount = computed(() => dayEntries.value.filter(e => e.type === '算法题').length)

const dayMasteredRate = computed(() => {
  if (!dayEntries.value.length) return 100
  const mastered = dayEntries.value.filter(e => e.status === '已掌握').length
  return Math.round((mastered / dayEntries.value.length) * 100)
})

const dayCompanies = computed(() => {
  const ids = [...new Set(dayEntries.value.map(e => e.companyId))]
  return store.companies.filter(c => ids.includes(c.id))
})

function getCompanyName(companyId) {
  const c = store.companies.find(item => item.id === companyId)
  return c ? c.name : '未知企业'
}

function toggleStar(id) {
  store.toggleStar(id)
}

function startDayZenSprint() {
  if (!dayEntries.value.length) {
    message.warning('该日期暂无题目可供复习')
    return
  }
  router.push(`/zen-mode?date=${selectedDateStr.value}`)
}

// ===== Daily Note AutoSave =====
const currentDayNote = ref('')
const noteSaved = ref(true)
let noteTimer = null

watch(selectedDateStr, (newDate) => {
  currentDayNote.value = store.getDailyNote(newDate)
  noteSaved.value = true
}, { immediate: true })

function handleNoteInput() {
  noteSaved.value = false
  clearTimeout(noteTimer)
  noteTimer = setTimeout(() => {
    store.saveDailyNote(selectedDateStr.value, currentDayNote.value)
    noteSaved.value = true
  }, 600)
}

function insertTemplate(type) {
  let snippet = ''
  if (type === 'breakthrough') {
    snippet = '\n💡 **今日核心突破**：\n- 搞懂了高频考点核心原理，理清了底层运行机制...\n'
  } else if (type === 'pitfall') {
    snippet = '\n⚠️ **易错/卡壳点剖析**：\n- 面试官容易在边界条件和并发场景下挖坑...\n'
  } else if (type === 'plan') {
    snippet = '\n🎯 **明日攻克目标**：\n- 二刷手撕算法题，掌握动态规划状态转移方程...\n'
  }
  currentDayNote.value += snippet
  handleNoteInput()
}
</script>

<style scoped>
.daily-review-page-container {
  max-width: 1280px;
  margin: 0 auto;
}

/* Header */
.dr-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.dr-main-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.5px;
  margin-bottom: 4px;
}
.dr-sub-title {
  font-size: 13px;
  color: var(--text-2);
}

.dr-date-picker-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dr-nav-day-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid var(--border);
  color: var(--text-2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s;
}
.dr-nav-day-btn:hover:not(:disabled) {
  background: var(--bg-subtle);
  border-color: var(--accent);
  color: var(--accent);
}
.dr-nav-day-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.dr-current-date-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  height: 36px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid var(--border);
}
.dr-cal-icon { color: var(--accent); }
.dr-date-text { font-size: 13.5px; font-weight: 700; color: var(--text-1); }
.dr-weekday-tag { font-size: 11.5px; color: var(--text-3); }

.dr-today-btn {
  padding: 0 12px;
  height: 36px;
  border-radius: 8px;
  background: var(--accent-subtle);
  border: 1px solid var(--accent-border);
  color: var(--accent);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

/* Heatmap Card */
.heatmap-card-wrapper {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.hcw-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.hcw-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hcw-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
}
.hcw-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
}

.hcw-stats-pills {
  display: flex;
  gap: 12px;
}
.hcw-pill {
  font-size: 12px;
  background: var(--bg-subtle);
  padding: 3px 10px;
  border-radius: 6px;
  color: var(--text-2);
}
.hcw-pill strong {
  color: var(--text-1);
}

/* Heatmap Grid */
.heatmap-matrix-scroll {
  overflow-x: auto;
  padding-bottom: 6px;
}

.heatmap-matrix {
  display: flex;
  gap: 5px;
  margin-bottom: 12px;
}

.heatmap-column {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.heatmap-cell {
  width: 15px;
  height: 15px;
  border-radius: 3.5px;
  cursor: pointer;
  transition: all 0.18s;
  position: relative;
}

.cell-inner {
  width: 100%;
  height: 100%;
}

/* GitHub Greens */
.level-0 { background: #f1f5f9; }
.level-1 { background: #bbf7d0; }
.level-2 { background: #4ade80; }
.level-3 { background: #16a34a; }
.level-4 { background: #15803d; }

.heatmap-cell:hover:not(.cell-future) {
  transform: scale(1.25);
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.cell-selected {
  outline: 2px solid #4f46e5 !important;
  outline-offset: 1.5px;
  transform: scale(1.18);
}

.cell-future {
  opacity: 0.35;
  cursor: default;
}

.heatmap-legend-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}
.hlr-label { font-size: 11px; color: var(--text-3); margin: 0 4px; }
.legend-box {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

/* Dual Grid */
.daily-dual-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.daily-card-box {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
}

.dcb-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.dcb-title-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.dcb-icon { font-size: 18px; }
.dcb-title { font-size: 15px; font-weight: 700; color: var(--text-1); }

.day-sprint-btn {
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 100%);
  color: #ffffff;
  border: none;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.18s;
  box-shadow: 0 2px 6px rgba(124, 58, 237, 0.25);
}
.day-sprint-btn:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.dcb-autosave-tag {
  font-size: 11.5px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 4px;
}

.dcb-metrics-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 14px;
  background: var(--bg-subtle);
  border-radius: 12px;
  margin-bottom: 14px;
  text-align: center;
}

.dms-val {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 2px;
}
.dms-lbl { font-size: 11px; color: var(--text-3); }

.dcb-companies-covered {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
}
.dcc-label { font-size: 12px; color: var(--text-3); }
.dcc-list { display: flex; flex-wrap: wrap; gap: 6px; }
.dcc-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #ffffff;
  border: 1px solid var(--border);
  font-size: 11.5px;
  font-weight: 600;
  color: var(--text-1);
}

/* Journal Pad */
.quick-template-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.qtb-btn {
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 11.5px;
  padding: 3px 8px;
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.18s;
}
.qtb-btn:hover {
  background: #ffffff;
  border-color: var(--accent);
  color: var(--accent);
}

.daily-note-textarea {
  border-radius: 10px !important;
  font-size: 13px !important;
  line-height: 1.6 !important;
}

/* Daily Entries Section */
.section-card-wrapper {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
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
}
.dot-indigo { background: #4f46e5; }

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

.empty-day-box {
  padding: 32px 0;
  text-align: center;
}

.daily-entries-list {
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

.etr-arrow {
  font-size: 12px;
  color: var(--text-3);
}
</style>

<template>
  <a-modal
    v-model:open="modalOpen"
    title="🎲 随机模拟面试抽题（Mock Interview）"
    :footer="null"
    :width="880"
    :destroy-on-close="true"
    @cancel="emit('update:open', false)"
  >
    <!-- Phase 1: Setup & Draw Configuration -->
    <div v-if="phase === 'setup'" class="mock-setup-container">
      <div class="mock-banner-box">
        <div class="mbb-icon">⏱️</div>
        <div class="mbb-text">
          <div class="mbb-title">全真模拟面试考场</div>
          <div class="mbb-desc">随机抽取指定企业的经典八股理论与手撕算法，模拟真实面试答题节奏</div>
        </div>
      </div>

      <div class="mock-settings-grid">
        <!-- Target Company -->
        <div class="setting-item">
          <label class="setting-label">目标面试企业</label>
          <a-select v-model:value="targetCompanyId" size="large" style="width: 100%" placeholder="全库随机抽取">
            <a-select-option :value="null">🌐 全题库综合模拟面试</a-select-option>
            <a-select-option v-for="c in store.companies" :key="c.id" :value="c.id">
              {{ c.name }} ({{ getCompanyCount(c.id) }} 题在库)
            </a-select-option>
          </a-select>
        </div>

        <!-- Bagu Count -->
        <div class="setting-item">
          <label class="setting-label">八股理论考点题数</label>
          <a-radio-group v-model:value="baguNum" button-style="solid" size="large" style="width: 100%">
            <a-radio-button :value="2" style="width: 33.3%; text-align: center">2 题</a-radio-button>
            <a-radio-button :value="3" style="width: 33.3%; text-align: center">3 题 (标准)</a-radio-button>
            <a-radio-button :value="5" style="width: 33.3%; text-align: center">5 题 (冲刺)</a-radio-button>
          </a-radio-group>
        </div>

        <!-- Algo Count -->
        <div class="setting-item">
          <label class="setting-label">手撕算法真题数</label>
          <a-radio-group v-model:value="algoNum" button-style="solid" size="large" style="width: 100%">
            <a-radio-button :value="0" style="width: 33.3%; text-align: center">0 题 (仅八股)</a-radio-button>
            <a-radio-button :value="1" style="width: 33.3%; text-align: center">1 题 (标准)</a-radio-button>
            <a-radio-button :value="2" style="width: 33.3%; text-align: center">2 题 (进阶)</a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <div class="mock-setup-footer">
        <a-button size="large" @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" size="large" class="start-mock-btn" @click="startInterview">
          <ThunderboltOutlined /> 随机抽取并进入考场
        </a-button>
      </div>
    </div>

    <!-- Phase 2: Active Exam Arena -->
    <div v-else-if="phase === 'exam'" class="mock-exam-container">
      <!-- Exam Header Timer -->
      <div class="exam-header-bar">
        <div class="ehb-left">
          <span class="exam-tag">模拟面试中</span>
          <span class="exam-comp-name">{{ examCompanyName }}</span>
          <span class="exam-progress-num">第 {{ currentIndex + 1 }} / {{ examQuestions.length }} 题</span>
        </div>

        <div class="ehb-timer">
          <span>⏱️ 面试计时：</span>
          <strong class="timer-digits">{{ formattedTime }}</strong>
        </div>
      </div>

      <!-- Current Question Card -->
      <div class="exam-question-card" v-if="currentQuestion">
        <div class="eqc-header">
          <span class="eqc-type" :class="currentQuestion.type === '八股文' ? 'eqc-blue' : 'eqc-green'">
            {{ currentQuestion.type }}
          </span>
          <span class="diff-pill" :class="`diff-${currentQuestion.difficulty}`">{{ currentQuestion.difficulty }}</span>
          <div class="eqc-tags" v-if="currentQuestion.tags?.length">
            <span v-for="t in currentQuestion.tags" :key="t" class="eqc-tag">{{ t }}</span>
          </div>
        </div>

        <h2 class="eqc-title">{{ currentQuestion.title }}</h2>

        <!-- Answer / Scratchpad Input -->
        <div class="eqc-scratchpad-area">
          <label class="eqc-label">💡 个人答题草稿 / 核心思路笔记：</label>
          <a-textarea
            v-model:value="userAnswers[currentQuestion.id]"
            :rows="6"
            placeholder="在此简要写下你的回答思路、关键机制或伪代码，交卷后可对比官方参考解析..."
            class="eqc-textarea"
          />
        </div>
      </div>

      <!-- Exam Navigation & Submit Footer -->
      <div class="exam-footer-bar">
        <div class="efb-left-pills">
          <span
            v-for="(q, idx) in examQuestions"
            :key="q.id"
            class="q-pill-btn"
            :class="{
              'qp-active': currentIndex === idx,
              'qp-filled': !!userAnswers[q.id]
            }"
            @click="currentIndex = idx"
          >
            {{ idx + 1 }}
          </span>
        </div>

        <div class="efb-right-actions">
          <a-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</a-button>
          <a-button v-if="currentIndex < examQuestions.length - 1" type="primary" @click="currentIndex++">下一题</a-button>
          <a-button v-else type="primary" class="submit-exam-btn" @click="finishExam">交卷并查看解析</a-button>
        </div>
      </div>
    </div>

    <!-- Phase 3: Result & Analysis Review -->
    <div v-else class="mock-result-container">
      <div class="result-summary-card">
        <div class="rsc-left">
          <div class="rsc-badge">🎉 模拟面试完成</div>
          <h2 class="rsc-title">{{ examCompanyName }} 模拟测评报告</h2>
          <div class="rsc-meta">
            <span>总计用时：<strong>{{ formattedTime }}</strong></span>
            <span>完成题目：<strong>{{ examQuestions.length }}</strong> 篇</span>
          </div>
        </div>
        <div class="rsc-right">
          <a-button type="primary" @click="phase = 'setup'">再抽一套题</a-button>
        </div>
      </div>

      <!-- Questions Review Accordion -->
      <div class="result-questions-list">
        <div v-for="(q, idx) in examQuestions" :key="q.id" class="result-question-block">
          <div class="rqb-header">
            <span class="rqb-num">#{{ idx + 1 }}</span>
            <span class="rqb-type" :class="q.type === '八股文' ? 'rqb-blue' : 'rqb-green'">{{ q.type }}</span>
            <span class="rqb-title">{{ q.title }}</span>
          </div>

          <!-- User Scratchpad note -->
          <div class="rqb-user-answer" v-if="userAnswers[q.id]">
            <div class="rua-title">📝 你的现场答题草稿：</div>
            <div class="rua-text">{{ userAnswers[q.id] }}</div>
          </div>

          <!-- Official Analysis -->
          <div class="rqb-analysis-wrap">
            <div class="raw-title">📖 官方参考解析与代码：</div>
            <div class="drawer-markdown-render" v-html="renderMd(q.content)"></div>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined } from '@ant-design/icons-vue'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import cpp from 'highlight.js/lib/languages/cpp'
import python from 'highlight.js/lib/languages/python'
import 'highlight.js/styles/atom-one-dark.css'
import { useInterviewStore } from '../stores/useInterviewStore'

hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('python', python)

const props = defineProps({ open: Boolean })
const emit = defineEmits(['update:open'])
const store = useInterviewStore()

const modalOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

const phase = ref('setup') // 'setup' | 'exam' | 'result'
const targetCompanyId = ref(null)
const baguNum = ref(3)
const algoNum = ref(1)

const examQuestions = ref([])
const currentIndex = ref(0)
const userAnswers = ref({})

// Timer
const elapsedSeconds = ref(0)
let timerInterval = null

const formattedTime = computed(() => {
  const m = Math.floor(elapsedSeconds.value / 60).toString().padStart(2, '0')
  const s = (elapsedSeconds.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

const currentQuestion = computed(() => examQuestions.value[currentIndex.value])

const examCompanyName = computed(() => {
  if (!targetCompanyId.value) return '全题库综合'
  const c = store.companies.find(item => item.id === targetCompanyId.value)
  return c ? c.name : '综合面试'
})

function getCompanyCount(cid) {
  return store.entries.filter(e => e.companyId === cid).length
}

watch(() => props.open, (v) => {
  if (v) {
    phase.value = 'setup'
    userAnswers.value = {}
    currentIndex.value = 0
    clearInterval(timerInterval)
  }
})

onUnmounted(() => {
  clearInterval(timerInterval)
})

function startInterview() {
  const drawn = store.drawMockInterview(targetCompanyId.value, baguNum.value, algoNum.value)
  if (!drawn.length) {
    message.warning('当前所选企业在库题目不足，请先录入题目或选择【全题库综合】')
    return
  }

  examQuestions.value = drawn
  currentIndex.value = 0
  userAnswers.value = {}
  elapsedSeconds.value = 0
  phase.value = 'exam'

  timerInterval = setInterval(() => {
    elapsedSeconds.value++
  }, 1000)

  message.success(`已成功抽取 ${drawn.length} 道面试题，祝你面试顺利！🎉`)
}

function finishExam() {
  clearInterval(timerInterval)
  phase.value = 'result'
  message.success('模拟面试已完成！请查看解析复盘')
}

function renderMd(content) {
  if (!content) return '<p style="color:var(--text-3);">暂无解析内容</p>'
  return content
    .replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
      let hl = ''
      try {
        hl = hljs.highlight(code, { language: lang || 'java' }).value
      } catch {
        hl = hljs.highlightAuto(code).value
      }
      return `<pre class="render-code-box"><code class="hljs">${hl}</code></pre>`
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
/* Phase 1: Setup */
.mock-setup-container {
  padding-top: 6px;
}

.mock-banner-box {
  background: linear-gradient(135deg, #eef2ff 0%, #ffffff 100%);
  border: 1px solid var(--accent-border);
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}
.mbb-icon { font-size: 32px; }
.mbb-title { font-size: 16px; font-weight: 700; color: var(--text-1); margin-bottom: 2px; }
.mbb-desc { font-size: 12px; color: var(--text-3); }

.mock-settings-grid {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 24px;
}

.setting-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  margin-bottom: 8px;
}

.mock-setup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.start-mock-btn {
  font-weight: 600 !important;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%) !important;
}

/* Phase 2: Exam Arena */
.mock-exam-container {}

.exam-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #0f172a;
  border-radius: 12px;
  color: #ffffff;
  margin-bottom: 16px;
}

.ehb-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.exam-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #e11d48;
  color: #ffffff;
  font-weight: 600;
}
.exam-comp-name { font-weight: 700; font-size: 14px; }
.exam-progress-num { font-size: 12px; color: #94a3b8; }

.ehb-timer {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.timer-digits {
  font-size: 16px;
  font-family: 'JetBrains Mono', monospace;
  color: #38bdf8;
}

.exam-question-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 16px;
}

.eqc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.eqc-type {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}
.eqc-blue { background: #eff6ff; color: #2563eb; }
.eqc-green { background: #ecfdf5; color: #059669; }

.eqc-tags { display: flex; gap: 4px; }
.eqc-tag { font-size: 11px; padding: 1px 6px; border-radius: 4px; background: var(--bg-subtle); color: var(--text-3); }

.eqc-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 16px;
  line-height: 1.4;
}

.eqc-scratchpad-area {
  margin-top: 16px;
}
.eqc-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
  margin-bottom: 6px;
  display: block;
}
.eqc-textarea {
  border-radius: 10px !important;
  font-size: 13px !important;
}

.exam-footer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid var(--border-light);
}

.efb-left-pills {
  display: flex;
  gap: 6px;
}
.q-pill-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s;
}
.qp-active {
  background: var(--accent) !important;
  color: #ffffff !important;
  border-color: var(--accent) !important;
}
.qp-filled {
  border-color: #059669;
  color: #059669;
}

.efb-right-actions {
  display: flex;
  gap: 10px;
}
.submit-exam-btn {
  background: #059669 !important;
  font-weight: 600 !important;
}

/* Phase 3: Result */
.mock-result-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-summary-card {
  background: linear-gradient(135deg, #ecfdf5 0%, #ffffff 100%);
  border: 1px solid #a7f3d0;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rsc-badge { font-size: 12px; font-weight: 700; color: #059669; margin-bottom: 2px; }
.rsc-title { font-size: 20px; font-weight: 800; color: var(--text-1); margin-bottom: 6px; }
.rsc-meta { font-size: 13px; color: var(--text-2); display: flex; gap: 16px; }

.result-questions-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 480px;
  overflow-y: auto;
}

.result-question-block {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 18px 20px;
}

.rqb-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.rqb-num { font-size: 14px; font-weight: 800; color: var(--accent); }
.rqb-type { font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600; }
.rqb-blue { background: #eff6ff; color: #2563eb; }
.rqb-green { background: #ecfdf5; color: #059669; }
.rqb-title { font-size: 15px; font-weight: 700; color: var(--text-1); }

.rqb-user-answer {
  background: var(--bg-subtle);
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 12px;
}
.rua-title { font-size: 11.5px; font-weight: 700; color: var(--text-2); margin-bottom: 4px; }
.rua-text { font-size: 13px; color: var(--text-1); white-space: pre-wrap; }

.rqb-analysis-wrap {
  border-top: 1px solid var(--border-light);
  padding-top: 12px;
}
.raw-title { font-size: 12px; font-weight: 700; color: var(--accent); margin-bottom: 8px; }
</style>

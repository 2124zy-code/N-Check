<template>
  <AppLayout>
    <div class="search-page-container">
      <!-- Search Header & Input Area -->
      <div class="search-header-box">
        <h1 class="search-main-title">全库智能检索</h1>
        <p class="search-sub-desc">支持跨公司搜索题目标题、详细解答、算法思路及分类标签</p>

        <!-- Custom Clean Search Input Box (No Overlapping) -->
        <div class="search-input-wrapper">
          <div class="search-bar">
            <SearchOutlined class="search-lead-icon" />
            <input
              v-model="keyword"
              type="text"
              class="search-native-input"
              placeholder="输入知识点关键词（如：MySQL、红黑树、HashMap、动态规划...）"
              @keyup.enter="doSearch"
            />
            <span v-if="keyword" class="clear-btn" @click="keyword = ''; doSearch()">
              <CloseCircleFilled />
            </span>
            <button class="search-action-btn" @click="doSearch">
              <span>搜 索</span>
            </button>
          </div>
        </div>

        <!-- Popular Tags Pills -->
        <div class="hot-tags-row" v-if="popularTags.length">
          <span class="hot-tags-label">热门标签：</span>
          <div class="hot-tags-list">
            <span
              v-for="t in popularTags"
              :key="t"
              class="hot-tag-pill"
              :class="{ 'tag-selected': keyword === t }"
              @click="keyword = t; doSearch()"
            >
              {{ t }}
            </span>
          </div>
        </div>
      </div>

      <!-- Result Section -->
      <div class="search-results-section">
        <!-- Results Header -->
        <div v-if="searched" class="results-status-bar">
          <span class="status-text">
            搜索结果：共找到 <strong class="highlight-count">{{ results.length }}</strong> 条相关题目
          </span>
          <span v-if="results.length" class="clear-filter" @click="keyword = ''; doSearch()">清空检索</span>
        </div>

        <!-- No Results -->
        <div v-if="searched && !results.length" class="empty-state-box">
          <a-empty description="未检索到匹配题目，请尝试更换关键词或标签" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        </div>

        <!-- Result List Cards -->
        <div v-if="results.length" class="result-cards-list">
          <div
            v-for="e in results"
            :key="e.id"
            class="result-item-card"
            @click="$router.push(`/companies/${e.companyId}`)"
          >
            <div class="ric-header">
              <!-- Type Badge -->
              <span class="type-pill" :class="e.type === '八股文' ? 'pill-blue' : 'pill-green'">
                <ReadOutlined v-if="e.type === '八股文'" />
                <CodeOutlined v-else />
                {{ e.type }}
              </span>

              <!-- Company Tag -->
              <span class="ric-company">
                <BankOutlined style="margin-right: 4px; color: var(--text-3);" />
                {{ companyName(e.companyId) }}
              </span>

              <!-- Meta Pills -->
              <div class="ric-meta-pills">
                <span class="diff-badge" :class="`diff-${e.difficulty}`">{{ e.difficulty }}</span>
                <span class="status-badge" :class="`status-${e.status}`">{{ e.status }}</span>
                <span v-for="t in (e.tags || []).slice(0, 3)" :key="t" class="tag-badge">{{ t }}</span>
              </div>
            </div>

            <!-- Title -->
            <div class="ric-title">{{ e.title }}</div>

            <!-- Footer info -->
            <div class="ric-footer">
              <span class="ric-date">录入时间：{{ e.createdAt?.slice(0, 10) }}</span>
              <span class="ric-view-link">
                查看公司题库详情 <ArrowRightOutlined />
              </span>
            </div>
          </div>
        </div>

        <!-- Initial Placeholder State -->
        <div v-if="!searched" class="initial-guide-box">
          <div class="guide-icon-wrap">
            <SearchOutlined />
          </div>
          <h3 class="guide-title">输入关键词开启极速检索</h3>
          <p class="guide-desc">支持搜索题目名称、八股解析要点、代码实现及标签体系</p>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Empty } from 'ant-design-vue'
import {
  SearchOutlined,
  CloseCircleFilled,
  ReadOutlined,
  CodeOutlined,
  BankOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const store = useInterviewStore()
const keyword = ref('')
const results = ref([])
const searched = ref(false)

const popularTags = computed(() => {
  const freq = {}
  store.entries.forEach(e => (e.tags || []).forEach(t => { freq[t] = (freq[t] || 0) + 1 }))
  return Object.entries(freq).sort((a, b) => b[1] - a[1]).slice(0, 14).map(([t]) => t)
})

function doSearch() {
  if (!keyword.value.trim()) {
    results.value = []
    searched.value = false
    return
  }
  results.value = store.searchEntries(keyword.value)
  searched.value = true
}

function companyName(id) {
  return store.getCompany(id)?.name || '未知企业'
}
</script>

<style scoped>
.search-page-container {
  max-width: 960px;
  margin: 0 auto;
}

/* ===== Search Header Area ===== */
.search-header-box {
  text-align: center;
  margin-bottom: 32px;
  padding: 10px 0;
}

.search-main-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.search-sub-desc {
  font-size: 14px;
  color: var(--text-2);
  margin-bottom: 24px;
}

/* ===== Search Input Box (Custom Native without wrapper overlap) ===== */
.search-input-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.search-bar {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 720px;
  height: 52px;
  background: #ffffff;
  border: 1.5px solid var(--border);
  border-radius: 14px;
  padding: 4px 6px 4px 18px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.search-bar:focus-within {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.12), 0 6px 20px rgba(0, 0, 0, 0.06);
}

.search-lead-icon {
  font-size: 18px;
  color: var(--text-3);
  margin-right: 12px;
  flex-shrink: 0;
}

.search-native-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: var(--text-1);
}
.search-native-input::placeholder {
  color: var(--text-3);
}

.clear-btn {
  color: var(--text-3);
  font-size: 16px;
  cursor: pointer;
  margin-right: 10px;
  display: flex;
  align-items: center;
  transition: color 0.18s;
}
.clear-btn:hover { color: var(--text-2); }

.search-action-btn {
  height: 42px;
  padding: 0 24px;
  border-radius: 10px;
  border: none;
  background: var(--accent);
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.search-action-btn:hover {
  background: var(--accent-hover);
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.3);
}

/* Hot Tags */
.hot-tags-row {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 720px;
  margin: 0 auto;
}

.hot-tags-label {
  font-size: 12px;
  color: var(--text-3);
  font-weight: 500;
}

.hot-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.hot-tag-pill {
  font-size: 12px;
  color: var(--text-2);
  background: #ffffff;
  border: 1px solid var(--border);
  padding: 3px 10px;
  border-radius: 100px;
  cursor: pointer;
  transition: all 0.18s ease;
}
.hot-tag-pill:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-subtle);
}
.tag-selected {
  background: var(--accent) !important;
  color: #ffffff !important;
  border-color: var(--accent) !important;
}

/* ===== Results Section ===== */
.results-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 0 4px;
}

.status-text {
  font-size: 14px;
  color: var(--text-2);
}
.highlight-count {
  color: var(--accent);
  font-weight: 700;
}
.clear-filter {
  font-size: 13px;
  color: var(--text-3);
  cursor: pointer;
}
.clear-filter:hover { color: var(--accent); }

.result-cards-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 18px 22px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.result-item-card:hover {
  border-color: var(--accent-border);
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.ric-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.type-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 9px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.pill-blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.pill-green { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }

.ric-company {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
}

.ric-meta-pills {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
}

.diff-badge {
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 4px;
  font-weight: 600;
}
.diff-简单 { background: #ecfdf5; color: #059669; }
.diff-中等 { background: #fffbeb; color: #d97706; }
.diff-困难 { background: #fff1f2; color: #e11d48; }

.status-badge {
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 4px;
  background: var(--bg-subtle);
  color: var(--text-2);
}
.status-已掌握 { background: #ecfdf5; color: #059669; font-weight: 600; }

.tag-badge {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--bg-subtle);
  color: var(--text-3);
}

.ric-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 10px;
  line-height: 1.4;
}

.ric-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
  font-size: 12px;
}
.ric-date { color: var(--text-3); }
.ric-view-link {
  color: var(--accent);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* Empty & Initial State */
.empty-state-box {
  padding: 60px 0;
  text-align: center;
}

.initial-guide-box {
  padding: 60px 0;
  text-align: center;
}
.guide-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: var(--bg-subtle);
  color: var(--text-3);
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.guide-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 6px;
}
.guide-desc {
  font-size: 13px;
  color: var(--text-3);
}
</style>
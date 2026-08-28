<template>
  <AppLayout>
    <div class="companies-page-container">
      <!-- Page Header -->
      <div class="companies-header-row">
        <div>
          <h1 class="page-main-title">目标企业与题库归档</h1>
          <p class="page-sub-title">按企业归纳历年高频技术面经、八股考点及手撕算法</p>
        </div>
        <a-button type="primary" size="large" class="add-company-btn" @click="openAdd">
          <template #icon><PlusOutlined /></template>
          添加目标企业
        </a-button>
      </div>

      <!-- Filter Bar -->
      <div class="filter-card-bar">
        <div class="fc-left">
          <a-input
            v-model:value="keyword"
            placeholder="搜索公司名称..."
            style="width: 260px"
            allow-clear
          >
            <template #prefix>
              <SearchOutlined style="color: var(--text-3);" />
            </template>
          </a-input>

          <a-select
            v-model:value="industry"
            placeholder="所属行业筛选"
            style="width: 150px"
            allow-clear
          >
            <a-select-option v-for="i in industries" :key="i" :value="i">{{ i }}</a-select-option>
          </a-select>
        </div>

        <div class="fc-right">
          <span class="fc-count-pill">共收录 <strong>{{ filtered.length }}</strong> 家目标企业</span>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!filtered.length" class="empty-companies-box">
        <a-empty
          :description="!store.companies.length ? '暂无企业，快去添加你的第一个目标企业吧' : '未找到符合条件的企业'"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
        >
          <a-button v-if="!store.companies.length" type="primary" @click="openAdd">立即添加企业</a-button>
        </a-empty>
      </div>

      <!-- Company Grid -->
      <div v-else class="companies-grid">
        <div
          v-for="c in filtered"
          :key="c.id"
          class="company-grid-card"
          @click="$router.push(`/companies/${c.id}`)"
        >
          <!-- Card Top -->
          <div class="cgc-top">
            <CompanyLogo :logo="c.logo" :name="c.name" :size="48" :radius="12" />
            
            <div class="cgc-actions" @click.stop>
              <a-dropdown :trigger="['click']">
                <button class="cgc-more-btn" title="更多操作">
                  <EllipsisOutlined />
                </button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="openEdit(c)">
                      <EditOutlined /> 编辑企业信息
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item danger>
                      <a-popconfirm
                        title="确认删除该公司及其所有面经？"
                        ok-text="确认删除"
                        cancel-text="取消"
                        ok-type="danger"
                        @confirm="handleDelete(c.id)"
                      >
                        <DeleteOutlined /> 删除企业
                      </a-popconfirm>
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </div>

          <!-- Name & Tag -->
          <div class="cgc-info">
            <div class="cgc-name">{{ c.name }}</div>
            <span class="cgc-industry-tag">{{ c.industry || '互联网' }}</span>
          </div>

          <!-- 3 Columns Stats -->
          <div class="cgc-stats-grid">
            <div class="cgc-stat-col">
              <span class="cs-number text-blue">{{ entryCount(c.id, '八股文') }}</span>
              <span class="cs-text">八股考点</span>
            </div>
            <div class="cgc-col-divider"></div>
            <div class="cgc-stat-col">
              <span class="cs-number text-green">{{ entryCount(c.id, '算法题') }}</span>
              <span class="cs-text">算法题解</span>
            </div>
            <div class="cgc-col-divider"></div>
            <div class="cgc-stat-col">
              <span class="cs-number text-purple">{{ entryCount(c.id) }}</span>
              <span class="cs-text">累计题量</span>
            </div>
          </div>

          <!-- Card Footer -->
          <div class="cgc-footer">
            <span class="cgc-date">建档时间：{{ c.createdAt?.slice(0, 10) }}</span>
            <span class="cgc-enter-link">
              进入题库 <RightOutlined />
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <CompanyModal
      v-model:open="modalOpen"
      :edit-data="editTarget"
      @saved="modalOpen = false"
    />
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Empty, message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EllipsisOutlined,
  SearchOutlined,
  RightOutlined,
} from '@ant-design/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import CompanyModal from '../components/CompanyModal.vue'
import CompanyLogo from '../components/CompanyLogo.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const store = useInterviewStore()
const keyword = ref('')
const industry = ref(null)
const modalOpen = ref(false)
const editTarget = ref(null)

const industries = ['互联网', '智能硬件', '电子商务', '金融科技', '游戏娱乐', '人工智能', '外企软件', '生活服务', '其他']

const filtered = computed(() => {
  let list = store.companies
  if (keyword.value) list = list.filter(c => c.name?.toLowerCase().includes(keyword.value.toLowerCase()))
  if (industry.value) list = list.filter(c => c.industry === industry.value)
  return list
})

function entryCount(id, type) {
  return store.entries.filter(e => e.companyId === id && (!type || e.type === type)).length
}

function openAdd() {
  editTarget.value = null
  modalOpen.value = true
}

function openEdit(c) {
  editTarget.value = c
  modalOpen.value = true
}

function handleDelete(id) {
  store.deleteCompany(id)
  message.success('目标企业及关联题目已删除')
}
</script>

<style scoped>
.companies-page-container {
  max-width: 1280px;
  margin: 0 auto;
}

/* Header */
.companies-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-main-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
  letter-spacing: -0.5px;
  margin-bottom: 4px;
}
.page-sub-title {
  font-size: 13px;
  color: var(--text-2);
}

.add-company-btn {
  height: 42px !important;
  padding: 0 20px !important;
  font-size: 14px !important;
}

/* Filter Bar */
.filter-card-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.fc-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fc-count-pill {
  font-size: 13px;
  color: var(--text-2);
  background: var(--bg-subtle);
  padding: 4px 12px;
  border-radius: 100px;
  border: 1px solid var(--border);
}
.fc-count-pill strong {
  color: var(--accent);
}

/* Grid */
.companies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 20px;
}

.company-grid-card {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.22s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
}

.company-grid-card:hover {
  border-color: var(--accent-border);
  box-shadow: var(--shadow-hover);
  transform: translateY(-3px);
}

.cgc-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.cgc-more-btn {
  background: transparent;
  border: none;
  color: var(--text-3);
  font-size: 18px;
  padding: 4px 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.18s;
}
.cgc-more-btn:hover {
  background: var(--bg-subtle);
  color: var(--text-1);
}

.cgc-info {
  margin-bottom: 16px;
}
.cgc-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 4px;
}
.cgc-industry-tag {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 4px;
  background: var(--purple-subtle);
  color: var(--purple);
  border: 1px solid var(--purple-border);
  font-weight: 500;
}

/* 3 Cols Stats */
.cgc-stats-grid {
  display: flex;
  align-items: center;
  background: var(--bg-subtle);
  border-radius: 10px;
  padding: 10px 0;
  margin-bottom: 16px;
}

.cgc-stat-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cgc-col-divider {
  width: 1px;
  height: 24px;
  background: var(--border);
}

.cs-number {
  font-size: 18px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 2px;
}
.text-blue { color: #2563eb; }
.text-green { color: #059669; }
.text-purple { color: #7c3aed; }

.cs-text {
  font-size: 11px;
  color: var(--text-3);
}

/* Card Footer */
.cgc-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
  font-size: 12px;
  color: var(--text-3);
  margin-top: auto;
}

.cgc-enter-link {
  color: var(--accent);
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  transition: transform 0.18s;
}
.company-grid-card:hover .cgc-enter-link {
  transform: translateX(3px);
}

.empty-companies-box {
  padding: 80px 0;
  text-align: center;
}
</style>
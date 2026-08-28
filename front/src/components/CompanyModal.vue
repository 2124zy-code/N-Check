<template>
  <a-modal
    v-model:open="modalOpen"
    :title="editData ? '编辑目标企业' : '添加目标企业'"
    :footer="null"
    :width="580"
    :destroy-on-close="true"
    @cancel="emit('update:open', false)"
  >
    <a-form :model="form" layout="vertical" @finish="handleSubmit" class="company-modal-form">
      <!-- 1. 知名大厂快捷选取看板 -->
      <a-form-item label="知名大厂一键填入（点击直接载入官方 Logo 与赛道）">
        <div class="famous-companies-selector">
          <div
            v-for="fc in FAMOUS_COMPANIES"
            :key="fc.id"
            class="famous-item-chip"
            :class="{ 'chip-selected': form.name === fc.name }"
            @click="pickFamous(fc)"
            :title="`一键填入 ${fc.name}`"
          >
            <div class="fic-logo" v-html="fc.svg"></div>
            <span class="fic-name">{{ fc.name }}</span>
          </div>
        </div>
      </a-form-item>

      <!-- 2. 企业名称输入与实时 Logo 徽标预览 -->
      <a-form-item
        label="企业全称 / 简称"
        name="name"
        :rules="[{ required: true, message: '请输入企业名称' }]"
      >
        <div class="name-input-with-preview">
          <!-- 实时动态图标预览：知名大厂显示官方SVG，其他企业自动生成名称文字徽章 -->
          <div class="live-avatar-box">
            <CompanyLogo :logo="form.logo" :name="form.name" :size="40" :radius="10" />
          </div>

          <a-input
            v-model:value="form.name"
            placeholder="输入企业名称，如：商汤科技、蔚来、小红书、微软..."
            size="large"
            allow-clear
            class="name-input-field"
            @input="handleNameInput"
          />
        </div>
        <div class="name-hint-tip">
          💡 知名大厂将自动匹配官方矢量 Logo，其他企业将自动生成专属名称品牌徽标。
        </div>
      </a-form-item>

      <!-- 3. 所属行业分类 -->
      <a-form-item label="所属行业 / 赛道分类" name="industry">
        <a-select v-model:value="form.industry" placeholder="选择行业类别" size="large">
          <a-select-option v-for="i in industries" :key="i" :value="i">{{ i }}</a-select-option>
        </a-select>
      </a-form-item>

      <!-- 4. Footer Actions -->
      <div class="modal-bottom-actions">
        <a-button size="large" @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" size="large" html-type="submit" class="submit-btn">
          {{ editData ? '保存企业信息' : '确认添加企业' }}
        </a-button>
      </div>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import CompanyLogo from './CompanyLogo.vue'
import { FAMOUS_COMPANIES } from '../utils/companyLogos'
import { useInterviewStore } from '../stores/useInterviewStore'

const props = defineProps({
  open: Boolean,
  editData: { type: Object, default: null },
})
const emit = defineEmits(['update:open', 'saved'])
const store = useInterviewStore()

const modalOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

const industries = ['互联网', '智能硬件', '电子商务', '金融科技', '游戏娱乐', '人工智能', '外企软件', '生活服务', '其他']

const defaultForm = () => ({ name: '', industry: '互联网', logo: '' })
const form = ref(defaultForm())

watch(() => props.open, (v) => {
  if (v) form.value = props.editData ? { ...props.editData } : defaultForm()
})

function pickFamous(fc) {
  form.value.name = fc.name
  form.value.industry = fc.industry
  form.value.logo = fc.svg
  message.success(`已一键配置 ${fc.name} 专属官方矢量 Logo`)
}

function handleNameInput() {
  const query = (form.value.name || '').trim()
  const found = FAMOUS_COMPANIES.find(c => c.name === query)
  if (found) {
    form.value.logo = found.svg
    form.value.industry = found.industry
  } else {
    // 非知名大厂：logo 清空，CompanyLogo 会自动根据名称生成文字徽标
    form.value.logo = ''
  }
}

function handleSubmit() {
  if (props.editData) {
    store.updateCompany(props.editData.id, form.value)
    message.success('企业信息已成功更新 ✅')
  } else {
    store.addCompany(form.value)
    message.success('目标企业已成功建档 🎉')
  }
  emit('saved')
  emit('update:open', false)
}
</script>

<style scoped>
.company-modal-form {
  padding-top: 4px;
}

/* Famous Companies Selector */
.famous-companies-selector {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  max-height: 180px;
  overflow-y: auto;
  padding: 8px 10px;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 12px;
}

.famous-item-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid var(--border);
  cursor: pointer;
  transition: all 0.18s ease;
  user-select: none;
}

.famous-item-chip:hover {
  border-color: var(--accent);
  background: var(--accent-subtle);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.chip-selected {
  background: var(--accent-subtle) !important;
  border-color: var(--accent) !important;
  box-shadow: 0 0 0 1px var(--accent);
}

.fic-logo {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
:deep(.fic-logo svg) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.fic-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Name Input with Live Avatar */
.name-input-with-preview {
  display: flex;
  align-items: center;
  gap: 10px;
}

.live-avatar-box {
  flex-shrink: 0;
}

.name-input-field {
  flex: 1;
}

.name-hint-tip {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 6px;
}

.modal-bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid var(--border-light);
}

.submit-btn {
  min-width: 120px;
}
</style>

<template>
  <div
    class="company-logo-avatar"
    :style="avatarStyle"
    :title="name || '企业Logo'"
  >
    <!-- 1. 知名大厂官方 SVG 矢量图标 -->
    <div v-if="isSvg" class="svg-inner-wrap" v-html="svgContent"></div>

    <!-- 2. 其他企业：自动使用其企业名称文字生成专属徽标 -->
    <span v-else class="text-logo-wrap">
      {{ displayName }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { FAMOUS_COMPANIES } from '../utils/companyLogos'

const props = defineProps({
  logo: { type: String, default: '' },
  name: { type: String, default: '' },
  size: { type: Number, default: 44 },
  radius: { type: Number, default: 12 },
})

// 匹配知名大厂
const matchedFamous = computed(() => {
  if (props.logo && props.logo.startsWith('<svg')) {
    return { svg: props.logo }
  }
  const query = (props.name || props.logo || '').trim()
  if (!query) return null
  return FAMOUS_COMPANIES.find(c => c.name === query || c.id === query || query.includes(c.name))
})

const isSvg = computed(() => !!matchedFamous.value?.svg)
const svgContent = computed(() => matchedFamous.value?.svg || '')

// 其他企业的名称文字提取（1~2个字）
const displayName = computed(() => {
  const n = (props.name || props.logo || '企').trim()
  if (n.length <= 2) return n
  // 英文单词取前2个字母大写
  if (/^[a-zA-Z]+$/.test(n)) return n.slice(0, 2).toUpperCase()
  // 中文取前两个汉字
  return n.slice(0, 2)
})

// 根据公司名字计算固定优雅的背景渐变色
const bgGradients = [
  'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)', // Indigo-Purple
  'linear-gradient(135deg, #059669 0%, #10b981 100%)', // Emerald
  'linear-gradient(135deg, #0284c7 0%, #38bdf8 100%)', // Sky
  'linear-gradient(135deg, #d97706 0%, #f59e0b 100%)', // Amber
  'linear-gradient(135deg, #e11d48 0%, #f43f5e 100%)', // Rose
  'linear-gradient(135deg, #0d9488 0%, #14b8a6 100%)', // Teal
  'linear-gradient(135deg, #7c3aed 0%, #c084fc 100%)', // Violet
  'linear-gradient(135deg, #ea580c 0%, #fb923c 100%)', // Orange
]

const avatarStyle = computed(() => {
  const sz = props.size
  const rad = props.radius

  if (isSvg.value) {
    return {
      width: `${sz}px`,
      height: `${sz}px`,
      borderRadius: `${rad}px`,
      background: '#ffffff',
      border: '1px solid var(--border, #e2e8f0)',
    }
  }

  // 非大厂名称徽标：使用名字哈希计算渐变背景
  let hash = 0
  const str = props.name || '企'
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  const grad = bgGradients[Math.abs(hash) % bgGradients.length]

  return {
    width: `${sz}px`,
    height: `${sz}px`,
    borderRadius: `${rad}px`,
    background: grad,
    fontSize: displayName.value.length >= 2 ? `${Math.round(sz * 0.36)}px` : `${Math.round(sz * 0.44)}px`,
    fontWeight: '700',
    color: '#ffffff',
    border: 'none',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
  }
})
</script>

<style scoped>
.company-logo-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  box-sizing: border-box;
  user-select: none;
}

.svg-inner-wrap {
  width: 76%;
  height: 76%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.svg-inner-wrap svg) {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.text-logo-wrap {
  line-height: 1;
  letter-spacing: 0.5px;
  text-align: center;
}
</style>

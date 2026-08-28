import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request, { getToken, setToken, removeToken } from '../utils/request'

const STORAGE_KEY_AUTH_USER = 'ncheck_authenticated_user'

export const useInterviewStore = defineStore('interview', () => {
  // 1. Auth & User State (未登录时默认 null，严格跳转登录页)
  const token = ref(getToken())
  const currentUser = ref(loadInitialUser())

  function loadInitialUser() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_AUTH_USER)
      return (token.value && saved) ? JSON.parse(saved) : null
    } catch (e) {
      return null
    }
  }

  const isLoggedIn = computed(() => !!currentUser.value && !!token.value)

  // 2. Business Reactive State
  const companies = ref([])
  const entries = ref([])
  const dailyNotes = ref({})
  const radarSkillStats = ref([])
  const companyDistributionStats = ref([])
  const loading = ref(false)

  // Load all active user data from Spring Boot Backend
  async function loadActiveUserData() {
    if (!isLoggedIn.value) {
      companies.value = []
      entries.value = []
      dailyNotes.value = {}
      radarSkillStats.value = []
      companyDistributionStats.value = []
      return
    }

    loading.value = true
    try {
      // 1. 拉取当前用户企业列表
      const compList = await request.get('/companies')
      companies.value = (compList || []).map(normalizeCompany)

      // 2. 拉取题目列表 (默认拉取 500 条)
      const pageData = await request.get('/entries', { pageNum: 1, pageSize: 500 })
      entries.value = ((pageData && pageData.records) || []).map(normalizeEntry)

      // 3. 拉取统计大盘数据 (六维雷达图与企业分布)
      await fetchDashboardStats()
    } catch (e) {
      console.error('Failed to load active user data:', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchDashboardStats() {
    try {
      const stats = await request.get('/stats/dashboard')
      if (stats) {
        if (stats.radarStats) {
          radarSkillStats.value = stats.radarStats
        }
        if (stats.companyDistribution) {
          companyDistributionStats.value = stats.companyDistribution
        }
      }
    } catch (e) {
      console.warn('Failed to fetch dashboard stats:', e)
    }
  }

  // ===== Entity Adapters / Normalizers =====
  function normalizeUser(user) {
    if (!user) return null
    return {
      id: user.id,
      username: user.username,
      name: user.nickname || user.username,
      color: user.avatarColor || '#4f46e5',
      role: user.role || 'USER',
      createTime: user.createTime,
    }
  }

  function normalizeCompany(c) {
    if (!c) return null
    return {
      id: String(c.id),
      name: c.name,
      logo: c.logo || 'default',
      industry: c.industry || '互联网',
      baguCount: c.baguCount || 0,
      algoCount: c.algoCount || 0,
      masteredCount: c.masteredCount || 0,
      totalCount: c.totalCount || 0,
      createdAt: c.createTime,
    }
  }

  function normalizeEntry(e) {
    if (!e) return null
    return {
      id: String(e.id),
      companyId: String(e.companyId),
      companyName: e.companyName,
      companyLogo: e.companyLogo,
      type: e.type,
      title: e.title,
      difficulty: e.difficulty || '中等',
      status: e.status || '未掌握',
      isStarred: e.isStarred === 1 || e.isStarred === true,
      tags: Array.isArray(e.tags) ? e.tags : [],
      content: e.content || '',
      createdAt: e.createTime,
      updatedAt: e.updateTime,
    }
  }

  // Initial load if already logged in
  if (isLoggedIn.value) {
    loadActiveUserData()
  }

  // ===== Authentication Actions =====
  async function login(username, password) {
    try {
      const res = await request.post('/auth/login', { username, password })
      token.value = res.token
      setToken(res.token)

      const user = normalizeUser(res.user)
      currentUser.value = user
      localStorage.setItem(STORAGE_KEY_AUTH_USER, JSON.stringify(user))

      // 加载该用户的真实题库
      await loadActiveUserData()
      return { success: true, user }
    } catch (e) {
      return { success: false, message: e.message || '登录失败' }
    }
  }

  async function register({ username, password, name }) {
    try {
      const res = await request.post('/auth/register', { username, password, nickname: name })
      token.value = res.token
      setToken(res.token)

      const user = normalizeUser(res.user)
      currentUser.value = user
      localStorage.setItem(STORAGE_KEY_AUTH_USER, JSON.stringify(user))

      await loadActiveUserData()
      return { success: true, user }
    } catch (e) {
      return { success: false, message: e.message || '注册失败' }
    }
  }

  function logout() {
    currentUser.value = null
    token.value = ''
    removeToken()
    localStorage.removeItem(STORAGE_KEY_AUTH_USER)
    companies.value = []
    entries.value = []
    dailyNotes.value = {}
  }

  // ===== Getters =====
  const starredEntries = computed(() => entries.value.filter(e => e.isStarred))

  function getCompany(id) {
    return companies.value.find(c => String(c.id) === String(id)) || null
  }

  function getEntriesByCompany(companyId) {
    return entries.value.filter(e => String(e.companyId) === String(companyId))
  }

  // ===== Company CRUD =====
  async function addCompany({ name, logo, industry }) {
    try {
      const res = await request.post('/companies', {
        name,
        logo: logo || 'default',
        industry: industry || '互联网',
      })
      const newComp = normalizeCompany(res)
      companies.value.push(newComp)
      await fetchDashboardStats()
      return newComp.id
    } catch (e) {
      console.error('Failed to add company:', e)
      throw e
    }
  }

  async function updateCompany(id, { name, logo, industry }) {
    try {
      const res = await request.put(`/companies/${id}`, { name, logo, industry })
      const updated = normalizeCompany(res)
      const idx = companies.value.findIndex(c => String(c.id) === String(id))
      if (idx !== -1) {
        companies.value[idx] = updated
      }
      await fetchDashboardStats()
    } catch (e) {
      console.error('Failed to update company:', e)
      throw e
    }
  }

  async function deleteCompany(id) {
    try {
      await request.delete(`/companies/${id}`)
      companies.value = companies.value.filter(c => String(c.id) !== String(id))
      entries.value = entries.value.filter(e => String(e.companyId) !== String(id))
      await fetchDashboardStats()
    } catch (e) {
      console.error('Failed to delete company:', e)
      throw e
    }
  }

  // ===== Entry CRUD =====
  async function addEntry({ companyId, type, title, difficulty, status, tags, content, isStarred }) {
    try {
      const res = await request.post('/entries', {
        companyId: Number(companyId),
        type,
        title,
        difficulty,
        status: status || '未掌握',
        isStarred: isStarred ? 1 : 0,
        tags: tags || [],
        content: content || '',
      })
      const newEntry = normalizeEntry(res)
      entries.value.unshift(newEntry)
      await fetchDashboardStats()
      return newEntry
    } catch (e) {
      console.error('Failed to add entry:', e)
      throw e
    }
  }

  async function updateEntry(id, patch) {
    try {
      const res = await request.put(`/entries/${id}`, {
        companyId: patch.companyId ? Number(patch.companyId) : undefined,
        type: patch.type,
        title: patch.title,
        difficulty: patch.difficulty,
        status: patch.status,
        isStarred: patch.isStarred !== undefined ? (patch.isStarred ? 1 : 0) : undefined,
        tags: patch.tags,
        content: patch.content,
      })
      const updated = normalizeEntry(res)
      const idx = entries.value.findIndex(e => String(e.id) === String(id))
      if (idx !== -1) {
        entries.value[idx] = { ...entries.value[idx], ...updated }
      }
      await fetchDashboardStats()
      return updated
    } catch (e) {
      console.error('Failed to update entry:', e)
      throw e
    }
  }

  async function deleteEntry(id) {
    try {
      await request.delete(`/entries/${id}`)
      entries.value = entries.value.filter(e => String(e.id) !== String(id))
      await fetchDashboardStats()
    } catch (e) {
      console.error('Failed to delete entry:', e)
      throw e
    }
  }

  async function toggleStar(id) {
    try {
      const res = await request.patch(`/entries/${id}/star`)
      const updated = normalizeEntry(res)
      const entry = entries.value.find(e => String(e.id) === String(id))
      if (entry) {
        entry.isStarred = updated.isStarred
      }
    } catch (e) {
      console.error('Failed to toggle star:', e)
    }
  }

  async function updateMasteryStatus(id, newStatus) {
    try {
      const res = await request.patch(`/entries/${id}/status`, { status: newStatus })
      const updated = normalizeEntry(res)
      const entry = entries.value.find(e => String(e.id) === String(id))
      if (entry) {
        entry.status = updated.status
        entry.updatedAt = updated.updatedAt
      }
      await fetchDashboardStats()
    } catch (e) {
      console.error('Failed to update mastery status:', e)
    }
  }

  // ===== Daily Review & Notes =====
  async function saveDailyNote(dateStr, noteContent) {
    try {
      await request.post('/reviews/daily', { reviewDate: dateStr, noteContent })
      dailyNotes.value[dateStr] = noteContent
    } catch (e) {
      console.error('Failed to save daily note:', e)
    }
  }

  async function fetchDailyReview(dateStr) {
    try {
      const data = await request.get('/reviews/daily', { date: dateStr })
      if (data) {
        dailyNotes.value[dateStr] = data.noteContent || ''
        return {
          noteContent: data.noteContent || '',
          entries: (data.entries || []).map(normalizeEntry),
        }
      }
    } catch (e) {
      console.error('Failed to fetch daily review:', e)
    }
    return { noteContent: '', entries: [] }
  }

  function getDailyNote(dateStr) {
    return dailyNotes.value[dateStr] || ''
  }

  function getEntriesByDate(dateStr) {
    return entries.value.filter(e => {
      const created = e.createdAt?.slice(0, 10)
      const updated = e.updatedAt?.slice(0, 10)
      return created === dateStr || updated === dateStr
    })
  }

  // ===== Mock Interview Draw =====
  async function drawMockInterview(companyId = null, baguCount = 3, algoCount = 1) {
    try {
      const res = await request.post('/entries/mock-draw', {
        companyId: companyId ? Number(companyId) : null,
        baguCount,
        algoCount,
      })
      return (res || []).map(normalizeEntry)
    } catch (e) {
      console.error('Failed to draw mock interview from API, falling back locally:', e)
      let pool = entries.value
      if (companyId) {
        pool = pool.filter(e => String(e.companyId) === String(companyId))
      }
      const baguPool = pool.filter(e => e.type === '八股文')
      const algoPool = pool.filter(e => e.type === '算法题')
      const shuffle = (arr) => [...arr].sort(() => Math.random() - 0.5)
      return [...shuffle(baguPool).slice(0, baguCount), ...shuffle(algoPool).slice(0, algoCount)]
    }
  }

  return {
    currentUser,
    isLoggedIn,
    loading,
    companies,
    entries,
    dailyNotes,
    starredEntries,
    radarSkillStats,
    companyDistributionStats,
    login,
    register,
    logout,
    loadActiveUserData,
    getCompany,
    getEntriesByCompany,
    addCompany,
    updateCompany,
    deleteCompany,
    addEntry,
    updateEntry,
    deleteEntry,
    toggleStar,
    updateMasteryStatus,
    drawMockInterview,
    saveDailyNote,
    fetchDailyReview,
    getDailyNote,
    getEntriesByDate,
  }
})

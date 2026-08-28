import { createRouter, createWebHashHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import CompaniesView from '../views/CompaniesView.vue'
import CompanyDetailView from '../views/CompanyDetailView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import DailyReviewView from '../views/DailyReviewView.vue'
import ZenModeView from '../views/ZenModeView.vue'
import SearchView from '../views/SearchView.vue'
import LoginView from '../views/LoginView.vue'
import { useInterviewStore } from '../stores/useInterviewStore'

const routes = [
  { path: '/login', name: 'Login', component: LoginView, meta: { public: true } },
  { path: '/', name: 'Dashboard', component: DashboardView },
  { path: '/companies', name: 'Companies', component: CompaniesView },
  { path: '/companies/:id', name: 'CompanyDetail', component: CompanyDetailView },
  { path: '/favorites', name: 'Favorites', component: FavoritesView },
  { path: '/daily-review', name: 'DailyReview', component: DailyReviewView },
  { path: '/zen-mode', name: 'ZenMode', component: ZenModeView },
  { path: '/search', name: 'Search', component: SearchView },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// Global Auth Navigation Guard
router.beforeEach((to, from, next) => {
  const store = useInterviewStore()
  if (!to.meta.public && !store.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && store.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router

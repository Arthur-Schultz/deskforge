import { useQuery } from '@tanstack/react-query'
import { api } from './api'
import type { Category, StoreInfo } from './types'

export function useStoreInfo() {
  return useQuery({
    queryKey: ['store'],
    queryFn: () => api.get<StoreInfo>('/api/store'),
    staleTime: Infinity,
  })
}

export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => api.get<Category[]>('/api/categories'),
    staleTime: Infinity,
  })
}

export function useIsSharedDemoAccount(email?: string | null) {
  const { data: store } = useStoreInfo()
  return Boolean(email && store?.publicDemo && store.sharedDemoAccounts.includes(email))
}

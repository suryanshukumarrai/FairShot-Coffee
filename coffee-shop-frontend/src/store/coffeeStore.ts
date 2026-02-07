import { create } from 'zustand'
import type { Order, Barista, UserRole } from '../types'

interface CoffeeStore {
    orders: Order[]
    baristas: Barista[]
    currentRole: UserRole
    selectedDrinkType: string | null
    selectedCustomerType: string
    isLoading: boolean
    error: string | null

    setOrders: (orders: Order[]) => void
    setBaristas: (baristas: Barista[]) => void
    setCurrentRole: (role: UserRole) => void
    setSelectedDrinkType: (drinkType: string | null) => void
    setSelectedCustomerType: (customerType: string) => void
    setLoading: (loading: boolean) => void
    setError: (error: string | null) => void

    // Computed values
    getPendingOrders: () => Order[]
    getInProgressOrders: () => Order[]
    getCompletedOrders: () => Order[]
    getAvailableBaristas: () => Barista[]
    getBusyBaristas: () => Barista[]
    getUrgentOrders: () => Order[]
}

export const useCoffeeStore = create<CoffeeStore>((set, get) => ({
    orders: [],
    baristas: [],
    currentRole: 'customer',
    selectedDrinkType: null,
    selectedCustomerType: 'REGULAR',
    isLoading: false,
    error: null,

    setOrders: (orders) => set({ orders }),
    setBaristas: (baristas) => set({ baristas }),
    setCurrentRole: (role) => set({ currentRole: role }),
    setSelectedDrinkType: (drinkType) => set({ selectedDrinkType: drinkType }),
    setSelectedCustomerType: (customerType) => set({ selectedCustomerType: customerType }),
    setLoading: (loading) => set({ isLoading: loading }),
    setError: (error) => set({ error }),

    getPendingOrders: () => get().orders.filter(o => o.status === 'PENDING'),
    getInProgressOrders: () => get().orders.filter(o => o.status === 'IN_PROGRESS'),
    getCompletedOrders: () => get().orders.filter(o => o.status === 'COMPLETED'),
    getAvailableBaristas: () => get().baristas.filter(b => b.isAvailable),
    getBusyBaristas: () => get().baristas.filter(b => b.isBusy),
    getUrgentOrders: () => get().orders.filter(o => o.waitTimeMinutes >= 8),
}))

export interface DrinkType {
    name: string
    displayName: string
    prepTimeMinutes: number
    complexity: 'quick' | 'medium' | 'complex'
}

export interface MenuItem {
    type: string
    displayName: string
    prepTimeMinutes: number
    priceInRupees: number
    complexity: 'QUICK' | 'MEDIUM' | 'COMPLEX'
}

export interface CustomerType {
    name: string
    displayName: string
}

export interface Order {
    id: string
    drinkType: DrinkType
    customerType: CustomerType
    status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
    arrivalTime: string
    startTime?: string
    completionTime?: string
    assignedBaristaId?: number
    waitTimeMinutes: number
    priorityScore?: number
}

export interface Barista {
    id: number
    name: string
    isAvailable: boolean
    isBusy: boolean
    totalWorkedMinutes: number
    remainingMinutes: number
    currentOrder?: Order
    busyUntil?: string
}

export interface OrderRequest {
    drinkType: string
    customerType: string
}

export interface ManagerMetrics {
    totalCoffeesServed: number
    byDrink: Record<string, number>
    avgWaitMinutes: number
    slaViolations: number
    baristaUtilization: Record<number, number>
}

export interface SimulationRequest {
    orders: number
    random?: boolean
}

export interface SimulationResponse {
    ordersProcessed: number
    maxQueueSize: number
    avgWaitMinutes: number
    fairnessSkips: number
    slaBreaches: number
    baristaWorkload: Record<number, number>
    executionTimeMs: number
    transparencyMessages: string[]
}

export type UserRole = 'customer' | 'barista' | 'manager' | 'public'

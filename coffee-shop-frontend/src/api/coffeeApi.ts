import axios from 'axios'
import type { Order, Barista, OrderRequest, ManagerMetrics, SimulationRequest, SimulationResponse, MenuItem } from '../types'

const API_BASE = 'http://localhost:8080/api'

const api = axios.create({
    baseURL: API_BASE,
    headers: {
        'Content-Type': 'application/json',
    },
})

export const fetchOrders = async (): Promise<Order[]> => {
    const response = await api.get('/orders')
    return response.data
}

export const fetchBaristas = async (): Promise<Barista[]> => {
    const response = await api.get('/baristas')
    return response.data
}

export const fetchMenu = async (): Promise<MenuItem[]> => {
    const response = await api.get('/menu')
    return response.data
}

export const createOrder = async (order: OrderRequest): Promise<Order> => {
    const response = await api.post('/orders', order)
    return response.data
}

export const fetchOrderById = async (id: string): Promise<Order> => {
    const response = await api.get(`/orders/${id}`)
    return response.data
}

export const assignOrderToBarista = async (baristaId: number, orderId: string): Promise<void> => {
    await api.put(`/baristas/${baristaId}/assign/${orderId}`)
}

export const fetchManagerMetrics = async (): Promise<ManagerMetrics> => {
    const response = await api.get('/manager/metrics')
    return response.data
}

export const runSimulation = async (request: SimulationRequest): Promise<SimulationResponse> => {
    const response = await api.post('/manager/simulate', request)
    return response.data
}

export const resetMetrics = async (): Promise<void> => {
    await api.post('/manager/metrics/reset')
}

export const resetSystem = async (): Promise<void> => {
    await api.post('/manager/reset')
}

export default api

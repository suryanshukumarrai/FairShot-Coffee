import { useEffect, useState } from 'react'
import { useCoffeeStore } from '../store/coffeeStore'
import { fetchOrders, fetchBaristas, fetchManagerMetrics } from '../api/coffeeApi'
import BaristaCard from '../components/BaristaCard'
import { AlertTriangle, BarChart, Settings, TrendingUp } from 'lucide-react'
import type { ManagerMetrics } from '../types'

export default function ManagerDashboard() {
    const { orders, baristas, setOrders, setBaristas } = useCoffeeStore()
    const [metrics, setMetrics] = useState<ManagerMetrics | null>(null)

    useEffect(() => {
        const loadData = async () => {
            try {
                const [ordersData, baristasData, metricsData] = await Promise.all([
                    fetchOrders(),
                    fetchBaristas(),
                    fetchManagerMetrics()
                ])
                setOrders(ordersData)
                setBaristas(baristasData)
                setMetrics(metricsData)
            } catch (error) {
                console.error('Failed to load data:', error)
            }
        }

        loadData()
        const interval = setInterval(loadData, 3000) // 3 seconds for manager

        return () => clearInterval(interval)
    }, [setOrders, setBaristas])

    const urgentOrders = orders.filter(o => o.waitTimeMinutes >= 8)
    const warningOrders = orders.filter(o => o.waitTimeMinutes >= 7 && o.waitTimeMinutes < 8)

    const getUtilization = (barista: any) => {
        // CRITICAL: NaN protection - handle division by zero
        const maxMinutes = 120
        const worked = barista.totalWorkedMinutes || 0
        const utilization = (worked / maxMinutes) * 100
        return Math.min(isNaN(utilization) ? 0 : utilization, 100)
    }

    return (
        <div className="max-w-7xl mx-auto p-6">
            {/* Header */}
            <div className="flex items-center justify-between mb-8">
                <div className="flex items-center space-x-3">
                    <Settings className="w-10 h-10 text-accent-light" />
                    <h1 className="text-4xl font-bold">Manager Console</h1>
                </div>
            </div>

            {/* Alerts */}
            {(urgentOrders.length > 0 || warningOrders.length > 0) && (
                <div className="card bg-status-urgent bg-opacity-10 border-status-urgent border-2 mb-8">
                    <div className="flex items-center space-x-3 mb-4">
                        <AlertTriangle className="w-8 h-8 text-status-urgent" />
                        <h2 className="text-2xl font-bold text-status-urgent">ALERTS</h2>
                    </div>
                    <div className="space-y-2">
                        {urgentOrders.map(order => (
                            <div key={order.id} className="flex items-center justify-between p-3 bg-status-urgent bg-opacity-20 rounded">
                                <div>
                                    <span className="font-semibold">Order #{order.id.substring(0, 8)}</span>
                                    <span className="mx-2">•</span>
                                    <span>{order.drinkType.displayName}</span>
                                </div>
                                <span className="font-bold text-status-urgent">{order.waitTimeMinutes.toFixed(1)} min</span>
                            </div>
                        ))}
                        {warningOrders.map(order => (
                            <div key={order.id} className="flex items-center justify-between p-3 bg-status-warning bg-opacity-20 rounded">
                                <div>
                                    <span className="font-semibold">Order #{order.id.substring(0, 8)}</span>
                                    <span className="mx-2">•</span>
                                    <span>{order.drinkType.displayName}</span>
                                </div>
                                <span className="font-bold text-status-warning">{order.waitTimeMinutes.toFixed(1)} min</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Metrics Grid */}
            <div className="grid grid-cols-4 gap-4 mb-8">
                <div className="card text-center">
                    <p className="text-gray-400 text-sm mb-2">Total Coffees Served</p>
                    <p className="text-4xl font-bold text-status-normal">{metrics?.totalCoffeesServed || 0}</p>
                </div>
                <div className="card text-center">
                    <p className="text-gray-400 text-sm mb-2">Pending</p>
                    <p className="text-4xl font-bold text-status-warning">
                        {orders.filter(o => o.status === 'PENDING').length}
                    </p>
                </div>
                <div className="card text-center">
                    <p className="text-gray-400 text-sm mb-2">Delivered</p>
                    <p className="text-4xl font-bold text-status-normal">
                        {orders.filter(o => o.status === 'COMPLETED').length}
                    </p>
                </div>
                <div className="card text-center">
                    <p className="text-gray-400 text-sm mb-2">Avg Wait</p>
                    <p className="text-4xl font-bold text-accent-light">{metrics?.avgWaitMinutes.toFixed(1) || '0.0'} min</p>
                </div>
            </div>

            {/* Coffees by Drink Type */}
            {metrics && metrics.totalCoffeesServed > 0 && (
                <div className="card mb-8">
                    <div className="flex items-center space-x-3 mb-6">
                        <TrendingUp className="w-6 h-6 text-accent-light" />
                        <h2 className="text-2xl font-bold">Coffees Served by Type</h2>
                    </div>
                    <div className="grid grid-cols-5 gap-4">
                        {Object.entries(metrics.byDrink).map(([drink, count]) => (
                            <div key={drink} className="text-center p-4 bg-bg-tertiary rounded-lg">
                                <p className="text-2xl font-bold text-accent-light">{count}</p>
                                <p className="text-sm text-gray-400 mt-1">{drink}</p>
                            </div>
                        ))}
                    </div>
                    <div className="mt-4 pt-4 border-t border-bg-tertiary">
                        <div className="flex items-center justify-between text-sm">
                            <span className="text-gray-400">SLA Violations (&gt;8 min wait)</span>
                            <span className={`font-bold ${metrics.slaViolations > 0 ? 'text-status-urgent' : 'text-status-normal'}`}>
                                {metrics.slaViolations}
                            </span>
                        </div>
                    </div>
                </div>
            )}

            {/* Barista Utilization */}
            <div className="card mb-8">
                <div className="flex items-center space-x-3 mb-6">
                    <BarChart className="w-6 h-6 text-accent-light" />
                    <h2 className="text-2xl font-bold">Barista Utilization</h2>
                </div>
                <div className="space-y-4">
                    {baristas.map(barista => {
                        const utilization = getUtilization(barista)
                        const workedMinutes = barista.totalWorkedMinutes || 0
                        return (
                            <div key={barista.id}>
                                <div className="flex items-center justify-between mb-2">
                                    <div className="flex items-center space-x-3">
                                        <div className={`w-3 h-3 rounded-full ${barista.currentOrder ? 'bg-blue-500 animate-pulse' : 'bg-status-normal'}`} />
                                        <span className="font-semibold">{barista.name}</span>
                                        {barista.currentOrder && (
                                            <span className="text-sm text-blue-400 ml-2">
                                                → {barista.currentOrder.drinkType.displayName}
                                            </span>
                                        )}
                                    </div>
                                    <div className="text-right">
                                        <span className="font-bold">{utilization.toFixed(0)}%</span>
                                        <span className="text-gray-400 text-sm ml-2">({workedMinutes} min)</span>
                                    </div>
                                </div>
                                <div className="w-full bg-bg-tertiary rounded-full h-3">
                                    <div
                                        className={`h-3 rounded-full ${utilization > 80 ? 'bg-status-urgent' : utilization > 50 ? 'bg-status-warning' : 'bg-status-normal'
                                            }`}
                                        style={{ width: `${utilization}%` }}
                                    />
                                </div>
                            </div>
                        )
                    })}
                </div>
            </div>

            {/* Currently Being Prepared */}
            <div className="card mb-8">
                <h2 className="text-2xl font-bold mb-4">Currently Being Prepared</h2>
                {baristas.filter(b => b.currentOrder).length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                        <p>No orders currently in preparation</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-3 gap-4">
                        {baristas.filter(b => b.currentOrder).map(barista => (
                            <div key={barista.id} className="bg-blue-900 bg-opacity-20 p-4 rounded-lg border-2 border-blue-500">
                                <div className="flex items-center justify-between mb-2">
                                    <span className="font-semibold">{barista.name}</span>
                                    <div className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
                                </div>
                                <div className="text-lg font-bold text-blue-400 mb-1">
                                    {barista.currentOrder?.drinkType.displayName}
                                </div>
                                <div className="text-sm text-gray-400">
                                    Order #{barista.currentOrder?.id.substring(0, 8)}
                                </div>
                                {barista.remainingMinutes > 0 && (
                                    <div className="text-xs text-accent-light mt-2">
                                        ⏱️ ~{barista.remainingMinutes} min remaining
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Baristas Overview */}
            <div className="mb-8">
                <h2 className="text-2xl font-bold mb-4">Baristas Overview</h2>
                <div className="grid grid-cols-3 gap-4">
                    {baristas.map(barista => (
                        <BaristaCard key={barista.id} barista={barista} />
                    ))}
                </div>
            </div>

            {/* Controls */}
            <div className="card">
                <h2 className="text-2xl font-bold mb-4">Emergency Controls</h2>
                <div className="grid grid-cols-3 gap-4">
                    <button className="btn-secondary py-4">Force Assign Order</button>
                    <button className="btn-secondary py-4">Rebalance Queue</button>
                    <button className="bg-status-urgent text-white px-6 py-4 rounded-lg font-semibold hover:bg-red-600 transition-colors">
                        Pause Orders
                    </button>
                </div>
            </div>
        </div>
    )
}

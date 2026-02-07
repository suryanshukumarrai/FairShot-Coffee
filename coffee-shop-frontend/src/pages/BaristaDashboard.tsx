import { useEffect, useState } from 'react'
import { useCoffeeStore } from '../store/coffeeStore'
import { fetchOrders, fetchBaristas } from '../api/coffeeApi'
import OrderCard from '../components/OrderCard'
import { User, Coffee } from 'lucide-react'

export default function BaristaDashboard() {
    const { orders, baristas, setOrders, setBaristas } = useCoffeeStore()
    const [selectedBaristaId, setSelectedBaristaId] = useState<number>(1)

    useEffect(() => {
        const loadData = async () => {
            try {
                const [ordersData, baristasData] = await Promise.all([
                    fetchOrders(),
                    fetchBaristas()
                ])
                setOrders(ordersData)
                setBaristas(baristasData)
            } catch (error) {
                console.error('Failed to load data:', error)
            }
        }

        loadData()
        const interval = setInterval(loadData, 1000) // 1 second for barista

        return () => clearInterval(interval)
    }, [setOrders, setBaristas])

    const selectedBarista = baristas.find(b => b.id === selectedBaristaId)
    const pendingOrders = orders.filter(o => o.status === 'PENDING').slice(0, 5)
    
    // CRITICAL: Only show next in queue if barista is actually free AND has no current order
    const showNextInQueue = selectedBarista && !selectedBarista.currentOrder && pendingOrders.length > 0
    const nextRecommended = showNextInQueue ? pendingOrders[0] : null

    const getWorkloadLabel = (minutes: number) => {
        if (!minutes || minutes < 30) return 'Light'
        if (minutes < 60) return 'Medium'
        return 'Heavy'
    }

    const getWorkloadColor = (minutes: number) => {
        if (!minutes || minutes < 30) return 'text-status-normal'
        if (minutes < 60) return 'text-status-warning'
        return 'text-status-urgent'
    }

    return (
        <div className="max-w-5xl mx-auto p-6">
            {/* Barista Selector */}
            <div className="mb-6 flex items-center justify-between">
                <div className="flex items-center space-x-4">
                    <User className="w-8 h-8 text-accent-light" />
                    <select
                        value={selectedBaristaId}
                        onChange={(e) => setSelectedBaristaId(Number(e.target.value))}
                        className="bg-bg-tertiary text-white px-4 py-2 rounded-lg text-lg font-semibold border border-bg-tertiary focus:border-accent-light focus:outline-none"
                    >
                        {baristas.map(b => (
                            <option key={b.id} value={b.id}>{b.name}</option>
                        ))}
                    </select>
                </div>

                {selectedBarista && (
                    <div className="text-right">
                        <p className="text-gray-400 text-sm">Workload</p>
                        <p className={`text-xl font-bold ${getWorkloadColor(selectedBarista.totalWorkedMinutes)}`}>
                            {getWorkloadLabel(selectedBarista.totalWorkedMinutes)}
                        </p>
                    </div>
                )}
            </div>

            {/* Current Order */}
            <div className="mb-8">
                <h2 className="text-2xl font-bold mb-4">CURRENT ORDER</h2>
                {selectedBarista?.currentOrder ? (
                    <div className="card border-2 border-blue-500">
                        <div className="flex items-center space-x-4 mb-4">
                            <Coffee className="w-12 h-12 text-blue-500 animate-pulse" />
                            <div>
                                <p className="text-gray-400 text-sm">Order #{selectedBarista.currentOrder.id.substring(0, 8)}</p>
                                <h3 className="text-3xl font-bold">{selectedBarista.currentOrder.drinkType.displayName}</h3>
                            </div>
                        </div>
                        <div className="flex items-center justify-between mb-4">
                            <span className="text-lg">Prep Time: {selectedBarista.currentOrder.drinkType.prepTimeMinutes} min</span>
                            {selectedBarista.remainingMinutes > 0 && (
                                <span className="text-lg text-accent-light">⏱️ {selectedBarista.remainingMinutes} min remaining</span>
                            )}
                        </div>
                        <div className="bg-blue-900 bg-opacity-30 p-4 rounded-lg text-center">
                            <div className="text-sm text-gray-300 mb-2">Status</div>
                            <div className="text-2xl font-bold text-blue-400">🔥 PREPARING</div>
                            <div className="text-xs text-gray-400 mt-2">Auto-started when assigned</div>
                        </div>
                    </div>
                ) : (
                    <div className="card text-center py-12 text-gray-500">
                        <div className="text-green-500 text-5xl mb-4">✓</div>
                        <p className="text-2xl mb-2">AVAILABLE</p>
                        <p className="text-sm">Waiting for next order...</p>
                    </div>
                )}
            </div>

            {/* Next in Queue */}
            {!selectedBarista?.currentOrder && nextRecommended && (
                <div className="mb-8">
                    <h2 className="text-2xl font-bold mb-4 flex items-center space-x-2">
                        <span>NEXT IN QUEUE</span>
                        {nextRecommended.drinkType.prepTimeMinutes <= 2 && (
                            <span className="text-sm bg-green-900 text-green-200 px-2 py-1 rounded">⚡ Quick</span>
                        )}
                    </h2>
                    <div className="card border-2 border-accent-light bg-amber-900 bg-opacity-10">
                        <div className="flex items-center space-x-4 mb-4">
                            <Coffee className="w-12 h-12 text-accent-light" />
                            <div>
                                <p className="text-gray-400 text-sm">Order #{nextRecommended.id.substring(0, 8)}</p>
                                <h3 className="text-3xl font-bold">{nextRecommended.drinkType.displayName}</h3>
                            </div>
                        </div>
                        <div className="mb-4">
                            <p className="text-lg">Prep Time: {nextRecommended.drinkType.prepTimeMinutes} min</p>
                            <p className="text-sm text-gray-400">Wait Time: {nextRecommended.waitTimeMinutes} min</p>
                        </div>
                        {nextRecommended.waitTimeMinutes >= 8 && (
                            <div className="mb-4 p-3 bg-status-urgent bg-opacity-20 rounded text-status-urgent font-semibold">
                                🚨 URGENT - Will be prioritized
                            </div>
                        )}
                        <div className="bg-accent-light bg-opacity-10 p-4 rounded-lg text-center">
                            <div className="text-sm text-gray-400 mb-2">Auto-Assignment</div>
                            <div className="text-lg font-semibold text-accent-light">Will start automatically when you're free</div>
                        </div>
                    </div>
                </div>
            )}

            {/* Queue Preview */}
            <div>
                <h2 className="text-2xl font-bold mb-4">QUEUE PREVIEW</h2>
                {pendingOrders.length === 0 ? (
                    <div className="card text-center py-8 text-gray-500">
                        <p>No pending orders</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {pendingOrders.map((order) => (
                            <OrderCard key={order.id} order={order} compact />
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}

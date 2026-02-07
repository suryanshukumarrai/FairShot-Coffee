import { useEffect } from 'react'
import { useCoffeeStore } from '../store/coffeeStore'
import { fetchOrders } from '../api/coffeeApi'
import { Coffee } from 'lucide-react'

export default function PublicQueue() {
    const { orders, setOrders } = useCoffeeStore()

    useEffect(() => {
        const loadOrders = async () => {
            try {
                const data = await fetchOrders()
                setOrders(data)
            } catch (error) {
                console.error('Failed to load orders:', error)
            }
        }

        loadOrders()
        const interval = setInterval(loadOrders, 2000)

        return () => clearInterval(interval)
    }, [setOrders])

    const inProgressOrders = orders.filter(o => o.status === 'IN_PROGRESS').slice(0, 4)
    const upNextOrders = orders.filter(o => o.status === 'PENDING').slice(0, 6)

    const isQuickOrder = (prepTime: number) => prepTime <= 2
    const isComplexOrder = (prepTime: number) => prepTime >= 5

    return (
        <div className="min-h-screen p-8">
            {/* Header */}
            <div className="text-center mb-12">
                <div className="flex items-center justify-center space-x-4 mb-4">
                    <Coffee className="w-16 h-16 text-accent-light" />
                    <h1 className="text-6xl font-bold">Coffee Shop Queue</h1>
                </div>
                <p className="text-2xl text-gray-400">Live Order Status</p>
            </div>

            {/* Two Column Layout */}
            <div className="grid grid-cols-2 gap-8 max-w-7xl mx-auto">
                {/* Now Preparing */}
                <div>
                    <h2 className="text-3xl font-bold mb-6 text-blue-400">NOW PREPARING</h2>
                    {inProgressOrders.length === 0 ? (
                        <div className="card text-center text-gray-500 py-12">
                            <p className="text-xl">No orders being prepared</p>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {inProgressOrders.map((order) => (
                                <div
                                    key={order.id}
                                    className="card bg-blue-900 bg-opacity-30 border-l-4 border-blue-400"
                                >
                                    <div className="flex items-center justify-between mb-2">
                                        <h3 className="text-3xl font-bold">#{order.id.substring(0, 8)}</h3>
                                        {isQuickOrder(order.drinkType.prepTimeMinutes) && (
                                            <span className="text-xs bg-green-900 text-green-200 px-3 py-1 rounded-full font-semibold">
                                                ⚡ QUICK
                                            </span>
                                        )}
                                        {isComplexOrder(order.drinkType.prepTimeMinutes) && (
                                            <span className="text-xs bg-orange-900 text-orange-200 px-3 py-1 rounded-full font-semibold">
                                                ☕ COMPLEX
                                            </span>
                                        )}
                                    </div>
                                    <div className="flex items-center space-x-3">
                                        <Coffee className="w-8 h-8 text-accent-light" />
                                        <p className="text-2xl font-semibold">{order.drinkType.displayName}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Up Next */}
                <div>
                    <h2 className="text-3xl font-bold mb-6 text-accent-light">UP NEXT</h2>
                    {upNextOrders.length === 0 ? (
                        <div className="card text-center text-gray-500 py-12">
                            <p className="text-xl">Queue is empty</p>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {upNextOrders.map((order, index) => (
                                <div
                                    key={order.id}
                                    className={`card border-l-4 border-accent-light ${index === 0 ? 'bg-amber-900 bg-opacity-20' : ''
                                        }`}
                                >
                                    <div className="flex items-center justify-between mb-2">
                                        <h3 className="text-2xl font-bold">#{order.id.substring(0, 8)}</h3>
                                        {isQuickOrder(order.drinkType.prepTimeMinutes) && (
                                            <span className="text-xs bg-green-900 text-green-200 px-3 py-1 rounded-full font-semibold">
                                                ⚡ QUICK
                                            </span>
                                        )}
                                        {isComplexOrder(order.drinkType.prepTimeMinutes) && (
                                            <span className="text-xs bg-orange-900 text-orange-200 px-3 py-1 rounded-full font-semibold">
                                                ☕ COMPLEX
                                            </span>
                                        )}
                                    </div>
                                    <div className="flex items-center space-x-3">
                                        <Coffee className="w-6 h-6 text-accent-light" />
                                        <p className="text-xl">{order.drinkType.displayName}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* Footer Info */}
            <div className="mt-12 text-center text-gray-500 text-sm">
                <p>Updated every 2 seconds • Quick orders prioritized to reduce overall wait time</p>
            </div>
        </div>
    )
}

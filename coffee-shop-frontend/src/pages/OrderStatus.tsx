import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { fetchOrders } from '../api/coffeeApi'
import type { Order } from '../types'
import { Coffee, Clock } from 'lucide-react'

export default function OrderStatus() {
    const { orderId } = useParams<{ orderId?: string }>()
    const [order, setOrder] = useState<Order | null>(null)
    const [queuePosition, setQueuePosition] = useState<number>(0)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const loadOrderStatus = async () => {
            try {
                const orders = await fetchOrders()

                if (orderId) {
                    const foundOrder = orders.find(o => o.id === orderId)
                    setOrder(foundOrder || null)

                    // Calculate queue position
                    const pendingOrders = orders
                        .filter(o => o.status === 'PENDING')
                        .sort((a, b) => new Date(a.arrivalTime).getTime() - new Date(b.arrivalTime).getTime())

                    const position = pendingOrders.findIndex(o => o.id === orderId) + 1
                    setQueuePosition(position)
                } else {
                    // Show latest order
                    const latestOrder = orders[orders.length - 1]
                    setOrder(latestOrder || null)
                }

                setLoading(false)
            } catch (error) {
                console.error('Failed to load order:', error)
                setLoading(false)
            }
        }

        loadOrderStatus()
        const interval = setInterval(loadOrderStatus, 2000)

        return () => clearInterval(interval)
    }, [orderId])

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center">
                    <Coffee className="w-16 h-16 text-accent-light mx-auto mb-4 animate-spin" />
                    <p className="text-xl">Loading order status...</p>
                </div>
            </div>
        )
    }

    if (!order) {
        return (
            <div className="max-w-2xl mx-auto p-6 text-center">
                <div className="card">
                    <h2 className="text-2xl font-bold mb-4">Order Not Found</h2>
                    <p className="text-gray-400">We couldn't find your order. Please check your order number.</p>
                </div>
            </div>
        )
    }

    const progress = order.status === 'COMPLETED' ? 100 : order.status === 'IN_PROGRESS' ? 65 : 30
    const isUrgent = order.waitTimeMinutes >= 8
    const isWarning = order.waitTimeMinutes >= 7

    return (
        <div className="max-w-2xl mx-auto p-6">
            {/* Order Number */}
            <div className="text-center mb-8">
                <p className="text-gray-400 text-sm mb-2">Your Order Number</p>
                <h1 className="text-6xl font-bold text-accent-light mb-4">#{order.id.substring(0, 8)}</h1>
                <p className="text-xl">{order.drinkType.displayName}</p>
            </div>

            {/* Progress Bar */}
            <div className="card mb-6">
                <div className="mb-4">
                    <div className="flex items-center justify-between mb-2">
                        <span className="text-sm font-semibold">Progress</span>
                        <span className="text-sm">{progress}%</span>
                    </div>
                    <div className="w-full bg-bg-tertiary rounded-full h-4">
                        <div
                            className={`h-4 rounded-full transition-all ${isUrgent ? 'bg-status-urgent' : isWarning ? 'bg-status-warning' : 'bg-accent-light'
                                }`}
                            style={{ width: `${progress}%` }}
                        />
                    </div>
                </div>

                {/* Status */}
                <div className="flex items-center justify-between">
                    <div>
                        <p className="text-gray-400 text-sm">Status</p>
                        <p className="text-2xl font-bold capitalize">{order.status.toLowerCase().replace('_', ' ')}</p>
                    </div>
                    <div className="text-right">
                        {order.status === 'PENDING' && queuePosition > 0 && (
                            <>
                                <p className="text-gray-400 text-sm">Queue Position</p>
                                <p className="text-4xl font-bold text-accent-light">{queuePosition}</p>
                            </>
                        )}
                    </div>
                </div>
            </div>

            {/* ETA */}
            <div className={`card mb-6 ${isUrgent ? 'bg-status-urgent bg-opacity-20 border-status-urgent border-2' : ''}`}>
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                        <Clock className={`w-12 h-12 ${isUrgent ? 'text-status-urgent' : 'text-accent-light'}`} />
                        <div>
                            <p className="text-gray-400 text-sm">Estimated Time</p>
                            <p className={`text-3xl font-bold ${isUrgent ? 'text-status-urgent' : 'text-white'}`}>
                                ~{order.waitTimeMinutes} min
                            </p>
                        </div>
                    </div>
                    {order.assignedBaristaId && (
                        <div className="text-right">
                            <p className="text-gray-400 text-sm">Barista</p>
                            <p className="text-xl font-semibold">#{order.assignedBaristaId}</p>
                        </div>
                    )}
                </div>

                {isUrgent && (
                    <div className="mt-4 p-3 bg-status-urgent bg-opacity-30 rounded text-sm font-semibold">
                        🚨 Your order is being prioritized now
                    </div>
                )}
            </div>

            {/* Fairness Message */}
            {order.status === 'PENDING' && (
                <div className="card bg-blue-900 bg-opacity-20">
                    <p className="text-blue-200 text-sm leading-relaxed">
                        <span className="font-semibold">ℹ️ Why some orders may go first:</span><br />
                        Quick orders (1-2 min) may be served before you to reduce overall wait time for everyone.
                        This smart queue system ensures the best experience for all customers.
                    </p>
                </div>
            )}

            {/* Completion Message */}
            {order.status === 'COMPLETED' && (
                <div className="card bg-status-normal bg-opacity-20 text-center">
                    <div className="text-6xl mb-4">✅</div>
                    <h2 className="text-2xl font-bold text-status-normal mb-2">Your Order is Ready!</h2>
                    <p className="text-gray-300">Please pick it up at the counter</p>
                </div>
            )}
        </div>
    )
}

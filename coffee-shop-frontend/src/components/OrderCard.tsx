import { Coffee, Clock, User } from 'lucide-react'
import type { Order } from '../types'

interface OrderCardProps {
    order: Order
    showBarista?: boolean
    compact?: boolean
}

export default function OrderCard({ order, showBarista = false, compact = false }: OrderCardProps) {
    const getStatusColor = () => {
        switch (order.status) {
            case 'PENDING':
                return 'bg-status-warning'
            case 'IN_PROGRESS':
                return 'bg-blue-500'
            case 'COMPLETED':
                return 'bg-status-normal'
            default:
                return 'bg-gray-500'
        }
    }

    const getUrgencyStyle = () => {
        if (order.waitTimeMinutes >= 8) {
            return 'border-status-urgent border-2 animate-pulse'
        }
        if (order.waitTimeMinutes >= 7) {
            return 'border-status-warning border-2'
        }
        return ''
    }

    const isQuick = order.drinkType.prepTimeMinutes <= 2
    const isComplex = order.drinkType.prepTimeMinutes >= 5

    return (
        <div className={`card ${getUrgencyStyle()} ${compact ? 'p-4' : ''}`}>
            <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-3">
                    <Coffee className="w-6 h-6 text-accent-light" />
                    <div>
                        <h3 className="font-semibold text-lg">{order.drinkType.displayName}</h3>
                        {!compact && (
                            <p className="text-gray-400 text-sm">Order #{order.id.substring(0, 8)}</p>
                        )}
                    </div>
                </div>
                <span className={`${getStatusColor()} status-badge`}>
                    {order.status}
                </span>
            </div>

            <div className="flex items-center space-x-4 text-sm text-gray-300">
                <div className="flex items-center space-x-1">
                    <User className="w-4 h-4" />
                    <span>{order.customerType.displayName}</span>
                </div>
                <div className="flex items-center space-x-1">
                    <Clock className="w-4 h-4" />
                    <span className={order.waitTimeMinutes >= 8 ? 'text-status-urgent font-bold' : ''}>
                        {order.waitTimeMinutes} min wait
                    </span>
                </div>
                {showBarista && order.assignedBaristaId && (
                    <span className="text-accent-light">Barista {order.assignedBaristaId}</span>
                )}
            </div>

            {(isQuick || isComplex) && !compact && (
                <div className="mt-2">
                    {isQuick && (
                        <span className="text-xs bg-green-900 text-green-200 px-2 py-1 rounded">
                            ⚡ Quick Order
                        </span>
                    )}
                    {isComplex && (
                        <span className="text-xs bg-orange-900 text-orange-200 px-2 py-1 rounded">
                            ☕ Complex Drink
                        </span>
                    )}
                </div>
            )}

            {order.waitTimeMinutes >= 8 && (
                <div className="mt-3 p-2 bg-status-urgent bg-opacity-20 rounded text-status-urgent text-sm font-semibold">
                    🚨 URGENT - Prioritized
                </div>
            )}
        </div>
    )
}

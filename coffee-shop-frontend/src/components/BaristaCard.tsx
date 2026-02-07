import type { Barista } from '../types'

interface BaristaCardProps {
    barista: Barista
    onClick?: () => void
}

export default function BaristaCard({ barista, onClick }: BaristaCardProps) {
    const getWorkloadLabel = () => {
        const worked = barista.totalWorkedMinutes
        if (worked < 30) return 'Light'
        if (worked < 60) return 'Medium'
        return 'Heavy'
    }

    const getWorkloadColor = () => {
        const worked = barista.totalWorkedMinutes
        if (worked < 30) return 'text-status-normal'
        if (worked < 60) return 'text-status-warning'
        return 'text-status-urgent'
    }

    return (
        <div
            className={`card ${onClick ? 'cursor-pointer hover:scale-105' : ''} transition-all ${barista.isAvailable ? 'border-status-normal' : 'border-status-urgent'
                } border-l-4`}
            onClick={onClick}
        >
            <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-3">
                    <div className={`w-3 h-3 rounded-full ${barista.isAvailable ? 'bg-status-normal' : 'bg-status-urgent'} shadow-lg`} />
                    <h3 className="text-xl font-semibold">{barista.name}</h3>
                </div>
                <span className={`text-sm font-medium ${getWorkloadColor()}`}>
                    {getWorkloadLabel()}
                </span>
            </div>

            {barista.currentOrder && (
                <div className="mb-3 p-3 bg-bg-tertiary rounded-lg">
                    <p className="text-sm text-gray-400">Currently making:</p>
                    <p className="font-medium">{barista.currentOrder.drinkType.displayName}</p>
                </div>
            )}

            <div className="flex items-center justify-between text-sm text-gray-400">
                <span>📊 Worked: {barista.totalWorkedMinutes} min</span>
                {barista.remainingMinutes > 0 && (
                    <span>⏱️ Remaining: {barista.remainingMinutes} min</span>
                )}
            </div>
        </div>
    )
}

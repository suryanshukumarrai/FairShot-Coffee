import { Coffee } from 'lucide-react'

interface DrinkCardProps {
    name: string
    displayName: string
    prepTime: number
    priceInRupees: number
    complexity: 'QUICK' | 'MEDIUM' | 'COMPLEX'
    selected: boolean
    onClick: () => void
}

export default function DrinkCard({ displayName, prepTime, priceInRupees, complexity, selected, onClick }: DrinkCardProps) {
    const getComplexityColor = () => {
        if (complexity === 'QUICK') return 'bg-status-normal'
        if (complexity === 'MEDIUM') return 'bg-status-warning'
        return 'bg-status-urgent'
    }

    const getComplexityLabel = () => {
        if (complexity === 'QUICK') return 'Quick'
        if (complexity === 'MEDIUM') return 'Medium'
        return 'Complex'
    }

    return (
        <button
            onClick={onClick}
            className={`card transition-all hover:scale-105 ${selected ? 'ring-2 ring-accent-light' : ''
                }`}
        >
            <div className="flex flex-col items-center space-y-3">
                <Coffee className="w-12 h-12 text-accent-light" />
                <h3 className="text-lg font-semibold">{displayName}</h3>
                <div className="flex items-center space-x-2">
                    <span className={`${getComplexityColor()} text-white px-2 py-1 rounded-full text-xs font-semibold`}>
                        {getComplexityLabel()}
                    </span>
                    <span className="text-gray-400 text-sm">{prepTime} min</span>
                </div>
                <span className="text-accent-light font-bold text-xl">₹{priceInRupees}</span>
            </div>
        </button>
    )
}

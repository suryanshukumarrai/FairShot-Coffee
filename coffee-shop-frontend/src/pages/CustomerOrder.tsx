import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import DrinkCard from '../components/DrinkCard'
import { createOrder, fetchMenu } from '../api/coffeeApi'
import { useCoffeeStore } from '../store/coffeeStore'
import { Coffee, AlertCircle } from 'lucide-react'
import type { MenuItem } from '../types'

export default function CustomerOrder() {
    const navigate = useNavigate()
    const { selectedDrinkType, selectedCustomerType, setSelectedDrinkType, setSelectedCustomerType } = useCoffeeStore()
    const [estimatedWait, setEstimatedWait] = useState(0)
    const [isPlacing, setIsPlacing] = useState(false)
    const [menu, setMenu] = useState<MenuItem[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    // Load menu from backend on mount
    useEffect(() => {
        const loadMenu = async () => {
            try {
                setIsLoading(true)
                setError(null)
                const menuData = await fetchMenu()
                setMenu(menuData)
            } catch (err) {
                console.error('Failed to load menu:', err)
                setError('Failed to load menu. Please refresh the page.')
            } finally {
                setIsLoading(false)
            }
        }
        loadMenu()
    }, [])

    // Update estimated wait when drink is selected
    useEffect(() => {
        if (selectedDrinkType && menu.length > 0) {
            const drink = menu.find(d => d.type === selectedDrinkType)
            // Rough estimate: base wait + prep time
            setEstimatedWait(drink ? drink.prepTimeMinutes + 2 : 0)
        }
    }, [selectedDrinkType, menu])

    const handlePlaceOrder = async () => {
        if (!selectedDrinkType) return

        // Validate price is available
        const selectedDrink = menu.find(d => d.type === selectedDrinkType)
        if (!selectedDrink || !selectedDrink.priceInRupees) {
            setError('Price unavailable. Please refresh the page.')
            return
        }

        setIsPlacing(true)
        try {
            const order = await createOrder({
                drinkType: selectedDrinkType,
                customerType: selectedCustomerType,
            })

            // Show success and redirect to order status
            alert(`Order placed successfully! Order #${order.id.substring(0, 8)}`)
            navigate(`/status/${order.id}`)
        } catch (error) {
            console.error('Failed to place order:', error)
            alert('Failed to place order. Please try again.')
        } finally {
            setIsPlacing(false)
        }
    }

    return (
        <div className="max-w-6xl mx-auto p-6">
            <div className="mb-8 text-center">
                <div className="flex items-center justify-center space-x-3 mb-2">
                    <Coffee className="w-10 h-10 text-accent-light" />
                    <h1 className="text-4xl font-bold">Place Your Order</h1>
                </div>
                <p className="text-gray-400">Select your drink and customer type</p>
            </div>

            {/* Error Message */}
            {error && (
                <div className="card bg-status-urgent bg-opacity-20 border-2 border-status-urgent mb-6">
                    <div className="flex items-center space-x-3">
                        <AlertCircle className="w-6 h-6 text-status-urgent" />
                        <p className="text-status-urgent font-semibold">{error}</p>
                    </div>
                </div>
            )}

            {/* Drink Selection */}
            <div className="mb-8">
                <h2 className="text-xl font-semibold mb-4">Choose Your Drink</h2>
                {isLoading ? (
                    <div className="text-center py-12 text-gray-400">
                        <p>Loading menu...</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                        {menu.map((drink) => (
                            <DrinkCard
                                key={drink.type}
                                name={drink.type}
                                displayName={drink.displayName}
                                prepTime={drink.prepTimeMinutes}
                                priceInRupees={drink.priceInRupees}
                                complexity={drink.complexity}
                                selected={selectedDrinkType === drink.type}
                                onClick={() => setSelectedDrinkType(drink.type)}
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Customer Type */}
            <div className="card mb-8">
                <h2 className="text-xl font-semibold mb-4">Customer Type</h2>
                <div className="flex space-x-4">
                    <button
                        className={`flex-1 py-3 px-6 rounded-lg font-semibold transition-colors ${selectedCustomerType === 'REGULAR'
                                ? 'bg-accent-light text-gray-900'
                                : 'bg-bg-tertiary text-white hover:bg-slate-600'
                            }`}
                        onClick={() => setSelectedCustomerType('REGULAR')}
                    >
                        Regular Customer
                    </button>
                    <button
                        className={`flex-1 py-3 px-6 rounded-lg font-semibold transition-colors ${selectedCustomerType === 'VIP'
                                ? 'bg-accent-light text-gray-900'
                                : 'bg-bg-tertiary text-white hover:bg-slate-600'
                            }`}
                        onClick={() => setSelectedCustomerType('VIP')}
                    >
                        VIP Member
                    </button>
                    <button
                        className={`flex-1 py-3 px-6 rounded-lg font-semibold transition-colors ${selectedCustomerType === 'NEW'
                                ? 'bg-accent-light text-gray-900'
                                : 'bg-bg-tertiary text-white hover:bg-slate-600'
                            }`}
                        onClick={() => setSelectedCustomerType('NEW')}
                    >
                        New Customer
                    </button>
                </div>
            </div>

            {/* Estimated Wait */}
            {selectedDrinkType && (
                <div className="card mb-8 bg-accent-coffee bg-opacity-20">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-400 text-sm">Estimated Wait Time</p>
                            <p className="text-3xl font-bold text-accent-light">~{estimatedWait} minutes</p>
                        </div>
                        <div className="text-right text-sm text-gray-400">
                            <p>Prep time: {menu.find(d => d.type === selectedDrinkType)?.prepTimeMinutes} min</p>
                            <p>Queue wait: ~2 min</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Place Order Button */}
            <button
                onClick={handlePlaceOrder}
                disabled={!selectedDrinkType || isPlacing || isLoading || error !== null}
                className="w-full btn-primary text-lg py-4"
            >
                {isPlacing ? 'Placing Order...' : 'Place Order'}
            </button>

            {/* Fairness Info */}
            <div className="mt-6 p-4 bg-blue-900 bg-opacity-20 rounded-lg text-blue-200 text-sm">
                <p className="font-semibold mb-1">ℹ️ How our queue works:</p>
                <p>Quick orders (1-2 min) may be served before longer orders to reduce overall wait time for everyone.</p>
            </div>
        </div>
    )
}

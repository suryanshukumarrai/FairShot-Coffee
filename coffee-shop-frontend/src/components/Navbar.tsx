import { NavLink } from 'react-router-dom'
import { Coffee, ListOrdered, Tv, User, Settings, Zap } from 'lucide-react'
import { useCoffeeStore } from '../store/coffeeStore'

export default function Navbar() {
    const currentRole = useCoffeeStore(state => state.currentRole)

    const navItems = [
        { to: '/order', icon: Coffee, label: 'Order', roles: ['customer'] },
        { to: '/status', icon: ListOrdered, label: 'My Order', roles: ['customer'] },
        { to: '/public-queue', icon: Tv, label: 'Queue Display', roles: ['public', 'customer', 'barista', 'manager'] },
        { to: '/barista', icon: User, label: 'Barista', roles: ['barista', 'manager'] },
        { to: '/manager', icon: Settings, label: 'Manager', roles: ['manager'] },
        { to: '/simulation', icon: Zap, label: 'Simulation', roles: ['manager'] },
    ]

    const filteredItems = navItems.filter(item => item.roles.includes(currentRole))

    return (
        <nav className="bg-bg-secondary border-b border-bg-tertiary">
            <div className="max-w-7xl mx-auto px-4">
                <div className="flex items-center justify-between h-16">
                    <div className="flex items-center space-x-2">
                        <Coffee className="w-8 h-8 text-accent-light" />
                        <span className="text-xl font-bold">Coffee Shop</span>
                    </div>

                    <div className="flex space-x-1">
                        {filteredItems.map((item) => {
                            const Icon = item.icon
                            return (
                                <NavLink
                                    key={item.to}
                                    to={item.to}
                                    className={({ isActive }) =>
                                        `flex items-center space-x-2 px-4 py-2 rounded-lg transition-colors ${isActive
                                            ? 'bg-accent-light text-gray-900'
                                            : 'text-gray-300 hover:bg-bg-tertiary hover:text-white'
                                        }`
                                    }
                                >
                                    <Icon className="w-5 h-5" />
                                    <span className="font-medium">{item.label}</span>
                                </NavLink>
                            )
                        })}
                    </div>

                    {/* Role Switcher for Demo */}
                    <div className="flex items-center space-x-2">
                        <select
                            value={currentRole}
                            onChange={(e) => useCoffeeStore.getState().setCurrentRole(e.target.value as any)}
                            className="bg-bg-tertiary text-white px-3 py-1 rounded-lg text-sm border border-bg-tertiary focus:border-accent-light focus:outline-none"
                        >
                            <option value="customer">Customer</option>
                            <option value="barista">Barista</option>
                            <option value="manager">Manager</option>
                            <option value="public">Public</option>
                        </select>
                    </div>
                </div>
            </div>
        </nav>
    )
}

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import CustomerOrder from './pages/CustomerOrder'
import OrderStatus from './pages/OrderStatus'
import PublicQueue from './pages/PublicQueue'
import BaristaDashboard from './pages/BaristaDashboard'
import ManagerDashboard from './pages/ManagerDashboard'
import SimulationPage from './pages/SimulationPage'

function App() {
    return (
        <BrowserRouter>
            <div className="min-h-screen bg-bg-primary text-white">
                <Navbar />
                <Routes>
                    <Route path="/" element={<Navigate to="/order" replace />} />
                    <Route path="/order" element={<CustomerOrder />} />
                    <Route path="/status" element={<OrderStatus />} />
                    <Route path="/status/:orderId" element={<OrderStatus />} />
                    <Route path="/public-queue" element={<PublicQueue />} />
                    <Route path="/barista" element={<BaristaDashboard />} />
                    <Route path="/manager" element={<ManagerDashboard />} />
                    <Route path="/simulation" element={<SimulationPage />} />
                </Routes>
            </div>
        </BrowserRouter>
    )
}

export default App

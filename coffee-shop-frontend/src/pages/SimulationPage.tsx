import { useState } from 'react'
import { runSimulation, resetSystem } from '../api/coffeeApi'
import { Play, Zap, TrendingUp, AlertCircle, RefreshCw } from 'lucide-react'
import type { SimulationResponse } from '../types'

export default function SimulationPage() {
    const [loading, setLoading] = useState(false)
    const [resetting, setResetting] = useState(false)
    const [result, setResult] = useState<SimulationResponse | null>(null)
    const [error, setError] = useState<string | null>(null)

    const runSim = async (orderCount: number) => {
        setLoading(true)
        setError(null)
        try {
            const response = await runSimulation({ orders: orderCount, random: true })
            setResult(response)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Simulation failed')
            console.error('Simulation error:', err)
        } finally {
            setLoading(false)
        }
    }

    const handleReset = async () => {
        if (!confirm('Reset will clear ALL orders, barista assignments, and scheduled completions. Continue?')) {
            return
        }

        setResetting(true)
        setError(null)
        try {
            await resetSystem()
            setResult(null)
            alert('✅ System reset successfully! All orders cleared, baristas available.')
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Reset failed')
            console.error('Reset error:', err)
        } finally {
            setResetting(false)
        }
    }

    return (
        <div className="max-w-7xl mx-auto p-6">
            {/* Header */}
            <div className="mb-8">
                <div className="flex items-center space-x-3 mb-2">
                    <Zap className="w-10 h-10 text-accent-light" />
                    <h1 className="text-4xl font-bold">Simulation / Cheat Mode</h1>
                </div>
                <p className="text-gray-400">
                    Instantly demo system behavior under heavy load. Orders complete immediately without waiting.
                </p>
            </div>

            {/* Warning Banner */}
            <div className="card bg-amber-900 bg-opacity-20 border-2 border-accent-light mb-8">
                <div className="flex items-start space-x-3">
                    <AlertCircle className="w-6 h-6 text-accent-light mt-1" />
                    <div>
                        <h3 className="text-xl font-bold text-accent-light mb-2">Demo Mode Only</h3>
                        <p className="text-gray-300">
                            This feature is for demonstration and analysis. Orders are processed instantly to showcase
                            scalability, fairness, SLA handling, and system behavior under load.
                        </p>
                    </div>
                </div>
            </div>

            {/* Simulation Controls */}
            <div className="card mb-8">
                <h2 className="text-2xl font-bold mb-6">Run Simulation</h2>
                <div className="grid grid-cols-4 gap-4">
                    <button
                        onClick={() => runSim(100)}
                        disabled={loading || resetting}
                        className="bg-green-600 text-white px-8 py-6 rounded-lg text-xl font-bold hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-3"
                    >
                        <Play className="w-6 h-6" />
                        <span>100 Orders</span>
                    </button>
                    <button
                        onClick={() => runSim(150)}
                        disabled={loading || resetting}
                        className="bg-blue-600 text-white px-8 py-6 rounded-lg text-xl font-bold hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-3"
                    >
                        <Play className="w-6 h-6" />
                        <span>150 Orders</span>
                    </button>
                    <button
                        onClick={() => runSim(200)}
                        disabled={loading || resetting}
                        className="bg-purple-600 text-white px-8 py-6 rounded-lg text-xl font-bold hover:bg-purple-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-3"
                    >
                        <Play className="w-6 h-6" />
                        <span>200 Orders</span>
                    </button>
                    <button
                        onClick={handleReset}
                        disabled={loading || resetting}
                        className="bg-status-urgent text-white px-8 py-6 rounded-lg text-xl font-bold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-3"
                    >
                        <RefreshCw className={`w-6 h-6 ${resetting ? 'animate-spin' : ''}`} />
                        <span>Reset</span>
                    </button>
                </div>
                {(loading || resetting) && (
                    <div className="mt-6 text-center">
                        <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-accent-light"></div>
                        <p className="mt-4 text-lg text-gray-400">
                            {loading ? 'Processing simulation...' : 'Resetting system...'}
                        </p>
                    </div>
                )}
            </div>

            {/* Error Display */}
            {error && (
                <div className="card bg-status-urgent bg-opacity-20 border-2 border-status-urgent mb-8">
                    <h3 className="text-xl font-bold text-status-urgent mb-2">Error</h3>
                    <p className="text-white">{error}</p>
                </div>
            )}

            {/* Results */}
            {result && !loading && (
                <div className="space-y-8">
                    {/* Metrics Summary */}
                    <div className="card">
                        <div className="flex items-center space-x-3 mb-6">
                            <TrendingUp className="w-6 h-6 text-accent-light" />
                            <h2 className="text-2xl font-bold">Simulation Results</h2>
                        </div>
                        <div className="grid grid-cols-5 gap-4">
                            <div className="text-center p-4 bg-bg-tertiary rounded-lg">
                                <p className="text-3xl font-bold text-status-normal">{result.ordersProcessed}</p>
                                <p className="text-sm text-gray-400 mt-1">Orders Processed</p>
                            </div>
                            <div className="text-center p-4 bg-bg-tertiary rounded-lg">
                                <p className="text-3xl font-bold text-blue-500">{result.maxQueueSize}</p>
                                <p className="text-sm text-gray-400 mt-1">Max Queue Size</p>
                            </div>
                            <div className="text-center p-4 bg-bg-tertiary rounded-lg">
                                <p className="text-3xl font-bold text-accent-light">{result.avgWaitMinutes.toFixed(1)}</p>
                                <p className="text-sm text-gray-400 mt-1">Avg Wait (min)</p>
                            </div>
                            <div className="text-center p-4 bg-bg-tertiary rounded-lg">
                                <p className="text-3xl font-bold text-status-warning">{result.fairnessSkips}</p>
                                <p className="text-sm text-gray-400 mt-1">Fairness Skips</p>
                            </div>
                            <div className="text-center p-4 bg-bg-tertiary rounded-lg">
                                <p className={`text-3xl font-bold ${result.slaBreaches > 0 ? 'text-status-urgent' : 'text-status-normal'}`}>
                                    {result.slaBreaches}
                                </p>
                                <p className="text-sm text-gray-400 mt-1">SLA Breaches</p>
                            </div>
                        </div>
                        <div className="mt-4 pt-4 border-t border-bg-tertiary text-center text-sm text-gray-400">
                            Completed in {result.executionTimeMs} ms
                        </div>
                    </div>

                    {/* Barista Workload */}
                    <div className="card">
                        <h2 className="text-2xl font-bold mb-6">Barista Workload Distribution</h2>
                        <div className="space-y-4">
                            {Object.entries(result.baristaWorkload).map(([baristaId, workload]) => {
                                const maxWorkload = Math.max(...Object.values(result.baristaWorkload))
                                const percentage = (workload / maxWorkload) * 100
                                return (
                                    <div key={baristaId}>
                                        <div className="flex items-center justify-between mb-2">
                                            <span className="font-semibold">Barista {baristaId}</span>
                                            <span className="text-gray-400">{workload} min</span>
                                        </div>
                                        <div className="w-full bg-bg-tertiary rounded-full h-4">
                                            <div
                                                className="h-4 rounded-full bg-accent-light"
                                                style={{ width: `${percentage}%` }}
                                            />
                                        </div>
                                    </div>
                                )
                            })}
                        </div>
                    </div>

                    {/* Transparency Messages */}
                    {result.transparencyMessages.length > 0 && (
                        <div className="card">
                            <h2 className="text-2xl font-bold mb-4">System Behavior Insights</h2>
                            <div className="space-y-2">
                                {result.transparencyMessages.map((message, index) => (
                                    <div key={index} className="p-3 bg-bg-tertiary rounded text-sm flex items-start space-x-2">
                                        <span>{message}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    )
}

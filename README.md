# ☕ FairShot Coffee - Intelligent Queue Management System

A production-ready coffee shop queue management system featuring intelligent order prioritization, real-time barista assignment, and comprehensive analytics. Built with Spring Boot and React.

**🌐 [LIVE DEMO](https://fairshot-coffee.onrender.com)** - Visit the application now!

![Java](https://img.shields.io/badge/Java-21-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)
![Deployed](https://img.shields.io/badge/Deployed%20on-Render-46E3B7?logo=render)

## 🌟 Features

### Core Functionality
- **Intelligent Order Prioritization**: Dynamic priority scoring based on wait time, customer type (VIP/Regular/New), drink complexity, and fairness penalties
- **Automated Barista Assignment**: Orders automatically assigned to available baristas with workload balancing
- **Real-time State Management**: Live updates across customer, barista, and manager dashboards
- **Fairness System**: Prevents order starvation with skip tracking and priority boosting
- **SLA Monitoring**: Tracks orders exceeding 8-minute wait time threshold

### User Roles & Dashboards

#### 👤 Customer Portal
- Browse menu with INR pricing fetched from backend
- Place orders with drink selection and customer type
- Real-time order status tracking
- Estimated wait time display

#### ☕ Barista Dashboard
- View current assigned order with preparation status
- Preview next order in queue
- Automatic order start on assignment
- Workload tracking (Light/Medium/Heavy)

#### 📊 Manager Dashboard
- Real-time analytics: total coffees served, SLA violations, average wait time
- Barista utilization metrics with visual progress bars
- Currently preparing orders overview
- Coffees served breakdown by drink type
- Emergency alerts for orders exceeding thresholds

#### 🎮 Simulation Mode
- Instant order generation (100/150/200 orders)
- Demo mode with immediate order completion
- System reset functionality

#### 📺 Public Queue Display
- Live order queue with wait times
- Real-time barista status
- System capacity monitoring

## 🏗️ Architecture

### Backend (Spring Boot)
```
src/main/java/com/coffeeShop/Coffee/Shop/
├── controller/          # REST API endpoints
│   ├── BaristaController.java
│   ├── ComplaintController.java
│   ├── ManagerController.java
│   ├── MenuController.java
│   └── OrderController.java
├── service/            # Business logic
│   ├── BaristaAssignmentService.java
│   ├── OrderCompletionService.java
│   ├── QueueSchedulerService.java
│   ├── PriorityScoreService.java
│   ├── FairnessTrackerService.java
│   └── ManagerMetricsService.java
├── model/              # Domain models
│   ├── Order.java
│   ├── Barista.java
│   ├── DrinkType.java
│   └── CustomerType.java
├── dto/                # Data Transfer Objects
└── util/               # Mappers and utilities
```

### Frontend (React + TypeScript)
```
coffee-shop-frontend/src/
├── pages/              # Route components
│   ├── CustomerOrder.tsx
│   ├── OrderStatus.tsx
│   ├── BaristaDashboard.tsx
│   ├── ManagerDashboard.tsx
│   ├── PublicQueue.tsx
│   └── SimulationPage.tsx
├── components/         # Reusable components
│   ├── DrinkCard.tsx
│   ├── OrderCard.tsx
│   ├── BaristaCard.tsx
│   └── Navbar.tsx
├── api/               # API client
├── store/             # Zustand state management
└── types/             # TypeScript types
```

## 🛠️ Technology Stack

### Backend
- **Java 21**: Modern Java features with pattern matching and records
- **Spring Boot 4.0.2**: Framework for building production-ready applications
- **Maven**: Dependency management and build automation
- **Spring TaskScheduler**: Scheduled order completion and assignment
- **Concurrent Collections**: Thread-safe queue and barista management

### Frontend
- **React 18**: Modern UI library with hooks
- **TypeScript 5**: Type-safe JavaScript
- **Vite 6.4.1**: Fast build tool and dev server
- **Zustand**: Lightweight state management
- **Tailwind CSS**: Utility-first CSS framework
- **Axios**: HTTP client for API calls
- **React Router**: Client-side routing

## 📋 Prerequisites

### Backend
- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** (Included via Maven Wrapper)

### Frontend
- **Node.js 18+** ([Download](https://nodejs.org/))
- **npm 9+** (comes with Node.js)

## 🚀 Getting Started

### Try the Live Demo
**No setup required!** Visit the application at: **[https://fairshot-coffee.onrender.com](https://fairshot-coffee.onrender.com)**

Explore all features:
- 👤 Place orders via [Customer Portal](https://fairshot-coffee.onrender.com/order)
- ☕ Monitor via [Barista Dashboard](https://fairshot-coffee.onrender.com/barista)
- 📊 Analyze with [Manager Dashboard](https://fairshot-coffee.onrender.com/manager)
- 🎮 Run [Simulations](https://fairshot-coffee.onrender.com/simulation)
- 📺 View [Public Queue](https://fairshot-coffee.onrender.com/public-queue)

### Local Development

Or clone and run locally:
```bash
git clone https://github.com/suryanshukumarrai/FairShot-Coffee.git
cd FairShot-Coffee
```

### 2. Backend Setup

#### Configure Application (Optional)
Edit `src/main/resources/application.properties` if needed:
```properties
server.port=8080
logging.level.com.coffeeShop=DEBUG
```

#### Build and Run
```bash
# Clean and compile
./mvnw clean compile

# Run Spring Boot application
./mvnw spring-boot:run
```

Backend will start on **http://localhost:8080**

#### Verify Backend
```bash
curl http://localhost:8080/api/menu
```

### 3. Frontend Setup

```bash
cd coffee-shop-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start on **http://localhost:5173**

#### Build for Production
```bash
npm run build
```

## 📡 API Endpoints

### Menu API

#### `GET /api/menu`
Get all available drinks with prices and metadata.

**Response:**
```json
[
  {
    "type": "COLD_BREW",
    "displayName": "Cold Brew",
    "prepTimeMinutes": 1,
    "priceInRupees": 120,
    "complexity": "QUICK"
  },
  {
    "type": "ESPRESSO",
    "displayName": "Espresso",
    "prepTimeMinutes": 2,
    "priceInRupees": 150,
    "complexity": "QUICK"
  }
  // ... more drinks
]
```

### Order Management

#### `GET /api/orders`
Get all orders with their current status.

**Response:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "drinkType": {
      "name": "ESPRESSO",
      "displayName": "Espresso",
      "prepTimeMinutes": 2
    },
    "customerType": {
      "name": "REGULAR",
      "displayName": "Regular"
    },
    "status": "IN_PROGRESS",
    "arrivalTime": "2026-02-07T10:00:00Z",
    "startTime": "2026-02-07T10:02:00Z",
    "assignedBaristaId": 1,
    "waitTimeMinutes": 2.5,
    "priceInRupees": 150,
    "skipCount": 0,
    "explanation": null
  }
]
```

#### `POST /api/orders`
Create a new order.

**Request:**
```json
{
  "drinkType": "CAPPUCCINO",
  "customerType": "VIP"
}
```

**Response:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "PENDING",
  "arrivalTime": "2026-02-07T10:05:00Z",
  "priceInRupees": 180,
  // ... other fields
}
```

#### `GET /api/orders/{id}`
Get details of a specific order.

**Response:** Same as single order object above.

### Barista Management

#### `GET /api/baristas`
Get all baristas with their current status.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Barista 1",
    "available": false,
    "busy": true,
    "totalWorkedMinutes": 45,
    "remainingMinutes": 3,
    "busyUntil": "2026-02-07T10:10:00Z",
    "currentOrder": {
      "id": "...",
      "drinkType": { ... },
      "status": "IN_PROGRESS"
    }
  }
]
```

#### `GET /api/baristas/{id}`
Get details of a specific barista.

**Response:** Same as single barista object above.

### Manager Analytics

#### `GET /api/manager/metrics`
Get comprehensive system metrics.

**Response:**
```json
{
  "totalCoffeesServed": 42,
  "byDrink": {
    "ESPRESSO": 15,
    "CAPPUCCINO": 12,
    "LATTE": 8,
    "MOCHA": 7
  },
  "avgWaitMinutes": 4.2,
  "slaViolations": 2,
  "baristaUtilization": {
    "1": 75.5,
    "2": 68.3,
    "3": 82.1
  }
}
```

#### `POST /api/manager/simulate`
Run simulation mode with instant order generation.

**Request:**
```json
{
  "orders": 100,
  "random": true
}
```

**Response:**
```json
{
  "ordersProcessed": 100,
  "maxQueueSize": 15,
  "avgWaitMinutes": 3.8,
  "fairnessSkips": 23,
  "slaBreaches": 5,
  "baristaWorkload": {
    "1": 120,
    "2": 115,
    "3": 125
  },
  "executionTimeMs": 8500,
  "transparencyMessages": [
    "Order prioritization active",
    "Workload balanced across baristas"
  ]
}
```

#### `POST /api/manager/reset`
Reset entire system state (orders, baristas, metrics).

**Response:** `200 OK`

### Complaint Management

#### `GET /api/complaints`
Get all customer complaints.

#### `POST /api/complaints`
Submit a new complaint.

**Request:**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Order taking too long"
}
```

## 🎯 Priority Scoring Algorithm

Orders are prioritized using a sophisticated scoring system:

### Base Score Calculation
```
priorityScore = (waitTime × 10) + customerTypeMultiplier + speedBonus - fairnessPenalty
```

### Components:
1. **Wait Time**: Primary factor (10 points per minute)
2. **Customer Type**: 
   - VIP: +20 points
   - New: +10 points
   - Regular: +5 points
3. **Speed Bonus**: Quick drinks (<3 min) get +10 points
4. **Fairness Penalty**: Orders skipped 3+ times get massive priority boost

### Emergency Override
Orders waiting ≥8 minutes automatically move to front of queue regardless of other factors.

## 📊 Drink Menu

| Drink | Prep Time | Price (₹) | Complexity |
|-------|-----------|-----------|------------|
| Cold Brew | 1 min | ₹120 | QUICK |
| Espresso | 2 min | ₹150 | QUICK |
| Americano | 2 min | ₹140 | QUICK |
| Cappuccino | 4 min | ₹180 | MEDIUM |
| Latte | 4 min | ₹200 | MEDIUM |
| Mocha | 6 min | ₹250 | COMPLEX |

## 🔄 System Workflow

1. **Customer Places Order** → Order enters PENDING queue
2. **Priority Calculation** → Real-time scoring based on multiple factors
3. **Barista Assignment** → Automatic assignment to available barista
4. **Order Status: IN_PROGRESS** → Barista prepares drink
5. **Scheduled Completion** → Order completes after prep time
6. **Metrics Update** → Statistics updated, barista freed
7. **Next Assignment** → System immediately assigns next order

## 🧪 Testing the System

### Try the Live Demo
**Visit:** **[https://fairshot-coffee.onrender.com](https://fairshot-coffee.onrender.com)**

No setup required - test all features in real-time!

### Quick Test Flow (Local Development)

1. **Start Both Servers** (Backend: 8080, Frontend: 5173)

2. **Place Orders via Customer Portal**:
   - Live: https://fairshot-coffee.onrender.com/order
   - Local: http://localhost:5173/order
   - Select drinks and place multiple orders

3. **Monitor Barista Dashboard**:
   - Live: https://fairshot-coffee.onrender.com/barista
   - Local: http://localhost:5173/barista
   - Watch auto-assignment and preparation

4. **Check Manager Analytics**:
   - Live: https://fairshot-coffee.onrender.com/manager
   - Local: http://localhost:5173/manager
   - View real-time metrics and utilization

5. **Run Simulation**:
   - Live: https://fairshot-coffee.onrender.com/simulation
   - Local: http://localhost:5173/simulation
   - Test with 100/150/200 orders

6. **View Public Queue**:
   - Live: https://fairshot-coffee.onrender.com/public-queue
   - Local: http://localhost:5173/queue
   - See live queue status

### API Testing with cURL

```bash
# Get menu
curl http://localhost:8080/api/menu

# Place order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"drinkType":"ESPRESSO","customerType":"REGULAR"}'

# Get all orders
curl http://localhost:8080/api/orders

# Get barista status
curl http://localhost:8080/api/baristas

# Get metrics
curl http://localhost:8080/api/manager/metrics

# Run simulation
curl -X POST http://localhost:8080/api/manager/simulate \
  -H "Content-Type: application/json" \
  -d '{"orders":100,"random":true}'

# Reset system
curl -X POST http://localhost:8080/api/manager/reset
```

## 🐛 Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9

# Or change port in application.properties
server.port=8081
```

**Maven build fails:**
```bash
# Clean and rebuild
./mvnw clean install -DskipTests
```

### Frontend Issues

**Port 5173 already in use:**
```bash
# Kill process
lsof -ti:5173 | xargs kill -9
```

**Dependencies not installing:**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Build errors:**
```bash
# Clear Vite cache
rm -rf .vite dist
npm run build
```

## 📁 Project Structure

```
Coffee-Shop/
├── src/main/java/                    # Backend source code
├── src/main/resources/               # Application config
├── coffee-shop-frontend/             # Frontend React app
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
├── pom.xml                           # Maven configuration
├── mvnw                              # Maven wrapper
└── README.md                         # This file
```

## 🔐 CORS Configuration

The backend allows cross-origin requests from the frontend. CORS is configured in `WebConfig.java`:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

## 🎨 Features Highlight

### ✅ State Management
- **Single Source of Truth**: All state maintained in backend
- **Atomic Operations**: Orders transition atomically through lifecycle
- **Idempotent Operations**: Safe to retry without side effects

### ✅ Real-time Updates
- **Polling-based**: Frontend polls every 1-3 seconds based on role
- **Optimistic Updates**: Immediate feedback on user actions
- **Automatic Refresh**: No manual refresh needed

### ✅ Error Handling
- **Graceful Degradation**: System continues operating if non-critical errors occur
- **User Feedback**: Clear error messages displayed to users
- **Logging**: Comprehensive DEBUG/INFO/WARN logs for troubleshooting

### ✅ Fairness System
- **Skip Tracking**: Counts how many times an order is skipped
- **Priority Boosting**: Orders skipped 3+ times get massive priority boost
- **Explanations**: Users see why their order was delayed

## 🚀 Deployment

### Live on Render
The application is deployed and live at **[https://fairshot-coffee.onrender.com](https://fairshot-coffee.onrender.com)** on Render.

**Deployment Details:**
- **Platform:** Render (Free Tier)
- **Backend:** Spring Boot on Java 21 (Port 8080)
- **Frontend:** React + Vite (Served as static files)
- **Architecture:** Single Docker container with multi-stage build
- **Auto-Deploy:** Enabled on main branch pushes

### Local Deployment

#### Backend (Spring Boot)
```bash
# Package as JAR
./mvnw clean package

# Run JAR locally
java -Xms128m -Xmx384m -jar target/Coffee-Shop-0.0.1-SNAPSHOT.jar
```

#### Frontend (React)
```bash
cd coffee-shop-frontend

# Build production bundle
npm run build

# Serve with any static server
npx serve -s dist -l 3000
```

### Deployment Configuration
- **Docker Image:** `eclipse-temurin:21-jre-alpine` + Node.js 20
- **Memory Limits:** `-Xms128m -Xmx384m` (optimized for 512MB free tier)
- **Health Check:** `/actuator/health` endpoint
- **Logging:** Production-optimized (INFO level)
- **CORS:** Configured for `https://*.onrender.com`

### Platform-Specific Information
- **Render:** Auto-builds from Dockerfile, includes both frontend and backend
- **Environment Variables:** `PORT` (auto-set by Render)
- **Logs:** Accessible via Render dashboard
- **Auto-Restart:** On health check failures

---

### Backend (Spring Boot) - OLD
```bash
# Package as JAR
./mvnw clean package

# Run JAR
java -jar target/Coffee-Shop-0.0.1-SNAPSHOT.jar
```

### Frontend (React) - OLD

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

**Suryansh Kumar Rai**
- GitHub: [@suryanshukumarrai](https://github.com/suryanshukumarrai)
- Repository: [FairShot-Coffee](https://github.com/suryanshukumarrai/FairShot-Coffee)

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

If you encounter any issues or have questions:
1. Check the [Troubleshooting](#-troubleshooting) section
2. Open an issue on GitHub
3. Review API documentation above

---

**Built with ☕ and ❤️ for efficient coffee shop management**

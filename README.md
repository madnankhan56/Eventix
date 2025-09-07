# Eventix Android App

A modern Android application built with **Clean Architecture** principles, showcasing events with a focus on reactive programming using **Kotlin Flows** and **Jetpack Compose**.

## 🏗️ Architecture Overview

This project implements **Clean Architecture** with clear separation of concerns across multiple layers, ensuring maintainability, testability, and scalability.

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│         📱 PRESENTATION LAYER           │
│                                         │
│  • Compose UI (EventsScreen)           │
│  • ViewModel (StateFlow)               │
│  • collectAsStateWithLifecycle()       │
└─────────────────────────────────────────┘
                    │
                    │ Kotlin Flow
                    ▼
┌─────────────────────────────────────────┐
│          🧠 DOMAIN LAYER                │
│                                         │
│  • Use Cases (BrowseEvents)            │
│  • Domain Models (Event, Venue)        │
│  • .map() .filter() .catch()           │
└─────────────────────────────────────────┘
                    │
                    │ Kotlin Flow
                    ▼
┌─────────────────────────────────────────┐
│           🗄️ DATA LAYER                 │
│                                         │
│  • Repository (EventRepository)        │
│  • Network Models & Mapper             │
│  • flow { emit() } .catch()             │
└─────────────────────────────────────────┘
                    │
                    │ Retrofit
                    ▼
           🌐 TICKETMASTER API
```

### Kotlin Flow Data Pipeline

```
Repository        UseCase         ViewModel         UI
    │                │                │             │
flow {            .map()         .transform()   .collectAs
emit()            .catch()       .stateIn()     StateWith
}                 .filter()      StateFlow      Lifecycle()

Flow<ResultState> → Flow<Event> → StateFlow<UiState> → @Composable
```

### Flow Benefits in Architecture

```
🔄 Reactive Updates    ⚡ Async Operations    🧠 Memory Efficient
🎯 Backpressure       🛡️ Error Handling     📱 Lifecycle Aware
```

## 🌊 Unidirectional Data Flow (UDF) Implementation

The EventViewModel demonstrates perfect UDF architecture using Kotlin flows:

```
┌─────────────────────────────────────────────────────────────────┐
│                         UDF FLOW DIAGRAM                       │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐    STATE FLOWS DOWN (Read-Only)    ┌─────────────────┐
│                  │ ──────────────────────────────────► │                 │
│   EventViewModel │                                     │   Compose UI    │
│                  │ ◄────────────────────────────────── │                 │
└──────────────────┘     EVENTS FLOW UP (Actions)       └─────────────────┘

           │                                                        ▲
           │ Single Source of Truth                                 │
           ▼                                                        │

┌─────────────────────────────────────────────────────────────────────────────┐
│                           VIEWMODEL INTERNALS                              │
│                                                                             │
│  ┌─────────────────────┐                                                    │
│  │  eventsQuerySignal  │ ◄──── emit(EventQuery) ◄──── User Actions         │
│  │ MutableStateFlow    │                              • onLoadNextPage()    │
│  │ EventQuery(0, null) │                              • onSearch(keyword)   │
│  └─────────────────────┘                                                    │
│             │                                                               │
│             │ transform { }                                                 │
│             ▼                                                               │
│  ┌─────────────────────┐                                                    │
│  │ createEventUiState  │ ──── browseEventsUseCase ───► Repository/Network   │
│  │     Stream()        │                                                    │
│  └─────────────────────┘                                                    │
│             │                                                               │
│             │ map { result -> }                                             │
│             ▼                                                               │
│  ┌─────────────────────┐                                                    │
│  │ eventsScreenUiState │ ──────────────────────────────► UI Observes       │
│  │    StateFlow        │           (Read-Only)                             │
│  │ EventsScreenUiState │                                                    │
│  └─────────────────────┘                                                    │
└─────────────────────────────────────────────────────────────────────────────┘

                              DATA FLOW CYCLE
    ┌─────────────────────────────────────────────────────────────────┐
    │                                                                 │
    │  1. User Action (search/loadMore)                              │
    │  2. Event flows UP → emit(EventQuery)                         │
    │  3. MutableStateFlow triggers transform                        │
    │  4. UseCase fetches data                                       │
    │  5. State flows DOWN → UI receives new EventsScreenUiState    │
    │  6. UI recomposes with new state                              │
    │                                                                │
    └─────────────────────────────────────────────────────────────────┘
```

### **UDF Benefits with koltin flow:**
- 🔒 **Compiler-enforced unidirectional flow**
- 🔄 **Reactive state propagation** 
- 🧵 **Thread-safe concurrent updates**
- 📱 **Lifecycle-aware subscriptions**
- 🎯 **Single source of truth**

## 📦 Project Structure

```
com.tech.eventix/
├── 🎨 ui/                          # Presentation Layer
│   ├── EventsScreen.kt             # Compose UI
│   └── theme/                      # UI theming
├── 🧠 viewmodel/                   # Presentation Layer
│   └── EventViewModel.kt           # State management
├── 📊 uistate/                     # Presentation Layer
│   ├── EventsScreenUiState.kt      # Screen states
│   └── EventUiState.kt             # UI models
├── 🎯 usecase/                     # Domain Layer
│   └── GetEventsUseCase.kt         # Business logic
├── 🏛️ domain/                      # Domain Layer
│   ├── Event.kt                    # Domain entities
│   └── Venue.kt
├── 🗄️ repository/                  # Data Layer
│   ├── EventRepository.kt          # Repository interface
│   ├── EventRepositoryImpl.kt      # Repository implementation
│   └── NetworkEventMapper.kt       # Data mapping
├── 🌐 api/                         # Data Layer
│   ├── RemoteDataSource.kt         # Network calls
│   └── model/                      # Network models
├── 🔧 di/                          # Dependency Injection
│   └── AppModule.kt                # Hilt modules
└── 🛠️ utils/                       # Shared utilities
    └── ResultState.kt              # Result wrapper
```

## 🏛️ Clean Architecture Implementation

### 1. **Presentation Layer** 
Handles UI logic and user interactions

#### **Components:**
- **ViewModels**: Manage UI state using StateFlow
- **UI States**: Sealed classes representing different screen states (Loading, Success, Error)
- **Compose UI**: Declarative UI with state-driven rendering

### 2. **Domain Layer**
Contains business logic and domain entities

#### **Responsibilities:**
- ✅ Event validation and filtering
- 📅 Date and time formatting
- 🔄 Chronological sorting
- 🚫 Test event exclusion

#### **Components:**
- **Entities**: Pure domain models (Event, Venue)
- **Use Cases**: Business logic implementation
- **Repository Interfaces**: Abstraction for data access

### 3. **Data Layer**
Manages data sources and implements repository pattern

#### **Components:**
- **Repository Implementation**: Concrete data access
- **Data Sources**: Network API integration
- **Mappers**: Convert between network and domain models
- **Network Models**: API response structures

## 🌊 Reactive Programming with Flows

### **Flow Usage Throughout the Architecture**

#### **Data Flow Pipeline:**
```
Network API → Repository → Use Case → ViewModel → UI
     ↓           ↓          ↓          ↓        ↓
   Flow      Flow       Flow      StateFlow  Collect
```

#### **Key Flow Operations:**
1. **Repository Layer**: Emits data with `flow { }` builder
2. **Use Case Layer**: Transforms data with `.map()` operations
3. **ViewModel Layer**: Exposes UI state via `StateFlow`
4. **UI Layer**: Collects state with `collectAsStateWithLifecycle()`

### **Flow Benefits in This Architecture**

| Benefit | Implementation |
|---------|----------------|
| **🔄 Reactive Updates** | UI automatically updates when data changes |
| **⚡ Asynchronous** | Non-blocking data operations |
| **🎯 Backpressure Handling** | Automatic flow control |
| **🧠 Memory Efficient** | Lazy evaluation and cancellation |
| **🔍 Composable** | Easy to combine and transform |

## 🛡️ Error Handling

### **ResultState Pattern**
Wraps all data operations in a sealed class hierarchy:
- `ResultState.Loading` - Initial and loading states
- `ResultState.Success<T>` - Successful data with payload
- `ResultState.Error` - Error state with exception details

### **Error Propagation Flow**
```
Network Error → Repository → Use Case → ViewModel → UI
     ↓             ↓          ↓          ↓        ↓
  Exception → ResultState.Error → Preserved → UI Error State
```

## 🎨 UI Architecture (Jetpack Compose)

### **State-Driven UI**
- **Single Source of Truth**: All UI state managed centrally
- **Unidirectional Data Flow**: State flows down, events flow up
- **Reactive Updates**: Automatic recomposition on state changes


### **Compose Best Practices**
- ✅ **Stateless Composables** for better reusability
- ✅ **State Hoisting** for better testability
- ✅ **Preview Support** for UI development
- ✅ **Lifecycle Awareness** with proper state collection

## 🔧 Dependency Injection (Hilt)

### **DI Strategy**
- **Module Organization**: Centralized dependency configuration
- **Scope Management**: Singleton for repositories, scoped for ViewModels
- **Interface Abstraction**: Easy testing and implementation swapping

## 📱 Features

- 📋 **Event Listing**: Display events with filtering and sorting
- 🖼️ **Image Loading**: Async image loading with Coil
- 🎨 **Material Design 3**: Modern UI with Material You
- 🔄 **Reactive UI**: Real-time updates with Flows
- ⚡ **Performance**: Lazy loading and efficient rendering

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **🏗️ Architecture** | Clean Architecture, MVVM |
| **🌊 Reactive** | Kotlin Flows, Coroutines |
| **🎨 UI** | Jetpack Compose, Material Design 3 |
| **🔧 DI** | Hilt |
| **🌐 Network** | (Ready for Retrofit/OkHttp integration) |
| **🖼️ Images** | Coil |
| **📱 Android** | ViewModels, StateFlow, Lifecycle |

## 🧪 Testing Strategy

### **Testable Architecture Benefits**
- **🎯 Use Cases**: Easy unit testing of business logic
- **🗄️ Repository**: Mockable data layer
- **🧠 ViewModels**: Testable state management
- **🎨 UI**: Composable testing with preview support

## 🚀 Getting Started

1. **Clone the repository**
2. **Open in Android Studio**
3. **Build and run** the project
4. **Explore the architecture** layers

## 🎯 Architecture Benefits

- ✅ **Separation of Concerns**: Each layer has a single responsibility
- ✅ **Testability**: Easy to unit test each layer independently
- ✅ **Maintainability**: Changes are isolated to specific layers
- ✅ **Scalability**: Easy to add new features following the same pattern
- ✅ **Reactive**: Real-time UI updates with minimal boilerplate
- ✅ **Type Safety**: Strongly typed throughout the application

---

*Built with ❤️ using Clean Architecture and Kotlin Flows* 
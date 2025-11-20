# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Journey is a personal journaling Android app built with Jetpack Compose and Kotlin. It allows users to record daily entries, analyze journaling habits through streaks and analytics, and customize the app experience with dynamic theming.

**Key Technologies:**
- Kotlin with Jetpack Compose (Material 3)
- Hilt for Dependency Injection
- Room Database for local storage
- DataStore for user preferences
- MVVM architecture with Clean Architecture principles
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 36
- Requires JDK 17+

## Build & Development Commands

### Building the App
```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK (unsigned)
./gradlew build                # Full build including tests
```

### Running Tests
```bash
./gradlew test                          # Run unit tests
./gradlew connectedAndroidTest          # Run instrumented tests (requires device/emulator)
./gradlew testDebugUnitTest             # Run debug unit tests only
```

### Code Quality & Linting
```bash
./gradlew lint                 # Run Android lint checks
./gradlew lintDebug           # Lint debug variant only
```

### Gradle Tasks
```bash
./gradlew clean               # Clean build artifacts
./gradlew dependencies        # Show dependency tree
```

### Running the App
- Open the project in Android Studio (2025.1.2 or newer)
- Sync Gradle files
- Run the app via the "Run" button or use `./gradlew installDebug`

## Architecture

The codebase follows **Clean Architecture** with MVVM pattern, organized into distinct layers:

### Layer Structure

**1. Presentation Layer** (`presentation/`)
- **Screens:** Each feature has its own screen package with Screen composable + ViewModel
  - `home/` - Main journal entry list with streak display
  - `addentry/` - Create/edit journal entries
  - `viewentry/` - Read-only entry viewing
  - `analytics/` - Journaling statistics and heatmap visualization
  - `settings/` - Theme customization and preferences
- **Components:** Reusable UI components (e.g., `JourneyTopAppBar`, `BottomNavBar`, `JournalEntryCard`)
- **Navigation:** Single-activity architecture with Jetpack Navigation Compose
  - Navigation graph defined in `JournalNavHost.kt`
  - Dynamic top bar management via `setTopBar` callback pattern
  - Bottom navigation visible on main screens (Home, Analytics, Settings)

**2. Domain Layer** (`domain/`)
- **Models:** Pure Kotlin data classes (e.g., `JournalEntry`, `StreakStats`)
- **Repository Interfaces:** Define data operations contracts
- **Use Cases:** Single-responsibility business logic (e.g., `AddEntryUseCase`, `GetJournalStreakUseCase`)

**3. Data Layer** (`data/`)
- **Local Database:** Room database (`JournalDatabase`) with DAO pattern
- **Entities:** Room database entities with mappers to domain models
- **DataStore:** Preferences storage for app settings (theme, accent color)
- **Repository Implementations:** Bridge between domain and data sources

### Key Architectural Patterns

**Dependency Injection (Hilt):**
- `JournalApp` is annotated with `@HiltAndroidApp`
- `MainActivity` uses `@AndroidEntryPoint`
- All dependencies provided in `di/AppModule.kt`
- ViewModels injected via `@HiltViewModel` and retrieved with `hiltViewModel()`

**State Management:**
- ViewModels expose state via `StateFlow` and `Flow`
- UI observes state with `collectAsState()`
- Database queries return `Flow` for reactive updates
- Example pattern: `GetJournalEntriesUseCase` returns `Flow<List<JournalEntry>>`

**Theme System:**
- Dynamic color support (Android 12+) via Material 3
- Custom accent color picker stored in DataStore
- Dark/light theme toggle with system default fallback
- Theme configuration in `SettingsViewModel` and applied in `JourneyTheme` composable
- Color schemes generated dynamically based on user preferences

**Navigation:**
- Type-safe navigation with sealed `Screen` classes
- Route parameters for entry IDs (e.g., `add_entry/{entryId}`)
- NavController passed to screens that need navigation
- FAB visibility controlled by current route

## Important Implementation Details

### Database Migrations
- Room database currently at version 1
- `fallbackToDestructiveMigration` is set to `false` - add proper migrations when schema changes

### DataStore Usage
- Settings stored in `SettingsPreferences.kt` using Preferences DataStore
- Keys: `accent_color` (Int), `use_dynamic_color` (Boolean), `dark_theme` (Boolean)
- Always read/write via flows and `dataStore.edit { }`

### ViewModel Patterns
- Use `viewModelScope.launch` for coroutines
- Prefer `stateIn()` with `WhileSubscribed(5000)` for Flow-to-StateFlow conversion
- Example: `HomeViewModel` demonstrates proper use case injection and flow handling

### UI State Management
- Screens accept a `setTopBar` lambda to dynamically update the top app bar
- This avoids prop drilling and allows screens to customize their own top bar

### Date Handling
- Use `java.time.LocalDate` and related classes (available API 26+)
- Utility functions in `utils/DateTimeUtils.kt`

### Streak Calculation
- Handled by `GetJournalStreakUseCase`
- Flows from `getAllEntryDatesFlow()` to reactively update streaks
- Returns `StreakStats` with current and longest streak

## CI/CD

The project uses GitHub Actions for continuous integration:
- **Workflow:** `.github/workflows/android-build.yml`
- Triggers on push to `master` branch
- Builds debug APK and creates/updates a GitHub release with tag `latest`
- Uses Gradle caching to speed up builds

## Code Conventions

- Package structure: `com.tobibur.journey.<layer>.<feature>`
- Compose previews included where applicable
- Use Hilt for all dependency injection - avoid manual instantiation
- Follow Material 3 design guidelines
- ViewModels should never hold references to Android framework classes (except Application via Hilt)

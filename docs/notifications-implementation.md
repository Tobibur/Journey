# Notifications (Daily Reminder) — Implementation Plan

Scheduler choice: **WorkManager** (survives reboots, no exact-alarm permission on targetSdk 36; fires within a window, fine for a journaling nudge).

The Notifications UI block already exists in `SettingsScreen.kt` (~lines 296–307) but is wired to a throwaway local `reminderEnabled` state and no-op callbacks. The work below builds the backing layers and connects them.

---

## 1. Dependencies & permissions

**`gradle/libs.versions.toml` + `app/build.gradle.kts`** — add:
- `androidx.work:work-runtime-ktx`
- `androidx.hilt:hilt-work`
- `androidx.hilt:hilt-compiler` (ksp) — for `@HiltWorker`

**`AndroidManifest.xml`** — add:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
Runtime-requested on API 33+ only (minSdk is 26 — guard with a version check). No `SCHEDULE_EXACT_ALARM` needed with WorkManager.

---

## 2. Persistence — `SettingsPreferences.kt`  ✅ DONE

Following the existing pattern. Reminder time is stored as **minutes since midnight** (single Int), converted with `hour = v / 60`, `minute = v % 60`.

- `REMINDER_ENABLED` (Boolean) → `reminderEnabledFlow` / `setReminderEnabled`  ✅
- `REMINDER_TIME` (Int, default `20 * 60` = 8:00 PM) → `reminderTimeFlow` / `setReminderTime(hour, minute)`  ✅

Decision: single `Flow<Int>` (minutes since midnight) chosen over two Ints / `LocalTime` — atomic single-key write, no flow combining, sorts naturally, and the TimePicker only needs `hour`/`minute` Ints anyway.

---

## 3. Notification infrastructure — new package `com.tobibur.journey.notifications`

Summary of pieces:
- **`NotificationHelper`** — creates the channel; `showReminderNotification()` builds a `NotificationCompat` notification with a `PendingIntent` opening `MainActivity`.
- **`ReminderWorker`** (`@HiltWorker`, `CoroutineWorker`) — calls `NotificationHelper.showReminderNotification()`.
- **`ReminderScheduler`** — `schedule(hour, minute)` enqueues a unique 24h periodic job with computed `initialDelay`; `cancel()` cancels it. (WorkManager min periodic interval is 15 min, hence 24h period + initial delay. Re-enqueue with `UPDATE` on time change.)
- **Hilt wiring** — `JournalApp` implements `Configuration.Provider`; provide `WorkManager` in `AppModule`; disable WorkManager's default initializer in the manifest.

### 3a. `NotificationHelper.kt`

```kotlin
package com.tobibur.journey.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tobibur.journey.MainActivity
import com.tobibur.journey.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "daily_reminder"
        private const val CHANNEL_NAME = "Daily Reminder"
        private const val NOTIFICATION_ID = 1001
    }

    /** Safe to call repeatedly; creating an existing channel is a no-op. */
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you to write your journal entry"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // see note below
            .setContentTitle("Time to journal ✍️")
            .setContentText("Take a moment to write about your day.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // POST_NOTIFICATIONS can be revoked at runtime — guard the post.
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled()) {
            nm.notify(NOTIFICATION_ID, notification)
        }
    }
}
```

> **Small icon:** `ic_launcher_foreground.xml` is NOT a valid status-bar icon (must be monochrome/transparent). Add a white notification icon via Android Studio → right-click `res` → New → Image Asset → *Notification Icons*, name it `ic_notification`. Quick placeholder: `android.R.drawable.ic_dialog_info` (replace before shipping).

### 3b. `ReminderWorker.kt`

```kotlin
package com.tobibur.journey.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        notificationHelper.showReminderNotification()
        return Result.success()
    }
}
```

### 3c. `ReminderScheduler.kt`

```kotlin
package com.tobibur.journey.notifications

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    companion object {
        private const val WORK_NAME = "daily_reminder_work"
    }

    fun schedule(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        if (!next.isAfter(now)) next = next.plusDays(1)  // already passed today → tomorrow

        val initialDelay = Duration.between(now, next).toMillis()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }
}
```

### 3d. Hilt wiring

**`JournalApp.kt`** — implement `Configuration.Provider`, inject the worker factory, create the channel on startup:

```kotlin
@HiltAndroidApp
class JournalApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationHelper.createChannel()
    }

    companion object {
        lateinit var instance: JournalApp
            private set
    }
}
```
Imports: `androidx.work.Configuration`, `androidx.hilt.work.HiltWorkerFactory`, `javax.inject.Inject`.

**`AppModule.kt`** — provide `WorkManager` (scheduler & helper are constructor-injected, no `@Provides` needed):

```kotlin
@Provides
@Singleton
fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
    WorkManager.getInstance(context)
```

**`AndroidManifest.xml`** — disable WorkManager's default initializer (required when using a custom Hilt worker factory, else it crashes at startup):

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```
(`xmlns:tools` is already declared in the manifest.)

---

## 4. ViewModel — `SettingsViewModel.kt`

- Inject `ReminderScheduler` (already has `SettingsPreferences`).
- Expose via `stateIn(viewModelScope, WhileSubscribed(5000), default)`:
  - `reminderEnabled`
  - `reminderTime` (Int minutes-since-midnight)
- `setReminderEnabled(enabled)` — persist, then `scheduler.schedule(...)` or `scheduler.cancel()`.
- `setReminderTime(hour, minute)` — persist, and reschedule if currently enabled.

---

## 5. UI — `SettingsScreen.kt`

- Remove local `reminderEnabled = remember { mutableStateOf(false) }` and the dummy `onReminderToggle` / `onReminderTimeClick` params.
- Collect `viewModel.reminderEnabled` and `viewModel.reminderTime`.
- Notifications `item` block:
  - `SwitchSetting` bound to VM state / `setReminderEnabled`. On enable, request POST_NOTIFICATIONS via `rememberLauncherForActivityResult(RequestPermission())`; only persist `true` once granted.
  - "Reminder Time" row → Material3 `TimePicker` dialog; subtitle shows formatted current time.

```kotlin
val reminderTime by viewModel.reminderTime.collectAsState()  // Int minutes since midnight

val timeState = rememberTimePickerState(
    initialHour = reminderTime / 60,
    initialMinute = reminderTime % 60,
    is24Hour = false
)
// on confirm:
viewModel.setReminderTime(timeState.hour, timeState.minute)

// subtitle label:
val label = remember(reminderTime) {
    LocalTime.of(reminderTime / 60, reminderTime % 60)
        .format(DateTimeFormatter.ofPattern("h:mm a"))
}
```

---

## 6. Reboot handling

Not needed — WorkManager's persistent periodic work survives reboots automatically. (Only relevant if you switch to AlarmManager, which would need a `BootReceiver` + `RECEIVE_BOOT_COMPLETED`.)

---

## 7. Tests

Extend `SettingsViewModelTest` (already in working tree): add cases for `setReminderEnabled` / `setReminderTime` verifying prefs writes and `ReminderScheduler` calls (mock the scheduler).

---

## Progress

- [x] Step 1 — deps & permission (incl. `androidx.hilt:hilt-compiler` ksp for `@HiltWorker`)
- [x] Step 2 — `SettingsPreferences` keys/flows/setters
- [x] Step 3 — notification infra (`NotificationHelper`, `ReminderWorker`, `ReminderScheduler`, Hilt wiring)
- [x] Step 4 — ViewModel (`reminderEnabled`/`reminderTime` flows, `setReminderEnabled`/`setReminderTime`)
- [x] Step 5 — UI (collected state, POST_NOTIFICATIONS launcher, `ReminderTimePickerDialog`)
- [x] Step 7 — tests (4 reminder cases in `SettingsViewModelTest`)

All implemented; `./gradlew testDebugUnitTest` passes.

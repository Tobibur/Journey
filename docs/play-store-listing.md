# Journey — Google Play Store Listing

This document contains all the text, metadata, and asset requirements needed to publish **Journey** on the Google Play Store. Copy fields directly into the Play Console.

---

## 1. App Identity

| Field | Value |
|---|---|
| App name (max 30 chars) | `Journey` |
| Package / Application ID | `com.tobibur.journey` |
| Version name | `1.0` |
| Version code | `1` |
| Default language | English (United States) – `en-US` |
| App or game | App |
| Free or paid | Free |
| Contains ads | No |
| In-app purchases | No |

---

## 2. Store Listing — Text Fields

### Short description (max 80 characters)
> A private journal with streaks, analytics, reminders & biometric lock.

*(72 chars — alternatives below)*
- `Your private daily journal — track streaks, analyze habits, stay reminded.` (73)
- `Simple, private journaling with streaks, insights, and a personal touch.` (71)

### Full description (max 4000 characters)

```
Journey is a beautifully simple, private journaling app that helps you build a daily writing habit and reflect on your life — one entry at a time.

Whether you're capturing thoughts, recording memories, or building a consistent reflection routine, Journey gives you a calm, distraction-free space that's entirely your own. Every entry stays on your device.

★ BUILD A WRITING HABIT
Stay motivated with current and longest streak tracking. Watch your journaling streak grow as you show up day after day.

★ UNDERSTAND YOUR HABITS
Visualize your journaling history with an activity heatmap and analytics. See your most consistent periods at a glance and stay inspired to keep going.

★ NEVER MISS A DAY
Set a daily reminder at the time that works for you, and Journey will gently nudge you to write.

★ PRIVATE BY DESIGN
Lock the app behind biometric authentication (fingerprint or face unlock). Your journal stays for your eyes only. All entries are stored locally on your device — no account, no cloud, no tracking.

★ YOURS TO KEEP
Export your entire journal to PDF or JSON, and import entries back whenever you like. Your data belongs to you — take it with you anytime.

★ MAKE IT YOURS
Personalize the app with a custom accent color, light and dark themes, and Material You dynamic theming (Android 12+) that adapts to your wallpaper.

FEATURES AT A GLANCE
• Quick, distraction-free daily entries
• Current & longest streak tracking
• Activity heatmap and journaling analytics
• Daily reminder notifications
• Biometric app lock
• Export to PDF and JSON
• Import entries from JSON
• Light / dark / system themes
• Custom accent colors & Material You dynamic theming
• 100% offline — your data never leaves your device

Start your Journey today and turn reflection into a daily habit.
```

*(Keep under 4000 characters. Current draft ≈ 1,750 characters — room to expand.)*

---

## 3. Graphic Assets (required for publishing)

> Google requires these before you can submit. Specs as of 2024+ Play Console.

| Asset | Spec | Required | Notes |
|---|---|---|---|
| App icon | 512 × 512 px, 32-bit PNG (with alpha), max 1 MB | ✅ Yes | Use the existing launcher icon, upscaled/redrawn to 512px. |
| Feature graphic | 1024 × 500 px, PNG or JPG (no alpha) | ✅ Yes | Shown at top of listing. Include app name + tagline. |
| Phone screenshots | 16:9 or 9:16, min 320px, max 3840px on a side. 2–8 images | ✅ Yes (min 2) | Capture Home (with streak), Add Entry, Analytics heatmap, Settings/themes. |
| 7" tablet screenshots | Same constraints | Optional | Only if you want tablet distribution highlighted. |
| 10" tablet screenshots | Same constraints | Optional | — |
| Promo video | YouTube URL | Optional | — |

**Suggested screenshot set (4–6):**
1. Home screen showing journal entries + current streak
2. Add/edit entry screen
3. Analytics screen with activity heatmap
4. Daily reminder / notifications setting
5. Theme & accent color personalization
6. Biometric app lock screen

> Tip: Add a short caption overlay on each screenshot (e.g., "Track your streak", "See your habits", "Lock it down").

---

## 4. Categorization & Contact

| Field | Value |
|---|---|
| App category | Productivity |
| Tags | journal, diary, journaling, habit tracker, notes, reflection |
| Email address | tobiburrahman786@gmail.com |
| Website | *(optional — add if available)* |
| Phone | *(optional)* |
| Privacy Policy URL | https://tobibur.github.io/Journey/privacy-policy.html |

---

## 5. Content Rating

Complete the **IARC content rating questionnaire** in Play Console. Based on Journey's content, expected answers:

- Violence: None
- Sexual content: None
- Profanity: None
- Controlled substances: None
- User-generated content shared with others: **No** (entries are private/local only)
- User interaction / social features: No

**Expected rating:** Everyone / PEGI 3.

---

## 6. Data Safety & Privacy

### Data Safety form (Play Console → App content → Data safety)
Journey is privacy-friendly. Expected declarations:

| Question | Answer |
|---|---|
| Does your app collect or share user data? | **No** — all journal data is stored locally on the device. |
| Is data encrypted in transit? | N/A (no data leaves the device) |
| Can users request data deletion? | Yes — "Clear All Data" in Settings; export available before deletion. |

> ⚠️ Confirm there is **no** analytics SDK, crash reporting (Firebase/Crashlytics), or ad SDK in the build. If any are added later, the Data Safety form must be updated to declare collected data.

### Permissions used (declare/justify if asked)
| Permission | Purpose |
|---|---|
| `USE_BIOMETRIC` | Optional biometric app lock |
| `POST_NOTIFICATIONS` | Daily journaling reminder notifications |
| `WRITE_EXTERNAL_STORAGE` (maxSdk 28) | Saving PDF/JSON exports on older devices |

### Privacy Policy (REQUIRED)
Google requires a publicly hosted privacy policy URL. A minimal policy is sufficient given no data is collected. It is published via GitHub Pages from `docs/privacy-policy.html`:

**https://tobibur.github.io/Journey/privacy-policy.html**

(Readable source: `docs/privacy-policy.md`.)

---

## 7. Release Build Checklist

Before uploading the App Bundle:

- [ ] Build a signed **Android App Bundle (.aab)**, not APK: `./gradlew bundleRelease`
- [ ] Configure an **upload key / keystore** and enable **Play App Signing** (recommended).
- [ ] Set `versionCode = 1`, `versionName = "1.0"` (already set). Bump `versionCode` for every future upload.
- [ ] Enable code shrinking/minification (`isMinifyEnabled = true`) and verify the release build runs.
- [ ] Test the release build on a physical device (biometric lock, reminders, export/import).
- [ ] Confirm `targetSdk = 36` meets Google's current target-API requirement.
- [ ] Remove the committed `app/release/` artifacts from the repo if they contain a real keystore (never commit keystores).

> ⚠️ Keep the keystore and its passwords backed up securely. Losing the upload key (without Play App Signing recovery) means you cannot update the app.

---

## 8. Suggested Release Notes (v1.0)

```
Welcome to Journey 1.0 — your private daily journal.

• Write distraction-free daily entries
• Track current & longest streaks
• Visualize your habits with an activity heatmap
• Set daily reminders so you never miss a day
• Lock the app with biometrics
• Export to PDF / JSON and import anytime
• Light & dark themes with custom accent colors

100% offline. Your data stays on your device.
```

---

## 9. Pre-Submission Summary

| Item | Status |
|---|---|
| App name, package, version | ✅ Ready |
| Short & full description | ✅ Drafted above |
| App icon 512px | ⬜ Export needed |
| Feature graphic 1024×500 | ⬜ Create |
| Screenshots (min 2) | ⬜ Capture |
| Content rating questionnaire | ⬜ Complete in Console |
| Data safety form | ⬜ Complete in Console |
| Privacy policy URL | ⬜ Host & link |
| Signed .aab | ⬜ Build & sign |
| Category / contact email | ✅ Ready |

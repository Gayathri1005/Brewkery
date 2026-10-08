# Brewkery ☕🥐

A native Android coffee & bakery ordering app built for the Android Developer take-home assignment. The UI recreates the supplied design screenshots in Jetpack Compose and loads all data from the live Brewkery REST API.

## Features
- **Home / Menu** – store info banner, search, category chips, item cards (thumbnail, badge, title, rating, price), skeleton loading, error + Retry.
- **Item Customizer** – hero image, ingredients, and *dynamic* size / milk-or-spread / sugar options read from the API; price updates with selections; quantity stepper; Add to Cart.
- **Cart** – multiple items, +/- quantity, remove, clear; subtotal, delivery fee, tax and total update live.
- **Order Status** – ticket ID (`#BK-xxxxx`), `PREPARING` status; Home then shows an *Active Order* banner with a Track button.
- Images via Coil with loading/error placeholders. No database – the cart lives in memory.

## Tech stack
Kotlin · Jetpack Compose (Material 3) · Navigation Compose · ViewModel + StateFlow · Retrofit + Gson · Kotlin Coroutines · Coil · JUnit 4

## Architecture
```
UI (Compose)  →  ViewModel (StateFlow)  →  Repository  →  Retrofit API  →  REST API
```
```
app/src/main/java/com/brewkery/app/
 ├── data/ model · remote · repository (+ AppContainer for manual DI)
 ├── viewmodel/ MenuViewModel · DetailViewModel · CartViewModel · CartCalculator
 ├── ui/ theme · components · home · detail · cart · order
 ├── navigation/ AppNavHost
 └── MainActivity.kt
```
`MenuViewModel` and `CartViewModel` are activity-scoped so the menu, cart and active order persist across screens; `DetailViewModel` is scoped per detail destination.

Cart maths (`CartCalculator`): `tax = subtotal × 0.08`, `total = subtotal + 2.50 + tax`. Delivery fee, tax rate and ETA are read from the API `meta` block (defaults of $2.50 / 8% are used if absent). An empty cart shows a $0.00 total.

## API used
- Menu: `GET https://vivekshah138.github.io/Brewkery/data.json`
- Item: `GET https://vivekshah138.github.io/Brewkery/api/items/{id}.json` (used by the detail screen; falls back to the cached menu entry if the request fails)

## How to run
1. Install **Android Studio Koala (2024.1.1)** or newer (JDK 17, which is bundled).
2. *File ▸ Open* and select the `Brewkery` folder; let Gradle sync (Gradle 8.7 / AGP 8.5.2 / Kotlin 1.9.24).
3. Choose an emulator or device (API 24+) and press **Run ▶**. The device needs internet access.

## Build the APK
- Android Studio: *Build ▸ Build Bundle(s) / APK(s) ▸ Build APK(s)*.
- Command line (after Android Studio has generated the wrapper, or with a local Gradle 8.7): `./gradlew assembleDebug`
  → `app/build/outputs/apk/debug/app-debug.apk`

## Testing
`./gradlew test` (or right-click `app/src/test` ▸ Run).
- `CartCalculatorTest` – subtotal, 8% tax, $2.50 delivery, total, empty cart, unit price with extras.
- `CartViewModelTest` – add with extras, merging identical lines, increase/decrease/remove, placing an order clears the cart and creates the active order.

## Notes
- Sugar levels are plain strings in the API with no price field, so they never affect price (one label, "Light Wildflower Honey (+0.40)", mentions a price only in text).
- The "Open" button on the store banner and the heart on the detail screen are visual only, as in the design.

## AI tools used during development
Claude (Anthropic) was used to inspect the API and screenshots, generate the project scaffold and code, and draft the unit tests and this README. All code was reviewed against the assignment requirements.

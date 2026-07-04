# Fridge

Fridge is an Android app for managing a personal fridge, discovering recipes, and sharing cooking content with other users. The app combines fridge inventory tracking, recipe search, grocery-list support, user profiles, collections, reviews, tips, and Firebase-backed notifications.

## Main Features

- Email/Google authentication with Firebase Authentication.
- Onboarding flow for profile, diet, allergens, ingredient preferences, and cooking role.
- Personal fridge management with ingredients and quantities
- Grocery list management and fridge-to-grocery workflows.
- Recipe browsing, filtering, creation, editing, remixing, reviews, and tips.
- Fridge-based recipe search that highlights recipes already doable with available ingredients.
- User profiles with followers, followed users, recipes, favourites, custom collections, reviews, and tips.
- Save-to-collection flow for organizing recipes.
- Camera/image support for profile and recipe media.
- Firebase Cloud Messaging notifications, backed by a Firebase Functions trigger.

## Tech Stack

- Kotlin
- Jetpack Compose
- Cloud Firestore
- Firebase Authentication
- Firebase Storage
- CameraX
- Gradle Kotlin

## Repository Structure

```text
.
├── app/                         # Android application module
│   └── src/main/java/com/example/fridgeproject/
│       ├── camera/              # Camera actions and camera screen integration
│       ├── data/                # Firebase repositories, models, app container, use cases
│       ├── domain/              # Repository contracts
│       ├── model/               # App domain/UI models and enums
│       ├── navigation/          # App navigation, routes, graphs, top/bottom bars
│       ├── notification/        # Firebase Messaging service
│       ├── ui/                  # Compose screens, components, theme
│       └── viewmodel/           # Feature-oriented ViewModels and UI states
├── functions/                   # Firebase Functions project
├── gradle/                      # Version catalog and Gradle wrapper files
├── firebase.json                # Firebase configuration
├── settings.gradle.kts
└── build.gradle.kts
```

## App Architecture

The app follows a feature-oriented MVVM structure:

- Screens and reusable UI live under `ui/`.
- ViewModels expose feature UI state and actions under `viewmodel/`.
- Repository interfaces live under `domain/`.
- Firebase implementations live under `data/`.
- Navigation is split into route declarations, graph builders, top/bottom bars, and shared navigation utilities.

Reactive data flows use Kotlin `Flow` where screens need live updates from Firestore or session state. One-shot UI feedback, such as snackbars and navigation side effects, is handled through event flows rather than long-lived UI state.


## Notes

- The app package is `com.example.fridgeproject`.
- The app label shown on device is `Fridge`.
- Minimum SDK is 31.
- Target SDK is 36.
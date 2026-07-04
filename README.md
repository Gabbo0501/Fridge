# Fridge

Fridge is an Android app for managing a personal fridge, discovering recipes, and sharing cooking content with other users. The app combines fridge inventory tracking, recipe search, grocery-list support, user profiles, collections, reviews, tips, and Firebase-backed notifications.

## Main Features

- Recipe browsing, filtering, creation, editing and remixing.
- Personal fridge management with ingredients and quantities.
- Grocery list management and fridge-to-grocery workflows.
- Fridge-based recipe search that highlights recipes already doable with available ingredients.
- Google authentication with Firebase Authentication.
- Onboarding flow for profile, diet, allergens, ingredient preferences, and cooking role.
- User profiles with followers, followed users, recipes, favourites, custom collections, reviews, and tips.
- Save-to-collection flow for organizing recipes.
- Reviews and tips sharing.
- Camera/image support for profile and recipe media.
- Push notifications.

## Tech Stack

- Kotlin
- Jetpack Compose
- Cloud Firestore
- Firebase Authentication
- Firebase Storage
- CameraX
- Gradle Kotlin

## App Architecture

The app follows an MVVM structure:

- Screens and reusable UI live under `ui/`.
- ViewModels expose feature UI state and actions under `viewmodel/`.
- Repository interfaces live under `domain/`.
- Firebase implementations live under `data/`.
- Navigation is split into route declarations, graph builders, top/bottom bars and shared navigation utilities.

Reactive data flows use Kotlin `Flow` where screens need live updates from Firestore or session state. One-shot UI feedback, such as snackbars and navigation side effects, is handled through event flows.

## Notes

- The app package is `com.example.fridgeproject`.
- The app label shown on device is `Fridge`.
- Minimum SDK is 31.
- Target SDK is 36.
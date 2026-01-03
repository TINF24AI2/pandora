# Pandora Pass - Secure Password Manager

**Pandora Pass** is a modern, secure password manager for Android, built as a university project. It leverages the latest Android development technologies, including Jetpack Compose for the UI, and strong cryptographic practices to ensure user data is kept safe. The application provides a clean, intuitive interface for managing sensitive credentials locally on the device.

## ✨ Features

The application's settings screen demonstrates several core features of the app:

*   **Secure Authentication:**
    *   **Biometric Unlock:** Users can enable fingerprint or face unlock for quick and secure access, leveraging the Android Biometric API.
    *   **PIN Unlock:** A fallback PIN option is available for devices without biometric hardware.
*   **Modern UI:**
    *   **Light & Dark Themes:** The app includes beautiful, hand-crafted light and dark themes that can be switched manually.
    *   **Dynamic Theming:** (Work in Progress) The app is set up to support Material You dynamic colors on Android 12+.
    *   **Built with Jetpack Compose:** The entire UI is built with Google's modern, declarative UI toolkit.
*   **Security Focused:**
    *   **Secure Data Storage:** Utilizes encrypted storage mechanisms for sensitive data.
    *   **Automatic Locking:** Configurable auto-lock timers to protect the app when not in use.
    *   **Clipboard Management:** Options to automatically clear the clipboard after a set time to prevent credential leakage.
*   **User-Friendly:**
    *   **Intuitive Settings:** A well-organized settings page to easily manage app preferences.
    *   **Localization Ready:** The app is structured to support multiple languages.

## 🛠️ Technology Stack & Architecture

This project utilizes a modern Android architecture and libraries to ensure it is robust, scalable, and maintainable.

*   **UI:**
    *   **Jetpack Compose:** The UI is built 100% in Kotlin using Jetpack Compose.
    *   **Material 3:** Implements Google's latest Material Design 3 guidelines for components, colors, and typography.
    *   **Compose Navigation:** Handles all in-app navigation between different screens.
*   **Architecture:**
    *   **MVVM (Model-View-ViewModel):** The project follows a reactive MVVM architecture.
    *   **ViewModel:** `SettingsViewModel` handles the business logic and state management for the settings screen.
    *   **State Management:** Utilizes `StateFlow` and `SharedFlow` for reactive state updates and one-shot events (like showing error messages).
*   **Security:**
    *   **AndroidX Biometric:** Provides a secure and user-friendly way to implement biometric authentication.
    *   **Argon2kt:** Integrates the Argon2 hashing algorithm, a modern and secure choice for password hashing and key derivation.
    *   **Cryptography:** Implements secure cryptographic ciphers for encrypting and decrypting user data.
*   **Data:**
    *   **DataStore:** (Likely used or planned) Jetpack DataStore is ideal for persisting user preferences like theme choice or biometric settings.
*   **Core Libraries:**
    *   **Kotlin & Coroutines:** For asynchronous operations and modern language features.
    *   **Kotlinx Serialization:** For efficient and type-safe parsing of JSON data.

## 🚀 Setup and Installation

To get this project running on your local machine, follow these steps:

1.  **Prerequisites:**
    *   Android Studio (latest stable version recommended, e.g., Iguana or newer)
    *   Android SDK API Level 30+

2.  **Clone the repository:**

    ```bash
    git clone https://github.com/TINF24AI2/pandora.git
    cd pandora
    ```
3.  **Sync Gradle:**
    *   Android Studio will automatically detect the Gradle files. Allow it to sync and download the required dependencies. This may take a few minutes.

4.  **Run the application:**
    *   Select an emulator (running API 30+) or a physical Android device.
    *   Click the **Run 'app'** button (▶️) in the toolbar.

## 🎓 Project Context

This application was developed as a part of a university course on mobile application development. The primary goals were to demonstrate proficiency in:
*   Modern Android development practices (Jetpack Compose, MVVM).
*   Implementing secure authentication and data handling.
*   Designing a clean and intuitive user interface following Material Design principles.
*   Managing project dependencies and build configurations with Gradle.
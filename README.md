# Pandora Pass - Secure Password Manager

**Pandora Pass** is a modern, secure password manager for Android, built as a university project. It
leverages the latest Android development technologies, including Jetpack Compose for the UI, and
strong cryptographic practices to ensure user data is kept safe. The application provides a clean,
intuitive interface for managing sensitive credentials locally on the device.

## ✨ Features

The application's settings screen demonstrates several core features of the app:

* **Biometric Unlock:** Users can enable fingerprint or face unlock for quick and secure access,
  leveraging the Android Biometric API.
* **Modern UI:**
    * **Light & Dark Themes:** The app includes beautiful, hand-crafted light and dark themes that
      can be switched manually.
    * **Built with Jetpack Compose:** The entire UI is built with Google's modern, declarative UI
      toolkit.
* **Security Focused:**
    * **Secure Data Storage:** Utilizes encrypted storage mechanisms for sensitive data.
    * **Automatic Locking:** Configurable auto-lock timers to protect the app when not in use.
    * **Clipboard Management:** Options to automatically clear the clipboard after a set time to
      prevent credential leakage.
    * **Android Autofill Integration**: Ability to automatically fill in your username and password
      in a login form using the Android Autofill API. Just select Pandora as your preferred Autofill
      service in the settings and you are good to go.
* **User-Friendly:**
    * **Intuitive Settings:** A well-organized settings page to easily manage app preferences.

## 🚀 Setup and Installation

To get this project running on your local machine, follow these steps:

1. **Prerequisites:**
    * Android Studio (latest stable version recommended, e.g., Otter or newer)
    * Android SDK API Level 30+
    * An Android emulator or device with Android 11 or later, preferably with Android 16 (for
      running the app).

2. **Clone the repository:**
   ```bash
   git clone https://github.com/TINF24AI2/pandora.git
   cd pandora
   ```
3. **Sync Gradle:**
    * Android Studio will automatically detect the Gradle files. Allow it to sync and download the
      required dependencies. This may take a few minutes.

4. **Run the application:**
    * Select an emulator or a physical Android device. (running Android 11 or later)
    * Click the **Run 'app'** button (▶️) in the toolbar.

## 🎓 Project Context

This application was developed as a part of a university course on mobile application development.
The primary goals were to demonstrate proficiency in:

* Modern Android development practices (Jetpack Compose, MVVM).
* Implementing secure authentication and data handling.
* Designing a clean and intuitive user interface following Material Design principles.
* Managing project dependencies and build configurations with Gradle.

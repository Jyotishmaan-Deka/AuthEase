# AuthEase - TOTP Authenticator

[![Android](https://img.shields.io/badge/Android-API%2028+-green.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A simple Android app to generate TOTP (Time-based One-Time Password) codes for two-factor authentication. Works with Google Authenticator, Authy, and any standard TOTP service.

---

## Features

- Generate TOTP codes with real-time 30-second countdown
- Secure storage using Android Keystore + AES encryption
- Biometric lock
- Dark mode support
- No internet permission — fully offline
- Add and delete accounts easily

---

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM + Repository Pattern
- **UI**: Material Design 3, ViewBinding
- **Jetpack**: Room, ViewModel, LiveData, Navigation Component
- **DI**: Hilt
- **Security**: Android Keystore, Biometric API
- **Async**: Coroutines

---

## Getting Started

```bash
git clone https://github.com/Jyotishmaan-Deka/authease.git
```

Open in Android Studio, let Gradle sync, then run on a device or emulator (API 28+).

---

## How to Add an Account

1. Tap **+** on the home screen
2. Enter the service name, your account/email, and the base32 secret key provided by the service
3. Tap **Add** — the code starts generating immediately

Codes refresh every 30 seconds automatically.

---

## Project Structure

```
com.deadlyord.authease/
├── auth/        # TOTP logic & crypto
├── db/          # Room database
├── di/          # Hilt modules
├── ui/          # Fragments, Adapter, MainActivity
└── utils/       # Helper extensions
```

---

## Roadmap

- QR code scanning
- Account import/export
- HOTP support
---

## License

MIT — see [LICENSE](LICENSE)

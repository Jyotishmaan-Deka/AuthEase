# AuthEase

AuthEase is a lightweight and secure Android application designed for Two-Factor Authentication (2FA). It generates Time-based One-Time Passwords (TOTP) to provide an extra layer of security for your digital accounts, fully compatible with standard services like Google, GitHub and others.

[![Android](https://img.shields.io/badge/Android-API%2028+-green.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Key Features

- **Secure Token Generation** – Instantly generate standard TOTP codes with a 30-second refresh cycle and a visual countdown timer.
- **Hardware-Backed Encryption** – Keys are never stored in plain text. They are encrypted using AES and managed by the Android Keystore System for maximum protection.
- **Biometric Security** – Integrated biometric authentication (fingerprint, face, or pattern lock) prevents unauthorized local access.
- **Offline by Design** – The application requires zero internet permissions.
- **QR Code Integration** – Quickly import accounts by scanning QR codes or by manually entering the details.
- **Backup and Migration** – Securely export and import accounts, allowing safe backups and easy migration between devices.

## Screenshots
|                 Main Screen                 |                    Add Account                    |                Settings Code View                 |
|:-------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|
| ![Main Screen](screenshots/Home_Screen.png) | ![Add Account](screenshots/AddAccount_Screen.png) | ![Settings View](screenshots/Settings_Screen.png) |

## Tech Stack

AuthEase is built using modern Android development tools and patterns:

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **UI Framework:** Material Design 3 with ViewBinding
- **Database:** Room for local persistence
- **Dependency Injection:** Hilt
- **Security:** Android Keystore API and Biometric API
- **Concurrency:** Kotlin Coroutines and Flow

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/Jyotishmaan-Deka/authease.git
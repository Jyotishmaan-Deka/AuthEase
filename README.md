# AuthEase - Android TOTP Authenticator

[![Android](https://img.shields.io/badge/Android-API%2028+-green.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A secure, modern Android authenticator app that generates Time-based One-Time Passwords (TOTP) for two-factor authentication. Built with the latest Android development best practices using Kotlin, Jetpack Compose principles, and Material Design 3.

## 🌟 Features

- **🔐 TOTP Generation**: Generate time-based one-time passwords compatible with Google Authenticator, Authy, and other TOTP services
- **🛡️ Secure Storage**: Uses Android Keystore for secure key management and encryption
- **📱 Modern UI**: Clean, intuitive interface following Material Design 3 guidelines
- **🌙 Dark Mode**: Full support for system dark mode
- **⏱️ Real-time Countdown**: Visual progress indicators showing time remaining for each code
- **📋 Easy Management**: Add, view, and delete authenticator accounts with ease
- **🔒 Biometric Security**: Optional biometric authentication for app access
- **📱 Responsive Design**: Optimized for various screen sizes and orientations

## 📱 Screenshots

| Home Screen | Add Account | Dark Mode |
|-------------|-------------|-----------|
| *Coming Soon* | *Coming Soon* | *Coming Soon* |

## 🛠️ Tech Stack

### Architecture & Design Patterns
- **MVVM Architecture** - Clean separation of concerns
- **Repository Pattern** - Data layer abstraction
- **Dependency Injection** - Hilt for DI management

### Libraries & Frameworks
- **Kotlin** - Primary programming language
- **Android Jetpack Components**:
  - Navigation Component - Fragment navigation
  - Room Database - Local data persistence
  - ViewModel & LiveData - UI state management
  - ViewBinding - Type-safe view binding
- **Hilt** - Dependency injection
- **Material Design 3** - Modern UI components
- **Coroutines** - Asynchronous programming
- **Android Keystore** - Secure cryptographic operations

### Security Features
- **Android Keystore** - Hardware-backed key storage
- **AES Encryption** - Data encryption at rest
- **Biometric Authentication** - Fingerprint/Face unlock
- **No Cloud Backup** - Sensitive data excluded from backups

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 28 or higher
- Kotlin 1.9 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/authease.git
   cd authease
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Click "Open an Existing Project"
   - Navigate to the cloned directory and select it

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio

### Building APK

```bash
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/release/`

## 📖 Usage

### Adding an Account

1. **Tap the "+" button** on the home screen
2. **Enter account details**:
   - **Issuer**: The service name (e.g., "Google", "GitHub", "Microsoft")
   - **Account Name**: Your username or email
   - **Secret Key**: The base32-encoded secret provided by the service

3. **Tap "Add Account"** to save

### Using QR Codes (Future Feature)

*QR code scanning functionality will be added in a future update.*

### Managing Accounts

- **View Codes**: Codes are automatically generated and refresh every 30 seconds
- **Delete Account**: Tap the delete button (🗑️) next to any account
- **Manual Refresh**: Pull down to refresh all codes

## 🔧 Configuration

### Secret Key Format

The app accepts secret keys in base32 format (the standard for TOTP). Common examples:

```
JBSWY3DPEHPK3PXP
HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ
```

### Supported Algorithms

- **SHA1** (default)
- **SHA256** 
- **SHA512**

### Code Length

- **6 digits** (default)
- **8 digits** (configurable per account)

## 🏗️ Project Structure

```
app/
├── src/main/
│   ├── java/com/deadlyord/authease/
│   │   ├── auth/                 # Authentication & TOTP logic
│   │   │   ├── CryptoHelper.kt
│   │   │   ├── OTPGenerator.kt
│   │   │   └── TOTP.kt
│   │   ├── db/                   # Database & entities
│   │   │   ├── AccountDao.kt
│   │   │   ├── AccountDatabase.kt
│   │   │   └── AccountEntity.kt
│   │   ├── di/                   # Dependency injection
│   │   │   └── AppModule.kt
│   │   ├── ui/                   # UI components
│   │   │   ├── home/
│   │   │   ├── addaccount/
│   │   │   ├── AccountAdapter.kt
│   │   │   └── MainActivity.kt
│   │   ├── utils/                # Utilities
│   │   │   ├── Extensions.kt
│   │   │   └── QRCodeParser.kt
│   │   └── AuthenticatorApplication.kt
│   └── res/                      # Resources
│       ├── layout/               # XML layouts
│       ├── navigation/           # Navigation graph
│       ├── values/               # Strings, colors, themes
│       └── drawable/             # Icons and drawables
```

## 🔐 Security Considerations

### Data Protection
- **Encrypted Storage**: All secret keys are encrypted using Android Keystore
- **No Network Access**: App works completely offline
- **Secure Key Generation**: Uses Android's cryptographically secure random number generator
- **No Backup**: Sensitive data is excluded from Android's backup mechanisms

### Privacy
- **No Analytics**: No user data collection or tracking
- **No Internet Permission**: App cannot send data over the network
- **Local Only**: All data remains on the device

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Write unit tests for new features

### Issue Reporting

When reporting issues, please include:
- Android version
- Device model
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)

## 📋 Roadmap

### Version 1.1 (Planned)
- [ ] QR code scanning support
- [ ] Batch account import/export
- [ ] Account categories/folders
- [ ] Search functionality

### Version 1.2 (Planned)
- [ ] HOTP support
- [ ] Custom time periods
- [ ] Account icons
- [ ] Backup/restore (encrypted)

### Version 2.0 (Future)
- [ ] Wear OS companion app
- [ ] Widget support
- [ ] Advanced security options
- [ ] Multi-language support

## 🐛 Known Issues

- None currently reported

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 AuthEase

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

- [Material Design 3](https://m3.material.io/) for the design system
- [Android Jetpack](https://developer.android.com/jetpack) for the architecture components
- [RFC 6238](https://tools.ietf.org/html/rfc6238) for the TOTP algorithm specification
- [RFC 4648](https://tools.ietf.org/html/rfc4648) for the Base32 encoding specification

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/authease/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/authease/discussions)
- **Email**: support@authease.app

## 📊 Stats

![GitHub stars](https://img.shields.io/github/stars/yourusername/authease?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/authease?style=social)
![GitHub issues](https://img.shields.io/github/issues/yourusername/authease)
![GitHub pull requests](https://img.shields.io/github/issues-pr/yourusername/authease)

---

**Made with ❤️ for the Android community**

*AuthEase - Secure, Simple, Smart Authentication*

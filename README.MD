# 🏦 Thellex POS

**Thellex POS (Point of Sale)** is a **Kotlin-powered Android application** designed to empower merchants in emerging markets to seamlessly accept **crypto and fiat payments** using Thellex’s secure blockchain infrastructure. It bridges **traditional finance (fiat)** and **Web3 (crypto)**, offering businesses a fast, transparent, and reliable way to manage transactions. With an intuitive interface and robust backend, Thellex POS simplifies payment processing, wallet management, and on/off-ramping for merchants in supported regions.

## 🌟 Features

- 💳 **Seamless Payments**: Accept crypto (e.g., BTC, ETH) and fiat (e.g., NGN, GHS) in real-time with minimal fees.
- 🔄 **On/Off-Ramp**: Buy and sell cryptocurrencies directly within the app using local currencies.
- 📊 **Wallet Management**: Monitor balances, view detailed transaction history, and manage multiple wallets.
- 🔔 **Real-Time Notifications**: Receive instant payment confirmations and transaction updates via Firebase Cloud Messaging.
- 🛡️ **Secure Transactions**: Leverage Thellex’s escrow system and risk management for safe, fraud-resistant payments.
- 🌍 **Multi-Currency Support**: Handle transactions in local fiat currencies and major cryptocurrencies.
- 📱 **User-Friendly Interface**: Built with Jetpack components for a smooth and responsive experience.

## 🌍 Supported Countries

Thellex POS is tailored for merchants in emerging markets, currently supporting:

- 🇳🇬 **Nigeria**
<!-- - 🇬🇭 **Ghana**
- 🇿🇦 **South Africa**
- 🇰🇪 **Kenya**
- 🇺🇬 **Uganda** -->
- 🌐 **More countries coming soon!**

## 📲 Download

- **Google Play Store**: [Thellex POS on Google Play](https://play.google.com/store/apps/details?id=com.thellex.pos) *(Coming Soon)*
- **Apple App Store**: [Thellex POS on App Store](https://apps.apple.com/app/thellex-pos/id000000000) *(Planned for future release)*

## ⚡ Rules of Usage

To ensure a secure and compliant experience with Thellex POS, please adhere to the following guidelines:

### 🚫 Don’t
- Engage in illegal or fraudulent activities, including money laundering or unauthorized transactions.
- Attempt to reverse-engineer, decompile, or tamper with the application’s code.
- Share private keys, login credentials, or sensitive account information.
- Misuse on/off-ramp features for spamming or speculative trading.

### ✅ Do
- Use Thellex POS responsibly to process legitimate business transactions.
- Secure your wallet with strong passwords and enable two-factor authentication (2FA).
- Comply with local KYC/AML regulations in your country.
- Report issues or suspicious activity to our support team.

## 🛠️ Tech Stack

Thellex POS is built with modern Android development tools and best practices:

- **Language**: Kotlin (1.7+)
- **Framework**: Android SDK, Jetpack Components (LiveData, ViewModel, Navigation)
- **Architecture**: MVVM (Model-View-ViewModel) for clean separation of concerns
- **Backend**: Thellex API (built with NestJS for scalable RESTful services)
- **Database**: Room (SQLite) for local caching and offline support
- **Networking**: Retrofit for API calls, OkHttp for HTTP client
- **Build Tools**: Gradle (Kotlin DSL)
- **Dependencies**: Hilt for dependency injection, Coroutines for asynchronous programming
- **Blockchain Integration**: Thellex blockchain for secure crypto transactions
- **Notifications**: Firebase Cloud Messaging (FCM) for push notifications

## 🚀 Installation

Follow these steps to set up and run Thellex POS locally:

### Prerequisites

- **Android Studio**: 2022.3 (Hedgehog) or later
- **Kotlin**: 1.7 or higher
- **Java**: JDK 11 or 17
- **Gradle**: 7.0 or later
- **Git**: For cloning the repository
- **Firebase Account**: For notifications and authentication (optional for local testing)
- **Thellex API Key**: For backend integration (contact [support@thellex.com](mailto:support@thellex.com))

### Cloning the Repository

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/thellex/thellex-pos.git
   cd thellex-pos

2. **Open in Android Studio:**
- Launch Android Studio
- Select File > Open and choose the thellex-pos directory.
- Sync the project with Gradle (Sync Project with Gradle Files).

3. **Configure Environment:**
- Create a .env file in the project root for sensitive configurations:env
 
4. **Set Up Firebase**
- Create a Firebase project at console.firebase.google.com.
- Download google-services.json and place it in the app/ directory.
- Ensure Firebase SDK dependencies are in app/build.gradle.kts.

5. Build and Run:
- Connect an Android device or emulator (API 21+ recommended).
- Click Run > Run 'app' in Android Studio, or use:bash

```bash 
   ./gradlew build
   ./gradlew installDebug
```


 <!-- TestingUnit Tests: Run ./gradlew test for ViewModel and repository tests using JUnit 5.
UI Tests: Run ./gradlew connectedAndroidTest for Espresso-based UI tests.
Mock API: Use MockWebServer to simulate Thellex API responses for testing.

 ContributingWe welcome contributions to improve Thellex POS! To contribute:Fork the repository: git fork https://github.com/thellex/thellex-pos.git.
Create a feature branch: git checkout -b feature/your-feature.
Commit changes: git commit -m "Add your feature".
Push to the branch: git push origin feature/your-feature.
Open a pull request with a detailed description.

Follow our Contributing Guidelines (CONTRIBUTING.md) and adhere to the Kotlin Coding Conventions. SupportIssues: Report bugs or feature requests on GitHub Issues.
Contact: Email support@thellex.com (mailto:support@thellex.com).
Documentation: Check the docs/ folder for API specs, architecture, and setup guides.

 LicenseThellex POS is licensed under the MIT License (LICENSE.txt). See the license file for details.
 -->
 
 ## About Thellex
 Thellex is a fintech platform dedicated to bridging traditional finance and Web3, enabling businesses in emerging markets to thrive with secure, transparent, and efficient payment solutions. Thellex POS is built by the Thellex team, passionate about empowering merchants with cutting-edge blockchain technology. Follow us on GitHub or visit thellex.com for more information.


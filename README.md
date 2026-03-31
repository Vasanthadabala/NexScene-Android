# NexScene
one app to discover, track, rate, and find where to watch movies and series.

<p align="center">
  <img src="https://github.com/user-attachments/assets/eb8f2add-13fd-47c0-a0f7-16bb3f961948" width="24%" />
  <img src="https://github.com/user-attachments/assets/9ad03a41-f34b-45ec-841b-14b8830a559d" width="24%" />
  <img src="https://github.com/user-attachments/assets/a563dcb1-85ab-4034-a840-b5df5ae48b17" width="24%" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/ea7fbe73-6edf-47fd-8037-d1d74d71a451" width="24%" />
  <img src="https://github.com/user-attachments/assets/d17d806d-f79a-4d7f-b8c1-4544dad596dd" width="24%" />
  <img src="https://github.com/user-attachments/assets/da7514d5-04b6-4a84-b3ff-8ea98876005b" width="24%" />
</p>


A beautifully designed **Android Budget Tracker App** built with **Kotlin** and **JetPack Compose**, helping users easily track their income, expenses, and spending trends — all synced securely via **G-Drive**.  

---

## 🚀 Overview

The Budget Tracker App allows users to manage their financial activities smoothly with a clean, modern design.
It supports charts, category insights, detailed expense tracking, and secure backup/restore through Google Drive.

---

## ✨ Features

### 🏠 Onboarding
- Simple onboarding screen with app intro and “Get Started” button.  
- Bottom sheet for entering username and continuing to the home screen.

### 📊 Dashboard
- Credit-card style summary card showing:
      -Total Budget
      -Total Spent
      -Remaining Balance
- Search bar and filters to quickly find transactions.
- Complete list of all income and expenses.
- Each transaction opens a detailed view with:
      - Amount
      - Category
      - Description
      - Edit
      - Delete
- Floating Action Button to open a bottom sheet for:
- Adding Income And Expense
- Detailed expense view with editing and delete capability.

### 🧾 Stats
- Interactive line chart showing monthly spending trends.  
- “Top Spending Categories” section ranked from high to low.
- Each category is clickable, opening a detail screen showing:
    - All transactions under that category
    - Budget vs Expense analysis
- One-tap option to export the category report as PDF

### Settings & Account
- User profile card showing username.
- Google Sign-In support for cloud sync.
- Options available:
    - Backup Data
    - Restore Backup
    - Delete Backup
    - Sign Out
- About card leading to app information screen.


### ☁️ Cloud Sync & Authentication
- Fully integrated Google Sign-In for secure data backup and restore.
- Allows users to store, retrieve, and delete cloud backups anytime.

### 🔔 Notifications Panel
- Stay informed with a built-in notifications section for alerts and updates.

---

## 🧠 Tech Stack

| Component | Technology |
|------------|-------------|
| Language | Kotlin |
| Framework | Jetpack Compose |
| Database | RoomDatabase |
| Cloud Sync | Google Drive |
| Authentication | Sign in with Google |
| Charts | Canva Charts |
| Navigation | Jetpack Navigation |
| Network | Ktor Client |
| PDF Export | Android PDFDocument |

---

## 🎉 Additional Highlights
- Light & Dark mode support
- Elegant and smooth UI animations.
- Clean architecture with scalable codebase.

## TMDB API key setup
Add your key in `local.properties`:

```properties
TMDB_API_KEY=your_tmdb_api_key_here
```

`local.properties` is already gitignored, so this key will not be pushed to GitHub.

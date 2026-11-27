# RAI FISH

A digital ledger for selling fish directly to customers. Record sales by weight, track customer credit and payments, manage stock, and keep everything synced to your Google account.

## What it does

- **Sales by weight** — record a sale in kg × price/kg, with pieces, per customer, per day.
- **Customer credit** — every customer has a balance. See what each customer owes at a glance.
- **Payments** — add a payment against a customer's oldest dues, or mark a single order paid. Payments sync to the cloud with their date.
- **Invoice** — tap the receipt icon on a customer to see total billed, total paid, balance due, and the full payment history (date and amount), and copy it to share.
- **Fish types & stock** — product catalog with price per kg and automatic stock deduction on every sale.
- **Daily batches** — track each day's incoming stock by pieces and weight.
- **Reports** — daily and range reports with CSV export.
- **Offline first** — works without internet. When you're signed in with Google, sales, payments, customers, products, and batches sync to Firebase.

## Design

Gemini-style interface built with Jetpack Compose and Material 3. Follows your phone's light/dark display mode, with a manual toggle in the app.

## Tech

- Kotlin + Jetpack Compose (Material 3)
- Room for offline storage, Firebase Firestore for cloud sync, Google Sign-In
- GitHub Actions builds the debug APK on every push

## Get the app

Every push builds a debug APK: **Actions → Build Debug APK → Artifacts → debug-apk**.

## Version

1.1

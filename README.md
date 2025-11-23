# Gemini Web App

This is a simple Android application that displays the Gemini website (gemini.google.com) in a WebView.

## Features

- **Persistent Session:** The WebView is configured to allow cookies and DOM storage. This helps to maintain your session, so you don't have to log in again every time you open the app.
- **Full-Screen WebView:** The app displays the Gemini website in a full-screen WebView.

## How It Works

The application consists of a single `MainActivity` that contains a Jetpack Compose `AndroidView`. This `AndroidView` is configured as follows:
- JavaScript is enabled.
- DOM Storage is enabled.
- Third-party cookies are accepted.
- It loads the URL `https://gemini.google.com`.

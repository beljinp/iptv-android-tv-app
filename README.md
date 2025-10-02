# IPTV Android TV App

A custom IPTV player app designed for Android TV and Fire TV devices that integrates with VLC Media Player.

## Features

- ✅ One-time credential setup (Host, Username, Password)
- ✅ Secure encrypted credential storage
- ✅ Automatic content categorization (TV Channels vs Movies)
- ✅ VLC Media Player integration for stream playback
- ✅ Android TV/Fire TV optimized interface
- ✅ Remote control navigation support
- ✅ Channel up/down navigation
- ✅ M3U playlist parsing

## Installation

1. Download the APK from the [Releases](../../releases) section or [Actions](../../actions) artifacts
2. Install VLC Media Player on your Android TV/Fire TV device
3. Enable "Unknown Sources" in your device settings
4. Install the IPTV app APK
5. Launch the app and enter your IPTV server credentials

## Pre-configured Server

This app is pre-configured to work with starshare.net IPTV service.

## Requirements

- Android TV 5.0+ (API level 21+)
- VLC Media Player (will prompt to install if missing)
- Internet connection

## Build Status

![Build Status](https://github.com/beljinp/iptv-android-tv-app/workflows/Build%20Android%20APK/badge.svg)

## How to Build

The APK is automatically built using GitHub Actions. Every push to the main branch triggers a new build.

### Automatic Build with GitHub Actions

1. Push your code to the `main` branch
2. Go to the [Actions](../../actions) tab in your repository
3. Wait for the "Build Android APK" workflow to complete (usually 5-10 minutes)
4. Download the APK from the "Artifacts" section:
   - `iptv-debug-apk` - for testing purposes (automatically built)

### Manual Build (if you have Android Studio)

1. Clone this repository
2. Open in Android Studio
3. Build → Generate Signed Bundle/APK
4. Choose APK and follow the wizard

## Usage

1. **First Launch**: Enter your IPTV server details
   - Host: Your IPTV server URL
   - Username: Your IPTV username  
   - Password: Your IPTV password

2. **Navigation**: Use your TV remote's D-pad to navigate
   - Up/Down: Navigate between categories and items
   - Left/Right: Navigate within categories
   - OK/Enter: Select and play content
   - Back: Return to previous screen

3. **Channel Navigation**: 
   - Channel Up/Down buttons for quick channel switching
   - Number keys for direct channel selection

## Troubleshooting

### VLC Not Installed
- The app will prompt you to install VLC Media Player
- Download VLC from the Google Play Store on your TV device

### Connection Issues
- Verify your internet connection
- Check your IPTV server credentials
- Ensure your IPTV service is active

### Playback Issues
- Make sure VLC is updated to the latest version
- Check if the stream format is supported by VLC
- Verify your IPTV server is working with other players

## License

This project is for educational purposes. Ensure you have proper rights to access any IPTV content.
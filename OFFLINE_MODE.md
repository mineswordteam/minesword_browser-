# Offline Mode Architecture - Minesword Browser

## Network Status Tracking
Minesword Browser tracks network status dynamically using Android's `ConnectivityManager`:
- **ONLINE**: Validated internet connectivity.
- **LOCAL_NETWORK_ONLY**: Connected to WiFi/Ethernet LAN without internet validation.
- **OFFLINE**: No network connection.

Visual banners notify the user when internet connectivity is degraded or offline.

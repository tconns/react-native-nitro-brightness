# react-native-nitro-brightness

Native screen brightness control for React Native built with Nitro Modules.

## Overview

This module provides native-level screen brightness control for both Android and iOS. It exposes simple JS/TS APIs to read, set, and listen to brightness changes with system-level and window-level brightness control.

## Features

- 🔆 Get current screen brightness (0.0 - 1.0)
- ⚙️ Get and set system brightness
- 🪟 Set window-level brightness (app-specific)
- 🔐 Check and request brightness permissions (Android)
- 👂 Listen to brightness change events with callback API
- 🚀 Built with Nitro Modules for native performance and autolinking support
- 📱 Cross-platform support (iOS & Android)

## Requirements

- React Native >= 0.76
- Node >= 18
- `react-native-nitro-modules` must be installed (Nitro runtime)

## Installation

```bash
npm install react-native-nitro-brightness react-native-nitro-modules
# or
yarn add react-native-nitro-brightness react-native-nitro-modules
```

## Configuration

### Android

Add the following permission to `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
```

For system brightness control on Android 6.0+, users need to grant the "Modify system settings" permission. The module will automatically guide users to the settings page when needed.

### iOS

No additional configuration required. iOS apps can control screen brightness without special permissions.

## Quick usage (JS/TS)

```ts
import {
  getBrightness,
  setBrightness,
  getSystemBrightness,
  setSystemBrightness,
  addBrightnessListener,
  removeBrightnessListener,
  getBrightnessPermissions,
  requestBrightnessPermissions,
} from 'react-native-nitro-brightness'

// Get current brightness (0.0 - 1.0)
const currentBrightness = getBrightness()
console.log('Current brightness:', currentBrightness)

// Set app window brightness
setBrightness(0.5) // 50% brightness

// Get system brightness
const systemBrightness = getSystemBrightness()

// Set system brightness (requires permission on Android)
setSystemBrightness(0.8) // 80% brightness

// Listen to brightness changes
const listenerId = addBrightnessListener((brightness) => {
  console.log('Brightness changed:', brightness)
})

// Remove listener when done
removeBrightnessListener(listenerId)
```

## API Reference

### Brightness Control

#### `getBrightness(): number`
Returns the current app window brightness level (0.0 - 1.0).

#### `setBrightness(value: number): void`
Sets the app window brightness level. 
- `value`: Number between 0.0 (darkest) and 1.0 (brightest)

#### `getSystemBrightness(): number`
Returns the current system brightness level (0.0 - 1.0).

#### `setSystemBrightness(value: number): void`
Sets the system brightness level. Requires permission on Android.
- `value`: Number between 0.0 (darkest) and 1.0 (brightest)

#### `getSystemBrightnessMode(): string`
Returns the current brightness mode: `"automatic"` or `"manual"`.

#### `restoreSystemBrightness(): void`
Restores the original system brightness that was active when the module was initialized.

### Permission Management

#### `getBrightnessPermissions(): boolean`
Checks if the app has permission to modify system settings.
- Returns `true` on iOS (always allowed)
- Returns `true`/`false` on Android based on permission status

#### `requestBrightnessPermissions(): Promise<boolean>`
Requests permission to modify system settings (Android only).
- Opens system settings on Android for user to grant permission
- Returns `true` on iOS immediately

### Availability

#### `isAvailable(): boolean`
Checks if brightness control is available on the current device.

### Event Listening

#### `addBrightnessListener(listener: (value: number) => void): number`
Adds a listener for brightness changes.
- `listener`: Callback function that receives the new brightness value
- Returns: Listener ID for removal

#### `removeBrightnessListener(listenerId: number): void`
Removes a brightness change listener.
- `listenerId`: The ID returned from `addBrightnessListener`

## Usage Examples

### Basic Brightness Control

```ts
import { setBrightness, getBrightness } from 'react-native-nitro-brightness'

// Set brightness to 75%
setBrightness(0.75)

// Get current brightness
const brightness = getBrightness()
console.log(`Current brightness: ${Math.round(brightness * 100)}%`)
```

### System Brightness with Permission Check

```ts
import {
  setSystemBrightness,
  getBrightnessPermissions,
  requestBrightnessPermissions,
} from 'react-native-nitro-brightness'

const setSystemBrightnessWithPermission = async (value: number) => {
  // Check if we have permission
  if (!getBrightnessPermissions()) {
    // Request permission
    const granted = await requestBrightnessPermissions()
    if (!granted) {
      console.log('Permission denied')
      return
    }
  }
  
  // Set system brightness
  setSystemBrightness(value)
}

// Usage
setSystemBrightnessWithPermission(0.6) // 60%
```

### React Hook for Brightness Listening

```ts
import { useEffect, useState } from 'react'
import { addBrightnessListener, removeBrightnessListener } from 'react-native-nitro-brightness'

const useBrightness = () => {
  const [brightness, setBrightness] = useState<number>(0)

  useEffect(() => {
    const listenerId = addBrightnessListener((value) => {
      setBrightness(value)
    })

    return () => {
      removeBrightnessListener(listenerId)
    }
  }, [])

  return brightness
}

// Usage in component
const MyComponent = () => {
  const brightness = useBrightness()
  
  return (
    <Text>Current brightness: {Math.round(brightness * 100)}%</Text>
  )
}
```

### Class Component Example

```ts
import React, { Component } from 'react'
import { addBrightnessListener, removeBrightnessListener } from 'react-native-nitro-brightness'

class BrightnessDisplay extends Component {
  state = { brightness: 0 }
  private listenerId: number | null = null

  componentDidMount() {
    this.listenerId = addBrightnessListener(this.handleBrightnessChange)
  }

  componentWillUnmount() {
    if (this.listenerId !== null) {
      removeBrightnessListener(this.listenerId)
    }
  }

  handleBrightnessChange = (brightness: number) => {
    this.setState({ brightness })
  }

  render() {
    return (
      <Text>Brightness: {Math.round(this.state.brightness * 100)}%</Text>
    )
  }
}
```

### Advanced Brightness Manager

```ts
import {
  addBrightnessListener,
  removeBrightnessListener,
  setBrightness,
  restoreSystemBrightness,
} from 'react-native-nitro-brightness'

class BrightnessManager {
  private listeners: number[] = []
  private originalBrightness: number | null = null

  addListener(callback: (value: number) => void): number {
    const listenerId = addBrightnessListener(callback)
    this.listeners.push(listenerId)
    return listenerId
  }

  removeAllListeners() {
    this.listeners.forEach(id => removeBrightnessListener(id))
    this.listeners = []
  }

  setBrightnessTemporarily(value: number) {
    if (this.originalBrightness === null) {
      this.originalBrightness = getBrightness()
    }
    setBrightness(value)
  }

  restoreBrightness() {
    if (this.originalBrightness !== null) {
      setBrightness(this.originalBrightness)
      this.originalBrightness = null
    }
  }

  dispose() {
    this.removeAllListeners()
    this.restoreBrightness()
  }
}
```

## Platform Support

| Feature | iOS | Android |
|---------|-----|---------|
| Get brightness | ✅ | ✅ |
| Set window brightness | ✅ | ✅ |
| Get system brightness | ✅ | ✅ |
| Set system brightness | ✅ | ✅ (with permission) |
| Brightness listeners | ✅ | ✅ |
| Permission management | ✅ (automatic) | ✅ |
| Brightness mode detection | ✅ (manual only) | ✅ |

## Troubleshooting

### Android Permission Issues
- **System brightness not changing**: Make sure the user has granted "Modify system settings" permission
- **Permission dialog not appearing**: Ensure you're targeting Android API 23+ and have the WRITE_SETTINGS permission in AndroidManifest.xml

### iOS Issues
- **Brightness not changing**: Make sure you're setting values between 0.0 and 1.0
- **Listeners not working**: Ensure you're properly removing listeners in component cleanup

### General
- **Memory leaks**: Always remove brightness listeners when components unmount
- **Values out of range**: Brightness values are automatically clamped between 0.0 and 1.0

## Best Practices

1. **Always remove listeners**: Use proper cleanup in `useEffect` or `componentWillUnmount`
2. **Check permissions**: Use `getBrightnessPermissions()` before setting system brightness
3. **Handle permission requests**: Guide users through the permission flow on Android
4. **Validate values**: Ensure brightness values are between 0.0 and 1.0
5. **Restore brightness**: Consider restoring original brightness when your app goes to background

## Migration Notes

When updating spec files in `src/specs/*.nitro.ts`, regenerate Nitro artifacts:

```bash
npx nitro-codegen
```

## Contributing

See `CONTRIBUTING.md` for contribution workflow. Run `npx nitro-codegen` after editing spec files.

## Project Structure

- `android/` — Native Android implementation (Kotlin)
- `ios/` — Native iOS implementation (Swift)
- `src/` — TypeScript API exports
- `nitrogen/` — Generated Nitro artifacts

## Acknowledgements

Special thanks to the following open-source projects which inspired and supported the development of this library:

- [mrousavy/nitro](https://github.com/mrousavy/nitro) – for the Nitro Modules architecture and tooling

## License

MIT © [Thành Công](https://github.com/tconns)

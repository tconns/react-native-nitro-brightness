import type { HybridObject } from 'react-native-nitro-modules'

export interface NitroBrightness
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  getBrightness(): number
  getBrightnessPermissions(): boolean
  requestBrightnessPermissions(): Promise<boolean>
  getSystemBrightness(): number
  getSystemBrightnessMode(): string
  isAvailable(): boolean
  restoreSystemBrightness(): void
  setBrightness(value: number): void
  setSystemBrightness(value: number): void
  addBrightnessListener(listener: (value: number) => void): number
  removeBrightnessListener(listenerId: number): void
}

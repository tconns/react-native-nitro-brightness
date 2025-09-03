import { NitroModules } from 'react-native-nitro-modules'
import type { NitroBrightness as NitroBrightnessSpec } from './specs/NitroBrightness.nitro'

const NitroBrightnessModule =
  NitroModules.createHybridObject<NitroBrightnessSpec>('NitroBrightness')

// MARK: - Brightness Control Functions

export const getBrightness = (): number => {
  return NitroBrightnessModule.getBrightness()
}

export const getBrightnessPermissions = (): boolean => {
  return NitroBrightnessModule.getBrightnessPermissions()
}

export const requestBrightnessPermissions = (): Promise<boolean> => {
  return NitroBrightnessModule.requestBrightnessPermissions()
}

export const getSystemBrightness = (): number => {
  return NitroBrightnessModule.getSystemBrightness()
}

export const getSystemBrightnessMode = (): string => {
  return NitroBrightnessModule.getSystemBrightnessMode()
}

export const isAvailable = (): boolean => {
  return NitroBrightnessModule.isAvailable()
}

export const restoreSystemBrightness = (): void => {
  NitroBrightnessModule.restoreSystemBrightness()
}

export const setBrightness = (value: number): void => {
  NitroBrightnessModule.setBrightness(value)
}

export const setSystemBrightness = (value: number): void => {
  NitroBrightnessModule.setSystemBrightness(value)
}

export const addBrightnessListener = (
  listener: (value: number) => void
): number => {
  return NitroBrightnessModule.addBrightnessListener(listener)
}

export const removeBrightnessListener = (listenerId: number): void => {
  NitroBrightnessModule.removeBrightnessListener(listenerId)
}

// Export the hybrid object itself for advanced use cases
export { NitroBrightnessModule }

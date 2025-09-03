//
//  NitroBrightness.swift
//  NitroBrightness
//
//  Created by tconns94 on 8/21/2025.
//

import UIKit
import NitroModules

class NitroBrightness: HybridNitroBrightnessSpec {
  private var originalBrightness: CGFloat = UIScreen.main.brightness
  private var listeners: [Int: (Double) -> Void] = []
  private var nextListenerId: Int = 1

  // MARK: - Brightness Listener Management

  func addBrightnessListener(listener: @escaping (Double) -> Void) throws -> Double {
    let id = nextListenerId
    nextListenerId += 1
    listeners[id] = listener
    return Double(id)
  }

  func removeBrightnessListener(listenerId: Double) throws {
    let id = Int(listenerId)
    listeners.removeValue(forKey: id)
  }

  private func notifyListeners(value: Double) {
    for (_, listener) in listeners {
      listener(value)
    }
  }

  // MARK: - Brightness Control Methods

  func getBrightness() throws -> Double {
    return Double(UIScreen.main.brightness)
  }

  func getBrightnessPermissions() throws -> Bool {
    // iOS không yêu cầu quyền cho brightness
    return true
  }

  func requestBrightnessPermissions() throws -> Promise<Bool> {
    return Promise<Bool> { resolve, _ in
      // iOS luôn có quyền điều khiển brightness
      resolve(true)
    }
  }

  func getSystemBrightness() throws -> Double {
    return Double(UIScreen.main.brightness)
  }

  func getSystemBrightnessMode() throws -> String {
    // iOS không có mode auto/manual công khai
    return "manual"
  }

  func isAvailable() throws -> Bool {
    return true
  }

  func restoreSystemBrightness() throws {
    UIScreen.main.brightness = originalBrightness
    notifyListeners(value: Double(originalBrightness))
  }

  func setBrightness(value: Double) throws {
    let clampedValue = max(0.0, min(1.0, value)) // Clamp giá trị từ 0.0 đến 1.0
    UIScreen.main.brightness = CGFloat(clampedValue)
    notifyListeners(value: clampedValue)
  }

  func setSystemBrightness(value: Double) throws {
    try setBrightness(value: value)
  }
  
  // MARK: - Memory Management
  
  override var memorySize: Int {
    return MemoryHelper.getSizeOf(self) + listeners.count * 32 // Approximate size per closure
  }
  
  override func dispose() {
    listeners.removeAll()
    super.dispose()
  }
}

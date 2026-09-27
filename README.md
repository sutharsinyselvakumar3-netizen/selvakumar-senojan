# AI COMPANION — Smart Onion Garden Robot
> **“See the Weed. Protect the Onion. Work Smart.”**

Production Android companion application for autonomous onion crop preservation and mechanical weed eradication, communicating over Wi-Fi with dual microcontroller hardware (**ESP32-CAM** and **Main ESP32**).

---

## 1. Project Overview
The **AI COMPANION** Android application is the control, telemetry monitoring, AI workflow coordinator, and computer vision interface for the Smart Onion Garden Robot.
- **Physical Safety Authority**: The **Main ESP32** has the final and absolute physical safety authority over motors, relays, servos, and emergency stop actions. The Android phone acts as the user interface and high-level coordinator.
- **No Simulated / Fake Data**: When microcontrollers or sensors are offline, the application explicitly shows `OFFLINE`, `UNKNOWN`, or `DATA STALE`. No mock or synthetic telemetry values are substituted.
- **UI Design System**: Clean agricultural robotics theme built on **Royal White (`#FAFAFA`)** with **Dark Green (`#145A32`)** accents in Manual Mode, and **Navy Blue (`#0B2A5B`)** accents in Auto Mode. Natural camera and weed colors are preserved with zero distortion or color tinting.

---

## 2. Hardware Architecture
```
                         ANDROID PHONE
                         AI COMPANION
                              │
                              │ Wi-Fi (HTTP / MJPEG)
                    ┌─────────┴─────────┐
                    │                   │
                    ▼                   ▼
              ESP32-CAM             MAIN ESP32
                CAMERA              CONTROLLER
                    │                   │
                    │                   ├── Left Motor (IN1, IN2, PWM)
                    │                   ├── Right Motor (IN1, IN2, PWM)
                    │                   ├── Servo 1 (Arm Base)
                    │                   ├── Servo 2 (Arm Elbow)
                    │                   ├── Servo 3 (Arm Wrist/Tool)
                    │                   ├── Servo 4 (Manual Aux)
                    │                   ├── Servo 5 (Camera PAN)
                    │                   ├── Servo 6 (Camera TILT)
                    │                   ├── Relay 1 (AUTO DRILL - Max 7s)
                    │                   ├── Relay 2 (SOIL)
                    │                   ├── Relay 3 (WATER PUMP)
                    │                   ├── Relay 4 (SIREN/ALARM)
                    │                   ├── Soil Moisture Sensor (Analog)
                    │                   ├── Water Level Sensor (Analog)
                    │                   ├── Battery Voltage (Resistor Divider)
                    │                   ├── MPU6050 (I2C SDA/SCL)
                    │                   └── Vibration Sensor (Digital)
```

---

## 3. ESP32-CAM Setup
- **Role**: Dedicated high-speed camera streaming and image acquisition only.
- **Forbidden**: The ESP32-CAM does **NOT** control chassis motors, arm servos, drills, pumps, or safety systems.
- **Interfaces**:
  - `GET /api/status`: Camera online and health heartbeat.
  - `GET /capture`: Full-resolution single JPEG snapshot.
  - `GET :81/stream`: Multipart MJPEG continuous video stream (`multipart/x-mixed-replace; boundary=frame`).
  - `POST /api/camera/brightness`: Optional sensor hardware register brightness update.

---

## 4. Main ESP32 Setup
- **Role**: Master physical controller and **FINAL SAFETY AUTHORITY**.
- Runs local HTTP REST server on port 80.
- Implements internal watchdog timers: stops motors if no valid command packet is received within 2 seconds.
- Automatically cuts off Relay 1 (AUTO DRILL) after 7 seconds regardless of software command state.

---

## 5. Wiring & Schematics
- Provide separate 5V / 3A buck converter for logic and servos.
- Use a dedicated high-current 12V battery bus for DC chassis drive motors (via H-bridge driver).
- Ground lines (GND) across battery, motor driver, Main ESP32, and ESP32-CAM must be tied together to a common ground bus.

---

## 6. Pinout Reference Table
| Hardware | Function | ESP32 GPIO | Mode / Type |
|---|---|---|---|
| Left Motor | IN1 | GPIO 26 | Digital Output |
| Left Motor | IN2 | GPIO 25 | Digital Output |
| Left Motor | PWM (Speed) | GPIO 23 | PWM Output |
| Right Motor | IN1 | GPIO 27 | Digital Output |
| Right Motor | IN2 | GPIO 32 | Digital Output |
| Right Motor | PWM (Speed) | GPIO 12 | PWM Output |
| Servo 1 | Arm Base Rotation | GPIO 13 | PWM Output (Auto) |
| Servo 2 | Arm Reach / Elbow | GPIO 14 | PWM Output (Auto) |
| Servo 3 | Arm Wrist / Tool Spindle | GPIO 16 | PWM Output (Auto) |
| Servo 4 | Manual Auxiliary (0°–45°) | GPIO 17 | PWM Output (Manual) |
| Servo 5 | Camera PAN Gimbal | GPIO 1 | PWM Output |
| Servo 6 | Camera TILT Gimbal | GPIO 3 | PWM Output |
| Relay 1 | AUTO DRILL | GPIO 18 | Digital Output (Auto Only, Max 7s) |
| Relay 2 | SOIL | GPIO 19 | Digital Output (Manual) |
| Relay 3 | WATER PUMP | GPIO 5 | Digital Output (Manual + Safety Lockout) |
| Relay 4 | SIREN / ALARM | GPIO 15 | Digital Output (Manual + Auto Alert) |
| Soil Moisture | Analog Moisture Sense | GPIO 4 | Analog Input (ADC2) |
| Water Level | Analog Level Sense | GPIO 34 | Analog Input (Input-Only ADC1) |
| Battery Voltage | Voltage Divider | GPIO 35 | Analog Input (Input-Only ADC1) |
| Vibration Sensor | Shock / Knock Input | GPIO 33 | Digital Input |
| MPU6050 SDA | Gyro / Accelerometer | GPIO 21 | I2C Data |
| MPU6050 SCL | Gyro / Accelerometer | GPIO 22 | I2C Clock |
| GPIO 36 | UNUSED | GPIO 36 | Input-Only |

*Note: GPIO 34, 35, and 36 are input-only pins without internal pull-ups/pull-downs. Battery voltage MUST use an external resistor divider (e.g. 100kΩ / 20kΩ) and never be connected directly to GPIO 35.*

---

## 7. Wi-Fi Setup
- Standard topology uses Android phone Mobile Hotspot or field Wi-Fi router.
- Default Hotspot SSID: `AI_GARDEN_HOTSPOT`
- Password: `onionprotect2026`
- Both microcontrollers connect as static or DHCP clients upon boot.

---

## 8. Camera IP Configuration
- Configured in the **SETTINGS** tab under Camera Settings.
- Default Stream URL: `http://192.168.1.51:81/stream`
- Default Capture URL: `http://192.168.1.51:80/capture`
- Double-tapping the camera preview in the application restarts/pauses the stream.

---

## 9. Main ESP32 IP Configuration
- Default IP: `192.168.1.50`
- Default Port: `80`
- Configurable in **SETTINGS** tab with adjustable retry count and response timeout.

---

## 10. API Endpoints
### Main ESP32
- `GET /api/status`: Returns complete JSON telemetry.
- `GET /api/mode`: Returns current mode (`{"mode": "MANUAL"}`).
- `POST /api/mode`: Requests mode switch (`{"mode": "AUTO"}`).
- `POST /api/robot`: Manual chassis movement (`{"command": "FORWARD", "speed": "SLOW"}`).
- `POST /api/servo4`: Manual auxiliary servo (`{"angle": 25}`).
- `POST /api/camera/pantilt`: Gimbal angle adjustment (`{"pan": 90, "tilt": 90}`).
- `POST /api/relay`: Relay actuator control (`{"relay": 2, "state": true}`).
- `POST /api/emergency_stop`: Immediate hardware shutdown (`{"active": true}`).
- `POST /api/target_arm`: Autonomous weed arm positioning (`{"servo1": 90, "servo2": 95, "servo3": 90, "drill": true, "maxDrillSeconds": 7}`).

### ESP32-CAM
- `GET /api/status`: Camera health.
- `GET /capture`: Single JPEG frame byte response.
- `GET :81/stream`: Live MJPEG stream.
- `POST /api/camera/brightness`: Hardware brightness register configuration.

---

## 11. Android Permissions
Declared in `AndroidManifest.xml`:
- `INTERNET`: Communicates with ESP32 REST endpoints and MJPEG stream.
- `ACCESS_NETWORK_STATE` & `ACCESS_WIFI_STATE`: Detects local Wi-Fi connectivity.
- `VIBRATE`: Haptic alerts for dry soil, critical vibration, and e-stop.
- `POST_NOTIFICATIONS`: System alerts on Android 13+.
- `CAMERA`: Capturing weed reference images via phone camera.
- `android:usesCleartextTraffic="true"`: Mandatory for local HTTP IP communication with microcontrollers.

---

## 12. AI Configuration
- Minimum Confidence: Configurable (default `80%`).
- Consecutive Frame Detections: Configurable (default `3`).
- **Onion Protection**: When an onion leaf silhouette is detected (`ONION`), the status transitions to `ONION PROTECTED`. Drilling is strictly prohibited.
- **Unknown Objects**: Ignored (`UNKNOWN — IGNORED`).

---

## 13. Weed Image Upload
- **Exact Slots**: Exactly 3 slots (**WEED 1**, **WEED 2**, **WEED 3**). Slot 4 does not exist.
- Sources: Android Gallery or Device Camera.
- **Original Image Preservation**: Images retain exact native colors, saturation, contrast, and aspect ratio. Stored in internal app storage.

---

## 14. Brightness Control
- Continuous slider (0% to 100%, default 50%).
- Hardware Mode: Sends `POST /api/camera/brightness` to ESP32-CAM.
- Software Preview Mode: If hardware control is unsupported, adjusts render exposure without altering underlying image color balance.
- Includes `RESET TO 50%` button.

---

## 15. Manual Mode
- Accent: **Dark Green (`#145A32`)**.
- Movement: Exactly 4 buttons (`FORWARD`, `LEFT`, `RIGHT`, `REVERSE`). Hold-to-move, release-to-stop.
- Speeds: `SLOW`, `MEDIUM`, `FAST`.
- Gimbal: Sliders for PAN and TILT + `HOME` and `CENTER`.
- Auxiliary Servo 4: Strictly limited to 0°–45°.
- Relays: Toggle switches for Relay 2 (Soil), Relay 3 (Water Pump), Relay 4 (Siren).
- **Relay 1 AUTO DRILL**: Has NO manual button.

---

## 16. Auto Mode
- Accent: **Navy Blue (`#0B2A5B`)**.
- Engaged only after user confirms dialog and passes 11-step pre-flight safety checklist.
- Robot scans crops slowly; confirmed weeds trigger the 3-DOF targeting and drilling sequence.

---

## 17. Safety System
Automatic operations halt immediately upon:
- Critical Battery Voltage (`< 10.2V`)
- Chassis Instability / Tilt (`MPU6050 Unstable`)
- High Vibration Threshold (`GPIO 33 Critical`)
- Critical Low Water (Pump Lockout)
- Physical Emergency Stop button activation

---

## 18. Relay Functions
- **Relay 1 (AUTO DRILL)**: Engaged solely by autonomous weed targeting logic. Hard 7-second auto shut-off.
- **Relay 2 (SOIL)**: Soil aeration/conditioning solenoid.
- **Relay 3 (WATER PUMP)**: Irrigates soil; locked out when water is low.
- **Relay 4 (SIREN / ALARM)**: Emits warning buzzer during safety faults and dry soil alarms.

---

## 19. Sensor Calibration
- Calibration offsets for Soil Moisture and Water Level sensors in the Settings menu.
- MPU6050 pitch and roll angle limits (default `25.0°`).

---

## 20. Arm Calibration & Inverse Kinematics
- Converts Camera Pixel `(X, Y)` $\rightarrow$ Optical Center Offset $\rightarrow$ Ground Distance `(cm)` $\rightarrow$ Arm Mount Origin $\rightarrow$ 3-DOF Kinematic Angles for Servos 1, 2, and 3.
- Link lengths: Link 1 (`12 cm`), Link 2 (`10 cm`), Tool Spindle (`8 cm`).
- Angle offsets for Servos 1, 2, and 3 stored in settings.

---

## 21. Troubleshooting
1. **ESP32 OFFLINE**: Verify phone is connected to the garden hotspot and IP address matches `192.168.1.50`.
2. **STREAM NOT AVAILABLE**: Ensure ESP32-CAM is powered and stream server on port 81 is running. Tap `RETRY`.
3. **AUTO MODE BLOCKED**: Check the warning banner for the exact safety rejection reason (e.g., low battery, unstable MPU, or camera offline).
4. **DATA STALE**: Indicated when status updates have not been received for over 4 seconds. Check wireless signal strength.

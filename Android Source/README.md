# 👁️ OpticView – AI Vision Assistant for the Visually Impaired

**OpticView** is an AI-powered real-time vision assistant built to support visually impaired individuals by helping them understand their surroundings through spoken feedback.

The app automatically captures an image every 30 seconds using your device's **rear camera**, analyzes the scene using advanced AI (Meta LLaMA-4 on Groq), and then **speaks out a simple, helpful description** — focusing on people, objects, signs, and potential dangers.

---

## 🌐 Try the Web Demo

🔗 [**https://opticview-prototype.onrender.com/**](https://opticview-prototype.onrender.com/)

> ✅ Open it on your **mobile device browser**, **allow camera access**, and **hold for 30 seconds** to experience it in action.

---

## 📱 Android Beta Release

Try the Android app for a more seamless and native experience:

📥 [**Download Android Beta Release**](https://github.com/sidinsearch/OpticView/releases/tag/Beta)

---

## 🚀 Features

- 📸 Uses the **rear camera** of mobile devices for real-world capture.
- 🔄 **Auto-captures** images every 30 seconds, no button presses needed.
- 🧠 Uses **Groq's LLaMA-4** to describe your surroundings.
- 🗣️ Audio-only feedback via the browser (no on-screen text).
- ☁️ Lightweight Flask backend (Python).
- 📱 **Responsive mobile-first UI** with TailwindCSS.
- 📲 Android app version (coming soon!).

---

## 📸 How It Works

1. The browser requests permission to use your **rear camera**.
2. Every 30 seconds, it takes a snapshot of your surroundings.
3. The image is sent to the backend, encoded in Base64.
4. Using Groq’s blazing-fast LLM (LLaMA 4), it analyzes and generates a description:
   - Simple, clear language.
   - Focus on people, objects, and **potential hazards**.
   - Avoids the word “image” to make narration natural.
5. The description is then **spoken aloud** using the Web Speech API.

---

## 🧪 Web Version – Local Setup

> This is perfect for developers or testers running the app on their own device.

### ✅ Prerequisites

- Python 3.10+
- pip

### 🔧 Installation

```bash
git clone https://github.com/your-org/opticview.git
cd Web Version
pip install -r requirements.txt
```

### ▶️ Run Locally

```bash
python app.py
```

Open `http://localhost:5000` on your **phone’s browser**, grant camera access, and test the app.

---

## 📲 Android App 

We're currently working on an Android version for native performance and offline support. Stay tuned here and on our GitHub releases.

---

## 🎥 Demo

> 📽️ **Make Sure To Enable Sound **





https://github.com/user-attachments/assets/45cf2a55-f15c-43d9-9d79-3700e188c0fa




---

## 🧠 Tech Stack

| Layer       | Technology                                                                 |
|-------------|----------------------------------------------------------------------------|
| Frontend    | HTML5, TailwindCSS, WebRTC, JavaScript                                     |
| Backend     | Python, Flask, Gunicorn                                                    |
| AI Model    | Meta LLaMA-4 Maverick (via Groq API)                                       |
| Audio       | Web Speech API (Browser TTS), pyttsx3 (TTS on Raspberry Pi)                |
| Image Proc. | OpenCV (Image capture and processing)                                      |
| Android     | Android Studio, Java, Android SDK, CameraX, Retrofit, Gson, Logcat, Gradle |


## 📱 Android-Specific Stack

| Technology         | Purpose                                                             |
|--------------------|----------------------------------------------------------------------|
| **Java**           | Core Android App development                                         |
| **Android Studio** | Primary development IDE                                              |
| **CameraX**        | Real-time image capture via rear camera                              |
| **Base64 Encoder** | Convert image to Base64 string for API transmission                  |
| **Retrofit**       | HTTP client for API communication                                    |
| **Gson**           | JSON parsing and serialization                                       |
| **Volley**         | Alternative HTTP request handling (optional)                         |
| **Logcat**         | Debugging and real-time logging during app execution                 |
| **Gradle**         | Dependency management and project building                           |
| **Postman**        | API testing and validation                                           |
| **GitHub**         | Version control and collaborative development                        |



## 🧪 Deployment Notes

- Tested & deployed on **Render** (link above).
- Works best on Chrome or Chromium-based mobile browsers.
- The Android application is compatible with all devices running Android 8.0 (Oreo) and above.
- Ensure camera permission is enabled.

---

## 👥 Contributors

Made with ❤️ by:

- [JaidTamboli](https://github.com/JaidTamboli)
- [Prathamesh0901](https://github.com/Prathamesh0901)
- [sidinsearch](https://github.com/sidinsearch/)
- [rrr77718](https://github.com/rrr77718)

---

## 📜 License

This project is licensed under the **MIT License** — feel free to use, modify, and contribute!

> If you find this useful, don’t forget to ⭐️ star the repo and share the demo!

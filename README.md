# RayVita: Contactless Health Monitoring & AI-Powered Wellness App (Beta) 🌿

<p align="center">
  <img src="https://via.placeholder.com/150/00C4B4/FFFFFF?text=RayVita" alt="RayVita Logo" width="150"/>
</p>

<p align="center">
  <strong>Revolutionizing health monitoring with remote photoplethysmography (rPPG)</strong><br>
  The world’s first smartphone-based app to measure heart rate (HR) and heart rate variability (HRV) using only your phone’s camera. Powered by AI, RayVita offers personalized insights, stunning themes, and a vibrant community for collaborative wellness.
</p>

<p align="center">
  <a href="http://47.96.237.130/" target="_blank"><img src="https://img.shields.io/badge/Download-App-00C4B4?style=flat-square&logo=android" alt="Download App"/></a>
  <a href="https://github.com/Nickory/rayvita_api" target="_blank"><img src="https://img.shields.io/badge/API%20Repository-rayvita_api-00C4B4?style=flat-square&logo=github" alt="API Repository"/></a>
  <a href="mailto:zhwang@nuist.edu.cn"><img src="https://img.shields.io/badge/Contact-zhwang@nuist.edu.cn-00C4B4?style=flat-square" alt="Contact"/></a>
</p>

---

## 🌟 Key Features

- **Contactless rPPG Scan**  
  Measure HR and HRV with <5% Mean Absolute Error (MAE) using facial video analysis, powered by Contrast-Phys+ and 3DCNN-based spatiotemporal contrastive learning.  
  <small>*Reference: Sun, Z., & Li, X. (2023). IEEE T-PAMI*</small>

- **AI Health Assistant**  
  Meet *Little R*, your interactive AI agent delivering daily personalized health tips tailored to your habits.

- **Dynamic Visual Themes**  
  Choose from 10+ Material Design-based themes (e.g., Warm Earth, Ocean Blue, Violet Dream) or create custom themes with DeepSeek AI.  

- **Health History & Trends**  
  Visualize physiological changes with smooth charts, weekly/monthly summaries, and secure cloud sync via JWT authentication.

- **Social Community**  
  Share health insights, post achievements, and engage in friend-based challenges to boost motivation.

- **Multilingual Experience**  
  Seamless language switching (English, Simplified Chinese) with more languages planned for global accessibility.

- **Developer Playground**  
  Debug tools for theme injection, cache inspection, and force logout functionality.

---

## 📱 App Preview

<div align="center">
  <img src="https://via.placeholder.com/300x150/00C4B4/FFFFFF?text=Home+Page" alt="Home Page" width="200"/>
  <img src="https://via.placeholder.com/300x150/00C4B4/FFFFFF?text=Health+Trends" alt="Health Trends" width="200"/>
  <img src="https://via.placeholder.com/300x150/00C4B4/FFFFFF?text=AI+Assistant" alt="AI Assistant" width="200"/>
</div>

- **Home Page**: Quick access to health data, breathing exercises, daily tips, and dynamic banners.
- **Health History**: Track HR/HRV trends with intuitive charts and secure cloud sync.
- **RayVita-Synapse**: A lightweight web hub for cross-platform health data visualization and social syncing.  
  <a href="http://47.96.237.130/" target="_blank">Download the App</a>
- **Settings**: Customize language, notifications, and themes for a tailored experience.

---

## 🛠️ Technical Architecture

### Mobile
- **Framework**: Kotlin + Jetpack Compose + MVVM
- **Design**: Material Design principles for a sleek, responsive UI
- **Inference**: ONNX Runtime for low-latency, privacy-focused edge ML
- **Storage**: Room for local data, Retrofit for API integration

### Backend
- **APIs**: Flask REST APIs (Python) with JWT authentication  
  <a href="https://github.com/Nickory/rayvita_api" target="_blank">API Repository</a>
- **Database**: MySQL/MariaDB on Alibaba Cloud for efficient CRUD operations
- **AI Integration**: DeepSeek API for theme generation and health tips

### rPPG Technology
- **Contrast-Phys+**: Unsupervised/weakly-supervised 3DCNN model for robust HR/HRV extraction from facial videos.  
- **Reference**: Sun, Z., & Li, X. (2023). *Contrast-Phys+: Unsupervised and Weakly-supervised Video-based Remote Physiological Measurement via Spatiotemporal Contrast*. IEEE T-PAMI.

---

## 🚀 Future Roadmap

- **RayVita Perpetu**  
  Enhanced Android model with federated learning for multi-user collaboration and personalized model fine-tuning using hardware-collected ground truth.

- **VITA Vision**  
  - **V**itality: Atrial fibrillation detection from facial videos.  
  - **I**nsight: Real-time health data processing with advanced algorithms.  
  - **T**echnology: HRV-based AI themes and personalized exercise plans.  
  - **A**ction: Emotion and stress tracking via video and computer vision.

- **Community Features**  
  Image posts, friend-based challenges, and an expanded social feed.

- **Global Accessibility**  
  Additional language support to reach a broader audience.

---

## 📦 Installation

### Prerequisites
- Android Studio (latest stable version)
- Kotlin 1.9+
- Node.js and Python 3.8+ for backend setup
- MySQL/MariaDB for database
- DeepSeek API key (for AI features)

### Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Nickory/rayvita.git
   cd rayvita
   ```
2. **Mobile App**:
   - Open the project in Android Studio.
   - Sync Gradle and build the app.
   - Configure `local.properties` for API endpoints and DeepSeek API key.
3. **Backend**:
   - Clone the API repository:  
     ```bash
     git clone https://github.com/Nickory/rayvita_api.git
     ```
   - Install dependencies:  
     ```bash
     cd rayvita_api
     pip install -r requirements.txt
     ```
   - Set up MySQL/MariaDB and update `.env` with database credentials.
   - Run the Flask server:  
     ```bash
     python app.py
     ```
4. **Run the App**:
   - Deploy the app on an Android device/emulator.
   - Ensure the backend server is running for API connectivity.

---

## 🤝 Contributing

We welcome contributions to enhance RayVita! To contribute:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/YourFeature`).
3. Commit changes (`git commit -m 'Add YourFeature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a Pull Request with a clear description.

Please review our [Code of Conduct](CODE_OF_CONDUCT.md) and [Contributing Guidelines](CONTRIBUTING.md).

---

## 📚 Documentation

- [API Documentation](https://github.com/Nickory/rayvita_api/wiki)
- [Developer Playground Guide](docs/developer_playground.md)
- [rPPG Technical Overview](docs/rppg_technical.md)

---

## 🙌 Acknowledgments

- **Team**: Ziheng Wang, Dongxu Xia, Di Wu, Renzhe Zhao
- **Instructors**: 
  - Professor Baowei Wang
  - Professor Xu Cheng
  - Associate Professor Haibin Wang
  - Dr. Rui Su
  - Dr. Zhaodong Sun
  - Professor Xiaobai Li
- **Instructor**: Associate Professor Weiwei Jiang
- **Technology**: DeepSeek, ONNX Runtime, Alibaba Cloud

---

## 📬 Contact

- **Email**: <a href="mailto:zhwang@nuist.edu.cn">zhwang@nuist.edu.cn</a>
- **Help Center**: 30+ FAQs on data, syncing, and privacy (in-app).
- **Feedback**: Submit via the [RayVita Issues Page](https://github.com/Nickory/rayvita/issues).
- **Community**: Join our vibrant community for updates and collaboration!

---

<p align="center">
  <strong>© 2025 RayVita. All rights reserved.</strong><br>
  Together, let’s redefine health monitoring with rPPG and AI-driven insights! 🌍
</p>

<p align="center">
  <a href="http://47.96.237.130/" target="_blank"><img src="https://img.shields.io/badge/Download-RayVita-00C4B4?style=for-the-badge&logo=android" alt="Download RayVita"/></a>
</p>

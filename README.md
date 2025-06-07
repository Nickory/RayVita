RayVita: Contactless Health Monitoring & AI-Powered Wellness App (Beta)
🚀 RayVita is the world's first smartphone-based health monitoring app leveraging remote photoplethysmography (rPPG) to measure heart rate (HR) and heart rate variability (HRV) using only your phone's camera. Powered by AI, RayVita delivers personalized health insights, dynamic themes, and a social community for collaborative wellness.
🌟 Features

Contactless rPPG Scan: Measure HR and HRV with <5% Mean Absolute Error (MAE) using facial video analysis, powered by Contrast-Phys+ and 3DCNN-based spatiotemporal contrastive learning.
AI Health Assistant: Daily personalized health tips and an interactive AI agent ("Little R") for tailored guidance.
Dynamic Visual Themes: Over 10 Material Design-based themes (e.g., Warm Earth, Ocean Blue) with AI-generated custom themes via DeepSeek API.
Health History & Trends: Visualize HR/HRV trends with weekly/monthly summaries and secure cloud sync using JWT authentication.
Social System: Share health insights, post achievements, and engage in friend-based challenges.
Multilingual Support: English and Simplified Chinese, with more languages planned.
Developer Playground: Debug tools for theme injection, cache inspection, and force logout functionality.

📱 App Preview

Home Page: Quick access to health data, breathing exercises, daily tips, and dynamic banners.
Health History: Smooth charts for tracking physiological changes over time.
RayVita-Synapse: Lightweight web hub for cross-platform data visualization and social syncing.
Settings: Customize language, notifications, and themes for a personalized experience.

🛠️ Technical Architecture
Mobile

Framework: Kotlin + Jetpack Compose + MVVM
Design: Material Design principles for a sleek, responsive UI
Inference: ONNX Runtime for low-latency, privacy-focused edge ML
Storage: Room for local data, Retrofit for API calls

Backend

APIs: Flask REST APIs (Python) with JWT authentication
Database: MySQL/MariaDB on Alibaba Cloud for efficient CRUD operations
AI Integration: DeepSeek API for theme generation and health tips

rPPG Technology

Contrast-Phys+: Unsupervised/weakly-supervised 3DCNN model for robust HR/HRV extraction from facial videos.
Reference: Sun, Z., & Li, X. (2023). Contrast-Phys+: Unsupervised and Weakly-supervised Video-based Remote Physiological Measurement via Spatiotemporal Contrast. IEEE T-PAMI.

🚀 Future Plans

RayVita Perpetu: Enhanced Android model with federated learning for multi-user collaboration and personalized model fine-tuning using hardware-collected ground truth.
VITA Roadmap:
Vitality: Atrial fibrillation detection from facial videos.
Insight: Real-time health data processing with advanced algorithms.
Technology: Personalized HRV-based AI themes and exercise plans.
Action: Emotion and stress tracking via video and computer vision.


Community Features: Image posts, friend-based challenges, and expanded social feed.
Global Accessibility: Additional language support for broader reach.

📦 Installation
Prerequisites

Android Studio (latest stable version)
Kotlin 1.9+
Node.js and Python 3.8+ for backend setup
MySQL/MariaDB for database
DeepSeek API key (for AI features)

Setup Instructions

Clone the Repository:git clone https://github.com/Nickory/rayvita.git
cd rayvita


Mobile App:
Open the project in Android Studio.
Sync Gradle and build the app.
Configure local.properties for API endpoints and DeepSeek API key.


Backend:
Clone the API repository:  git clone https://github.com/Nickory/rayvita_api.git


Install dependencies:  cd rayvita_api
pip install -r requirements.txt


Set up MySQL/MariaDB and update .env with database credentials.
Run the Flask server:  python app.py




Run the App:
Deploy the app on an Android device/emulator.
Ensure the backend server is running for API connectivity.



🤝 Contributing
We welcome contributions to make RayVita even better! To contribute:

Fork the repository.
Create a feature branch (git checkout -b feature/YourFeature).
Commit changes (git commit -m 'Add YourFeature').
Push to the branch (git push origin feature/YourFeature).
Open a Pull Request with a clear description.

Please follow our Code of Conduct and check the Contributing Guidelines for details.
📚 Documentation

API Documentation
Developer Playground Guide
rPPG Technical Overview

🙌 Acknowledgments

Team: Ziheng Wang, Dongxu Xia, Di Wu, Renzhe Zhao
Instructor: Associate Professor Weiwei Jiang
Technology: DeepSeek, ONNX Runtime, Alibaba Cloud
Reference: Sun, Z., & Li, X. (2023). IEEE T-PAMI

📬 Contact

Help Center: 30+ FAQs on data, syncing, and privacy (accessible in-app).
Feedback: Reach out via the RayVita Issues Page.
Community: Join our growing community for updates and collaboration!


© 2025 RayVita. All rights reserved.
Together, let’s revolutionize health monitoring with rPPG and AI-driven insights! 🌍

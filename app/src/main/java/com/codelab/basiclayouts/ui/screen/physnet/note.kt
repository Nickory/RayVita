//physnet/
//├── ui/
//│   └── /screen/physnet/
//│       ├── RppgScreen.kt              // 主界面（Compose UI）
//│       ├── RppgCameraPreview.kt       // 相机预览与辅助对齐框
//│       ├── RppgResultCard.kt          // 心率展示卡片
//│       ├── RppgWaveform.kt            // 波形图组件
//│       └── RppgLoadingOverlay.kt      // 高级加载动画覆盖层
//├── data/
//│   └── physnet/
//│       ├── RppgInference.kt           // 模型加载与推理（ONNX）
//│       ├── RppgProcessor.kt           // 包装推理流程（帧→输入→推理→心率）
//│       ├── VideoRecorder.kt           // 相机采样封装（输出 Bitmap 列表）
//│       ├── RppgRepository.kt          // 数据持久化与上传（API/JSON）
//│       └── model/
//│           └── RppgResult.kt          // 数据模型（波形+心率+时间戳）
//├── viewmodel/
//│   └── physnet/
//│       └── RppgViewModel.kt           // 状态管理，协调录制、推理、结果
//
//// 说明：
//// 1. 所有 ui 层均使用 Material3 风格构建，按钮/波形卡片等统一使用 theme 样式。
//// 2. RppgViewModel 管理：isRecording、isProcessing、result（心率+波形）、errorMessage。
//// 3. 所有数据层与模型独立组织于 data/physnet 模块。
//// 4. 加载动画由 RppgLoadingOverlay.kt 管理，可在 isProcessing = true 时显示。
//// 5. 所有测量结束后由 RppgRepository 提供：上传结果（to API）和本地保存 JSON。
//// 6. 拍摄视频帧（VideoRecorder）采样后统一送入 RppgProcessor，调用 RppgInference 完成。

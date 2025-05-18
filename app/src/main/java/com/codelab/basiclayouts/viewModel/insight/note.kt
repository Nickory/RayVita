//ui/insight/
//│
//├── InsightScreen.kt            # UI 页面主入口（Compose 设计，调用 ViewModel）
//├── InsightCard.kt              # 显示最近测量数据的卡片
//├── InsightPromptCard.kt       # AI 智能体响应展示卡片（50字建议）
//│
//viewmodel/insight/
//│
//├── InsightViewModel.kt        # 管理最近数据状态和 AI 请求逻辑
//│
//network/
//├── DeepSeekApi.kt             # AI 接口定义（基于你提供的接口）
//├── RetrofitClient.kt          # Retrofit 单例（可共用）
//├── model/InsightAiModels.kt   # Message / ChatRequest / ChatResponse 数据类

//ui/
//├── profile/
//│   ├── ProfileScreen.kt           # 我的页面主入口（展示用户信息，跳转登录/注册）
//│   ├── LoginScreen.kt             # 登录界面（Compose + 调用 ViewModel）
//│   ├── RegisterScreen.kt          # 注册界面
//│
//viewmodel/profile/
//│   └── AuthViewModel.kt           # 管理登录、注册、用户状态等逻辑
//│
//network/
//├── AuthApi.kt                     # 用户登录注册接口定义
//├── RetrofitClient.kt              # 公共 Retrofit 单例（已存在，可复用）
//│
//model/
//├── UserModels.kt                  # 包含 LoginRequest, RegisterRequest, UserInfo 等数据类

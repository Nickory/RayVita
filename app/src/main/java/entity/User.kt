package entity
import org.threeten.bp.LocalDateTime
//import java.time.LocalDateTime

// 枚举定义与数据库 ENUM 严格对应
enum class ThemePreference { light, dark, R }
enum class AccountStatus { active, deactivated }

// 主用户数据类（仅属性定义）
data class User(
    // 主键允许为空（插入时自增）
    val userId: Long? = null,

    // NOT NULL 字段用非空类型
    val email: String,
    val passwordHash: String,

    // 可空字段用可空类型
    val nickname: String? = null,
    val avatarUrl: String? = null,

    // 带默认值的枚举字段
    val themePref: ThemePreference = ThemePreference.light,

    // 时间字段使用 LocalDateTime
    val registrationDt: LocalDateTime = LocalDateTime.now(),
    val lastLoginDt: LocalDateTime? = null,

    // 带默认值的状态枚举
    val status: AccountStatus = AccountStatus.active,

    // 设备信息字段
    val deviceInfo: String? = null
)
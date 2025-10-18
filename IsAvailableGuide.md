# isAvailable() 方法使用指南

## 🎯 什么是 isAvailable()

`isAvailable()` 是 StepStep 接口中的一个方法，用于**动态控制步骤是否执行**。

- 返回 `true`：步骤会被执行
- 返回 `false`：步骤会被跳过，引擎自动查找下一个可用步骤

## 🔍 工作原理

```
Step引擎执行流程：
  │
  ▼
查找下一个步骤
  │
  ▼
调用 step.isAvailable()
  │
  ├─ true ──▶ 执行 onStepStarted()
  │
  └─ false ─▶ 跳过该步骤，继续查找下一个
```

引擎代码（StepEngine.kt 第160行）：
```kotlin
while (nextIndex < steps.size) {
    val nextStep = steps[nextIndex]
    if (nextStep.isAvailable()) {  // ← 在这里检查
        // 执行步骤
        nextStep.onStepStarted(provider)
        return
    }
    nextIndex++  // 跳过不可用的步骤
}
```

## 📝 如何使用

### 方式1：在自定义步骤中重写（推荐）

```kotlin
class PermissionStepStep : BaseStep() {
    
    override fun getStepId(): String = "PermissionStep"
    
    /**
     * 重写 isAvailable() 方法
     * 只有在权限未授予时才执行此步骤
     */
    override fun isAvailable(): Boolean {
        // 检查权限是否已授予
        val hasPermission = checkPermissions()
        logI("isAvailable: hasPermission=$hasPermission")
        return !hasPermission  // 没有权限时才需要执行
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        // 请求权限...
        requestPermissions()
    }
    
    private fun checkPermissions(): Boolean {
        // 实际的权限检查逻辑
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

### 方式2：使用构造函数参数

```kotlin
class ConditionalStepStep(
    private val condition: () -> Boolean  // 传入条件函数
) : BaseStep() {
    
    override fun getStepId(): String = "ConditionalStep"
    
    override fun isAvailable(): Boolean {
        return condition()  // 使用传入的条件
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        // 执行业务逻辑
    }
}

// 使用：
val engine = StepApi.createStepEngineBuilder()
    .addStep(ConditionalStepStep { isFirstTimeStep() })
    .addStep(ConditionalStepStep { needsUpdate() })
    .build()
```

### 方式3：基于配置参数

```kotlin
class OptionalStepStep(
    private val config: StepConfig
) : BaseStep() {
    
    override fun getStepId(): String = "OptionalStep"
    
    override fun isAvailable(): Boolean {
        // 根据配置决定是否执行
        return when {
            config.skipOptionalSteps -> false
            config.isTransferMode -> false
            config.deviceModel == "SpecialModel" -> true
            else -> true
        }
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        // 执行业务逻辑
    }
}
```

## 📚 实际应用场景示例

### 场景1：首次设置 vs 重新设置

```kotlin
class WelcomeStepStep(
    private val isFirstTime: Boolean
) : BaseStep() {
    
    override fun isAvailable(): Boolean {
        // 只在首次设置时显示欢迎页面
        return isFirstTime
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        showWelcomeScreen()
    }
}

// 使用：
val isFirstTime = preferences.getBoolean("is_first_time", true)
val engine = StepApi.createStepEngineBuilder()
    .addStep(WelcomeStepStep(isFirstTime))  // 首次会显示，之后会跳过
    .addStep(ConnectionStepStep())
    .build()
```

### 场景2：根据设备类型

```kotlin
class NfcGuideStepStep(
    private val deviceModel: String
) : BaseStep() {
    
    override fun isAvailable(): Boolean {
        // 只有支持NFC的设备型号才显示NFC引导
        return deviceModel in listOf("Model_A", "Model_B", "Model_C")
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        showNfcGuide()
    }
}
```

### 场景3：根据系统版本

```kotlin
class CompanionDeviceManagerStep : BaseStep() {
    
    override fun isAvailable(): Boolean {
        // 只在 Android 12 (API 31) 及以上版本执行
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        // 使用 Companion Device Manager API
        associateDevice()
    }
}
```

### 场景4：根据用户权限状态

```kotlin
class PermissionStepStep(
    private val context: Context,
    private val permissions: List<String>
) : BaseStep() {
    
    override fun isAvailable(): Boolean {
        // 检查是否有未授予的权限
        val hasAllPermissions = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == 
                PackageManager.PERMISSION_GRANTED
        }
        
        logI("isAvailable: hasAllPermissions=$hasAllPermissions")
        return !hasAllPermissions  // 如果全部已授予，跳过此步骤
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        requestPermissions(permissions)
    }
}
```

### 场景5：根据业务状态

```kotlin
class DataMigrationStep(
    private val dataManager: DataManager
) : BaseMyStep() {
    
    override fun isAvailable(): Boolean {
        // 只有在有旧数据需要迁移时才执行
        val hasOldData = dataManager.hasOldVersionData()
        logI("isAvailable: hasOldData=$hasOldData")
        return hasOldData
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        // 执行数据迁移
        migrateOldData()
    }
}
```

## ⚠️ 重要注意事项

### 1. isAvailable() 可能被多次调用

```kotlin
override fun isAvailable(): Boolean {
    // ⚠️ 这个方法可能被多次调用
    // 不要在这里执行耗时操作！
    
    // ❌ 错误示例：
    // val data = fetchDataFromNetwork()  // 耗时操作
    // return data != null
    
    // ✅ 正确示例：
    return cachedData != null  // 快速返回
}
```

### 2. 不要依赖执行顺序

```kotlin
override fun isAvailable(): Boolean {
    // ⚠️ 不要假设其他步骤已经执行
    
    // ❌ 错误：假设 Step1 已经执行
    // return Step1.result != null
    
    // ✅ 正确：独立检查条件
    return checkMyOwnCondition()
}
```

### 3. 保持简单和快速

```kotlin
override fun isAvailable(): Boolean {
    // ✅ 推荐：简单的条件判断
    return Build.VERSION.SDK_INT >= 31
    
    // ✅ 推荐：检查缓存的状态
    return preferences.getBoolean("need_Step", true)
    
    // ❌ 不推荐：复杂的计算
    // return calculateComplexCondition()
    
    // ❌ 不推荐：网络请求
    // return fetchFromServer()
}
```

### 4. 添加日志便于调试

```kotlin
override fun isAvailable(): Boolean {
    val available = checkCondition()
    logI("isAvailable: $available (reason: ...)")  // 记录判断结果
    return available
}
```

## 🔄 完整的工作流程示例

```kotlin
// 定义步骤
class Step1 : BaseMyStep() {
    override fun isAvailable() = true  // 总是执行
    override fun getStepId() = "Step1"
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        logI("Step1 执行")
        finish()
    }
}

class Step2 : BaseMyStep() {
    override fun isAvailable() = false  // 总是跳过
    override fun getStepId() = "Step2"
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        logI("Step2 执行")  // 这个不会被打印
        finish()
    }
}

class Step3 : BaseMyStep() {
    override fun isAvailable() = true  // 总是执行
    override fun getStepId() = "Step3"
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        logI("Step3 执行")
        finish()
    }
}

// 创建引擎
val engine = MyStepApi.createStepEngineBuilder()
    .addStep(Step1())
    .addStep(Step2())  // 这个会被跳过
    .addStep(Step3())
    .build()

// 监听步骤变化
lifecycleScope.launch {
    engine.getStepChangeFlow().collect { change ->
        change?.let {
            logI("当前步骤: ${it.currentStep?.getStepId()}")
        }
    }
}

// 启动
engine.start()

// 输出：
// [MyStep#Step1] isAvailable
// [MyStep#Step1] onStepStarted
// [MyStep#Step1] Step1 执行
// 当前步骤: Step1
// [MyStep#Step1] finish
// [MyStep#Step2] isAvailable  ← Step2的isAvailable被调用
// [MyStep#Step3] isAvailable  ← 但因为返回false，跳过Step2，检查Step3
// [MyStep#Step3] onStepStarted
// [MyStep#Step3] Step3 执行
// 当前步骤: Step3
```

## 📊 与Google Step SDK对比

| 特性 | Google Step SDK | MyStep Framework |
|------|------------------|-------------------|
| 支持 isAvailable | ✅ | ✅ |
| 默认返回值 | true | true |
| 重写方式 | override | override |
| 是否必须实现 | ❌ (有默认值) | ❌ (有默认值) |
| 多次调用 | ✅ 可能 | ✅ 可能 |

## 🎯 最佳实践

1. ✅ **重写方法时添加注释**，说明判断逻辑
2. ✅ **添加日志**，便于调试
3. ✅ **保持快速**，不要执行耗时操作
4. ✅ **独立判断**，不要依赖其他步骤的状态
5. ✅ **考虑缓存**，如果判断逻辑复杂，可以缓存结果

## 💡 高级技巧

### 动态调整可用性

```kotlin
class DynamicStepStep : BaseMyStep() {
    
    private var _isAvailable = true
    
    /**
     * 外部可以动态修改可用性
     */
    fun setAvailable(available: Boolean) {
        _isAvailable = available
    }
    
    override fun isAvailable(): Boolean {
        return _isAvailable
    }
    
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        // ...
    }
}

// 使用：
val dynamicStep = DynamicStepStep()
val engine = MyStepApi.createStepEngineBuilder()
    .addStep(dynamicStep)
    .build()

// 根据某些条件动态调整
if (someCondition) {
    dynamicStep.setAvailable(false)  // 动态设置为不可用
}

engine.start()
```

### 组合多个条件

```kotlin
class ComplexConditionStep(
    private val config: Config
) : BaseMyStep() {
    
    override fun isAvailable(): Boolean {
        // 组合多个条件
        return when {
            !config.enabled -> {
                logI("isAvailable: false (disabled in config)")
                false
            }
            Build.VERSION.SDK_INT < 28 -> {
                logI("isAvailable: false (SDK version too low)")
                false
            }
            !hasRequiredFeature() -> {
                logI("isAvailable: false (missing required feature)")
                false
            }
            else -> {
                logI("isAvailable: true")
                true
            }
        }
    }
    
    private fun hasRequiredFeature(): Boolean {
        // 检查设备是否支持某个特性
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
}
```

## 📖 总结

`isAvailable()` 是一个强大的方法，允许你：

✅ 根据条件动态控制步骤是否执行  
✅ 实现灵活的流程分支  
✅ 避免不必要的步骤  
✅ 提升用户体验（跳过已完成的步骤）

记住要**重写这个方法**来实现你的业务逻辑，不要依赖默认的 `true` 返回值！


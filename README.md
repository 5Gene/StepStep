# StepStep Framework

一个仿照Google Step SDK设计的完整Step框架，支持灵活的步骤管理和流程控制。

## 架构设计

### 核心类

1. **StepStep** - Step步骤接口
   - 定义步骤的生命周期方法
   - 支持 isAvailable、onStepStarted、onStepResumed、onStepStopped、cleanup

2. **MyStepCompletionProvider** - 步骤完成提供者
   - 控制步骤流转：finish()、navigateBack()、abortStep()

3. **StepEngine** - Step引擎
   - 管理步骤执行流程
   - 维护执行历史栈
   - 提供数据流监听

4. **StepEngineBuilder** - Step引擎构建器
   - Builder模式构建引擎
   - 支持动态插入步骤（addAfter/addBefore）
   - 冲突检测和有向无环图验证

5. **StepApi** - Step API入口
   - 提供创建引擎的便捷方法

6. **BaseStep** - 步骤基类
   - 提供通用功能和便捷方法
   - 日志、状态管理等

## 核心特性

### ✅ 已实现的功能

#### 1. 多步骤管理
- ✅ 支持添加任意数量的步骤
- ✅ 步骤按顺序执行
- ✅ 支持跳过不可用的步骤（isAvailable）

#### 2. 动态步骤插入
- ✅ 支持在任意步骤之后插入（addAfter）
- ✅ 支持在任意步骤之前插入（addBefore）
- ✅ 防止多个步骤插入同一位置（冲突检测）
- ✅ 确保步骤流程是有向无环的（DAG验证）

#### 3. 步骤变化监听
- ✅ 提供StateFlow数据流
- ✅ 支持转换为LiveData
- ✅ 包含详细的步骤变化信息（当前步骤、上一步、索引、总数、变化类型）

#### 4. 完整的步骤生命周期
- ✅ **isAvailable()** - 步骤是否可用
- ✅ **onStepStarted()** - 步骤开始
- ✅ **onStepResumed()** - 步骤恢复（navigateBack时）
- ✅ **onStepStopped()** - 步骤停止
- ✅ **cleanup()** - 资源清理

#### 5. 流程控制
- ✅ **finish()** - 完成当前步骤，进入下一步
- ✅ **navigateBack()** - 返回上一步
- ✅ **abortStep()** - 中止整个流程

#### 6. 其他特性
- ✅ 执行历史栈管理（支持多层返回）
- ✅ 错误处理和异常提示
- ✅ DSL风格API支持
- ✅ Kotlin扩展函数支持

## 快速开始

### 基本使用

```kotlin
// 1. 创建Step引擎
val engine = StepApi.createStepEngineBuilder()
    .addStep(Step1())
    .addStep(Step2())
    .addStep(Step3())
    .build()

// 2. 监听步骤变化
lifecycleScope.launch {
    engine.getStepChangeFlow().collect { stepChange ->
        stepChange?.let { 
            // 处理步骤变化
            handleStep(it)
        }
    }
}

// 3. 启动Step流程
engine.start()
```

### 自定义步骤

```kotlin
class MyCustomStep : BaseStep() {
    
    override fun getStepId(): String = "MyCustomStep"
    
    /**
     * ⭐ 重写 isAvailable() 控制步骤是否执行
     * 返回 false 时，此步骤会被跳过
     */
    override fun isAvailable(): Boolean {
        // 根据条件决定是否显示此步骤
        val shouldShow = checkSomeCondition()
        logI("isAvailable: $shouldShow")
        return shouldShow
    }
    
    override fun onStepStarted(stepCompletionProvider: MyStepCompletionProvider) {
        super.onStepStarted(stepCompletionProvider)
        
        // 执行业务逻辑
        doSomething { success ->
            if (success) {
                finish()  // 完成当前步骤
            } else {
                navigateBack()  // 返回上一步
                // 或者 abortStep()  // 中止流程
            }
        }
    }
    
    override fun onStepStopped() {
        super.onStepStopped()
        // 步骤停止时的清理工作
    }
    
    override fun cleanup() {
        super.cleanup()
        // 最终清理工作
    }
    
    private fun checkSomeCondition(): Boolean {
        // 实际的条件判断逻辑
        return true
    }
}
```

### 条件步骤（isAvailable 的使用）

`isAvailable()` 方法用于控制步骤是否执行。返回 `false` 时，步骤会被自动跳过。

```kotlin
// 示例1: 只在首次设置时执行
class WelcomeStep(private val isFirstTime: Boolean) : BaseStep() {
    override fun isAvailable() = isFirstTime
    override fun getStepId() = "WelcomeStep"
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        showWelcome()
        finish()
    }
}

// 示例2: 根据系统版本决定
class ModernFeatureStep : BaseStep() {
    override fun isAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    override fun getStepId() = "ModernFeatureStep"
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        useModernApi()
        finish()
    }
}

// 示例3: 使用构造函数传入条件
class ConditionalStep(
    private val condition: () -> Boolean
) : BaseStep() {
    override fun isAvailable() = condition()
    override fun getStepId() = "ConditionalStep"
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        doWork()
        finish()
    }
}

// 使用条件步骤
val engine = StepApi.createStepEngineBuilder()
    .addStep(WelcomeStep(isFirstTime = true))  // 会执行
    .addStep(WelcomeStep(isFirstTime = false)) // 会跳过
    .addStep(ModernFeatureStep())               // Android 12+ 才执行
    .addStep(ConditionalStep { needsUpdate() }) // 动态条件
    .build()
```

**重要提示**:
- ⚠️ `isAvailable()` 可能被多次调用，不要在其中执行耗时操作
- ⚠️ 保持判断逻辑简单快速
- ✅ 添加日志便于调试
- ✅ 参考 `IsAvailableGuide.md` 获取更多示例

### 动态插入步骤

```kotlin
val engine = StepApi.createStepEngineBuilder()
    .addStep(WelcomeStep())
    .addStep(ConnectionStep())
    .addStep(CompleteStep())
    // 在WelcomeStep之后插入权限步骤
    .addStepAfter<WelcomeStep>(PermissionStep())
    // 在CompleteStep之前插入配置步骤
    .addStepBefore<CompleteStep>(ConfigStep())
    .build()
```

### DSL风格

```kotlin
val engine = StepApi.createStepEngineBuilder {
    step(WelcomeStep())
    step(PermissionStep())
    step(ConnectionStep())
    stepAfter<WelcomeStep>(ExtraStep())
    step(CompleteStep())
}.build()
```

## 与Google Step SDK的对比

| 特性 | Google Step SDK | Step Framework |
|------|------------------|-------------------|
| 步骤管理 | ✅ | ✅ |
| 动态插入 | ✅ addAfter/addBefore | ✅ addStepAfter/addStepBefore |
| 冲突检测 | ✅ | ✅ 更严格的检测 |
| 步骤生命周期 | ✅ 完整生命周期 | ✅ 完整生命周期 + onStepResumed |
| 流程控制 | ✅ finish/navigateBack/abort | ✅ finish/navigateBack/abortStep |
| 数据流监听 | ✅ Flow | ✅ StateFlow (更强大) |
| 执行历史 | ✅ | ✅ 支持多层返回 |
| DSL支持 | ❌ | ✅ |
| 依赖性 | ❌ 依赖Google库 | ✅ 完全独立 |

## 设计模式

1. **Builder模式** - StepEngineBuilder
2. **责任链模式** - 步骤按顺序执行
3. **观察者模式** - StateFlow/LiveData监听
4. **状态机模式** - 步骤状态管理
5. **策略模式** - 每个步骤是独立的策略

## 最佳实践

### 1. 步骤应该短小精悍
每个步骤只做一件事，保持单一职责原则。

### 2. 合理使用isAvailable
不要在isAvailable中执行耗时操作，它可能被多次调用。

### 3. 正确清理资源
在cleanup()中释放所有资源，避免内存泄漏。

### 4. 避免插入冲突
多个模块协作时，使用不同的插入位置，或者协调好插入顺序。

### 5. 善用navigateBack
支持用户返回操作，提升用户体验。

### 6. 使用有意义的StepId
便于调试和日志追踪。

## 高级用法

### 条件步骤

```kotlin
class ConditionalStep(
    private val condition: () -> Boolean
) : BaseStep() {
    override fun isAvailable(): Boolean = condition()
}

// 使用
val engine = MyStepApi.createStepEngineBuilder()
    .addStep(ConditionalStep { needPermission })
    .build()
```

### 步骤间数据传递

```kotlin
// 使用单例或ViewModel在步骤间共享数据
object StepDataHolder {
    var deviceMac: String? = null
    var userName: String? = null
}

class Step1 : BaseMyStep() {
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        StepDataHolder.deviceMac = "00:11:22:33:44:55"
        finish()
    }
}

class Step2 : BaseMyStep() {
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        val mac = StepDataHolder.deviceMac
        // 使用数据
    }
}
```

### 异步操作

```kotlin
class AsyncStep : BaseMyStep() {
    override fun onStepStarted(provider: MyStepCompletionProvider) {
        super.onStepStarted(provider)
        
        // 使用协程执行异步操作
        lifecycleScope.launch {
            try {
                val result = performAsyncOperation()
                if (result) {
                    finish()
                } else {
                    abortStep()
                }
            } catch (e: Exception) {
                logE("操作失败: ${e.message}")
                abortStep(fromUser = false)
            }
        }
    }
}
```

## 完整示例

参见 `samples/UsageExample.kt` 文件，包含了框架的所有使用方式。

## 测试

建议为自定义步骤编写单元测试：

```kotlin
@Test
fun testCustomStep() {
    val step = MyCustomStep()
    val mockProvider = mock<MyStepCompletionProvider>()
    
    step.onStepStarted(mockProvider)
    
    // 验证行为
    verify(mockProvider).finish()
}
```

## 注意事项

1. ⚠️ Step引擎一旦启动就不能重新启动，需要重新创建
2. ⚠️ 步骤的isAvailable在流程中可能被多次调用
3. ⚠️ 在Activity/Fragment中使用时注意生命周期管理
4. ⚠️ 不要在步骤中保存Activity/Context的强引用，避免内存泄漏
5. ⚠️ 插入步骤时要确保目标步骤存在，否则会抛出异常

## 常见问题

### Q: 如何在步骤间传递数据？
A: 使用ViewModel、单例或者构造函数参数传递数据。

### Q: 如何支持多层返回？
A: 框架自动维护执行历史栈，多次调用navigateBack即可。

### Q: 如何处理异步操作？
A: 在步骤中使用协程或回调，操作完成后调用finish()。

### Q: 插入冲突如何解决？
A: 选择不同的插入位置，或者设置allowConflict=true（不推荐）。

## 版本历史

- v1.0.0 - 初始版本，实现所有核心功能

## License

Copyright (C) 2023 OPPO Mobile Comm Corp., Ltd.


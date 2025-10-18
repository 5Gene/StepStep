# StepStep Framework 架构设计文档

## 1. 架构概览

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        StepStep Framework                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐      ┌──────────────────┐                     │
│  │  StepApi │─────▶│StepEngineBuilder│                    │
│  └─────────────┘      └────────┬─────────┘                     │
│        │                        │                                │
│        │                        │ addStep()                      │
│        │                        │ addStepAfter()                 │
│        │                        │ addStepBefore()                │
│        │                        │                                │
│        │                        ▼                                │
│        │               ┌────────────────┐                       │
│        │               │ StepEngine  │                       │
│        │               └────────┬───────┘                       │
│        │                        │                                │
│        │                        │ start()                        │
│        │                        │ abort()                        │
│        │                        │ getCurrentStep()               │
│        │                        │ getStepChangeFlow()            │
│        │                        │                                │
│        │                        ▼                                │
│        │         ┌──────────────────────────────┐              │
│        │         │      Step Execution          │              │
│        │         │   ┌──────────────────────┐   │              │
│        │         │   │  StepStep (IF)    │   │              │
│        │         │   ├──────────────────────┤   │              │
│        │         │   │ isAvailable()        │   │              │
│        │         │   │ onStepStarted()      │   │              │
│        │         │   │ onStepResumed()      │   │              │
│        │         │   │ onStepStopped()      │   │              │
│        │         │   │ cleanup()            │   │              │
│        │         │   └──────────┬───────────┘   │              │
│        │         │              │                │              │
│        │         │              ▼                │              │
│        │         │   ┌──────────────────────┐   │              │
│        │         │   │ MyStepCompletionProv│   │              │
│        │         │   ├──────────────────────┤   │              │
│        │         │   │ finish()             │   │              │
│        │         │   │ navigateBack()       │   │              │
│        │         │   │ abortStep()         │   │              │
│        │         │   └──────────────────────┘   │              │
│        │         └──────────────────────────────┘              │
│        │                        │                                │
│        │                        ▼                                │
│        │         ┌──────────────────────────────┐              │
│        │         │ StateFlow<StepStepChange> │              │
│        │         └──────────────┬───────────────┘              │
│        │                        │                                │
│        │                        ▼                                │
│        │         ┌──────────────────────────────┐              │
│        │         │    Observer / Collector      │              │
│        │         └──────────────────────────────┘              │
│        │                                                         │
└────────┴─────────────────────────────────────────────────────────┘
```

### 1.2 类关系图

```
StepApi (Object)
    │
    └─── creates ──▶ StepEngineBuilder
                          │
                          │ builds
                          ▼
                     StepEngine
                          │
                          │ manages
                          ▼
                     List<StepStep> ◀────── implements ────┐
                          │                                     │
                          │ provides                            │
                          ▼                                     │
                  MyStepCompletionProvider                BaseStep
                          │                                     │
                          │ controls                            │
                          ▼                                     │
                     StepStepChange                          │
                          │                                     │
                          └──── observes ──▶ External Observer │
                                                                │
                                            Custom Steps ───────┘
                                            (WelcomeStep, etc.)
```

## 2. 核心组件详解

### 2.1 StepApi

**职责**: 框架的入口点，提供创建Step引擎的API

**设计模式**: Singleton（Object）

**主要方法**:
- `createStepEngineBuilder()`: 创建引擎构建器
- `createStepEngineBuilder(steps)`: 创建带初始步骤的构建器

**使用场景**: 所有Step流程的起点

```kotlin
val engine = StepApi.createStepEngineBuilder()
    .addStep(Step1())
    .build()
```

### 2.2 MyStepEngineBuilder

**职责**: 使用Builder模式构建Step引擎，管理步骤的添加和顺序

**设计模式**: Builder Pattern

**主要功能**:
1. **步骤添加**: addStep()
2. **动态插入**: addStepAfter(), addStepBefore()
3. **冲突检测**: 防止多个步骤插入同一位置
4. **DAG构建**: 构建有向无环的步骤流程

**核心算法**:
```
步骤构建算法:
1. 收集所有 End 位置的步骤 → 基础列表
2. 迭代处理所有 Before 插入 → 找到目标位置插入
3. 迭代处理所有 After 插入 → 找到目标位置插入
4. 验证: 检查重复、循环依赖等
```

### 2.3 StepEngine

**职责**: Step引擎核心，管理步骤的执行流程

**设计模式**: 
- State Machine Pattern (状态机)
- Chain of Responsibility Pattern (责任链)

**核心状态**:
- `currentStepIndex`: 当前步骤索引
- `executionStack`: 执行历史栈（支持navigateBack）
- `_stepChangeFlow`: 步骤变化的数据流

**状态转换**:
```
[未开始 -1] 
    │ start()
    ▼
[步骤0] ─finish()→ [步骤1] ─finish()→ [步骤2] ─finish()→ [完成 -1]
    ▲                │                │
    │                │                │
    └───navigateBack()───────────────┘
```

**主要方法**:
- `start()`: 启动流程
- `abort()`: 中止流程
- `getCurrentStep()`: 获取当前步骤
- `getStepChangeFlow()`: 获取数据流
- `finishCurrentStep()`: 完成当前步骤（内部）
- `navigateBackToPreviousStep()`: 返回上一步（内部）

### 2.4 Step（接口）

**职责**: 定义步骤的契约，规定步骤的生命周期

**设计模式**: Strategy Pattern (策略模式)

**生命周期**:
```
isAvailable() → true
    │
    ▼
onStepStarted(provider)
    │
    ├─────────────────────┐
    │                     │
    │ 用户操作            │ navigateBack()
    │                     │
    ▼                     ▼
onStepStopped()    onStepResumed(provider)
    │                     │
    │                     ▼
    │              onStepStopped()
    │                     │
    └─────────┬───────────┘
              ▼
          cleanup()
```

**方法说明**:
- `isAvailable()`: 步骤是否可用（条件步骤）
- `onStepStarted()`: 步骤首次启动
- `onStepResumed()`: 步骤恢复（从后续步骤返回）
- `onStepStopped()`: 步骤停止（暂停，可能恢复）
- `cleanup()`: 最终清理（不会再恢复）

### 2.5 MyStepCompletionProvider（接口）

**职责**: 提供步骤流转控制能力

**设计模式**: Command Pattern (命令模式)

**方法**:
- `finish()`: 完成当前步骤 → 进入下一步
- `navigateBack()`: 返回上一步 → 恢复上一步
- `abortStep()`: 中止流程 → 清理所有步骤

**调用效果**:
```
finish():
    currentStep.onStepStopped()
    → nextStep.onStepStarted()
    
navigateBack():
    currentStep.onStepStopped()
    → previousStep.onStepResumed()
    
abortStep():
    allSteps.forEach { it.cleanup() }
    → notify ABORTED
```

### 2.6 StepChange（数据类）

**职责**: 封装步骤变化信息

**设计模式**: Data Transfer Object (DTO)

**属性**:
- `currentStep`: 当前步骤
- `previousStep`: 上一个步骤
- `currentIndex`: 当前索引
- `totalSteps`: 总步骤数
- `changeType`: 变化类型

**变化类型**:
```kotlin
enum class ChangeType {
    STARTED,    // 流程开始
    FORWARD,    // 前进
    BACKWARD,   // 后退
    COMPLETED,  // 完成
    ABORTED     // 中止
}
```

### 2.7 BaseMyStep（抽象基类）

**职责**: 提供步骤的通用实现和便捷方法

**设计模式**: Template Method Pattern (模板方法)

**提供的功能**:
- 状态管理: isStepStarted, isStepStopped
- 日志方法: logD(), logI(), logW(), logE()
- 便捷方法: finish(), navigateBack(), abortStep()

## 3. 设计模式应用

### 3.1 Builder Pattern (构建器模式)

**应用**: MyStepEngineBuilder

**优点**:
- 支持链式调用
- 构建过程灵活
- 参数可选，易扩展

```kotlin
val engine = MyStepApi.createStepEngineBuilder()
    .addStep(Step1())
    .addStep(Step2())
    .addStepAfter<Step1>(Step1_5())
    .build()
```

### 3.2 Chain of Responsibility Pattern (责任链模式)

**应用**: MyStepEngine中的步骤执行

**优点**:
- 步骤解耦
- 可动态组合
- 每个步骤独立处理

```
Step1 → Step2 → Step3 → Step4
每个步骤处理自己的逻辑，然后传递给下一个
```

### 3.3 State Machine Pattern (状态机模式)

**应用**: MyStepEngine的状态管理

**状态**:
- 未开始 (-1)
- 执行中 (0 ~ n-1)
- 已完成 (-1, COMPLETED)
- 已中止 (-1, ABORTED)

**转换**:
- start() → 开始执行
- finish() → 进入下一状态
- navigateBack() → 返回上一状态
- abort() → 终止状态

### 3.4 Observer Pattern (观察者模式)

**应用**: StateFlow监听步骤变化

**优点**:
- 解耦业务逻辑和UI
- 支持多个观察者
- 响应式编程

```kotlin
engine.getStepChangeFlow().collect { change ->
    // 响应步骤变化
}
```

### 3.5 Strategy Pattern (策略模式)

**应用**: MyStepStep接口

**优点**:
- 每个步骤是独立策略
- 可随意替换
- 符合开闭原则

```kotlin
interface MyStepStep {
    fun onStepStarted(provider)
    // 每个实现类有不同的策略
}
```

### 3.6 Template Method Pattern (模板方法模式)

**应用**: BaseMyStep抽象类

**优点**:
- 定义骨架，子类填充
- 复用通用逻辑
- 强制执行流程

```kotlin
abstract class BaseMyStep {
    // 模板方法
    final fun onStepStarted(provider) {
        super.onStepStarted(provider)
        // 子类可重写具体逻辑
    }
}
```

### 3.7 Command Pattern (命令模式)

**应用**: MyStepCompletionProvider

**优点**:
- 将请求封装为对象
- 解耦调用者和接收者
- 支持撤销操作（navigateBack）

```kotlin
provider.finish()      // 命令：完成
provider.navigateBack() // 命令：返回
provider.abortStep()   // 命令：中止
```

## 4. 数据流设计

### 4.1 步骤执行流

```
Start
  │
  ▼
Find Next Available Step
  │
  ├─ Not Found → Completed
  │
  ▼
Check isAvailable()
  │
  ├─ false → Skip, Find Next
  │
  ▼
onStepStarted()
  │
  ▼
User Interaction
  │
  ├─ finish() → Find Next Available Step
  ├─ navigateBack() → Pop from Stack, onStepResumed()
  └─ abortStep() → cleanup() all steps → Aborted
```

### 4.2 执行历史栈

```
步骤序列: [Step0, Step1, Step2, Step3, Step4]

执行过程:
Start
  ↓
Step0 (stack: [])
  ↓ finish()
Step1 (stack: [0])
  ↓ finish()
Step2 (stack: [0, 1])
  ↓ finish()
Step3 (stack: [0, 1, 2])
  ↓ navigateBack()
Step2 (stack: [0, 1])  ← onStepResumed()
  ↓ navigateBack()
Step1 (stack: [0])     ← onStepResumed()
  ↓ finish()
Step2 (stack: [0, 1])  ← onStepStarted() again
```

### 4.3 StateFlow数据流

```
MyStepEngine
    │
    │ emits
    ▼
StateFlow<MyStepStepChange?>
    │
    ├──▶ Collector 1 (UI)
    ├──▶ Collector 2 (Logger)
    └──▶ Collector 3 (Analytics)
```

**StateFlow特性**:
- Hot Flow: 始终活跃
- 保持最新值: 新订阅者立即收到当前值
- 线程安全: 支持多个收集者

## 5. 冲突检测与DAG保证

### 5.1 插入冲突检测

**问题**: 多个步骤插入同一位置会导致顺序不确定

**解决方案**:
```kotlin
private val insertionTracker = mutableMapOf<String, Int>()

fun checkInsertionConflict(positionKey: String, allowConflict: Boolean) {
    val count = insertionTracker.getOrDefault(positionKey, 0)
    if (!allowConflict && count > 0) {
        throw IllegalArgumentException("插入位置冲突")
    }
    insertionTracker[positionKey] = count + 1
}
```

**位置标识**:
- `StepClassName_after`: 在该步骤后插入
- `StepClassName_before`: 在该步骤前插入

### 5.2 有向无环图(DAG)保证

**问题**: 循环依赖会导致死循环

**当前保证**:
1. 只允许在已存在步骤前后插入
2. 构建时验证目标步骤存在
3. 检测重复步骤

**示例**:
```
✅ 有效的DAG:
Step1 → Step2 → Step3
         ↓
       Step2.5

❌ 循环依赖:
Step1 → Step2 → Step3
  ↑               ↓
  └───────────────┘
```

## 6. 扩展性设计

### 6.1 如何添加新功能

1. **扩展MyStepStep**: 实现新的生命周期方法
2. **扩展BaseMyStep**: 添加通用工具方法
3. **扩展MyStepEngine**: 添加新的控制方法
4. **添加扩展函数**: 在MyStepExtensions.kt中添加

### 6.2 扩展点

```kotlin
// 1. 自定义步骤类型
interface MyStepStep {
    fun getStepType(): StepType  // 可扩展
}

// 2. 自定义变化类型
enum class ChangeType {
    STARTED, FORWARD, BACKWARD, COMPLETED, ABORTED,
    // 可添加新类型
}

// 3. 自定义监听器
interface StepLifecycleListener {
    fun onStepStarted()
    fun onStepCompleted()
    // ...
}
```

## 7. 性能考虑

### 7.1 时间复杂度

- `addStep()`: O(1)
- `addStepAfter/Before()`: O(1)
- `build()`: O(n²) worst case (多次插入)
- `start()`: O(n) (查找第一个可用步骤)
- `finish()`: O(n) (查找下一个可用步骤)
- `navigateBack()`: O(1)

### 7.2 空间复杂度

- 步骤列表: O(n)
- 执行历史栈: O(n) worst case
- StateFlow: O(1) (只保存最新值)

### 7.3 优化建议

1. 步骤数量建议不超过20个
2. isAvailable()应该快速返回，避免耗时操作
3. 使用StateFlow而非SharedFlow，减少内存占用

## 8. 线程安全

### 8.1 当前实现

- MyStepEngine: 非线程安全，应在主线程使用
- StateFlow: 线程安全

### 8.2 建议

在Android中：
- 创建和操作Engine: 主线程
- 收集StateFlow: 任意线程
- 步骤的业务逻辑: 可使用协程切换线程

## 9. 总结

MyStep Framework 是一个设计优雅、功能完善的Step流程管理框架：

**核心优势**:
1. ✅ 完整的步骤生命周期管理
2. ✅ 灵活的动态插入能力
3. ✅ 强大的冲突检测机制
4. ✅ 现代化的响应式API (StateFlow)
5. ✅ 丰富的设计模式应用
6. ✅ 良好的扩展性

**适用场景**:
- 设备配对流程
- 用户引导流程
- 向导式表单
- 多步骤配置
- 任何需要流程管理的场景

**设计原则**:
- 单一职责原则
- 开闭原则
- 里氏替换原则
- 接口隔离原则
- 依赖倒置原则


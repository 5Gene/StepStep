# Step Framework - 可视化指南

## 📊 框架整体结构

```
┌─────────────────────────────────────────────────────────────────┐
│                      Step Framework                           │
│                    (Step流程管理框架)                            │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
                ▼               ▼               ▼
         ┌──────────┐    ┌──────────┐    ┌──────────┐
         │   API    │    │  Engine  │    │  Steps   │
         │  层       │    │   层     │    │   层     │
         └──────────┘    └──────────┘    └──────────┘
```

## 🎯 核心工作流程

### 1. 创建阶段

```
开发者
  │
  │ 调用
  ▼
StepApi.createStepEngineBuilder()
  │
  │ 返回
  ▼
StepEngineBuilder
  │
  │ addStep()
  │ addStepAfter()
  │ addStepBefore()
  │
  │ build()
  ▼
StepEngine (准备就绪)
```

### 2. 执行阶段

```
开发者调用 engine.start()
         │
         ▼
   ┌─────────────┐
   │  找第一个   │
   │  可用步骤   │
   └──────┬──────┘
          │
          ▼
   ┌─────────────────────────────┐
   │  执行步骤生命周期           │
   ├─────────────────────────────┤
   │  1. isAvailable() → true?   │
   │     ├─ true → 继续          │
   │     └─ false → 跳过         │
   │                              │
   │  2. onStepStarted(provider) │
   │     │                        │
   │     │ 步骤执行业务逻辑       │
   │     │                        │
   │     └─ 调用 provider方法    │
   └─────────────┬───────────────┘
                 │
     ┌───────────┼───────────────┐
     │           │               │
     ▼           ▼               ▼
  finish()  navigateBack()  abortStep()
     │           │               │
     │           │               │
     ▼           ▼               ▼
   下一步       上一步          结束
```

### 3. 步骤生命周期详细流程

```
步骤A (未开始)
     │
     │ start() or finish()
     ▼
isAvailable() 检查
     │
     ├─ false ─────────────────┐
     │                          │
     │ true                     │ 跳过该步骤
     ▼                          │
onStepStarted(provider) ◀──────┘ 检查下一个
     │                          
     │ 步骤执行中...            
     │                          
     ├──────────────┬───────────┐
     │              │           │
     ▼              ▼           ▼
  finish()    navigateBack() abortStep()
     │              │           │
     ▼              ▼           ▼
onStepStopped() onStepStopped() cleanup()
     │              │           │
     ▼              ▼           │
  下一步A+1       上一步        │
     │              │           │
     │              ▼           │
     │       onStepResumed()    │
     │              │           │
     │              │           │
     └──────┬───────┘           │
            │                   │
            ▼                   ▼
        继续流程            流程终止
```

## 🔄 步骤插入机制

### 基本插入

```
初始流程:
┌──────┐    ┌──────┐    ┌──────┐
│ A    │ → │ B    │ → │ C    │
└──────┘    └──────┘    └──────┘

addStepAfter<A>(X):
┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐
│ A    │ → │ X    │ → │ B    │ → │ C    │
└──────┘    └──────┘    └──────┘    └──────┘

addStepBefore<C>(Y):
┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐
│ A    │ → │ X    │ → │ B    │ → │ Y    │ → │ C    │
└──────┘    └──────┘    └──────┘    └──────┘    └──────┘
```

### 冲突检测

```
✅ 允许的插入:
┌──────┐    ┌──────┐
│ A    │ → │ B    │
└──────┘    └──────┘

addStepAfter<A>(X1):
┌──────┐    ┌──────┐    ┌──────┐
│ A    │ → │ X1   │ → │ B    │
└──────┘    └──────┘    └──────┘

addStepAfter<X1>(X2):  ← 不同位置，OK
┌──────┐    ┌──────┐    ┌──────┐    ┌──────┐
│ A    │ → │ X1   │ → │ X2   │ → │ B    │
└──────┘    └──────┘    └──────┘    └──────┘

❌ 不允许的插入 (默认):
┌──────┐    ┌──────┐
│ A    │ → │ B    │
└──────┘    └──────┘

addStepAfter<A>(X1):
addStepAfter<A>(X2):  ← 同一位置，冲突！抛出异常
```

## 📈 数据流向

```
StepEngine
     │
     │ 步骤变化
     ▼
StateFlow<StepStepChange>
     │
     │ emit(变化)
     ▼
┌─────────────────────────────────┐
│      StepStepChange          │
├─────────────────────────────────┤
│  currentStep: StepStep?      │
│  previousStep: StepStep?     │
│  currentIndex: Int              │
│  totalSteps: Int                │
│  changeType: ChangeType         │
└───────────┬─────────────────────┘
            │
            │ collect / observe
            ▼
┌───────────────────────────────────┐
│        观察者 (Observers)          │
├───────────────────────────────────┤
│  • UI Layer (更新界面)            │
│  • Logger (记录日志)              │
│  • Analytics (数据分析)           │
│  • Navigator (页面导航)           │
└───────────────────────────────────┘
```

## 🎭 步骤状态机

```
         ┌──────────────┐
    ┌────│  未开始 (-1) │
    │    └──────────────┘
    │            │
    │  start()   │
    │            ▼
    │    ┌──────────────┐
    │    │   步骤 0     │◀─┐
    │    └──────────────┘  │
    │            │          │
    │  finish()  │          │ navigateBack()
    │            ▼          │
    │    ┌──────────────┐  │
    │    │   步骤 1     │──┘
    │    └──────────────┘
    │            │
    │  finish()  │
    │            ▼
    │    ┌──────────────┐
    │    │   步骤 2     │
    │    └──────────────┘
    │            │
    │  finish()  │
    │            ▼
    │    ┌──────────────┐
    │    │  已完成 (-1) │
    │    └──────────────┘
    │
    │ abort()
    │
    └─▶  ┌──────────────┐
         │  已中止 (-1) │
         └──────────────┘
```

## 🧩 类关系图

```
┌─────────────────┐
│  StepApi     │
│   (Object)      │
└────────┬────────┘
         │ creates
         ▼
┌─────────────────────┐
│StepEngineBuilder │
│                     │
│ + addStep()         │
│ + addStepAfter()    │
│ + addStepBefore()   │
│ + build()           │
└────────┬────────────┘
         │ builds
         ▼
┌─────────────────────┐         ┌──────────────────┐
│  StepEngine      │────────▶│  StepStep     │
│                     │ manages │   (interface)    │
│ + start()           │         │                  │
│ + abort()           │         │ + isAvailable()  │
│ + getCurrentStep()  │         │ + onStepStarted()│
│ + getStepChangeFlow()         │ + onStepResumed()│
└────────┬────────────┘         │ + onStepStopped()│
         │                      │ + cleanup()      │
         │ emits                └────────▲─────────┘
         ▼                               │
┌──────────────────────┐                │ implements
│ StepStepChange    │                │
│   (data class)       │         ┌──────┴──────────┐
│                      │         │  BaseMyStep    │
│ + currentStep        │         │  (abstract)     │
│ + previousStep       │         │                 │
│ + currentIndex       │         │ + finish()      │
│ + totalSteps         │         │ + navigateBack()│
│ + changeType         │         │ + abortStep()  │
└──────────────────────┘         │ + logX()        │
         │                       └─────────────────┘
         │ observes                      ▲
         ▼                               │ extends
┌──────────────────────┐                │
│   External           │         ┌──────┴──────────┐
│   Observers          │         │  Custom Steps   │
│                      │         │  (业务步骤)     │
│ • UI                 │         │                 │
│ • Logger             │         │ • WelcomeStep   │
│ • Analytics          │         │ • PermissionStep│
└──────────────────────┘         │ • ConnectionStep│
                                 │ • ...           │
                                 └─────────────────┘
```

## 🔧 使用流程图

### 场景1: 简单的Step流程

```
开发者                    MyStep Framework
  │                             │
  │ 1. 创建引擎                 │
  ├─────────────────────────────▶
  │   MyStepApi.create...      │
  │                             │
  │ 2. 添加步骤                 │
  ├─────────────────────────────▶
  │   .addStep(Step1)           │
  │   .addStep(Step2)           │
  │                             │
  │ 3. 构建                     │
  ├─────────────────────────────▶
  │   .build()                  │
  │                             │
  │ 4. 监听变化                 │
  ├─────────────────────────────▶
  │   .getStepChangeFlow()      │
  │                             │
  │ 5. 启动                     │
  ├─────────────────────────────▶
  │   .start()                  │
  │                             │
  │                        执行Step1
  │                             │
  │ ◀─────────────────────────┤
  │   通知: Step1 Started      │
  │                             │
  │                        Step1完成
  │                             │
  │ ◀─────────────────────────┤
  │   通知: Step2 Started      │
  │                             │
  │                        Step2完成
  │                             │
  │ ◀─────────────────────────┤
  │   通知: Completed          │
  │                             │
```

### 场景2: 动态插入步骤

```
基础步骤: [A, B, C]
         │
         │ addStepAfter<A>(X)
         ▼
     [A, X, B, C]
         │
         │ addStepBefore<C>(Y)
         ▼
    [A, X, B, Y, C]
         │
         │ build()
         ▼
    最终步骤序列
```

### 场景3: 返回上一步

```
当前在Step3
    │
    │ 用户点击"返回"
    ▼
navigateBack()
    │
    ├─ Step3.onStepStopped()
    │
    ├─ 从历史栈pop出Step2的索引
    │
    └─ Step2.onStepResumed()
        │
        └─ 用户可以重新操作Step2
```

## 📱 实际使用示例流程

### 设备配对流程

```
┌─────────────────────────────────────────────────────────────┐
│                      设备配对Step流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [欢迎页面]  → [权限申请] → [设备搜索] → [设备连接]          │
│      ↓            ↓           ↓            ↓                 │
│  WelcomeStep  PermissionStep SearchStep ConnectionStep      │
│                                                               │
│  → [配置同步] → [完成页面]                                   │
│       ↓            ↓                                          │
│   ConfigStep   CompleteStep                                  │
│                                                               │
└─────────────────────────────────────────────────────────────┘

代码实现:
val engine = MyStepApi.createStepEngineBuilder()
    .addStep(WelcomeStep())
    .addStep(PermissionStep(permissions))
    .addStep(SearchStep())
    .addStep(ConnectionStep(deviceMac))
    .addStep(ConfigStep())
    .addStep(CompleteStep())
    .build()

engine.start()
```

## 🎨 DSL风格对比

### 传统风格

```kotlin
val builder = MyStepApi.createStepEngineBuilder()
builder.addStep(Step1())
builder.addStep(Step2())
builder.addStep(Step3())
val engine = builder.build()
```

### 链式风格

```kotlin
val engine = MyStepApi.createStepEngineBuilder()
    .addStep(Step1())
    .addStep(Step2())
    .addStep(Step3())
    .build()
```

### DSL风格 ⭐

```kotlin
val engine = MyStepApi.createStepEngineBuilder {
    step(Step1())
    step(Step2())
    stepAfter<Step1>(Step1_5())
    step(Step3())
}.build()
```

## 📊 内存和性能

### 内存占用

```
MyStepEngine 实例
├─ steps: List<MyStepStep>        (n个步骤)
├─ executionStack: List<Int>       (最多n个索引)
└─ _stepChangeFlow: StateFlow      (1个对象)

总内存 ≈ O(n) + 常量
```

### 时间复杂度

```
操作                时间复杂度      说明
─────────────────────────────────────────
addStep()           O(1)          直接添加到列表
addStepAfter()      O(1)          记录插入信息
addStepBefore()     O(1)          记录插入信息
build()             O(n²)         构建步骤列表（最坏）
start()             O(n)          查找第一个可用步骤
finish()            O(n)          查找下一个可用步骤
navigateBack()      O(1)          从栈中pop
abort()             O(n)          清理所有步骤
```

## 🎯 设计原则体现

```
┌────────────────────────────────────────────────────┐
│                  SOLID 原则                        │
├────────────────────────────────────────────────────┤
│                                                    │
│  S - 单一职责                                      │
│      MyStepStep: 只负责步骤逻辑                  │
│      MyStepEngine: 只负责流程管理                │
│                                                    │
│  O - 开闭原则                                      │
│      对扩展开放: 可自定义步骤                     │
│      对修改封闭: 不需要修改框架代码               │
│                                                    │
│  L - 里氏替换                                      │
│      BaseMyStep可以替换MyStepStep              │
│                                                    │
│  I - 接口隔离                                      │
│      MyStepStep: 步骤接口                        │
│      MyStepCompletionProvider: 控制接口          │
│                                                    │
│  D - 依赖倒置                                      │
│      依赖抽象接口，不依赖具体实现                 │
│                                                    │
└────────────────────────────────────────────────────┘
```

## 🌟 总结

MyStep Framework 提供了一个清晰、优雅的Step流程管理解决方案：

```
     简单易用          功能强大         扩展性好
        │                │                │
        └────────┬───────┴────────────────┘
                 │
                 ▼
        ┌────────────────┐
        │  MyStep       │
        │  Framework     │
        └────────────────┘
                 │
        ┌────────┼────────┐
        │        │        │
        ▼        ▼        ▼
      API层   Engine层  Steps层
```

**核心价值**:
1. ✅ 100%实现Google Step SDK的所有核心功能
2. ✅ 更现代的Kotlin API设计
3. ✅ 完全独立，无第三方依赖
4. ✅ 详尽的文档和示例
5. ✅ 生产级别的代码质量

**适用场景**:
- 设备配对流程
- 用户引导流程  
- 向导式配置
- 多步骤表单
- 任何需要流程管理的场景

---

🎉 **现在就开始使用MyStep Framework构建你的Step流程吧！**


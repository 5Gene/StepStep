# StepStep API 回调优化实施计划

> **For Claude:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 优化 StepEngine 回调 API，新增 onAbort 回调，修改 abort 方法支持传入原因

**Architecture:** 职责分离设计 - 成功、失败、中止分别使用独立回调，StepChange 移除 ABORTED 类型

**Tech Stack:** Kotlin, Coroutines, StateFlow

---

## Task 1: 修改 StepChange.kt - 移除 ABORTED 类型

**Files:**

- Modify: `stepstep/src/main/java/org/spark/stepstep/StepChange.kt:41-56`

**Step 1: 修改 ChangeType 枚举**

```kotlin
// 将第41-56行的 ChangeType 枚举修改为：
enum class ChangeType {
    /** 流程开始 */
    STARTED,

    /** 前进到下一个步骤 */
    FORWARD,

    /** 返回到上一个步骤 */
    BACKWARD,

    /** 流程完成 */
    COMPLETED
    // ABORTED 已移除，由 onAbort 回调替代
}
```

**Step 2: 验证修改**

检查 StepChange.kt 文件确认 ChangeType 枚举已正确修改

---

## Task 2: 修改 StepCompletionProvider.kt - abortStep 增加 reason 参数

**Files:**

- Modify: `stepstep/src/main/java/org/spark/stepstep/StepCompletionProvider.kt:42`

**Step 1: 修改接口方法**

```kotlin
// 将第42行修改为：
suspend fun abortStep(reason: String, fromUser: Boolean = true)
```

**Step 2: 验证修改**

确认接口方法签名已正确修改

---

## Task 3: 修改 BaseStep.kt - abortStep 增加 reason 参数

**Files:**

- Modify: `stepstep/src/main/java/org/spark/stepstep/BaseStep.kt:143-146`

**Step 1: 修改基类方法**

```kotlin
// 将第143-146行修改为：
protected suspend fun abortStep(reason: String = "", fromUser: Boolean = true) {
    logD("abortStep(reason=$reason, fromUser=$fromUser)")
    stepCompletionProvider.abortStep(reason, fromUser)
}
```

**Step 2: 验证修改**

确认方法签名和内部调用已正确修改

---

## Task 4: 修改 StepEngine.kt - 新增 onAbort 回调

**Files:**

- Modify: `stepstep/src/main/java/org/spark/stepstep/StepEngine.kt`

**Step 1: 新增 onAbort 回调字段**

在第72行后（onError 字段后）添加：

```kotlin
private var onAbort: ((String, Boolean) -> Unit)? = null
```

**Step 2: 新增 onAbort 方法**

在 onStepChange 方法后（第169行后）添加：

```kotlin
/**
 * 设置中止回调
 */
fun onAbort(callback: (reason: String, fromUser: Boolean) -> Unit): StepEngine<T> {
    this.onAbort = callback
    return this
}
```

**Step 3: 修改 abort 方法签名和实现**

将第230行修改为：

```kotlin
suspend fun abort(reason: String, fromUser: Boolean = true) {
```

在 abort 方法内部（第243行后）添加触发回调：

```kotlin
// 触发中止回调
onAbort?.invoke(reason, fromUser)
```

**Step 4: 修改 handleError 方法中的 abort 调用**

将第212行的 `abort(fromUser = false)` 修改为：

```kotlin
abort(reason = "Error: ${throwable.message}", fromUser = false)
```

**Step 5: 修改 StepCompletionProviderImpl 中的 abortStep 实现**

找到第400行附近的 abortStep 实现，修改为：

```kotlin
override suspend fun abortStep(reason: String, fromUser: Boolean) {
    abort(reason, fromUser)
}
```

**Step 6: 验证所有修改**

检查 StepEngine.kt 确认所有改动正确

---

## Task 5: 更新 README.md 文档（可选）

**Files:**

- Modify: `README.md`

**说明:** 如果需要，更新 API 文档说明新的回调结构

---

## 验证步骤

完成所有修改后，运行以下验证：

1. 检查代码编译：`./gradlew compileDebugKotlin`
2. 运行单元测试：`./gradlew test`
3. 检查是否有编译错误

---

## 回滚计划

如果出现问题，可以回滚以下文件：

- StepChange.kt - 恢复 ABORTED 枚举
- StepCompletionProvider.kt - 恢复原方法签名
- BaseStep.kt - 恢复原方法签名
- StepEngine.kt - 移除 onAbort 相关代码
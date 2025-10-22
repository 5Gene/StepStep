package org.spark.stepstep

/**
 * Step引擎构建器
 * 
 * 使用Builder模式构建Step引擎
 * 支持在任意位置插入步骤，构建有向无环的步骤流程
 * 支持协程和泛型数据传递
 * 
 * 使用示例：
 * ```
 * val engine = StepEngineBuilder<String>()
 *     .addStep(Step1())
 *     .addStep(Step2())
 *     .addStepAfter(Step1::class.java, Step1_5())
 *     .addStepBefore(Step2::class.java, Step1_8())
 *     .build()
 * ```
 */
class StepEngineBuilder<T> {
    
    /**
     * 内部步骤节点
     * 
     * 用于构建步骤的有向无环图
     */
    private data class StepNode(
        val step: StepStep<T>,
        val insertPosition: InsertPosition = InsertPosition.End
    )
    
    /**
     * 插入位置
     */
    private sealed class InsertPosition {
        /** 追加到末尾 */
        object End : InsertPosition()
        
        /** 插入到指定步骤之后 */
        data class After(val targetStepClass: Class<out StepStep<T>>) : InsertPosition()
        
        /** 插入到指定步骤之前 */
        data class Before(val targetStepClass: Class<out StepStep<T>>) : InsertPosition()
        
        /** 插入到指定ID的步骤之后 */
        data class AfterById(val targetStepId: String) : InsertPosition()
        
        /** 插入到指定ID的步骤之前 */
        data class BeforeById(val targetStepId: String) : InsertPosition()
    }
    
    // 步骤节点列表
    private val stepNodes = mutableListOf<StepNode>()
    
    // 插入位置冲突检测：记录每个位置的插入次数
    private val insertionTracker = mutableMapOf<String, Int>()
    
    /**
     * 添加步骤（追加到末尾）
     * 
     * @param step 要添加的步骤
     * @return Builder本身，支持链式调用
     */
    fun addStep(step: StepStep<T>): StepEngineBuilder<T> {
        stepNodes.add(StepNode(step, InsertPosition.End))
        return this
    }
    
    /**
     * 在指定步骤之后插入步骤
     * 
     * @param targetStepClass 目标步骤的类
     * @param step 要插入的步骤
     * @param allowConflict 是否允许冲突（默认false，不允许多个步骤插入同一位置）
     * @return Builder本身，支持链式调用
     * @throws IllegalArgumentException 如果不允许冲突且检测到冲突
     */
    fun addStepAfter(
        targetStepClass: Class<out StepStep<T>>,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ): StepEngineBuilder<T> {
        val position = InsertPosition.After(targetStepClass)
        checkInsertionConflict(targetStepClass.simpleName + "_after", allowConflict)
        stepNodes.add(StepNode(step, position))
        return this
    }
    
    /**
     * 在指定步骤之前插入步骤
     * 
     * @param targetStepClass 目标步骤的类
     * @param step 要插入的步骤
     * @param allowConflict 是否允许冲突（默认false，不允许多个步骤插入同一位置）
     * @return Builder本身，支持链式调用
     * @throws IllegalArgumentException 如果不允许冲突且检测到冲突
     */
    fun addStepBefore(
        targetStepClass: Class<out StepStep<T>>,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ): StepEngineBuilder<T> {
        val position = InsertPosition.Before(targetStepClass)
        checkInsertionConflict(targetStepClass.simpleName + "_before", allowConflict)
        stepNodes.add(StepNode(step, position))
        return this
    }
    
    /**
     * 在指定ID的步骤之后插入步骤
     * 
     * @param targetStepId 目标步骤的ID
     * @param step 要插入的步骤
     * @param allowConflict 是否允许冲突（默认false，不允许多个步骤插入同一位置）
     * @return Builder本身，支持链式调用
     * @throws IllegalArgumentException 如果不允许冲突且检测到冲突
     */
    fun addStepAfter(
        targetStepId: String,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ): StepEngineBuilder<T> {
        val position = InsertPosition.AfterById(targetStepId)
        checkInsertionConflict(targetStepId + "_after", allowConflict)
        stepNodes.add(StepNode(step, position))
        return this
    }
    
    /**
     * 在指定ID的步骤之前插入步骤
     * 
     * @param targetStepId 目标步骤的ID
     * @param step 要插入的步骤
     * @param allowConflict 是否允许冲突（默认false，不允许多个步骤插入同一位置）
     * @return Builder本身，支持链式调用
     * @throws IllegalArgumentException 如果不允许冲突且检测到冲突
     */
    fun addStepBefore(
        targetStepId: String,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ): StepEngineBuilder<T> {
        val position = InsertPosition.BeforeById(targetStepId)
        checkInsertionConflict(targetStepId + "_before", allowConflict)
        stepNodes.add(StepNode(step, position))
        return this
    }
    
    /**
     * 添加多个步骤
     * 
     * @param steps 要添加的步骤列表
     * @return Builder本身，支持链式调用
     */
    fun addSteps(vararg steps: StepStep<T>): StepEngineBuilder<T> {
        steps.forEach { addStep(it) }
        return this
    }
    
    /**
     * 添加多个步骤
     * 
     * @param steps 要添加的步骤列表
     * @return Builder本身，支持链式调用
     */
    fun addSteps(steps: List<StepStep<T>>): StepEngineBuilder<T> {
        steps.forEach { addStep(it) }
        return this
    }
    
    /**
     * 在指定ID的步骤之后插入多个步骤
     */
    fun addStepsAfter(targetStepId: String, vararg steps: StepStep<T>): StepEngineBuilder<T> {
        steps.forEach { addStepAfter(targetStepId, it) }
        return this
    }
    
    /**
     * 在指定ID的步骤之前插入多个步骤
     */
    fun addStepsBefore(targetStepId: String, vararg steps: StepStep<T>): StepEngineBuilder<T> {
        steps.forEach { addStepBefore(targetStepId, it) }
        return this
    }
    
    /**
     * 检查插入位置冲突
     * 
     * @param positionKey 位置标识
     * @param allowConflict 是否允许冲突
     * @throws IllegalArgumentException 如果不允许冲突且检测到冲突
     */
    private fun checkInsertionConflict(positionKey: String, allowConflict: Boolean) {
        val count = insertionTracker.getOrDefault(positionKey, 0)
        if (!allowConflict && count > 0) {
            throw IllegalArgumentException(
                "插入位置冲突：已有步骤插入到 $positionKey 位置。" +
                "请使用不同的插入位置，或设置 allowConflict=true 允许多个步骤插入同一位置。"
            )
        }
        insertionTracker[positionKey] = count + 1
    }
    
    /**
     * 构建Step引擎
     * 
     * @return Step引擎实例
     * @throws IllegalStateException 如果检测到循环依赖
     */
    fun build(): StepEngine<T> {
        val orderedSteps = buildStepList()
        validateStepOrder(orderedSteps)
        return StepEngine(orderedSteps.toMutableList())
    }
    
    /**
     * 构建步骤列表
     * 
     * 根据插入位置构建最终的步骤执行顺序
     * 
     * 算法：
     * 1. 首先添加所有End位置的步骤（构建基础列表）
     * 2. 处理Before插入（从前往后）
     * 3. 处理After插入（从后往前）
     */
    private fun buildStepList(): List<StepStep<T>> {
        // 1. 构建基础列表（End位置的步骤）
        val baseSteps = stepNodes
            .filter { it.insertPosition == InsertPosition.End }
            .map { it.step }
            .toMutableList()
        
        if (baseSteps.isEmpty() && stepNodes.isNotEmpty()) {
            // 所有步骤都是插入的，需要一个基础步骤
            throw IllegalStateException(
                "无法构建步骤列表：所有步骤都需要插入到其他步骤前后，但没有基础步骤。" +
                "请至少使用 addStep() 添加一个基础步骤。"
            )
        }
        
        val result = baseSteps.toMutableList()
        
        // 2. 处理Before插入
        processInsertions(result, stepNodes.filter { 
            it.insertPosition is InsertPosition.Before || it.insertPosition is InsertPosition.BeforeById 
        }, isBefore = true)
        
        // 3. 处理After插入
        processInsertions(result, stepNodes.filter { 
            it.insertPosition is InsertPosition.After || it.insertPosition is InsertPosition.AfterById 
        }, isBefore = false)
        
        return result
    }
    
    /**
     * 处理步骤插入
     */
    private fun processInsertions(
        result: MutableList<StepStep<T>>, 
        nodes: List<StepNode>, 
        isBefore: Boolean
    ) {
        var processed = 0
        while (processed < nodes.size) {
            val processedInThisRound = mutableSetOf<StepNode>()
            
            for (node in nodes) {
                if (node in processedInThisRound) continue
                
                val targetIndex = findTargetIndex(result, node.insertPosition)
                
                if (targetIndex != -1) {
                    val insertIndex = if (isBefore) targetIndex else targetIndex + 1
                    result.add(insertIndex, node.step)
                    processedInThisRound.add(node)
                }
            }
            
            if (processedInThisRound.isEmpty() && processed < nodes.size) {
                // 无法找到目标步骤
                val unprocessed = nodes.filter { it !in processedInThisRound }
                val missingTargets = unprocessed.map { node ->
                    when (val position = node.insertPosition) {
                        is InsertPosition.Before -> position.targetStepClass.simpleName
                        is InsertPosition.BeforeById -> position.targetStepId
                        is InsertPosition.After -> position.targetStepClass.simpleName
                        is InsertPosition.AfterById -> position.targetStepId
                        else -> "unknown"
                    }
                }.distinct().joinToString(", ")
                throw IllegalStateException(
                    "无法找到插入目标步骤：$missingTargets。" +
                    "请确保目标步骤存在，且不存在循环依赖。"
                )
            }
            
            processed += processedInThisRound.size
        }
    }
    
    /**
     * 查找目标步骤的索引
     */
    private fun findTargetIndex(result: List<StepStep<T>>, position: InsertPosition): Int {
        return when (position) {
            is InsertPosition.Before -> {
                result.indexOfFirst { it::class.java == position.targetStepClass }
            }
            is InsertPosition.BeforeById -> {
                result.indexOfFirst { it.getStepId() == position.targetStepId }
            }
            is InsertPosition.After -> {
                result.indexOfFirst { it::class.java == position.targetStepClass }
            }
            is InsertPosition.AfterById -> {
                result.indexOfFirst { it.getStepId() == position.targetStepId }
            }
            else -> -1
        }
    }
    
    /**
     * 验证步骤顺序
     * 
     * 检查是否存在循环依赖等问题
     */
    private fun validateStepOrder(steps: List<StepStep<T>>) {
        // 检查是否有重复的步骤实例
        val stepIds = steps.map { it.getStepId() }
        val duplicates = stepIds.groupingBy { it }.eachCount().filter { it.value > 1 }
        
        if (duplicates.isNotEmpty()) {
            throw IllegalStateException(
                "检测到重复的步骤：${duplicates.keys.joinToString(", ")}。" +
                "每个步骤只能添加一次。"
            )
        }
        
        // 更多验证可以在这里添加
    }
}


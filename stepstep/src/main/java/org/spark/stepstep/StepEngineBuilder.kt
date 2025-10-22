package org.spark.stepstep

/**
 * Step引擎构建器 - 简化版本
 * 
 * 使用Builder模式构建Step引擎
 * 支持在任意位置插入步骤，构建有向无环的步骤流程
 * 支持协程和泛型数据传递
 * 
 * 简化设计：
 * 1. 去掉复杂的StepNode和InsertPosition
 * 2. 使用简单的延迟插入机制
 * 3. 减少不必要的缓存和冲突检测
 * 4. 专注于核心功能：步骤管理和DAG验证
 */
class StepEngineBuilder<T> {
    
    // 步骤列表
    private val steps = mutableListOf<StepStep<T>>()
    
    // 插入操作列表（延迟处理）
    private val insertions = mutableListOf<Insertion<T>>()
    
    /**
     * 插入操作
     */
    private data class Insertion<T>(
        val step: StepStep<T>,
        val targetStepId: String,
        val isAfter: Boolean
    )
    
    /**
     * 添加步骤（追加到末尾）
     */
    fun addStep(step: StepStep<T>): StepEngineBuilder<T> {
        steps.add(step)
        return this
    }
    
    /**
     * 在指定ID的步骤之后插入步骤
     */
    fun addStepAfter(
        targetStepId: String,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ): StepEngineBuilder<T> {
        insertions.add(Insertion(step, targetStepId, true))
        return this
    }
    
    /**
     * 在指定ID的步骤之前插入步骤
     */
    fun addStepBefore(
        targetStepId: String,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ): StepEngineBuilder<T> {
        insertions.add(Insertion(step, targetStepId, false))
        return this
    }
    
    /**
     * 添加多个步骤
     */
    fun addSteps(vararg steps: StepStep<T>): StepEngineBuilder<T> {
        this.steps.addAll(steps)
        return this
    }
    
    fun addSteps(steps: List<StepStep<T>>): StepEngineBuilder<T> {
        this.steps.addAll(steps)
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
     * 构建Step引擎
     */
    fun build(): StepEngine<T> {
        val orderedSteps = buildStepList()
        validateStepOrder(orderedSteps)
        return StepEngine(orderedSteps.toMutableList())
    }
    
    /**
     * 构建步骤列表
     * 
     * 简化算法：
     * 1. 先添加所有基础步骤
     * 2. 处理所有插入操作
     */
    private fun buildStepList(): List<StepStep<T>> {
        val result = steps.toMutableList()
        
        // 处理所有插入操作
        for (insertion in insertions) {
            val targetIndex = result.indexOfFirst { it.getStepId() == insertion.targetStepId }
            if (targetIndex == -1) {
                throw IllegalStateException("无法找到目标步骤：${insertion.targetStepId}")
            }
            
            val insertIndex = if (insertion.isAfter) targetIndex + 1 else targetIndex
            result.add(insertIndex, insertion.step)
        }
        
        return result
    }
    
    /**
     * 验证步骤顺序
     * 
     * 检查是否存在循环依赖等问题
     */
    private fun validateStepOrder(steps: List<StepStep<T>>) {
        // 检查重复步骤
        val stepIds = steps.map { it.getStepId() }
        val duplicateIds = stepIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicateIds.isNotEmpty()) {
            throw IllegalStateException("检测到重复步骤：${duplicateIds.joinToString(", ")}")
        }
        
        // 简化的DAG验证
        validateDAG(steps)
    }
    
    /**
     * 简化的DAG验证
     * 
     * 对于只有十几个步骤的场景，使用简单的验证即可
     */
    private fun validateDAG(steps: List<StepStep<T>>) {
        // 这里可以实现简单的循环检测
        // 由于步骤数量少，可以使用简单的算法
        
        // 检查是否有步骤依赖自己（直接循环）
        for (step in steps) {
            val stepId = step.getStepId()
            // 检查插入操作中是否有循环依赖
            for (insertion in insertions) {
                if (insertion.step.getStepId() == stepId && insertion.targetStepId == stepId) {
                    throw IllegalStateException("检测到循环依赖：步骤 $stepId 依赖自己")
                }
            }
        }
    }
}
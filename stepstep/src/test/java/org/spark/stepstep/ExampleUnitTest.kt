package org.spark.stepstep

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        runBlocking {
            coroutineScope {

            }
//            AdvancedDynamicStepExample.demonstrateAdvancedDynamicSteps()
//            CompleteUsageExample.basicUsageExample()
            StepNavigator.getInstance().startStep("")
        }
    }
}
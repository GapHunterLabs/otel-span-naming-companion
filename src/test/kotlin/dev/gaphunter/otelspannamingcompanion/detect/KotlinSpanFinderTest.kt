package dev.gaphunter.otelspannamingcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinSpanFinderTest : BasePlatformTestCase() {

    fun `test a spanBuilder call with string interpolation is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun process(orderId: String) {
                    val span = tracer.spanBuilder("order ${'$'}orderId").startSpan()
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinSpanFinder.findAll(file).size)
    }

    fun `test a spanBuilder call with a static literal is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun process(orderId: String) {
                    val span = tracer.spanBuilder("process order").startSpan()
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinSpanFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated method call is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun process(orderId: String) {
                    logger.info("order ${'$'}orderId")
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinSpanFinder.findAll(file).isEmpty())
    }
}

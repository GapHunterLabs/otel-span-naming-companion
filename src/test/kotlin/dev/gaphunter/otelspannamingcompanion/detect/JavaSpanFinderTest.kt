package dev.gaphunter.otelspannamingcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaSpanFinderTest : BasePlatformTestCase() {

    fun `test a spanBuilder call with string concatenation is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process(String orderId) {
                    Span span = tracer.spanBuilder("order " + orderId).startSpan();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaSpanFinder.findAll(file).size)
    }

    fun `test a spanBuilder call with a static literal is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process(String orderId) {
                    Span span = tracer.spanBuilder("process order").startSpan();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaSpanFinder.findAll(file).isEmpty())
    }

    fun `test two concatenated string literals with no variable is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process() {
                    Span span = tracer.spanBuilder("process " + "order").startSpan();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaSpanFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated method call is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process(String orderId) {
                    logger.info("order " + orderId);
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaSpanFinder.findAll(file).isEmpty())
    }
}

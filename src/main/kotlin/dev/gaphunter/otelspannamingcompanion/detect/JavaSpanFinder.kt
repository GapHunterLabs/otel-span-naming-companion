package dev.gaphunter.otelspannamingcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiPolyadicExpression
import dev.gaphunter.otelspannamingcompanion.model.SpanHit

/**
 * Finds Java `tracer.spanBuilder(...)`/`tracer.startSpan(...)` calls
 * ([SpanSignals]) whose name argument is a string concatenation
 * (`"order " + orderId`) instead of a static literal -- the classic
 * high-cardinality-span-name footgun flagged by OpenTelemetry's own
 * naming guidance: each unique id/value becomes its own span name in
 * the tracing backend instead of a low-cardinality attribute.
 *
 * **v0.1 scope, stated honestly:** only a direct `+` concatenation in
 * the call's first argument is detected -- a name built earlier and
 * passed in as a single variable (`String name = "order " + id;
 * tracer.spanBuilder(name)`) isn't covered, a real, documented
 * limitation (same class of limitation as other text/PSI-only plugins
 * in this catalog).
 */
object JavaSpanFinder {

    fun findAll(file: PsiFile): List<SpanHit> {
        val hits = mutableListOf<SpanHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(call: PsiMethodCallExpression): SpanHit? {
        val methodName = call.methodExpression.referenceName ?: return null
        if (methodName !in SpanSignals.SPAN_NAME_METHODS) return null

        val firstArg = call.argumentList.expressions.firstOrNull() ?: return null
        if (firstArg is PsiLiteralExpression) return null
        // A plain two-operand `a + b` still surfaces as `PsiPolyadicExpression`
        // at runtime (`PsiBinaryExpression` implements it), so this cast
        // covers both the two-operand and 3+-operand (`a + b + c`) shapes.
        val polyadic = firstArg as? PsiPolyadicExpression ?: return null
        // IElementType.toString() on JavaTokenType.PLUS is "PLUS", not the
        // "+" symbol -- comparing against the literal symbol here always
        // failed silently (confirmed live via a diagnostic test), meaning
        // this inspection never actually fired since it was first built.
        if (polyadic.operationTokenType != JavaTokenType.PLUS) return null
        val operands = polyadic.operands.toList()
        // Must mix a real string literal with a non-literal (dynamic)
        // operand -- otherwise this isn't a "static text + dynamic value"
        // name at all: two literals (`"process " + "order"`) is still a
        // fully static name (legitimately not flagged), and `a + b` where
        // neither is a string literal is unrelated to span naming.
        if (operands.none { it is PsiLiteralExpression && it.value is String }) return null
        if (operands.all { it is PsiLiteralExpression && it.value is String }) return null

        return SpanHit(leafOf(call))
    }

    /** Descends to a real leaf PSI element -- `LineMarkerInfo` must never anchor on a composite node. */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}

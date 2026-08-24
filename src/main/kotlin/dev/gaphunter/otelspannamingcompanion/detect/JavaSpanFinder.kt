package dev.gaphunter.otelspannamingcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
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
        val polyadic = firstArg as? PsiPolyadicExpression ?: return null
        if (polyadic.operationTokenType.toString() != "+") return null
        // At least one operand must be a real string literal -- otherwise
        // this isn't a "static text + dynamic value" name at all (e.g.
        // `a + b` where both are numeric variables, unrelated to span
        // naming).
        if (polyadic.operands.none { it is PsiLiteralExpression && it.value is String }) return null

        return SpanHit(leafOf(firstArg))
    }

    /** Descends to a real leaf PSI element -- `LineMarkerInfo` must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}

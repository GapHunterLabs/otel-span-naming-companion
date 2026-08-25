package dev.gaphunter.otelspannamingcompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.otelspannamingcompanion.model.SpanHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaSpanFinder] -- flags a `$`/`${}` interpolated string template instead of `+` concatenation. */
object KotlinSpanFinder {

    fun findAll(file: PsiFile): List<SpanHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<SpanHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(expression: KtDotQualifiedExpression): SpanHit? {
        val call = expression.selectorExpression as? KtCallExpression ?: return null
        val methodName = call.calleeExpression?.text ?: return null
        if (methodName !in SpanSignals.SPAN_NAME_METHODS) return null

        val firstArg = call.valueArguments.firstOrNull()?.getArgumentExpression() ?: return null
        val template = firstArg as? KtStringTemplateExpression ?: return null
        if (!template.hasInterpolation()) return null

        return SpanHit(leafOf(expression))
    }

    private fun KtStringTemplateExpression.hasInterpolation(): Boolean =
        entries.any { it.text.startsWith("$") }

    /** Descends to a real leaf PSI element -- `LineMarkerInfo` must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}

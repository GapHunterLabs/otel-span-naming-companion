package dev.gaphunter.otelspannamingcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.otelspannamingcompanion.detect.JavaSpanFinder
import dev.gaphunter.otelspannamingcompanion.detect.KotlinSpanFinder
import dev.gaphunter.otelspannamingcompanion.model.SpanHit
import dev.gaphunter.otelspannamingcompanion.review.ReviewPrompt

class HighCardinalitySpanNameLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "High-cardinality OpenTelemetry span name"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaSpanFinder.findAll(file)
            "kotlin" -> KotlinSpanFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.callElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: SpanHit): LineMarkerInfo<PsiElement> {
        val tooltip = "This span name is built from a dynamic value -- each unique value becomes its own span name " +
            "in the tracing backend instead of a low-cardinality attribute (OpenTelemetry naming guidance)"
        return LineMarkerInfo(
            hit.callElement,
            hit.callElement.textRange,
            SpanIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}

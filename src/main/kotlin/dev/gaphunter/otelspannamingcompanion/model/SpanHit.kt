package dev.gaphunter.otelspannamingcompanion.model

import com.intellij.psi.PsiElement

/**
 * One span-creation call (`tracer.spanBuilder(...)`/`tracer.startSpan(...)`)
 * whose name argument is built from a dynamic value (string
 * concatenation or interpolation) instead of a static literal -- the
 * classic high-cardinality-span-name footgun: each unique id/value
 * becomes its own span name in the tracing backend instead of a
 * low-cardinality attribute, per OpenTelemetry's own documented
 * naming guidance.
 */
data class SpanHit(val callElement: PsiElement)
